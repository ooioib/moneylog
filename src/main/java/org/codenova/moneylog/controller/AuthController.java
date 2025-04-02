package org.codenova.moneylog.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.UserRepository;
import org.codenova.moneylog.request.LoginRequest;
import org.codenova.moneylog.service.KakaoApiService;
import org.codenova.moneylog.service.NaverApiService;
import org.codenova.moneylog.vo.KakaoTokenResponse;
import org.codenova.moneylog.vo.NaverProfileResponse;
import org.codenova.moneylog.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {
    private KakaoApiService kakaoApiService;
    private NaverApiService naverApiService;
    private UserRepository userRepository;

    // ======================================================================================

    @GetMapping("/login")
    public String loginHandle(Model model) {

        model.addAttribute("kakaoClientId", "4b3f549d8e9aa658e6b6c56845dc99e3");
        model.addAttribute("kakaoRedirectUri", "http://192.168.10.97:8080/auth/kakao/callback");

        model.addAttribute("naverClientId", "qgbepxDWYD0ib9tnY65b");
        model.addAttribute("naverRedirectUri", "http://192.168.10.97:8080/auth/naver/callback");

        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPostHandle(
            @ModelAttribute LoginRequest loginRequest,
            HttpSession session, Model model) {

        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            session.setAttribute("user", user);

            return "redirect:/index";

        } else {

            return "redirect:/auth/login";
        }
    }

    // ======================================================================================

    @GetMapping("/signup")
    public String signupGetHandle(Model model) {

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user) {

        User found = userRepository.findByEmail(user.getEmail());

        if (found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
        }

        return "redirect:/index";
    }

    // ======================================================================================

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
        }

        // log.info("decodedJWT: sub={}, nickname={}, picture={}", sub, nickname, picture);

        return "redirect:/";
    }

    // ======================================================================================


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

         // DB에 있는 user 라면
        } else {
            session.setAttribute("user", found);
        }

        return "redirect:/index";
    }
}