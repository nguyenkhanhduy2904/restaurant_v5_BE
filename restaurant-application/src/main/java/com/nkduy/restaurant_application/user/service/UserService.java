package com.nkduy.restaurant_application.user.service;

import com.nkduy.restaurant_application.image.entity.Upload;
import com.nkduy.restaurant_application.image.entity.UploadStatus;
import com.nkduy.restaurant_application.image.entity.UserAvatar;
import com.nkduy.restaurant_application.image.repository.UserAvatarRepository;
import com.nkduy.restaurant_application.image.service.StorageUrlService;
import com.nkduy.restaurant_application.image.service.UploadImageService;
import com.nkduy.restaurant_application.mapper.UserMapper;
import com.nkduy.restaurant_application.user.dto.request.AvatarChangeRequest;
import com.nkduy.restaurant_application.user.dto.request.UpdateProfileRequest;
import com.nkduy.restaurant_application.user.dto.response.AvatarChangeResponse;
import com.nkduy.restaurant_application.user.dto.response.UserInfoResponse;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UploadImageService uploadImageService;
    private final UserAvatarRepository userAvatarRepository;
    private final StorageUrlService storageUrlService;
    private final UserImageService userImageService;


    public UserInfoResponse getMe(String id) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toUserInfoResponse(user, storageUrlService.getPublicUrl(userImageService.getUserAvatarUrl(user.getId())));
    }

    @Transactional
    public UserInfoResponse editProfile(String id, UpdateProfileRequest request) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.displayName()!=null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName());
        }
        if(request.phone()!=null && !request.phone().isBlank()){
            user.setPhone(request.phone());
        }
        if (request.gender()!=null) {
            user.setGender(request.gender());
        }
        String avatarUrl = userImageService.getUserAvatarUrl(id);
        return userMapper.toUserInfoResponse(userRepository.save(user), storageUrlService.getPublicUrl(userImageService.getUserAvatarUrl(user.getId())));
    }

    @Transactional
    public AvatarChangeResponse changeAvatar(String id, AvatarChangeRequest request) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Upload> upload = uploadImageService.getUploads(List.of(request.uploadId()));

        UserAvatar userAvatar = userAvatarRepository.findByUserId(user.getId()).orElse(UserAvatar.builder().user(user).build());
        userAvatar.setUrl(upload.getFirst().getObjectKey());

        UserAvatar saved = userAvatarRepository.save(userAvatar);
        // Change upload status
        uploadImageService.changeUploadStatus(upload.getFirst(), UploadStatus.ATTACHED);

        return new AvatarChangeResponse(
                "Avatar changed successfully",
                storageUrlService.getPublicUrl(saved.getUrl())
        );

    }
}
