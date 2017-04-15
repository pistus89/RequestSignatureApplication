package com.krister.signature.backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	//add here a normal username-password authentication when SignatureFilter is ready
    	http
    	.addFilterAfter(new SignatureFilter(), SecurityContextPersistenceFilter.class)
    	.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
    }
}