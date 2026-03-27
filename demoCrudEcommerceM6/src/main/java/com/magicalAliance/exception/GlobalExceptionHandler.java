package com.magicalAliance.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Si algo NO SE ENCUENTRA (404)
    @ExceptionHandler(MagicalNotFoundException.class)
    public String handleNotFound(MagicalNotFoundException ex, Model model) {
        model.addAttribute("errorTitulo", "Objeto Desvanecido");
        model.addAttribute("errorMensaje", ex.getMessage());
        return "error/404";
    }

    // Si el usuario rompe una REGLA (Precio negativo, etc)
    @ExceptionHandler(MagicalBusinessException.class)
    public String handleBusiness(MagicalBusinessException ex, RedirectAttributes flash, HttpServletRequest request) {
        // Agregamos el mensaje mágico para mostrarlo en el formulario
        flash.addFlashAttribute("error", "🔮 " + ex.getMessage());

        // Lo devolvemos automáticamente a la página donde estaba intentando guardar
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/productos");
    }

    // Por si ocurre cualquier otro error inesperado (500)
    @ExceptionHandler(Exception.class)
    public String handleGlobal(Exception ex, Model model) {
        model.addAttribute("errorTitulo", "Perturbación en la Magia");
        model.addAttribute("errorMensaje", "Algo salió mal en el servidor: " + ex.getMessage());
        return "error/500";
    }
}