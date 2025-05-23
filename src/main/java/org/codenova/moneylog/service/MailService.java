package org.codenova.moneylog.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.entity.Verification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {

    private JavaMailSender mailSender;

    // ==============================================================================
    // SimpleMailMessage 사용해서 메일 전송
    public boolean sendWelcomeMessage(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[코드노바] 머니로그 회원가입을 환영합니다.");
        message.setText("안녕하세요, " + user.getNickname() + "님!\n머니로그에 가입해주셔서 감사합니다.\n\n앞으로 다양한 콘텐츠와 서비스를 제공해드리겠습니다.\n\n팀 코드노바 드림");

        boolean result = true;

        try {
            mailSender.send(message);

        } catch (Exception e) {
            log.error("error = {}", e);
            result = false;

        }

        return false;
    }

    // ==============================================================================
    // MimeMessage 사용해서 HTML 전송
    public boolean sendWelcomeHtmlMessage(User user) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "utf-8");
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("[코드노바] 머니로그 회원가입을 환영합니다.");

            // html 적용 (MimeMessage는 html을 보낼 수 있음)
            String html = "<h2>안녕하세요. 머니로그입니다.</h2>";
            html += "<a href='http://192.168.10.97:8080'>머니로그</a>에 가입하신 것을 진심으로 환영합니다.";
            html += "<p>앞으로 다양한 컨텐츠와 서비스를 제공해 드리겠습니다. </p>";
            html += "<p><span style='color : gray'>팀 코드노바 올림</span></p>";

            messageHelper.setText(html, true); // html 보내기

            mailSender.send(message);

        } catch (Exception e) {
            log.error("error = {}", e);
        }

        return true;
    }

    // ==============================================================================
    // 임시 비밀번호 전송
    public boolean sendTemporalPasswordMessage(String email, String temporalPassword) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("[코드노바] 임시 비밀번호를 발급하였습니다.");
        message.setText("임시 비밀번호는 " + temporalPassword + "입니다.");

        boolean result = true;

        try {
            mailSender.send(message);

        } catch (Exception e) {
            log.error("error = {}", e);

            result = false;
        }

        return result;
    }


    // ==============================================================================
    // 이메일 인증 메일 전송
    public boolean sendVerificationEmail(User user, String token) {

        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[코드노바] 이메일 인증 요청");
        message.setText("아래 링크를 클릭하여 이메일 인증을 하세요.\n" + verificationLink);

        boolean result = true;

        try {
            mailSender.send(message);


        } catch (Exception e) {
            log.error("error = {}", e);

            result = false;

        }

        return false;
    }

    // ==============================================================================

    public boolean sendVerificationMessage(User user, Verification verification) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject("[코드노바] 머니로그 이메일 인증");
        String text = "안녕하세요, " + user.getNickname() + "님\n";
        text += "이메일 인증 토큰을 보내드립니다.\n\n토큰값 : " + verification.getToken() + "\n";
        text += "인증 토큰 유효기간은 " + verification.getExpiresAt() + "까지 입니다.\n\n";
        text += "인증 링크 :  http://192.168.10.97:8080/auth/email-verify?token=" + verification.getToken();
        text += "\n\n";

        text += "팀 코드노바";

        message.setText(text);

        boolean result = true;

        try {
            mailSender.send(message);

        } catch (Exception e) {
            log.error("error = {}", e.getMessage());

            result = false;
        }

        return result;
    }
}
