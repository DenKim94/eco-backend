package eco.backend.main_app.config;

import eco.backend.main_app.core.security.JwtAuthenticationFilter;
import eco.backend.main_app.core.security.CustomAccessDeniedHandler;
import eco.backend.main_app.feature.auth.UserService;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UserService userService; // Inject UserService
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF deaktivieren (Cross-Site Request Forgery), da JWT genutzt wird
            .csrf(AbstractHttpConfigurer::disable)

            // Routen konfigurieren
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/auth/refresh-token",
                            "/api/auth/logout",
                            "/api/auth/delete-account",
                            "/api/auth/verify-email",
                            "/api/auth/resend-email",
                            "/api/auth/user/get-info"
                    ).authenticated()

                    // WICHTIG: Registrierung muss für JEDEN offen sein (permitAll)
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/config/**").authenticated()
                    .requestMatchers("/api/tracking/**").authenticated()
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Erlaubte Ursprünge (Frontend-URLs) setzen
        configuration.setAllowedOrigins(allowedOrigins);

        // Erlaubte HTTP-Methoden (OPTIONS ist zwingend erforderlich für Preflight-Requests)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Erlaubte Header (Authorization für das JWT, Content-Type für JSON-Bodys)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // WICHTIG: Wenn AllowCredentials auf 'true' steht, darf allowedOrigins nicht "*" sein!
        configuration.setAllowCredentials(true);

        // Die Konfiguration auf alle Endpunkte (/**) anwenden
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
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
