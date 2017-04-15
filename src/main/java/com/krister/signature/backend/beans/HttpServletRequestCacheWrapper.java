package com.krister.signature.backend.beans;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

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
		if (this.inputStream == null) {
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
		// solve this how to handle situation of content overflow
	}

	public void writeNewBodyContent(byte[] content) {
		// here we need to ensure that the content is in it's original format
		// i.e. JSON
		ByteArrayInputStream stream = new ByteArrayInputStream(content);
		ServletInputStream servletInputStream = new ServletInputStream() {
			public int read() throws IOException {
				return stream.read();
			}

			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return stream.available() == 0;
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return stream.available() > 0;
			}

			@Override
			public void setReadListener(ReadListener arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void close() throws IOException {
				stream.close();
			}
		};
		this.inputStream = new ContentCachingInputStream(servletInputStream);
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
				} else {
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

		}

	}

}