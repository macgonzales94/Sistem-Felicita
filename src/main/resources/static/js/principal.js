/**
 * Script principal mejorado para FELICITA
 * Incluye animaciones, interactividad y funcionalidades avanzadas
 */

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar componentes
    initBootstrapComponents();
    initAnimations();
    initSearchFunctionality();
    initServiceCards();
    initEstablishmentCards();
    setupSmoothScrolling();
    setupJWTHandling();
    
    // Cerrar alertas automáticamente
    setupAutoCloseAlerts();
    
    // Optimizar layout después de que se carguen las imágenes
    window.addEventListener('load', function() {
        setTimeout(() => {
            optimizarLayoutServicios();
        }, 100);
    });
    
    // Optimizar layout al cambiar el tamaño de ventana
    window.addEventListener('resize', debounce(optimizarLayoutServicios, 250));
    
    console.log('FELICITA - Página principal cargada correctamente');
});

/**
 * Inicializar componentes de Bootstrap
 */
function initBootstrapComponents() {
    // Inicializar tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Inicializar popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

/**
 * Inicializar animaciones con Intersection Observer
 */
function initAnimations() {
    // Crear observer para animaciones de entrada
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in-up');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observar elementos para animar
    const elementsToAnimate = document.querySelectorAll(
        '.card, .categoria-card, .service-card, .establishment-card, .feature-icon'
    );
    
    elementsToAnimate.forEach(el => {
        observer.observe(el);
    });
    
    // Animaciones de contadores para estadísticas
    animateCounters();
}

/**
 * Animar contadores numéricos
 */
function animateCounters() {
    const counters = document.querySelectorAll('.hero h3');
    
    counters.forEach(counter => {
        const target = parseInt(counter.textContent);
        if (isNaN(target)) return;
        
        let current = 0;
        const increment = target / 50; // Dividir en 50 pasos
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                counter.textContent = target;
                clearInterval(timer);
            } else {
                counter.textContent = Math.floor(current);
            }
        }, 50);
    });
}

/**
 * Configurar funcionalidad de búsqueda
 */
function initSearchFunctionality() {
    const formBusqueda = document.querySelector('#buscar form');
    if (!formBusqueda) return;
    
    const servicioInput = formBusqueda.querySelector('input[placeholder*="servicio"]');
    const ubicacionInput = formBusqueda.querySelector('input[placeholder*="ubicación"]');
    
    // Autocompletado para servicios
    if (servicioInput) {
        setupServiceAutocomplete(servicioInput);
    }
    
    // Autocompletado para ubicaciones
    if (ubicacionInput) {
        setupLocationAutocomplete(ubicacionInput);
    }
    
    // Manejar envío del formulario
    formBusqueda.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const servicio = servicioInput?.value || '';
        const ubicacion = ubicacionInput?.value || '';
        
        // Validar que al menos un campo esté completo
        if (!servicio.trim() && !ubicacion.trim()) {
            mostrarAlerta('Por favor ingresa un servicio o ubicación para buscar', 'warning');
            return;
        }
        
        // Mostrar loading
        const submitBtn = this.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Buscando...';
        submitBtn.disabled = true;
        
        // Simular búsqueda (en una implementación real, harías una llamada AJAX)
        setTimeout(() => {
            // Redirigir a página de resultados
            const params = new URLSearchParams();
            if (servicio.trim()) params.append('servicio', servicio);
            if (ubicacion.trim()) params.append('ubicacion', ubicacion);
            
            window.location.href = `/cliente/establecimientos?${params.toString()}`;
        }, 1000);
    });
}

/**
 * Configurar autocompletado para servicios
 */
function setupServiceAutocomplete(input) {
    const servicios = [
        'Corte de cabello', 'Corte y barba', 'Arreglo de barba', 'Afeitado clásico',
        'Maquillaje', 'Manicura', 'Pedicura', 'Tratamiento capilar', 'Coloración',
        'Peinado', 'Depilación', 'Tratamiento facial', 'Spa', 'Masaje'
    ];
    
    setupAutocomplete(input, servicios);
}

/**
 * Configurar autocompletado para ubicaciones
 */
function setupLocationAutocomplete(input) {
    const ubicaciones = [
        'Lima Centro', 'Miraflores', 'San Isidro', 'Surco', 'La Molina',
        'Barranco', 'Chorrillos', 'San Borja', 'Magdalena', 'Pueblo Libre'
    ];
    
    setupAutocomplete(input, ubicaciones);
}

