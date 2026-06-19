if (!requireAuth()) {}

const errorEl = document.getElementById('error');
const groupsView = document.getElementById('groups-view');
const detailView = document.getElementById('group-detail-view');

document.getElementById('showCreateBtn').addEventListener('click', () => {
    document.getElementById('createForm').classList.remove('hidden');
});
document.getElementById('cancelCreateBtn').addEventListener('click', () => {
    document.getElementById('createForm').classList.add('hidden');
});

document.getElementById('createForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('groupName').value.trim();
    const description = document.getElementById('groupDesc').value.trim();
    try {
        const res = await apiFetch('/api/v1/groups', { method: 'POST', body: JSON.stringify({ name, description }) });
        if (!res.ok) {
            const data = await res.json();
            showError(data.message || 'Ошибка создания группы');
            return;
        }
        document.getElementById('createForm').classList.add('hidden');
        document.getElementById('createForm').reset();
        loadGroups();
    } catch { showError('Не удалось подключиться к серверу'); }
});

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove('hidden');
}

function formatDate(iso) {
    return iso ? new Date(iso).toLocaleDateString('ru-RU') : '';
}

async function loadGroups() {
    errorEl.classList.add('hidden');
    const list = document.getElementById('groups-list');
    try {
        const res = await apiFetch('/api/v1/groups');
        if (!res.ok) throw new Error('Ошибка загрузки групп');
        const groups = await res.json();
        if (groups.length === 0) {
            list.innerHTML = '<p class="muted">У вас нет групп. Создайте первую или найдите через поиск!</p>';
            return;
        }
        list.innerHTML = groups.map(g => `
            <div class="card card-clickable" data-id="${g.id}">
                <div class="card-title">${g.name}</div>
                ${g.description ? `<div class="card-desc">${g.description}</div>` : ''}
                <div class="card-date">${formatDate(g.createdAt)}</div>
            </div>
        `).join('');
        list.querySelectorAll('.card-clickable').forEach(card => {
            card.addEventListener('click', () => openGroup(card.dataset.id));
        });
    } catch (e) { showError(e.message); }
}

let currentGroupId = null;

async function openGroup(groupId) {
    errorEl.classList.add('hidden');
    currentGroupId = groupId;
    try {
        const res = await apiFetch(`/api/v1/groups/${groupId}`);
        if (!res.ok) throw new Error('Ошибка загрузки группы');
        const g = await res.json();

        document.getElementById('detail-name').textContent = g.name;
        document.getElementById('detail-desc').textContent = g.description || '';

        const canManage = g.memberStatus === 'creator' || g.memberStatus === 'admin';

        // кнопка добавить подборку — только для creator/admin
        const addCompBtn = document.getElementById('addCompilationBtn');
        addCompBtn.classList.toggle('hidden', !canManage);
        document.getElementById('compilationGroupId').value = groupId;

        const actions = document.getElementById('detail-actions');
        if (g.memberStatus === 'creator') {
            actions.innerHTML = '<span class="badge">Вы создатель</span>';
        } else if (g.memberStatus === 'admin') {
            actions.innerHTML = '<span class="badge">Вы администратор</span>';
        } else if (g.memberStatus === 'member') {
            actions.innerHTML = `<button class="btn-danger" onclick="leaveGroup('${groupId}')">Покинуть группу</button>`;
        } else {
            actions.innerHTML = `<button class="btn-primary" onclick="joinGroup('${groupId}')">Вступить</button>`;
        }

        renderCompilations(g.compilations || [], canManage);

        groupsView.classList.add('hidden');
        detailView.classList.remove('hidden');
    } catch (e) { showError(e.message); }
}

function renderCompilations(compilations, canManage) {
    const compEl = document.getElementById('detail-compilations');
    if (compilations.length === 0) {
        compEl.innerHTML = '<p class="muted">Подборок пока нет</p>';
        return;
    }
    compEl.innerHTML = compilations.map(c => `
        <div class="card" data-compilation-id="${c.id}">
            <div class="card-title">${c.title || 'Без названия'}</div>
            ${c.description ? `<div class="card-desc">${c.description}</div>` : ''}
            ${canManage ? `<button class="btn-primary" style="margin-top:8px;"
                onclick="openGiftModal('${c.id}', 'COMPILATION')">+ Добавить подарок</button>` : ''}
            <div class="gift-sublist" id="gifts-${c.id}" style="margin-top:8px;"></div>
        </div>
    `).join('');

    compilations.forEach(c => loadCompilationGifts(c.id));
}

async function loadCompilationGifts(compilationId) {
    try {
        const res = await apiFetch(`/api/v1/gifts?ownerId=${compilationId}&ownerType=COMPILATION`);
        if (!res.ok) return;
        const gifts = await res.json();
        const el = document.getElementById(`gifts-${compilationId}`);
        if (!el) return;
        if (gifts.length === 0) {
            el.innerHTML = '<p class="muted" style="font-size:13px;">Подарков пока нет</p>';
            return;
        }
        el.innerHTML = gifts.map(g => `
            <div class="card" style="margin-top:6px; background:#f9f9f9;">
                <div class="card-title" style="font-size:14px;">${g.title}</div>
                ${g.description ? `<div class="card-desc">${g.description}</div>` : ''}
                ${g.link ? `<div class="card-desc"><a href="${g.link}" target="_blank">Ссылка</a></div>` : ''}
                ${g.price ? `<div class="card-sub">${g.price} ₽</div>` : ''}
            </div>
        `).join('');
    } catch { /* ignore */ }
}

