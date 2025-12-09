package catchthepattern.com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import catchthepattern.com.models.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class EmailSenderService {    
    
    @Value("${catchthepattern.app.email}")
    private String APP_EMAIL;

	@Autowired
    private JavaMailSender javaMailSender;

    @Async
    public void sendEmail(SimpleMailMessage email) {
         javaMailSender.send(email);
    }
    
    public SimpleMailMessage constructResetTokenEmail(
            String contextPath, String token, User user) {
              String url = contextPath + "/user/changePassword?token=" + token;
              String message = "Click this link to reset your password: ";
              return constructEmail("CatchThePattern.com - Reset Password", message + " \r\n" + url, user);
          }
    
    public SimpleMailMessage constructEmail(String subject, String body, 
            User user) {
              SimpleMailMessage email = new SimpleMailMessage();
              email.setSubject(subject);
              email.setText(body);
              email.setTo(user.getUsername());
              email.setFrom(APP_EMAIL);
              return email;
          }
}