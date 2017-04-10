package com.krister.signature.backend.service;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

import com.krister.signature.backend.entity.User;

@Service
public class CryptService {
	
	public byte[] generateHmacSignature(User user,byte[] payload) throws GeneralSecurityException, SQLException {
		Blob key = user.getSignatureKey();
		//try to find a way to define use algorithm via application.properties file
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(1, (int)key.length()),"HmacSHA512");
		Mac mac = Mac.getInstance("HmacSHA512");
		mac.init(secretKeySpec);
		byte[] result = mac.doFinal(payload);
		//hex encoding is needed to transform result from binary into readable form
		return Hex.encode(result);
	}
	
	public boolean verifyHmacSignature(String signature,String payload,User user) {
		try {
			byte[] localGenerated = generateHmacSignature(user,payload.getBytes());
			String generatedSignature = new String(localGenerated);
			generatedSignature.toUpperCase();
			signature.toUpperCase();
			
			return generatedSignature.equals(signature);
			
		} catch (GeneralSecurityException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
