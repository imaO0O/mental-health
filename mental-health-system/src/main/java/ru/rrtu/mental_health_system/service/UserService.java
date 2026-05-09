package ru.rrtu.mental_health_system.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rrtu.mental_health_system.dto.RegisterRequest;
import ru.rrtu.mental_health_system.model.Psychologist;
import ru.rrtu.mental_health_system.model.Role;
import ru.rrtu.mental_health_system.model.Student;
import ru.rrtu.mental_health_system.model.User;
import ru.rrtu.mental_health_system.repository.PsychologistRepository;
import ru.rrtu.mental_health_system.repository.RoleRepository;
import ru.rrtu.mental_health_system.repository.StudentRepository;
import ru.rrtu.mental_health_system.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PsychologistRepository psychologistRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       StudentRepository studentRepository,
                       PsychologistRepository psychologistRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.psychologistRepository = psychologistRepository;
    }

    @Transactional
    public User registerUserWithProfile(RegisterRequest req) {
        if (userRepository.existsByLogin(req.getLogin())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        String roleName = req.getRole() != null && !req.getRole().isBlank() ? req.getRole() : "STUDENT";
        if ("ADMIN".equals(roleName)) {
            throw new IllegalArgumentException("Регистрация администратора недоступна");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setLogin(req.getLogin());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setRole(role);
        user = userRepository.save(user);

        if ("STUDENT".equals(roleName)) {
            Student s = new Student();
            s.setUser(user);
            s.setLastName(orDefault(req.getLastName(), "—"));
            s.setFirstName(orDefault(req.getFirstName(), req.getLogin()));
            s.setMiddleName(req.getMiddleName());
            s.setGroupName(orDefault(req.getGroupName(), "—"));
            s.setRecordBookNumber(req.getRecordBookNumber() != null
                    ? req.getRecordBookNumber()
                    : (20240000_0000L + (System.currentTimeMillis() % 9999_9999L)));
            s.setEmail(req.getEmail());
            s.setPhone(req.getPhone());
            studentRepository.save(s);
        } else if ("PSYCHOLOGIST".equals(roleName)) {
            Psychologist p = new Psychologist();
            p.setUser(user);
            p.setLastName(orDefault(req.getLastName(), "—"));
            p.setFirstName(orDefault(req.getFirstName(), req.getLogin()));
            p.setMiddleName(req.getMiddleName());
            p.setPosition(orDefault(req.getPosition(), "Психолог"));
            p.setPersonnelNumber(req.getPersonnelNumber() != null
                    ? req.getPersonnelNumber()
                    : (100_000L + (System.currentTimeMillis() % 899_999L)));
            p.setEmail(req.getEmail());
            p.setPhone(req.getPhone());
            psychologistRepository.save(p);
        }
        return user;
    }

    private static String orDefault(String v, String d) {
        return (v == null || v.isBlank()) ? d : v;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
        );
    }

    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(String login) {
        return userRepository.findById(login);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User registerUser(String login, String password, String roleName) {
        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Обновление пользователя. Логин — это естественный PK, поэтому
     * поменять можно только роль (и пароль — отдельным методом).
     * Параметр newLogin сохранён для совместимости вызывающего кода
     * и игнорируется, если совпадает с существующим логином.
     */
    @Transactional
    public User updateUser(String login, String newLogin, String roleName) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (roleName != null && !roleName.isBlank()) {
            Role role = roleRepository.findById(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));
            user.setRole(role);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String login, String newPassword) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String login) {
        userRepository.deleteById(login);
    }

    @Transactional(readOnly = true)
    public boolean isUserExists(String login) {
        return userRepository.existsByLogin(login);
    }
}