/**
 * Configurar autocompletado genérico
 */
function setupAutocomplete(input, suggestions) {
    let currentFocus = -1;
    
    input.addEventListener('input', function() {
        const value = this.value.toLowerCase();
        closeAllLists();
        
        if (!value) return;
        
        const matches = suggestions.filter(item => 
            item.toLowerCase().includes(value)
        );
        
        if (matches.length === 0) return;
        
        const listDiv = document.createElement('div');
        listDiv.className = 'autocomplete-list';
        listDiv.style.cssText = `
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border: 1px solid #ddd;
            border-top: none;
            border-radius: 0 0 8px 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            z-index: 1000;
            max-height: 200px;
            overflow-y: auto;
        `;
        
        matches.forEach((item, index) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'autocomplete-item';
            itemDiv.style.cssText = `
                padding: 10px;
                cursor: pointer;
                border-bottom: 1px solid #eee;
                transition: background-color 0.2s;
            `;
            itemDiv.textContent = item;
            
            itemDiv.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#f8f9fa';
            });
            
            itemDiv.addEventListener('mouseleave', function() {
                this.style.backgroundColor = 'white';
            });
            
            itemDiv.addEventListener('click', function() {
                input.value = item;
                closeAllLists();
            });
            
            listDiv.appendChild(itemDiv);
        });
        
        // Posicionar relative al input
        input.parentNode.style.position = 'relative';
        input.parentNode.appendChild(listDiv);
    });
    
    input.addEventListener('keydown', function(e) {
        const list = this.parentNode.querySelector('.autocomplete-list');
        if (!list) return;
        
        const items = list.querySelectorAll('.autocomplete-item');
        
        if (e.keyCode === 40) { // Arrow Down
            currentFocus++;
            setActiveItem(items);
        } else if (e.keyCode === 38) { // Arrow Up
            currentFocus--;
            setActiveItem(items);
        } else if (e.keyCode === 13) { // Enter
            e.preventDefault();
            if (currentFocus > -1 && items[currentFocus]) {
                items[currentFocus].click();
            }
        }
    });
    
    function setActiveItem(items) {
        if (!items) return;
        
        items.forEach(item => item.classList.remove('active'));
        
        if (currentFocus >= items.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = items.length - 1;
        
        if (items[currentFocus]) {
            items[currentFocus].classList.add('active');
            items[currentFocus].style.backgroundColor = '#007bff';
            items[currentFocus].style.color = 'white';
        }
    }
    
    function closeAllLists() {
        document.querySelectorAll('.autocomplete-list').forEach(list => {
            list.remove();
        });
        currentFocus = -1;
    }
    
    // Cerrar lista al hacer clic fuera
    document.addEventListener('click', function(e) {
        if (!input.contains(e.target)) {
            closeAllLists();
        }
    });
}

/**
 * Inicializar tarjetas de servicios
 */
function initServiceCards() {
    const serviceCards = document.querySelectorAll('.service-card');
    
    serviceCards.forEach(card => {
        // Efecto hover mejorado
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px) scale(1.02)';
            this.style.boxShadow = '0 15px 35px rgba(0,0,0,0.15)';
            this.style.zIndex = '10';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 2px 10px rgba(0,0,0,0.05)';
            this.style.zIndex = '1';
        });
        
        // Click para ir al establecimiento (solo si no es un botón)
        card.addEventListener('click', function(e) {
            // Evitar que se active si se hace click en un botón
            if (e.target.tagName === 'BUTTON' || e.target.closest('button')) {
                return;
            }
            
            const establecimientoId = this.dataset.establecimientoId;
            
            if (establecimientoId) {
                // Efecto de click
                this.style.transform = 'scale(0.98)';
                setTimeout(() => {
                    window.location.href = `/cliente/establecimientos/${establecimientoId}`;
                }, 150);
            }
        });
        
        // Animación del icono al pasar el mouse
        const icon = card.querySelector('.service-icon i');
        if (icon) {
            card.addEventListener('mouseenter', function() {
                icon.style.transform = 'scale(1.2) rotate(5deg)';
            });
            
            card.addEventListener('mouseleave', function() {
                icon.style.transform = 'scale(1) rotate(0deg)';
            });
        }
    });
}

/**
 * Mejorar layout de servicios en tiempo real
 */
