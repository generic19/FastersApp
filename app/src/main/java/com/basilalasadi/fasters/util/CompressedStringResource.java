package com.basilalasadi.fasters.util;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


/**
 * Loads and unpacks a compressed string resource with the resource id `redId`. The compressed
 * string resource must start with the size of the original data as a 4-byte int.
 */
public class CompressedStringResource {
	Context context;
	int resId;
	
	private byte[] compressedData;
	private int dataSize = -1;
	private String data;
	
	/**
	 * Stores the provided context and resource id to use it later for loading compressed string
	 * data.
	 *
	 * DO NOT store this instance statically if you used this constructor to make it. Otherwise,
	 * memory leaks will occur.
	 *
	 * To preload compressed data to memory, call `load()`. Call `unpack()` to preload and
	 * decompress the string resource. Otherwise, the resource will be loaded and decompressed
	 * when `getData()` is called and decompressed data is not in memory.
	 *
	 * @param context The current context.
	 * @param resId The resource id of the compressed string data.
	 */
	public CompressedStringResource(Context context, int resId) {
		this.context = context;
		this.resId = resId;
	}
	
	/**
	 * Uses provided context and resource id to read compressed string resource without storing
	 * context or resource id. Safe to store this instance statically if this constructor is used
	 * to make it.
	 *
	 * Do not call `clearCompressedData()` or else this instance will be rendered useless. Calling
	 * `load()` will produce a runtime error.
	 *
	 * Call `unpack()` to unpack compressed data. Otherwise, the data will be decompressed
	 * when `getData()` is called and decompressed data is not in memory.
	 *
	 * @param context The current context. It will not be stored.
     * @param resId The resource id. It will not be stored.
	 * @return CompressedStringResource instance.
	 */
	public static CompressedStringResource doNotStoreContext(Context context, int resId)
			throws IOException {
		
		CompressedStringResource instance = new CompressedStringResource();
		
		instance.loadResource(context, resId);
		
		return instance;
	}
	
	/**
	 * Stores provided compressed data to unpack it later.
	 *
	 * @param compressedData The compressed data only.
	 * @param dataSize The original size of the compressed data.
	 */
	private CompressedStringResource(byte[] compressedData, int dataSize) {
		this.compressedData = compressedData;
		this.dataSize = dataSize;
	}
	
	/**
	 * Empty instance.
	 */
	private CompressedStringResource() {}
	
	/**
	 * Uses provided context and resource id to read compressed string resource without storing
	 * context or resource id.
	 *
	 * @param context The current context. It will not be stored.
	 * @param resId The resource id. It will not be stored.
	 * @throws IOException if reading the resource fails.
	 */
	private void loadResource(Context context, int resId) throws IOException {
		try (InputStream iStream = context.getResources().openRawResource(resId)) {
			long t = SystemClock.elapsedRealtimeNanos();
			
			final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
			
			// Read original data size from file head.
			{
				byte[] head = new byte[4];
				
				final int count = iStream.read(head, 0, 4);
				if (count != 4) {
					throw new IOException("Resource file is too short.");
				}
				
				dataSize = ByteBuffer.wrap(head).getInt();
			}
			
			
			final byte[] buffer = new byte[1024];
			
			while (true) {
				int count = iStream.read(buffer, 0, 1024);
				
				if (count > 0) {
					oStream.write(buffer, 0, count);
				}
				else {
					break;
				}
			}
			
			compressedData = oStream.toByteArray();
			
			Log.d("Countries", "loading compressed data took " + (SystemClock.elapsedRealtimeNanos() - t) + " ns.");
		}
	}
	
	/**
	 * Loads the resource into memory.
	 *
	 * @throws IOException if reading the resource fails.
	 * @throws RuntimeException if the context is null or the resource id is -1.
	 */
	public void load() throws IOException, RuntimeException {
		if (context == null || resId == -1) {
			throw new RuntimeException("The context is null or the resource id is -1.");
		}
		
		loadResource(context, resId);
	}
	
	/**
	 * Clears decompressed data from memory.
	 */
	public void clearData() {
		data = null;
	}
	
	/**
	 * Clears the compressed data from memory.
	 */
	public void clearCompressedData() {
		compressedData = null;
		dataSize = -1;
	}
	
	/**
	 * Gets decompressed data from memory. If it isn't in memory, `unpack()` is internally called
	 * first.
	 *
	 * @return The decompressed data.
	 * @throws IOException if reading resource file fails.
	 * @throws DataFormatException if decompressing the data fails.
	 */
	public String getData() throws IOException, DataFormatException {
		if (data == null) {
			unpack();
		}
		
		return data;
	}
	
	/**
	 * Decompresses and stores decompressed data in memory. Calls `load()` internally first if
	 * compressed data is not in memory.
	 *
	 * @throws IOException if reading resource file fails.
	 * @throws DataFormatException if decompressing the data fails.
	 */
	public void unpack() throws IOException, DataFormatException {
		if (dataSize == -1 || compressedData == null) {
			load();
		}
		
		long t = SystemClock.elapsedRealtimeNanos();
		
		Inflater inflater = new Inflater();
		byte[] bytes = new byte[dataSize];
		
		inflater.setInput(compressedData);
		inflater.inflate(bytes, 0, dataSize);
		inflater.end();
		
		data = new String(bytes);
		
		Log.d("Countries", "unpacking data took " + (SystemClock.elapsedRealtimeNanos() - t) + " ns.");
	}
}
