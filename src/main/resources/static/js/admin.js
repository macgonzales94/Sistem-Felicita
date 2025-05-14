/**
 * Script principal para el módulo de Administrador
 */

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar componentes
    initBootstrapComponents();
    
    // Manejar sidebar responsive
    setupSidebar();
    
    // Resaltar menú activo
    highlightActiveMenu();
    
    // Configurar manejo de formularios
    setupForms();
    
    // Configurar comportamiento específico de la página actual
    setupCurrentPage();
    
    // Cerrar alertas automáticamente después de 5 segundos
    setupAutoCloseAlerts();
});

/**
 * Inicializa componentes de Bootstrap
 */
function initBootstrapComponents() {
    // Tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

/**
 * Configura el sidebar para dispositivos móviles
 */
function setupSidebar() {
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            document.body.classList.toggle('sidebar-collapsed');
            
            // Guardar preferencia en localStorage
            localStorage.setItem('admin-sidebar-collapsed', document.body.classList.contains('sidebar-collapsed'));
        });
    }
    
    // Aplicar estado guardado
    if (localStorage.getItem('admin-sidebar-collapsed') === 'true') {
        document.body.classList.add('sidebar-collapsed');
    }
}

/**
 * Resalta el menú activo según la URL actual
 */
function highlightActiveMenu() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar .nav-link');
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.includes(href)) {
            link.classList.add('active');
        }
    });
}

/**
 * Configura el manejo de formularios
 */
function setupForms() {
    // Formulario de gestión de usuarios
    const usuarioForm = document.getElementById('usuarioForm');
    if (usuarioForm) {
        usuarioForm.addEventListener('submit', function(e) {
            const password = document.getElementById('password');
            const confirmarPassword = document.getElementById('confirmarPassword');
            
            // Si ambos campos están presentes y no coinciden
            if (password && confirmarPassword && 
                password.value && confirmarPassword.value && 
                password.value !== confirmarPassword.value) {
                e.preventDefault();
                
                const errorMsg = document.createElement('div');
                errorMsg.className = 'alert alert-danger';
                errorMsg.textContent = 'Las contraseñas no coinciden';
                
                // Insertar mensaje de error
                const firstFormGroup = usuarioForm.querySelector('.form-group');
                usuarioForm.insertBefore(errorMsg, firstFormGroup);
                
                // Scroll al inicio del formulario
                window.scrollTo(0, 0);
            }
        });
    }
}

/**
 * Configura comportamiento específico según la página actual
 */
function setupCurrentPage() {
    const currentPath = window.location.pathname;
    
    // Dashboard
    if (currentPath.includes('/admin/dashboard')) {
        initDashboard();
    }
    // Usuarios
    else if (currentPath.includes('/admin/usuarios')) {
        initUsuariosPage();
    }
    // Establecimientos
    else if (currentPath.includes('/admin/establecimientos')) {
        initEstablecimientosPage();
    }
    // Reportes
    else if (currentPath.includes('/admin/reportes')) {
        initReportesPage();
    }
    // Configuración
    else if (currentPath.includes('/admin/configuracion')) {
        initConfiguracionPage();
    }
}

/**
 * Inicializa la página de dashboard
 */
function initDashboard() {
    // Cargar estadísticas mediante fetch API
    fetch('/admin/api/estadisticas')
        .then(response => response.json())
        .then(data => {
            // Actualizar tarjetas de estadísticas
            updateStatsCards(data);
            
            // Inicializar gráficos
            initCharts(data);
        })
        .catch(error => {
            console.error('Error al cargar estadísticas:', error);
            showErrorAlert('Error al cargar las estadísticas. Por favor, recarga la página.');
        });
}

/**
 * Actualiza las tarjetas de estadísticas en el dashboard
 */
function updateStatsCards(data) {
    // Actualizar contenido de las tarjetas según los datos
    if (document.getElementById('totalUsuarios')) {
        document.getElementById('totalUsuarios').textContent = data.datosAdicionales.totalUsuarios || 0;
    }
    
    if (document.getElementById('totalClientes')) {
        document.getElementById('totalClientes').textContent = data.datosAdicionales.totalClientes || 0;
    }
    
    if (document.getElementById('totalEstablecimientos')) {
        document.getElementById('totalEstablecimientos').textContent = data.datosAdicionales.totalEstablecimientos || 0;
    }
    
    if (document.getElementById('totalProAdmins')) {
        document.getElementById('totalProAdmins').textContent = data.datosAdicionales.totalProAdmins || 0;
    }
}

/**
 * Inicializa los gráficos del dashboard
 */
function initCharts(data) {
    // Gráfico de usuarios por rol
    const usuariosPorRolCtx = document.getElementById('usuariosPorRolChart');
    if (usuariosPorRolCtx) {
        new Chart(usuariosPorRolCtx, {
            type: 'doughnut',
            data: {
                labels: ['Clientes', 'ProAdmins', 'Administradores'],
                datasets: [{
                    data: [
                        data.datosAdicionales.totalClientes || 0,
                        data.datosAdicionales.totalProAdmins || 0,
                        data.datosAdicionales.totalAdministradores || 0
                    ],
                    backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc'],
                    hoverBackgroundColor: ['#2e59d9', '#17a673', '#2c9faf'],
                    hoverBorderColor: "rgba(234, 236, 244, 1)",
                }],
            },
            options: {
                maintainAspectRatio: false,
                tooltips: {
                    backgroundColor: "rgb(255,255,255)",
                    bodyFontColor: "#858796",
                    borderColor: '#dddfeb',
                    borderWidth: 1,
                    xPadding: 15,
                    yPadding: 15,
                    displayColors: false,
                    caretPadding: 10,
                },
                legend: {
                    display: true,
                    position: 'bottom'
                },
                cutoutPercentage: 70,
            },
        });
    }
}

