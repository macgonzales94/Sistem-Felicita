/**
 * Scripts principales para el módulo de cliente en FELICITA
 */

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar tooltips de Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Inicializar popovers de Bootstrap
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Menú de navegación activo
    marcarMenuActivo();
    
    // Cerrar alertas automáticamente después de 5 segundos
    setTimeout(function() {
        var alertas = document.querySelectorAll('.alert.alert-success, .alert.alert-info');
        alertas.forEach(function(alerta) {
            var bsAlert = new bootstrap.Alert(alerta);
            bsAlert.close();
        });
    }, 5000);
    
    // Confirmación para acciones importantes
    document.querySelectorAll('[data-confirm]').forEach(function(element) {
        element.addEventListener('click', function(e) {
            if (!confirm(this.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });
    
    // Funciones específicas para cada página
    var currentPath = window.location.pathname;
    
    if (currentPath.includes('/cliente/inicio')) {
        initClienteInicio();
    } else if (currentPath.includes('/cliente/establecimientos')) {
        if (currentPath.split('/').length > 3) {
            initDetalleEstablecimiento();
        } else {
            initListadoEstablecimientos();
        }
    } else if (currentPath.includes('/cliente/citas')) {
        if (currentPath.includes('/reservar')) {
            initReservarCita();
        } else if (currentPath.split('/').length > 3) {
            initDetalleCita();
        } else {
            initListadoCitas();
        }
    } else if (currentPath.includes('/cliente/giftcards')) {
        if (currentPath.includes('/enviar')) {
            initEnviarGiftCard();
        } else if (currentPath.split('/').length > 3) {
            initDetalleGiftCard();
        } else {
            initListadoGiftCards();
        }
    } else if (currentPath.includes('/cliente/perfil')) {
        initPerfil();
    }
});

/**
 * Marca el menú activo según la ruta actual
 */
function marcarMenuActivo() {
    var currentPath = window.location.pathname;
    var navItems = document.querySelectorAll('.navbar-nav .nav-link');
    
    navItems.forEach(function(item) {
        var href = item.getAttribute('href');
        if (href && currentPath.includes(href) && href !== '/') {
            item.classList.add('active');
        } else if (href === '/' && currentPath === '/') {
            item.classList.add('active');
        }
    });
}

/**
 * Inicializa la página de inicio del cliente
 */
function initClienteInicio() {
    // Cargar establecimientos recomendados
    fetch('/api/establecimientos/recomendados')
        .then(response => response.json())
        .then(data => {
            if (data.length === 0) {
                // Mostrar mensaje si no hay recomendaciones
                document.getElementById('establecimientos-recomendados-container').innerHTML = `
                    <div class="col-12 text-center py-4">
                        <i class="fas fa-store fa-3x text-muted mb-3"></i>
                        <h4>Aún no tenemos recomendaciones para ti</h4>
                        <p>A medida que uses el sistema, personalizaremos las recomendaciones.</p>
                        <a href="/cliente/establecimientos" class="btn btn-primary mt-2">
                            <i class="fas fa-search me-2"></i> Explorar establecimientos
                        </a>
                    </div>
                `;
                return;
            }
            
            // Llenar con establecimientos recomendados
            const container = document.getElementById('establecimientos-recomendados-container');
            container.innerHTML = '';
            
            data.forEach(est => {
                const estCard = document.createElement('div');
                estCard.className = 'col-md-4 mb-4';
                
                const imagenUrl = est.imagenUrl || '/img/establecimiento-default.jpg';
                const calificacion = est.calificacionPromedio || 0;
                const estrellas = '★'.repeat(Math.round(calificacion)) + '☆'.repeat(5 - Math.round(calificacion));
                
                estCard.innerHTML = `
                    <div class="card shadow-sm h-100">
                        <img src="${imagenUrl}" class="card-img-top" alt="${est.nombre}" style="height: 200px; object-fit: cover;">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h5 class="card-title">${est.nombre}</h5>
                                <span class="badge bg-primary" title="${calificacion} / 5">${estrellas}</span>
                            </div>
                            <p class="card-text mb-3">${est.descripcion || ''}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <small class="text-muted">
                                    ${est.ciudad || ''}
                                </small>
                                <a href="/cliente/establecimientos/${est.id}" class="btn btn-sm btn-outline-primary">Ver detalle</a>
                            </div>
                        </div>
                    </div>
                `;
                
                container.appendChild(estCard);
            });
        })
        .catch(error => {
            console.error('Error al cargar recomendaciones:', error);
            document.getElementById('establecimientos-recomendados-container').innerHTML = `
                <div class="col-12 text-center py-4">
                    <i class="fas fa-exclamation-triangle fa-3x text-warning mb-3"></i>
                    <h4>Error al cargar recomendaciones</h4>
                    <p>Por favor, intenta nuevamente más tarde.</p>
                </div>
            `;
        });
    
    // Cargar próximas citas
    fetch('/cliente/citas/api/pendientes')
        .then(response => response.json())
        .then(data => {
            const container = document.getElementById('proximas-citas-container');
            const tabla = document.getElementById('proximas-citas-table');
            const mensaje = document.getElementById('no-citas-mensaje');
            const tbody = document.getElementById('proximas-citas-tbody');
            
            if (data.length === 0) {
                // Mostrar mensaje si no hay citas
                tabla.style.display = 'none';
                mensaje.style.display = 'block';
                return;
            }
            
            // Llenar con próximas citas
            tabla.style.display = 'block';
            mensaje.style.display = 'none';
            tbody.innerHTML = '';
            
            // Mostrar solo las 3 citas más próximas
            const citasProximas = data.sort((a, b) => new Date(a.fechaHora) - new Date(b.fechaHora)).slice(0, 3);
            
            citasProximas.forEach(cita => {
                const fechaHora = new Date(cita.fechaHora);
                const formattedFecha = fechaHora.toLocaleDateString();
                const formattedHora = fechaHora.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                
                const fila = document.createElement('tr');
                
                let estadoBadge = '';
                switch (cita.estado) {
                    case 'PENDIENTE':
                        estadoBadge = '<span class="badge bg-warning">Pendiente</span>';
                        break;
                    case 'CONFIRMADA':
                        estadoBadge = '<span class="badge bg-success">Confirmada</span>';
                        break;
                    default:
                        estadoBadge = `<span class="badge bg-secondary">${cita.estado}</span>`;
                }
                
                fila.innerHTML = `
                    <td>${cita.nombreEstablecimiento}</td>
                    <td>${cita.nombreServicio}</td>
                    <td>${formattedFecha} ${formattedHora}</td>
                    <td>${estadoBadge}</td>
                    <td>
                        <a href="/cliente/citas/${cita.id}" class="btn btn-sm btn-outline-primary">
                            <i class="fas fa-eye"></i>
                        </a>
                    </td>
                `;
                
                tbody.appendChild(fila);
            });
        })
        .catch(error => {
            console.error('Error al cargar citas:', error);
            document.getElementById('proximas-citas-container').innerHTML = `
                <div class="alert alert-warning text-center">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    Error al cargar tus próximas citas. Por favor, intenta nuevamente más tarde.
                </div>
            `;
        });
}

/**
 * Inicializa la página de listado de establecimientos
 */
function initListadoEstablecimientos() {
    // La funcionalidad principal está implementada directamente en la vista
    console.log('Página de listado de establecimientos inicializada');
}

/**
 * Inicializa la página de detalle de establecimiento
 */
function initDetalleEstablecimiento() {
    // Implementación para la página de detalle de establecimiento
    console.log('Página de detalle de establecimiento inicializada');
}

/**
 * Inicializa la página de listado de citas
 */
function initListadoCitas() {
    // La funcionalidad principal está implementada directamente en la vista
    console.log('Página de listado de citas inicializada');
}

/**
 * Inicializa la página de reserva de citas
 */
function initReservarCita() {
    // La funcionalidad principal está implementada directamente en la vista
    console.log('Página de reserva de citas inicializada');
}

/**
 * Inicializa la página de detalle de cita
 */
function initDetalleCita() {
    // Implementación para la página de detalle de cita
    console.log('Página de detalle de cita inicializada');
}

/**
 * Inicializa la página de listado de gift cards
 */
function initListadoGiftCards() {
    // La funcionalidad principal está implementada directamente en la vista
    console.log('Página de listado de gift cards inicializada');
}

/**
 * Inicializa la página de envío de gift card
 */
function initEnviarGiftCard() {
    // Implementación para la página de envío de gift card
    console.log('Página de envío de gift card inicializada');
}

/**
 * Inicializa la página de detalle de gift card
 */
function initDetalleGiftCard() {
    // Implementación para la página de detalle de gift card
    console.log('Página de detalle de gift card inicializada');
}

/**
 * Inicializa la página de perfil
 */
function initPerfil() {
    // La funcionalidad principal está implementada directamente en la vista
    console.log('Página de perfil inicializada');
}

/**
 * Formatea fecha y hora
 * @param {string} fechaHora - Fecha y hora en formato ISO
 * @returns {string} - Fecha y hora formateada
 */
function formatearFechaHora(fechaHora) {
    const fecha = new Date(fechaHora);
    return fecha.toLocaleDateString() + ' ' + fecha.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

/**
 * Formatea moneda
 * @param {number} monto - Cantidad a formatear
 * @returns {string} - Monto formateado como moneda
 */
function formatearMoneda(monto) {
    return '$' + monto.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}