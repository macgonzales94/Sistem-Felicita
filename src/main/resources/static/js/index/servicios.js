/**
 * JavaScript para la página de servicios de FELICITA
 */

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar la página de servicios
    initServicios();
});

class ServiciosManager {
    constructor() {
        this.servicios = [];
        this.filtros = {
            categoria: 'todos',
            subcategoria: '',
            ubicacion: '',
            precioMin: null,
            precioMax: null,
            calificacion: null,
            busqueda: ''
        };
        this.paginaActual = 1;
        this.serviciosPorPagina = 12;
        this.isAuthenticated = false;
        
        this.init();
    }

    init() {
        this.checkAuthentication();
        this.bindEvents();
        this.cargarServicios();
        this.configurarFechaMinima();
    }

    checkAuthentication() {
        // Verificar si el usuario está autenticado
        const token = localStorage.getItem('jwtToken');
        this.isAuthenticated = !!token;
    }

    bindEvents() {
        // Evento de búsqueda
        document.getElementById('btnBuscar').addEventListener('click', () => this.buscarServicios());
        document.getElementById('busquedaServicio').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.buscarServicios();
        });

        // Eventos de filtros
        document.getElementById('aplicarFiltros').addEventListener('click', () => this.aplicarFiltros());
        document.getElementById('ordenarPor').addEventListener('change', () => this.ordenarServicios());

        // Eventos de categorías
        document.querySelectorAll('.lista-subcategorias a').forEach(link => {
            link.addEventListener('click', (e) => this.filtrarPorCategoria(e));
        });

        // Eventos de tabs
        document.querySelectorAll('#serviciosTabs button').forEach(tab => {
            tab.addEventListener('click', (e) => this.cambiarTab(e));
        });

        // Eventos de modales
        this.bindModalEvents();
    }

    bindModalEvents() {
        // Eventos del formulario de login
        document.getElementById('loginForm').addEventListener('submit', (e) => this.handleLogin(e));
        
        // Eventos del formulario de registro
        document.getElementById('registroForm').addEventListener('submit', (e) => this.handleRegistro(e));
        
        // Eventos del formulario de reserva
        document.getElementById('reservaForm').addEventListener('submit', (e) => this.handleReserva(e));
        
        // Eventos de selección en el formulario de reserva
        document.getElementById('establecimientoSelect').addEventListener('change', () => this.cargarServicios());
        document.getElementById('servicioSelect').addEventListener('change', () => this.actualizarResumen());
        document.getElementById('fechaCita').addEventListener('change', () => this.cargarHorariosDisponibles());
        document.getElementById('horaCita').addEventListener('change', () => this.actualizarResumen());
    }

    configurarFechaMinima() {
        const fechaInput = document.getElementById('fechaCita');
        if (fechaInput) {
            const hoy = new Date();
            const fechaMinima = new Date(hoy.getTime() + 24 * 60 * 60 * 1000); // Mínimo mañana
            fechaInput.min = fechaMinima.toISOString().split('T')[0];
        }
    }

    async cargarServicios() {
        try {
            this.mostrarEstadoCarga();
            
            // Simular datos de servicios (en producción vendrían del backend)
            const serviciosData = await this.fetchServicios();
            this.servicios = serviciosData;
            
            this.renderizarServicios();
            this.renderizarPaginacion();
            
        } catch (error) {
            console.error('Error al cargar servicios:', error);
            this.mostrarEstadoError();
        }
    }

    async fetchServicios() {
        // Simular llamada a API (reemplazar con llamada real al backend)
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve(this.generarServiciosEjemplo());
            }, 500);
        });
    }

    generarServiciosEjemplo() {
        return [
            {
                id: 1,
                nombre: "Corte de Cabello Moderno",
                descripcion: "Corte profesional con las últimas tendencias",
                precio: 25.00,
                precioOriginal: 35.00,
                categoria: "barberia",
                subcategoria: "corte-cabello",
                establecimiento: "Barbería El Corte",
                ubicacion: "Lima, San Isidro",
                calificacion: 4.8,
                numeroResenas: 156,
                imagen: "/img/corte-moderno.jpg",
                descuento: 30,
                disponible: true,
                duracion: 45
            },
            {
                id: 2,
                nombre: "Tratamiento Facial Hidratante",
                descripcion: "Tratamiento facial completo con productos premium",
                precio: 80.00,
                categoria: "belleza",
                subcategoria: "tratamientos-faciales",
                establecimiento: "Spa Belleza Total",
                ubicacion: "Lima, Miraflores",
                calificacion: 4.9,
                numeroResenas: 203,
                imagen: "/img/facial-hidratante.jpg",
                disponible: true,
                duracion: 90
            },
            {
                id: 3,
                nombre: "Manicure Gel con Diseño",
                descripcion: "Manicure profesional con esmalte gel y diseños personalizados",
                precio: 35.00,
                categoria: "belleza",
                subcategoria: "manicure",
                establecimiento: "Nails & Beauty",
                ubicacion: "Lima, San Borja",
                calificacion: 4.7,
                numeroResenas: 89,
                imagen: "/img/manicure-gel.jpg",
                disponible: true,
                duracion: 60
            },
            {
                id: 4,
                nombre: "Gift Card Barbería - S/ 100",
                descripcion: "Tarjeta de regalo válida en cualquier barbería afiliada",
                precio: 100.00,
                categoria: "giftcard",
                subcategoria: "giftcard-barberia",
                establecimiento: "Red de Barberías FELICITA",
                ubicacion: "Válido en toda Lima",
                calificacion: 5.0,
                numeroResenas: 45,
                imagen: "/img/giftcard-barberia.jpg",
                disponible: true,
                validez: "12 meses"
            },
            {
                id: 5,
                nombre: "Coloración Completa",
                descripcion: "Cambio de color completo con productos de alta calidad",
                precio: 120.00,
                precioOriginal: 150.00,
                categoria: "belleza",
                subcategoria: "coloracion",
                establecimiento: "Color Studio",
                ubicacion: "Lima, La Molina",
                calificacion: 4.6,
                numeroResenas: 78,
                imagen: "/img/coloracion.jpg",
                descuento: 20,
                disponible: true,
                duracion: 180
            },
            {
                id: 6,
                nombre: "Arreglo de Barba Profesional",
                descripcion: "Arreglo y diseño de barba con técnicas profesionales",
                precio: 20.00,
                categoria: "barberia",
                subcategoria: "arreglo-barba",
                establecimiento: "Barbería Premium",
                ubicacion: "Lima, Surco",
                calificacion: 4.8,
                numeroResenas: 134,
                imagen: "/img/arreglo-barba.jpg",
                disponible: true,
                duracion: 30
            }
        ];
    }

    filtrarServicios() {
        let serviciosFiltrados = [...this.servicios];

        // Filtrar por categoría
        if (this.filtros.categoria !== 'todos') {
            serviciosFiltrados = serviciosFiltrados.filter(s => s.categoria === this.filtros.categoria);
        }

        // Filtrar por subcategoría
        if (this.filtros.subcategoria) {
            serviciosFiltrados = serviciosFiltrados.filter(s => s.subcategoria === this.filtros.subcategoria);
        }

        // Filtrar por búsqueda
        if (this.filtros.busqueda) {
            const busqueda = this.filtros.busqueda.toLowerCase();
            serviciosFiltrados = serviciosFiltrados.filter(s => 
                s.nombre.toLowerCase().includes(busqueda) ||
                s.descripcion.toLowerCase().includes(busqueda) ||
                s.establecimiento.toLowerCase().includes(busqueda)
            );
        }

        // Filtrar por precio
        if (this.filtros.precioMin !== null) {
            serviciosFiltrados = serviciosFiltrados.filter(s => s.precio >= this.filtros.precioMin);
        }
        if (this.filtros.precioMax !== null) {
            serviciosFiltrados = serviciosFiltrados.filter(s => s.precio <= this.filtros.precioMax);
        }

        // Filtrar por calificación
        if (this.filtros.calificacion !== null) {
            serviciosFiltrados = serviciosFiltrados.filter(s => s.calificacion >= this.filtros.calificacion);
        }

        return serviciosFiltrados;
    }

    renderizarServicios() {
        const container = document.getElementById('serviciosContainer');
        const serviciosFiltrados = this.filtrarServicios();
        
        // Calcular servicios para la página actual
        const inicio = (this.paginaActual - 1) * this.serviciosPorPagina;
        const fin = inicio + this.serviciosPorPagina;
        const serviciosPagina = serviciosFiltrados.slice(inicio, fin);

        if (serviciosPagina.length === 0) {
            container.innerHTML = this.renderizarSinResultados();
            return;
        }

        container.innerHTML = serviciosPagina.map(servicio => this.crearCardServicio(servicio)).join('');
        
        // Bind eventos de reserva
        this.bindReservaEvents();
    }

    crearCardServicio(servicio) {
        const descuentoHtml = servicio.descuento ? 
            `<div class="servicio-descuento">-${servicio.descuento}%</div>` : '';
        
        const precioOriginalHtml = servicio.precioOriginal ? 
            `<span class="servicio-precio-original">S/ ${servicio.precioOriginal.toFixed(2)}</span>` : '';

        return `
            <div class="col-lg-4 col-md-6 mb-4">
                <div class="card servicio-card h-100">
                    ${descuentoHtml}
                    <img src="${servicio.imagen}" class="card-img-top" alt="${servicio.nombre}" 
                         onerror="this.src='/img/servicio-default.jpg'">
                    <div class="card-body d-flex flex-column">
                        <div class="servicio-calificacion">
                            <span class="estrellas">${this.generarEstrellas(servicio.calificacion)}</span>
                            <span class="numero">${servicio.calificacion} (${servicio.numeroResenas})</span>
                        </div>
                        <h5 class="card-title">${servicio.nombre}</h5>
                        <p class="card-text">${servicio.descripcion}</p>
                        <div class="servicio-ubicacion">
                            <i class="fas fa-map-marker-alt"></i>
                            <span>${servicio.ubicacion}</span>
                        </div>
                        <div class="servicio-ubicacion">
                            <i class="fas fa-store"></i>
                            <span>${servicio.establecimiento}</span>
                        </div>
                        ${servicio.duracion ? `
                        <div class="servicio-ubicacion">
                            <i class="fas fa-clock"></i>
                            <span>${servicio.duracion} minutos</span>
                        </div>` : ''}
                        <div class="mt-auto">
                            <div class="d-flex align-items-center justify-content-between mb-3">
                                <div>
                                    <div class="servicio-precio">S/ ${servicio.precio.toFixed(2)}</div>
                                    ${precioOriginalHtml}
                                </div>
                                <span class="badge badge-categoria">${this.obtenerNombreCategoria(servicio.categoria)}</span>
                            </div>
                            <button class="btn btn-reservar" 
                                    onclick="serviciosManager.iniciarReserva(${servicio.id})"
                                    ${!servicio.disponible ? 'disabled' : ''}>
                                <i class="fas fa-calendar-plus me-2"></i>
                                ${servicio.disponible ? 'Reservar' : 'No Disponible'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    generarEstrellas(calificacion) {
        const estrellasCompletas = Math.floor(calificacion);
        const mediaEstrella = calificacion % 1 >= 0.5;
        let estrellas = '';
        
        for (let i = 0; i < estrellasCompletas; i++) {
            estrellas += '★';
        }
        if (mediaEstrella) {
            estrellas += '☆';
        }
        for (let i = estrellasCompletas + (mediaEstrella ? 1 : 0); i < 5; i++) {
            estrellas += '☆';
        }
        
        return estrellas;
    }

    obtenerNombreCategoria(categoria) {
        const nombres = {
            'barberia': 'Barbería',
            'belleza': 'Belleza',
            'giftcard': 'Gift Card'
        };
        return nombres[categoria] || categoria;
    }

    renderizarSinResultados() {
        return `
            <div class="col-12">
                <div class="no-results">
                    <i class="fas fa-search"></i>
                    <h4>No se encontraron servicios</h4>
                    <p>Intenta ajustar tus filtros de búsqueda o explora otras categorías.</p>
                    <button class="btn btn-primary" onclick="serviciosManager.limpiarFiltros()">
                        Limpiar Filtros
                    </button>
                </div>
            </div>
        `;
    }

    mostrarEstadoCarga() {
        const container = document.getElementById('serviciosContainer');
        container.innerHTML = `
            <div class="col-12">
                <div class="loading-state">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Cargando...</span>
                    </div>
                    <p class="mt-3">Cargando servicios...</p>
                </div>
            </div>
        `;
    }

    mostrarEstadoError() {
        const container = document.getElementById('serviciosContainer');
        container.innerHTML = `
            <div class="col-12">
                <div class="error-state">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h4>Error al cargar servicios</h4>
                    <p>Ha ocurrido un error. Por favor, intenta nuevamente.</p>
                    <button class="btn btn-primary" onclick="serviciosManager.cargarServicios()">
                        Reintentar
                    </button>
                </div>
            </div>
        `;
    }

    renderizarPaginacion() {
        const serviciosFiltrados = this.filtrarServicios();
        const totalPaginas = Math.ceil(serviciosFiltrados.length / this.serviciosPorPagina);
        const paginacion = document.getElementById('paginacion');

        if (totalPaginas <= 1) {
            paginacion.innerHTML = '';
            return;
        }

        let html = '';
        
        // Botón anterior
        html += `
            <li class="page-item ${this.paginaActual === 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="serviciosManager.cambiarPagina(${this.paginaActual - 1})">
                    <i class="fas fa-chevron-left"></i>
                </a>
            </li>
        `;

        // Números de página
        for (let i = 1; i <= totalPaginas; i++) {
            if (i === this.paginaActual) {
                html += `<li class="page-item active"><span class="page-link">${i}</span></li>`;
            } else if (i === 1 || i === totalPaginas || Math.abs(i - this.paginaActual) <= 2) {
                html += `<li class="page-item"><a class="page-link" href="#" onclick="serviciosManager.cambiarPagina(${i})">${i}</a></li>`;
            } else if (Math.abs(i - this.paginaActual) === 3) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        // Botón siguiente
        html += `
            <li class="page-item ${this.paginaActual === totalPaginas ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="serviciosManager.cambiarPagina(${this.paginaActual + 1})">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </li>
        `;

        paginacion.innerHTML = html;
    }

    cambiarPagina(nuevaPagina) {
        const serviciosFiltrados = this.filtrarServicios();
        const totalPaginas = Math.ceil(serviciosFiltrados.length / this.serviciosPorPagina);
        
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            this.paginaActual = nuevaPagina;
            this.renderizarServicios();
            this.renderizarPaginacion();
            
            // Scroll al inicio del contenido
            document.getElementById('serviciosContainer').scrollIntoView({ behavior: 'smooth' });
        }
    }

    buscarServicios() {
        const busqueda = document.getElementById('busquedaServicio').value.trim();
        this.filtros.busqueda = busqueda;
        this.paginaActual = 1;
        this.renderizarServicios();
        this.renderizarPaginacion();
    }

    aplicarFiltros() {
        // Obtener valores de los filtros
        this.filtros.ubicacion = document.getElementById('filtroUbicacion').value;
        this.filtros.precioMin = parseFloat(document.getElementById('precioMin').value) || null;
        this.filtros.precioMax = parseFloat(document.getElementById('precioMax').value) || null;
        
        const ratingChecked = document.querySelector('input[name="rating"]:checked');
        this.filtros.calificacion = ratingChecked ? parseFloat(ratingChecked.value) : null;

        this.paginaActual = 1;
        this.renderizarServicios();
        this.renderizarPaginacion();
    }

    filtrarPorCategoria(event) {
        event.preventDefault();
        
        // Remover clase active de todos los enlaces
        document.querySelectorAll('.lista-subcategorias a').forEach(link => {
            link.classList.remove('active');
        });
        
        // Agregar clase active al enlace clickeado
        event.target.classList.add('active');
        
        const categoria = event.target.dataset.categoria;
        const servicio = event.target.dataset.servicio;
        
        this.filtros.categoria = categoria;
        this.filtros.subcategoria = servicio;
        this.paginaActual = 1;
        
        this.renderizarServicios();
        this.renderizarPaginacion();
    }

    cambiarTab(event) {
        const tabId = event.target.getAttribute('data-bs-target').replace('#', '');
        
        // Actualizar filtros según el tab
        switch(tabId) {
            case 'todos':
                this.filtros.categoria = 'todos';
                this.filtros.subcategoria = '';
                break;
            case 'barberia':
                this.filtros.categoria = 'barberia';
                this.filtros.subcategoria = '';
                break;
            case 'belleza':
                this.filtros.categoria = 'belleza';
                this.filtros.subcategoria = '';
                break;
            case 'giftcard':
                this.filtros.categoria = 'giftcard';
                this.filtros.subcategoria = '';
                break;
        }
        
        this.paginaActual = 1;
        this.renderizarServicios();
        this.renderizarPaginacion();
    }

    ordenarServicios() {
        const criterio = document.getElementById('ordenarPor').value;
        
        this.servicios.sort((a, b) => {
            switch(criterio) {
                case 'precio-asc':
                    return a.precio - b.precio;
                case 'precio-desc':
                    return b.precio - a.precio;
                case 'calificacion':
                    return b.calificacion - a.calificacion;
                case 'distancia':
                    // Simular ordenamiento por distancia
                    return Math.random() - 0.5;
                default:
                    return 0;
            }
        });
        
        this.renderizarServicios();
    }

    limpiarFiltros() {
        // Resetear filtros
        this.filtros = {
            categoria: 'todos',
            subcategoria: '',
            ubicacion: '',
            precioMin: null,
            precioMax: null,
            calificacion: null,
            busqueda: ''
        };
        
        // Limpiar formularios
        document.getElementById('busquedaServicio').value = '';
        document.getElementById('filtroUbicacion').value = '';
        document.getElementById('precioMin').value = '';
        document.getElementById('precioMax').value = '';
        document.querySelectorAll('input[name="rating"]').forEach(radio => radio.checked = false);
        document.querySelectorAll('.lista-subcategorias a').forEach(link => {
            link.classList.remove('active');
        });
        
        // Resetear tab activo
        document.getElementById('todos-tab').click();
        
        this.paginaActual = 1;
        this.renderizarServicios();
        this.renderizarPaginacion();
    }

    iniciarReserva(servicioId) {
        if (!this.isAuthenticated) {
            // Mostrar modal de login
            const modalAuth = new bootstrap.Modal(document.getElementById('modalAuth'));
            modalAuth.show();
            return;
        }
        
        // Cargar datos para la reserva
        const servicio = this.servicios.find(s => s.id === servicioId);
        if (servicio) {
            this.cargarDatosReserva(servicio);
            const modalReserva = new bootstrap.Modal(document.getElementById('modalReserva'));
            modalReserva.show();
        }
    }

    async cargarDatosReserva(servicio) {
        // Cargar establecimientos (simular llamada a API)
        const establecimientos = await this.fetchEstablecimientos(servicio.categoria);
        const establecimientoSelect = document.getElementById('establecimientoSelect');
        
        establecimientoSelect.innerHTML = '<option value="">Seleccionar establecimiento</option>';
        establecimientos.forEach(est => {
            const selected = est.nombre === servicio.establecimiento ? 'selected' : '';
            establecimientoSelect.innerHTML += `<option value="${est.id}" ${selected}>${est.nombre} - ${est.ubicacion}</option>`;
        });
        
        // Si hay establecimiento preseleccionado, cargar sus servicios
        if (establecimientoSelect.value) {
            await this.cargarServiciosEstablecimiento();
            document.getElementById('servicioSelect').value = servicio.id;
            this.actualizarResumen();
        }
    }

    async fetchEstablecimientos(categoria) {
        // Simular datos de establecimientos
        return [
            { id: 1, nombre: "Barbería El Corte", ubicacion: "San Isidro", categoria: "barberia" },
            { id: 2, nombre: "Spa Belleza Total", ubicacion: "Miraflores", categoria: "belleza" },
            { id: 3, nombre: "Nails & Beauty", ubicacion: "San Borja", categoria: "belleza" },
            { id: 4, nombre: "Color Studio", ubicacion: "La Molina", categoria: "belleza" },
            { id: 5, nombre: "Barbería Premium", ubicacion: "Surco", categoria: "barberia" }
        ].filter(est => categoria === 'todos' || est.categoria === categoria || categoria === 'giftcard');
    }

    async cargarServiciosEstablecimiento() {
        const establecimientoId = document.getElementById('establecimientoSelect').value;
        if (!establecimientoId) return;
        
        // Filtrar servicios por establecimiento
        const serviciosEstablecimiento = this.servicios.filter(s => s.establecimiento.includes('Barbería') || s.establecimiento.includes('Spa') || s.establecimiento.includes('Studio'));
        
        const servicioSelect = document.getElementById('servicioSelect');
        servicioSelect.innerHTML = '<option value="">Seleccionar servicio</option>';
        
        serviciosEstablecimiento.forEach(servicio => {
            servicioSelect.innerHTML += `<option value="${servicio.id}" data-precio="${servicio.precio}" data-duracion="${servicio.duracion}">${servicio.nombre} - S/ ${servicio.precio.toFixed(2)}</option>`;
        });
    }

    async cargarHorariosDisponibles() {
        const fecha = document.getElementById('fechaCita').value;
        const servicioId = document.getElementById('servicioSelect').value;
        
        if (!fecha || !servicioId) return;
        
        // Simular horarios disponibles
        const horarios = this.generarHorariosDisponibles();
        const horaSelect = document.getElementById('horaCita');
        
        horaSelect.innerHTML = '<option value="">Seleccionar hora</option>';
        horarios.forEach(hora => {
            horaSelect.innerHTML += `<option value="${hora}">${hora}</option>`;
        });
    }

    generarHorariosDisponibles() {
        const horarios = [];
        for (let hora = 9; hora <= 18; hora++) {
            for (let minuto of ['00', '30']) {
                if (hora === 18 && minuto === '30') break; // No generar 18:30
                horarios.push(`${hora.toString().padStart(2, '0')}:${minuto}`);
            }
        }
        
        // Simular algunos horarios ocupados
        const ocupados = ['10:00', '14:30', '16:00'];
        return horarios.filter(hora => !ocupados.includes(hora));
    }

    actualizarResumen() {
        const establecimientoSelect = document.getElementById('establecimientoSelect');
        const servicioSelect = document.getElementById('servicioSelect');
        const fechaCita = document.getElementById('fechaCita').value;
        const horaCita = document.getElementById('horaCita').value;
        
        if (!establecimientoSelect.value || !servicioSelect.value || !fechaCita || !horaCita) {
            document.getElementById('resumenReserva').style.display = 'none';
            return;
        }
        
        const establecimientoTexto = establecimientoSelect.options[establecimientoSelect.selectedIndex].text;
        const servicioOption = servicioSelect.options[servicioSelect.selectedIndex];
        const servicioTexto = servicioOption.text;
        const precio = servicioOption.dataset.precio;
        
        document.getElementById('resumenEstablecimiento').textContent = establecimientoTexto;
        document.getElementById('resumenServicio').textContent = servicioTexto.split(' - ')[0];
        document.getElementById('resumenFechaHora').textContent = `${fechaCita} a las ${horaCita}`;
        document.getElementById('resumenPrecio').textContent = `S/ ${parseFloat(precio).toFixed(2)}`;
        
        document.getElementById('resumenReserva').style.display = 'block';
    }

    async handleLogin(event) {
        event.preventDefault();
        
        const email = document.getElementById('emailLogin').value;
        const password = document.getElementById('passwordLogin').value;
        
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });
            
            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                this.isAuthenticated = true;
                
                // Cerrar modal y mostrar mensaje de éxito
                bootstrap.Modal.getInstance(document.getElementById('modalAuth')).hide();
                this.mostrarNotificacion('Inicio de sesión exitoso', 'success');
                
                // Redirigir según el rol si es necesario
                if (data.roles.includes('CLIENTE')) {
                    // Permanecer en la página de servicios
                } else if (data.roles.includes('PROADMIN')) {
                    window.location.href = '/proadmin/dashboard';
                } else if (data.roles.includes('ADMINISTRADOR')) {
                    window.location.href = '/admin/dashboard';
                }
                
            } else {
                throw new Error('Credenciales incorrectas');
            }
        } catch (error) {
            this.mostrarNotificacion('Error al iniciar sesión: ' + error.message, 'error');
        }
    }

    async handleRegistro(event) {
        event.preventDefault();
        
        const formData = {
            nombre: document.getElementById('nombreRegistro').value,
            apellido: document.getElementById('apellidoRegistro').value,
            email: document.getElementById('emailRegistro').value,
            telefono: document.getElementById('telefonoRegistro').value,
            password: document.getElementById('passwordRegistro').value,
            confirmarPassword: document.getElementById('confirmarPasswordRegistro').value,
            tipoUsuario: document.getElementById('tipoUsuario').value
        };
        
        // Validar que las contraseñas coincidan
        if (formData.password !== formData.confirmarPassword) {
            this.mostrarNotificacion('Las contraseñas no coinciden', 'error');
            return;
        }
        
        try {
            const response = await fetch('/api/auth/registro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            
            if (response.ok) {
                // Cerrar modal y mostrar mensaje de éxito
                bootstrap.Modal.getInstance(document.getElementById('modalAuth')).hide();
                this.mostrarNotificacion('Registro exitoso. Por favor, inicia sesión.', 'success');
                
                // Cambiar al tab de login
                setTimeout(() => {
                    const modalAuth = new bootstrap.Modal(document.getElementById('modalAuth'));
                    modalAuth.show();
                    document.getElementById('login-tab').click();
                }, 2000);
                
            } else {
                const errorData = await response.json();
                throw new Error(errorData.mensaje || 'Error en el registro');
            }
        } catch (error) {
            this.mostrarNotificacion('Error en el registro: ' + error.message, 'error');
        }
    }

    async handleReserva(event) {
        event.preventDefault();
        
        const reservaData = {
            establecimientoId: parseInt(document.getElementById('establecimientoSelect').value),
            servicioId: parseInt(document.getElementById('servicioSelect').value),
            fechaHora: `${document.getElementById('fechaCita').value}T${document.getElementById('horaCita').value}:00`,
            comentarios: document.getElementById('comentariosCita').value
        };
        
        try {
            const response = await fetch('/cliente/citas/api', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
                },
                body: JSON.stringify(reservaData)
            });
            
            if (response.ok) {
                const cita = await response.json();
                
                // Cerrar modal y mostrar mensaje de éxito
                bootstrap.Modal.getInstance(document.getElementById('modalReserva')).hide();
                this.mostrarNotificacion(`Cita reservada exitosamente. Código: ${cita.codigoUnico}`, 'success');
                
                // Limpiar formulario
                document.getElementById('reservaForm').reset();
                document.getElementById('resumenReserva').style.display = 'none';
                
            } else {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Error al reservar la cita');
            }
        } catch (error) {
            this.mostrarNotificacion('Error al reservar: ' + error.message, 'error');
        }
    }

    bindReservaEvents() {
        // Rebind events para los botones de reserva generados dinámicamente
        document.querySelectorAll('.btn-reservar').forEach(btn => {
            if (!btn.hasAttribute('data-bound')) {
                btn.setAttribute('data-bound', 'true');
                // El evento onclick ya está definido en el HTML generado
            }
        });
    }

    mostrarNotificacion(mensaje, tipo) {
        // Crear notificación
        const alertClass = tipo === 'success' ? 'alert-success' : 'alert-danger';
        const iconClass = tipo === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
        
        const notificacion = document.createElement('div');
        notificacion.className = `alert ${alertClass} alert-dismissible fade show position-fixed`;
        notificacion.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        notificacion.innerHTML = `
            <i class="fas ${iconClass} me-2"></i>
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(notificacion);
        
        // Auto-remover después de 5 segundos
        setTimeout(() => {
            if (notificacion.parentNode) {
                notificacion.remove();
            }
        }, 5000);
    }
}

// Inicializar cuando se carga el DOM
function initServicios() {
    window.serviciosManager = new ServiciosManager();
}