// Global API Base Configuration
// Priority: 1) Localhost if testing locally 2) Saved custom URL 3) Default Railway URL
const DEFAULT_RAILWAY_URL = "https://smart-attendance-backend-production-1406.up.railway.app/api";

function resolveApiBase() {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        return 'http://localhost:8081/api';
    }
    const savedUrl = localStorage.getItem('CUSTOM_API_BASE');
    if (savedUrl && savedUrl.trim() !== '') {
        return savedUrl.trim().replace(/\/+$/, '');
    }
    return DEFAULT_RAILWAY_URL;
}

window.API_BASE = resolveApiBase();
var API_BASE = window.API_BASE;

window.setBackendUrl = function(newUrl) {
    if (newUrl && newUrl.trim()) {
        let clean = newUrl.trim().replace(/\/+$/, '');
        if (!clean.endsWith('/api')) clean += '/api';
        localStorage.setItem('CUSTOM_API_BASE', clean);
        window.API_BASE = clean;
        API_BASE = clean;
        return clean;
    } else {
        localStorage.removeItem('CUSTOM_API_BASE');
        window.API_BASE = resolveApiBase();
        API_BASE = window.API_BASE;
        return window.API_BASE;
    }
};
