/**
 * Script de validación para el formulario de registro
 * Incluye validaciones en tiempo real y al enviar el formulario
 */
document.addEventListener('DOMContentLoaded', function() {
    // Elementos del formulario
    const form = document.getElementById('registerForm');
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const username = document.getElementById('username');
    const email = document.getElementById('email');
    const telefono = document.getElementById('telefono');
    const togglePasswordButtons = document.querySelectorAll('.toggle-password');
    const terminos = document.getElementById('terminos');

    // Mostrar/ocultar contraseña
    togglePasswordButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);

            // Cambiar ícono
            const icon = this.querySelector('i');
            icon.classList.toggle('fa-eye');
            icon.classList.toggle('fa-eye-slash');
        });
    });

    // Validar que el nombre de usuario solo contenga caracteres permitidos
    function validateUsername() {
        const usernameRegex = /^[a-zA-Z0-9._-]+$/;
        if (!usernameRegex.test(username.value)) {
            username.setCustomValidity('Solo se permiten letras, números, puntos, guiones bajos y guiones');
            username.classList.add('is-invalid');
            return false;
        } else {
            username.setCustomValidity('');
            username.classList.remove('is-invalid');
            return true;
        }
    }

    // Validar formato de correo electrónico
    function validateEmail() {
        const emailRegex = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/i;
        if (!emailRegex.test(email.value)) {
            email.setCustomValidity('Por favor ingresa un correo electrónico válido');
            email.classList.add('is-invalid');
            return false;
        } else {
            email.setCustomValidity('');
            email.classList.remove('is-invalid');
            return true;
        }
    }

    // Validar formato de teléfono (exactamente 9 dígitos)
    function validateTelefono() {
        const telefonoRegex = /^[0-9]{9}$/;
        if (!telefonoRegex.test(telefono.value)) {
            telefono.setCustomValidity('El teléfono debe tener exactamente 9 dígitos');
            telefono.classList.add('is-invalid');
            return false;
        } else {
            telefono.setCustomValidity('');
            telefono.classList.remove('is-invalid');
            return true;
        }
    }

    // Validar que las contraseñas coincidan
    function validatePassword() {
        if (password.value !== confirmPassword.value) {
            confirmPassword.setCustomValidity('Las contraseñas no coinciden');
            confirmPassword.classList.add('is-invalid');
            return false;
        } else {
            confirmPassword.setCustomValidity('');
            confirmPassword.classList.remove('is-invalid');
            return true;
        }
    }

    // Validar fortaleza de la contraseña
    function validatePasswordStrength() {
        // Al menos 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial
        const strongRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{6,}$/;

        if (!strongRegex.test(password.value)) {
            password.setCustomValidity(
                'La contraseña debe tener al menos 6 caracteres, incluyendo ' +
                'mayúsculas, minúsculas, números y al menos un carácter especial (@#$%^&+=)'
            );
            password.classList.add('is-invalid');
            return false;
        } else {
            password.setCustomValidity('');
            password.classList.remove('is-invalid');
            return true;
        }
    }

    // Validar términos y condiciones
    function validateTerminos() {
        if (!terminos.checked) {
            terminos.setCustomValidity('Debes aceptar los términos y condiciones');
            terminos.classList.add('is-invalid');
            return false;
        } else {
            terminos.setCustomValidity('');
            terminos.classList.remove('is-invalid');
            return true;
        }
    }

    // Validar todo el formulario
    function validateForm(event = null) {
        if (event) event.preventDefault();
        
        // Ejecutar todas las validaciones
        const isUsernameValid = validateUsername();
        const isEmailValid = validateEmail();
        const isPasswordValid = validatePasswordStrength();
        const isPasswordMatch = validatePassword();
        const isTelefonoValid = validateTelefono();
        const isTerminosValid = validateTerminos();

        // Si hay errores, detener el envío
        if (!form.checkValidity() || !isUsernameValid || !isEmailValid || 
            !isPasswordValid || !isPasswordMatch || !isTelefonoValid || !isTerminosValid) {
            if (event) {
                event.preventDefault();
                event.stopPropagation();
            }
            return false;
        }

        // Si llegamos aquí, el formulario es válido
        if (event) {
            // Mostrar indicador de carga en el botón de enviar
            const submitButton = form.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Registrando...';
            }
            // Permitir que el formulario se envíe
            return true;
        }
        
        return true;
    }

    // Event listeners para validación en tiempo real
    if (username) username.addEventListener('input', validateUsername);
    if (email) email.addEventListener('input', validateEmail);
    if (password) password.addEventListener('input', validatePasswordStrength);
    if (confirmPassword) confirmPassword.addEventListener('input', validatePassword);
    if (telefono) telefono.addEventListener('input', validateTelefono);
    if (terminos) terminos.addEventListener('change', validateTerminos);

    // Validar al enviar el formulario
    form.addEventListener('submit', validateForm, false);

    // Validar campos al perder el foco
    const campos = [username, email, password, confirmPassword, telefono].filter(Boolean);
    campos.forEach(input => {
        input.addEventListener('blur', function() {
            // Solo validar si el campo no está vacío
            if (this.value.trim() !== '') {
                switch(this.id) {
                    case 'username': validateUsername(); break;
                    case 'email': validateEmail(); break;
                    case 'password': validatePasswordStrength(); break;
                    case 'confirmPassword': validatePassword(); break;
                    case 'telefono': validateTelefono(); break;
                }
            }
        });
    });
});
