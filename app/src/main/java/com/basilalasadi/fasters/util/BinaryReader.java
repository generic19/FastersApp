package com.basilalasadi.fasters.util;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BinaryReader {
	
	private final InputStream src;
	
	
	public BinaryReader(InputStream in) {
		this.src = new BufferedInputStream(in);
	}
	
	public void close() throws IOException {
		src.close();
	}
	
	public String readString() throws IOException {
		int len = src.read();
		
		if (len == -1) {
			throw new EOFException();
		}
		
		byte[] bytes = readNBytes(len);
		
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public double readDouble() throws IOException {
		byte[] bytes = readNBytes(8);
		
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
	}
	
	public byte[] readNBytes(int n) throws IOException {
		byte[] bytes = new byte[n];
		
		int totalLen = 0;
		
		while (totalLen < n) {
			int len = src.read(bytes, totalLen, n - totalLen);
			
			if (len == -1) {
				if (totalLen == 0) {
					throw new EOFException();
				}
				else {
					throwUnexpectedEOF();
				}
			}
			
			totalLen += len;
		}
		
		return bytes;
	}
	
	private static void throwUnexpectedEOF() throws IOException {
		throw new IOException("Unexpected end of stream.");
	}
}
