package com.basilalasadi.fasters.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AssetDatabase {
	protected final Context context;
	protected final String name;
	protected final String assetPath;
	protected final boolean isCompressed;

	public AssetDatabase(Context context, String name) throws IOException {
		this.context = context;
		this.name = name;
		
		final AssetManager assets = context.getAssets();
		
		String[] paths = assets.list("");
		
		if (paths == null)
			throw new RuntimeException("Could not list assets dir.");
		
		final Util.PathResult pathResult = Util.findValidPath(paths, name);
		
		if (pathResult == null)
			throw new FileNotFoundException("Could not find valid filename in assets dir.");
		
		assetPath = pathResult.path;
		isCompressed = pathResult.isCompressed;
	}
	
	public File databasePath() {
		return context.getDatabasePath(name + ".db");
	}
	
	public boolean databaseExists() {
		 return databasePath().isFile();
	}
	
	protected void unpack() throws IOException {
		File databasePath = context.getDatabasePath(name + ".db");
		final AssetManager assets = context.getAssets();
		
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		
		try {
			if (isCompressed) {
				ZipInputStream zipInputStream = new ZipInputStream(assets.open(assetPath));
				
				boolean validPosition = false;
				
				while (!validPosition) {
					ZipEntry entry = zipInputStream.getNextEntry();
					if (entry == null) break;
					
					String name = entry.getName();
					int dotIndex = name.lastIndexOf('.');
					
					if (dotIndex == -1 || dotIndex >= name.length() - 2) continue;
					
					String extension = name.substring(dotIndex + 1);
					
					switch (extension) {
						case "db":
						case "sqlite":
						case "sqlite3":
							validPosition = true;
					}
				}
				
				if (!validPosition) throw new IllegalArgumentException("Zip archive does not contain a valid entry.");
				
				inputStream = zipInputStream;
			} else {
				inputStream = assets.open(assetPath);
			}
			
			Log.d("AssetDatabase", "database path " + databasePath);
			
			outputStream = new FileOutputStream(databasePath);
			
			byte[] buffer = new byte[1024];
			
			while (true) {
				int numBytes = inputStream.read(buffer);
				
				if (numBytes == -1) {
					break;
				}
				else {
					outputStream.write(buffer, 0, numBytes);
				}
			}
		}
		finally {
			if (inputStream != null) {
				inputStream.close();
			}
			
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
	
	public SQLiteDatabase openDatabase() throws IOException {
		if (!databaseExists()) {
			unpack();
		}
		
		return SQLiteDatabase.openDatabase(databasePath().getPath(), null, SQLiteDatabase.OPEN_READONLY);
	}
	
	
	protected static class Util {
		public static final class PathResult {
			public final String path;
			public final boolean isCompressed;
			
			public PathResult(String path, boolean isCompressed) {
				this.path = path;
				this.isCompressed = isCompressed;
			}
		}
		
		public static PathResult findValidPath(String[] paths, String name) {
			String assetPath = null;
			boolean isCompressed = false;
			
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
					
					case "db":
					case "sqlite":
					case "sqlite3":
						assetPath = path;
				}
			}
			
			if (assetPath == null)
				return null;
			else
				return new PathResult(assetPath, isCompressed);
		}
	}
}
