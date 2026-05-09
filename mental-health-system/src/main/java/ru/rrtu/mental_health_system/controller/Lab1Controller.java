package ru.rrtu.mental_health_system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class Lab1Controller {

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriver;

    public Lab1Controller(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/lab1")
    public String lab1(Model model) {
        Map<String, String> connectionInfo = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        LocalDateTime requestTime = LocalDateTime.now();

        // Метод 3: Использование Spring DataSource (наиболее предпочтительный)
        try {
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            connectionInfo.put("Способ подключения", "Spring DataSource (внедрение зависимостей)");
            connectionInfo.put("URL базы данных", metaData.getURL());
            connectionInfo.put("Пользователь БД", metaData.getUserName());
            connectionInfo.put("Драйвер JDBC", metaData.getDriverName() + " " + metaData.getDriverVersion());
            connectionInfo.put("Версия БД", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            connectionInfo.put("Протокол", "JDBC " + metaData.getJDBCMajorVersion() + "." + metaData.getJDBCMinorVersion());
            connectionInfo.put("Уровень изоляции", getIsolationLevel(connection.getTransactionIsolation()));
            connectionInfo.put("Поддержка транзакций", metaData.supportsTransactions() ? "Да" : "Нет");
            connectionInfo.put("Время запроса", requestTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

            connection.close();
        } catch (SQLException e) {
            errors.add("Ошибка при подключении через Spring DataSource: " + e.getMessage());
        }

        model.addAttribute("connectionInfo", connectionInfo);
        model.addAttribute("errors", errors);
        model.addAttribute("requestTime", requestTime);

        return "lab1/connection";
    }

    @GetMapping("/lab1/drivermanager")
    public String lab1DriverManager(Model model) {
        Map<String, String> connectionInfo = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        LocalDateTime requestTime = LocalDateTime.now();

        // Метод 1: Использование DriverManager
        Connection conn = null;
        try {
            Class.forName(dbDriver);
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            DatabaseMetaData metaData = conn.getMetaData();

            connectionInfo.put("Способ подключения", "DriverManager (прямое подключение)");
            connectionInfo.put("URL базы данных", metaData.getURL());
            connectionInfo.put("Пользователь БД", metaData.getUserName());
            connectionInfo.put("Драйвер JDBC", metaData.getDriverName() + " " + metaData.getDriverVersion());
            connectionInfo.put("Версия БД", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            connectionInfo.put("Время запроса", requestTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

            conn.close();
        } catch (ClassNotFoundException e) {
            errors.add("Драйвер не найден: " + e.getMessage());
        } catch (SQLException e) {
            errors.add("Ошибка при подключении через DriverManager: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    errors.add("Ошибка закрытия соединения: " + e.getMessage());
                }
            }
        }

        model.addAttribute("connectionInfo", connectionInfo);
        model.addAttribute("errors", errors);
        model.addAttribute("requestTime", requestTime);

        return "lab1/connection";
    }

    @GetMapping("/lab1/datasource")
    public String lab1DataSource(Model model) {
        Map<String, String> connectionInfo = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        LocalDateTime requestTime = LocalDateTime.now();

        // Метод 2: Использование PGSimpleDataSource
        try {
            org.postgresql.ds.PGSimpleDataSource pgDataSource = new org.postgresql.ds.PGSimpleDataSource();
            pgDataSource.setURL(dbUrl);
            pgDataSource.setUser(dbUsername);
            pgDataSource.setPassword(dbPassword);

            Connection connection = pgDataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            connectionInfo.put("Способ подключения", "PGSimpleDataSource (PostgreSQL DataSource)");
            connectionInfo.put("URL базы данных", metaData.getURL());
            connectionInfo.put("Пользователь БД", metaData.getUserName());
            connectionInfo.put("Драйвер JDBC", metaData.getDriverName() + " " + metaData.getDriverVersion());
            connectionInfo.put("Версия БД", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            connectionInfo.put("Хост", pgDataSource.getServerNames()[0]);
            connectionInfo.put("Порт", String.valueOf(pgDataSource.getPortNumbers()[0]));
            connectionInfo.put("Время запроса", requestTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

            connection.close();
        } catch (SQLException e) {
            errors.add("Ошибка при подключении через PGSimpleDataSource: " + e.getMessage());
        }

        model.addAttribute("connectionInfo", connectionInfo);
        model.addAttribute("errors", errors);
        model.addAttribute("requestTime", requestTime);

        return "lab1/connection";
    }

    @GetMapping("/lab1/all")
    public String lab1All(Model model) {
        List<Map<String, String>> allConnections = new ArrayList<>();
        List<String> allErrors = new ArrayList<>();
        LocalDateTime requestTime = LocalDateTime.now();

        // Метод 1: DriverManager
        Map<String, String> driverManagerInfo = getConnectionViaDriverManager(allErrors);
        driverManagerInfo.put("Способ подключения", "Метод 1: DriverManager");
        allConnections.add(driverManagerInfo);

        // Метод 2: PGSimpleDataSource
        Map<String, String> pgDataSourceInfo = getConnectionViaPGSimpleDataSource(allErrors);
        pgDataSourceInfo.put("Способ подключения", "Метод 2: PGSimpleDataSource");
        allConnections.add(pgDataSourceInfo);

        // Метод 3: Spring DataSource
        Map<String, String> springDataSourceInfo = getConnectionViaSpringDataSource(allErrors);
        springDataSourceInfo.put("Способ подключения", "Метод 3: Spring DataSource");
        allConnections.add(springDataSourceInfo);

        model.addAttribute("allConnections", allConnections);
        model.addAttribute("allErrors", allErrors);
        model.addAttribute("requestTime", requestTime);

        return "lab1/connection-all";
    }

    private Map<String, String> getConnectionViaDriverManager(List<String> errors) {
        Map<String, String> info = new LinkedHashMap<>();
        Connection conn = null;
        try {
            Class.forName(dbDriver);
            conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            DatabaseMetaData metaData = conn.getMetaData();

            info.put("URL", metaData.getURL());
            info.put("Пользователь", metaData.getUserName());
            info.put("Драйвер", metaData.getDriverName() + " " + metaData.getDriverVersion());
            info.put("БД", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());

            conn.close();
        } catch (Exception e) {
            errors.add("DriverManager: " + e.getMessage());
            info.put("Статус", "Ошибка: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    errors.add("Ошибка закрытия: " + e.getMessage());
                }
            }
        }
        return info;
    }

    private Map<String, String> getConnectionViaPGSimpleDataSource(List<String> errors) {
        Map<String, String> info = new LinkedHashMap<>();
        try {
            org.postgresql.ds.PGSimpleDataSource pgDataSource = new org.postgresql.ds.PGSimpleDataSource();
            pgDataSource.setURL(dbUrl);
            pgDataSource.setUser(dbUsername);
            pgDataSource.setPassword(dbPassword);

            Connection connection = pgDataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            info.put("URL", metaData.getURL());
            info.put("Пользователь", metaData.getUserName());
            info.put("Драйвер", metaData.getDriverName() + " " + metaData.getDriverVersion());
            info.put("БД", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            info.put("Хост", pgDataSource.getServerNames()[0]);
            info.put("Порт", String.valueOf(pgDataSource.getPortNumbers()[0]));

            connection.close();
        } catch (SQLException e) {
            errors.add("PGSimpleDataSource: " + e.getMessage());
            info.put("Статус", "Ошибка: " + e.getMessage());
        }
        return info;
    }

    private Map<String, String> getConnectionViaSpringDataSource(List<String> errors) {
        Map<String, String> info = new LinkedHashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            info.put("URL", metaData.getURL());
            info.put("Пользователь", metaData.getUserName());
            info.put("Драйвер", metaData.getDriverName() + " " + metaData.getDriverVersion());
            info.put("БД", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());

            connection.close();
        } catch (SQLException e) {
            errors.add("Spring DataSource: " + e.getMessage());
            info.put("Статус", "Ошибка: " + e.getMessage());
        }
        return info;
    }

    private String getIsolationLevel(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "Нет транзакций";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "Read Uncommitted";
            case Connection.TRANSACTION_READ_COMMITTED -> "Read Committed";
            case Connection.TRANSACTION_REPEATABLE_READ -> "Repeatable Read";
            case Connection.TRANSACTION_SERIALIZABLE -> "Serializable";
            default -> "Неизвестно (" + level + ")";
        };
    }
}
