const API = {
  auth: "http://localhost:8081",
  finance: "http://localhost:8082",
  reports: "http://localhost:8083"
};

const state = {
  token: localStorage.getItem("financeToken") || "",
  email: localStorage.getItem("financeEmail") || "",
  groups: [],
  categories: [],
  operations: [],
  reportRange: defaultRange()
};

const titles = {
  dashboard: ["Обзор финансов", "Доходы, расходы и семейный баланс"],
  operations: ["Операции", "Доходы и расходы"],
  groups: ["Группы", "Семейный и совместный учет"],
  categories: ["Категории", "Классификация операций"],
  reports: ["Отчеты", "Периоды, участники и аналитика"]
};

document.addEventListener("DOMContentLoaded", () => {
  setDefaultDates();
  bindNavigation();
  bindForms();
  renderAuthState();
  if (state.token) {
    bootstrap();
  }
});

function bindNavigation() {
  document.querySelectorAll(".nav-item").forEach((button) => {
    button.addEventListener("click", () => switchView(button.dataset.view));
  });

  document.getElementById("logoutButton").addEventListener("click", logout);
  document.getElementById("refreshDashboard").addEventListener("click", () => run(refreshDashboard));
  document.getElementById("downloadCsv").addEventListener("click", () => run(downloadCsv));
  document.getElementById("operationFilterGroup").addEventListener("change", () => run(loadOperations));

  document.getElementById("operationGroup").addEventListener("change", (event) => {
    syncCategoryScope(event.target.value);
    run(loadCategories);
  });

  document.getElementById("categoryGroup").addEventListener("change", (event) => {
    syncCategoryScope(event.target.value);
    run(loadCategories);
  });

  document.getElementById("categoryFilterGroup").addEventListener("change", (event) => {
    syncCategoryScope(event.target.value);
    run(loadCategories);
  });

  document.getElementById("reportGroup").addEventListener("change", (event) => {
    state.reportRange.groupId = event.target.value || undefined;
  });
}

function bindForms() {
  onSubmit("loginForm", async (form) => {
    const data = formData(form);
    const response = await request(`${API.auth}/api/auth/login`, {
      method: "POST",
      body: JSON.stringify(data)
    }, false);
    saveSession(response.token, data.email);
    await bootstrap();
  });

  onSubmit("registerForm", async (form) => {
    const data = formData(form);
    const response = await request(`${API.auth}/api/auth/register`, {
      method: "POST",
      body: JSON.stringify(data)
    }, false);
    saveSession(response.token, data.email);
    await bootstrap();
  });

  onSubmit("groupForm", async (form) => {
    const group = await request(`${API.finance}/api/groups`, {
      method: "POST",
      body: JSON.stringify(formData(form))
    });
    form.reset();
    await loadGroups();
    selectGroupEverywhere(group.id);
    await Promise.all([loadCategories(), loadOperations(), renderReport(), refreshDashboard()]);
    toast("Группа создана и выбрана");
  });

  onSubmit("memberForm", async (form) => {
    const data = formData(form);
    if (!data.groupId) {
      toast("Сначала создайте или выберите группу");
      return;
    }
    await request(`${API.finance}/api/groups/${data.groupId}/members`, {
      method: "POST",
      body: JSON.stringify({ userId: Number(data.userId) })
    });
    form.reset();
    fillGroupSelects();
    toast("Участник добавлен");
  });

  onSubmit("categoryForm", async (form) => {
    const data = normalizeOptionalGroup(formData(form));
    const category = await request(`${API.finance}/api/categories`, {
      method: "POST",
      body: JSON.stringify(data)
    });
    syncCategoryScope(category.groupId || "");
    await loadCategories();
    document.getElementById("operationCategory").value = String(category.id);
    toast("Категория создана и выбрана");
  });

  onSubmit("operationForm", async (form) => {
    const data = normalizeOptionalGroup(formData(form));
    if (!data.categoryId) {
      toast("Сначала создайте и выберите категорию");
      return;
    }
    data.amount = Number(data.amount);
    data.categoryId = Number(data.categoryId);
    await request(`${API.finance}/api/operations`, {
      method: "POST",
      body: JSON.stringify(data)
    });
    document.getElementById("operationFilterGroup").value = data.groupId || "";
    toast("Операция добавлена");
    await Promise.all([loadOperations(), refreshDashboard(), renderReport()]);
  });

  onSubmit("reportFilter", async (form) => {
    state.reportRange = normalizeOptionalGroup(formData(form));
    await renderReport();
  });
}

