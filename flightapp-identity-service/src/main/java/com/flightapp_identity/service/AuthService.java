package com.flightapp_identity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp_identity.model.UserCredential;
import com.flightapp_identity.repository.UserCredentialRepository;

@Service
public class AuthService {
	
	@Autowired
    private UserCredentialRepository userCredRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    
    public String saveUser(UserCredential creds) {
    	creds.setPassword(passwordEncoder.encode(creds.getPassword()));
    	userCredRepo.save(creds);
    	return "User Added to the system";
    }
    
    public String generateToken(String username) {
    	return jwtService.generateToken(username);
    }
    
    public void validateToken(String token) {
    	jwtService.validateToken(token);
    }
}
