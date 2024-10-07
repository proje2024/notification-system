package com.example.notification_system.service;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notification_system.model.EmailLog;
import com.example.notification_system.model.Event;
import com.example.notification_system.model.Participant;
import com.example.notification_system.repository.EmailLogRepository;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final String baseUrl;
    private final String fileUploadDir;

    @Autowired
    public EmailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
        String frontendHost = System.getenv("FRONTEND_HOST");
        String frontendPort = System.getenv("FRONTEND_PORT");
        this.baseUrl = frontendHost + ":" + frontendPort;
        this.fileUploadDir = System.getenv("FILE_UPLOAD_DIR");
    }

    @Transactional(readOnly = false) 
    public EmailLog sendEmailToUser(Event event, String email) {
        String text = generateEmailContentForUser(event);
        return sendEmail(event, email, text);
    }

    @Transactional(readOnly = false) 
    public EmailLog sendEmailToParticipant(Event event, Participant participant) {
        String to = participant.getToUserEmail();
        String text = generateEmailContentForParticipant(event);
        return sendEmail(event, to, text);
    }
    @Transactional(readOnly = false)
    private EmailLog sendEmail(Event event, String to, String content) {
        String subject = event.getEventName() + " Etkinliği Bildirimi";
        MimeMessage message = mailSender.createMimeMessage();
        EmailLog emailLog = new EmailLog();
        emailLog.setEvent(event);
        emailLog.setEmail(to);
        emailLog.setAttemptTime(OffsetDateTime.now());

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            if (event.getFileName() != null && !event.getFileName().isEmpty()) {
                File file = new File(fileUploadDir + "/" + event.getFileName());
                if (file.exists()) {
                    helper.addAttachment(file.getName(), file);
                } else {
                    emailLog.setStatus("FAILED");
                    emailLogRepository.save(emailLog);
                    throw new RuntimeException("Dosya bulunamadı: " + file.getPath());
                }
            }

            mailSender.send(message);
            emailLog.setStatus("SENT");
        } catch (MessagingException | RuntimeException e) {
            emailLog.setStatus("FAILED");
        }

        return emailLogRepository.save(emailLog);
    }

    private String generateEmailContentForUser(Event event) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("dd MMMM yyyy")
            .toFormatter(new Locale("tr"));
    
        String formattedDate = event.getEventTime().format(formatter);
        return "<strong>" + event.getEventName() + "</strong>"+ " isimli etkinliğinizin tarihi: " + formattedDate + "."
                + "<br><br>Eğer bu etkinlik için artık bildirim almak istemiyorsanız, lütfen "
                + "<a href=\"" + baseUrl + "/calendar?cancelNotification=" + event.getId() + "\">buraya tıklayınız</a>"
                + ".<br><br>Yönlendirileceğiniz sayfada takviminizi inceleyerek, ilgili etkinlik için bildirimlerinizi iptal edebilirsiniz.";
    }
    

    private String generateEmailContentForParticipant(Event event) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("dd MMMM yyyy")
            .toFormatter(new Locale("tr"));
    
        String formattedDate = event.getEventTime().format(formatter);
        return "<strong>" + event.getEventName() + "</strong>" + " isimli etkinliğe katılım tarihiniz: " + formattedDate + "."
                + "<br><br>Eğer bu etkinlik için artık bildirim almak istemiyorsanız, lütfen "
                + "<a href=\"" + baseUrl + "/calendar?cancelNotification=" + event.getId() + "\">buraya tıklayınız</a>"
                + ".<br><br>Yönlendirileceğiniz sayfada takviminizi inceleyerek, ilgili etkinlik için bildirimlerinizi iptal edebilirsiniz.";
    }
    
}