function onSubmit(formId, handler) {
  document.getElementById(formId).addEventListener("submit", (event) => {
    event.preventDefault();
    run(() => handler(event.target));
  });
}

async function run(action) {
  try {
    await action();
  } catch (error) {
    toast(error.message || "Ошибка запроса");
  }
}

async function bootstrap() {
  renderAuthState();
  await loadGroups();
  if (state.groups.length > 0) {
    selectGroupEverywhere(state.groups[0].id);
  }
  await Promise.all([loadCategories(), loadOperations(), refreshDashboard(), renderReport()]);
  document.getElementById("apiStatus").classList.add("ok");
  document.getElementById("apiStatus").textContent = "API online";
}

async function loadGroups() {
  state.groups = await request(`${API.finance}/api/groups`);
  renderGroups();
  fillGroupSelects();
}

async function loadCategories() {
  const groupId = document.getElementById("categoryFilterGroup").value;
  const query = groupId ? `?groupId=${groupId}` : "";
  state.categories = await request(`${API.finance}/api/categories${query}`);
  renderCategories();
  fillCategorySelect();
}

async function loadOperations() {
  const groupId = document.getElementById("operationFilterGroup").value;
  const query = groupId ? `?groupId=${groupId}` : "";
  state.operations = await request(`${API.finance}/api/operations${query}`);
  renderOperations();
}

async function refreshDashboard() {
  const range = defaultRange();
  const groupId = document.getElementById("reportGroup").value || firstGroupId();
  const query = new URLSearchParams({ from: range.from, to: range.to });
  if (groupId) {
    query.set("groupId", groupId);
  }
  const summary = await request(`${API.reports}/api/reports/summary?${query}`);
  const analytics = await request(`${API.reports}/api/reports/expense-analytics?${query}`);
  renderMetrics(summary);
  renderCategoryBars("categoryBars", analytics.categoryShares);
  renderMonthlyTrend(analytics.monthlyTrend);
}

async function renderReport() {
  const params = new URLSearchParams({
    from: state.reportRange.from,
    to: state.reportRange.to
  });
  if (state.reportRange.groupId) {
    params.set("groupId", state.reportRange.groupId);
  }
  const summary = await request(`${API.reports}/api/reports/summary?${params}`);
  const analytics = await request(`${API.reports}/api/reports/expense-analytics?${params}`);
  renderSummaryOutput(summary);
  renderCategoryBars("reportCategoryBars", analytics.categoryShares);
}

function renderAuthState() {
  document.getElementById("sessionEmail").textContent = state.email || "Гость";
  document.getElementById("authPanel").classList.toggle("is-hidden", Boolean(state.token));
  document.getElementById("workspace").classList.toggle("is-hidden", !state.token);
  document.getElementById("logoutButton").disabled = !state.token;
}

function renderGroups() {
  const list = document.getElementById("groupsList");
  list.innerHTML = state.groups.length ? "" : empty("Групп пока нет");
  state.groups.forEach((group) => {
    list.insertAdjacentHTML("beforeend", `
      <div class="list-item">
        <div>
          <strong>${escapeHtml(group.name)}</strong>
          <span>ID ${group.id} · владелец ${group.ownerUserId}</span>
        </div>
      </div>
    `);
  });
}

function renderCategories() {
  const list = document.getElementById("categoriesList");
  list.innerHTML = state.categories.length ? "" : empty("Категорий пока нет");
  state.categories.forEach((category) => {
    list.insertAdjacentHTML("beforeend", `
      <div class="list-item">
        <div>
          <strong>${escapeHtml(category.name)}</strong>
          <span>${category.type} · ${category.groupId ? `группа ${category.groupId}` : "личная"}</span>
        </div>
        <span class="badge ${category.type === "INCOME" ? "income" : "expense"}">${category.type}</span>
      </div>
    `);
  });
}