/**
 * Inicializa la página de usuarios
 */
function initUsuariosPage() {
    // Inicializar manejadores de eventos para filtros
    const filtroEstado = document.getElementById('filtroEstado');
    const filtroRol = document.getElementById('filtroRol');
    const filtroBusqueda = document.getElementById('filtroBusqueda');
    
    // Aplicar filtros si existen los elementos
    if (filtroEstado || filtroRol || filtroBusqueda) {
        const aplicarFiltros = function() {
            const estado = filtroEstado ? filtroEstado.value : '';
            const rol = filtroRol ? filtroRol.value : '';
            const busqueda = filtroBusqueda ? filtroBusqueda.value.toLowerCase() : '';
            
            // Filtrar filas de la tabla
            const filas = document.querySelectorAll('.table-usuarios tbody tr');
            filas.forEach(fila => {
                const estadoUsuario = fila.querySelector('[data-estado]')?.getAttribute('data-estado') || '';
                const rolesUsuario = fila.querySelector('[data-roles]')?.getAttribute('data-roles') || '';
                const textoFila = fila.textContent.toLowerCase();
                
                // Aplicar filtros
                const cumpleEstado = estado === '' || estadoUsuario === estado;
                const cumpleRol = rol === '' || rolesUsuario.includes(rol);
                const cumpleBusqueda = busqueda === '' || textoFila.includes(busqueda);
                
                // Mostrar u ocultar fila
                fila.style.display = (cumpleEstado && cumpleRol && cumpleBusqueda) ? '' : 'none';
            });
        };
        
        // Asignar eventos
        if (filtroEstado) filtroEstado.addEventListener('change', aplicarFiltros);
        if (filtroRol) filtroRol.addEventListener('change', aplicarFiltros);
        if (filtroBusqueda) filtroBusqueda.addEventListener('input', aplicarFiltros);
    }
    
    // Configurar botones de acción
    setupActionButtons();
}

/**
 * Configura los botones de acción en tablas
 */
function setupActionButtons() {
    // Botones de activar/desactivar
    document.querySelectorAll('.btn-activar').forEach(btn => {
        btn.addEventListener('click', function() {
            const usuarioId = this.getAttribute('data-id');
            if (confirm('¿Estás seguro de que deseas activar este usuario?')) {
                fetch(`/admin/api/usuarios/${usuarioId}/activar`, {
                    method: 'POST'
                })
                .then(response => response.json())
                .then(data => {
                    showSuccessAlert('Usuario activado correctamente');
                    setTimeout(() => { window.location.reload(); }, 2000);
                })
                .catch(error => {
                    console.error('Error:', error);
                    showErrorAlert('Error al activar el usuario');
                });
            }
        });
    });
    
    document.querySelectorAll('.btn-desactivar').forEach(btn => {
        btn.addEventListener('click', function() {
            const usuarioId = this.getAttribute('data-id');
            if (confirm('¿Estás seguro de que deseas desactivar este usuario?')) {
                fetch(`/admin/api/usuarios/${usuarioId}/desactivar`, {
                    method: 'POST'
                })
                .then(response => response.json())
                .then(data => {
                    showSuccessAlert('Usuario desactivado correctamente');
                    setTimeout(() => { window.location.reload(); }, 2000);
                })
                .catch(error => {
                    console.error('Error:', error);
                    showErrorAlert('Error al desactivar el usuario');
                });
            }
        });
    });
}

/**
 * Inicializa la página de establecimientos
 */
function initEstablecimientosPage() {
    // Lógica específica para la página de establecimientos
    console.log('Página de establecimientos cargada');
}

/**
 * Inicializa la página de reportes
 */
function initReportesPage() {
    // Lógica específica para la página de reportes
    console.log('Página de reportes cargada');
}

/**
 * Inicializa la página de configuración
 */
function initConfiguracionPage() {
    // Lógica específica para la página de configuración
    console.log('Página de configuración cargada');
}

/**
 * Cierra automáticamente las alertas después de un tiempo
 */
function setupAutoCloseAlerts() {
    setTimeout(function() {
        document.querySelectorAll('.alert-success, .alert-info').forEach(function(alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
}

/**
 * Muestra una alerta de éxito
 */
function showSuccessAlert(message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success alert-dismissible fade show';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Insertar al inicio del contenido
    const content = document.querySelector('.content');
    content.insertBefore(alertDiv, content.firstChild);
    
    // Cerrar automáticamente después de 5 segundos
    setTimeout(() => {
        const bsAlert = new bootstrap.Alert(alertDiv);
        bsAlert.close();
    }, 5000);
}

/**
 * Muestra una alerta de error
 */
function showErrorAlert(message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Insertar al inicio del contenido
    const content = document.querySelector('.content');
    content.insertBefore(alertDiv, content.firstChild);
}

/**
 * Formatea una fecha para mostrar
 */
function formatDate(dateString) {
    if (!dateString) return '';
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
}