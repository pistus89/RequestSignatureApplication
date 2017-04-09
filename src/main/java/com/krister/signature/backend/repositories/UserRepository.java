package com.krister.signature.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krister.signature.backend.entity.User;

public interface UserRepository extends JpaRepository<User,Long>{
	User findByUsername(String username);
}
