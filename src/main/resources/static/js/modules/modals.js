/**
 * MÓDULO: modals.js
 * Modal de confirmación personalizado (reemplaza window.confirm)
 * Sistema de Ventas Multi-Sucursal
 *
 * Requiere: los estilos de css/modules/components.css
 * Uso: confirmDialog('Eliminar', '¿Seguro?', () => { ... accion ... });
 */

/**
 * Muestra un diálogo modal de confirmación.
 * @param {string} title - Título del modal.
 * @param {string} text - Mensaje descriptivo.
 * @param {Function} onConfirm - Callback a ejecutar al confirmar.
 * @param {Object} [options] - Opciones avanzadas.
 * @param {string} [options.confirmLabel='Eliminar'] - Texto del botón de confirmación.
 * @param {string} [options.cancelLabel='Cancelar'] - Texto del botón de cancelar.
 * @param {string} [options.confirmColor='var(--red)'] - Color CSS del botón de confirmación.
 */
function confirmDialog(title, text, onConfirm, options = {}) {
    const {
        confirmLabel = 'Eliminar',
        cancelLabel  = 'Cancelar',
        confirmColor = 'var(--red)'
    } = options;

    // Crear el overlay
    const overlay = document.createElement('div');
    overlay.className = 'confirm-overlay';

    // Crear la caja del modal
    const box = document.createElement('div');
    box.className = 'confirm-box';

    box.innerHTML = `
        <div class="confirm-icon">
            <i class="bi bi-exclamation-triangle"></i>
        </div>
        <h4>${title}</h4>
        <p>${text}</p>
        <div class="confirm-actions">
            <button class="btn btn-ghost" id="btnConfirmCancel">${cancelLabel}</button>
            <button class="btn btn-filled" style="background:${confirmColor};" id="btnConfirmOk">
                ${confirmLabel}
            </button>
        </div>
    `;

    overlay.appendChild(box);
    document.body.appendChild(overlay);

    // Función para cerrar el diálogo con animación
    const close = () => {
        overlay.style.opacity = '0';
        overlay.style.transition = 'opacity .2s ease';
        setTimeout(() => overlay.remove(), 200);
    };

    // Event listeners
    box.querySelector('#btnConfirmCancel').addEventListener('click', close);
    box.querySelector('#btnConfirmOk').addEventListener('click', () => {
        close();
        if (typeof onConfirm === 'function') onConfirm();
    });

    // Cerrar al hacer clic en el overlay (fuera del box)
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) close();
    });
}
