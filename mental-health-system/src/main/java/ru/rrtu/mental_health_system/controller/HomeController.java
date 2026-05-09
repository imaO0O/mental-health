package ru.rrtu.mental_health_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.rrtu.mental_health_system.model.User;
import ru.rrtu.mental_health_system.service.TestService;
import ru.rrtu.mental_health_system.service.UserService;

@Controller
public class HomeController {

    private final UserService userService;
    private final TestService testService;

    public HomeController(UserService userService, TestService testService) {
        this.userService = userService;
        this.testService = testService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByLogin(authentication.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("tests", testService.findAll());

                String role = user.getRole().getName();
                if ("ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                } else if ("PSYCHOLOGIST".equals(role)) {
                    return "redirect:/psychologist/dashboard";
                } else {
                    return "redirect:/student/dashboard";
                }
            }
        }
        return "redirect:/login";
    }
}
