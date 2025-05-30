// Funcionalidades específicas para la gestión de servicios
document.addEventListener('DOMContentLoaded', function() {
    
    // ===== GESTIÓN DE SERVICIOS =====
    
    /**
     * Configurar eventos para la gestión de servicios
     */
    function configurarEventosServicios() {
        // Botones de edición de servicios
        document.querySelectorAll('.btn-editar-servicio').forEach(boton => {
            boton.addEventListener('click', function() {
                abrirModalEditarServicio(this);
            });
        });
        
        // Botones de eliminación de servicios
        document.querySelectorAll('.btn-eliminar-servicio').forEach(boton => {
            boton.addEventListener('click', function() {
                abrirModalEliminarServicio(this);
            });
        });
        
        // Configurar filtros de servicios
        configurarFiltrosServicios();
        
        // Configurar validación del formulario
        configurarValidacionFormularioServicio();
    }
    
    /**
     * Abrir modal para editar servicio
     */
    function abrirModalEditarServicio(boton) {
        const id = boton.getAttribute('data-id');
        const nombre = boton.getAttribute('data-nombre');
        const descripcion = boton.getAttribute('data-descripcion');
        const duracion = boton.getAttribute('data-duracion');
        const precio = boton.getAttribute('data-precio');
        const categoria = boton.getAttribute('data-categoria');
        const disponible = boton.getAttribute('data-disponible') === 'true';
        
        // Cambiar título del modal
        const tituloModal = document.getElementById('tituloModalServicio');
        if (tituloModal) {
            tituloModal.textContent = 'Editar Servicio';
        }
        
        // Llenar formulario con datos actuales
        const form = document.getElementById('formServicio');
        if (form) {
            document.getElementById('servicioId').value = id;
            document.getElementById('nombre').value = nombre || '';
            document.getElementById('descripcion').value = descripcion || '';
            document.getElementById('duracionMinutos').value = duracion || '';
            document.getElementById('precio').value = precio || '';
            document.getElementById('categoria').value = categoria || '';
            document.getElementById('estaDisponible').checked = disponible;
            
            // Cambiar acción del formulario para edición
            form.action = `/proadmin/servicios/${id}/editar`;
        }
        
        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('modalNuevoServicio'));
        modal.show();
    }
    
    /**
     * Abrir modal para eliminar servicio
     */
    function abrirModalEliminarServicio(boton) {
        const id = boton.getAttribute('data-id');
        const nombre = boton.getAttribute('data-nombre');
        
        // Actualizar nombre del servicio en el modal
        const nombreElement = document.getElementById('nombreServicioEliminar');
        if (nombreElement) {
            nombreElement.textContent = nombre;
        }
        
        // Actualizar acción del formulario de eliminación
        const form = document.getElementById('formEliminarServicio');
        if (form) {
            form.action = `/proadmin/servicios/${id}/eliminar`;
        }
        
        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('modalEliminarServicio'));
        modal.show();
    }
    
    /**
     * Configurar filtros de servicios
     */
    function configurarFiltrosServicios() {
        const buscarInput = document.getElementById('buscarServicio');
        const filtroCategoria = document.getElementById('filtroCategoria');
        const tabla = document.getElementById('tablaServicios');
        
        if (buscarInput && filtroCategoria && tabla) {
            function aplicarFiltros() {
                const textoBusqueda = buscarInput.value.toLowerCase().trim();
                const categoriaSeleccionada = filtroCategoria.value;
                const filas = tabla.querySelectorAll('tbody tr');
                
                let serviciosVisibles = 0;
                
                filas.forEach(fila => {
                    const nombre = fila.cells[0].textContent.toLowerCase();
                    const descripcion = fila.cells[1].textContent.toLowerCase();
                    const categoria = fila.cells[4].textContent;
                    
                    // Verificar coincidencia de texto
                    const coincideTexto = !textoBusqueda || 
                                        nombre.includes(textoBusqueda) || 
                                        descripcion.includes(textoBusqueda);
                    
                    // Verificar coincidencia de categoría
                    const coincideCategoria = !categoriaSeleccionada || categoria === categoriaSeleccionada;
                    
                    // Mostrar u ocultar fila
                    const mostrar = coincideTexto && coincideCategoria;
                    fila.style.display = mostrar ? '' : 'none';
                    
                    if (mostrar) serviciosVisibles++;
                });
                
                // Mostrar mensaje si no hay resultados
                mostrarMensajeSinResultados(serviciosVisibles === 0);
            }
            
            // Aplicar filtros en tiempo real
            buscarInput.addEventListener('input', aplicarFiltros);
            filtroCategoria.addEventListener('change', aplicarFiltros);
        }
    }
    
    /**
     * Mostrar mensaje cuando no hay resultados de búsqueda
     */
    function mostrarMensajeSinResultados(mostrar) {
        let mensajeExistente = document.getElementById('mensajeSinResultados');
        
        if (mostrar && !mensajeExistente) {
            const tabla = document.getElementById('tablaServicios');
            if (tabla) {
                const mensaje = document.createElement('div');
                mensaje.id = 'mensajeSinResultados';
                mensaje.className = 'text-center py-4 text-muted';
                mensaje.innerHTML = `
                    <i class="fas fa-search fa-2x mb-2"></i>
                    <p>No se encontraron servicios que coincidan con los filtros aplicados.</p>
                `;
                tabla.parentNode.insertBefore(mensaje, tabla.nextSibling);
            }
        } else if (!mostrar && mensajeExistente) {
            mensajeExistente.remove();
        }
    }
    
    /**
     * Configurar validación del formulario de servicios
     */
    function configurarValidacionFormularioServicio() {
        const form = document.getElementById('formServicio');
        if (!form) return;
        
        form.addEventListener('submit', function(e) {
            const errores = validarFormularioServicio();
            
            if (errores.length > 0) {
                e.preventDefault();
                mostrarErroresValidacion(errores);
                return false;
            }
        });
        
        // Validación en tiempo real
        const campos = ['nombre', 'duracionMinutos', 'precio'];
        campos.forEach(campo => {
            const elemento = document.getElementById(campo);
            if (elemento) {
                elemento.addEventListener('blur', function() {
                    validarCampoIndividual(campo);
                });
            }
        });
    }
    
    /**
     * Validar formulario de servicio
     */
    function validarFormularioServicio() {
        const errores = [];
        
        // Validar nombre
        const nombre = document.getElementById('nombre').value.trim();
        if (!nombre) {
            errores.push('El nombre del servicio es requerido.');
        } else if (nombre.length > 100) {
            errores.push('El nombre del servicio no puede exceder 100 caracteres.');
        }
        
        // Validar categoría
        const categoria = document.getElementById('categoria').value;
        if (!categoria) {
            errores.push('La categoría del servicio es requerida.');
        }
        
        // Validar duración
        const duracion = parseInt(document.getElementById('duracionMinutos').value);
        if (!duracion || isNaN(duracion)) {
            errores.push('La duración del servicio es requerida.');
        } else if (duracion < 5) {
            errores.push('La duración mínima del servicio es de 5 minutos.');
        } else if (duracion > 480) {
            errores.push('La duración máxima del servicio es de 480 minutos (8 horas).');
        }
        
        // Validar precio
        const precio = parseFloat(document.getElementById('precio').value);
        if (!precio || isNaN(precio)) {
            errores.push('El precio del servicio es requerido.');
        } else if (precio <= 0) {
            errores.push('El precio del servicio debe ser mayor a 0.');
        } else if (precio > 9999.99) {
            errores.push('El precio máximo del servicio es S/ 9999.99.');
        }
        
        // Validar descripción
        const descripcion = document.getElementById('descripcion').value;
        if (descripcion && descripcion.length > 500) {
            errores.push('La descripción no puede exceder 500 caracteres.');
        }
        
        return errores;
    }
    
    /**
     * Validar campo individual
     */
    function validarCampoIndividual(campo) {
        const elemento = document.getElementById(campo);
        if (!elemento) return;
        
        // Remover clases de validación anteriores
        elemento.classList.remove('is-valid', 'is-invalid');
        
        let esValido = true;
        const valor = elemento.value.trim();
        
        switch (campo) {
            case 'nombre':
                esValido = valor.length > 0 && valor.length <= 100;
                break;
            case 'duracionMinutos':
                const duracion = parseInt(valor);
                esValido = !isNaN(duracion) && duracion >= 5 && duracion <= 480;
                break;
            case 'precio':
                const precio = parseFloat(valor);
                esValido = !isNaN(precio) && precio > 0 && precio <= 9999.99;
                break;
        }
        
        // Aplicar clase de validación
        elemento.classList.add(esValido ? 'is-valid' : 'is-invalid');
    }
    
    /**
     * Mostrar errores de validación
     */
    function mostrarErroresValidacion(errores) {
        // Crear o actualizar alert de errores
        let alertExistente = document.querySelector('.alert-validation-errors');
        if (alertExistente) {
            alertExistente.remove();
        }
        
        const alert = document.createElement('div');
        alert.className = 'alert alert-danger alert-dismissible fade show alert-validation-errors';
        alert.innerHTML = `
            <strong>Por favor, corrige los siguientes errores:</strong>
            <ul class="mb-0 mt-2">
                ${errores.map(error => `<li>${error}</li>`).join('')}
            </ul>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        // Insertar alert al inicio del modal body
        const modalBody = document.querySelector('#modalNuevoServicio .modal-body');
        if (modalBody) {
            modalBody.insertBefore(alert, modalBody.firstChild);
        }
    }
    
    /**
     * Limpiar modal cuando se abre para nuevo servicio
     */
    function configurarLimpiezaModal() {
        const modal = document.getElementById('modalNuevoServicio');
        if (modal) {
            modal.addEventListener('hidden.bs.modal', function() {
                // Resetear título del modal
                const titulo = document.getElementById('tituloModalServicio');
                if (titulo) {
                    titulo.textContent = 'Nuevo Servicio';
                }
                
                // Resetear formulario
                const form = document.getElementById('formServicio');
                if (form) {
                    form.action = '/proadmin/servicios/nuevo';
                    form.reset();
                    
                    // Establecer valores por defecto
                    document.getElementById('servicioId').value = '';
                    document.getElementById('estaDisponible').checked = true;
                    document.getElementById('duracionMinutos').value = '30';
                    
                    // Limpiar clases de validación
                    form.querySelectorAll('.is-valid, .is-invalid').forEach(elemento => {
                        elemento.classList.remove('is-valid', 'is-invalid');
                    });
                    
                    // Remover alertas de error
                    form.querySelectorAll('.alert-validation-errors').forEach(alert => {
                        alert.remove();
                    });
                }
            });
        }
    }
    
    /**
     * Función para cambiar establecimiento
     */
    window.cambiarEstablecimiento = function() {
        const selector = document.getElementById('selectorEstablecimiento');
        const establecimientoId = selector.value;
        
        if (establecimientoId) {
            // Mostrar loading
            mostrarLoading('Cargando servicios...');
            
            // Redirigir con el nuevo establecimiento
            window.location.href = `/proadmin/servicios?establecimientoId=${establecimientoId}`;
        }
    };
    
    /**
     * Mostrar loading overlay
     */
    function mostrarLoading(mensaje = 'Cargando...') {
        // Remover loading anterior si existe
        const loadingAnterior = document.getElementById('loadingOverlay');
        if (loadingAnterior) {
            loadingAnterior.remove();
        }
        
        // Crear nuevo loading
        const loading = document.createElement('div');
        loading.id = 'loadingOverlay';
        loading.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center';
        loading.style.backgroundColor = 'rgba(0,0,0,0.5)';
        loading.style.zIndex = '9999';
        loading.innerHTML = `
            <div class="text-center text-white">
                <div class="spinner-border mb-3" role="status">
                    <span class="visually-hidden">Cargando...</span>
                </div>
                <p>${mensaje}</p>
            </div>
        `;
        
        document.body.appendChild(loading);
        
        // Auto-remover después de 10 segundos
        setTimeout(() => {
            if (document.getElementById('loadingOverlay')) {
                loading.remove();
            }
        }, 10000);
    }
    
    // Inicializar todas las funcionalidades
    configurarEventosServicios();
    configurarLimpiezaModal();
    
    // API para uso externo
    window.serviciosManager = {
        aplicarFiltros: configurarFiltrosServicios,
        validarFormulario: validarFormularioServicio,
        mostrarLoading: mostrarLoading
    };
});