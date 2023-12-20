package com.gmonitor.converter.configuration;

import com.gmonitor.model.configuration.User;
import com.gmonitor.model.configuration.UserRole;
import com.gmonitor.storage.entity.configuration.UserEntity;
import org.springframework.core.convert.converter.Converter;

public class UserConverter implements Converter<UserEntity, User> {

    @Override
    public User convert(UserEntity source) {
        return  User.builder()
                .active(source.isActivated())
                .username(source.getLogin())
                .password(source.getPassword())
                .roles(source.getRoles().stream().map(r -> UserRole.valueOf(r.getRole())).toList())
                .build();
    }
}
