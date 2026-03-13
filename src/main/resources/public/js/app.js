let me = null;
let grid = true;
let selectedEventId = null;
let chart1, chart2;

const q = (s) => document.querySelector(s);
const api = async (url, options = {}) => {
  const res = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...options });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Error');
  return data;
};

async function refreshMe() {
  try {
    me = await api('/api/me');
    q('#meLabel').innerText = `${me.name} (${me.role})`;
  } catch {
    me = null;
    q('#meLabel').innerText = 'No autenticado';
  }
  const canManage = me && ['ADMIN','ORGANIZADOR'].includes(me.role);
  q('#eventFormCard').style.display = canManage ? 'block' : 'none';
  q('#scanCard').style.display = canManage ? 'block' : 'none';
  q('#usersCard').style.display = me?.role === 'ADMIN' ? 'block' : 'none';
  if (me?.role === 'ADMIN') await loadUsers();
}

function eventCard(e) {
  const col = document.createElement('div');
  col.className = grid ? 'col-md-6' : 'col-12';
  col.innerHTML = `<div class="card event-card"><div class="card-body">
  <h5>${e.title}</h5>
  <p>${e.description}</p>
  <p><b>Fecha:</b> ${e.dateTime?.replace('T', ' ')}</p>
  <p><b>Lugar:</b> ${e.location}</p>
  <p><b>Cupo:</b> ${e.maxCapacity} | <b>Estado:</b> ${e.status}</p>
  <div class="d-flex flex-wrap gap-2">
   <button class="btn btn-sm btn-primary inscribir">Inscribirme</button>
   <button class="btn btn-sm btn-outline-danger cancelar-ins">Cancelar inscripción</button>
   <button class="btn btn-sm btn-outline-dark qr">Mi QR</button>
   <button class="btn btn-sm btn-warning resumen">Resumen</button>
   <button class="btn btn-sm btn-info editar">Editar</button>
   <button class="btn btn-sm btn-secondary publicar">Publicar</button>
   <button class="btn btn-sm btn-secondary borrar">Eliminar</button>
  </div>
  <div class="mt-2 qr-box"></div>
  </div></div>`;

  col.querySelector('.inscribir').onclick = async () => run(() => api(`/api/eventos/${e.id}/inscribirse`, { method: 'POST' }), 'Inscripción OK');
  col.querySelector('.cancelar-ins').onclick = async () => run(() => api(`/api/eventos/${e.id}/inscripcion`, { method: 'DELETE' }), 'Cancelación OK');
  col.querySelector('.qr').onclick = async () => run(async () => {
    const qr = await api(`/api/eventos/${e.id}/mi-qr`);
    col.querySelector('.qr-box').innerHTML = `<img class="img-fluid" src="data:image/png;base64,${qr.qrBase64}"><small class="d-block">Token: ${qr.token}</small>`;
  }, 'QR generado');
  col.querySelector('.resumen').onclick = async () => { selectedEventId = e.id; loadStats(); };
  col.querySelector('.editar').onclick = () => {
    q('#evId').value = e.id;
    q('#title').value = e.title;
    q('#description').value = e.description;
    q('#dateTime').value = e.dateTime?.slice(0,16);
    q('#location').value = e.location;
    q('#maxCapacity').value = e.maxCapacity;
  };
  col.querySelector('.publicar').onclick = async () => run(() => api(`/api/eventos/${e.id}/status?value=PUBLICADO`, { method: 'PATCH' }), 'Actualizado');
  col.querySelector('.borrar').onclick = async () => run(() => api(`/api/eventos/${e.id}`, { method: 'DELETE' }), 'Eliminado');

  if (!me || !['ADMIN','ORGANIZADOR'].includes(me.role)) {
    col.querySelector('.editar').style.display = 'none';
    col.querySelector('.publicar').style.display = 'none';
    col.querySelector('.borrar').style.display = 'none';
  }
  return col;
}

async function loadEvents() {
  const data = await api('/api/eventos?all=true');
  q('#eventsContainer').innerHTML = '';
  data.forEach(e => q('#eventsContainer').appendChild(eventCard(e)));
}

