/* =========================================
   1. NAVEGACIÓN Y CARRITO (ESTADO GLOBAL)
   ========================================= */
document.addEventListener("DOMContentLoaded", () => {
    // Báculo para subir (Scroll top)
    const btnSubir = document.getElementById("btnSubir");
    if (btnSubir) {
        window.addEventListener('scroll', () => {
            btnSubir.style.display = (window.scrollY > 300) ? "block" : "none";
        });
    }

    // Persistencia del carrito
    const numerito = document.getElementById('numerito');
    if (numerito) {
        numerito.innerText = localStorage.getItem('carritoCantidad') || "0";
    }

    // Auto-cierre de alertas mágicas tras 5 segundos
    document.querySelectorAll('.alert').forEach(alerta => {
        setTimeout(() => {
            const bootstrapAlert = bootstrap.Alert.getOrCreateInstance(alerta);
            if (bootstrapAlert) bootstrapAlert.close();
        }, 5000);
    });

    // Reset de carrito si detectamos logout en la URL
    if (new URLSearchParams(window.location.search).get('logout') === 'true') {
        localStorage.removeItem('carritoCantidad');
        if (numerito) numerito.innerText = "0";
    }
});

function agregarAlCarrito(nombre) {
    const numerito = document.getElementById('numerito');
    if (numerito) {
        let total = parseInt(numerito.innerText) + 1;
        numerito.innerText = total;
        localStorage.setItem('carritoCantidad', total);
    }

    // Notificación visual (Toast)
    const toastEl = document.getElementById('toastCarrito');
    if (toastEl) {
        const toast = new bootstrap.Toast(toastEl);
        const mensaje = document.getElementById('mensajeToast');
        if(mensaje) mensaje.innerHTML = `<b>✨ ¡Hechizo realizado!</b><br>${nombre} añadido al caldero.`;
        toast.show();
    }
}

/* =========================================
   2. GESTIÓN DE IMÁGENES (VISTA PREVIA MÁGICA)
   ========================================= */

// Manejador centralizado para cambios en inputs de imagen
document.addEventListener("change", (e) => {
    const preview = document.getElementById("preview");
    if (!preview) return;

    // Caso A: Elige una imagen que ya existe en el servidor (Galería)
    if (e.target.name === "imagenExistente" && e.target.value !== "") {
        // En Spring Boot las imágenes estáticas están en @{/assets/img/
        preview.src = "/assets/img/" + e.target.value;

        // Limpiamos el input de archivo para que no haya conflictos
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput) fileInput.value = "";
    }

    // Caso B: Sube una imagen nueva desde su PC
    if (e.target.type === "file" && e.target.files[0]) {
        const reader = new FileReader();
        reader.onload = (ev) => {
            preview.src = ev.target.result; // DataURL de la imagen local

            // Limpiamos el select de galería para priorizar la subida
            const selectGaleria = document.querySelector('select[name="imagenExistente"]');
            if (selectGaleria) selectGaleria.value = "";
        };
        reader.readAsDataURL(e.target.files[0]);
    }


        function cambiarImagen() {
            const select = document.getElementsByName('imagenExistente')[0];
            const imgPreview = document.getElementById('preview');
            // Usamos una ruta base de thymeleaf procesada
            const pathBase = /*[[@{/assets/img/}]]*/ '/assets/img/';
            if(select.value) {
                imgPreview.src = pathBase + select.value;
            }
        }

        function prepararBorradoCat(id, nombre) {
            document.getElementById('nombreCatBorrar').innerText = nombre;
            const baseUrl = /*[[@{/gestion-categorias}]]*/ '/gestion-categorias';
            document.getElementById('btnConfirmarBorradoCat').href = baseUrl + "?action=delete&idCategoria=" + id;
        }

});

/* =========================================
   3. VALIDACIONES DE FORMULARIOS (USUARIOS Y PROD)
   ========================================= */

// --- VALIDACIÓN DE EDAD (Mínimo 18 años) ---
document.addEventListener("change", (e) => {
    if (e.target.name === "fechaNacimiento") {
        const spanEdad = document.getElementById("verEdad");
        if (!spanEdad) return;

        const fecha = new Date(e.target.value);
        const hoy = new Date();
        let edad = hoy.getFullYear() - fecha.getFullYear();

        if (hoy.getMonth() < fecha.getMonth() || (hoy.getMonth() === fecha.getMonth() && hoy.getDate() < fecha.getDate())) {
            edad--;
        }

        if (e.target.value === "" || edad < 18 || edad > 105) {
            e.target.setCustomValidity("Invalido");
            spanEdad.innerText = "(⚠️ Fecha Inválida)";
            spanEdad.style.color = "#ff6b6b";
        } else {
            e.target.setCustomValidity("");
            spanEdad.innerText = "(✨ " + edad + " años)";
            spanEdad.style.color = "#00d4aa";
        }
    }
});

