// ===== E-Learning CNTT – Client-side helpers =====

function toggleNotif() {
    const d = document.getElementById('notif-dropdown');
    const a = document.getElementById('account-dropdown');
    if (a) a.classList.add('hidden');
    if (d) d.classList.toggle('hidden');
}

function toggleAccount() {
    const d = document.getElementById('account-dropdown');
    const n = document.getElementById('notif-dropdown');
    if (n) n.classList.add('hidden');
    if (d) d.classList.toggle('hidden');
}

function setMobileNavOpen(open) {
    const panel = document.getElementById('mobile-nav-panel');
    const toggle = document.getElementById('mobile-nav-toggle');
    if (!panel || !toggle) return;
    panel.classList.toggle('hidden', !open);
    toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    document.body.classList.toggle('mobile-nav-open', open);
}

function initMobileNavigation() {
    const panel = document.getElementById('mobile-nav-panel');
    const toggle = document.getElementById('mobile-nav-toggle');
    if (!panel || !toggle) return;

    toggle.addEventListener('click', function(event) {
        event.preventDefault();
        event.stopPropagation();
        setMobileNavOpen(panel.classList.contains('hidden'));
    });

    panel.addEventListener('click', function(event) {
        if (event.target === panel) {
            setMobileNavOpen(false);
        }
    });

    panel.querySelectorAll('a').forEach(function(link) {
        link.addEventListener('click', function() {
            setMobileNavOpen(false);
        });
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            setMobileNavOpen(false);
        }
    });
}

function initActiveNavigation() {
    const currentPath = window.location.pathname;
    document.querySelectorAll('#top-nav a, #mobile-nav-panel a').forEach(function(link) {
        try {
            const linkPath = new URL(link.href, window.location.origin).pathname;
            if (linkPath === currentPath) {
                link.classList.add('text-primary');
            }
        } catch (e) {
        }
    });
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('.account-menu')) {
        document.querySelectorAll('#notif-dropdown, #account-dropdown').forEach(function(m) {
            if (m) m.classList.add('hidden');
        });
    }

    if (!e.target.closest('#top-nav')) {
        setMobileNavOpen(false);
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
    initMobileNavigation();
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
            setMobileNavOpen(false);
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
