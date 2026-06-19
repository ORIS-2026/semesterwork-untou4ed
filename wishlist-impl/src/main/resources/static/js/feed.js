if (!requireAuth()) {}

const errorEl       = document.getElementById('error');
const searchPanel   = document.getElementById('search-panel');
const feedWrapper   = document.getElementById('feed-tabs-wrapper');
const searchList    = document.getElementById('search-list');
const searchCount   = document.getElementById('searchResultsCount');
const searchClear   = document.getElementById('searchClearBtn');

// поиск подарков

function searchGiftCard(g) {
    const price = g.price != null
        ? `<span class="gift-price">${Number(g.price).toLocaleString('ru-RU')} ₽</span>`
        : '';
    const link = g.link
        ? `<a href="${g.link}" target="_blank" rel="noopener" class="gift-link">Открыть ссылку ↗</a>`
        : '';
    const type = g.ownerType === 'wishlist' ? 'Вишлист' : 'Подборка';
    return `<div class="card">
        <div class="search-gift-type">${type}</div>
        <div class="card-title">${g.title || 'Без названия'}</div>
        ${g.description ? `<div class="card-desc">${g.description}</div>` : ''}
        ${(price || link) ? `<div class="gift-item-footer" style="margin-top:8px">${price}${link}</div>` : ''}
    </div>`;
}

async function runSearch() {
    const title     = document.getElementById('searchTitle').value.trim();
    const minPrice  = document.getElementById('searchMinPrice').value;
    const maxPrice  = document.getElementById('searchMaxPrice').value;
    const ownerType = document.getElementById('searchOwnerType').value;

    const params = new URLSearchParams();
    if (title)     params.set('title',     title);
    if (minPrice)  params.set('minPrice',  minPrice);
    if (maxPrice)  params.set('maxPrice',  maxPrice);
    if (ownerType) params.set('ownerType', ownerType);

    errorEl.classList.add('hidden');

    try {
        const res = await apiFetch(`/api/v1/gifts/search?${params.toString()}`);
        if (!res.ok) throw new Error('Ошибка поиска');
        const gifts = await res.json();

        searchList.innerHTML = gifts.length
            ? gifts.map(searchGiftCard).join('')
            : '<p class="muted">Ничего не найдено</p>';

        searchCount.textContent = gifts.length
            ? `${gifts.length} ${pluralGifts(gifts.length)}`
            : '';

        // Показываем результаты, скрываем ленту
        searchPanel.classList.remove('hidden');
        feedWrapper.classList.add('hidden');
        searchClear.classList.remove('hidden');
    } catch (e) {
        errorEl.textContent = e.message;
        errorEl.classList.remove('hidden');
    }
}

function clearSearch() {
    document.getElementById('searchTitle').value    = '';
    document.getElementById('searchMinPrice').value = '';
    document.getElementById('searchMaxPrice').value = '';
    document.getElementById('searchOwnerType').value = '';

    searchPanel.classList.add('hidden');
    feedWrapper.classList.remove('hidden');
    searchClear.classList.add('hidden');
    searchList.innerHTML = '';
}

function pluralGifts(n) {
    if (n % 10 === 1 && n % 100 !== 11) return 'подарок';
    if ([2,3,4].includes(n % 10) && ![12,13,14].includes(n % 100)) return 'подарка';
    return 'подарков';
}

document.getElementById('searchBtn').addEventListener('click', runSearch);
document.getElementById('searchTitle').addEventListener('keydown', e => {
    if (e.key === 'Enter') runSearch();
});
searchClear.addEventListener('click', clearSearch);

document.querySelectorAll('.tab').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        btn.classList.add('active');
        const tab = btn.dataset.tab;
        document.getElementById('wishlists-panel').classList.toggle('hidden', tab !== 'wishlists');
        document.getElementById('compilations-panel').classList.toggle('hidden', tab !== 'compilations');
    });
});

function formatDate(iso) {
    return iso ? new Date(iso).toLocaleDateString('ru-RU') : '';
}

// подарок

