package com.magicalAliance.controller.usuario;

import com.magicalAliance.entity.producto.Categoria;
import com.magicalAliance.entity.producto.Producto;
import com.magicalAliance.exception.MagicalBusinessException;
import com.magicalAliance.exception.MagicalNotFoundException;
import com.magicalAliance.service.producto.ICategoriaService;
import com.magicalAliance.service.producto.IProductoService;
import com.magicalAliance.service.usuario.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class HomeController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ICategoriaService catService;

    @Autowired
    private IProductoService prodService;

    @GetMapping({"/home", "/"})
    public String home(Authentication authentication, Model model) {

        try {
            // 1. CARGA DE DATOS PARA EL INDEX (Protegido)

            // Categorías para el menú y banners
            List<Categoria> listaCat = catService.listarTodas();
            model.addAttribute("listaCategorias", listaCat);

            // Carga de productos con stock (esAdmin = false)
            List<Producto> listaProd = prodService.listar(null, null, null, "id_desc", false);
            model.addAttribute("listaProductos", listaProd);

        } catch (MagicalBusinessException | MagicalNotFoundException e) {
            // Si hay un error de negocio o no se encuentra algo, avisamos en el model
            model.addAttribute("error", "Aviso del reino: " + e.getMessage());
        } catch (Exception e) {
            // Error técnico genérico
            model.addAttribute("error", "Hubo un problema al invocar el catálogo. Intenta refrescar la página.");
        }

        // 2. LÓGICA DE IDENTIDAD (Independiente de la carga de productos)

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            model.addAttribute("isLogueado", false);
            model.addAttribute("nombreUsuario", "Invitado");

        } else {
            String email = authentication.getName();

            usuarioService.buscarPorEmail(email).ifPresentOrElse(u -> {
                model.addAttribute("nombreUsuario", u.getCliente().getNombre());
                model.addAttribute("usuarioId", u.getId());
                model.addAttribute("rol", u.getRol().getNombre());
                model.addAttribute("isLogueado", true);
            }, () -> {
                model.addAttribute("nombreUsuario", "Viajero/a");
                model.addAttribute("isLogueado", false);
            });
        }

        return "index";
    }
}