/**
 * Scripts principales para el módulo ProAdmin en FELICITA
 */

document.addEventListener('DOMContentLoaded', function() {
    // Manejar el menú lateral
    setupSidebar();
    
    // Inicializar tooltips y popovers de Bootstrap
    initBootstrapComponents();
    
    // Marcar la sección activa en el menú
    highlightActiveMenu();
    
    // Cerrar alertas automáticamente después de 5 segundos
    setupAutoCloseAlerts();
});

/**
 * Configura el funcionamiento del menú lateral
 */
function setupSidebar() {
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    const content = document.querySelector('.content');
    const sidebar = document.querySelector('.sidebar');
    
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            document.body.classList.toggle('sidebar-collapsed');
            
            // Guardar preferencia en localStorage
            const isCollapsed = document.body.classList.contains('sidebar-collapsed');
            localStorage.setItem('sidebar-collapsed', isCollapsed);
        });
    }
    
    // Aplicar estado guardado del menú
    const savedState = localStorage.getItem('sidebar-collapsed');
    if (savedState === 'true') {
        document.body.classList.add('sidebar-collapsed');
    }
    
    // En dispositivos móviles, cerrar el menú al hacer clic en un enlace
    if (window.innerWidth <= 768) {
        const navLinks = document.querySelectorAll('.sidebar-link');
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (sidebar && sidebar.classList.contains('active')) {
                    sidebar.classList.remove('active');
                }
            });
        });
        
        // Mostrar/ocultar menú en móviles
        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', function() {
                if (sidebar) {
                    sidebar.classList.toggle('active');
                }
            });
        }
    }
}

/**
 * Inicializa componentes de Bootstrap
 */
function initBootstrapComponents() {
    // Tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Toasts
    var toastElList = [].slice.call(document.querySelectorAll('.toast'));
    toastElList.map(function (toastEl) {
        return new bootstrap.Toast(toastEl);
    });
}

/**
 * Marca la sección activa en el menú lateral
 */
function highlightActiveMenu() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar-link');
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        
        // Si el href está contenido en el path actual o coincide exactamente
        if (currentPath.includes(href) && href !== '/' || currentPath === href) {
            link.classList.add('active');
        }
    });
}

/**
 * Configura el cierre automático de alertas
 */
function setupAutoCloseAlerts() {
    setTimeout(function() {
        var alertsSuccess = document.querySelectorAll('.alert-success, .alert-info');
        alertsSuccess.forEach(function(alert) {
            if (typeof bootstrap !== 'undefined') {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            } else {
                alert.style.display = 'none';
            }
        });
    }, 5000);
}

/**
 * Función para confirmar acciones importantes
 * @param {string} mensaje - Mensaje de confirmación
 * @param {Function} callbackFn - Función a ejecutar si se confirma
 */
function confirmarAccion(mensaje, callbackFn) {
    if (confirm(mensaje)) {
        callbackFn();
    }
}

/**
 * Formatea una fecha para mostrarla
 * @param {string} fechaStr - Fecha en formato ISO
 * @returns {string} - Fecha formateada
 */
function formatearFecha(fechaStr) {
    const fecha = new Date(fechaStr);
    return fecha.toLocaleDateString();
}

/**
 * Formatea un importe como moneda
 * @param {number} importe - Importe a formatear
 * @returns {string} - Importe formateado
 */
function formatearMoneda(importe) {
    return '$' + Number(importe).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

/**
 * Muestra un mensaje de notificación
 * @param {string} mensaje - Mensaje a mostrar
 * @param {string} tipo - Tipo de mensaje (success, danger, info, warning)
 */
function mostrarNotificacion(mensaje, tipo = 'success') {
    // Crear elemento de notificación
    const notificacion = document.createElement('div');
    notificacion.className = `toast align-items-center text-white bg-${tipo} border-0`;
    notificacion.setAttribute('role', 'alert');
    notificacion.setAttribute('aria-live', 'assertive');
    notificacion.setAttribute('aria-atomic', 'true');
    
    notificacion.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${mensaje}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;
    
    // Agregar al contenedor de notificaciones
    const container = document.getElementById('toast-container');
    if (!container) {
        // Si no existe el contenedor, crear uno
        const newContainer = document.createElement('div');
        newContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        newContainer.id = 'toast-container';
        document.body.appendChild(newContainer);
        newContainer.appendChild(notificacion);
    } else {
        container.appendChild(notificacion);
    }
    
    // Mostrar notificación
    const toast = new bootstrap.Toast(notificacion);
    toast.show();
    
    // Eliminar después de cerrar
    notificacion.addEventListener('hidden.bs.toast', function() {
        notificacion.remove();
    });
}