async function joinGroup(groupId) {
    try {
        await apiFetch(`/api/v1/groups/${groupId}/members`, { method: 'POST' });
        openGroup(groupId);
    } catch (e) { showError(e.message); }
}

async function leaveGroup(groupId) {
    try {
        await apiFetch(`/api/v1/groups/${groupId}/members`, { method: 'DELETE' });
        openGroup(groupId);
    } catch (e) { showError(e.message); }
}

document.getElementById('backBtn').addEventListener('click', () => {
    detailView.classList.add('hidden');
    groupsView.classList.remove('hidden');
    loadGroups();
});

async function openCompilationModal() {
    document.getElementById('compilationError').classList.add('hidden');
    document.getElementById('compilationSuccess').classList.add('hidden');
    document.getElementById('addCompilationForm').reset();
    document.getElementById('compilationGroupId').value = currentGroupId;

    // загрузить категории если ещё не загружены
    const select = document.getElementById('compilationCategory');
    if (select.options.length <= 1) {
        try {
            const res = await fetch('/api/v1/categories');
            const categories = await res.json();
            categories.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.id;
                opt.textContent = c.name;
                select.appendChild(opt);
            });
        } catch { }
    }

    document.getElementById('compilationModal').style.display = 'flex';
}

function closeCompilationModal() {
    document.getElementById('compilationModal').style.display = 'none';
}

document.getElementById('addCompilationForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const compilationError = document.getElementById('compilationError');
    compilationError.classList.add('hidden');

    const body = {
        groupId: document.getElementById('compilationGroupId').value,
        title: document.getElementById('compilationTitle').value,
        description: document.getElementById('compilationDesc').value,
        categoryId: parseInt(document.getElementById('compilationCategory').value)
    };

    try {
        const res = await apiFetch('/api/v1/compilations', { method: 'POST', body: JSON.stringify(body) });
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            compilationError.textContent = data.message || 'Ошибка создания подборки';
            compilationError.classList.remove('hidden');
            return;
        }
        document.getElementById('compilationSuccess').classList.remove('hidden');
        setTimeout(() => {
            closeCompilationModal();
            openGroup(currentGroupId);
        }, 1000);
    } catch (err) {
        if (err.message !== 'Unauthorized') {
            compilationError.textContent = 'Не удалось подключиться к серверу';
            compilationError.classList.remove('hidden');
        }
    }
});

function openGiftModal(ownerId, ownerType) {
    document.getElementById('giftOwnerId').value = ownerId;
    document.getElementById('giftOwnerType').value = ownerType;
    document.getElementById('giftError').classList.add('hidden');
    document.getElementById('giftSuccess').classList.add('hidden');
    document.getElementById('addGiftForm').reset();
    document.getElementById('giftModal').style.display = 'flex';
}

function closeGiftModal() {
    document.getElementById('giftModal').style.display = 'none';
}

document.getElementById('addGiftForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const giftError = document.getElementById('giftError');
    giftError.classList.add('hidden');

    const ownerId = document.getElementById('giftOwnerId').value;
    const ownerType = document.getElementById('giftOwnerType').value;

    const body = {
        name: document.getElementById('giftTitle').value,
        description: document.getElementById('giftDesc').value,
        link: document.getElementById('giftLink').value,
        price: parseFloat(document.getElementById('giftPrice').value) || 0,
        ownerId,
        ownerType
    };

    try {
        const res = await apiFetch('/api/v1/gifts', { method: 'POST', body: JSON.stringify(body) });
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            giftError.textContent = data.message || 'Ошибка добавления подарка';
            giftError.classList.remove('hidden');
            return;
        }
        document.getElementById('giftSuccess').classList.remove('hidden');
        document.getElementById('addGiftForm').reset();
        setTimeout(() => {
            closeGiftModal();
            loadCompilationGifts(ownerId);
        }, 1000);
    } catch (err) {
        if (err.message !== 'Unauthorized') {
            giftError.textContent = 'Не удалось подключиться к серверу';
            giftError.classList.remove('hidden');
        }
    }
});

window.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) e.target.style.display = 'none';
});

document.getElementById('searchGroupForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const query = document.getElementById('searchGroupInput').value.trim();
    if (!query) return;
    await searchGroups(query);
});

async function searchGroups(query) {
    const resultList = document.getElementById('search-groups-list');
    resultList.innerHTML = '<p class="muted">Поиск...</p>';
    errorEl.classList.add('hidden');
    try {
        const res = await apiFetch(`/api/v1/search?q=${encodeURIComponent(query)}`);
        if (!res.ok) throw new Error('Ошибка поиска');
        const data = await res.json();
        const groups = data.groups || [];
        if (groups.length === 0) {
            resultList.innerHTML = '<p class="muted">Группы не найдены</p>';
            return;
        }
        resultList.innerHTML = groups.map(g => `
            <div class="card">
                <div class="card-title">${g.name}</div>
                ${g.description ? `<div class="card-desc">${g.description}</div>` : ''}
                <div style="margin-top:8px;">
                    <button class="btn-primary" onclick="openGroup('${g.id}')">Открыть</button>
                </div>
            </div>
        `).join('');
    } catch (e) { showError(e.message); }
}

loadGroups();