function optimizarLayoutServicios() {
    const serviceCards = document.querySelectorAll('.service-card');
    
    // Asegurar altura uniforme por fila
    let currentRow = [];
    let currentTop = null;
    
    serviceCards.forEach(card => {
        const rect = card.getBoundingClientRect();
        
        if (currentTop === null || Math.abs(rect.top - currentTop) < 10) {
            // Misma fila
            currentRow.push(card);
            currentTop = rect.top;
        } else {
            // Nueva fila
            if (currentRow.length > 1) {
                ajustarAlturaFila(currentRow);
            }
            currentRow = [card];
            currentTop = rect.top;
        }
    });
    
    // Ajustar la última fila
    if (currentRow.length > 1) {
        ajustarAlturaFila(currentRow);
    }
}

/**
 * Ajustar altura de tarjetas en la misma fila
 */
function ajustarAlturaFila(cards) {
    // Resetear alturas
    cards.forEach(card => {
        card.style.height = 'auto';
    });
    
    // Encontrar la altura máxima
    let maxHeight = 0;
    cards.forEach(card => {
        const height = card.offsetHeight;
        if (height > maxHeight) {
            maxHeight = height;
        }
    });
    
    // Aplicar la altura máxima a todas las tarjetas
    cards.forEach(card => {
        card.style.height = maxHeight + 'px';
    });
}

/**
 * Inicializar tarjetas de establecimientos
 */
function initEstablishmentCards() {
    const establishmentCards = document.querySelectorAll('.establishment-card');
    
    establishmentCards.forEach(card => {
        // Efecto parallax en la imagen
        const img = card.querySelector('.card-img-top');
        
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px)';
            this.style.boxShadow = '0 25px 50px rgba(0,0,0,0.15)';
            
            if (img) {
                img.style.transform = 'scale(1.1)';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 2px 10px rgba(0,0,0,0.05)';
            
            if (img) {
                img.style.transform = 'scale(1)';
            }
        });
        
        // Animación de las estrellas de rating
        const stars = card.querySelectorAll('.rating-stars i');
        let delay = 0;
        
        card.addEventListener('mouseenter', function() {
            stars.forEach(star => {
                setTimeout(() => {
                    star.style.transform = 'scale(1.2) rotate(10deg)';
                    setTimeout(() => {
                        star.style.transform = 'scale(1) rotate(0deg)';
                    }, 200);
                }, delay);
                delay += 50;
            });
            delay = 0;
        });
    });
}

/**
 * Configurar scroll suave
 */
function setupSmoothScrolling() {
    // Scroll suave para enlaces internos
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Mostrar/ocultar botón "volver arriba"
    createBackToTopButton();
}

/**
 * Crear botón "volver arriba"
 */
