package com.pythongong.community.user.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pythongong.community.user.domain.CommunityUser;

public interface CommunityUserRepo extends ReactiveCrudRepository<CommunityUser, Integer>, CustomCommunityUserRepo {

}
