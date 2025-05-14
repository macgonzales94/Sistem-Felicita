document.addEventListener('DOMContentLoaded', function() {
    // Inicializar tooltips de Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Manejar el formulario de búsqueda
    const formBusqueda = document.querySelector('#buscar form');
    if (formBusqueda) {
        formBusqueda.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Obtener valores del formulario
            const servicio = this.querySelector('input[placeholder*="servicio"]').value;
            const ubicacion = this.querySelector('input[placeholder*="ubicación"]').value;
            
            // Validar que al menos un campo esté completo
            if (!servicio && !ubicacion) {
                // Mostrar alerta
                mostrarAlerta('Por favor ingresa un servicio o ubicación para buscar', 'warning');
                return;
            }
            
            // Redirigir a la página de resultados (a implementar)
            window.location.href = `/buscar?servicio=${encodeURIComponent(servicio)}&ubicacion=${encodeURIComponent(ubicacion)}`;
        });
    }
    
    // Función para mostrar alertas
    function mostrarAlerta(mensaje, tipo) {
        // Crear elemento de alerta
        const alertaDiv = document.createElement('div');
        alertaDiv.className = `alert alert-${tipo} alert-dismissible fade show`;
        alertaDiv.innerHTML = `
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        `;
        
        // Insertar alerta en el contenedor
        const contenedor = document.querySelector('#buscar .container');
        contenedor.insertBefore(alertaDiv, contenedor.firstChild);
        
        // Eliminar alerta después de 5 segundos
        setTimeout(() => {
            alertaDiv.classList.remove('show');
            setTimeout(() => alertaDiv.remove(), 300);
        }, 5000);
    }
    
    // Manejar inicio de sesión con JWT si estamos en modo API
    const btnLoginApi = document.getElementById('login-api');
    if (btnLoginApi) {
        btnLoginApi.addEventListener('click', function() {
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            // Validar campos
            if (!email || !password) {
                mostrarAlerta('Por favor completa todos los campos', 'danger');
                return;
            }
            
            // Datos para enviar
            const loginData = {
                email: email,
                password: password
            };
            
            // Llamada a la API
            fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(loginData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Credenciales incorrectas');
                }
                return response.json();
            })
            .then(data => {
                // Guardar token en localStorage
                localStorage.setItem('jwtToken', data.token);
                
                // Redirigir según el rol
                if (data.roles.includes('ADMINISTRADOR')) {
                    window.location.href = '/admin/dashboard';
                } else if (data.roles.includes('PROADMIN')) {
                    window.location.href = '/proadmin/dashboard';
                } else if (data.roles.includes('CLIENTE')) {
                    window.location.href = '/cliente/inicio';
                } else {
                    window.location.href = '/';
                }
            })
            .catch(error => {
                mostrarAlerta(error.message, 'danger');
            });
        });
    }
    
    // Comprobar si hay un token JWT almacenado
    const jwtToken = localStorage.getItem('jwtToken');
    if (jwtToken) {
        // Agregar el token a todas las peticiones fetch
        const originalFetch = window.fetch;
        window.fetch = function(url, options = {}) {
            // Clonar las opciones para no modificar el objeto original
            const newOptions = {...options};
            
            // Asegurarse de que headers existe
            newOptions.headers = newOptions.headers || {};
            
            // Agregar el token a la cabecera Authorization
            newOptions.headers.Authorization = `Bearer ${jwtToken}`;
            
            // Llamar al fetch original con las nuevas opciones
            return originalFetch(url, newOptions);
        };
    }
});