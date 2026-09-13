/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.pagilaapp.web.security;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security configuration.
 * <p>
 * See <a href="https://docs.spring.io/spring-security/reference/index.html">Spring Security</a>.
 * <p>
 * Also see <a href="https://docs.spring.io/spring-security/reference/servlet/architecture.html">Spring Security Architecture</a> and
 * <a href="https://docs.spring.io/spring-security/reference/servlet/getting-started.html">Spring Security (Servlets)</a>.
 * <p>
 * Also see <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html">Spring Security OAuth 2.0</a>.
 *
 * @author Chris de Vreeze
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    private final String clientId;
    private final String logoutUrl;

    public SecurityConfig(
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
            String clientId,
            @Value("${spring.security.oauth2.client.provider.myrealm.issuer-uri}/protocol/openid-connect/logout")
            String logoutUrl
    ) {
        this.clientId = clientId;
        this.logoutUrl = logoutUrl;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        // See https://docs.spring.io/spring-security/reference/servlet/architecture.html#servlet-securityfilterchain
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(withDefaults())
                .logout(logout -> logout
                        .addLogoutHandler(logoutHandler()));
        return http.build();
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
                Preconditions.checkArgument(authentication instanceof OAuth2AuthenticationToken);
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                Preconditions.checkArgument(token.getPrincipal() instanceof OidcUser);
                OidcUser user = (OidcUser) token.getPrincipal();

                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                // See https://forum.keycloak.org/t/how-is-logout-really-working-and-can-we-bypass-the-logout-confirm-page/15314
                // Also see https://gpiskas.com/posts/id-token-access-token-spring-boot-spring-cloud-azure/
                String redirectUri = UriComponentsBuilder.fromUriString(logoutUrl)
                        .queryParam("client_id", clientId)
                        .queryParam("id_token_hint", user.getIdToken().getTokenValue())
                        .queryParam("post_logout_redirect_uri", baseUrl)
                        .build()
                        .toUriString();
                response.sendRedirect(redirectUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
