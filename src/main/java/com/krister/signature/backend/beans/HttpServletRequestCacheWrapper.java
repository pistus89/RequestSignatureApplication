package com.krister.signature.backend.beans;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class HttpServletRequestCacheWrapper extends HttpServletRequestWrapper {
	
	private final ByteArrayOutputStream cachedContent;
	private final Integer contentCacheLimit;

	private ServletInputStream inputStream;

	private BufferedReader reader;
	
	public HttpServletRequestCacheWrapper(HttpServletRequest request) {
		super(request);
		int contentLength = request.getContentLength();
		cachedContent = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
		this.contentCacheLimit = null;
	}
	
	public HttpServletRequestCacheWrapper(HttpServletRequest request, int contentCacheLimit) {
		super(request);
		this.contentCacheLimit = contentCacheLimit;
		this.cachedContent = new ByteArrayOutputStream(contentCacheLimit);
	}
	
	@Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.getInputStream() != null) {
        	this.inputStream = new ContentCachingInputStream(getRequest().getInputStream());
        }
        return this.inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
    	if (this.reader == null) {
			this.reader = new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
		}
    	return this.reader;
    }
    protected void handleContentOverflow(int contentCacheLimit) {
	}


private class ContentCachingInputStream extends ServletInputStream {

	private final ServletInputStream is;

	private boolean overflow = false;

	public ContentCachingInputStream(ServletInputStream is) {
		this.is = is;
	}

	@Override
	public int read() throws IOException {
		int ch = this.is.read();
		if (ch != -1 && !this.overflow) {
			if (contentCacheLimit != null && cachedContent.size() == contentCacheLimit) {
				this.overflow = true;
				handleContentOverflow(contentCacheLimit);
			}
			else {
				cachedContent.write(ch);
			}
		}
		return ch;
	}

	@Override
	public boolean isFinished() {
		return this.is.isFinished();
	}

	@Override
	public boolean isReady() {
		return this.is.isReady();
	}

	@Override
	public void setReadListener(ReadListener arg0) {
		// TODO Auto-generated method stub
		
	}

}

}