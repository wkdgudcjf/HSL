package com.samsung.hsl.fitnesstrainer.comm; 

import android.util.Log;

public class CmdUtils {
	static final String TAG = "fitness util";
	public static final int BAND2APP = 0;
	public static final int APP2BAND = 1;
	private static final int DBG_LEN = 20;

	public static int hexCharToInt(char c) {
		if (c >= '0' && c <= '9')
			return (c - '0');
		if (c >= 'A' && c <= 'F')
			return (c - 'A' + 10);
		if (c >= 'a' && c <= 'f')
			return (c - 'a' + 10);
		throw new RuntimeException("invalid hex char '" + c + "'");
	}

	public static byte[] hexStringToBytes(String s) {
		byte[] ret;
		if (s == null)
			return null;
		int sz = s.length();
		ret = new byte[sz / 2];
		for (int i = 0; i < sz; i += 2) {
			ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s
					.charAt(i + 1)));
		}
		return ret;
	}

	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null)
			return null;
		StringBuilder ret = new StringBuilder(2 * bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			int b;
			b = 0x0f & (bytes[i] >> 4);
			ret.append("0123456789abcdef".charAt(b));
			b = 0x0f & bytes[i];
			ret.append("0123456789abcdef".charAt(b));
		}
		return ret.toString();
	}

	public static String byteToHexString(byte a) {
		StringBuilder ret = new StringBuilder(2);
		int b;
		b = 0x0f & (a >> 4);
		ret.append("0123456789abcdef".charAt(b));
		b = 0x0f & a;
		ret.append("0123456789abcdef".charAt(b));
		return ret.toString();
	}

	public static int bytesToInt(byte[] bytes) {
		return (int) ((bytes[0] & 0XFF) << 24)
				| (int) ((bytes[1] & 0XFF) << 16)
				| (int) ((bytes[2] & 0XFF) << 8) | (int) ((bytes[3] & 0XFF));
	}

	public static void printRaw(int direction, byte[] rawdata) {
		int iter = 0;
		if (direction == BAND2APP)
			Log.d(TAG, "== Band => APP ======");
		else if (direction == APP2BAND)
			Log.d(TAG, "== APP => BAND ======");
		else
			return;
		iter = rawdata.length / DBG_LEN;
		if (iter > 0) {
			for (int i = 0; i < iter; i++) {
				String print_val = "";
				for (int j = i * DBG_LEN; j < i * DBG_LEN + DBG_LEN; j++) {
					print_val = print_val + byteToHexString(rawdata[j]) + " ";
				}
				Log.d(TAG, print_val);
			}
		}
		if (rawdata.length % DBG_LEN > 0) {
			String print_val = "";
			for (int j = iter * DBG_LEN; j < iter * DBG_LEN + rawdata.length
					% DBG_LEN; j++) {
				print_val = print_val + byteToHexString(rawdata[j]) + " ";
			}
			Log.d(TAG, print_val);
		}
		Log.d(TAG, "=== END ==================");
	}
}
