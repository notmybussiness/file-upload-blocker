const fixedContainer = document.getElementById('fixed-extensions');
const customInput = document.getElementById('custom-input');
const addButton = document.getElementById('add-custom-btn');
const customCount = document.getElementById('custom-count');
const customTags = document.getElementById('custom-tags');
const errorMessage = document.getElementById('error-message');
const uploadInput = document.getElementById('upload-file-input');
const uploadButton = document.getElementById('upload-btn');
const uploadResult = document.getElementById('upload-result');
const fileList = document.getElementById('file-list');

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

async function uploadFile(url, file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(url, {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        let message = 'upload failed';
        try {
            const error = await response.json();
            message = error.message || message;
        } catch (e) {
            // no-op
        }
        throw new Error(message);
    }

    return response.json();
}

function setError(message) {
    errorMessage.textContent = message || '';
}

function setUploadResult(message) {
    uploadResult.textContent = message || '';
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
                await fetchJson(`/api/v1/extensions/fixed/${fixed.name}`, {
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
                await fetchJson(`/api/v1/extensions/custom/${item.id}`, {
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

function renderFiles(files) {
    fileList.innerHTML = '';

    if (files.length === 0) {
        const empty = document.createElement('tr');
        empty.innerHTML = '<td class="empty" colspan="5">업로드된 파일이 없습니다.</td>';
        fileList.appendChild(empty);
        return;
    }

    for (const file of files) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${file.originalName}</td>
            <td>${file.extension || '-'}</td>
            <td>${file.sizeBytes}</td>
            <td>${file.createdAt}</td>
            <td><a href="/api/v1/files/${file.id}/download">다운로드</a></td>
        `;
        fileList.appendChild(row);
    }
}

async function loadPolicy() {
    const policy = await fetchJson('/api/v1/extensions/policy');
    renderPolicy(policy);
}

async function loadFiles() {
    const files = await fetchJson('/api/v1/files');
    renderFiles(files);
}

addButton.addEventListener('click', async () => {
    try {
        setError('');
        await fetchJson('/api/v1/extensions/custom', {
            method: 'POST',
            body: JSON.stringify({ name: customInput.value }),
        });
        customInput.value = '';
        await loadPolicy();
    } catch (error) {
        setError(error.message);
    }
});

uploadButton.addEventListener('click', async () => {
    try {
        setError('');
        setUploadResult('');

        if (!uploadInput.files || uploadInput.files.length === 0) {
            throw new Error('file is required');
        }

        const uploaded = await uploadFile('/api/v1/files', uploadInput.files[0]);
        setUploadResult(`업로드 성공: ${uploaded.originalName}`);
        uploadInput.value = '';
        await loadFiles();
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

Promise.all([loadPolicy(), loadFiles()]).catch((error) => {
    setError(error.message);
});
