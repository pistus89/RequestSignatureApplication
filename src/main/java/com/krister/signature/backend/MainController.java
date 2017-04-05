package com.krister.signature.backend;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	@RequestMapping(value="/test/hello",method=RequestMethod.GET)
	public String sayHello() {
		return "hello!";
	}
}
