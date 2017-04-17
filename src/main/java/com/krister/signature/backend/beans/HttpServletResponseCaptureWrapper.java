package com.krister.signature.backend.beans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class HttpServletResponseCaptureWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream capture;
	private ServletOutputStream outputStream;
	private PrintWriter printWriter;
	
	public HttpServletResponseCaptureWrapper(HttpServletResponse response) {
		super(response);
		capture = new ByteArrayOutputStream(response.getBufferSize());
	}

	@Override
	public ServletOutputStream getOutputStream() {
		if (printWriter != null) {
			throw new IllegalStateException("getWriter() is already called");
		}
		if (outputStream == null) {
			outputStream = new ServletOutputStream() {
				
				@Override
				public boolean isReady() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setWriteListener(WriteListener listener) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void write(int arg0) throws IOException {
					// TODO Auto-generated method stub
					capture.write(arg0);
					
				}
				@Override
				public void flush() throws IOException {
					capture.flush();
				}
				
				@Override
				public void close() throws IOException {
					capture.close();
				}
			};
		}
		return outputStream;
	}
	
	@Override
	public PrintWriter getWriter() throws UnsupportedEncodingException {
		if (outputStream != null) {
			throw new IllegalStateException("getWriter() has already been called for this response");
		}
		if (printWriter == null) {
			printWriter = new PrintWriter(new OutputStreamWriter(capture, getCharacterEncoding()));
		}
		
		return printWriter;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		super.flushBuffer();
		if (printWriter != null) {
			printWriter.flush();
		} else if (outputStream != null) {
			outputStream.flush();
		}
	}
	
	public byte[] getCaptureAsBytes() throws IOException {
		if (printWriter != null) {
			printWriter.close();
		} else if (outputStream != null) {
			outputStream.close();
		}
		
		return capture.toByteArray();
	}
	
	public String getCaptureAsString() throws IOException {
        return new String(getCaptureAsBytes(), getCharacterEncoding());
    }

}
