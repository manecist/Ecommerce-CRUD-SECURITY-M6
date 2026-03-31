package com.magicalAliance.controller;

import com.magicalAliance.entity.Suscriptor;
import com.magicalAliance.repository.SuscriptorRepository;
import com.magicalAliance.service.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/subscribir")
public class SuscripcionController {

    @Autowired
    private SuscriptorRepository suscriptorRepository;

    @Autowired
    private IEmailService emailService;

    @PostMapping
    public ResponseEntity<Map<String, String>> suscribir(@RequestParam String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("estado", "error", "mensaje", "El correo ingresado no es válido."));
        }

        if (!suscriptorRepository.existsByEmail(email)) {
            Suscriptor nuevo = Suscriptor.builder().email(email).build();
            suscriptorRepository.save(nuevo);
        }

        try {
            emailService.enviarConfirmacionSuscripcion(email);
        } catch (Exception e) {
            // El correo falló pero igual confirmamos al usuario (ya quedó guardado)
            return ResponseEntity.ok(Map.of(
                    "estado", "ok_sin_correo",
                    "mensaje", "¡Gracias! Tu suscripción fue registrada, aunque el correo de confirmación no pudo enviarse en este momento."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "estado", "ok",
                "mensaje", "¡Listo! Enviamos un correo de confirmación a <strong>" + email + "</strong>. Revisa tu bandeja de entrada."
        ));
    }
}