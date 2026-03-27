package com.magicalAliance.service.img;

import com.magicalAliance.exception.MagicalBusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.stream.Stream;

@Service
public class UploadFileServiceImpl implements IUploadFileService {

    @Value("${app.upload.path}")
    private String uploadPath;

    // GUARDAR O EDITAR (Editar es simplemente subir una nueva y reemplazar la ruta en BD)
    @Override
    public String copiar(MultipartFile archivo, String subCarpeta) {
        try {
            // Generamos un nombre único para evitar que fotos con el mismo nombre se borren
            String nombreUnico = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path rutaCarpeta = Paths.get(uploadPath).resolve(subCarpeta);

            if (!Files.exists(rutaCarpeta)) {
                Files.createDirectories(rutaCarpeta);
            }

            Files.copy(archivo.getInputStream(), rutaCarpeta.resolve(nombreUnico));

            // Retornamos la ruta relativa para guardar en la BD
            return subCarpeta + "/" + nombreUnico;

        } catch (IOException e) {
            // AJUSTE: Si falla la escritura en disco, lanzamos nuestra excepción mágica
            throw new MagicalBusinessException("Error de alquimia: No se pudo guardar la imagen en el servidor.");
        }
    }

    // ELIMINAR FÍSICAMENTE EL ARCHIVO
    @Override
    public boolean eliminar(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty() || nombreImagen.contains("default.jpg")) {
            return false; // No borramos la imagen por defecto
        }

        Path rutaAbsoluta = Paths.get(uploadPath).resolve(nombreImagen);
        File archivo = rutaAbsoluta.toFile();

        if (archivo.exists() && archivo.canRead()) {
            return archivo.delete();
        }
        return false;
    }

    // BUSCAR TODAS LAS IMÁGENES (Galería)
    @Override
    public List<String> listarGaleria() {
        List<String> fotos = new ArrayList<>();
        Path root = Paths.get(uploadPath);

        if (!Files.exists(root)) {
            return fotos;
        }

        // Caminamos por las carpetas 'categorias' y 'productos'
        try (Stream<Path> walk = Files.walk(root, 2)) {
            walk.filter(p -> p.toString().toLowerCase().matches(".*\\.(jpg|png|jpeg|webp)$"))
                    .forEach(p -> {
                        // Normalizamos la ruta para el navegador (usando /)
                        String rel = root.relativize(p).toString().replace("\\", "/");
                        fotos.add(rel);
                    });
        } catch (IOException e) {
            // AJUSTE: Si falla la lectura de la galería
            throw new MagicalBusinessException("No se pudo invocar la galería de imágenes en este momento.");
        }

        return fotos;
    }
}