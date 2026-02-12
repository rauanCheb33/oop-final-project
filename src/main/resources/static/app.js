const state = {
  activeSection: "movies",
  filters: {
    movies: "",
    viewers: "",
    cinemas: "",
  },
  loading: {
    movies: false,
    viewers: false,
    cinemas: false,
  },
  data: {
    movies: [],
    viewers: [],
    cinemas: [],
  },
  modal: null,
  deleteTarget: null,
};

const ENTITY_CONFIG = {
  movies: {
    endpoint: "movies",
    singular: "Movie",
    rowsId: "moviesRows",
    columns: 7,
    searchFields: ["title", "description"],
    fields: [
      { name: "title", label: "Title", type: "text", required: true, placeholder: "Interstellar", full: true },
      { name: "description", label: "Description", as: "textarea", required: true, placeholder: "Sci-fi story...", full: true },
      { name: "durationMinutes", label: "Duration (min)", type: "number", required: true, min: "1", step: "1", defaultValue: "120" },
      { name: "ageRestriction", label: "Age restriction", type: "number", required: true, min: "0", step: "1", defaultValue: "0" },
      { name: "ticketPrice", label: "Ticket price", type: "number", required: true, min: "0", step: "0.01", defaultValue: "2000", full: true },
    ],
    toPayload(raw) {
      return {
        title: raw.title.trim(),
        description: raw.description.trim(),
        durationMinutes: Number(raw.durationMinutes),
        ageRestriction: Number(raw.ageRestriction),
        ticketPrice: Number(raw.ticketPrice),
      };
    },
    validate(payload) {
      if (!payload.title) return "Title is required";
      if (!payload.description) return "Description is required";
      if (!Number.isFinite(payload.durationMinutes) || payload.durationMinutes <= 0) return "Duration must be a positive number";
      if (!Number.isFinite(payload.ageRestriction) || payload.ageRestriction < 0) return "Age restriction cannot be negative";
      if (!Number.isFinite(payload.ticketPrice) || payload.ticketPrice < 0) return "Ticket price cannot be negative";
      return null;
    },
    renderRow(movie) {
      return `
        <tr>
          <td>${movie.id}</td>
          <td><strong>${escapeHtml(movie.title)}</strong></td>
          <td class="muted">${escapeHtml(truncate(movie.description, 92))}</td>
          <td>${Number(movie.durationMinutes)} min</td>
          <td>${Number(movie.ageRestriction)}+</td>
          <td class="align-right">$${formatMoney(movie.ticketPrice)}</td>
          <td class="align-right">${renderRowActions("movies", movie.id)}</td>
        </tr>
      `;
    },
  },
  viewers: {
    endpoint: "viewers",
    singular: "Viewer",
    rowsId: "viewersRows",
    columns: 5,
    searchFields: ["fullName", "email"],
    fields: [
      { name: "fullName", label: "Full name", type: "text", required: true, placeholder: "Aruzhan A.", full: true },
      { name: "age", label: "Age", type: "number", required: true, min: "0", step: "1", defaultValue: "16" },
      { name: "email", label: "Email", type: "email", placeholder: "name@example.com", full: true },
    ],
    toPayload(raw) {
      return {
        fullName: raw.fullName.trim(),
        age: Number(raw.age),
        email: raw.email.trim() || null,
      };
    },
    validate(payload) {
      if (!payload.fullName) return "Full name is required";
      if (!Number.isFinite(payload.age) || payload.age < 0) return "Age cannot be negative";
      return null;
    },
    renderRow(viewer) {
      return `
        <tr>
          <td>${viewer.id}</td>
          <td><strong>${escapeHtml(viewer.fullName)}</strong></td>
          <td>${Number(viewer.age)}</td>
          <td class="muted">${escapeHtml(viewer.email || "-")}</td>
          <td class="align-right">${renderRowActions("viewers", viewer.id)}</td>
        </tr>
      `;
    },
  },
  cinemas: {
    endpoint: "cinemas",
    singular: "Cinema",
    rowsId: "cinemasRows",
    columns: 5,
    searchFields: ["name", "city", "address"],
    fields: [
      { name: "name", label: "Name", type: "text", required: true, placeholder: "Kinopark", full: true },
      { name: "city", label: "City", type: "text", placeholder: "Almaty" },
      { name: "address", label: "Address", type: "text", placeholder: "Abylai Khan 50", full: true },
    ],
    toPayload(raw) {
      return {
        name: raw.name.trim(),
        city: raw.city.trim() || null,
        address: raw.address.trim() || null,
      };
    },
    validate(payload) {
      if (!payload.name) return "Name is required";
      return null;
    },
    renderRow(cinema) {
      return `
        <tr>
          <td>${cinema.id}</td>
          <td><strong>${escapeHtml(cinema.name)}</strong></td>
          <td class="muted">${escapeHtml(cinema.city || "-")}</td>
          <td class="muted">${escapeHtml(cinema.address || "-")}</td>
          <td class="align-right">${renderRowActions("cinemas", cinema.id)}</td>
        </tr>
      `;
    },
  },
};

