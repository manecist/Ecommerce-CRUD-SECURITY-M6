package com.magicalAliance.service.img;

import com.magicalAliance.exception.MagicalBusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // 1. MANTENER LO DE STATIC (Accesorios, Cosméticos, Vestuario, CSS, JS)
            // Spring ya lo hace por defecto, pero esto asegura que no se pierda
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/static/assets/");

            // 2. AGREGAR LO DEL DISCO C (Categorías y Productos del Admin)
            // Usamos "file:" para decirle que salga del proyecto y vaya al disco duro
            // Convertimos la ruta del disco en una URI válida (file:/C:/...)
            String rootPath = Paths.get(uploadPath).toAbsolutePath().toUri().toString();

            // Mapeamos la URL /categorias/** a la carpeta física de categorías
            registry.addResourceHandler("/categorias/**")
                    .addResourceLocations(rootPath + "categorias/");

            // Mapeamos la URL /productos/** a la carpeta física de productos
            registry.addResourceHandler("/productos/**")
                    .addResourceLocations(rootPath + "productos/");

        } catch (Exception e) {
            // AJUSTE: Si hay un error configurando las rutas del servidor
            throw new MagicalBusinessException("Error crítico: No se pudo establecer el puente con el almacenamiento de imágenes.");
        }
    }
}