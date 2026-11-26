package com.nkduy.restaurant_application.user.service;

import com.nkduy.restaurant_application.image.entity.UserAvatar;
import com.nkduy.restaurant_application.image.repository.UserAvatarRepository;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserImageService {
    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;



    public String getUserAvatarUrl(String userId) {
        UserAvatar userAvatar = userAvatarRepository.findById(userId).orElse(null);

        if (userAvatar != null) {
            return userAvatar.getUrl();
        }
        else {
            return null;
        }
    }
}
