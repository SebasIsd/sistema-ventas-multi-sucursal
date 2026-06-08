/**
 * MÓDULO: formatters.js
 * Funciones de formato de datos: moneda, fechas, textos
 * Sistema de Ventas Multi-Sucursal
 */

/**
 * Formatea un número como moneda en USD con localización es-EC.
 * @param {number} monto - El valor a formatear.
 * @returns {string} - Ejemplo: "$12.50"
 */
function formatCurrency(monto) {
    return new Intl.NumberFormat('es-EC', {
        style: 'currency',
        currency: 'USD'
    }).format(monto || 0);
}

/**
 * Formatea una fecha ISO a formato local legible.
 * @param {string} isoString - Fecha en formato ISO 8601.
 * @returns {string} - Ejemplo: "18 may. 2026"
 */
function formatDate(isoString) {
    if (!isoString) return '—';
    return new Date(isoString).toLocaleDateString('es-EC', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    });
}

/**
 * Trunca un texto si supera el límite de caracteres.
 * @param {string} text - Texto a truncar.
 * @param {number} limit - Máximo de caracteres (por defecto 50).
 * @returns {string}
 */
function truncateText(text, limit = 50) {
    if (!text) return '—';
    return text.length > limit ? text.substring(0, limit) + '...' : text;
}
