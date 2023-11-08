package org.esupportail.esupagape.service.mail;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import org.esupportail.esupagape.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@EnableConfigurationProperties(ApplicationProperties.class)
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final ApplicationProperties applicationProperties;

    private JavaMailSenderImpl mailSender;

    public MailService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Autowired(required = false)
    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    @Resource
    private TemplateEngine templateEngine;

    public void sendTest(String to) throws MessagingException {
        if (!checkMailSender()) {
            return;
        }
        final Context ctx = new Context(Locale.FRENCH);
        setTemplate(ctx);
        MimeMessageHelper mimeMessage = new MimeMessageHelper(getMailSender().createMimeMessage(), true, "UTF-8");
        String htmlContent = templateEngine.process("mail/email-test.html", ctx);
        addInLineImages(mimeMessage, htmlContent);
        mimeMessage.setSubject("Certificat d'aménagement");
        mimeMessage.setFrom(new InternetAddress(applicationProperties.getApplicationEmail()));
        mimeMessage.setTo(new InternetAddress(to));
        send(mimeMessage.getMimeMessage());
        logger.info("send test email for " + to);
    }

    public void sendAlert(List<String> to) throws MessagingException {
        if (!checkMailSender()) {
            return;
        }
        final Context ctx = new Context(Locale.FRENCH);
        setTemplate(ctx);
        MimeMessageHelper mimeMessage = new MimeMessageHelper(getMailSender().createMimeMessage(), true, "UTF-8");
        String htmlContent = templateEngine.process("mail/email-alert.html", ctx);
        addInLineImages(mimeMessage, htmlContent);
        mimeMessage.setSubject("Nouveau certificat d'aménagement validé");
        mimeMessage.setFrom(new InternetAddress(applicationProperties.getApplicationEmail()));
        List<InternetAddress> internetAddresses = new ArrayList<>();
        for (String s : to) {
            internetAddresses.add(new InternetAddress(s));
        }
        mimeMessage.setTo(internetAddresses.toArray(InternetAddress[]::new));
        send(mimeMessage.getMimeMessage());
        logger.info("send alert email for " + to);
    }

    @Transactional
    public void sendCertificat(InputStream inputStream, String to) throws MessagingException, IOException {
        if (!checkMailSender()) {
            return;
        }
        final Context ctx = new Context(Locale.FRENCH);
        setTemplate(ctx);
        MimeMessageHelper mimeMessage = new MimeMessageHelper(getMailSender().createMimeMessage(), true, "UTF-8");
        String htmlContent = templateEngine.process("mail/email-certificat.html", ctx);
        addInLineImages(mimeMessage, htmlContent);
        mimeMessage.setSubject("Certificat d'aménagement");
        mimeMessage.setFrom(new InternetAddress(applicationProperties.getApplicationEmail()));
        mimeMessage.setTo(new InternetAddress(to));
        File tmpDir = Files.createTempDirectory("esupagape").toFile();
        File certificatFile = new File(tmpDir + "/certificat.pdf");
        FileUtils.copyInputStreamToFile(inputStream, certificatFile);
        mimeMessage.addAttachment("certificat_amenagement.pdf", certificatFile);
        send(mimeMessage.getMimeMessage());
        logger.info("send certificat email for " + to);
    }

    private void send(MimeMessage mimeMessage) {
        if(applicationProperties.getActivateSendEmails()) {
            mailSender.send(mimeMessage);
        }
    }

    private void addInLineImages(MimeMessageHelper mimeMessage, String htmlContent) throws MessagingException {
        mimeMessage.setText(htmlContent, true);
        mimeMessage.addInline("logo", new ClassPathResource("/static/images/logo.png", MailService.class));
        mimeMessage.addInline("logo-univ", new ClassPathResource("/static/images/logo-univ.png", MailService.class));
    }

    private void setTemplate(Context ctx) {
        try {
            ctx.setVariable("logo", getBase64Image(new ClassPathResource("/static/images/logo.png", MailService.class).getInputStream(), "logo.png"));
            ctx.setVariable("logoUrn", getBase64Image(new ClassPathResource("/static/images/logo-univ.png", MailService.class).getInputStream(), "logo-univ.png"));
            try (Reader reader = new InputStreamReader(new ClassPathResource("/static/css/bootstrap.min.css", MailService.class).getInputStream(), UTF_8)) {
                ctx.setVariable("css", FileCopyUtils.copyToString(reader));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        } catch (IOException e) {
            logger.error("error while setting template", e);
        }
    }

    private boolean checkMailSender() {
        if (mailSender == null) {
            logger.warn("message not sended : mail host not configured");
            return false;
        }
        return true;
    }

    public JavaMailSenderImpl getMailSender() {
        return mailSender;
    }

    public String getBase64Image(InputStream is, String name) throws IOException {
        BufferedImage imBuff = ImageIO.read(is);
        return getBase64Image(imBuff, name);
    }

    public String getBase64Image(BufferedImage imBuff, String name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imBuff, "png", baos);
        baos.flush();
        String out = DatatypeConverter.printBase64Binary(baos.toByteArray());
        baos.close();
        return out;
    }

}
