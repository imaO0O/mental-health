package ru.rrtu.mental_health_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rrtu.mental_health_system.model.User;
import ru.rrtu.mental_health_system.repository.RoleRepository;
import ru.rrtu.mental_health_system.repository.TestResultRepository;
import ru.rrtu.mental_health_system.service.TestService;
import ru.rrtu.mental_health_system.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TestService testService;
    private final RoleRepository roleRepository;
    private final TestResultRepository testResultRepository;

    public AdminController(UserService userService,
                           TestService testService,
                           RoleRepository roleRepository,
                           TestResultRepository testResultRepository) {
        this.userService = userService;
        this.testService = testService;
        this.roleRepository = roleRepository;
        this.testResultRepository = testResultRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("allTests", testService.findAll());
        model.addAttribute("studentsCount", userService.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole().getName()))
                .count());
        model.addAttribute("psychologistsCount", userService.findAll().stream()
                .filter(u -> "PSYCHOLOGIST".equals(u.getRole().getName()))
                .count());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String roleName,
                        Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        String query = q == null ? "" : q.trim().toLowerCase();
        java.util.List<User> filtered = userService.findAll().stream()
                .filter(u -> query.isEmpty() || u.getLogin().toLowerCase().contains(query))
                .filter(u -> roleName == null || roleName.isBlank()
                        || (u.getRole() != null && roleName.equals(u.getRole().getRoleName())))
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("allUsers", filtered);
        model.addAttribute("allRoles", roleRepository.findAll());
        model.addAttribute("q", q);
        model.addAttribute("filterRoleId", roleName);
        return "admin/users";
    }

    @PostMapping("/user/{login}/role")
    public String changeUserRole(@PathVariable String login, @RequestParam String roleName) {
        userService.updateUser(login, null, roleName);
        return "redirect:/admin/users";
    }

    @PostMapping("/user/{login}/password")
    public String resetPassword(@PathVariable String login, @RequestParam String password) {
        if (password == null || password.isBlank()) {
            return "redirect:/admin/users?error=empty_password";
        }
        userService.updatePassword(login, password);
        return "redirect:/admin/users?reset=true";
    }

    @GetMapping("/user/{login}/delete")
    public String deleteUser(@PathVariable String login, Authentication authentication) {
        User currentUser = userService.findByLogin(authentication.getName()).orElse(null);
        if (currentUser == null) return "redirect:/login";

        if (currentUser.getLogin().equals(login)) {
            return "redirect:/admin/users?error=cannot_delete_self";
        }

        userService.deleteUser(login);
        return "redirect:/admin/users";
    }

    @GetMapping("/tests")
    public String tests(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String status,
                        Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        String query = q == null ? "" : q.trim().toLowerCase();
        var tests = testService.findAll().stream()
                .filter(t -> query.isEmpty() || (t.getName() != null && t.getName().toLowerCase().contains(query)))
                .filter(t -> status == null || status.isBlank()
                        || ("active".equals(status) && Boolean.TRUE.equals(t.getIsActive()))
                        || ("inactive".equals(status) && !Boolean.TRUE.equals(t.getIsActive())))
                .toList();

        java.util.Map<String, Integer> questionCounts = new java.util.HashMap<>();
        for (var t : tests) {
            questionCounts.put(t.getTestCode(), testService.getQuestionsByTestId(t.getTestCode()).size());
        }
        model.addAttribute("user", user);
        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", questionCounts);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "admin/tests";
    }

    @GetMapping("/test/{code}/delete")
    public String deleteTest(@PathVariable("code") String testCode, Authentication authentication) {
        userService.findByLogin(authentication.getName()).orElse(null);
        testService.deleteTest(testCode);
        return "redirect:/admin/tests";
    }

    @GetMapping("/statistics")
    public String statistics(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        var allUsers = userService.findAll();
        var allTests = testService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalTests", allTests.size());
        model.addAttribute("totalResults", testResultRepository.count());
        model.addAttribute("studentsCount", allUsers.stream()
                .filter(u -> "STUDENT".equals(u.getRole().getName()))
                .count());
        model.addAttribute("psychologistsCount", allUsers.stream()
                .filter(u -> "PSYCHOLOGIST".equals(u.getRole().getName()))
                .count());
        model.addAttribute("adminsCount", allUsers.stream()
                .filter(u -> "ADMIN".equals(u.getRole().getName()))
                .count());

        return "admin/statistics";
    }
}
