package com.bulletin.config;

import com.bulletin.security.JwtAccessDeniedHandler;
import com.bulletin.security.JwtAuthenticationEntryPoint;
import com.bulletin.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final Environment environment;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean localProfile = environment.acceptsProfiles(Profiles.of("local"));

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                        .accessDeniedHandler(jwtAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/health").permitAll();
                    auth.requestMatchers(
                            "/auth/status",
                            "/auth/token",
                            "/auth/mot-de-passe-oublie",
                            "/auth/reinitialiser-mot-de-passe",
                            "/auth/init-admin"
                    ).permitAll();
                    if (localProfile) {
                        auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll();
                        auth.requestMatchers("/h2-console/**").permitAll();
                        auth.requestMatchers("/debug/**").permitAll();
                    } else {
                        // La page Swagger UI (HTML/JS statique) est publique : elle ne contient aucune donnée
                        // sensible et permet à l'utilisateur de s'authentifier via le bouton "Authorize".
                        auth.requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll();
                        // La doc OpenAPI est lisible publiquement (elle décrit les endpoints, pas les données).
                        // Les appels API réels restent protégés par anyRequest().authenticated() ci-dessous :
                        // chaque endpoint exige un JWT valide (et les permissions associées).
                        auth.requestMatchers("/v3/api-docs/**").permitAll();
                        auth.requestMatchers("/debug/**").authenticated();
                    }
                    auth.anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());

        boolean localProfile = environment.acceptsProfiles(Profiles.of("local"));
        if (origins.isEmpty() || origins.contains("*")) {
            if (!localProfile) {
                log.warn("CORS_ALLOWED_ORIGINS is not configured or set to wildcard; using default production origins.");
                configuration.setAllowedOriginPatterns(List.of(
                        "https://app.gestbulletin.com",
                        "https://www.gestbulletin.com",
                        "https://scolanote.vercel.app",
                        "https://*.vercel.app"
                ));
            } else {
                configuration.addAllowedOriginPattern("*");
            }
        } else {
            
            if (!origins.contains("https://www.gestbulletin.com")) {
                origins.add("https://www.gestbulletin.com");
                log.info("CORS: added missing origin https://www.gestbulletin.com to allowed patterns");
            }
            configuration.setAllowedOriginPatterns(origins);
            log.info("CORS configured with allowed origin patterns: {}", origins);
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
