package com.krister.signature.backend.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.github.tsohr.JSONException;
import com.github.tsohr.JSONObject;
import com.krister.signature.backend.beans.HttpServletRequestCacheWrapper;
import com.krister.signature.backend.beans.HttpServletResponseCaptureWrapper;
import com.krister.signature.backend.entity.User;
import com.krister.signature.backend.service.CryptService;
import com.krister.signature.backend.service.UserService;

public class SignatureFilter extends GenericFilterBean{
	
	private final int MAX_TIME_LIMIT_MINUTES = 15;
	
	//autowired-annotation doesn't work here so we need to use FilterConfig to pass the beans
	private CryptService cryptService = FilterConfig.getBean("CryptService", CryptService.class);
	private UserService userService = FilterConfig.getBean("UserService", UserService.class);
	
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
			HttpServletRequestWrapper rw = null;
			//here we can trim the request body content from signature related form and pass just the parameters
			//needed by REST endpoint
			if (request.getContentType() == "Application/json") {
				rw = trimRequestJsonContent(request,requestContent);
			} else {
				rw = trimRequestFormContent(request,requestContent);
			}
		} else {
			request.setAttribute("error", "an error occurred");
			request.getRequestDispatcher("/error/signature").forward(request, response);
			return;
		}
		//handle response format by generating a new signature from the content
		response.getWriter();
	}
	
	private byte[] parseRequestContent(HttpServletRequestCacheWrapper req) {
		try {
			return IOUtils.toByteArray(req.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean verifySignatureFromJSONBody(byte[] content, HttpServletRequest request) {
		JSONObject root = parseJSONBody(content, request.getCharacterEncoding());
		String signature = root.getString("sig");
		JSONObject requestContent = root.getJSONObject("content");
		String payload = requestContent.getString("payload");
		String hexTime = payload.substring(payload.length()- 8,payload.length()-1);
		User user = userService.findByUsername(requestContent.getString("user"));
		if (user != null && hexTimeWithinLimit(hexTime)) {
			return cryptService.verifyHmacSignature(signature,payload,user);
		}
		return false;
	}
	
	private boolean verifySignatureFromFormBody(byte[] content, HttpServletRequest request) {
		return false;
	}
	
	private JSONObject parseJSONBody(byte[] content, String charEncoding) {
		JSONObject object = null;
		try {
			object = new JSONObject(new String(content,charEncoding));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}
	
	private boolean hexTimeWithinLimit(String hexTime) {
		//the hexTime is supposed to be unix time in seconds UTF
		Date hexDate = new Date(Long.parseLong(hexTime, 16)*1000);
		Date maxDate = new Date(System.currentTimeMillis() + (MAX_TIME_LIMIT_MINUTES * 60 * 1000));
		if (hexDate.before(maxDate)) {
			return true;
		}
		return false;
	}
	
	private HttpServletRequestWrapper trimRequestJsonContent(HttpServletRequestWrapper request, byte[] requestContent) {
		//finish this method
		return request;
	}
	
	private HttpServletRequestWrapper trimRequestFormContent(HttpServletRequestWrapper request, byte[] requestContent) {
		//finish this method
		return request;
	}

}