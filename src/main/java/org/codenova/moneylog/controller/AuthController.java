package org.codenova.moneylog.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.entity.Verification;
import org.codenova.moneylog.repository.UserRepository;
import org.codenova.moneylog.repository.VerificationRepository;
import org.codenova.moneylog.request.FindPasswordRequest;
import org.codenova.moneylog.request.LoginRequest;
import org.codenova.moneylog.service.KakaoApiService;
import org.codenova.moneylog.service.MailService;
import org.codenova.moneylog.service.NaverApiService;
import org.codenova.moneylog.vo.KakaoTokenResponse;
import org.codenova.moneylog.vo.NaverProfileResponse;
import org.codenova.moneylog.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/auth")

// ======================================================================================
// 의존성 주입 (DI)

public class AuthController {
    private KakaoApiService kakaoApiService;
    private NaverApiService naverApiService;
    private MailService mailService;
    private UserRepository userRepository;
    private VerificationRepository verificationRepository;

    // ======================================================================================
    // 로그인 화면 렌더링 처리 핸들

    @GetMapping("/login")
    public String loginHandle(Model model) {

        // 소셜 로그인에 필요한 클라이언트 ID 및 Redirect URI 설정
        model.addAttribute("kakaoClientId", "4b3f549d8e9aa658e6b6c56845dc99e3");
        model.addAttribute("kakaoRedirectUri", "http://192.168.10.97:8080/auth/kakao/callback");

        model.addAttribute("naverClientId", "qgbepxDWYD0ib9tnY65b");
        model.addAttribute("naverRedirectUri", "http://192.168.10.97:8080/auth/naver/callback");

        return "auth/login";
    }

    // ======================================================================================
    // 일반 로그인 처리 핸들

    @PostMapping("/login")
    public String loginPostHandle(
            @ModelAttribute LoginRequest loginRequest,
            HttpSession session,
            Model model) {

        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {

            session.setAttribute("user", user);

            return "redirect:/index";

        } else {

            return "redirect:/auth/login";
        }
    }


    // ======================================================================================
    // 회원가입 처리 핸들

    @GetMapping("/signup")
    public String signupGetHandle(Model model) {
        LocalDateTime.now().plusDays(1); // 유효기간 설정 하루

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user) {

        User found = userRepository.findByEmail(user.getEmail());

        if (found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
            mailService.sendWelcomeHtmlMessage(user);
        }

        return "redirect:/index";
    }

    // ==================================================================================
    // 비밀번호 찾기 핸들

    @GetMapping("/find-password")
    public String findPasswordHandle(Model model) {

        return "auth/find-password";
    }

    @PostMapping("/find-password")
    public String findPasswordPostHandle(@ModelAttribute @Valid FindPasswordRequest req,
                                         BindingResult result,
                                         Model model) {

        // 유효성 검사
        if (result.hasErrors()) {
            model.addAttribute("error", "이메일 형식이 아닙니다.");

            return "auth/find-password-error";
        }

        User found = userRepository.findByEmail(req.getEmail());

        if (found == null) {   // 가입한 적 없는 이메일이라면
            model.addAttribute("error", "해당 이메일로 임시번호를 전송할 수 없습니다.");

            return "auth/find-password-error";
        }

        String temporalPassword = UUID.randomUUID().toString().substring(0, 8);// 랜덤 비밀번호 (임시 비밀번호)
        userRepository.updatePasswordByEmail(req.getEmail(), temporalPassword);
        mailService.sendTemporalPasswordMessage(req.getEmail(), temporalPassword); // 보내야할 사용자, 임시 비밀번호 정보 넘기기

        return "auth/find-password-success";
    }

    // ==================================================================================
    // 토큰 유효성 검사 핸들

