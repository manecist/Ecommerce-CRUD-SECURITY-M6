package com.magicalAliance.service.producto;

import com.magicalAliance.entity.producto.Categoria;
import com.magicalAliance.exception.MagicalBusinessException;
import com.magicalAliance.exception.MagicalNotFoundException;
import com.magicalAliance.repository.producto.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class CategoriaServiceImpl implements ICategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepo;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepo.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepo.findById(id);
    }

    @Override
    @Transactional
    public void guardar(Categoria cat) {
        // 1. VALIDACIÓN: Nombre obligatorio
        if (cat.getNombre() == null || cat.getNombre().isBlank()) {
            throw new MagicalBusinessException("El nombre de la categoría es un elemento obligatorio del reino.");
        }

        String nombreTrim = cat.getNombre().trim();
        cat.setNombre(nombreTrim);

        // 2. LÓGICA PARA CREAR (Si el ID es nulo)
        if (cat.getId() == null) {
            if (categoriaRepo.existsByNombre(nombreTrim)) {
                throw new MagicalBusinessException("Ya existe una categoría invocada con el nombre: " + nombreTrim);
            }
        }
        // 3. LÓGICA PARA ACTUALIZAR (Si el ID ya existe)
        else {
            Categoria existente = categoriaRepo.findById(cat.getId())
                    .orElseThrow(() -> new MagicalNotFoundException("Error: La categoría con ID " + cat.getId() + " no ha sido hallada."));

            // Si el nombre cambió, verificamos que el nuevo no esté duplicado
            if (!existente.getNombre().equalsIgnoreCase(nombreTrim)) {
                if (categoriaRepo.existsByNombre(nombreTrim)) {
                    throw new MagicalBusinessException("Ese nombre ya está en uso por otra categoría de la alianza.");
                }
            }
        }

        // 4. GUARDADO FINAL: JPA decide si hace INSERT o UPDATE
        categoriaRepo.save(cat);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Verificamos si existe antes de intentar borrar
        if (!categoriaRepo.existsById(id)) {
            throw new MagicalNotFoundException("No se puede eliminar: La categoría no existe en los registros actuales.");
        }

        try {
            categoriaRepo.deleteById(id);
        } catch (Exception e) {
            // Captura el error de integridad referencial
            // AJUSTE: Usamos BusinessException porque es un impedimento lógico por asociaciones
            throw new MagicalBusinessException("No se puede desvanecer la categoría porque tiene subcategorías o productos asociados.");
        }
    }
}