package com.pythongong.community.user.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pythongong.community.user.model.CommunityUser;

public interface CommunityUserRepo extends ReactiveCrudRepository<CommunityUser, Integer>, CustomCommunityUserRepo {

}