    @GetMapping("/email-verify")
    public String emailVerifyHandle(@RequestParam("token") String token, Model model) {

        Verification found = verificationRepository.findByToken(token);

        if (found == null) {
            model.addAttribute("error", "유효하지 않은 인증토큰 입니다.");

            return "auth/email-verify-error";
        }

        // found.getExpiresAt();   // 토큰이 가진 유효만료시점
        // LocalDateTime.now();    // 인증 시점
        if (LocalDateTime.now().isAfter(found.getExpiresAt())) {
            model.addAttribute("error", "유효기간이 만료된 인증토큰 입니다.");

            return "auth/email-verify-error";
        }

        String userEmail = found.getUserEmail();
        userRepository.updateVerifiedByEmail(userEmail);

        return "auth/email-verify-success";
    }


    // ==================================================================================
    // 인증 메일 재전송 핸들


    @GetMapping("/send-token")
    public String sendTokenHandle(@SessionAttribute("user") User user,
                                  Model model) {

        String token = UUID.randomUUID().toString().replace("-", "");

        Verification one = Verification.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .userEmail(user.getEmail())
                .build();

        verificationRepository.save(one);

        // 어디다가 보내야하는지와 생성된 토큰번호를 넘겨줘야함.
        mailService.sendVerificationMessage(user, one);

        return "auth/send-token";
    }


    // ==================================================================================
    // 카카오 로그인 콜백 처리 핸들

    @GetMapping("/kakao/callback")
    public String kakaoCallbackHandle(@RequestParam("code") String code,
                                      HttpSession session) throws JsonProcessingException {

        // log.info("code = {}", code);  // 카카오에서 보낸 사용자의 인증코드

        KakaoTokenResponse response = kakaoApiService.exchangeToken(code);


        log.info("response.idToken = {}", response.getIdToken());

        DecodedJWT decodedJWT = JWT.decode(response.getIdToken());
        String sub = decodedJWT.getClaim("sub").asString();
        String nickname = decodedJWT.getClaim("nickname").asString();
        String picture = decodedJWT.getClaim("picture").asString();

        User found = userRepository.findByProviderAndProviderId("KAKAO", sub);

        // 세션에 user 값이 있다면
        if (found != null) {
            session.setAttribute("user", found);

            // DB에 없는 user 라면
        } else {
            // user 객체를 만들어서 save
            User user = User.builder()
                    .provider("KAKAO")
                    .providerId(sub)
                    .nickname(nickname)
                    .picture(picture)
                    .verified("T")
                    .build();

            userRepository.save(user);
            session.setAttribute("user", user);
         // session.setAttribute("userId", user.getId());
        }

        // log.info("decodedJWT: sub={}, nickname={}, picture={}", sub, nickname, picture);

        return "redirect:/";
    }

    // ======================================================================================
    // 네이버 로그인 콜백 처리 핸들

    @GetMapping("/naver/callback")
    public String naverCallbackHandle(@RequestParam("code") String code,
                                      @RequestParam("state") String state,
                                      HttpSession session) throws JsonProcessingException {

        // log.info("code = {}, state = {}", code, state);

        NaverTokenResponse tokenResponse = naverApiService.exchangeToken(code, state);

        // log.info("accessToken = {}", tokenResponse.getAccessToken());

        NaverProfileResponse profileResponse = naverApiService.exchangeProfile(tokenResponse.getAccessToken());

        log.info("profileResponse id = {}", profileResponse.getId());
        log.info("profileResponse nickname = {}", profileResponse.getNickname());
        log.info("profileResponse profileImage = {}", profileResponse.getProfileImage());

        User found = userRepository.findByProviderAndProviderId("NAVER", profileResponse.getId());

        // 세션에 user 값이 없다면
        // user 객체를 만들어서 save
        if (found == null) {
            User user = User.builder()
                    .provider("NAVER")
                    .providerId(profileResponse.getId())
                    .nickname(profileResponse.getNickname())
                    .picture(profileResponse.getProfileImage())
                    .verified("T")
                    .build();

            userRepository.save(user);

            session.setAttribute("user", user);
            // session.setAttribute("userId", user.getId());

            // DB에 있는 user 라면
        } else {
            session.setAttribute("user", found);
        }

        return "redirect:/index";
    }
}