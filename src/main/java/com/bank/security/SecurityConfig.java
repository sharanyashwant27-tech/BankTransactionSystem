package com.bank.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final BankInvalidSessionStrategy invalidSessionStrategy;
    private final LoginRedirectHandlers loginRedirectHandlers;
    private final SensitiveQueryParamFilter sensitiveQueryParamFilter;

    public SecurityConfig(BankInvalidSessionStrategy invalidSessionStrategy,
                          LoginRedirectHandlers loginRedirectHandlers,
                          SensitiveQueryParamFilter sensitiveQueryParamFilter) {
        this.invalidSessionStrategy = invalidSessionStrategy;
        this.loginRedirectHandlers = loginRedirectHandlers;
        this.sensitiveQueryParamFilter = sensitiveQueryParamFilter;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http
                .addFilterBefore(sensitiveQueryParamFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/return-to-login", "/session-expired", "/css/**", "/images/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(loginRedirectHandlers)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(loginRedirectHandlers)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionStrategy(invalidSessionStrategy)
                )
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .cacheControl(cache -> {})
                )
                .build();
    }
}
