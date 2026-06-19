function getToken() {
    return localStorage.getItem('access_token');
}

function requireAuth() {
    if (!getToken()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

function logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('kc_url');
    localStorage.removeItem('kc_realm');
    localStorage.removeItem('kc_client');
    window.location.href = '/login';
}

async function refreshToken() {
    const refreshTkn = localStorage.getItem('refresh_token');
    const kcUrl = localStorage.getItem('kc_url');
    const kcRealm = localStorage.getItem('kc_realm');
    const kcClient = localStorage.getItem('kc_client');

    if (!refreshTkn || !kcUrl || !kcRealm || !kcClient) {
        logout();
        throw new Error('Unauthorized');
    }

    const tokenUrl = `${kcUrl}/realms/${kcRealm}/protocol/openid-connect/token`;

    const res = await fetch(tokenUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            grant_type: 'refresh_token',
            client_id: kcClient,
            refresh_token: refreshTkn
        })
    });

    if (!res.ok) {
        logout();
        throw new Error('Unauthorized');
    }

    const data = await res.json();
    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);
    return data.access_token;
}

async function apiFetch(url, options = {}, isRetry = false) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        ...(options.headers || {})
    };

    const res = await fetch(url, { ...options, headers });

    if (res.status === 401) {
        if (isRetry) {
            logout();
            throw new Error('Unauthorized');
        }
        // пробуем обновить токен и повторить запрос
        await refreshToken();
        return apiFetch(url, options, true);
    }

    return res;
}
