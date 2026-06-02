package ru.rrtu.mental_health_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rrtu.mental_health_system.model.User;
import ru.rrtu.mental_health_system.model.AuditLog;
import ru.rrtu.mental_health_system.repository.RoleRepository;
import ru.rrtu.mental_health_system.repository.TestResultRepository;
import ru.rrtu.mental_health_system.repository.AuditLogRepository;
import ru.rrtu.mental_health_system.service.TestService;
import ru.rrtu.mental_health_system.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TestService testService;
    private final RoleRepository roleRepository;
    private final TestResultRepository testResultRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(UserService userService,
                           TestService testService,
                           RoleRepository roleRepository,
                           TestResultRepository testResultRepository,
                           AuditLogRepository auditLogRepository) {
        this.userService = userService;
        this.testService = testService;
        this.roleRepository = roleRepository;
        this.testResultRepository = testResultRepository;
        this.auditLogRepository = auditLogRepository;
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

    /** Журнал аудита изменений в БД (рис. 40 ПЗ): фильтр по дате, пользователю и таблице. */
    @GetMapping("/audit")
    public String audit(@RequestParam(required = false) String login,
                        @RequestParam(required = false) String table,
                        @RequestParam(required = false) String from,
                        @RequestParam(required = false) String to,
                        Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        java.util.List<AuditLog> all = auditLogRepository.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "actionTime"));

        String loginQ = login == null ? "" : login.trim().toLowerCase();
        String tableQ = table == null ? "" : table.trim();
        java.time.LocalDate fromD = parseAuditDate(from);
        java.time.LocalDate toD = parseAuditDate(to);

        java.util.List<AuditLog> filtered = all.stream()
                .filter(a -> loginQ.isEmpty() || (a.getLogin() != null && a.getLogin().toLowerCase().contains(loginQ)))
                .filter(a -> tableQ.isEmpty() || tableQ.equals(a.getTableName()))
                .filter(a -> fromD == null || (a.getActionTime() != null && !a.getActionTime().toLocalDate().isBefore(fromD)))
                .filter(a -> toD == null || (a.getActionTime() != null && !a.getActionTime().toLocalDate().isAfter(toD)))
                .toList();

        java.util.List<String> tables = all.stream()
                .map(AuditLog::getTableName)
                .filter(java.util.Objects::nonNull)
                .distinct().sorted().toList();

        model.addAttribute("user", user);
        model.addAttribute("logs", filtered);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("tables", tables);
        model.addAttribute("fLogin", login);
        model.addAttribute("fTable", table);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/audit";
    }

    private java.time.LocalDate parseAuditDate(String s) {
        try {
            return (s == null || s.isBlank()) ? null : java.time.LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
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
        var allResults = testResultRepository.findAll();

        long studentsCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "STUDENT".equals(u.getRole().getName()))
                .count();
        long psychologistsCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "PSYCHOLOGIST".equals(u.getRole().getName()))
                .count();
        long adminsCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "ADMIN".equals(u.getRole().getName()))
                .count();

        // Распределение по уровням стресса — для диаграммы и таблицы
        java.util.Map<String, Long> stressDistribution = new java.util.LinkedHashMap<>();
        for (var r : allResults) {
            if (r.getStressLevel() != null) {
                stressDistribution.merge(r.getStressLevel().getName(), 1L, Long::sum);
            }
        }

        // Кол-во прохождений по каждому тесту — для столбчатой диаграммы
        java.util.Map<String, Long> testUsage = new java.util.LinkedHashMap<>();
        for (var t : allTests) {
            long n = allResults.stream()
                    .filter(r -> r.getTest() != null
                            && t.getTestCode().equals(r.getTest().getTestCode()))
                    .count();
            testUsage.put(t.getName(), n);
        }

        model.addAttribute("user", user);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalTests", allTests.size());
        model.addAttribute("totalResults", (long) allResults.size());
        model.addAttribute("studentsCount", studentsCount);
        model.addAttribute("psychologistsCount", psychologistsCount);
        model.addAttribute("adminsCount", adminsCount);
        model.addAttribute("stressDistribution", stressDistribution);
        model.addAttribute("testUsage", testUsage);
        return "admin/statistics";
    }
}
