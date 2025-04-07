package org.codenova.moneylog.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.CategoryRepository;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.codenova.moneylog.request.AddExpenseRequest;
import org.codenova.moneylog.request.SearchPeriodRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/expense")
@AllArgsConstructor
public class ExpenseController {
    private CategoryRepository categoryRepository;
    private ExpenseRepository expenseRepository;

    @GetMapping("/history")
    public String historyHandle(@SessionAttribute("user") User user,
                                @ModelAttribute SearchPeriodRequest searchPeriodRequest,
                                Model model) {
        LocalDate startDate;
        LocalDate endDate;

        if (searchPeriodRequest.getStartDate() != null && searchPeriodRequest.getEndDate() != null) {
            startDate = searchPeriodRequest.getStartDate();
            endDate = searchPeriodRequest.getEndDate();

        } else {
            LocalDate today = LocalDate.now();
            startDate = today.minusDays(today.getDayOfMonth() - 1);
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("categorys", categoryRepository.findAll());
        model.addAttribute("now", LocalDate.now());

        model.addAttribute("expenses",
                expenseRepository.findByUserIdAndDuration(user.getId(), startDate, endDate)
        );

        return "expense/history";
    }

    @PostMapping("/history")
    public String historyPostHandle(@ModelAttribute @Valid AddExpenseRequest addExpenseRequest,
                                    BindingResult bindingResult,
                                    @SessionAttribute("user") User user,
                                    Model model) {

        if (bindingResult.hasErrors()) {
            return "expense/history-error";
        }

        Expense expense = Expense.builder()
                .userId(user.getId())
                .expenseDate(addExpenseRequest.getExpenseDate())
                .amount(addExpenseRequest.getAmount())
                .description(addExpenseRequest.getDescription())
                .categoryId(addExpenseRequest.getCategoryId())
                .build();

        expenseRepository.save(expense);

        return "redirect:/expense/history";
    }
}
