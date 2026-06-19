if (!requireAuth()) {}

const userId     = location.pathname.split('/').pop();
const errorEl    = document.getElementById('error');
const avatarImg  = document.getElementById('userAvatarImg');
const subBtn     = document.getElementById('subBtn');

const SVG_DEFAULT = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100'%3E%3Ccircle cx='50' cy='50' r='50' fill='%23d1d5db'/%3E%3Ccircle cx='50' cy='38' r='16' fill='%23fff'/%3E%3Cellipse cx='50' cy='75' rx='26' ry='18' fill='%23fff'/%3E%3C/svg%3E";

avatarImg.addEventListener('error', () => { avatarImg.src = SVG_DEFAULT; });

async function loadUser() {
    const res = await apiFetch(`/api/v1/users/${userId}`);
    if (!res.ok) {
        errorEl.textContent = 'Пользователь не найден';
        errorEl.classList.remove('hidden');
        return;
    }
    const u = await res.json();
    const displayName = [u.name, u.surname].filter(Boolean).join(' ') || u.username;
    document.getElementById('userDisplayName').textContent = displayName;
    document.getElementById('userUsername').textContent    = '@' + u.username;
    document.getElementById('user-profile').classList.remove('hidden');

    loadUserAvatar();
    loadSubscriptionStatus();
    loadWishlist(u.id);
}

async function loadUserAvatar() {
    try {
        const res = await apiFetch(`/api/v1/users/${userId}/avatar`);
        if (!res.ok) { avatarImg.src = SVG_DEFAULT; return; }
        const url = await res.text();
        avatarImg.src = url.replace(/^"|"$/g, '');
    } catch { avatarImg.src = SVG_DEFAULT; }
}

async function loadSubscriptionStatus() {
    const res = await apiFetch('/api/v1/subscriptions');
    if (!res.ok) return;
    const subs = await res.json();
    const isSubbed = subs.some(u => u.id === userId);
    renderSubBtn(isSubbed);
}

function renderSubBtn(isSubbed) {
    subBtn.textContent  = isSubbed ? 'Отписаться' : 'Подписаться';
    subBtn.className    = isSubbed ? 'btn-danger'  : 'btn-primary';
    subBtn.dataset.subbed = String(isSubbed);
}

subBtn.addEventListener('click', async () => {
    const isSubbed = subBtn.dataset.subbed === 'true';
    const method   = isSubbed ? 'DELETE' : 'POST';
    subBtn.disabled = true;
    try {
        const res = await apiFetch(`/api/v1/subscriptions/${userId}`, { method });
        if (res.ok || res.status === 204) renderSubBtn(!isSubbed);
    } finally {
        subBtn.disabled = false;
    }
});

async function loadWishlist(uid) {
    const res = await apiFetch(`/api/v1/wishlists/user/${uid}`);
    if (!res.ok) return;
    const wl = await res.json();
    document.getElementById('wishlistTitle').textContent = wl.title       || 'Вишлист';
    document.getElementById('wishlistDesc').textContent  = wl.description || '';
    document.getElementById('wishlist-section').classList.remove('hidden');
    loadGifts(wl.id);
}

async function loadGifts(wishlistId) {
    const res = await apiFetch(`/api/v1/gifts?ownerId=${wishlistId}&ownerType=WISHLIST`);
    if (!res.ok) return;
    const gifts = await res.json();
    const list  = document.getElementById('gifts-list');
    if (gifts.length === 0) {
        list.innerHTML = '<p class="muted">Подарков пока нет.</p>';
        return;
    }
    list.innerHTML = gifts.map(g => `
        <div class="card">
            <div class="card-title">${g.title}</div>
            ${g.description ? `<div class="card-desc">${g.description}</div>` : ''}
            ${g.link  ? `<div class="card-desc"><a href="${g.link}" target="_blank">Ссылка</a></div>` : ''}
            ${g.price ? `<div class="card-sub">${g.price} ₽</div>` : ''}
        </div>
    `).join('');
}

loadUser();
