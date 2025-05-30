/**
 * MODO OSCURO PARA PROADMIN - FELICITA
 * Sistema de alternancia entre modo claro y oscuro
 */

class ModoOscuroProAdmin {
    constructor() {
        this.init();
    }

    init() {
        this.crearBotonAlternancia();
        this.cargarTemaGuardado();
        this.agregarEventListeners();
        this.aplicarTransiciones();
    }

    /**
     * Crear el botón de alternancia de tema
     */
    crearBotonAlternancia() {
        // Verificar si ya existe el botón
        if (document.getElementById('theme-toggle')) {
            return;
        }

        const boton = document.createElement('button');
        boton.id = 'theme-toggle';
        boton.className = 'theme-toggle';
        boton.setAttribute('aria-label', 'Alternar modo oscuro');
        boton.title = 'Alternar modo oscuro';
        
        boton.innerHTML = `
            <i class="fas fa-moon theme-icon-light"></i>
            <i class="fas fa-sun theme-icon-dark"></i>
        `;

        document.body.appendChild(boton);
    }

    /**
     * Agregar event listeners
     */
    agregarEventListeners() {
        const boton = document.getElementById('theme-toggle');
        if (boton) {
            boton.addEventListener('click', () => this.alternarTema());
        }

        // Escuchar cambios en las preferencias del sistema
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
            // Solo aplicar si no hay preferencia guardada
            if (!localStorage.getItem('tema-proadmin')) {
                this.aplicarTema(e.matches ? 'dark' : 'light');
            }
        });
    }

    /**
     * Aplicar transiciones suaves a todos los elementos
     */
    aplicarTransiciones() {
        // Agregar clase de transición a elementos principales
        const elementos = document.querySelectorAll('body, .card, .sidebar, .btn, .form-control, .modal-content, .alert, .table');
        elementos.forEach(elemento => {
            elemento.classList.add('theme-transition');
        });
    }

    /**
     * Cargar tema guardado o detectar preferencia del sistema
     */
    cargarTemaGuardado() {
        const temaGuardado = localStorage.getItem('tema-proadmin');
        
        if (temaGuardado) {
            this.aplicarTema(temaGuardado);
        } else {
            // Detectar preferencia del sistema
            const prefiereModoOscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
            this.aplicarTema(prefiereModoOscuro ? 'dark' : 'light');
        }
    }

    /**
     * Alternar entre modo claro y oscuro
     */
    alternarTema() {
        const temaActual = this.obtenerTemaActual();
        const nuevoTema = temaActual === 'dark' ? 'light' : 'dark';
        
        this.aplicarTema(nuevoTema);
        this.guardarTema(nuevoTema);

        // Animación del botón
        this.animarBoton();

        // Emitir evento personalizado para que otros componentes puedan reaccionar
        this.emitirEventoCambioTema(nuevoTema);
    }

    /**
     * Aplicar tema específico
     */
    aplicarTema(tema) {
        const body = document.body;
        const html = document.documentElement;
        
        if (tema === 'dark') {
            html.setAttribute('data-theme', 'dark');
            body.classList.add('modo-oscuro');
        } else {
            html.removeAttribute('data-theme');
            body.classList.remove('modo-oscuro');
        }

        // Actualizar gráficos si existen
        this.actualizarGraficos(tema);
        
        // Actualizar calendario si existe
        this.actualizarCalendario(tema);
    }

    /**
     * Obtener tema actual
     */
    obtenerTemaActual() {
        return document.documentElement.getAttribute('data-theme') || 'light';
    }

    /**
     * Guardar tema en localStorage
     */
    guardarTema(tema) {
        localStorage.setItem('tema-proadmin', tema);
    }

    /**
     * Animación del botón al hacer clic
     */
    animarBoton() {
        const boton = document.getElementById('theme-toggle');
        if (boton) {
            boton.style.transform = 'scale(0.9) rotate(180deg)';
            setTimeout(() => {
                boton.style.transform = 'scale(1) rotate(0deg)';
            }, 150);
        }
    }

    /**
     * Emitir evento personalizado cuando cambia el tema
     */
    emitirEventoCambioTema(tema) {
        const evento = new CustomEvent('tema-cambiado', {
            detail: { tema: tema }
        });
        window.dispatchEvent(evento);
    }

    /**
     * Actualizar gráficos de Chart.js para el nuevo tema
     */
    actualizarGraficos(tema) {
        if (typeof Chart !== 'undefined') {
            Chart.helpers.each(Chart.instances, (instance) => {
                if (instance.chart) {
                    const chart = instance.chart;
                    const opciones = chart.options;
                    
                    // Actualizar colores de texto y grid
                    if (tema === 'dark') {
                        // Aplicar colores oscuros
                        if (opciones.scales) {
                            Object.keys(opciones.scales).forEach(escala => {
                                if (opciones.scales[escala].ticks) {
                                    opciones.scales[escala].ticks.color = '#e3e6f0';
                                }
                                if (opciones.scales[escala].grid) {
                                    opciones.scales[escala].grid.color = '#404040';
                                }
                            });
                        }
                        
                        if (opciones.plugins && opciones.plugins.legend) {
                            opciones.plugins.legend.labels = {
                                ...opciones.plugins.legend.labels,
                                color: '#e3e6f0'
                            };
                        }
                    } else {
                        // Aplicar colores claros
                        if (opciones.scales) {
                            Object.keys(opciones.scales).forEach(escala => {
                                if (opciones.scales[escala].ticks) {
                                    opciones.scales[escala].ticks.color = '#858796';
                                }
                                if (opciones.scales[escala].grid) {
                                    opciones.scales[escala].grid.color = '#e3e6f0';
                                }
                            });
                        }
                        
                        if (opciones.plugins && opciones.plugins.legend) {
                            opciones.plugins.legend.labels = {
                                ...opciones.plugins.legend.labels,
                                color: '#858796'
                            };
                        }
                    }
                    
                    chart.update();
                }
            });
        }
    }

    /**
     * Actualizar calendario FullCalendar para el nuevo tema
     */
    actualizarCalendario(tema) {
        // Si hay una instancia de FullCalendar, actualizarla
        if (window.calendar && typeof window.calendar.render === 'function') {
            setTimeout(() => {
                window.calendar.render();
            }, 100);
        }
    }

    /**
     * Métodos públicos para uso externo
     */
    esModoOscuro() {
        return this.obtenerTemaActual() === 'dark';
    }

    establecerTema(tema) {
        this.aplicarTema(tema);
        this.guardarTema(tema);
    }

    obtenerColoresTema() {
        const tema = this.obtenerTemaActual();
        
        if (tema === 'dark') {
            return {
                primary: '#e3e6f0',
                secondary: '#b7b9cc',
                background: '#1a1d23',
                surface: '#2c2f36',
                border: '#404040'
            };
        } else {
            return {
                primary: '#5a5c69',
                secondary: '#6c757d',
                background: '#ffffff',
                surface: '#f8f9fc',
                border: '#e3e6f0'
            };
        }
    }
}

