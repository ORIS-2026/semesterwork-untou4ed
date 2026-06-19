let registrationId = null;

const errorEl = document.getElementById('error');
const successEl = document.getElementById('success');
const registerForm = document.getElementById('registerForm');
const verifyForm = document.getElementById('verifyForm');
const verifyHint = document.getElementById('verifyHint');
const resendBtn = document.getElementById('resendBtn');

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove('hidden');
}

function hideMessages() {
    errorEl.classList.add('hidden');
    successEl.classList.add('hidden');
}

function startCooldown() {
    let seconds = 60;
    resendBtn.disabled = true;
    resendBtn.textContent = `Повторно через ${seconds} сек.`;

    const timer = setInterval(() => {
        seconds--;
        if (seconds <= 0) {
            clearInterval(timer);
            resendBtn.disabled = false;
            resendBtn.textContent = 'Отправить код повторно';
        } else {
            resendBtn.textContent = `Повторно через ${seconds} сек.`;
        }
    }, 1000);
}

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    const body = {
        name: document.getElementById('name').value,
        surname: document.getElementById('surname').value,
        username: document.getElementById('username').value,
        number: document.getElementById('number').value,
        password: document.getElementById('password').value,
        passwordRepeat: document.getElementById('passwordRepeat').value
    };

    try {
        const res = await fetch('/api/v1/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        const data = await res.json();

        if (!res.ok) {
            if (Array.isArray(data)) {
                showError(data.map(e => e.message).join('\n'));
            } else {
                showError(data.message || data.error || JSON.stringify(data));
            }
            return;
        }

        registrationId = data.registrationId;
        verifyHint.textContent = `Код отправлен на номер ${data.phoneNumber}`;
        registerForm.classList.add('hidden');
        verifyForm.classList.remove('hidden');
        startCooldown();
    } catch {
        showError('Не удалось подключиться к серверу');
    }
});

verifyForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessages();

    const code = document.getElementById('code').value;

    try {
        const res = await fetch('/api/v1/auth/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ registrationId, code })
        });

        const data = await res.json();

        if (!res.ok || !data.verified) {
            showError('Неверный код. Попробуйте ещё раз.');
            return;
        }

        successEl.textContent = 'Номер подтверждён! Перенаправляем на страницу входа...';
        successEl.classList.remove('hidden');
        setTimeout(() => window.location.href = '/login', 2000);
    } catch {
        showError('Не удалось подключиться к серверу');
    }
});

resendBtn.addEventListener('click', async () => {
    hideMessages();

    try {
        const res = await fetch('/api/v1/auth/send-code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ registrationId })
        });

        if (!res.ok) {
            const data = await res.json();
            showError(data.message || 'Не удалось отправить код');
            return;
        }

        startCooldown();
        successEl.textContent = 'Код отправлен повторно';
        successEl.classList.remove('hidden');
    } catch {
        showError('Не удалось подключиться к серверу');
    }
});
