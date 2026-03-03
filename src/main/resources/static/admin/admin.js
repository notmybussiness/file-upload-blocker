const fixedContainer = document.getElementById('fixed-extensions');
const customInput = document.getElementById('custom-input');
const addButton = document.getElementById('add-custom-btn');
const customCount = document.getElementById('custom-count');
const customTags = document.getElementById('custom-tags');
const errorMessage = document.getElementById('error-message');

async function fetchJson(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
        ...options,
    });

    if (!response.ok) {
        let message = 'request failed';
        try {
            const error = await response.json();
            message = error.message || message;
        } catch (e) {
            // no-op
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function setError(message) {
    errorMessage.textContent = message || '';
}

function renderPolicy(policy) {
    fixedContainer.innerHTML = '';
    customTags.innerHTML = '';

    for (const fixed of policy.fixed) {
        const label = document.createElement('label');
        label.className = 'fixed-item';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = fixed.checked;
        checkbox.addEventListener('change', async () => {
            try {
                setError('');
                await fetchJson(`/api/v1/admin/extensions/fixed/${fixed.name}`, {
                    method: 'PATCH',
                    body: JSON.stringify({ checked: checkbox.checked }),
                });
                await loadPolicy();
            } catch (error) {
                checkbox.checked = !checkbox.checked;
                setError(error.message);
            }
        });

        const text = document.createElement('span');
        text.textContent = fixed.name;

        label.appendChild(checkbox);
        label.appendChild(text);
        fixedContainer.appendChild(label);
    }

    customCount.textContent = `${policy.custom.count}/${policy.custom.max}`;

    for (const item of policy.custom.items) {
        const tag = document.createElement('div');
        tag.className = 'tag';

        const name = document.createElement('span');
        name.textContent = item.name;

        const remove = document.createElement('button');
        remove.type = 'button';
        remove.textContent = 'X';
        remove.addEventListener('click', async () => {
            try {
                setError('');
                await fetchJson(`/api/v1/admin/extensions/custom/${item.id}`, {
                    method: 'DELETE',
                });
                await loadPolicy();
            } catch (error) {
                setError(error.message);
            }
        });

        tag.appendChild(name);
        tag.appendChild(remove);
        customTags.appendChild(tag);
    }
}

async function loadPolicy() {
    const policy = await fetchJson('/api/v1/admin/extensions/policy');
    renderPolicy(policy);
}

addButton.addEventListener('click', async () => {
    try {
        setError('');
        await fetchJson('/api/v1/admin/extensions/custom', {
            method: 'POST',
            body: JSON.stringify({ name: customInput.value }),
        });
        customInput.value = '';
        await loadPolicy();
    } catch (error) {
        setError(error.message);
    }
});

customInput.addEventListener('keydown', async (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        addButton.click();
    }
});

loadPolicy().catch((error) => {
    setError(error.message);
});
