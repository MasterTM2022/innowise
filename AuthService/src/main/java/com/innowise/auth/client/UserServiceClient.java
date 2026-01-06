package com.innowise.auth.client;

import com.innowise.auth.dto.CreateUserRequest;
import com.innowise.auth.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8081}")
public interface UserServiceClient {
    @PostMapping("/api/v1/users")
    UserDto createUserProfile(@RequestBody CreateUserRequest request);
}
