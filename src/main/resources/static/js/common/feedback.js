// feedback.js

function showToast(message, type = 'info', duration = 2200) {
    const shell = document.querySelector('.app-shell') || document.body;

    let toastRoot = shell.querySelector('.app-toast-root');

    if (!toastRoot) {
        toastRoot = document.createElement('div');
        toastRoot.className = 'app-toast-root';
        shell.appendChild(toastRoot);
    }

    const toast = document.createElement('div');
    toast.className = `app-toast ${type}`;
    toast.innerText = message;

    toastRoot.appendChild(toast);

    setTimeout(function () {
        toast.classList.add('hide');

        setTimeout(function () {
            toast.remove();
        }, 220);
    }, duration);
}