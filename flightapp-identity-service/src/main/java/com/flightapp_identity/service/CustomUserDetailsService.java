package com.flightapp_identity.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.flightapp_identity.model.CustomUserDetails;
import com.flightapp_identity.model.UserCredential;
import com.flightapp_identity.repository.UserCredentialRepository;

public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserCredentialRepository userCredRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserCredential> credential = userCredRepo.findByName(username);
		return credential.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("user not found with name :" + username));
	}

}
