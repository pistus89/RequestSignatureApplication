package com.krister.signature.backend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {

	@RequestMapping(value="/error/signature")
	public String signatureError(HttpServletRequest req, HttpServletResponse resp) {
		System.out.println("has errors");
		resp.setStatus(401);
		return "access denied " + req.getAttribute("error");
	}
}
