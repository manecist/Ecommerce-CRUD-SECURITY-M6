package com.magicalAliance.service.usuario;


import com.magicalAliance.entity.usuario.Usuario;
import com.magicalAliance.exception.MagicalNotFoundException;
import com.magicalAliance.repository.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos al habitante del reino por su email
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new MagicalNotFoundException("No se ha encontrado ningún usuario con el email: " + email));

        // Construimos el objeto UserDetails que Spring Security entiende
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                // 👈 Esto es más directo y seguro: asigna el rol (ej: ROLE_ADMIN)
                .authorities(usuario.getRol().getNombre())
                .build();
    }
}