async function loadStats() {
  if (!selectedEventId) return;
  const summary = await api(`/api/eventos/${selectedEventId}/resumen`);
  q('#summary').innerText = `Inscritos: ${summary.totalRegistrations} | Asistentes: ${summary.totalAttended} | % Asistencia: ${summary.attendancePct.toFixed(2)}%`;

  const day = await api(`/api/eventos/${selectedEventId}/stats/inscripciones-por-dia`);
  const hour = await api(`/api/eventos/${selectedEventId}/stats/asistencia-por-hora`);
  if (chart1) chart1.destroy();
  if (chart2) chart2.destroy();
  chart1 = new Chart(q('#chart1'), { type: 'bar', data: { labels: day.map(x => x.day), datasets: [{ label: 'Inscripciones por día', data: day.map(x => x.count) }] } });
  chart2 = new Chart(q('#chart2'), { type: 'line', data: { labels: hour.map(x => x.hour), datasets: [{ label: 'Asistencia por hora', data: hour.map(x => x.count) }] } });
}

async function loadUsers() {
  const users = await api('/api/admin/usuarios');
  q('#usersTable').innerHTML = `<table class="table table-sm"><tr><th>Nombre</th><th>Email</th><th>Rol</th><th>Bloqueado</th><th>Acciones</th></tr>
    ${users.map(u => `<tr><td>${u.name}</td><td>${u.email}</td><td>${u.role}</td><td>${u.blocked}</td>
    <td><button onclick="setRole(${u.id},'ORGANIZADOR')" class="btn btn-sm btn-outline-primary">Org</button>
    <button onclick="setRole(${u.id},'PARTICIPANTE')" class="btn btn-sm btn-outline-secondary">Part</button>
    <button onclick="toggleBlock(${u.id},true)" class="btn btn-sm btn-outline-danger">Bloquear</button>
    <button onclick="toggleBlock(${u.id},false)" class="btn btn-sm btn-outline-success">Desbloq</button></td></tr>`).join('')}
  </table>`;
}
window.setRole = async (id, role) => run(() => api(`/api/admin/usuarios/${id}/rol`, { method: 'PATCH', body: JSON.stringify({ role }) }), 'Rol actualizado', loadUsers);
window.toggleBlock = async (id, blocked) => run(() => api(`/api/admin/usuarios/${id}/bloqueo`, { method: 'PATCH', body: JSON.stringify({ blocked }) }), 'Estado actualizado', loadUsers);

async function run(fn, okMsg, cb) {
  try { await fn(); alert(okMsg); await refreshMe(); await loadEvents(); if (cb) await cb(); }
  catch (e) { alert(e.message); }
}

q('#loginBtn').onclick = () => run(() => api('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: q('#email').value, password: q('#password').value }) }), 'Login OK');
q('#registerBtn').onclick = () => run(() => api('/api/auth/register', { method: 'POST', body: JSON.stringify({ name: q('#rname').value, email: q('#remail').value, password: q('#rpass').value }) }), 'Registro OK');
q('#logoutBtn').onclick = () => run(() => api('/api/auth/logout', { method: 'POST' }), 'Logout');
q('#saveEventBtn').onclick = async () => {
  const payload = {
    title: q('#title').value,
    description: q('#description').value,
    dateTime: q('#dateTime').value + ':00',
    location: q('#location').value,
    maxCapacity: parseInt(q('#maxCapacity').value || '0')
  };
  const id = q('#evId').value;
  run(() => api(id ? `/api/eventos/${id}` : '/api/eventos', { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) }), 'Evento guardado');
};
q('#scanBtn').onclick = () => run(() => api('/api/asistencia/validar', { method: 'POST', body: JSON.stringify({ token: q('#scanToken').value }) }), 'Asistencia registrada');
q('#toggleViewBtn').onclick = () => { grid = !grid; loadEvents(); };

(async function init(){ await refreshMe(); await loadEvents(); })();
