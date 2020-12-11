package com.basilalasadi.fasters.util;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.Conversion;

public class BinaryReader {
	
	private final BufferedInputStream sin;
	
	
	public BinaryReader(InputStream in) {
		
		this.sin = new BufferedInputStream(in);
	}
	
	public void close() throws IOException {
		sin.close();
	}
	
	public String readString() throws IOException {
		int len = sin.read();
		
		if (len == -1) {
			throw new EOFException();
		}
		
		byte[] bytes = new byte[len];
		
		if (sin.read(bytes) != len) {
			throwUnexpectedEOF();
		}
		
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public double readDouble() throws IOException {
		byte[] bytes = new byte[8];
		
		int len = sin.read(bytes);
		
		if (len == -1) {
			throw new EOFException();
		}
		else if (len != 8) {
			throw new EOFException();
		}
		
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
	}
	
	private static void throwUnexpectedEOF() throws IOException {
		throw new IOException("Unexpected end of stream.");
	}
}
