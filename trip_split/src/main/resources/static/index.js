const API = "";

let trips = [];
let currentTrip = null;
let currentParticipants = [];
let currentExpenses = [];

document.querySelectorAll(".tabbtn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tabbtn").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    document.querySelectorAll("main section").forEach(s => s.style.display = "none");
    document.getElementById("tab-" + btn.dataset.tab).style.display = "block";
  });
});

function showToast(msg, kind){
  const t = document.getElementById("toast");
  t.textContent = msg;
  t.className = "toast show" + (kind ? " " + kind : "");
  clearTimeout(showToast._h);
  showToast._h = setTimeout(() => t.classList.remove("show"), 3200);
}

async function apiCall(url, method, body){
  method = method || "GET";
  const opts = { method: method, headers: {} };
  if (body !== undefined){
    opts.headers["Content-Type"] = "application/json";
    opts.body = JSON.stringify(body);
  }
  const res = await fetch(API + url, opts);
  const text = await res.text();
  let data = null;
  if (text){
    try { data = JSON.parse(text); } catch(e){ data = null; }
  }
  return { ok: res.ok, status: res.status, data: data };
}

function money(n){
  return Number(n).toFixed(2);
}

// ---------- TRIPS ----------
async function loadTrips(){
  const r = await apiCall("/trips");
  trips = r.data || [];
  renderTripList();
}

function renderTripList(){
  const list = document.getElementById("trip-list");
  if(!trips.length){
    list.innerHTML = '<div style="padding:10px 22px;color:#8D9A94;font-size:12.5px;">No trips yet.</div>';
    return;
  }
  list.innerHTML = trips.map(t => {
    const active = currentTrip && currentTrip.id === t.id ? "active" : "";
    return '<button class="trip-item ' + active + '" onclick="openTrip(' + t.id + ')">' +
      t.name + '<span class="sub">' + t.participants.length + ' participant' + (t.participants.length===1?'':'s') + '</span>' +
    '</button>';
  }).join("");
}

async function createTrip(){
  const name = document.getElementById("new-trip-name").value.trim();
  const peopleRaw = document.getElementById("new-trip-people").value.trim();
  if(!name){ showToast("Trip name is required.", "reject"); return; }
  const participantNames = peopleRaw.split(",").map(s => s.trim()).filter(s => s.length);
  if(!participantNames.length){ showToast("Add at least one participant.", "reject"); return; }

  const r = await apiCall("/trips", "POST", { name: name, participantNames: participantNames });
  if(r.ok){
    showToast("Trip created.", "ok");
    document.getElementById("new-trip-name").value = "";
    document.getElementById("new-trip-people").value = "";
    await loadTrips();
    openTrip(r.data.id);
  } else {
    showToast((r.data && r.data.error) || "Could not create trip.", "reject");
  }
}

async function openTrip(id){
  const r = await apiCall("/trips/" + id);
  if(!r.ok){ showToast("Could not load trip.", "reject"); return; }
  currentTrip = r.data;
  currentParticipants = r.data.participants;

  document.getElementById("empty-state").style.display = "none";
  document.getElementById("trip-view").style.display = "block";
  document.getElementById("trip-name").textContent = currentTrip.name;
  document.getElementById("trip-meta").textContent = currentParticipants.length + " participants";

  renderTripList();
  renderParticipants();
  fillPayerAndSharers();
  await loadExpenses();
  await loadBalances();
  await loadSettlements();
}

async function deleteCurrentTrip(){
  if(!currentTrip) return;
  const r = await apiCall("/trips/" + currentTrip.id, "DELETE");
  showToast(r.ok ? "Trip deleted." : "Could not delete trip.", r.ok ? "ok" : "reject");
  currentTrip = null;
  document.getElementById("trip-view").style.display = "none";
  document.getElementById("empty-state").style.display = "block";
  await loadTrips();
}

// ---------- PARTICIPANTS ----------
function renderParticipants(){
  const list = document.getElementById("participants-list");
  if(!currentParticipants.length){ list.innerHTML = '<div class="empty">No participants yet.</div>'; return; }
  list.innerHTML = currentParticipants.map(p => {
    return '<div class="record">' +
      '<div><div class="main-line">' + p.name + '</div></div>' +
      '<div></div>' +
    '</div>';
  }).join("");
}

function fillPayerAndSharers(){
  const payerSel = document.getElementById("e-payer");
  payerSel.innerHTML = currentParticipants.map(p => '<option value="' + p.id + '">' + p.name + '</option>').join("");

  const sharers = document.getElementById("e-sharers");
  sharers.innerHTML = currentParticipants.map(p =>
    '<label><input type="checkbox" value="' + p.id + '" class="sharer-box" checked> ' + p.name + '</label>'
  ).join("");
}

