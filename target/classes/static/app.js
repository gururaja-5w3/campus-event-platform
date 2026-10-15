const API = "";
let authMode = "login";
let currentUser = null;
let allEvents = [];
const $ = (s) => document.querySelector(s);
const authDialog = $("#authDialog");
const dashboardDialog = $("#dashboardDialog");

function escapeHtml(value="") {
  return String(value).replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
}
function requestOptions(method="GET", body=null) {
  const opts = {method, headers: {"Content-Type":"application/json"}, credentials:"same-origin"};
  if (body !== null) opts.body = JSON.stringify(body);
  return opts;
}
async function api(path, options={}) {
  const res = await fetch(API + path, options);
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try { const data = await res.json(); message = data.message || data.error || message; } catch {}
    throw new Error(message);
  }
  if (res.status === 204) return null;
  const type = res.headers.get("content-type") || "";
  return type.includes("application/json") ? res.json() : res.text();
}
function formatDate(value) {
  try { return new Intl.DateTimeFormat("en-IN",{day:"numeric",month:"short",year:"numeric",hour:"numeric",minute:"2-digit"}).format(new Date(value)); }
  catch { return value || "Date to be announced"; }
}
const iconFor = c => ({ "Open Mic":"🎤", "Cultural":"🎭", "Technical":"💻", "Workshop":"🛠️", "Sports":"🏆", "Music":"🎸" }[c] || "✦");

async function loadEvents() {
  try {
    allEvents = await api("/api/events");
    renderEvents(allEvents);
  } catch (e) {
    $("#eventGrid").innerHTML = `<div class="empty-state">Could not load events. Start the Spring Boot server and check the MySQL configuration.<br><small>${escapeHtml(e.message)}</small></div>`;
  }
}
function renderEvents(events) {
  if (!events.length) {
    $("#eventGrid").innerHTML = `<div class="empty-state">No approved events yet. Check back soon — new campus experiences are on the way.</div>`;
    return;
  }
  $("#eventGrid").innerHTML = events.map(e => `
    <article class="event-card">
      <div class="event-cover"><span class="category-pill">${escapeHtml(e.category)}</span><span class="cover-icon">${iconFor(e.category)}</span></div>
      <div class="event-body">
        <div class="event-date">${escapeHtml(formatDate(e.eventDate).toUpperCase())}</div>
        <h3>${escapeHtml(e.title)}</h3><p>${escapeHtml(e.description)}</p>
        <div class="event-meta"><span>⌖ ${escapeHtml(e.venue)}</span><span>◉ ${e.capacity} places</span></div>
        <div class="event-foot"><span class="price">${Number(e.ticketPrice) === 0 ? "FREE" : "₹" + Number(e.ticketPrice).toFixed(2)}</span><button class="btn btn-primary book-btn" data-book="${e.id}">Get ticket ↗</button></div>
      </div>
    </article>`).join("");
  document.querySelectorAll("[data-book]").forEach(btn => btn.addEventListener("click", () => bookEvent(Number(btn.dataset.book))));
}
document.querySelectorAll(".filter").forEach(btn => btn.addEventListener("click", () => {
  document.querySelectorAll(".filter").forEach(b => b.classList.toggle("active", b === btn));
  renderEvents(btn.dataset.filter === "All" ? allEvents : allEvents.filter(e => e.category.toLowerCase() === btn.dataset.filter.toLowerCase()));
}));

function openAuth(mode="login") {
  authMode = mode;
  $("#authError").textContent = "";
  $("#authTitle").textContent = mode === "register" ? "Create your account" : "Sign in";
  $("#authSub").textContent = mode === "register" ? "Join the NBKRIST campus community." : "Use your account to continue.";
  $("#nameWrap").classList.toggle("hidden", mode !== "register");
  $("#fullName").required = mode === "register";
  $("#authSubmit").textContent = mode === "register" ? "Create student account" : "Sign in";
  $("#switchAuth").classList.toggle("hidden", mode === "admin");
  if (mode === "admin") {
    $("#authTitle").textContent = "Admin sign in";
    $("#authSub").textContent = "Sign in with your provisioned administrator account.";
  }
  if (!authDialog.open) authDialog.showModal();
}
$("#loginOpen").addEventListener("click", () => openAuth("login"));
$("#registerOpen").addEventListener("click", () => openAuth("register"));
$("#adminOpen").addEventListener("click", () => openAuth("admin"));
$("#switchMode").addEventListener("click", () => openAuth(authMode === "register" ? "login" : "register"));

