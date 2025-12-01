package eco.backend.main_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF deaktivieren (für REST-APIs üblich, da wir JWT nutzen werden)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Routen konfigurieren
                .authorizeHttpRequests(auth -> auth
                        // WICHTIG: Registrierung muss für JEDEN offen sein (permitAll)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Alles andere braucht später Authentifizierung
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