function createBackToTopButton() {
    const backToTopButton = document.createElement('button');
    backToTopButton.innerHTML = '<i class="fas fa-chevron-up"></i>';
    backToTopButton.className = 'btn btn-primary back-to-top';
    backToTopButton.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        border: none;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        box-shadow: 0 4px 15px rgba(0,0,0,0.3);
        z-index: 1000;
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s ease;
        cursor: pointer;
    `;
    
    backToTopButton.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    
    document.body.appendChild(backToTopButton);
    
    // Mostrar/ocultar según scroll
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopButton.style.opacity = '1';
            backToTopButton.style.visibility = 'visible';
        } else {
            backToTopButton.style.opacity = '0';
            backToTopButton.style.visibility = 'hidden';
        }
    });
}

/**
 * Configurar manejo de JWT
 */
function setupJWTHandling() {
    // Verificar si hay token JWT
    const jwtToken = localStorage.getItem('jwtToken');
    
    if (jwtToken) {
        // Agregar token a todas las peticiones fetch
        const originalFetch = window.fetch;
        window.fetch = function(url, options = {}) {
            const newOptions = {...options};
            newOptions.headers = newOptions.headers || {};
            newOptions.headers.Authorization = `Bearer ${jwtToken}`;
            
            return originalFetch(url, newOptions);
        };
    }
    
    // Manejar login con JWT si existe el botón
    const btnLoginApi = document.getElementById('login-api');
    if (btnLoginApi) {
        btnLoginApi.addEventListener('click', handleJWTLogin);
    }
}

/**
 * Manejar login con JWT
 */
function handleJWTLogin() {
    const email = document.getElementById('email')?.value;
    const password = document.getElementById('password')?.value;
    
    if (!email || !password) {
        mostrarAlerta('Por favor completa todos los campos', 'danger');
        return;
    }
    
    const loginData = { email, password };
    
    // Mostrar loading
    const originalText = event.target.innerHTML;
    event.target.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Iniciando sesión...';
    event.target.disabled = true;
    
    fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Credenciales incorrectas');
        }
        return response.json();
    })
    .then(data => {
        // Guardar token
        localStorage.setItem('jwtToken', data.token);
        localStorage.setItem('userRole', data.roles[0]);
        
        // Mostrar mensaje de éxito
        mostrarAlerta('Inicio de sesión exitoso', 'success');
        
        // Redirigir según el rol
        setTimeout(() => {
            if (data.roles.includes('ADMINISTRADOR')) {
                window.location.href = '/admin/dashboard';
            } else if (data.roles.includes('PROADMIN')) {
                window.location.href = '/proadmin/dashboard';
            } else if (data.roles.includes('CLIENTE')) {
                window.location.href = '/cliente/inicio';
            } else {
                window.location.reload();
            }
        }, 1000);
    })
    .catch(error => {
        mostrarAlerta(error.message, 'danger');
        event.target.innerHTML = originalText;
        event.target.disabled = false;
    });
}

/**
 * Configurar cierre automático de alertas
 */
function setupAutoCloseAlerts() {
    setTimeout(function() {
        const alertas = document.querySelectorAll('.alert-success, .alert-info');
        alertas.forEach(function(alerta) {
            if (typeof bootstrap !== 'undefined') {
                const bsAlert = new bootstrap.Alert(alerta);
                bsAlert.close();
            } else {
                alerta.style.opacity = '0';
                alerta.style.transition = 'opacity 1s';
                setTimeout(() => alerta.style.display = 'none', 1000);
            }
        });
    }, 5000);
}

/**
 * Mostrar alertas personalizadas
 */
function mostrarAlerta(mensaje, tipo = 'info', duracion = 5000) {
    // Crear contenedor de alertas si no existe
    let alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alert-container';
        alertContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            width: 350px;
        `;
        document.body.appendChild(alertContainer);
    }
    
    // Crear alerta
    const alertaDiv = document.createElement('div');
    alertaDiv.className = `alert alert-${tipo} alert-dismissible fade show mb-2`;
    alertaDiv.style.cssText = `
        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        border-radius: 10px;
        border: none;
        animation: slideInRight 0.3s ease;
    `;
    
    alertaDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="fas fa-${getAlertIcon(tipo)} me-2"></i>
            <span class="flex-grow-1">${mensaje}</span>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        </div>
    `;
    
    // Agregar CSS para animación
    if (!document.getElementById('custom-alert-styles')) {
        const style = document.createElement('style');
        style.id = 'custom-alert-styles';
        style.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOutRight {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }
    
    alertContainer.appendChild(alertaDiv);
    
    // Auto-cerrar después del tiempo especificado
    if (duracion > 0) {
        setTimeout(() => {
            if (alertaDiv.parentNode) {
                alertaDiv.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => {
                    if (alertaDiv.parentNode) {
                        alertaDiv.remove();
                    }
                }, 300);
            }
        }, duracion);
    }
}

/**
 * Obtener icono para el tipo de alerta
 */
function getAlertIcon(tipo) {
    const iconos = {
        'success': 'check-circle',
        'danger': 'exclamation-triangle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle',
        'primary': 'info-circle'
    };
    return iconos[tipo] || 'info-circle';
}

/**
 * Funciones de utilidad
 */

// Formatear moneda
function formatearMoneda(monto) {
    return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: 'PEN',
        minimumFractionDigits: 2
    }).format(monto);
}

// Formatear fecha
function formatearFecha(fecha) {
    return new Intl.DateTimeFormat('es-PE', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    }).format(new Date(fecha));
}

// Debounce para optimizar eventos
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Detectar dispositivo móvil
function esMobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

// Lazy loading para imágenes
function initLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                observer.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// Inicializar cuando se carga el contenido
document.addEventListener('DOMContentLoaded', function() {
    initLazyLoading();
});

// Exportar funciones para uso global
window.FELICITA = {
    mostrarAlerta,
    formatearMoneda,
    formatearFecha,
    debounce,
    esMobile
};