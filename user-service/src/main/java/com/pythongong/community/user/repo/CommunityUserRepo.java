package com.pythongong.community.user.repo;

import com.pythongong.community.user.model.CommunityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityUserRepo extends JpaRepository<CommunityUser, Long> {

    /**
     * Counts the number of users with the given username.
     * Spring Data JPA will generate the implementation for this method.
     *
     * @param userName The username to count.
     * @return The number of users with the given username.
     */
    long countByUserName(String userName);

    /**
     * Finds a user by username.
     *
     * @param userName The username to find.
     * @return An Optional containing the CommunityUser if found, otherwise empty.
     */
    Optional<CommunityUser> findByUserName(String userName);
}