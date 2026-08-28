// Global API Base Configuration
// Dynamically resolves to local Spring Boot when testing locally, or Railway backend when deployed on Vercel
const DEFAULT_RAILWAY_URL = "https://smart-attendance-lals.up.railway.app/api";

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
