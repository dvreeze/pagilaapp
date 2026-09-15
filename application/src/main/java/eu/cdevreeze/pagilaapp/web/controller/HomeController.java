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

package eu.cdevreeze.pagilaapp.web.controller;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * Web MVC controller for the home page.
 *
 * @author Chris de Vreeze
 */
@Controller
public class HomeController {

    @GetMapping(value = "/")
    public String home(
            Model model,
            @Nullable @AuthenticationPrincipal OidcUser principal
    ) {
        String fullName = Optional.ofNullable(principal)
                .map(OidcUser::getUserInfo)
                .map(OidcUserInfo::getFullName)
                .orElse("");
        model.addAttribute("fullName", fullName);

        boolean isAuthenticated = principal != null;
        model.addAttribute("isAuthenticated", isAuthenticated);

        return "index";
    }
}
