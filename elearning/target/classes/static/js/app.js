// ===== E-Learning CNTT – Client-side helpers =====

function toggleNotif() {
    const d = document.getElementById('notif-dropdown');
    const a = document.getElementById('account-dropdown');
    if (a) a.style.display = 'none';
    if (d) d.style.display = d.style.display === 'none' ? 'block' : 'none';
}

function toggleAccount() {
    const d = document.getElementById('account-dropdown');
    const n = document.getElementById('notif-dropdown');
    if (n) n.style.display = 'none';
    if (d) d.style.display = d.style.display === 'none' ? 'block' : 'none';
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('.account-menu')) {
        document.querySelectorAll('.dropdown-menu').forEach(function(m) {
            m.style.display = 'none';
        });
    }
});

// Auto-hide flash messages after 4 seconds
document.addEventListener('DOMContentLoaded', function() {
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
});
