package com.magicalAliance.service.usuario;


import com.magicalAliance.entity.usuario.Cliente;
import com.magicalAliance.entity.usuario.DireccionCliente;
import com.magicalAliance.entity.usuario.Usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    // --- LEER ---
    List<Usuario> listarTodos(String orden);
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorRut(String rut);
    Optional<Usuario> obtenerPerfilCompleto(String email);
    List<Usuario> buscarPorCriterio(String termino);

    // --- CREAR ---
    Usuario registrarUsuario(Usuario usuario);
    Usuario crearUsuarioDesdeAdmin(Usuario u, Long idRol);

    // --- ACTUALIZAR ---
    void actualizarCredenciales(Long id, String email, String password);
    Usuario actualizarDatosPersonales(Long id, Cliente datosNuevos);
    void cambiarRol(Long idUsuario, Long idNuevoRol);
    public Cliente actualizarOCrearClienteInvitado(Cliente datosEntrantes);

    // --- ELIMINAR ---
    void eliminarUsuario(Long id);

    // --- DIRECCIONES ---
    void agregarDireccion(Long usuarioId, DireccionCliente direccion);
    void editarDireccion(Long direccionId, DireccionCliente datosNuevos);
    void eliminarDireccion(Long usuarioId, Long direccionId);


}