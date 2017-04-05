package com.krister.signature.backend.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.krister.signature.backend.beans.HttpServletRequestCacheWrapper;
import com.krister.signature.backend.beans.HttpServletResponseCaptureWrapper;

public class SignatureFilter extends GenericFilterBean{

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) arg0;
		if (req.getRequestURI().contains("exchangeKey")) {
			//signature is not delivered during a key-exchange process so request can be passed
			
		}
		HttpServletRequestCacheWrapper request = new HttpServletRequestCacheWrapper((HttpServletRequest)arg0);
		HttpServletResponseCaptureWrapper response = new HttpServletResponseCaptureWrapper((HttpServletResponse)arg1);
		byte[] requestContent = parseRequestContent(request);
		boolean signatureMatch = false;
		if (request.getContentType().equals("Application/json")) {
			signatureMatch = verifySignatureFromJSONBody(requestContent, request);
		} else {
			signatureMatch = verifySignatureFromFormBody(requestContent, request);
		}
		
		if (request.getAttribute("error") != null && !request.getAttribute("error").toString().isEmpty()) {
			
			request.getRequestDispatcher("/error/signature").forward(request, response);
			return;
		} else if (signatureMatch) {
			
		} else {
			request.setAttribute("error", "an error occurred");
			request.getRequestDispatcher("/error/signature").forward(request, response);
			return;
		}
		//handle response format by generating a new signature from the content
		response.getWriter();
	}
	
	private byte[] parseRequestContent(HttpServletRequest req) {
		//Todo
		return null;
	}
	
	private boolean verifySignatureFromJSONBody(byte[] content, HttpServletRequest request) {
		return false;
	}
	
	private boolean verifySignatureFromFormBody(byte[] content, HttpServletRequest request) {
		return false;
	}

}