/**
 * Funcionalidad para el formulario de login
 */
document.addEventListener('DOMContentLoaded', function() {
    // Elementos del DOM
    const loginForm = document.getElementById('loginForm');
    const togglePassword = document.querySelector('.toggle-password');
    const passwordInput = document.getElementById('password');

    // Mostrar/ocultar contraseña
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // Cambiar ícono
            const icon = this.querySelector('i');
            if (type === 'password') {
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            } else {
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            }
        });
    }

    // Validación del formulario
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            const username = document.getElementById('username').value.trim();
            const password = passwordInput.value.trim();
            const submitButton = this.querySelector('button[type="submit"]');
            let isValid = true;

            // Validar campos vacíos
            if (!username) {
                showError('Por favor, ingresa tu correo electrónico');
                isValid = false;
            } else if (!isValidEmail(username)) {
                showError('Por favor, ingresa un correo electrónico válido');
                isValid = false;
            } else if (!password) {
                showError('Por favor, ingresa tu contraseña');
                isValid = false;
            }

            // Si el formulario es válido, mostrar estado de carga
            if (isValid && submitButton) {
                // Deshabilitar el botón y mostrar spinner
                submitButton.disabled = true;
                submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Iniciando sesión...';
                submitButton.classList.add('btn-loading');
            } else if (!isValid) {
                e.preventDefault();
                return false;
            }

            return true;
        });
    }

    // Mostrar mensaje de error
    function showError(message) {
        // Eliminar alertas existentes
        const existingAlert = document.querySelector('.alert-dismissible');
        if (existingAlert) {
            existingAlert.remove();
        }

        // Crear nueva alerta
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger alert-dismissible fade show';
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            <i class="fas fa-exclamation-triangle me-2"></i>${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        // Insertar la alerta al principio del formulario
        const form = document.querySelector('form');
        if (form) {
            form.insertBefore(alertDiv, form.firstChild);

            // Hacer scroll hasta la alerta
            alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }

    // Validar formato de correo electrónico
    function isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    }

    // Mostrar mensajes del servidor (si los hay)
    const urlParams = new URLSearchParams(window.location.search);
    const errorParam = urlParams.get('error');
    const logoutParam = urlParams.get('logout');

    if (errorParam) {
        showError('Usuario o contraseña incorrectos. Por favor, inténtalo de nuevo.');
    }

    if (logoutParam) {
        showSuccess('Has cerrado sesión correctamente.');
    }

    // Función para mostrar mensaje de éxito
    function showSuccess(message) {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success alert-dismissible fade show';
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            <i class="fas fa-check-circle me-2"></i>${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        const form = document.querySelector('form');
        if (form) {
            form.insertBefore(alertDiv, form.firstChild);
        } else {
            const container = document.querySelector('.login-body');
            if (container) {
                container.insertBefore(alertDiv, container.firstChild);
            }
        }
    }

    // Inicializar tooltips de Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
});