function renderOperations() {
  const tbody = document.getElementById("operationsTable");
  tbody.innerHTML = state.operations.length ? "" : `<tr><td colspan="5">Операций пока нет</td></tr>`;
  state.operations.forEach((operation) => {
    tbody.insertAdjacentHTML("beforeend", `
      <tr>
        <td>${operation.operationDate}</td>
        <td><span class="badge ${operation.type === "INCOME" ? "income" : "expense"}">${operation.type}</span></td>
        <td>${escapeHtml(operation.categoryName)}</td>
        <td>${escapeHtml(operation.description || "")}</td>
        <td class="num">${money(operation.amount)}</td>
      </tr>
    `);
  });
}

function renderMetrics(summary) {
  document.getElementById("metricIncome").textContent = money(summary.totalIncome);
  document.getElementById("metricExpense").textContent = money(summary.totalExpense);
  document.getElementById("metricBalance").textContent = money(summary.balance);
  document.getElementById("metricCount").textContent = summary.operationsCount;
}

function renderCategoryBars(elementId, shares) {
  const root = document.getElementById(elementId);
  root.innerHTML = shares.length ? "" : empty("Нет расходов за период");
  shares.forEach((row) => {
    root.insertAdjacentHTML("beforeend", `
      <div class="bar-row">
        <div class="bar-top">
          <span>${escapeHtml(row.categoryName)}</span>
          <span>${money(row.amount)} · ${row.percent}%</span>
        </div>
        <div class="bar-track"><div class="bar-fill" style="width:${Math.max(2, Number(row.percent))}%"></div></div>
      </div>
    `);
  });
}

function renderMonthlyTrend(rows) {
  const root = document.getElementById("monthlyTrend");
  root.innerHTML = rows.length ? "" : empty("Нет данных за период");
  const max = Math.max(...rows.map((row) => Number(row.income) + Number(row.expense)), 1);
  rows.forEach((row) => {
    const incomeWidth = Math.max(2, Number(row.income) / max * 100);
    const expenseWidth = Math.max(2, Number(row.expense) / max * 100);
    root.insertAdjacentHTML("beforeend", `
      <div class="trend-row">
        <span>${row.month}</span>
        <div class="trend-line">
          <div class="trend-income" style="width:${incomeWidth}%"></div>
          <div class="trend-expense" style="width:${expenseWidth}%"></div>
        </div>
        <strong>${money(row.balance)}</strong>
      </div>
    `);
  });
}

function renderSummaryOutput(summary) {
  document.getElementById("summaryOutput").innerHTML = `
    <div class="summary-grid">
      <div class="summary-cell"><span>Доходы</span><strong>${money(summary.totalIncome)}</strong></div>
      <div class="summary-cell"><span>Расходы</span><strong>${money(summary.totalExpense)}</strong></div>
      <div class="summary-cell"><span>Баланс</span><strong>${money(summary.balance)}</strong></div>
      <div class="summary-cell"><span>Операции</span><strong>${summary.operationsCount}</strong></div>
    </div>
  `;
}

function fillGroupSelects() {
  const targets = [
    ["operationGroup", true],
    ["operationFilterGroup", true],
    ["memberGroup", false],
    ["categoryGroup", true],
    ["categoryFilterGroup", true],
    ["reportGroup", true]
  ];

  targets.forEach(([id, withPersonal]) => {
    const select = document.getElementById(id);
    const selected = select.value;
    select.innerHTML = withPersonal ? `<option value="">Личные данные</option>` : "";
    state.groups.forEach((group) => {
      select.insertAdjacentHTML("beforeend", `<option value="${group.id}">${escapeHtml(group.name)}</option>`);
    });
    if ([...select.options].some((option) => option.value === selected)) {
      select.value = selected;
    }
  });
}

