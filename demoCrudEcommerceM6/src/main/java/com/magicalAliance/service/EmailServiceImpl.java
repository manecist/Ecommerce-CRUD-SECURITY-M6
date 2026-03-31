package com.magicalAliance.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Override
    public void enviarConfirmacionSuscripcion(String destinatario) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("✨ ¡Bienvenida/o a la Alianza Mágica!");

            String cuerpo = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; background-color: #1a1a2e; color: #ffffff; border-radius: 12px; overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #f480ff, #9b59b6); padding: 30px; text-align: center;">
                            <h1 style="margin: 0; font-size: 26px; color: #ffffff;">✨ Magical Alliance</h1>
                            <p style="margin: 8px 0 0; font-size: 14px; color: #ffe0ff;">Tu comunidad mágica de belleza</p>
                        </div>
                        <div style="padding: 30px;">
                            <h2 style="color: #f480ff;">¡Gracias por unirte!</h2>
                            <p style="color: #cccccc; line-height: 1.6;">
                                A partir de ahora serás de las primeras en enterarte de nuestras
                                <strong style="color: #f480ff;">promociones exclusivas</strong>,
                                nuevos productos y novedades del mundo mágico.
                            </p>
                            <div style="margin: 25px 0; text-align: center;">
                                <a href="#" style="background: linear-gradient(135deg, #f480ff, #9b59b6); color: #ffffff; text-decoration: none; padding: 12px 30px; border-radius: 25px; font-weight: bold; font-size: 15px;">
                                    Visitar la tienda ✨
                                </a>
                            </div>
                            <p style="color: #888888; font-size: 12px; text-align: center; margin-top: 30px;">
                                Si no solicitaste esta suscripción, puedes ignorar este mensaje.
                            </p>
                        </div>
                        <div style="background-color: #111; padding: 15px; text-align: center;">
                            <p style="margin: 0; color: #666666; font-size: 12px;">© 2026 Magical Alliance — Todos los derechos reservados.</p>
                        </div>
                    </div>
                    """;

            helper.setText(cuerpo, true);
            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de confirmación: " + e.getMessage(), e);
        }
    }
}