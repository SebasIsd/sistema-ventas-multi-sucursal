// script.js - Utilidades globales para el Sistema de Ventas

// ==========================================
// TOASTS GLOBALES (Reemplaza a mostrarMensaje)
// ==========================================
function showToast(type, message) {
    let stack = document.getElementById('toastStack');
    if (!stack) {
        stack = document.createElement('div');
        stack.id = 'toastStack';
        stack.className = 'toast-stack';
        document.body.appendChild(stack);
    }

    const toast = document.createElement('div');
    toast.className = `toast-item ${type === 'error' ? 'error' : ''}`;
    
    const icon = type === 'error' 
        ? '<i class="fas fa-circle-xmark toast-icon"></i>' 
        : '<i class="fas fa-circle-check toast-icon"></i>';

    toast.innerHTML = `${icon} <span>${message}</span>`;
    
    stack.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('out');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ==========================================
// MODAL DE CONFIRMACIÓN (Reemplaza a confirm())
// ==========================================
function confirmDialog(title, text, onConfirm) {
    const overlay = document.createElement('div');
    overlay.className = 'confirm-overlay';
    
    const box = document.createElement('div');
    box.className = 'confirm-box';
    
    box.innerHTML = `
        <div class="confirm-icon"><i class="fas fa-triangle-exclamation"></i></div>
        <h4>${title}</h4>
        <p>${text}</p>
        <div class="confirm-actions">
            <button class="btn btn-ghost" id="btnConfirmCancel">Cancelar</button>
            <button class="btn btn-filled" style="background:var(--red);" id="btnConfirmOk">Eliminar</button>
        </div>
    `;
    
    overlay.appendChild(box);
    document.body.appendChild(overlay);
    
    const close = () => {
        overlay.style.opacity = '0';
        setTimeout(() => overlay.remove(), 200);
    };
    
    document.getElementById('btnConfirmCancel').addEventListener('click', close);
    document.getElementById('btnConfirmOk').addEventListener('click', () => {
        close();
        if (onConfirm) onConfirm();
    });
}

// ==========================================
// FORMATEO
// ==========================================
function formatCurrency(monto) {
    return new Intl.NumberFormat('es-EC', {
        style: 'currency',
        currency: 'USD'
    }).format(monto || 0);
}

// ==========================================
// PAGINADOR FRONTEND (Lógica reutilizable)
// ==========================================
class TablePaginator {
    constructor(dataList, renderCallback, rowsPerPage = 10) {
        this.dataList = dataList;
        this.renderCallback = renderCallback;
        this.currentPage = 1;
        this.rowsPerPage = rowsPerPage;
    }

    updateData(newDataList) {
        this.dataList = newDataList;
        this.currentPage = 1;
        this.renderCurrentPage();
    }

    setRowsPerPage(rows) {
        this.rowsPerPage = rows;
        this.currentPage = 1;
        this.renderCurrentPage();
    }

    goToPage(pageNumber) {
        const totalPages = Math.ceil(this.dataList.length / this.rowsPerPage);
        if (pageNumber >= 1 && pageNumber <= totalPages) {
            this.currentPage = pageNumber;
            this.renderCurrentPage();
        }
    }

    renderCurrentPage() {
        const totalItems = this.dataList.length;
        const totalPages = Math.ceil(totalItems / this.rowsPerPage);
        
        // Corregir página si queda fuera de rango al filtrar
        if (this.currentPage > totalPages && totalPages > 0) this.currentPage = totalPages;
        if (totalItems === 0) this.currentPage = 1;

        const startIdx = (this.currentPage - 1) * this.rowsPerPage;
        const endIdx = startIdx + this.rowsPerPage;
        const pageData = this.dataList.slice(startIdx, endIdx);

        // Llamar al callback que pinta la tabla
        this.renderCallback(pageData);

        // Actualizar controles UI si existen en la vista
        this.updatePaginatorUI(startIdx, endIdx, totalItems, totalPages);
    }

    updatePaginatorUI(startIdx, endIdx, totalItems, totalPages) {
        const infoEl = document.getElementById('pageInfoText');
        const controlsEl = document.getElementById('pageControls');
        
        if (!infoEl || !controlsEl) return;

        if (totalItems === 0) {
            infoEl.innerHTML = `Mostrando <strong>0</strong> registros`;
            controlsEl.innerHTML = '';
            return;
        }

        const realEnd = Math.min(endIdx, totalItems);
        infoEl.innerHTML = `Mostrando <strong>${startIdx + 1}-${realEnd}</strong> de <strong>${totalItems}</strong> registros`;

        let btnsHtml = `
            <button class="btn-page" ${this.currentPage === 1 ? 'disabled' : ''} 
                    onclick="paginator.goToPage(${this.currentPage - 1})">
                <i class="fas fa-chevron-left"></i>
            </button>
        `;

        // Lógica simple para mostrar botones (máx 5 botones de página)
        let startPage = Math.max(1, this.currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);
        
        if (endPage - startPage < 4 && startPage > 1) {
            startPage = Math.max(1, endPage - 4);
        }

        for (let i = startPage; i <= endPage; i++) {
            btnsHtml += `
                <button class="btn-page ${i === this.currentPage ? 'active' : ''}" 
                        onclick="paginator.goToPage(${i})">${i}</button>
            `;
        }

        btnsHtml += `
            <button class="btn-page" ${this.currentPage === totalPages ? 'disabled' : ''} 
                    onclick="paginator.goToPage(${this.currentPage + 1})">
                <i class="fas fa-chevron-right"></i>
            </button>
        `;

        controlsEl.innerHTML = btnsHtml;
    }
}