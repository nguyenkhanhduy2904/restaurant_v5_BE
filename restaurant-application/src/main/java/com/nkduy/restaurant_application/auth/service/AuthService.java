package com.nkduy.restaurant_application.auth.service;

import com.nkduy.restaurant_application.auth.dto.request.LoginRequest;
import com.nkduy.restaurant_application.auth.dto.request.RefreshTokenRequest;
import com.nkduy.restaurant_application.auth.dto.request.RegisterRequest;
import com.nkduy.restaurant_application.auth.dto.response.AuthResponse;
import com.nkduy.restaurant_application.auth.entity.LocalCredential;
import com.nkduy.restaurant_application.auth.repository.LocalCredentialRepository;
import com.nkduy.restaurant_application.image.service.StorageUrlService;
import com.nkduy.restaurant_application.mapper.UserMapper;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import com.nkduy.restaurant_application.user.service.UserImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final LocalCredentialRepository localCredentialRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshedTokenService;
    private final UserImageService userImageService;
    private final StorageUrlService storageUrlService;


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if(localCredentialRepository.existsByEmail(request.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        User newUser = createAccount(request.displayName(), request.phone(), request.gender(), "CUSTOMER");

        LocalCredential localCredential = LocalCredential.builder()
                .email(request.email())
                .passwordHashed(passwordEncoder.encode(request.password()))
                .user(newUser)
                .build();

        localCredentialRepository.save(localCredential);

        return toAuthResponse(newUser, null);
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        if(localCredentialRepository.existsByEmail(request.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        User newUser = createAccount(request.displayName(), request.phone(), request.gender(), "ADMIN");

        LocalCredential localCredential = LocalCredential.builder()
                .email(request.email())
                .passwordHashed(passwordEncoder.encode(request.password()))
                .user(newUser)
                .build();
        localCredentialRepository.save(localCredential);

        return toAuthResponse(newUser, null);
    }



    public User createAccount(String displayName, String phone, String gender, String role){

        User user = User.builder()
                .displayName(displayName)
                .role(role)
                .phone(phone)
                .gender(gender)
                .build();
        return userRepository.save(user);

    }


    public AuthResponse toAuthResponse(User user, String avatarUrl){
        return new AuthResponse(
                jwtService.generateToken(user),
                refreshedTokenService.issue(user),
                userMapper.toUserInfoResponse(user, avatarUrl)
        );
    }

    public AuthResponse login(LoginRequest request) {
        LocalCredential localCredential = localCredentialRepository.findByEmail(request.email()).orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), localCredential.getPasswordHashed())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        User user = localCredential.getUser();
        if(!"ACTIVE".equals(user.getStatus())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled");
        }


        return toAuthResponse(user, storageUrlService.getPublicUrl(userImageService.getUserAvatarUrl(user.getId())) );
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = refreshedTokenService.validateAndConsume(request.refreshToken());
        return toAuthResponse(user, storageUrlService.getPublicUrl(userImageService.getUserAvatarUrl(user.getId())));
    }

    public Void logOut(RefreshTokenRequest request) {
        refreshedTokenService.revokeIfPresent(request.refreshToken());
        return null;
    }
}
