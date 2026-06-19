if (!requireAuth()) {}

const errorEl = document.getElementById('error');
let currentWishlistId = null;

const SVG_DEFAULT = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Ccircle cx='50' cy='50' r='50' fill='%23d1d5db'/%3E%3Ccircle cx='50' cy='38' r='16' fill='%23fff'/%3E%3Cellipse cx='50' cy='75' rx='26' ry='18' fill='%23fff'/%3E%3C/svg%3E";


document.querySelectorAll('#tabs-bar .tab').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('#tabs-bar .tab').forEach(t => t.classList.remove('active'));
        btn.classList.add('active');
        const tab = btn.dataset.tab;
        document.getElementById('profile').classList.toggle('hidden', tab !== 'profile');
        document.getElementById('wishlist-section').classList.toggle('hidden', tab !== 'wishlist');
        document.getElementById('followers-section').classList.toggle('hidden', tab !== 'followers');
    });
});

apiFetch('/api/v1/users/me')
    .then(res => { if (!res.ok) throw new Error('Ошибка загрузки профиля'); return res.json(); })
    .then(data => {
        document.getElementById('profileUsername').textContent = data.username || '—';
        document.getElementById('profileName').textContent     = data.name     || '—';
        document.getElementById('profileSurname').textContent  = data.surname  || '—';
        document.getElementById('profileEmail').textContent    = data.email    || '—';
        document.getElementById('profilePhone').textContent    = data.phoneNumber || '—';
        document.getElementById('profile').classList.remove('hidden');
        document.getElementById('tabs-bar').classList.remove('hidden');
        loadAvatar();
        loadWishlist(data.id);
        loadFollowers();
    })
    .catch(err => {
        if (err.message !== 'Unauthorized') {
            errorEl.textContent = err.message;
            errorEl.classList.remove('hidden');
        }
    });

const avatarImg    = document.getElementById('avatarImg');
const avatarLoader = document.getElementById('avatarLoader');
const avatarError  = document.getElementById('avatarError');
const avatarInput  = document.getElementById('avatarInput');

async function loadAvatar() {
    try {
        const res = await apiFetch('/api/v1/users/me/avatar');
        if (!res.ok) return; // аватар ещё не загружен — показываем дефолтный
        const url = await res.text();
        avatarImg.src = url.replace(/^"|"$/g, '');
    } catch { /* нет аватара — ничего не делаем */ }
}

// SVG-заглушка
avatarImg.addEventListener('error', () => {
    avatarImg.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Ccircle cx='50' cy='50' r='50' fill='%23d1d5db'/%3E%3Ccircle cx='50' cy='38' r='16' fill='%23fff'/%3E%3Cellipse cx='50' cy='75' rx='26' ry='18' fill='%23fff'/%3E%3C/svg%3E";
});

avatarInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        avatarError.textContent = 'Можно загружать только изображения';
        avatarError.classList.remove('hidden');
        return;
    }

    avatarError.classList.add('hidden');
    avatarImg.style.opacity = '0.4';
    avatarLoader.classList.remove('hidden');

    try {
        // 1. Получаем presigned upload data
        const urlRes = await apiFetch('/api/v1/users/me/avatar/upload-url');
        if (!urlRes.ok) throw new Error('Не удалось получить URL для загрузки');
        const { uploadUrl, formData } = await urlRes.json();

        // 2. Собираем multipart FormData для S3 presigned POST
        const fd = new FormData();
        Object.entries(formData).forEach(([k, v]) => fd.append(k, v));
        fd.append('Content-Type', file.type);
        fd.append('file', file);  // file — последним

        // 3. Загружаем напрямую в MinIO (без Authorization)
        const uploadRes = await fetch(uploadUrl, { method: 'POST', body: fd });
        if (!uploadRes.ok) {
            const text = await uploadRes.text();
            console.error('MinIO upload error:', text);
            throw new Error('Ошибка загрузки файла');
        }

        // 4. Получаем свежую ссылку для отображения
        await loadAvatar();
    } catch (err) {
        avatarError.textContent = err.message;
        avatarError.classList.remove('hidden');
    } finally {
        avatarImg.style.opacity = '1';
        avatarLoader.classList.add('hidden');
        e.target.value = ''; // сбросить, чтобы можно было выбрать тот же файл снова
    }
});

async function loadWishlist(userId) {
    const res = await apiFetch(`/api/v1/wishlists/user/${userId}`);
    if (!res.ok) return;
    const wl = await res.json();
    currentWishlistId = wl.id;
    document.getElementById('wishlistTitle').textContent = wl.title       || '';
    document.getElementById('wishlistDesc').textContent  = wl.description || '';
    document.getElementById('wishlist-section').classList.remove('hidden');
    loadGifts(wl.id);
}

async function loadGifts(wishlistId) {
    const res = await apiFetch(`/api/v1/gifts?ownerId=${wishlistId}&ownerType=WISHLIST`);
    if (!res.ok) return;
    const gifts = await res.json();
    const list = document.getElementById('gifts-list');
    if (gifts.length === 0) {
        list.innerHTML = '<p class="muted">Подарков пока нет. Добавьте первый!</p>';
        return;
    }
    list.innerHTML = gifts.map(g => `
        <div class="card">
            <div class="card-title">${g.title}</div>
            ${g.description ? `<div class="card-desc">${g.description}</div>` : ''}
            ${g.link ? `<div class="card-desc"><a href="${g.link}" target="_blank">Ссылка</a></div>` : ''}
            ${g.price ? `<div class="card-sub">${g.price} ₽</div>` : ''}
        </div>
    `).join('');
}

