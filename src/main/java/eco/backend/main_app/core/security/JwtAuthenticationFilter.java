package eco.backend.main_app.core.security;

import eco.backend.main_app.feature.auth.UserService; // Deine Service-Klasse
import eco.backend.main_app.feature.auth.model.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization-Header auslesen
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Prüfung: Ist der Header vorhanden und startet er mit "Bearer "?
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Weiterleiten ohne Auth (für Login/Register wichtig)
            return;
        }
        // 3. Token extrahieren (alles nach "Bearer ")
        jwt = authHeader.substring(7);

        // Username aus dem Token holen
        username = jwtService.extractUsername(jwt);

        // 4. Validierungsprozess
        // Falls ein Username vorhanden UND aktuell noch niemand im SecurityContext authentifiziert ist
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // User aus der Datenbank laden
                UserDetails userDetails = this.userService.loadUserByUsername(username);

                if (userDetails instanceof UserEntity userEntity) {

                    // 3. Validierung mit dem ECHTEN Datenbank-Objekt
                    if (jwtService.isTokenValid(jwt, userEntity)) {

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } else {
                    logger.warn("Loaded user is not an instance of UserEntity!");
                }
            }

        // Kette fortsetzen
        filterChain.doFilter(request, response);
    }
}