// Inicializar modo oscuro cuando se carga el DOM
document.addEventListener('DOMContentLoaded', function() {
    // Solo inicializar en páginas de ProAdmin
    if (window.location.pathname.startsWith('/proadmin')) {
        window.modoOscuroProAdmin = new ModoOscuroProAdmin();
        
        // Hacer disponible globalmente para otros scripts
        window.alternarModoOscuro = () => {
            window.modoOscuroProAdmin.alternarTema();
        };
        
        // Event listener para reaccionar a cambios de tema
        window.addEventListener('tema-cambiado', function(event) {
            console.log('Tema cambiado a:', event.detail.tema);
            
            // Aquí se pueden agregar acciones adicionales cuando cambia el tema
            // Por ejemplo, recargar ciertos componentes o actualizar colores específicos
        });
    }
});

// Función para obtener colores del tema actual (útil para gráficos)
window.obtenerColoresTema = function() {
    if (window.modoOscuroProAdmin) {
        return window.modoOscuroProAdmin.obtenerColoresTema();
    }
    
    // Colores por defecto si no está inicializado
    return {
        primary: '#5a5c69',
        secondary: '#6c757d',
        background: '#ffffff',
        surface: '#f8f9fc',
        border: '#e3e6f0'
    };
};

// Función para verificar si está en modo oscuro
window.esModoOscuro = function() {
    return window.modoOscuroProAdmin ? window.modoOscuroProAdmin.esModoOscuro() : false;
};