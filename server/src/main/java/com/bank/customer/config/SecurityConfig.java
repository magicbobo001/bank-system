package com.bank.customer.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bank.customer.repository.UserRepository;
import com.bank.customer.repository.UserRoleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        // 添加CORS配置Bean
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                // 允许的源（增加OPTIONS方法支持）
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                // 允许的HTTP方法（添加OPTIONS）
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                // 允许的请求头（保持与前端一致）
                config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
                // 暴露的响应头（保持原样）
                config.setExposedHeaders(List.of("Authorization"));
                // 启用凭据
                config.setAllowCredentials(true);
                // 预检请求缓存时间（单位：秒）
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception { // 添加过滤器参数
                http
                                .cors(Customizer.withDefaults());
                http
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                                                        @Override
                                                        public void commence(HttpServletRequest request,
                                                                        HttpServletResponse response,
                                                                        AuthenticationException authException)
                                                                        throws IOException {
                                                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                                response.setContentType(MediaType.APPLICATION_JSON_VALUE
                                                                                + ";charset=UTF-8");
                                                                response.getWriter().write(
                                                                                "{\"error\":\"认证失败\",\"message\":\"用户名或密码错误\"}");
                                                        }
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE
                                                                        + ";charset=UTF-8");
                                                        response.getWriter().write(
                                                                        "{\"error\":\"权限不足\",\"message\":\"您没有执行该操作的权限\"}");
                                                }));
                http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

                // 在securityFilterChain配置中添加响应头设置
                http.headers(headers -> headers
                                .contentTypeOptions(Customizer.withDefaults())
                                .cacheControl(Customizer.withDefaults())
                                .httpStrictTransportSecurity(Customizer.withDefaults()));
                http
                                .csrf(csrf -> csrf.disable()) // 添加CSRF禁用配置
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http
                                .authorizeHttpRequests(auth -> auth

                                                // 公共接口
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // 用户相关接口
                                                .requestMatchers(HttpMethod.GET, "/api/users/me")
                                                .hasAnyRole("USER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/update").hasRole("ADMIN")

                                                // 账户相关接口
                                                .requestMatchers(HttpMethod.GET, "/api/accounts/my-accounts")
                                                .hasRole("USER")
                                                .requestMatchers("/api/accounts/admin/**").hasRole("ADMIN")

                                                // 贷款接口
                                                .requestMatchers(HttpMethod.POST, "/api/loans/apply").hasRole("USER")
                                                .requestMatchers(HttpMethod.POST, "/api/loans/*/repay").hasRole("ADMIN")

                                                // 交易记录
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/transactions/{accountId}/history")
                                                .hasRole("USER")
                                                .requestMatchers("/api/transactions/**").hasRole("ADMIN")

                                                // 管理员专属接口
                                                .requestMatchers("/api/users").hasRole("ADMIN")
                                                .requestMatchers("/api/loans/approve/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/change-password")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())
                                .httpBasic(Customizer.withDefaults());
                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository,
                        UserRoleRepository userRoleRepository) {
                return username -> userRepository.findByUsername(username)
                                .map(user -> {
                                        List<SimpleGrantedAuthority> authorities = userRoleRepository
                                                        .findByUserId(user.getUserId())
                                                        .stream()
                                                        .map(ur -> new SimpleGrantedAuthority(
                                                                        "ROLE_" + ur.getRole().getRoleName()
                                                                                        .toUpperCase()))
                                                        .collect(Collectors.toList());

                                        return new org.springframework.security.core.userdetails.User(
                                                        user.getUsername(),
                                                        user.getPasswordHash(),
                                                        authorities);
                                })
                                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        }

}