package com.krister.signature.backend.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class RSAPemKeyService {
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Cipher cipher;
	private String privateKeyPath;
	private String publicKeyPath;
	private String privateKeyName;
	private String publicKeyName;
	private String cipherInstance;
	private  final String PROVIDER = "BC";
	private final int KEY_SIZE=2048;
	
	public RSAPemKeyService(@Value("${privatekey.pem.path}") String privateKeyPath, @Value("${publickey.pem.path}") String publicKeyPath,
			@Value("${privatekey.pem.name}") String privateKeyName, @Value("${publickey.pem.name}") String publicKeyName,
			@Value("${encryption.pem.cipher}") String cipher) {
		this.privateKeyPath = privateKeyPath;
		this.publicKeyPath = publicKeyPath;
		this.privateKeyName = privateKeyName;
		this.publicKeyName = publicKeyName;
		this.cipherInstance = cipher;
		
		//adds bouncycastle as one of the supported providers
		Security.addProvider(new BouncyCastleProvider());
		
		try {
			boolean isInitialized = initializePemKeys(privateKeyPath,privateKeyName,publicKeyPath,publicKeyName);
			if (!isInitialized) {
				createNewPemKeyPair(privateKeyPath,privateKeyName,publicKeyPath,publicKeyName);
			}
		} catch(Exception e) {
			
		}
	}
	
	private boolean initializePemKeys(String privateKeyPath, String privateKeyName, String publicKeyPath, String publicKeyName) throws IOException, GeneralSecurityException {
		File publicKeyFile = new File(publicKeyPath + publicKeyName);
		File privateKeyFile = new File(privateKeyPath + privateKeyName);
		
		if (!publicKeyFile.exists() || !privateKeyFile.exists()) {
			return false;
		}
		
		this.privateKey = readPrivateKey(privateKeyFile);
		this.publicKey = readPublicKey(publicKeyFile);
		
		return (this.privateKey != null && this.publicKey != null);
	}
	
	private void createNewPemKeyPair(String privateKeyPath, String privateKeyName, String publicKeyPath, String publicKeyName) throws NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(cipherInstance, PROVIDER);
		generator.initialize(KEY_SIZE);
		
		KeyPair keyPair = generator.generateKeyPair();
		
		writePemKey(this.privateKeyPath+this.publicKeyName,"RSA PRIVATE KEY",keyPair.getPrivate());
		writePemKey(this.publicKeyPath+this.publicKeyName, "RSA PUBLIC KEY", keyPair.getPublic());
	}
	
	private void writePemKey(String path,String description,Key key) throws FileNotFoundException, IOException {
		PemObject po = new PemObject(description,key.getEncoded());
		PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(path)));
		try {
			pemWriter.writeObject(po);
		} finally {
			pemWriter.close();
		}
	}
	
	private PrivateKey readPrivateKey(File pemFile) throws GeneralSecurityException, IOException {
		PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(pemFile)));
		
		PemObject pem = null;
		try {
			pem = pemReader.readPemObject();
		} finally {
			pemReader.close();
		}
		
		if (pem == null) {
			return null;
		}
		byte[] content= pem.getContent();
		PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
		KeyFactory kf = KeyFactory.getInstance(cipherInstance,PROVIDER);
		return kf.generatePrivate(privKeySpec);
	}
	
	private PublicKey readPublicKey(File pemFile) throws GeneralSecurityException, FileNotFoundException {
		PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(pemFile)));
		
		PemObject pem = null;
		
		if (pem == null) {
			return null;
		}
		byte[] content = pem.getContent();
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(content);
		KeyFactory kf = KeyFactory.getInstance(cipherInstance,PROVIDER);
		return kf.generatePublic(keySpec);
	}
	
	public byte[] decryptMessageWithPEM(byte[] message) throws IOException, InvalidCipherTextException {
		AsymmetricKeyParameter privKey = PrivateKeyFactory.createKey(privateKey.getEncoded());
		AsymmetricBlockCipher engine = new RSAEngine();
		engine.init(false, privKey); //false for decryption
		byte[] hexEncodedCipher = engine.processBlock(message, 0, message.length);
		return hexEncodedCipher;
	}
	
}
