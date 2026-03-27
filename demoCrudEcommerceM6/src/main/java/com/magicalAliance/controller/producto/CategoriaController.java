package com.magicalAliance.controller.producto;


import com.magicalAliance.entity.producto.Categoria;
import com.magicalAliance.exception.MagicalBusinessException;
import com.magicalAliance.exception.MagicalNotFoundException;
import com.magicalAliance.service.img.IUploadFileService;
import com.magicalAliance.service.producto.ICategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/gestion-categorias")
@PreAuthorize("hasRole('ADMIN')")
public class CategoriaController {

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IUploadFileService uploadService;

    /**
     * LISTAR Y PREPARAR FORMULARIO
     */
    @GetMapping
    public String listar(Model model,
                         @RequestParam(name = "action", required = false) String action,
                         @RequestParam(name = "id", required = false) Long id) {

        try {
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("galeriaImagenes", uploadService.listarGaleria());

            if ("edit".equals(action) && id != null) {
                // Usamos orElseThrow para disparar tu excepción si el ID es inventado
                Categoria cat = categoriaService.buscarPorId(id)
                        .orElseThrow(() -> new MagicalNotFoundException("La categoría no existe en el registro místico."));
                model.addAttribute("categoriaEnEdicion", cat);
            } else {
                if (!model.containsAttribute("categoriaEnEdicion")) {
                    Categoria nueva = new Categoria();
                    nueva.setImagenBanner("categorias/default-banner.jpg");
                    model.addAttribute("categoriaEnEdicion", nueva);
                }
            }
        } catch (MagicalNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            // Si no existe, preparamos un objeto nuevo para que la vista no explote
            model.addAttribute("categoriaEnEdicion", new Categoria());
        }

        return "productos/categorias-list";
    }

    /**
     * GUARDAR O ACTUALIZAR
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoriaEnEdicion") Categoria categoria,
                          BindingResult result,
                          @RequestParam("imagenBanner") MultipartFile file,
                          @RequestParam(name = "imagenExistente", required = false) String imagenExistente,
                          @RequestParam(name = "imagenActual", required = false) String imagenActual,
                          RedirectAttributes flash,
                          Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("galeriaImagenes", uploadService.listarGaleria());
            return "productos/categorias-list";
        }

        try {
            String imagenFinal;

            // Lógica de Imagen (Subida vs Galería vs Mantener)
            if (!file.isEmpty()) {
                imagenFinal = uploadService.copiar(file, "categorias");
            } else if (imagenExistente != null && !imagenExistente.isEmpty()) {
                imagenFinal = imagenExistente;
            } else {
                imagenFinal = (imagenActual != null && !imagenActual.isEmpty())
                        ? imagenActual : "categorias/default-banner.jpg";
            }

            categoria.setImagenBanner(imagenFinal);
            categoriaService.guardar(categoria);

            flash.addFlashAttribute("success", "¡La esencia de la categoría ha sido guardada!");
            return "redirect:/gestion-categorias";

        } catch (MagicalBusinessException e) {
            // Captura errores de nombre duplicado o reglas de negocio
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/gestion-categorias";
        } catch (IOException e) {
            flash.addFlashAttribute("error", "Error místico al procesar la imagen del banner.");
            return "redirect:/gestion-categorias";
        }
    }

    /**
     * ELIMINAR CATEGORÍA
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaService.eliminar(id);
            flash.addFlashAttribute("success", "Categoría desvanecida correctamente.");
        } catch (MagicalBusinessException e) {
            // Error de integridad (ej: tiene productos vinculados)
            flash.addFlashAttribute("error", "No se puede eliminar: " + e.getMessage());
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Hubo un error crítico al intentar eliminar la categoría.");
        }
        return "redirect:/gestion-categorias";
    }
}