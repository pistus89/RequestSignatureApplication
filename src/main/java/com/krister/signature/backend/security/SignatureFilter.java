package com.krister.signature.backend.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.filter.GenericFilterBean;

import com.krister.signature.backend.beans.HttpServletRequestCacheWrapper;
import com.krister.signature.backend.beans.HttpServletResponseCaptureWrapper;
import com.krister.signature.backend.entity.User;
import com.krister.signature.backend.service.CryptService;
import com.krister.signature.backend.service.UserService;

public class SignatureFilter extends GenericFilterBean {

	private final int MAX_TIME_LIMIT_MINUTES = 15;
	private final String JSON_CONTENT_TYPE = "application/json";

	// autowired-annotation doesn't work here so we need to use FilterConfig to
	// pass the beans
	private CryptService cryptService = FilterConfig.getBean("CryptService", CryptService.class);
	private UserService userService = FilterConfig.getBean("UserService", UserService.class);

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) arg0;
		if (req.getRequestURI().contains("exchangeKey") || req.getRequestURI().contains("register")|| req.getAttribute("error") != null
				|| req.getMethod().equals("GET")) {
			// signature is not delivered during a key-exchange process so
			// request can be passed
			// GET-methods don't have any body content and therefore they need
			// to have other means of verification
			arg2.doFilter(req, arg1);
			return;
		}
		HttpServletRequestCacheWrapper request = new HttpServletRequestCacheWrapper((HttpServletRequest) arg0);
		HttpServletResponseCaptureWrapper response = new HttpServletResponseCaptureWrapper((HttpServletResponse) arg1);
		byte[] requestContent = parseRequestContent(request);
		boolean signatureMatch = false;
		if (request.getContentType().equals(JSON_CONTENT_TYPE)) {
			signatureMatch = verifySignatureFromJSONBody(requestContent, request);
		} else {
			signatureMatch = verifySignatureFromFormBody(requestContent, request);
		}

		if (request.getAttribute("error") != null && !request.getAttribute("error").toString().isEmpty()) {
			request.getRequestDispatcher("/error/signature").forward(request, response);
			return;
		} else if (signatureMatch) {

			HttpServletRequestWrapper rw = null;
			// here we can trim the request body content from signature related
			// form and pass just the parameters
			// needed by REST endpoint
			if (request.getContentType().equals(JSON_CONTENT_TYPE)) {
				rw = trimRequestJsonContent(request, requestContent);
				arg2.doFilter(request, response);
			} else {
				rw = trimRequestFormContent(request, requestContent);
				arg2.doFilter(request, response);
			}
		} else {
			request.setAttribute("error", "an error occurred");
			request.getRequestDispatcher("/error/signature").forward(request, response);
			return;
		}
		// handle response format by generating a new signature from the content
		if (request.getAttribute("error") == null) {
			//apparently the response content needs to be written into the original servletResponse
			//in order to return it back to the client
			HttpServletResponse responsed = (HttpServletResponse) arg1;
			if (response.getContentType().contains(JSON_CONTENT_TYPE)) {
				String responseContent = generateJSONResponseSignatureContent(response, requestContent);
				responsed.getWriter().write(responseContent.toString());
			} else {
				responsed.getWriter().write(response.getCaptureAsString());
			}
			
			// create here the other mapping methods for the other response
			// content types
		}

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
		try {
			JSONObject root = parseJSONBody(content, request.getCharacterEncoding());
			String signature = root.getString("sig");
			JSONObject requestContent = root.getJSONObject("content");
			String payload = requestContent.getString("payload");
			String hexTime = payload.substring(payload.length() - 8, payload.length());
			User user = userService.findByUsername(requestContent.getString("username"));
			System.out.println(
					"user is " + user.getUsername() + " time is within limit: " + isHexTimeWithinLimit(hexTime));
			if (user != null && isHexTimeWithinLimit(hexTime)) {
				return cryptService.verifyHmacSignature(signature, payload, user);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean verifySignatureFromFormBody(byte[] content, HttpServletRequest request) {
		return false;
	}

	private JSONObject parseJSONBody(byte[] content, String charEncoding) {
		JSONObject object = null;
		try {
			object = new JSONObject(new String(content, charEncoding));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}

	private boolean isHexTimeWithinLimit(String hexTime) {
		// the hexTime is supposed to be unix time in seconds UTF
		Date hexDate = new Date(Long.parseLong(hexTime, 16) * 1000);
		Date maxDate = new Date(System.currentTimeMillis() - (MAX_TIME_LIMIT_MINUTES * 60 * 1000));
		System.out.println("hextime: " + hexTime + " hexDate: " + hexDate + " maxDate: " + maxDate);
		return hexDate.after(maxDate);

	}
	
	private String getHexTime(Date date) throws UnsupportedEncodingException {
		int unixTime = (int)(date.getTime() / 1000);
		byte[] productionDate = new byte[]{
		        (byte) (unixTime >> 24),
		        (byte) (unixTime >> 16),
		        (byte) (unixTime >> 8),
		        (byte) unixTime

		};
		return Hex.toHexString(productionDate).toUpperCase();
	}

	private HttpServletRequestWrapper trimRequestJsonContent(HttpServletRequestCacheWrapper request,
			byte[] requestContent) throws UnsupportedEncodingException {
		try {
			JSONObject object = parseJSONBody(requestContent, request.getCharacterEncoding());
			JSONObject content = object.getJSONObject("content");
			content.remove("payload");
			byte[] parsedContent = content.toString().getBytes(request.getCharacterEncoding());
			request.writeNewBodyContent(parsedContent);
			return request;
		} catch (JSONException e) {
			e.printStackTrace();
			return request;
		}
	}

	private HttpServletRequestWrapper trimRequestFormContent(HttpServletRequestWrapper request, byte[] requestContent) {
		// finish this method
		return request;
	}

	private String generateJSONResponseSignatureContent(HttpServletResponseCaptureWrapper response,
			byte[] requestContent) {
		try {
			JSONObject rootObject = parseJSONBody(requestContent, response.getCharacterEncoding());
			String username = rootObject.getJSONObject("content").getString("username");
			// maybe user could be called once and declared as a class-wide
			// variable
			User user = userService.findByUsername(username);
			// the payload is a json response body transformed into a string
			// with keys in alphabetical order and
			// covered with base64 and hex unix timestamp added at the end
			// therefore, if the client wants to verify that there are no
			// changes on the response body after it left the backend
			// it can just decode the content from base64 secret
			JSONObject responseContent = parseJSONBody(response.getCaptureAsBytes(), response.getCharacterEncoding());
			byte[] payload = Base64.encode(responseContent.toString().getBytes());
			byte[] signature = cryptService.generateHmacSignature(user,payload);
			
			responseContent.put("payload",(new String(payload)) + getHexTime(new Date()));
			JSONObject rootResponseContent = new JSONObject();
			rootResponseContent.put("content", responseContent);
			rootResponseContent.put("sig", new String(signature));
			
			return rootResponseContent.toString();
		} catch (JSONException | IOException | GeneralSecurityException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}