// Global API Base Configuration
// Dynamically resolves to local Spring Boot when testing locally, or Railway backend when deployed on Vercel
window.API_BASE = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
    ? 'http://localhost:8081/api'
    : 'https://smart-attendance-backend-production-1406.up.railway.app/api';

var API_BASE = window.API_BASE;
