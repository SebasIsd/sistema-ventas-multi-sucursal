/**
 * script.js — Nota de compatibilidad
 * Sistema de Ventas Multi-Sucursal
 *
 * Este archivo ya NO contiene la lógica directamente.
 * El código fue dividido en módulos en js/modules/:
 *
 *   ├── modules/formatters.js  → formatCurrency(), formatDate(), truncateText()
 *   ├── modules/toasts.js      → showToast()
 *   ├── modules/modals.js      → confirmDialog()
 *   └── modules/paginator.js   → class TablePaginator
 *
 * Todos estos módulos se cargan desde layout/base.html (fragmento "scripts").
 * Este archivo se mantiene vacío para no romper referencias antiguas.
 */