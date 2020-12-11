package com.basilalasadi.fasters.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BinaryAsset {
	public final String name;
	
	private final Context context;
	
	private String assetPath = null;
	private boolean isCompressed = false;
	
	public BinaryAsset(Context context, String name) throws IOException {
		this.context = context;
		this.name = name;
		
		findAsset();
	}
	
	public InputStream open() throws IOException {
		AssetManager assets = context.getAssets();
		
		InputStream ain = assets.open(assetPath);
		
		if (isCompressed) {
			ZipInputStream zin = new ZipInputStream(ain);
			
			while (true) {
				ZipEntry entry = zin.getNextEntry();
				
				if (entry == null) {
					break;
				}
				else if (entry.getName().equals(name + ".bin")) {
					return new NestedInputStream(ain, zin);
				}
			}
			
			zin.close();
			ain.close();
			
			throw new IOException("File not found in archive.");
		}
		else {
			return ain;
		}
	}
	
	protected void findAsset() throws IOException {
		AssetManager assets = context.getAssets();
		
		String[] paths = assets.list("");
		
		if (paths == null)
			throw new RuntimeException("Could not list assets dir.");
		
		for (int i = 0; i < paths.length && assetPath == null; i++) {
			final String path = paths[i];
			
			File file = new File(path);
			
			String filename = file.getName();
			int dotIndex = filename.lastIndexOf('.');
			
			if (dotIndex == -1 || dotIndex >= filename.length() - 2) {
				continue;
			}
			
			String bareName = filename.substring(0, dotIndex);
			String extension = filename.substring(dotIndex + 1);
			
			if (!bareName.equals(name)) {
				continue;
			}
			
			switch (extension.toLowerCase()) {
				case "zip":
				case "gz":
					isCompressed = true;
				
				case "bin":
					assetPath = path;
			}
			
			if (isCompressed) {
				InputStream fin = null;
				ZipInputStream zin = null;
				
				try {
					fin = assets.open(path);
					zin = new ZipInputStream(fin);
					
					boolean inZip = false;
					
					while (true) {
						ZipEntry entry = zin.getNextEntry();
						
						if (entry == null) {
							break;
						}
						else {
							Log.d("BinaryAsset", "archive entry " + entry.getName());
							if (entry.getName().equals(name + ".bin")) {
								inZip = true;
							}
						}
					}
					
					if (!inZip) {
						assetPath = null;
						throw new FileNotFoundException("File not found in archive.");
					}
				}
				finally {
					if (zin != null) {
						zin.close();
					}
					if (fin != null) {
						fin.close();
					}
				}
			}
		}
		
		if (assetPath == null) {
			throw new FileNotFoundException("Could not find valid filename in assets dir.");
		}
	}
	
	
	public static final class PathResult {
		public final String path;
		public final boolean isCompressed;
		
		public PathResult(String path, boolean isCompressed) {
			this.path = path;
			this.isCompressed = isCompressed;
		}
	}
	
	
	public static class NestedInputStream extends InputStream {
		private final InputStream outer;
		private final InputStream inner;
		
		public NestedInputStream(InputStream outer, InputStream inner) {
			this.outer = outer;
			this.inner = inner;
		}
		
		@Override
		public void close() throws IOException {
			inner.close();
			outer.close();
		}
		
		@Override
		public int read() throws IOException {
			return inner.read();
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			return inner.read(b);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return inner.read(b, off, len);
		}
		
		@Override
		public long skip(long n) throws IOException {
			return inner.skip(n);
		}
		
		@Override
		public int available() throws IOException {
			return inner.available();
		}
		
		@Override
		public synchronized void mark(int readlimit) {
			inner.mark(readlimit);
		}
		
		@Override
		public synchronized void reset() throws IOException {
			inner.reset();
		}
		
		@Override
		public boolean markSupported() {
			return inner.markSupported();
		}
	}
}
