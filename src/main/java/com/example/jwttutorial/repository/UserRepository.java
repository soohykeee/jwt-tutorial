package com.example.jwttutorial.repository;


import com.example.jwttutorial.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /** @EntityGraph 는 쿼리가 수행이 될때 Lazy가 아닌 Eager조회로, authorities 정보를 같이 가져온다. */
    @EntityGraph(attributePaths = "authorities")
    /** username을 기준으로 user정보를 가져올 때 권한 정보고 같이 가져오는 메소드 */
    Optional<User> findOneWithAuthoritiesByUsername(String username);
}
