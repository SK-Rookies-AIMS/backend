package com.aims.backend.mapper;

import com.aims.backend.dto.test.TestResponse;
import com.aims.backend.repository.TestRepository;

public final class TestMapper {

    private TestMapper() {
    }

    public static TestResponse.TestUserDTO toUserDTO(TestRepository.TestUser user) {
        return TestResponse.TestUserDTO.builder()
                .id(user.id())
                .email(user.email())
                .build();
    }
}