function openGiftModal() {
    document.getElementById('giftOwnerId').value = currentWishlistId;
    document.getElementById('giftOwnerType').value = 'WISHLIST';
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

    const body = {
        name:        document.getElementById('giftTitle').value,
        description: document.getElementById('giftDesc').value,
        link:        document.getElementById('giftLink').value,
        price:       parseFloat(document.getElementById('giftPrice').value) || 0,
        ownerId:     document.getElementById('giftOwnerId').value,
        ownerType:   document.getElementById('giftOwnerType').value
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
            loadGifts(currentWishlistId);
        }, 1000);
    } catch (err) {
        if (err.message !== 'Unauthorized') {
            giftError.textContent = 'Не удалось подключиться к серверу';
            giftError.classList.remove('hidden');
        }
    }
});

document.getElementById('editWishlistBtn').addEventListener('click', () => {
    document.getElementById('wlTitle').value = document.getElementById('wishlistTitle').textContent;
    document.getElementById('wlDesc').value  = document.getElementById('wishlistDesc').textContent;
    document.getElementById('editWishlistModal').style.display = 'flex';
});

function closeEditWishlistModal() {
    document.getElementById('editWishlistModal').style.display = 'none';
}

document.getElementById('editWishlistForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
        title:       document.getElementById('wlTitle').value,
        description: document.getElementById('wlDesc').value
    };
    try {
        const res = await apiFetch('/api/v1/wishlists/me', { method: 'PATCH', body: JSON.stringify(body) });
        if (!res.ok) return;
        const wl = await res.json();
        document.getElementById('wishlistTitle').textContent = wl.title       || '';
        document.getElementById('wishlistDesc').textContent  = wl.description || '';
        closeEditWishlistModal();
    } catch { /* handled by apiFetch */ }
});

window.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) e.target.style.display = 'none';
});

document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/login';
});

async function loadFollowers() {
    const list = document.getElementById('followers-list');
    try {
        const res = await apiFetch('/api/v1/subscriptions/followers');
        if (!res.ok) return;
        const followers = await res.json();

        const count = followers.length;
        document.getElementById('followersTotal').textContent      = count;
        document.getElementById('followersCountBadge').textContent = count;

        if (count === 0) {
            list.innerHTML = '<p class="muted">Подписчиков пока нет.</p>';
            return;
        }

        list.innerHTML = '';
        for (const u of followers) {
            list.appendChild(followerCard(u));
        }

        for (const u of followers) {
            loadFollowerAvatar(u.id);
        }
    } catch (e) {
        if (e.message !== 'Unauthorized') {
            list.innerHTML = '<p class="muted">Не удалось загрузить подписчиков.</p>';
        }
    }
}

function followerCard(u) {
    const displayName = [u.name, u.surname].filter(Boolean).join(' ') || u.username;
    const div = document.createElement('div');
    div.className = 'card follower-card';
    div.style.cursor = 'pointer';
    div.innerHTML = `
        <img id="avatar-${u.id}" class="follower-avatar" src="${SVG_DEFAULT}" alt="">
        <div class="follower-info">
            <div class="card-title">${displayName}</div>
            <div class="card-sub">@${u.username}</div>
        </div>
        <span style="color:#4f46e5; font-size:13px;">→</span>
    `;
    div.addEventListener('click', () => { window.location.href = `/users/${u.id}`; });
    return div;
}

async function loadFollowerAvatar(userId) {
    try {
        const res = await apiFetch(`/api/v1/users/${userId}/avatar`);
        if (!res.ok) return;
        const url = await res.text();
        const img = document.getElementById(`avatar-${userId}`);
        if (img) img.src = url.replace(/^"|"$/g, '');
    } catch { /* нет аватара — остаётся дефолтный */ }
}

// удаление аккаунта
const deleteModal    = document.getElementById('deleteAccountModal');
const deleteErrorEl  = document.getElementById('deleteAccountError');

document.getElementById('deleteAccountBtn').addEventListener('click', () => {
    deleteErrorEl.classList.add('hidden');
    deleteModal.style.display = 'flex';
});

document.getElementById('cancelDeleteBtn').addEventListener('click', () => {
    deleteModal.style.display = 'none';
});

document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
    const btn = document.getElementById('confirmDeleteBtn');
    btn.disabled = true;
    btn.textContent = 'Удаление...';
    deleteErrorEl.classList.add('hidden');

    try {
        const res = await apiFetch('/api/v1/users/me', { method: 'DELETE' });
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            deleteErrorEl.textContent = data.message || 'Ошибка при удалении аккаунта';
            deleteErrorEl.classList.remove('hidden');
            return;
        }
        localStorage.clear();
        window.location.href = '/login';
    } catch (err) {
        if (err.message !== 'Unauthorized') {
            deleteErrorEl.textContent = 'Не удалось подключиться к серверу';
            deleteErrorEl.classList.remove('hidden');
        }
    } finally {
        btn.disabled = false;
        btn.textContent = 'Да, удалить';
    }
});