const dom = {
  menu: document.getElementById("sectionMenu"),
  sections: Array.from(document.querySelectorAll(".entity-section")),
  searches: Array.from(document.querySelectorAll(".section-search")),
  lastSyncText: document.getElementById("lastSyncText"),
  metrics: {
    movies: document.getElementById("metricMovies"),
    viewers: document.getElementById("metricViewers"),
    cinemas: document.getElementById("metricCinemas"),
  },
  entityModal: document.getElementById("entityModal"),
  entityModalTitle: document.getElementById("entityModalTitle"),
  entityForm: document.getElementById("entityForm"),
  entityModalClose: document.getElementById("entityModalClose"),
  entityModalCancel: document.getElementById("entityModalCancel"),
  entityModalSubmit: document.getElementById("entityModalSubmit"),
  confirmModal: document.getElementById("confirmModal"),
  confirmText: document.getElementById("confirmText"),
  confirmModalClose: document.getElementById("confirmModalClose"),
  confirmCancel: document.getElementById("confirmCancel"),
  confirmDelete: document.getElementById("confirmDelete"),
  toastStack: document.getElementById("toastStack"),
};

const appBase = window.location.pathname.endsWith("/")
  ? window.location.pathname
  : window.location.pathname.replace(/\/[^/]*$/, "/");

function buildApiUrl(path) {
  return appBase + path.replace(/^\/+/, "");
}

async function api(path, options = {}) {
  const requestOptions = {
    method: options.method ?? "GET",
    headers: { "Content-Type": "application/json" },
  };

  if (options.body !== undefined) {
    requestOptions.body = JSON.stringify(options.body);
  }

  const response = await fetch(buildApiUrl(path), requestOptions);
  if (response.status === 204) return null;

  const responseText = await response.text();
  let parsed = null;
  if (responseText) {
    try {
      parsed = JSON.parse(responseText);
    } catch {
      parsed = responseText;
    }
  }

  if (!response.ok) {
    const message = parsed && parsed.message ? parsed.message : `HTTP ${response.status}`;
    throw new Error(message);
  }
  return parsed;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function truncate(value, maxLen) {
  const text = String(value ?? "");
  if (text.length <= maxLen) return text;
  return `${text.slice(0, maxLen - 3)}...`;
}

function formatMoney(value) {
  const n = Number(value ?? 0);
  return Number.isFinite(n) ? n.toFixed(2) : "0.00";
}

function renderRowActions(entity, id) {
  return `
    <div class="row-actions">
      <button class="action-btn" type="button" data-row-action="edit" data-entity="${entity}" data-id="${id}">Edit</button>
      <button class="action-btn danger" type="button" data-row-action="delete" data-entity="${entity}" data-id="${id}">Delete</button>
    </div>
  `;
}

function setSection(section) {
  state.activeSection = section;
  dom.menu.querySelectorAll(".menu-item").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.section === section);
  });
  dom.sections.forEach((sectionEl) => {
    const isActive = sectionEl.dataset.entity === section;
    sectionEl.classList.toggle("is-active", isActive);
  });
}

function renderMetrics() {
  dom.metrics.movies.textContent = String(state.data.movies.length);
  dom.metrics.viewers.textContent = String(state.data.viewers.length);
  dom.metrics.cinemas.textContent = String(state.data.cinemas.length);
}

function setLastSync() {
  const now = new Date();
  dom.lastSyncText.textContent = `Last sync ${now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" })}`;
}

function renderLoadingRows(entity) {
  const config = ENTITY_CONFIG[entity];
  const body = document.getElementById(config.rowsId);
  body.innerHTML = new Array(4)
    .fill(0)
    .map(() => `
      <tr>
        ${new Array(config.columns).fill(0).map(() => "<td><div class=\"skeleton\"></div></td>").join("")}
      </tr>
    `)
    .join("");
}

function filterRecords(entity, records) {
  const query = state.filters[entity].trim().toLowerCase();
  if (!query) return records;

  const fields = ENTITY_CONFIG[entity].searchFields;
  return records.filter((record) =>
    fields.some((field) => String(record[field] ?? "").toLowerCase().includes(query))
  );
}

