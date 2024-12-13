package org.projects.eBankati.security.config;

import lombok.RequiredArgsConstructor;
import org.projects.eBankati.exceptions.SecurityExceptionHandler;
import org.projects.eBankati.security.filter.JwtAuthenticationFilter;
import org.projects.eBankati.services.impl.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers("/error", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // Endpoints des comptes bancaires
                        .requestMatchers("/api/accounts/create").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/accounts/{accountId}").hasAnyRole("USER", "ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/accounts/all").hasRole("ADMIN")

                        // Endpoints des transactions
                        .requestMatchers("/api/transactions/create").hasAnyRole("USER", "EMPLOYEE")
                        .requestMatchers("/api/transactions/{transactionId}").hasAnyRole("USER", "ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/transactions/all").hasRole("ADMIN")

                        // Endpoints des prêts
                        .requestMatchers("/api/loans/apply").hasRole("USER")
                        .requestMatchers("/api/loans/approve/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/loans/all").hasRole("ADMIN")

                        // Gestion des utilisateurs
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // Tout autre endpoint nécessite une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityExceptionHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}