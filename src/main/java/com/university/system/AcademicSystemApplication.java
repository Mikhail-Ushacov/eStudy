package com.university.system;

import com.university.system.model.User;
import com.university.system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class AcademicSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserRepository repository) {
        return (args) -> {
            System.out.println("-------------------------------");
            System.out.println("STARTUP CHECK: LISTING ALL USERS IN DB:");
            List<User> users = repository.findAll();
            if (users.isEmpty()) {
                System.out.println("!!! WARNING: DATABASE IS EMPTY !!!");
            } else {
                for (User u : users) {
                    System.out.println("User Found: ID=" + u.getId() + ", Username=[" + u.getUsername() + "], Role=" + u.getRole());
                }
            }
            System.out.println("-------------------------------");
        };
    }
}