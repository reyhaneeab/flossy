package com.group11.notificationservice.engine;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeBodyPart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.List;

@Component
public class EmailEngine {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttls;

    private Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        sendEmail(to, subject, body, false);
    }

    public void sendEmail(String to, String subject, String body, boolean isHtml) throws MessagingException {
        Message message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        if (isHtml) {
            message.setContent(body, "text/html; charset=utf-8");
        } else {
            message.setText(body);
        }

        Transport.send(message);
    }

    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException, IOException {
        Message message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/plain");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(attachmentPath);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    public void sendBulkEmail(List<String> recipients, String subject, String body) throws MessagingException {
        for (String recipient : recipients) {
            sendEmail(recipient, subject, body);
        }
    }

    public void sendEmailWithCC(String to, List<String> cc, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

        if (cc != null && !cc.isEmpty()) {
            InternetAddress[] ccAddresses = new InternetAddress[cc.size()];
            for (int i = 0; i < cc.size(); i++) {
                ccAddresses[i] = new InternetAddress(cc.get(i));
            }
            message.setRecipients(Message.RecipientType.CC, ccAddresses);
        }

        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}