const uploadInput = document.getElementById('upload-file-input');
const uploadButton = document.getElementById('upload-btn');
const uploadResult = document.getElementById('upload-result');
const fileList = document.getElementById('file-list');
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

async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/v1/client/files', {
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
            <td><a href="/api/v1/client/files/${file.id}/download">다운로드</a></td>
        `;
        fileList.appendChild(row);
    }
}

async function loadFiles() {
    const files = await fetchJson('/api/v1/client/files');
    renderFiles(files);
}

function setError(message) {
    errorMessage.textContent = message || '';
}

function setUploadResult(message) {
    uploadResult.textContent = message || '';
}

uploadButton.addEventListener('click', async () => {
    try {
        setError('');
        setUploadResult('');

        if (!uploadInput.files || uploadInput.files.length === 0) {
            throw new Error('file is required');
        }

        const uploaded = await uploadFile(uploadInput.files[0]);
        setUploadResult(`업로드 성공: ${uploaded.originalName}`);
        uploadInput.value = '';
        await loadFiles();
    } catch (error) {
        setError(error.message);
    }
});

loadFiles().catch((error) => {
    setError(error.message);
});
