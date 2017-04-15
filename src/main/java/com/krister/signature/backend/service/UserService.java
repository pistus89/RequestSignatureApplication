package com.krister.signature.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.krister.signature.backend.entity.User;
import com.krister.signature.backend.repositories.UserRepository;

@Scope("prototype")
@Service("UserService")
public class UserService {
	@Autowired
	private UserRepository userRepo;
	
	public User findByUsername(String username) {
		return userRepo.findByUsername(username);
	}
}