function fillCategorySelect() {
  const select = document.getElementById("operationCategory");
  select.innerHTML = state.categories.length ? "" : `<option value="">Нет категорий для выбранной группы</option>`;
  state.categories.forEach((category) => {
    select.insertAdjacentHTML("beforeend", `<option value="${category.id}">${escapeHtml(category.name)} · ${category.type}</option>`);
  });
}

function syncCategoryScope(groupId) {
  const value = groupId ? String(groupId) : "";
  ["operationGroup", "categoryGroup", "categoryFilterGroup"].forEach((id) => {
    document.getElementById(id).value = value;
  });
}

function selectGroupEverywhere(groupId) {
  const value = groupId ? String(groupId) : "";
  ["operationGroup", "operationFilterGroup", "memberGroup", "categoryGroup", "categoryFilterGroup", "reportGroup"].forEach((id) => {
    const select = document.getElementById(id);
    if ([...select.options].some((option) => option.value === value)) {
      select.value = value;
    }
  });
  state.reportRange.groupId = value || undefined;
}

function switchView(view) {
  document.querySelectorAll(".nav-item").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === view);
  });
  document.querySelectorAll(".view").forEach((element) => element.classList.add("is-hidden"));
  document.getElementById(`${view}View`).classList.remove("is-hidden");
  document.getElementById("viewTitle").textContent = titles[view][0];
  document.getElementById("viewSubtitle").textContent = titles[view][1];
}

async function downloadCsv() {
  const params = new URLSearchParams({
    from: state.reportRange.from,
    to: state.reportRange.to
  });
  if (state.reportRange.groupId) {
    params.set("groupId", state.reportRange.groupId);
  }
  const response = await fetch(`${API.reports}/api/reports/summary.csv?${params}`, {
    headers: authHeaders()
  });
  if (!response.ok) {
    throw new Error(await errorMessage(response));
  }
  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "finance-report.csv";
  link.click();
  URL.revokeObjectURL(url);
}

async function request(url, options = {}, auth = true) {
  const headers = {
    ...(auth ? authHeaders() : {}),
    ...(options.headers || {})
  };
  if (options.body) {
    headers["Content-Type"] = "application/json";
  }
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    throw new Error(await errorMessage(response));
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

function authHeaders() {
  return state.token ? { Authorization: `Bearer ${state.token}` } : {};
}

async function errorMessage(response) {
  try {
    const body = await response.json();
    return body.message || `HTTP ${response.status}`;
  } catch {
    return `HTTP ${response.status}`;
  }
}

function formData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function normalizeOptionalGroup(data) {
  if (data.groupId) {
    data.groupId = Number(data.groupId);
  } else {
    delete data.groupId;
  }
  return data;
}

function saveSession(token, email) {
  state.token = token;
  state.email = email;
  localStorage.setItem("financeToken", token);
  localStorage.setItem("financeEmail", email);
  toast("Вход выполнен");
}

function logout() {
  state.token = "";
  state.email = "";
  localStorage.removeItem("financeToken");
  localStorage.removeItem("financeEmail");
  renderAuthState();
}

function firstGroupId() {
  return state.groups[0]?.id || "";
}

function defaultRange() {
  const now = new Date();
  const from = new Date(now.getFullYear(), now.getMonth(), 1);
  const to = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  return {
    from: dateValue(from),
    to: dateValue(to)
  };
}

function setDefaultDates() {
  const range = defaultRange();
  document.querySelectorAll('input[type="date"][name="operationDate"]').forEach((input) => {
    input.value = dateValue(new Date());
  });
  document.querySelectorAll('input[type="date"][name="from"]').forEach((input) => {
    input.value = range.from;
  });
  document.querySelectorAll('input[type="date"][name="to"]').forEach((input) => {
    input.value = range.to;
  });
  state.reportRange = range;
}

function dateValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function money(value) {
  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "RUB",
    maximumFractionDigits: 2
  }).format(Number(value || 0));
}

function empty(text) {
  return `<div class="list-item"><span>${escapeHtml(text)}</span></div>`;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function toast(message) {
  const element = document.getElementById("toast");
  element.textContent = message;
  element.classList.add("show");
  window.clearTimeout(toast.timer);
  toast.timer = window.setTimeout(() => element.classList.remove("show"), 3600);
}
