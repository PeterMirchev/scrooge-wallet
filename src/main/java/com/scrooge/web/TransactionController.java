package com.scrooge.web;

import com.scrooge.model.Transaction;
import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.service.TransactionService;
import com.scrooge.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getTransactionsPage(@AuthenticationPrincipal CurrentPrinciple currentPrinciple) {

        User user = userService.getUserById(currentPrinciple.getId());
        List<Transaction> transactions = transactionService.getAllTransactionsByUserId(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("transactions", transactions);

        modelAndView.setViewName("transactions");

        return modelAndView;
    }
}