async function addParticipant(){
  if(!currentTrip) return;
  const name = document.getElementById("p-name").value.trim();
  if(!name){ showToast("Name is required.", "reject"); return; }

  const r = await apiCall("/trips/" + currentTrip.id + "/participants", "POST", { name: name });
  if(r.ok){
    showToast("Participant added.", "ok");
    document.getElementById("p-name").value = "";
    await openTrip(currentTrip.id);
  } else {
    showToast((r.data && r.data.error) || "Could not add participant.", "reject");
  }
}

// ---------- EXPENSES ----------
async function loadExpenses(){
  const r = await apiCall("/trips/" + currentTrip.id + "/expenses");
  currentExpenses = r.data || [];
  renderExpenses();
}

function renderExpenses(){
  const list = document.getElementById("expenses-list");
  if(!currentExpenses.length){ list.innerHTML = '<div class="empty">No expenses logged yet.</div>'; return; }
  list.innerHTML = currentExpenses.map(e => {
    return '<div class="record">' +
      '<div>' +
        '<div class="main-line">' + e.description + ' — ' + money(e.amount) + '</div>' +
        '<div class="sub-line">Paid by ' + e.paidByName + ' · shared by ' + e.sharedByNames.join(", ") + '</div>' +
      '</div>' +
      '<div class="actions">' +
        '<button class="btn btn-red" onclick="deleteExpense(' + e.id + ')">Delete</button>' +
      '</div>' +
    '</div>';
  }).join("");
}

async function createExpense(){
  if(!currentTrip) return;
  const description = document.getElementById("e-desc").value.trim();
  const amount = document.getElementById("e-amount").value;
  const paidById = document.getElementById("e-payer").value;
  const sharedByIds = Array.from(document.querySelectorAll(".sharer-box:checked")).map(cb => parseInt(cb.value, 10));

  if(!description || !amount || !paidById){ showToast("Fill in description, amount and payer.", "reject"); return; }
  if(!sharedByIds.length){ showToast("Select at least one participant to share the expense.", "reject"); return; }

  const r = await apiCall("/trips/" + currentTrip.id + "/expenses", "POST", {
    description: description,
    amount: parseFloat(amount),
    paidById: parseInt(paidById, 10),
    sharedByIds: sharedByIds
  });
  if(r.ok){
    showToast("Expense logged.", "ok");
    document.getElementById("e-desc").value = "";
    document.getElementById("e-amount").value = "";
    await loadExpenses();
    await loadBalances();
    await loadSettlements();
  } else {
    showToast((r.data && r.data.error) || "Could not log expense.", "reject");
  }
}

async function deleteExpense(id){
  const r = await apiCall("/expenses/" + id, "DELETE");
  showToast(r.ok ? "Expense deleted." : "Could not delete expense.", r.ok ? "ok" : "reject");
  await loadExpenses();
  await loadBalances();
  await loadSettlements();
}

// ---------- BALANCES & SETTLEMENT ----------
async function loadBalances(){
  const r = await apiCall("/trips/" + currentTrip.id + "/balances");
  renderBalances(r.data || []);
}

function renderBalances(balances){
  const list = document.getElementById("balances-list");
  if(!balances.length){ list.innerHTML = '<div class="empty">No participants yet.</div>'; return; }
  list.innerHTML = balances.map(b => {
    const amt = parseFloat(b.balance);
    const cls = amt > 0.001 ? "pos" : (amt < -0.001 ? "neg" : "zero");
    const sign = amt > 0.001 ? "+" : "";
    return '<div class="balance-row">' +
      '<span class="balance-name">' + b.name + '</span>' +
      '<span class="balance-amt ' + cls + '">' + sign + money(b.balance) + '</span>' +
    '</div>';
  }).join("");
}

async function loadSettlements(){
  const r = await apiCall("/trips/" + currentTrip.id + "/settlements");
  renderSettlements(r.data || []);
}

function renderSettlements(settlements){
  const list = document.getElementById("settlement-list");
  if(!settlements.length){ list.innerHTML = '<div class="empty">No settlement generated yet. Click "Generate settlement" above.</div>'; return; }
  list.innerHTML = settlements.map(s => {
    return '<div class="settle-row">' +
      '<span>' + s.fromName + '</span>' +
      '<span class="settle-arrow">&rarr;</span>' +
      '<span>' + s.toName + '</span>' +
      '<span class="settle-amt">' + money(s.amount) + '</span>' +
    '</div>';
  }).join("");
}

async function generateSettlement(){
  if(!currentTrip) return;
  const r = await apiCall("/trips/" + currentTrip.id + "/settlements", "POST");
  if(r.ok){
    showToast("Settlement generated.", "ok");
    renderSettlements(r.data || []);
  } else {
    showToast((r.data && r.data.error) || "Could not generate settlement.", "reject");
  }
}

// ---------- init ----------
(async function init(){
  await loadTrips();
})();
