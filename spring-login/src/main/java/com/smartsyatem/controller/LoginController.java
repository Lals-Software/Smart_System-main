package com.smartsyatem.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.smartsyatem.entity.UserAccount;
import com.smartsyatem.model.LoginForm;
import com.smartsyatem.repository.UserAccountRepository;

@Controller
public class LoginController {

    private final UserAccountRepository userAccountRepository;

    public LoginController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping({"/", "/login"})
    public String showLoginForm(@ModelAttribute("loginForm") LoginForm loginForm) {
        return "login";
    }

    @GetMapping("/accounts")
    public String showAccountList(Model model) {
        List<UserAccount> accounts = userAccountRepository.findAll();
        model.addAttribute("accounts", accounts);
        return "accounts";
    }
}