// --- CONFIRMACIÓN DE PRODUCTOS ---
function validarYConfirmar() {
    const form = document.getElementById('formProducto');
    if (!form) return;

    if (form.checkValidity()) {
        const modal = new bootstrap.Modal(document.getElementById('modalConfirmarProducto'));
        modal.show();
    } else {
        form.classList.add('was-validated');
        // Scroll suave al primer error
        const firstError = form.querySelector(':invalid');
        if (firstError) firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

// --- CONFIRMACIÓN DE USUARIOS ---
function validarYEnviar() {
    const form = document.getElementById('formUsuario');
    const passInput = document.getElementById('passInput');
    const idInput = document.querySelector('input[name="idUsuario"]');

    if (!form || !passInput) return;

    // Regex de Seguridad: 8+ carac, 1 Mayus, 1 Minus, 1 Num, 1 Símbolo
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.])[A-Za-z\d@$!%*?&.]{8,}$/;

    // Lógica para permitir pass vacío solo en edición
    const esNuevo = (!idInput || idInput.value === "0" || idInput.value === "");
    if (esNuevo || passInput.value.length > 0) {
        if (!regex.test(passInput.value)) {
            passInput.setCustomValidity("Invalido");
        } else {
            passInput.setCustomValidity("");
        }
    } else {
        passInput.setCustomValidity("");
    }

    if (form.checkValidity()) {
        const modal = new bootstrap.Modal(document.getElementById('confirmModal'));
        modal.show();
    } else {
        form.classList.add('was-validated');
    }
}


    function abrirModalConfirmacion() {
        // Aquí podrías agregar validaciones personalizadas de JS antes de abrir
        const modal = new bootstrap.Modal(document.getElementById('confirmModal'));
        modal.show();
    }

    function togglePassword() {
        const passInput = document.getElementById('passInput');
        const toggleIcon = document.getElementById('toggleIcon');
        if (passInput.type === "password") {
            passInput.type = "text";
            toggleIcon.classList.replace("bi-eye-slash", "bi-eye");
        } else {
            passInput.type = "password";
            toggleIcon.classList.replace("bi-eye", "bi-eye-slash");
        }
    }

    function prepararBorradoUsuario(id, nombre) {
        document.getElementById('nombreUsuarioBorrar').innerText = nombre;
        // Genera la URL de borrado dinámicamente
        const url = /*[[@{/usuarios}]]*/ '/usuarios';
        document.getElementById('confirmarBorradoUserBtn').href = url + "?action=delete&id=" + id;
    }

/* =========================================
   4. GESTIÓN DE BORRADOS (MODALES DINÁMICOS)
   ========================================= */
function confirmarEliminar(id, nombre, tipo) {
    // Configuración de rutas según el tipo
    const urls = {
        'usuario': { idLabel: 'nombreUsuarioBorrar', btn: 'confirmarBorradoUserBtn', url: '/usuarios/delete/' },
        'producto': { idLabel: 'nombreProdEliminar', btn: 'linkConfirmarEliminar', url: '/productos/delete/' },
        'subcategoria': { idLabel: 'nombreSubBorrar', btn: 'btnConfirmarBorrado', url: '/subcategorias/delete/' },
        'categoria': { idLabel: 'nombreCatBorrar', btn: 'btnConfirmarBorradoCat', url: '/categorias/delete/' }
    };

    // 1. Inyectamos el nombre en el modal para que el usuario sepa qué borra
    const nombreEl = document.getElementById('nombreProdEliminar');
    if (nombreEl) nombreEl.innerText = nombre;

    // 2. Configuramos el botón de "Sí, Eliminar" del modal
    const btnConfirmar = document.getElementById('linkConfirmarEliminar');
    if (btnConfirmar) {
        // Genera algo como: /productos/eliminar/5
        btnConfirmar.href = urls[tipo] + id;
    }

    // 3. Mostramos el modal usando Bootstrap 5
    const modalEl = document.getElementById('modalEliminar');
    if (modalEl) {
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    }


        function prepararBorradoSub(id, nombre) {
            document.getElementById('nombreSubBorrar').innerText = nombre;
            const baseUrl = /*[[@{/gestion-subcategorias}]]*/ '/gestion-subcategorias';
            document.getElementById('btnConfirmarBorrado').href = baseUrl + "?action=delete&idSubcategoria=" + id;
        }


// --- PARA EL LOGIN ---
function togglePassword(id) {
    const input = document.getElementById(id);
    const icon = document.querySelector(`[onclick="togglePassword('${id}')"] i`);
    if (input) {
        input.type = (input.type === 'password') ? 'text' : 'password';
        if (icon) {
            icon.classList.toggle('bi-eye');
            icon.classList.toggle('bi-eye-slash');
        }
    }
}


    document.addEventListener('DOMContentLoaded', function() {
        const errorModal = new bootstrap.Modal(document.getElementById('errorLoginModal'));
        errorModal.show();
    });


// --- FUNCIONES DE PRODUCTO-FORM ---

function actualizarPreview(select) {
    if(select.value) {
        // En Spring Boot, la ruta a tus imágenes será esta por defecto
        document.getElementById('preview').src = '/assets/img/' + select.value;
    }
}

function previewFile(input) {
    const file = input.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('preview').src = e.target.result;
        }
        reader.readAsDataURL(file);
    }
}

function validarYConfirmar() {
    const form = document.getElementById('formProducto');
    if (!form) return; // Seguridad por si no estamos en la página del form

    if (form.checkValidity()) {
        const modalEl = document.getElementById('modalConfirmarProducto');
        if (modalEl) {
            const modal = new bootstrap.Modal(modalEl);
            modal.show();
        }
    } else {
        // Esto hace que aparezcan los globitos de error de HTML5
        form.reportValidity();
        form.classList.add('was-validated');
    }
}

function enviarFormulario() {
    const form = document.getElementById('formProducto');
    if (form) form.submit();
}