package com.gmonitor.storage.repository;

import com.gmonitor.storage.entity.configuration.UserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<UserTokenEntity, Long> {

    @Query(value = """
    select t from UserTokenEntity t inner join UserEntity u on t.user.id = u.id
        where u.id =:userId and (t.expired = false or t.revoked = false )
    """)
    List<UserTokenEntity> findAllValidTokensByUser(@Param("userId") Long userId);

    Optional<UserTokenEntity> findByToken(String token);
}