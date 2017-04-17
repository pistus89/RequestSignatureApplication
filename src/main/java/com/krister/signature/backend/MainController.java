package com.krister.signature.backend;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.krister.signature.backend.beans.ErrorBody;
import com.krister.signature.backend.beans.LoginBody;

@RestController
public class MainController {
	
	@RequestMapping(value="/test/hello", method=RequestMethod.GET)
	public String sayHello() {
		return "hello!";
	}
	
	@RequestMapping(value="/test/hello", method=RequestMethod.POST)
	public String recognizeUser(@RequestBody LoginBody login) throws IOException {
		return "hello " + login.getUsername();
	}
	
}
