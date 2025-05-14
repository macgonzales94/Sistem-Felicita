document.addEventListener('DOMContentLoaded', function() {
    // Validación del formulario de registro
    const registroForm = document.querySelector('form[action="/registro"]');
    
    if (registroForm) {
        registroForm.addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmarPassword = document.getElementById('confirmarPassword').value;
            
            if (password !== confirmarPassword) {
                e.preventDefault();
                
                // Crear alerta
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger';
                alertDiv.textContent = 'Las contraseñas no coinciden';
                
                // Insertar alerta al inicio del formulario
                this.insertBefore(alertDiv, this.firstChild);
                
                // Scroll al inicio del formulario
                window.scrollTo(0, 0);
            }
        });
    }
    
    // Desvanecimiento de alertas después de 5 segundos
    const alertas = document.querySelectorAll('.alert');
    if (alertas.length > 0) {
        setTimeout(function() {
            alertas.forEach(function(alerta) {
                alerta.style.opacity = '0';
                alerta.style.transition = 'opacity 1s';
                
                setTimeout(function() {
                    alerta.style.display = 'none';
                }, 1000);
            });
        }, 5000);
    }
    
    // Alternar visibilidad de contraseña
    const passwordFields = document.querySelectorAll('input[type="password"]');
    passwordFields.forEach(function(field) {
        const toggleButton = document.createElement('button');
        toggleButton.type = 'button';
        toggleButton.className = 'btn btn-outline-secondary toggle-password';
        toggleButton.innerHTML = '<i class="fas fa-eye"></i>';
        toggleButton.style.position = 'absolute';
        toggleButton.style.right = '10px';
        toggleButton.style.top = '50%';
        toggleButton.style.transform = 'translateY(-50%)';
        toggleButton.style.zIndex = '10';
        
        // Agregar el botón solo si el campo tiene un padre
        if (field.parentNode) {
            field.parentNode.style.position = 'relative';
            field.parentNode.appendChild(toggleButton);
            
            toggleButton.addEventListener('click', function() {
                const type = field.getAttribute('type') === 'password' ? 'text' : 'password';
                field.setAttribute('type', type);
                this.innerHTML = type === 'password' ? '<i class="fas fa-eye"></i>' : '<i class="fas fa-eye-slash"></i>';
            });
        }
    });
});

// Función para manejar los eventos de login JWT
function iniciarSesionJWT(event) {
    event.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
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
        localStorage.setItem('userRole', data.roles[0]);
        
        // Redirigir según el rol
        switch(data.roles[0]) {
            case 'ADMINISTRADOR':
                window.location.href = '/admin/dashboard';
                break;
            case 'PROADMIN':
                window.location.href = '/proadmin/dashboard';
                break;
            case 'CLIENTE':
                window.location.href = '/cliente/inicio';
                break;
            default:
                window.location.href = '/';
        }
    })
    .catch(error => {
        // Mostrar error
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger';
        alertDiv.textContent = error.message;
        
        const form = document.querySelector('form');
        form.insertBefore(alertDiv, form.firstChild);
    });
}

// Si estamos en modo API, usamos el evento para JWT
const apiLoginForm = document.getElementById('api-login-form');
if (apiLoginForm) {
    apiLoginForm.addEventListener('submit', iniciarSesionJWT);
}