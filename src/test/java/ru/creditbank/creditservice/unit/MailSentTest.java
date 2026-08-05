package ru.creditbank.creditservice.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import ru.creditbank.creditservice.operation.entity.Status;
import ru.creditbank.creditservice.operation.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class MailSentTest {

  @Mock
  private JavaMailSender mailSender;

  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService = new NotificationService(mailSender);

    ReflectionTestUtils.setField(
        notificationService,
        "senderEmail",
        "it_one_smtp@mail.ru"
    );
  }

  @Test
  void shouldSendStatusChangedEmail() {
    UUID applicationId = UUID.randomUUID();

    notificationService.sendStatusChangedEmail(
        "client@mail.ru",
        "Иванов Иван Иванович",
        applicationId,
        Status.APPROVED,
        "Заявка одобрена"
    );

    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);

    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getFrom())
        .isEqualTo("it_one_smtp@mail.ru");

    assertThat(message.getTo())
        .containsExactly("client@mail.ru");

    assertThat(message.getSubject())
        .contains(applicationId.toString());

    assertThat(message.getText())
        .contains(
            "Иванов Иван Иванович",
            "APPROVED",
            "Заявка одобрена"
        );
  }
}
