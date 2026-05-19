/**
 * MÓDULO: toasts.js
 * Sistema de notificaciones tipo Toast (push temporal)
 * Sistema de Ventas Multi-Sucursal
 *
 * Requiere: los estilos de css/modules/components.css
 * Uso: showToast('success', 'Operación realizada exitosamente')
 *      showToast('error', 'Ocurrió un error')
 */

/**
 * Muestra una notificación toast temporal en la esquina inferior derecha.
 * @param {'success'|'error'} type - Tipo de notificación.
 * @param {string} message - Mensaje a mostrar.
 * @param {number} [duration=4000] - Tiempo en ms antes de desaparecer.
 */
function showToast(type, message, duration = 4000) {
    // Crear o reutilizar el stack de toasts
    let stack = document.getElementById('toastStack');
    if (!stack) {
        stack = document.createElement('div');
        stack.id = 'toastStack';
        stack.className = 'toast-stack';
        document.body.appendChild(stack);
    }

    // Crear el toast
    const toast = document.createElement('div');
    toast.className = `toast-item ${type === 'error' ? 'error' : ''}`;

    const icon = type === 'error'
        ? '<i class="bi bi-x-circle-fill toast-icon"></i>'
        : '<i class="bi bi-check-circle-fill toast-icon"></i>';

    toast.innerHTML = `${icon} <span>${message}</span>`;
    stack.appendChild(toast);

    // Auto-eliminar después del duration
    setTimeout(() => {
        toast.classList.add('out');
        setTimeout(() => toast.remove(), 300);
    }, duration);
}
