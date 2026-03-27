package com.magicalAliance.controller.usuario;

import com.magicalAliance.dto.usuario.RegistroDTO;
import com.magicalAliance.entity.usuario.Usuario;
import com.magicalAliance.exception.MagicalBusinessException;
import com.magicalAliance.mapper.UsuarioMapper;
import com.magicalAliance.service.usuario.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    // Sustituye al LoginServlet (doGet)
    @GetMapping("/login")
    public String login(Authentication auth,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {

        // Si ya está logueado, lo mandamos al home (evita que un logueado vea el login)
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }

        // Mensajes que venían del Servlet antiguo
        if (error != null) model.addAttribute("error", "Credenciales incorrectas o cuenta inexistente.");
        if (logout != null) model.addAttribute("msg", "Has cerrado tu sesión en la Alianza correctamente.");

        return "usuarios/login";
    }

    // Sustituye al UsuarioServlet (action=new)
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroDTO", new RegistroDTO());
        return "usuarios/registro";
    }

    // Sustituye al UsuarioServlet (doPost para nuevos usuarios)
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroDTO") RegistroDTO dto,
                            BindingResult result,
                            RedirectAttributes flash,
                            Model model) {

        if (result.hasErrors()) {
            return "usuarios/registro";
        }

        try {
            // Transformamos y registramos
            Usuario usuario = usuarioMapper.toUsuario(dto);
            usuarioService.registrarUsuario(usuario);

            // Mensaje de éxito para el Login
            flash.addFlashAttribute("exito", "¡Registro exitoso! Ya puedes iniciar sesión.");
            return "redirect:/login";

        } catch (MagicalBusinessException e) {
            // Capturamos el error (RUT duplicado, menor de edad, etc.) y volvemos al form
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registroDTO", dto); // Para no perder lo escrito
            return "usuarios/registro";
        } catch (Exception e) {
            model.addAttribute("error", "Error inesperado en el reino: " + e.getMessage());
            return "usuarios/registro";
        }
    }
}