package ru.rrtu.mental_health_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.service.AdminService;
import ru.rrtu.mental_health_system.service.UserService;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/tables")
public class AdminTableController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminTableController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.findByLogin(authentication.getName()).orElse(null);
    }

    // Главная страница управления таблицами
    @GetMapping
    public String tables(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "admin/tables/index";
    }

    // ==================== ROLES ====================
    @GetMapping("/roles")
    public String roles(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("roles", adminService.findAllRoles());
        return "admin/tables/roles";
    }

    @PostMapping("/roles/create")
    public String createRole(@RequestParam String name, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.createRole(name);
            redirectAttributes.addFlashAttribute("success", "Роль успешно создана");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/roles";
    }

    @PostMapping("/roles/update")
    public String updateRole(@RequestParam String id, @RequestParam String name,
                             Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateRole(id, name);
            redirectAttributes.addFlashAttribute("success", "Роль успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/roles";
    }

    @PostMapping("/roles/delete")
    public String deleteRole(@RequestParam String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteRole(id);
            redirectAttributes.addFlashAttribute("success", "Роль успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/roles";
    }

    // ==================== STRESS LEVELS ====================
    @GetMapping("/stress-levels")
    public String stressLevels(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("stressLevels", adminService.findAllStressLevels());
        return "admin/tables/stress-levels";
    }

    @PostMapping("/stress-levels/create")
    public String createStressLevel(@RequestParam String name, @RequestParam Short minScore, 
                                     @RequestParam Short maxScore, Authentication authentication, 
                                     RedirectAttributes redirectAttributes) {
        try {
            adminService.createStressLevel(name, minScore, maxScore);
            redirectAttributes.addFlashAttribute("success", "Уровень стресса успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/stress-levels";
    }

    @PostMapping("/stress-levels/update")
    public String updateStressLevel(@RequestParam String id, @RequestParam String name,
                                     @RequestParam Short minScore, @RequestParam Short maxScore,
                                     Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateStressLevel(id, name, minScore, maxScore);
            redirectAttributes.addFlashAttribute("success", "Уровень стресса успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/stress-levels";
    }

    @PostMapping("/stress-levels/delete")
    public String deleteStressLevel(@RequestParam String id, Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteStressLevel(id);
            redirectAttributes.addFlashAttribute("success", "Уровень стресса успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/stress-levels";
    }

    // ==================== TESTS ====================
    @GetMapping("/tests")
    public String tests(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("tests", adminService.findAllTests());
        return "admin/tables/tests";
    }

    @PostMapping("/tests/create")
    public String createTest(@RequestParam String testCode, @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String instructions,
                              @RequestParam(value = "isActive", required = false) Boolean isActive,
                              Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.createTest(testCode, name, description, instructions, isActive);
            redirectAttributes.addFlashAttribute("success", "Тест успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/tests";
    }

    @PostMapping("/tests/update")
    public String updateTest(@RequestParam String id, @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String instructions,
                              @RequestParam(value = "isActive", required = false) Boolean isActive,
                              Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateTest(id, name, description, instructions, isActive);
            redirectAttributes.addFlashAttribute("success", "Тест успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/tests";
    }

    @PostMapping("/tests/delete")
    public String deleteTest(@RequestParam String id, Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteTest(id);
            redirectAttributes.addFlashAttribute("success", "Тест успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/tests";
    }

    // ==================== QUESTIONS ====================
    @GetMapping("/questions")
    public String questions(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("questions", adminService.findAllQuestions());
        model.addAttribute("tests", adminService.findAllTests());
        return "admin/tables/questions";
    }

    @PostMapping("/questions/create")
    public String createQuestion(@RequestParam String testId, @RequestParam String questionText,
                                  @RequestParam Short orderNumber, Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            adminService.createQuestion(testId, questionText, orderNumber);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/questions";
    }

    @PostMapping("/questions/update")
    public String updateQuestion(@RequestParam Long id, @RequestParam(required = false) String testId,
                                  @RequestParam String questionText, @RequestParam Short orderNumber,
                                  Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateQuestion(id, testId, questionText, orderNumber);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/questions";
    }

    @PostMapping("/questions/delete")
    public String deleteQuestion(@RequestParam Long id, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/questions";
    }

    // ==================== ANSWERS ====================
    @GetMapping("/answers")
    public String answers(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("answers", adminService.findAllAnswers());
        model.addAttribute("questions", adminService.findAllQuestions());
        return "admin/tables/answers";
    }

    @PostMapping("/answers/create")
    public String createAnswer(@RequestParam Long questionId, @RequestParam String answerText,
                                @RequestParam Short score, @RequestParam Short orderNumber,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.createAnswer(questionId, answerText, score, orderNumber);
            redirectAttributes.addFlashAttribute("success", "Ответ успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/answers";
    }

    @PostMapping("/answers/update")
    public String updateAnswer(@RequestParam Long id, @RequestParam(required = false) Long questionId,
                                @RequestParam String answerText, @RequestParam Short score,
                                @RequestParam Short orderNumber, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            adminService.updateAnswer(id, questionId, answerText, score, orderNumber);
            redirectAttributes.addFlashAttribute("success", "Ответ успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/answers";
    }

    @PostMapping("/answers/delete")
    public String deleteAnswer(@RequestParam Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteAnswer(id);
            redirectAttributes.addFlashAttribute("success", "Ответ успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/answers";
    }

    // ==================== RECOMMENDATIONS ====================
    @GetMapping("/recommendations")
    public String recommendations(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("recommendations", adminService.findAllRecommendations());
        model.addAttribute("stressLevels", adminService.findAllStressLevels());
        return "admin/tables/recommendations";
    }

    @PostMapping("/recommendations/create")
    public String createRecommendation(@RequestParam String levelId, @RequestParam String recommendationText,
                                        @RequestParam Short orderNumber, Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        try {
            adminService.createRecommendation(levelId, recommendationText, orderNumber);
            redirectAttributes.addFlashAttribute("success", "Рекомендация успешно создана");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/recommendations";
    }

    @PostMapping("/recommendations/update")
    public String updateRecommendation(@RequestParam Long id, @RequestParam(required = false) String levelId,
                                        @RequestParam String recommendationText, @RequestParam Short orderNumber,
                                        Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateRecommendation(id, levelId, recommendationText, orderNumber);
            redirectAttributes.addFlashAttribute("success", "Рекомендация успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/recommendations";
    }

    @PostMapping("/recommendations/delete")
    public String deleteRecommendation(@RequestParam Long id, Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteRecommendation(id);
            redirectAttributes.addFlashAttribute("success", "Рекомендация успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/recommendations";
    }

    // ==================== STUDENTS ====================
    @GetMapping("/students")
    public String students(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("students", adminService.findAllStudents());
        model.addAttribute("users", userService.findAll());
        return "admin/tables/students";
    }

    @PostMapping("/students/create")
    public String createStudent(@RequestParam String login, @RequestParam Long recordBookNumber,
                                 @RequestParam String lastName,
                                 @RequestParam String firstName, @RequestParam(required = false) String middleName,
                                 @RequestParam String groupName, @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String phone,
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.createStudent(login, recordBookNumber, lastName, firstName, middleName, groupName, email, phone);
            redirectAttributes.addFlashAttribute("success", "Студент успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/students";
    }

    @PostMapping("/students/update")
    public String updateStudent(@RequestParam Long id, @RequestParam String lastName,
                                 @RequestParam String firstName, @RequestParam(required = false) String middleName,
                                 @RequestParam String groupName, @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String phone,
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateStudent(id, lastName, firstName, middleName, groupName, email, phone);
            redirectAttributes.addFlashAttribute("success", "Студент успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/students";
    }

    @PostMapping("/students/delete")
    public String deleteStudent(@RequestParam Long id, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("success", "Студент успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/students";
    }

    // ==================== PSYCHOLOGISTS ====================
    @GetMapping("/psychologists")
    public String psychologists(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("psychologists", adminService.findAllPsychologists());
        model.addAttribute("users", userService.findAll());
        return "admin/tables/psychologists";
    }

    @PostMapping("/psychologists/create")
    public String createPsychologist(@RequestParam String login, @RequestParam Long personnelNumber,
                                      @RequestParam String lastName,
                                      @RequestParam String firstName, @RequestParam(required = false) String middleName,
                                      @RequestParam(required = false) String position,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phone,
                                      Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.createPsychologist(login, personnelNumber, lastName, firstName, middleName, position, email, phone);
            redirectAttributes.addFlashAttribute("success", "Психолог успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/psychologists";
    }

    @PostMapping("/psychologists/update")
    public String updatePsychologist(@RequestParam Long id, @RequestParam String lastName,
                                      @RequestParam String firstName, @RequestParam(required = false) String middleName,
                                      @RequestParam(required = false) String position,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phone,
                                      Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updatePsychologist(id, lastName, firstName, middleName, position, email, phone);
            redirectAttributes.addFlashAttribute("success", "Психолог успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/psychologists";
    }

    @PostMapping("/psychologists/delete")
    public String deletePsychologist(@RequestParam Long id, Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            adminService.deletePsychologist(id);
            redirectAttributes.addFlashAttribute("success", "Психолог успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/psychologists";
    }

    // ==================== TEST RESULTS ====================
    @GetMapping("/results")
    public String results(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("results", adminService.findAllTestResults());
        model.addAttribute("stressLevels", adminService.findAllStressLevels());
        return "admin/tables/results";
    }

    @PostMapping("/results/update")
    public String updateResult(@RequestParam Long id, @RequestParam Short totalScore,
                                @RequestParam LocalDate dateTaken, @RequestParam String stressLevelId,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateTestResult(id, totalScore, dateTaken, stressLevelId);
            redirectAttributes.addFlashAttribute("success", "Результат успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/results";
    }

    @PostMapping("/results/delete")
    public String deleteResult(@RequestParam Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteTestResult(id);
            redirectAttributes.addFlashAttribute("success", "Результат успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables/results";
    }
}
