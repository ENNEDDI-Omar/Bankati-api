// seeders/UserSeeder.java
package org.projects.eBankati.seeders;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.projects.eBankati.domain.entities.Role;
import org.projects.eBankati.domain.entities.User;
import org.projects.eBankati.repositories.RoleRepository;
import org.projects.eBankati.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Vérifier si les données existent déjà
        if (userRepository.count() == 0) {
            seedUsers();
        }
    }

    private void seedUsers() {
        // Créer le rôle ADMIN s'il n'existe pas
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        // Créer le rôle USER s'il n'existe pas
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("USER");
                    return roleRepository.save(role);
                });

        // Créer le rôle EMPLOYEE s'il n'existe pas
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("EMPLOYEE");
                    return roleRepository.save(role);
                });

        // Créer deux utilisateurs admin
        User admin1 = new User();
        admin1.setName("Admin One");
        admin1.setEmail("admin1@ebankati.com");
        admin1.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        admin1.setAge(30);
        admin1.setMonthlyIncome(10000.0);
        admin1.setCreditScore(800);
        admin1.setRole(adminRole);
        userRepository.save(admin1);

        User admin2 = new User();
        admin2.setName("Admin Two");
        admin2.setEmail("admin2@ebankati.com");
        admin2.setPassword(BCrypt.hashpw("admin456", BCrypt.gensalt()));
        admin2.setAge(35);
        admin2.setMonthlyIncome(12000.0);
        admin2.setCreditScore(850);
        admin2.setRole(adminRole);
        userRepository.save(admin2);

        // Créer deux utilisateurs normaux
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@ebankati.com");
        user1.setPassword(BCrypt.hashpw("user123", BCrypt.gensalt()));
        user1.setAge(25);
        user1.setMonthlyIncome(5000.0);
        user1.setCreditScore(700);
        user1.setRole(userRole);
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@ebankati.com");
        user2.setPassword(BCrypt.hashpw("user456", BCrypt.gensalt()));
        user2.setAge(28);
        user2.setMonthlyIncome(6000.0);
        user2.setCreditScore(750);
        user2.setRole(userRole);
        userRepository.save(user2);

        // Créer deux employées de la banque
        User emp1 = new User();
        emp1.setName("Employee One");
        emp1.setEmail("emp1@ebankati.com");
        emp1.setPassword(BCrypt.hashpw("emp123", BCrypt.gensalt()));
        emp1.setAge(25);
        emp1.setMonthlyIncome(5000.0);
        emp1.setCreditScore(700);
        emp1.setRole(employeeRole);
        userRepository.save(emp1);
    }
}