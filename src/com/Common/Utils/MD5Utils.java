package com.Common.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
	public static String MD5(String in){
		if(in == null)
			return null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] inbyte = in.getBytes();
			md5.update(inbyte);
			byte[] retbyte = md5.digest();
			return bytes2Str(retbyte);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static String MD5(String username, String psw, String salt){
		if(username == null || psw == null || salt == null)
			return null;
		String in = username + salt + psw;
		return MD5(in);
	}
	
	public static String MD5(String psw, String salt){
		if(psw == null || salt == null)
			return null;
		String in = salt + psw;
		return MD5(in);
	}
	
	private static String bytes2Str(byte[] bytes){
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
		int j = bytes.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = bytes[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
	}
}