$("#authForm").addEventListener("submit", async e => {
  e.preventDefault();
  $("#authError").textContent = "";
  const email = $("#email").value.trim();
  const password = $("#password").value;
  try {
    if (authMode === "register") {
      await api("/api/auth/register", requestOptions("POST", {fullName:$("#fullName").value.trim(), email, password}));
    }
    // HTTP Basic credentials are kept in memory only for this page session.
    window.sessionStorage.setItem("cvAuth", btoa(unescape(encodeURIComponent(email + ":" + password))));
    const me = await api("/api/auth/me", {headers:{"Authorization":"Basic "+window.sessionStorage.getItem("cvAuth")}});
    if (!me) throw new Error("Could not sign in.");
    currentUser = me;
    authDialog.close();
    await showDashboard();
  } catch (err) {
    $("#authError").textContent = authMode === "register" ? err.message : (err.message.includes("401") ? "Email or password is incorrect." : err.message);
  }
});
async function apiAsUser(path, options={}) {
  const token = window.sessionStorage.getItem("cvAuth");
  options.headers = {...(options.headers || {}), ...(token ? {"Authorization":"Basic "+token} : {})};
  return api(path, options);
}
async function bookEvent(eventId) {
  if (!currentUser) { openAuth("login"); return; }
  try {
    const booking = await apiAsUser("/api/bookings", requestOptions("POST",{eventId}));
    alert("Booking confirmed! Your ticket code is:\n" + booking.ticketCode);
  } catch (e) { alert(e.message); }
}
async function showDashboard() {
  if (!currentUser) return;
  $("#dashboardTitle").textContent = `Hello, ${currentUser.fullName.split(" ")[0]}`;
  $("#dashboardSubtitle").textContent = `${currentUser.role} · ${currentUser.email}`;
  const content = $("#dashboardContent");
  content.innerHTML = `<p>Loading your dashboard…</p>`;
  if (!dashboardDialog.open) dashboardDialog.showModal();
  try {
    if (currentUser.role === "ADMIN") {
      const stats = await apiAsUser("/api/admin/dashboard");
      content.innerHTML = `
        <div class="dashboard-stats">
          <div class="stat"><small>Total events</small><b>${stats.totalEvents}</b></div>
          <div class="stat"><small>Approved</small><b>${stats.approvedEvents}</b></div>
          <div class="stat"><small>Pending review</small><b>${stats.pendingEvents}</b></div>
        </div>
        <div class="dashboard-actions"><button id="showPending" class="btn btn-primary">Review pending events</button><button id="showCreate" class="btn btn-outline">Create an event</button></div>
        <div id="adminPanel"></div>`;
      $("#showPending").addEventListener("click", loadPending);
      $("#showCreate").addEventListener("click", showCreateEvent);
      await loadPending();
    } else if (currentUser.role === "ORGANIZER") {
      content.innerHTML = `<p>You are signed in as an organizer. Use this starter's API to propose events for admin approval.</p><button id="organizerCreate" class="btn btn-primary">Propose an event</button><div id="adminPanel"></div>`;
      $("#organizerCreate").addEventListener("click", showCreateEvent);
    } else {
      const bookings = await apiAsUser("/api/bookings/mine");
      content.innerHTML = `<h3>Your tickets</h3>${bookings.length ? bookings.map(b=>`<div class="admin-event"><div><h4>${escapeHtml(b.event.title)}</h4><small>${escapeHtml(formatDate(b.event.eventDate))} · ${escapeHtml(b.event.venue)}</small><div class="ticket-code">${escapeHtml(b.ticketCode)}</div></div></div>`).join("") : "<p>You have not booked any events yet. Explore events and reserve your place.</p>"}`;
    }
  } catch(e) {
    content.innerHTML = `<p class="message">${escapeHtml(e.message)}</p>`;
  }
}
async function loadPending() {
  const panel = $("#adminPanel");
  if (!panel) return;
  try {
    const events = await apiAsUser("/api/admin/events/pending");
    panel.innerHTML = `<h3>Events awaiting approval</h3>` + (events.length ? `<div class="admin-event-list">${events.map(e=>`<div class="admin-event"><div><h4>${escapeHtml(e.title)}</h4><small>${escapeHtml(e.category)} · ${escapeHtml(formatDate(e.eventDate))} · ${escapeHtml(e.venue)}</small></div><div class="dashboard-actions"><button class="btn btn-primary" data-approve="${e.id}">Approve</button><button class="btn btn-outline" data-delete="${e.id}">Delete</button></div></div>`).join("")}</div>` : `<p>No pending events. You're all caught up.</p>`);
    panel.querySelectorAll("[data-approve]").forEach(b=>b.addEventListener("click", async()=>{try{await apiAsUser(`/api/admin/events/${b.dataset.approve}/approve`,requestOptions("POST"));await loadEvents();await showDashboard()}catch(e){alert(e.message)}}));
    panel.querySelectorAll("[data-delete]").forEach(b=>b.addEventListener("click", async()=>{if(!confirm("Delete this event?"))return;try{await apiAsUser(`/api/admin/events/${b.dataset.delete}`,requestOptions("DELETE"));await loadEvents();await showDashboard()}catch(e){alert(e.message)}}));
  } catch(e) { panel.innerHTML = `<p class="message">${escapeHtml(e.message)}</p>`; }
}
function showCreateEvent() {
  const panel = $("#adminPanel");
  panel.innerHTML = `<h3>Create an event</h3>
    <form id="createEventForm" class="admin-form">
      <label>Event title<input name="title" required maxlength="140" placeholder="e.g. Campus Open Mic Night"></label>
      <label>Category<select name="category"><option>Open Mic</option><option>Cultural</option><option>Technical</option><option>Workshop</option><option>Sports</option><option>Music</option></select></label>
      <label class="span-two">Description<textarea name="description" required maxlength="1500" rows="3" placeholder="Tell students what to expect"></textarea></label>
      <label>Venue<input name="venue" required maxlength="180" placeholder="Auditorium / Seminar Hall"></label>
      <label>Date and time<input name="eventDate" type="datetime-local" required></label>
      <label>Capacity<input name="capacity" type="number" min="1" required value="100"></label>
      <label>Ticket price (₹)<input name="ticketPrice" type="number" min="0" step="0.01" required value="0"></label>
      <button class="btn btn-primary span-two" type="submit">Submit event</button>
      <p class="message span-two" id="createError"></p>
    </form>`;
  $("#createEventForm").addEventListener("submit", async e=>{
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const body = {title:f.get("title"),category:f.get("category"),description:f.get("description"),venue:f.get("venue"),eventDate:f.get("eventDate"),capacity:Number(f.get("capacity")),ticketPrice:Number(f.get("ticketPrice"))};
    try {
      await apiAsUser("/api/events",requestOptions("POST",body));
      await loadEvents();
      await showDashboard();
    } catch(err) { $("#createError").textContent = err.message; }
  });
}
$("#logoutBtn").addEventListener("click", ()=>{
  window.sessionStorage.removeItem("cvAuth"); currentUser = null; dashboardDialog.close();
});
async function restoreSession() {
  const token = window.sessionStorage.getItem("cvAuth");
  if (!token) return;
  try {
    currentUser = await apiAsUser("/api/auth/me");
  } catch { window.sessionStorage.removeItem("cvAuth"); currentUser = null; }
}
(async()=>{await restoreSession();await loadEvents();})();
