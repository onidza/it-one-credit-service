package ru.creditbank.creditservice.operation.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.creditbank.creditservice.operation.entity.Status;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String senderEmail;

  public void sendStatusChangedEmail(
      String recipientEmail,
      String fullName,
      UUID applicationId,
      Status status,
      String managerComment
  ) {
    SimpleMailMessage message = new SimpleMailMessage();

    message.setFrom(senderEmail);
    message.setTo(recipientEmail);
    message.setSubject(
        "Ваша кредитная заявка #" + applicationId
    );

    String comment = managerComment == null
        || managerComment.isBlank()
        ? "Комментарий отсутствует"
        : managerComment.strip();

    message.setText("""
        Уважаемый %s!
        
        Статус вашей кредитной заявки изменён на: %s.
        
        Комментарий менеджера: %s
        """.formatted(
        fullName,
        status,
        comment
    ));

    mailSender.send(message);

    log.info(
        "Email notification sent, applicationId = {}, recipient = {}",
        applicationId,
        recipientEmail
    );
  }
}
