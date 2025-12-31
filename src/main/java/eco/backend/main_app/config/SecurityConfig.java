package eco.backend.main_app.config;

import eco.backend.main_app.core.security.JwtAuthenticationFilter;
import eco.backend.main_app.core.security.CustomAccessDeniedHandler;
import eco.backend.main_app.feature.auth.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UserService userService; // Inject UserService
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(UserService userService,
                          JwtAuthenticationFilter jwtAuthFilter,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.userService = userService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deaktivieren (Cross-Site Request Forgery), da JWT genutzt wird
            .csrf(AbstractHttpConfigurer::disable)

            // Routen konfigurieren
            .authorizeHttpRequests(auth -> auth
                    // WICHTIG: Registrierung muss für JEDEN offen sein (permitAll)
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/config/**").authenticated()
                    // Alles andere braucht eine Authentifizierung
                    .anyRequest().authenticated()
            )

            // Spezifische Fehlerbehandlung
            .exceptionHandling(exceptions -> exceptions
                            .accessDeniedHandler(accessDeniedHandler)
            )

            // Session auf Stateless setzen, da JWT genutzt wird
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authenticationProvider(authenticationProvider())
            // WICHTIG: Filter VOR dem Standard-UsernamePasswordAuthenticationFilter einfügen
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
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
