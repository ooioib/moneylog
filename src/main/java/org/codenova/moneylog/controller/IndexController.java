package org.codenova.moneylog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class IndexController {

    @GetMapping("/index")
    public String indexHandle() {
        return "index";
    }
}
