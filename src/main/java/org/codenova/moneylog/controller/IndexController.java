package org.codenova.moneylog.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping
public class IndexController {

    private ExpenseRepository expenseRepository;

    @GetMapping({"/", "/index"})
    public String indexHandle(@SessionAttribute("user") Optional<User> user, Model model) {

        if (user.isEmpty()) {   // 옵셔널에 데이터가 존재하지 않는다면

            return "index";

        } else {   // 데이터가 존재한다면

            return "redirect:/home";
        }
    }

    @GetMapping("/home")
    public String homeHandle(@SessionAttribute("user") Optional<User> user, Model model) {


        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endDate = today.plusDays(7 - today.getDayOfWeek().getValue());

        if (user.isPresent()) {   // 옵셔널에 데이터가 존재한다면
            model.addAttribute("user", user.get());
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            model.addAttribute("totalAmount", expenseRepository
                    .weeklyTotalAmountExpense(user.get().getId(),startDate ,endDate));

            model.addAttribute("top3Expense", expenseRepository
                    .WeeklyTop3Expense(user.get().getId(),startDate ,endDate));

            model.addAttribute("topExpense", expenseRepository
                    .WeeklyTopExpense(user.get().getId(),startDate ,endDate));


            return "home";

        } else {   // 데이터가 존재하지 않는다면

            return "redirect:/";
        }
    }
}
