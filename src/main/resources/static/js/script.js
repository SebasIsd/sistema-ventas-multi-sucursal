// Confirmación antes de eliminar
function confirmarEliminacion(mensaje) {
    return confirm(mensaje || '¿Estás seguro de eliminar este registro?');
}

// Formatear moneda
function formatearMoneda(monto) {
    return new Intl.NumberFormat('es-EC', {
        style: 'currency',
        currency: 'USD'
    }).format(monto);
}

// Mensaje de éxito
function mostrarMensaje(tipo, mensaje) {
    const div = document.createElement('div');
    div.className = `alert alert-${tipo} alert-dismissible fade show`;
    div.innerHTML = `
        ${mensaje}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.querySelector('.container').prepend(div);
    setTimeout(() => div.remove(), 5000);
}

// Validar formulario
function validarFormulario(formId) {
    const form = document.getElementById(formId);
    if (!form.checkValidity()) {
        form.reportValidity();
        return false;
    }
    return true;
}