/**
 * MÓDULO: paginator.js
 * Clase TablePaginator — Paginación frontend reutilizable
 * Sistema de Ventas Multi-Sucursal
 *
 * Requiere: los estilos de css/modules/tables.css
 *
 * Uso básico:
 *   const paginator = new TablePaginator(dataArray, renderFn, 10);
 *   paginator.renderCurrentPage();
 *
 * La variable global `paginator` debe estar disponible en la vista
 * para que los botones de página generados puedan llamar a paginator.goToPage().
 */
class TablePaginator {
    /**
     * @param {Array}    dataList       - Array completo de datos a paginar.
     * @param {Function} renderCallback - Función que recibe un sub-array y pinta la tabla.
     * @param {number}   rowsPerPage    - Filas por página (por defecto: 10).
     */
    constructor(dataList, renderCallback, rowsPerPage = 10) {
        this.dataList      = dataList;
        this.renderCallback = renderCallback;
        this.currentPage   = 1;
        this.rowsPerPage   = rowsPerPage;
    }

    /** Reemplaza los datos y vuelve a la página 1. */
    updateData(newDataList) {
        this.dataList    = newDataList;
        this.currentPage = 1;
        this.renderCurrentPage();
    }

    /** Cambia el tamaño de página y vuelve a la página 1. */
    setRowsPerPage(rows) {
        this.rowsPerPage = rows;
        this.currentPage = 1;
        this.renderCurrentPage();
    }

    /** Navega a una página específica (con validación de rango). */
    goToPage(pageNumber) {
        const totalPages = Math.ceil(this.dataList.length / this.rowsPerPage);
        if (pageNumber >= 1 && pageNumber <= totalPages) {
            this.currentPage = pageNumber;
            this.renderCurrentPage();
        }
    }

    /** Calcula el slice, llama al renderCallback y actualiza la UI. */
    renderCurrentPage() {
        const totalItems  = this.dataList.length;
        const totalPages  = Math.ceil(totalItems / this.rowsPerPage);

        // Corregir página si queda fuera de rango (ej. al filtrar)
        if (this.currentPage > totalPages && totalPages > 0) this.currentPage = totalPages;
        if (totalItems === 0) this.currentPage = 1;

        const startIdx = (this.currentPage - 1) * this.rowsPerPage;
        const endIdx   = startIdx + this.rowsPerPage;
        const pageData = this.dataList.slice(startIdx, endIdx);

        // Pintar la tabla a través del callback
        this.renderCallback(pageData);

        // Actualizar controles de navegación en el DOM
        this._updateUI(startIdx, endIdx, totalItems, totalPages);
    }

    /** (Privado) Actualiza el texto de info y los botones de paginación. */
    _updateUI(startIdx, endIdx, totalItems, totalPages) {
        const infoEl     = document.getElementById('pageInfoText');
        const controlsEl = document.getElementById('pageControls');

        if (!infoEl || !controlsEl) return;

        if (totalItems === 0) {
            infoEl.innerHTML   = `Mostrando <strong>0</strong> registros`;
            controlsEl.innerHTML = '';
            return;
        }

        const realEnd = Math.min(endIdx, totalItems);
        infoEl.innerHTML = `Mostrando <strong>${startIdx + 1}–${realEnd}</strong> de <strong>${totalItems}</strong> registros`;

        // Botón anterior
        let html = `
            <button class="btn-page" ${this.currentPage === 1 ? 'disabled' : ''}
                    onclick="paginator.goToPage(${this.currentPage - 1})">
                <i class="bi bi-chevron-left"></i>
            </button>
        `;

        // Ventana deslizante de hasta 5 botones de página
        let startPage = Math.max(1, this.currentPage - 2);
        let endPage   = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4 && startPage > 1) {
            startPage = Math.max(1, endPage - 4);
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `
                <button class="btn-page ${i === this.currentPage ? 'active' : ''}"
                        onclick="paginator.goToPage(${i})">${i}</button>
            `;
        }

        // Botón siguiente
        html += `
            <button class="btn-page" ${this.currentPage === totalPages ? 'disabled' : ''}
                    onclick="paginator.goToPage(${this.currentPage + 1})">
                <i class="bi bi-chevron-right"></i>
            </button>
        `;

        controlsEl.innerHTML = html;
    }
}
