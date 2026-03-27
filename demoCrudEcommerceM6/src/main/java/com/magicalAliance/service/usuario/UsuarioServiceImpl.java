package com.magicalAliance.service.usuario;

import com.magicalAliance.entity.usuario.Cliente;
import com.magicalAliance.entity.usuario.DireccionCliente;
import com.magicalAliance.entity.usuario.Rol;
import com.magicalAliance.entity.usuario.Usuario;
import com.magicalAliance.exception.MagicalBusinessException;
import com.magicalAliance.exception.MagicalNotFoundException;
import com.magicalAliance.repository.usuario.ClienteRepository;
import com.magicalAliance.repository.usuario.DireccionRepository;
import com.magicalAliance.repository.usuario.RolRepository;
import com.magicalAliance.repository.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private DireccionRepository direccionRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- BUSQUEDAS ---

    @Override @Transactional(readOnly = true) public Optional<Usuario> buscarPorId(Long id) { return usuarioRepo.findById(id); }
    @Override @Transactional(readOnly = true) public Optional<Usuario> buscarPorEmail(String email) { return usuarioRepo.findByEmail(email); }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos(String orden) {
        Sort sort;

        switch (orden != null ? orden : "id_asc") {
            case "nombre_az":
                sort = Sort.by("cliente.nombre").ascending();
                break;
            case "nombre_za":
                // AJUSTE: Se agregó .descending() para que el filtro Z-A funcione
                sort = Sort.by("cliente.nombre").descending();
                break;
            case "fecha_asc":
                sort = Sort.by("fechaRegistro").ascending();
                break;
            case "fecha_desc":
                // AJUSTE: Se agregó .descending() para que el filtro de fecha reciente funcione
                sort = Sort.by("fechaRegistro").descending();
                break;
            default:
                sort = Sort.by("id").ascending();
                break;
        }

        return usuarioRepo.findAll(sort);
    }

    // --- HERRAMIENTA DE LIMPIEZA (Privada) ---
    private String limpiarRut(String rut) {
        if (rut == null) return null;
        return rut.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
    }

    // --- BUSQUEDAS ACTUALIZADAS ---
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorRut(String rut) {
        return usuarioRepo.findByClienteRut(limpiarRut(rut));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorCriterio(String termino) {
        String limpio = limpiarRut(termino);
        // método con LIKE del Repository
        return usuarioRepo.buscarPorCriterioFlexible(limpio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPerfilCompleto(String email) {
        return usuarioRepo.findByEmail(email).map(u -> {
            if (u.getCliente() != null) u.getCliente().getDirecciones().size();
            return u;
        });
    }

    // --- REGISTRO ---
    @Override
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        // AJUSTE: Validación de rango de edad
        if (usuario.getCliente().getFechaNacimiento() != null) {
            int edad = usuario.getCliente().getEdad();
            if (edad < 18 || edad > 105) throw new MagicalBusinessException("La edad debe estar entre 18 y 105 años.");
        }

        // 1. Limpiamos el RUT antes de cualquier lógica
        String rutLimpio = limpiarRut(usuario.getCliente().getRut());
        usuario.getCliente().setRut(rutLimpio);

        // 2. ¿Ya existe como cliente? (Invitado previo)
        // Si existe, vinculamos ese cliente al nuevo usuario en lugar de crear uno duplicado
        clienteRepo.findByRut(rutLimpio).ifPresent(existente -> {
            if (usuarioRepo.findByClienteRut(rutLimpio).isPresent()) {
                throw new MagicalBusinessException("Este RUT ya tiene una cuenta de usuario activa.");
            }
            usuario.setCliente(existente); // Vinculamos al cliente que ya existía
        });

        // 3. Seguridad y Roles
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.getCliente().setEmail(usuario.getEmail());

        Rol rol = rolRepo.findByNombre("ROLE_CLIENT")
                .orElseThrow(() -> new MagicalNotFoundException("El rol ROLE_CLIENT no existe en el reino."));
        usuario.setRol(rol);

        return usuarioRepo.save(usuario);
    }

    @Override
    @Transactional
    public Usuario crearUsuarioDesdeAdmin(Usuario u, Long idRol) {
        // AJUSTE: Validación de rango de edad
        if (u.getCliente().getFechaNacimiento() != null) {
            int edad = u.getCliente().getEdad();
            if (edad < 18 || edad > 105) throw new MagicalBusinessException("La edad debe estar entre 18 y 105 años.");
        }

        String rutLimpio = limpiarRut(u.getCliente().getRut());
        u.getCliente().setRut(rutLimpio);

        // --- LÓGICA DE FUSIÓN ---
        Optional<Cliente> clienteExistente = clienteRepo.findByRut(rutLimpio);

        if (clienteExistente.isPresent()) {
            // ¿Ya tiene una cuenta de usuario?
            if (usuarioRepo.findByClienteRut(rutLimpio).isPresent()) {
                throw new MagicalBusinessException("Este RUT ya tiene una cuenta de usuario activa.");
            }
            // Si existe como cliente pero NO tiene usuario: LO FUSIONAMOS
            // Actualizamos los datos del cliente existente con los que mandó el admin ahora
            Cliente c = clienteExistente.get();
            c.setNombre(u.getCliente().getNombre());
            c.setApellido(u.getCliente().getApellido());
            c.setTelefono(u.getCliente().getTelefono());
            c.setFechaNacimiento(u.getCliente().getFechaNacimiento());

            // Reemplazamos el cliente nuevo por el persistente (el de la BD)
            u.setCliente(c);
        }

        // 3. VALIDACIÓN: ¿Ya existe este Email en la tabla Usuarios?
        if (usuarioRepo.findByEmail(u.getEmail()).isPresent()) {
            throw new MagicalBusinessException("Error: El email " + u.getEmail() + " ya está registrado.");
        }

        // 4. Encriptamos la clave enviada por el Admin
        u.setPassword(passwordEncoder.encode(u.getPassword()));

        // 5. Asignamos el Rol elegido en el formulario
        Rol rol = rolRepo.findById(idRol).orElseThrow(() ->
                new MagicalNotFoundException("Error: El Rol con ID " + idRol + " no existe."));
        u.setRol(rol);

        // 6. Sincronizamos el email del cliente con el del usuario (Información Espejo)
        u.getCliente().setEmail(u.getEmail());

        // 7. Guardamos todo (Gracias al Cascade, se guarda Usuario y Cliente a la vez)
        return usuarioRepo.save(u);
    }

    // --- CAMBIAR ROL CON SEGURIDAD ---
    @Transactional
    public void cambiarRol(Long idUsuario, Long idNuevoRol) {
        // CANDADO 1: No se puede cambiar el rol al ID 1 (Tú)
        if (idUsuario == 1) {
            throw new MagicalBusinessException("No está permitido modificar el rol del Administrador Principal.");
        }

        Usuario u = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new MagicalNotFoundException("Usuario no encontrado"));

        Rol nuevoRol = rolRepo.findById(idNuevoRol)
                .orElseThrow(() -> new MagicalNotFoundException("Rol no encontrado"));

        u.setRol(nuevoRol);
        usuarioRepo.save(u);
    }

    // --- EDITAR CREDENCIALES (Email/Pass) ---
    @Override
    @Transactional
    public void actualizarCredenciales(Long id, String email, String password) {
        Usuario u = usuarioRepo.findById(id).orElseThrow(() ->
                new MagicalNotFoundException("Usuario no encontrado"));

        // --- VALIDACIÓN DE DUPLICADOS ---
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(u.getEmail())) {
            // Buscamos si el nuevo email ya existe en otro ID
            Optional<Usuario> existe = usuarioRepo.findByEmail(email);
            if (existe.isPresent()) {
                throw new MagicalBusinessException("El email " + email + " ya está registrado por otro usuario.");
            }

            u.setEmail(email);
            // Sincronizamos con la ficha de cliente
            if (u.getCliente() != null) {
                u.getCliente().setEmail(email);
            }
        }

        if (password != null && !password.isBlank()) {
            u.setPassword(passwordEncoder.encode(password));
        }

        usuarioRepo.save(u);
    }

    // --- EDITAR DATOS PERSONALES (Nombre/Apel/Fec/Tel) ---
    @Override
    @Transactional
    public Usuario actualizarDatosPersonales(Long id, Cliente nuevos) {
        // Solo permitimos la edición si el usuario existe (es decir, está registrado)
        Usuario u = usuarioRepo.findById(id).orElseThrow(() ->
                new MagicalNotFoundException("Usuario no encontrado"));

        // Si llegamos aquí, es porque es un Usuario Registrado o el Admin editando.
        Cliente c = u.getCliente();

        if (nuevos.getNombre() != null) c.setNombre(nuevos.getNombre());
        if (nuevos.getApellido() != null) c.setApellido(nuevos.getApellido());
        if (nuevos.getTelefono() != null) c.setTelefono(nuevos.getTelefono());

        // AJUSTE: Validación de edad al editar
        if (nuevos.getFechaNacimiento() != null) {
            c.setFechaNacimiento(nuevos.getFechaNacimiento());
            if (c.getEdad() < 18 || c.getEdad() > 105) throw new MagicalBusinessException("La edad debe estar entre 18 y 105 años.");
        }

        // Sincronización de Email (Privilegio de cuenta)
        if (nuevos.getEmail() != null && !nuevos.getEmail().isBlank()) {
            // Validar que el nuevo email no lo tenga otro
            if (!nuevos.getEmail().equalsIgnoreCase(u.getEmail()) &&
                    usuarioRepo.findByEmail(nuevos.getEmail()).isPresent()) {
                throw new MagicalBusinessException("El email ya está en uso por otro usuario.");
            }
            c.setEmail(nuevos.getEmail());
            u.setEmail(nuevos.getEmail());
        }

        return usuarioRepo.save(u);
    }

    @Override
    @Transactional
    public Cliente actualizarOCrearClienteInvitado(Cliente datosEntrantes) {
        // 1. Limpiamos el RUT antes de cualquier operación
        String rutLimpio = limpiarRut(datosEntrantes.getRut());

        // 2. Buscamos si ya existe en la base de datos
        return clienteRepo.findByRut(rutLimpio).map(clienteExistente -> {

            // 3. Verificamos si este cliente tiene un Usuario asociado
            // Usamos una consulta al repo de usuarios para ver si existe el rut
            boolean esUsuarioRegistrado = usuarioRepo.findByClienteRut(rutLimpio).isPresent();

            if (!esUsuarioRegistrado) {
                // SI ES SOLO INVITADO: Actualizamos todo automáticamente
                clienteExistente.setNombre(datosEntrantes.getNombre());
                clienteExistente.setApellido(datosEntrantes.getApellido());
                clienteExistente.setEmail(datosEntrantes.getEmail());
                clienteExistente.setTelefono(datosEntrantes.getTelefono());
                clienteExistente.setFechaNacimiento(datosEntrantes.getFechaNacimiento());

                // Las direcciones se manejan por el CascadeType.ALL que ya tienes
                if (datosEntrantes.getDirecciones() != null) {
                    clienteExistente.getDirecciones().clear();
                    datosEntrantes.getDirecciones().forEach(d -> {
                        d.setCliente(clienteExistente);
                        clienteExistente.getDirecciones().add(d);
                    });
                }
                return clienteRepo.save(clienteExistente);
            }

            // SI ES USUARIO REGISTRADO: No actualizamos aquí,
            // porque debe hacerlo desde su perfil con su clave.
            return clienteExistente;

        }).orElseGet(() -> {
            // 4. Si el RUT no existe, es un cliente nuevo absoluto
            datosEntrantes.setRut(rutLimpio);
            return clienteRepo.save(datosEntrantes);
        });
    }

    // --- ELIMINAR USUARIO ---
    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        // CANDADO 2: No se puede eliminar al ID 1
        if (id == 1) {
            throw new MagicalBusinessException("El Administrador Principal no puede ser eliminado.");
        }
        if (!usuarioRepo.existsById(id)) {
            throw new MagicalNotFoundException("El usuario que intentas eliminar no existe.");
        }
        usuarioRepo.deleteById(id);
    }


    // --- GESTIÓN DE DIRECCIONES ---
    @Override
    @Transactional
    public void agregarDireccion(Long usuarioId, DireccionCliente dir) {
        Usuario u = usuarioRepo.findById(usuarioId).orElseThrow(()->
                new MagicalNotFoundException("Usuario no encontrado"));
        dir.setCliente(u.getCliente());
        u.getCliente().getDirecciones().add(dir);
        usuarioRepo.save(u);
    }

    @Override
    @Transactional
    public void editarDireccion(Long direccionId, DireccionCliente nuevos) {
        // 1. Buscamos la dirección persistente (la que ya existe en la BD)
        DireccionCliente dirExistente = direccionRepo.findById(direccionId)
                .orElseThrow(() -> new MagicalNotFoundException("Error: La dirección no existe."));

        // 2. Actualizamos solo si los datos nuevos no son nulos (mantenemos lo anterior si no vienen)
        if (nuevos.getDireccion() != null) dirExistente.setDireccion(nuevos.getDireccion());
        if (nuevos.getCiudad() != null) dirExistente.setCiudad(nuevos.getCiudad());
        if (nuevos.getEstadoRegion() != null) dirExistente.setEstadoRegion(nuevos.getEstadoRegion());
        if (nuevos.getPais() != null) dirExistente.setPais(nuevos.getPais());
        if (nuevos.getCodigoPostal() != null) dirExistente.setCodigoPostal(nuevos.getCodigoPostal());

        // Lógica para 'esPrincipal'
        if (nuevos.isEsPrincipal()) {
            // Si esta va a ser la principal, primero marcamos todas las demás del mismo cliente como FALSE
            dirExistente.getCliente().getDirecciones().forEach(d -> d.setEsPrincipal(false));
            dirExistente.setEsPrincipal(true);
        } else {
            dirExistente.setEsPrincipal(nuevos.isEsPrincipal());
        }

        // 3. Guardamos los cambios
        direccionRepo.save(dirExistente);
    }

    @Override
    @Transactional
    public void eliminarDireccion(Long usuarioId, Long direccionId) {
        Usuario u = usuarioRepo.findById(usuarioId).orElseThrow(() ->
                new MagicalNotFoundException("Error: La dirección no existe."));
        // orphanRemoval = true hará que al sacarla de la lista, se borre de la BD
        u.getCliente().getDirecciones().removeIf(d -> d.getId().equals(direccionId));
        usuarioRepo.save(u);
    }
}