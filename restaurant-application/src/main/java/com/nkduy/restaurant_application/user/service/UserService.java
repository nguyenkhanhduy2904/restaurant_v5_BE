package com.nkduy.restaurant_application.user.service;

import com.nkduy.restaurant_application.mapper.UserMapper;
import com.nkduy.restaurant_application.user.dto.request.UpdateProfileRequest;
import com.nkduy.restaurant_application.user.dto.response.UserInfoResponse;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public UserInfoResponse getMe(String id) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toUserInfoResponse(user);
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
        return userMapper.toUserInfoResponse(userRepository.save(user));
    }
}