function renderEntity(entity) {
  const config = ENTITY_CONFIG[entity];
  const body = document.getElementById(config.rowsId);

  if (state.loading[entity]) {
    renderLoadingRows(entity);
    return;
  }

  const records = filterRecords(entity, state.data[entity]);
  if (records.length === 0) {
    body.innerHTML = `<tr><td class="empty-cell" colspan="${config.columns}">No records found</td></tr>`;
    return;
  }

  body.innerHTML = records.map((record) => config.renderRow(record)).join("");
}

function renderAllTables() {
  renderEntity("movies");
  renderEntity("viewers");
  renderEntity("cinemas");
}

async function loadEntity(entity, options = {}) {
  const { silent = false } = options;
  const config = ENTITY_CONFIG[entity];
  state.loading[entity] = true;
  renderEntity(entity);
  try {
    const data = await api(config.endpoint);
    state.data[entity] = Array.isArray(data) ? data : [];
    renderMetrics();
    setLastSync();
    return true;
  } catch (error) {
    if (!silent) {
      showToast("error", `Failed to load ${entity}`, error.message);
    }
    return false;
  } finally {
    state.loading[entity] = false;
    renderEntity(entity);
  }
}

async function loadAllEntities() {
  const results = await Promise.all([
    loadEntity("movies", { silent: true }),
    loadEntity("viewers", { silent: true }),
    loadEntity("cinemas", { silent: true }),
  ]);
  renderAllTables();
  return results;
}

function showToast(type, title, message) {
  const toast = document.createElement("article");
  toast.className = `toast ${type}`;
  toast.innerHTML = `<strong>${escapeHtml(title)}</strong><p>${escapeHtml(message)}</p>`;
  dom.toastStack.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 3000);
}

function toggleModal(modalEl, open) {
  modalEl.classList.toggle("is-open", open);
  modalEl.setAttribute("aria-hidden", open ? "false" : "true");
  document.body.classList.toggle("modal-open", open || dom.confirmModal.classList.contains("is-open") || dom.entityModal.classList.contains("is-open"));
}

function normalizeFieldValue(field, value) {
  if (value !== undefined && value !== null) return String(value);
  if (field.defaultValue !== undefined) return String(field.defaultValue);
  return "";
}

function buildFieldMarkup(field, value) {
  const commonAttrs = [
    `name="${field.name}"`,
    field.required ? "required" : "",
    field.placeholder ? `placeholder="${escapeHtml(field.placeholder)}"` : "",
    field.type ? `type="${field.type}"` : "",
    field.min !== undefined ? `min="${field.min}"` : "",
    field.step !== undefined ? `step="${field.step}"` : "",
  ]
    .filter(Boolean)
    .join(" ");

  if (field.as === "textarea") {
    return `
      <div class="field ${field.full ? "full" : ""}">
        <label for="field_${field.name}">${escapeHtml(field.label)}</label>
        <textarea id="field_${field.name}" ${commonAttrs}>${escapeHtml(normalizeFieldValue(field, value))}</textarea>
      </div>
    `;
  }

  return `
    <div class="field ${field.full ? "full" : ""}">
      <label for="field_${field.name}">${escapeHtml(field.label)}</label>
      <input id="field_${field.name}" value="${escapeHtml(normalizeFieldValue(field, value))}" ${commonAttrs} />
    </div>
  `;
}

function openEntityModal(entity, mode, record) {
  const config = ENTITY_CONFIG[entity];
  state.modal = {
    entity,
    mode,
    id: record ? Number(record.id) : null,
  };

  dom.entityModalTitle.textContent = mode === "create"
    ? `Create ${config.singular}`
    : `Edit ${config.singular} #${record.id}`;
  dom.entityModalSubmit.textContent = mode === "create" ? "Create" : "Save changes";

  dom.entityForm.innerHTML = config.fields
    .map((field) => buildFieldMarkup(field, record ? record[field.name] : null))
    .join("");

  toggleModal(dom.entityModal, true);
}

function closeEntityModal() {
  state.modal = null;
  dom.entityForm.innerHTML = "";
  toggleModal(dom.entityModal, false);
}

function openDeleteConfirm(entity, id) {
  state.deleteTarget = { entity, id: Number(id) };
  const label = ENTITY_CONFIG[entity].singular;
  dom.confirmText.textContent = `Delete ${label.toLowerCase()} #${id}? This action cannot be undone.`;
  toggleModal(dom.confirmModal, true);
}

