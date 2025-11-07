package com.scrooge.security;

import com.scrooge.model.User;
import com.scrooge.model.enums.Role;
import com.scrooge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauthUser = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());

        if ("github".equals(registrationId)) {
            String token = userRequest.getAccessToken().getTokenValue();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                response.getBody().stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                        .findFirst()
                        .ifPresent(emailData -> attributes.put("email", emailData.get("email")));
            }
        }

        String email = (String) attributes.get("email");
        String name = (String) attributes.getOrDefault("name", attributes.get("login"));

        User user = userRepository.findByEmail(email).orElseGet(() -> {

            User newUser = User.builder()
                    .email(email)
                    .firstName(name)
                    .password("OAUTH2_USER")
                    .role(Role.USER)
                    .active(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            return userRepository.save(newUser);
        });

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes,
                "id"
        );
    }
}
