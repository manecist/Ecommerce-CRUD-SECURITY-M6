package com.magicalAliance.controller.usuario;

import com.magicalAliance.dto.usuario.RegistroDTO;
import com.magicalAliance.entity.usuario.Cliente;
import com.magicalAliance.entity.usuario.DireccionCliente;
import com.magicalAliance.entity.usuario.Usuario;
import com.magicalAliance.exception.MagicalBusinessException;
import com.magicalAliance.exception.MagicalNotFoundException;
import com.magicalAliance.mapper.UsuarioMapper;
import com.magicalAliance.repository.usuario.RolRepository;
import com.magicalAliance.service.usuario.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private RolRepository rolRepo;

    @Autowired
    private UsuarioMapper usuarioMapper;

    // --- 1. GESTIÓN ADMIN: Listado, Búsqueda y Ordenamiento ---
    @GetMapping("/admin/gestion")
    @PreAuthorize("hasRole('ADMIN')")
    public String buscarUsuarios(
            @RequestParam(required = false) String criterio,
            @RequestParam(required = false, defaultValue = "id_asc") String orden,
            Model model) {

        model.addAttribute("roles", rolRepo.findAll());
        model.addAttribute("ordenActual", orden);

        if (criterio != null && !criterio.isBlank()) {
            List<Usuario> resultados = usuarioService.buscarPorCriterio(criterio);
            model.addAttribute("usuarios", resultados);
            model.addAttribute("criterio", criterio);
        } else {
            model.addAttribute("usuarios", usuarioService.listarTodos(orden));
        }

        return "admin/gestion-usuario";
    }

    // --- 2. REGISTRO POR ADMIN ---
    @GetMapping("/admin/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String formularioNuevoUsuario(Model model) {
        model.addAttribute("nuevoUsuario", new RegistroDTO());
        model.addAttribute("roles", rolRepo.findAll());
        return "admin/form-usuario";
    }

    @PostMapping("/admin/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarUsuarioAdmin(@Valid @ModelAttribute("nuevoUsuario") RegistroDTO registroDTO,
                                      BindingResult result,
                                      @RequestParam Long idRol,
                                      RedirectAttributes flash,
                                      Model model) {

        if (result.hasErrors()) {
            model.addAttribute("roles", rolRepo.findAll());
            return "admin/form-usuario";
        }

        try {
            // Validación de rango de edad (Uso de Exception propia)
            if (registroDTO.getFechaNacimiento() != null) {
                long edad = ChronoUnit.YEARS.between(registroDTO.getFechaNacimiento(), LocalDate.now());
                if (edad < 18 || edad > 105) {
                    throw new MagicalBusinessException("La edad del habitante debe estar entre 18 y 105 años.");
                }
            }

            Usuario usuarioParaGuardar = usuarioMapper.toUsuario(registroDTO);
            usuarioService.crearUsuarioDesdeAdmin(usuarioParaGuardar, idRol);
            flash.addFlashAttribute("success", "Usuario creado exitosamente en el registro real.");
            return "redirect:/usuarios/admin/gestion";

        } catch (MagicalBusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", rolRepo.findAll());
            model.addAttribute("nuevoUsuario", registroDTO); // Persistencia de datos
            return "admin/form-usuario";
        }
    }

    // --- 3. CAMBIO DE ROL RÁPIDO ---
    @PostMapping("/admin/cambiar-rol")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarRolRapido(@RequestParam Long usuarioId, @RequestParam Long nuevoRolId, RedirectAttributes flash) {
        try {
            if (usuarioId == 1) throw new MagicalBusinessException("Protección: No puedes alterar el rango del Administrador Supremo.");
            usuarioService.cambiarRol(usuarioId, nuevoRolId);
            flash.addFlashAttribute("success", "Rango actualizado correctamente.");
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios/admin/gestion";
    }

    // --- 4. PERFIL: Ver y Navegar ---
    @GetMapping("/perfil/{id}")
    public String verPerfil(@PathVariable Long id, Authentication auth, Model model) {
        Usuario logueado = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new MagicalBusinessException("Sesión expirada."));

        if (!isAdmin(auth) && !logueado.getId().equals(id)) {
            return "redirect:/home?error=no-autorizado";
        }

        Usuario target = usuarioService.buscarPorId(id)
                .flatMap(u -> usuarioService.obtenerPerfilCompleto(u.getEmail()))
                .orElseThrow(() -> new MagicalNotFoundException("El habitante solicitado no existe."));

        model.addAttribute("u", target);
        return "perfil/detalle";
    }

    @GetMapping("/perfil")
    public String irMiPerfil(Authentication auth) {
        return usuarioService.buscarPorEmail(auth.getName())
                .map(u -> "redirect:/usuarios/perfil/" + u.getId())
                .orElse("redirect:/login");
    }

    // --- 5. EDICIÓN: Acceso y Datos Personales ---
    @PostMapping("/editar-acceso/{id}")
    public String editarAcceso(@PathVariable Long id, @RequestParam String email,
                               @RequestParam String password, Authentication auth, RedirectAttributes flash) {
        if (!isAdmin(auth) && !isDuenio(auth, id)) return "redirect:/home";

        try {
            String emailActual = auth.getName();
            usuarioService.actualizarCredenciales(id, email, password);

            if (!emailActual.equalsIgnoreCase(email) && !isAdmin(auth)) {
                return "redirect:/logout";
            }
            return "redirect:/usuarios/perfil/" + id + "?exito=credenciales";
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/perfil/" + id;
        }
    }

    @PostMapping("/editar-datos/{id}")
    public String editarDatos(@PathVariable Long id, @ModelAttribute Cliente cliente,
                              Authentication auth, RedirectAttributes flash) {
        if (!isAdmin(auth) && !isDuenio(auth, id)) return "redirect:/home";

        try {
            usuarioService.actualizarDatosPersonales(id, cliente);
            flash.addFlashAttribute("success", "Datos personales actualizados.");
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios/perfil/" + id;
    }

    // --- 6. GESTIÓN DE DIRECCIONES ---
    @PostMapping("/direcciones/agregar/{usuarioId}")
    public String agregarDir(@PathVariable Long usuarioId, @ModelAttribute DireccionCliente dir, Authentication auth, RedirectAttributes flash) {
        if (!isAdmin(auth) && !isDuenio(auth, usuarioId)) return "redirect:/home";
        try {
            usuarioService.agregarDireccion(usuarioId, dir);
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios/perfil/" + usuarioId;
    }

    @PostMapping("/direcciones/editar/{usuarioId}/{dirId}")
    public String editarDir(@PathVariable Long usuarioId, @PathVariable Long dirId,
                            @ModelAttribute DireccionCliente nuevos, Authentication auth, RedirectAttributes flash) {
        if (!isAdmin(auth) && !isDuenio(auth, usuarioId)) return "redirect:/home";
        try {
            usuarioService.editarDireccion(dirId, nuevos);
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios/perfil/" + usuarioId;
    }

    @PostMapping("/direcciones/eliminar/{usuarioId}/{dirId}")
    public String eliminarDir(@PathVariable Long usuarioId, @PathVariable Long dirId, Authentication auth, RedirectAttributes flash) {
        if (!isAdmin(auth) && !isDuenio(auth, usuarioId)) return "redirect:/home";
        try {
            usuarioService.eliminarDireccion(usuarioId, dirId);
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios/perfil/" + usuarioId;
    }

    // --- 7. ELIMINAR USUARIO ---
    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        try {
            usuarioService.eliminarUsuario(id);
            flash.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (MagicalBusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios/admin/gestion";
    }

    // MÉTODOS DE APOYO
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isDuenio(Authentication auth, Long id) {
        return usuarioService.buscarPorEmail(auth.getName())
                .map(u -> u.getId().equals(id))
                .orElse(false);
    }
}