package com.krister.signature.backend.beans;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class HttpServletResponseCaptureWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream capture;
	private ServletOutputStream os;
	private PrintWriter pw;
	
	public HttpServletResponseCaptureWrapper(HttpServletResponse response) {
		super(response);
		// TODO Auto-generated constructor stub
	}

}
