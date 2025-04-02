package org.codenova.moneylog.controller;

import org.codenova.moneylog.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class IndexController {

    @GetMapping("/index")
    public String indexHandle(@SessionAttribute("user") Optional<User> user, Model model) {

        if (user.isEmpty()) {   // 옵셔널에 데이터가 존재하지 않는다면

            return "index";

        } else {   // 데이터가 존재한다면

            return "redirect:/home";
        }
    }

    @GetMapping("/home")
    public String homeHandle(@SessionAttribute("user") Optional<User> user, Model model) {

        if (user.isPresent()) {   // 옵셔널에 데이터가 존재한다면
            model.addAttribute("user", user.get());

            return "home";

        } else {   // 데이터가 존재하지 않는다면

            return "redirect:/";
        }
    }
}
