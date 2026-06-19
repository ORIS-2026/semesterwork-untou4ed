if (!requireAuth()) {}

const errorEl    = document.getElementById('error');
const myList     = document.getElementById('my-list');
const myEmpty    = document.getElementById('my-empty');
const searchList = document.getElementById('search-list');
const searchEmpty = document.getElementById('search-empty');


const subscribed = {};

document.querySelectorAll('.tab').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        btn.classList.add('active');
        const tab = btn.dataset.tab;
        document.getElementById('my-panel').classList.toggle('hidden', tab !== 'my');
        document.getElementById('search-panel').classList.toggle('hidden', tab !== 'search');
    });
});

function userCard(u, container) {
    const isSubbed = !!subscribed[u.id];
    const displayName = [u.name, u.surname].filter(Boolean).join(' ') || u.username;
    const div = document.createElement('div');
    div.className = 'card user-card';
    div.dataset.id = u.id;
    div.innerHTML = `
        <div class="user-info">
            <div class="card-title">${displayName}</div>
            <div class="card-sub">@${u.username}</div>
        </div>
        <button class="sub-btn ${isSubbed ? 'btn-danger' : 'btn-primary'}"
                data-id="${u.id}" data-subbed="${isSubbed}">
            ${isSubbed ? 'Отписаться' : 'Подписаться'}
        </button>`;
    div.querySelector('.sub-btn').addEventListener('click', e =>
        toggleSubscription(e.currentTarget, container));
    return div;
}

async function toggleSubscription(btn, container) {
    const userId   = btn.dataset.id;
    const isSubbed = btn.dataset.subbed === 'true';
    const method   = isSubbed ? 'DELETE' : 'POST';

    btn.disabled = true;
    try {
        const res = await apiFetch(`/api/v1/subscriptions/${userId}`, { method });
        if (!res.ok && res.status !== 204) {
            const data = await res.json().catch(() => ({}));
            showError(data.message || 'Ошибка изменения подписки');
            return;
        }

        const nowSubbed = !isSubbed;
        subscribed[userId] = nowSubbed;
        btn.dataset.subbed = String(nowSubbed);
        btn.textContent = nowSubbed ? 'Отписаться' : 'Подписаться';
        btn.className = `sub-btn ${nowSubbed ? 'btn-danger' : 'btn-primary'}`;

        if (!nowSubbed && container === myList) {
            btn.closest('.user-card').remove();
            if (myList.children.length === 0) myEmpty.classList.remove('hidden');
        }
    } catch (e) {
        if (e.message !== 'Unauthorized') showError('Ошибка при изменении подписки');
    } finally {
        btn.disabled = false;
    }
}

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove('hidden');
}

async function loadMySubscriptions() {
    errorEl.classList.add('hidden');
    myList.innerHTML = '';
    myEmpty.classList.add('hidden');
    try {
        const res = await apiFetch('/api/v1/subscriptions');
        if (!res.ok) throw new Error('Ошибка загрузки подписок');
        const users = await res.json();

        // заполняем карту подписок
        users.forEach(u => { subscribed[u.id] = true; });

        if (users.length === 0) {
            myEmpty.classList.remove('hidden');
            return;
        }
        users.forEach(u => myList.appendChild(userCard(u, myList)));
    } catch (e) {
        if (e.message !== 'Unauthorized') showError(e.message);
    }
}

let debounceTimer = null;

async function doSearch(q) {
    q = q.trim();
    if (!q) {
        searchList.innerHTML = '';
        searchEmpty.classList.add('hidden');
        return;
    }
    errorEl.classList.add('hidden');
    searchList.innerHTML = '<p class="muted">Поиск...</p>';
    searchEmpty.classList.add('hidden');
    try {
        const res = await apiFetch(`/api/v1/search?q=${encodeURIComponent(q)}`);
        if (!res.ok) throw new Error('Ошибка поиска');
        const data = await res.json();
        const users = data.users || [];
        searchList.innerHTML = '';
        if (users.length === 0) {
            searchEmpty.classList.remove('hidden');
            return;
        }
        users.forEach(u => searchList.appendChild(userCard(u, searchList)));
    } catch (e) {
        searchList.innerHTML = '';
        if (e.message !== 'Unauthorized') showError(e.message);
    }
}

document.getElementById('searchBtn').addEventListener('click', () => {
    doSearch(document.getElementById('searchInput').value);
});

document.getElementById('searchInput').addEventListener('input', e => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => doSearch(e.target.value), 400);
});

document.getElementById('searchInput').addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        clearTimeout(debounceTimer);
        doSearch(e.target.value);
    }
});

// ── Init ──────────────────────────────────────────────
loadMySubscriptions();