function giftItem(g) {
    const price = g.price != null
        ? `<span class="gift-price">${Number(g.price).toLocaleString('ru-RU')} ₽</span>`
        : '';
    const link = g.link
        ? `<a href="${g.link}" target="_blank" rel="noopener" class="gift-link">Открыть ссылку ↗</a>`
        : '';
    return `<div class="gift-item">
        <div class="gift-item-title">${g.title || 'Без названия'}</div>
        ${g.description ? `<div class="gift-item-desc">${g.description}</div>` : ''}
        ${(price || link) ? `<div class="gift-item-footer">${price}${link}</div>` : ''}
    </div>`;
}

function giftsBlock(gifts) {
    if (!gifts || gifts.length === 0) {
        return '<div class="gift-sublist"><p class="gift-empty">Подарков пока нет</p></div>';
    }
    return `<div class="gift-sublist">${gifts.map(giftItem).join('')}</div>`;
}

// вишлисты

let wPage = 0;
const wList = document.getElementById('wishlists-list');
const wMore = document.getElementById('wishlists-more');

function wishlistCard(w, isOwn = false) {
    return `<div class="card">
        <div class="card-title">
            ${w.title || 'Без названия'}
            ${isOwn ? '<span class="badge" style="margin-left:8px;font-size:12px;">Мой</span>' : ''}
        </div>
        <div class="card-sub">@${w.authorUsername}</div>
        ${w.description ? `<div class="card-desc">${w.description}</div>` : ''}
        <div class="card-date">${formatDate(w.createdAt)}</div>
        ${giftsBlock(w.gifts)}
    </div>`;
}

async function loadOwnWishlist() {
    try {
        const meRes = await apiFetch('/api/v1/users/me');
        if (!meRes.ok) return;
        const me = await meRes.json();

        const wlRes = await apiFetch(`/api/v1/wishlists/user/${me.id}`);
        if (!wlRes.ok) return;
        const wl = await wlRes.json();

        wList.innerHTML = wishlistCard({ ...wl, authorUsername: me.username }, true);
    } catch { }
}

async function loadWishlists() {
    try {
        const res = await apiFetch(`/api/v1/wishlists/feed?page=${wPage}&size=20`);
        if (!res.ok) throw new Error('Ошибка загрузки ленты');
        const data = await res.json();
        const items = data.content || [];
        if (items.length === 0 && wPage === 0 && wList.children.length === 0) {
            wList.innerHTML = '<p class="muted">Нет вишлистов подписок. Подпишитесь на пользователей.</p>';
        } else {
            wList.innerHTML += items.map(w => wishlistCard(w)).join('');
        }
        wMore.classList.toggle('hidden', !!data.last);
        wPage++;
    } catch (e) {
        errorEl.textContent = e.message;
        errorEl.classList.remove('hidden');
    }
}

wMore.addEventListener('click', loadWishlists);
loadOwnWishlist().then(() => loadWishlists());

// подборки

let cPage = 0;
const cList = document.getElementById('compilations-list');
const cMore = document.getElementById('compilations-more');

function compilationCard(c) {
    return `<div class="card">
        <div class="card-title">${c.title || 'Без названия'}</div>
        ${c.description ? `<div class="card-desc">${c.description}</div>` : ''}
        <div class="card-date">${formatDate(c.createdAt)}</div>
        ${giftsBlock(c.gifts)}
    </div>`;
}

async function loadCompilations() {
    try {
        const res = await apiFetch(`/api/v1/compilations/feed?page=${cPage}&size=20`);
        if (!res.ok) throw new Error('Ошибка загрузки подборок');
        const data = await res.json();
        const items = data.content || [];
        if (items.length === 0 && cPage === 0) {
            cList.innerHTML = '<p class="muted">Нет подборок. Вступите в группы.</p>';
        } else {
            cList.innerHTML += items.map(compilationCard).join('');
        }
        cMore.classList.toggle('hidden', !!data.last);
        cPage++;
    } catch (e) {
        errorEl.textContent = e.message;
        errorEl.classList.remove('hidden');
    }
}

cMore.addEventListener('click', loadCompilations);
loadCompilations();
