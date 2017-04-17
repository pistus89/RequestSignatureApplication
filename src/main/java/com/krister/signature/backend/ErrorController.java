package com.krister.signature.backend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krister.signature.backend.beans.ErrorBody;

@RestController
public class ErrorController {

	@RequestMapping(value="/error/signature")
	public ResponseEntity signatureError(HttpServletRequest req, HttpServletResponse resp) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorBody("Invalid Signature Verification",(String)req.getAttribute("error")));
	}
}
