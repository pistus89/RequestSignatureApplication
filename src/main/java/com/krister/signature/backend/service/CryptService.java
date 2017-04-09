package com.krister.signature.backend.service;

import org.springframework.stereotype.Service;

import com.krister.signature.backend.entity.User;

@Service
public class CryptService {
	public boolean verifyHmacSignature(String signature,String payload,User user) {
		//Todo
		return false;
	}
}
