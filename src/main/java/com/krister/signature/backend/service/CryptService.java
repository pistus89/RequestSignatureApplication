package com.krister.signature.backend.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.krister.signature.backend.entity.User;

@Scope("prototype")
@Service("CryptService")
public class CryptService {
	
	@Autowired
	private RSAPemKeyService keyService;
	
	private static final String HASH_ALG= "HmacSHA512";
	private static final int BLOB_START = 1;

	public byte[] generateHmacSignature(User user, byte[] payload) throws GeneralSecurityException, SQLException {
		Blob key = user.getSignatureKey();
		// try to find a way to define use algorithm via application.properties
		// file
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(BLOB_START, (int) key.length()), HASH_ALG);
		Mac mac = Mac.getInstance(HASH_ALG);
		mac.init(secretKeySpec);
		byte[] result = mac.doFinal(payload);
		// hex encoding is needed to transform result from binary into readable
		// form
		return Hex.encode(result);
	}

	public boolean verifyHmacSignature(String signature, String payload, User user) {
		try {
			byte[] localGenerated = generateHmacSignature(user, payload.getBytes());
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
	
	public String decryptPemEncryptedSecret(String secret) throws InvalidCipherTextException, IOException {
		return new String(keyService.decryptMessageWithPEM(secret.getBytes()));
	}
}
