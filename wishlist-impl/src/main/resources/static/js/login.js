const container = document.querySelector('.container');
const kcUrl = container.dataset.kcUrl;
const kcRealm = container.dataset.kcRealm;
const kcClient = container.dataset.kcClient;

const tokenUrl = `${kcUrl}/realms/${kcRealm}/protocol/openid-connect/token`;

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById('error');
    errorEl.classList.add('hidden');

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const body = new URLSearchParams({
        grant_type: 'password',
        client_id: kcClient,
        username,
        password
    });

    try {
        const res = await fetch(tokenUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body
        });

        const data = await res.json();

        if (!res.ok) {
            errorEl.textContent = data.error_description || 'Ошибка входа';
            errorEl.classList.remove('hidden');
            return;
        }

        localStorage.setItem('access_token', data.access_token);
        localStorage.setItem('refresh_token', data.refresh_token);
        localStorage.setItem('kc_url', kcUrl);
        localStorage.setItem('kc_realm', kcRealm);
        localStorage.setItem('kc_client', kcClient);
        window.location.href = '/feed';
    } catch {
        errorEl.textContent = 'Не удалось подключиться к серверу';
        errorEl.classList.remove('hidden');
    }
});
