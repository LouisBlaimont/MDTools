package be.uliege.speam.team03.MDTools.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.models.User;


@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String to, String subject, String text) throws MailException{
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public void sendRegistrationEmail(String to, User user) throws MailException {
        String subject = "Registration Confirmation";
        String text = "Dear " + user.getUsername() + ",\n\n"
                + "Thank you for registering with MDTools. Please click the link below to confirm your registration:\n\n"
                + baseUrl + "/auth/registration?token=" + user.getResetToken() + "\n\n" + 
                "This token is valid until : " + user.getResetTokenExpiration() + "\n\n" +
                "Best regards,\nMDTools Team";
        sendEmail(to, subject, text);
    }

    public void sendPasswordResetEmail(String to, User user) throws MailException {
        String subject = "Password Reset";
        String text = "Dear " + user.getUsername() + ",\n\n"
                + "You have requested a password reset. Please click the link below to reset your password:\n\n"
                + baseUrl + "/auth/reset-password?token=" + user.getResetToken() + "\n\n" + 
                "This token is valid until : " + user.getResetTokenExpiration() + "\n\n" +
                "Best regards,\nMDTools Team";
        sendEmail(to, subject, text);
    }
   
}