function closeDeleteConfirm() {
  state.deleteTarget = null;
  toggleModal(dom.confirmModal, false);
}

function collectFormValues(formElement) {
  const formData = new FormData(formElement);
  return Object.fromEntries(formData.entries());
}

async function submitEntityForm() {
  if (!state.modal) return;
  const { entity, mode, id } = state.modal;
  const config = ENTITY_CONFIG[entity];
  const raw = collectFormValues(dom.entityForm);
  const payload = config.toPayload(raw);
  const validationError = config.validate(payload);
  if (validationError) {
    showToast("error", "Validation error", validationError);
    return;
  }

  try {
    if (mode === "create") {
      await api(config.endpoint, { method: "POST", body: payload });
      showToast("success", `${config.singular} created`, "Record saved successfully.");
    } else {
      await api(`${config.endpoint}/${id}`, { method: "PUT", body: payload });
      showToast("success", `${config.singular} updated`, "Changes saved successfully.");
    }
    closeEntityModal();
    await loadEntity(entity);
  } catch (error) {
    showToast("error", `Failed to save ${config.singular.toLowerCase()}`, error.message);
  }
}

async function handleRowAction(action, entity, id) {
  const numericId = Number(id);
  const config = ENTITY_CONFIG[entity];
  if (action === "edit") {
    let record = state.data[entity].find((item) => Number(item.id) === numericId);
    if (!record) {
      try {
        record = await api(`${config.endpoint}/${numericId}`);
      } catch (error) {
        showToast("error", "Failed to load record", error.message);
        return;
      }
    }
    openEntityModal(entity, "edit", record);
    return;
  }

  if (action === "delete") {
    openDeleteConfirm(entity, numericId);
  }
}

async function executeDelete() {
  if (!state.deleteTarget) return;
  const { entity, id } = state.deleteTarget;
  const config = ENTITY_CONFIG[entity];

  try {
    await api(`${config.endpoint}/${id}`, { method: "DELETE" });
    showToast("success", `${config.singular} deleted`, `Record #${id} removed.`);
    closeDeleteConfirm();
    await loadEntity(entity);
  } catch (error) {
    showToast("error", `Failed to delete ${config.singular.toLowerCase()}`, error.message);
  }
}

function bindEvents() {
  dom.menu.addEventListener("click", (event) => {
    const button = event.target.closest(".menu-item[data-section]");
    if (!button) return;
    setSection(button.dataset.section);
  });

  dom.searches.forEach((input) => {
    input.addEventListener("input", (event) => {
      const entity = event.target.dataset.entity;
      state.filters[entity] = event.target.value;
      renderEntity(entity);
    });
  });

  document.addEventListener("click", (event) => {
    const createButton = event.target.closest("[data-create]");
    if (createButton) {
      openEntityModal(createButton.dataset.create, "create", null);
      return;
    }

    const refreshButton = event.target.closest("[data-refresh]");
    if (refreshButton) {
      loadEntity(refreshButton.dataset.refresh);
      return;
    }

    const actionButton = event.target.closest("[data-row-action]");
    if (actionButton) {
      handleRowAction(
        actionButton.dataset.rowAction,
        actionButton.dataset.entity,
        actionButton.dataset.id
      );
    }
  });

  dom.entityModalClose.addEventListener("click", closeEntityModal);
  dom.entityModalCancel.addEventListener("click", closeEntityModal);

  dom.entityModal.addEventListener("click", (event) => {
    if (event.target === dom.entityModal) closeEntityModal();
  });

  dom.entityForm.addEventListener("submit", (event) => {
    event.preventDefault();
    submitEntityForm();
  });

  dom.confirmModalClose.addEventListener("click", closeDeleteConfirm);
  dom.confirmCancel.addEventListener("click", closeDeleteConfirm);
  dom.confirmDelete.addEventListener("click", executeDelete);

  dom.confirmModal.addEventListener("click", (event) => {
    if (event.target === dom.confirmModal) closeDeleteConfirm();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") return;
    if (dom.entityModal.classList.contains("is-open")) closeEntityModal();
    if (dom.confirmModal.classList.contains("is-open")) closeDeleteConfirm();
  });
}

async function init() {
  bindEvents();
  setSection("movies");
  renderAllTables();
  const results = await loadAllEntities();
  if (results.some(Boolean)) {
    showToast("info", "Workspace ready", "Cinema panel initialized.");
  } else {
    showToast("error", "API unavailable", "Could not fetch data from backend.");
  }
}

init();
