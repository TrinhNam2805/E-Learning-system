// ===== E-Learning CNTT – Client-side helpers =====

function toggleNotif() {
    const d = document.getElementById('notif-dropdown');
    const a = document.getElementById('account-dropdown');
    if (a) a.hidden = true;
    if (d) d.hidden = !d.hidden;
}

function toggleAccount() {
    const d = document.getElementById('account-dropdown');
    const n = document.getElementById('notif-dropdown');
    if (n) n.hidden = true;
    if (d) d.hidden = !d.hidden;
}

function toggleNavMenu(button) {
    const nav = button.closest('nav');
    if (!nav) return;
    const open = nav.classList.toggle('open');
    button.setAttribute('aria-expanded', open ? 'true' : 'false');
}

function initActiveNavigation() {
    const path = window.location.pathname || '/';
    document.querySelectorAll('[data-nav-match]').forEach(function(link) {
        const raw = link.getAttribute('data-nav-match');
        const mode = link.getAttribute('data-nav-mode');
        if (!raw) return;
        const matches = raw.split('|').map(function(item) { return item.trim(); }).filter(Boolean);
        const isHome = mode === 'home' && (path === '/' || path === '/home');
        const isMatch = isHome || matches.some(function(match) {
            if (match === '/') return path === '/';
            return path === match || path.indexOf(match + '/') === 0;
        });
        if (isMatch) {
            link.classList.add('is-active');
            link.setAttribute('aria-current', 'page');
        }
    });
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('.account-menu')) {
        document.querySelectorAll('.dropdown-menu').forEach(function(m) {
            m.hidden = true;
        });
    }

    if (!e.target.closest('#top-nav')) {
        const nav = document.getElementById('top-nav');
        if (nav) {
            nav.classList.remove('open');
            const toggle = nav.querySelector('.nav-toggle');
            if (toggle) toggle.setAttribute('aria-expanded', 'false');
        }
    }
});

// Auto-hide flash messages after 4 seconds
function initPasswordToggles() {
    document.querySelectorAll('.password-field').forEach(function(wrap) {
        var input = wrap.querySelector('input');
        var btn = wrap.querySelector('.password-toggle');
        if (!input || !btn) return;
        btn.addEventListener('click', function() {
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            wrap.classList.toggle('is-visible', show);
            btn.setAttribute('aria-pressed', show ? 'true' : 'false');
            var hideLabel = 'Hide password';
            var showLabel = 'Show password';
            btn.setAttribute('aria-label', show ? hideLabel : showLabel);
            btn.setAttribute('title', show ? hideLabel : showLabel);
        });
    });
}

document.addEventListener('DOMContentLoaded', function() {
    initPasswordToggles();
    initActiveNavigation();
    const alerts = document.querySelectorAll('.alert-success, .alert-error, .alert-info');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity .5s';
            alert.style.opacity = '0';
            setTimeout(function() { alert.remove(); }, 500);
        }, 4000);
    });

    // Color picker radio buttons visual feedback
    document.querySelectorAll('input[type=radio][name=highlightColor]').forEach(function(radio) {
        radio.addEventListener('change', function() {
            document.querySelectorAll('input[type=radio][name=highlightColor]').forEach(function(r) {
                const span = r.nextElementSibling;
                if (span) span.style.border = r.checked ? '3px solid #0a66c2' : '2px solid #ccc';
            });
        });
        // Init
        if (radio.checked) {
            const span = radio.nextElementSibling;
            if (span) span.style.border = '3px solid #0a66c2';
        }
    });

    document.querySelectorAll('#top-nav a').forEach(function(link) {
        link.addEventListener('click', function() {
            const nav = document.getElementById('top-nav');
            if (!nav) return;
            nav.classList.remove('open');
            const toggle = nav.querySelector('.nav-toggle');
            if (toggle) toggle.setAttribute('aria-expanded', 'false');
        });
    });
});

function showToast(msg, type) {
    var stack = document.querySelector('.toast-stack');
    if (!stack) {
        stack = document.createElement('div');
        stack.className = 'toast-stack';
        stack.setAttribute('aria-live', 'polite');
        stack.setAttribute('aria-atomic', 'false');
        document.body.appendChild(stack);
    }
    var t = document.createElement('div');
    t.className = 'toast toast-' + (type || 'success');
    t.setAttribute('role', 'status');
    t.textContent = msg;
    stack.appendChild(t);
    setTimeout(function() { t.classList.add('show'); }, 10);
    setTimeout(function() { t.classList.remove('show'); setTimeout(function() { t.remove(); }, 400); }, 3500);
}

function getParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}
