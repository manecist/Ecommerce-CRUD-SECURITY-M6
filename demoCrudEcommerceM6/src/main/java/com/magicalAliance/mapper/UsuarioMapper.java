package com.magicalAliance.mapper;

import com.magicalAliance.dto.usuario.RegistroDTO;
import com.magicalAliance.entity.usuario.Cliente;
import com.magicalAliance.entity.usuario.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;

@Component
public class UsuarioMapper {

    public Usuario toUsuario(RegistroDTO dto) {
        Cliente cliente = Cliente.builder()
                .rut(dto.getRut())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmailCliente())
                .telefono(dto.getTelefono())
                .fechaNacimiento(dto.getFechaNacimiento())
                .direcciones(new ArrayList<>())
                .build();

        return Usuario.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .fechaRegistro(LocalDate.now())
                .cliente(cliente)
                .build();
    }
}