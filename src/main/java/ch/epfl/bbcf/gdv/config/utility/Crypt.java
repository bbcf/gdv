package ch.epfl.bbcf.gdv.config.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Crypt {

	public static byte[] encrypt(String password) throws NoSuchAlgorithmException, IOException{
		byte[] input = password.getBytes();
		MessageDigest hash = MessageDigest.getInstance("SHA1");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
		DigestInputStream digestInputStream = new DigestInputStream(byteArrayInputStream, hash);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int ch;
		while ((ch = digestInputStream.read()) >= 0) {
			byteArrayOutputStream.write(ch);
		}
		byte[] newInput = byteArrayOutputStream.toByteArray();
		byteArrayOutputStream = new ByteArrayOutputStream();
		DigestOutputStream digestOutputStream = new DigestOutputStream(byteArrayOutputStream, hash);
		digestOutputStream.write(newInput);
		digestOutputStream.close();
		//Application.appLogger.info("encrypt : "+password +" to  "+digestOutputStream.getMessageDigest().digest().toString());
		digestOutputStream.close();
		byte[] array = digestOutputStream.getMessageDigest().digest();
		//Application.appLogger.info(array.length+" : ");
		for (byte b : array){
			//System.out.print( " - " +b);
		}
		
		//Application.appLogger.info("     out digest: " +array.toString());
		return array;
		
	}
	/**
	 * Basic IO example using SHA1
	 */
	public static void main(String[] args) throws Exception {
		// Security.addProvider();        
		byte[] input = "testme".getBytes();
		System.out.println("input     : " + new String(input));    
		MessageDigest hash = MessageDigest.getInstance("SHA1");

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
		DigestInputStream digestInputStream = new DigestInputStream(byteArrayInputStream, hash);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int ch;
		while ((ch = digestInputStream.read()) >= 0) {
			byteArrayOutputStream.write(ch);
		}

		byte[] newInput = byteArrayOutputStream.toByteArray();
		System.out.println("in digest : " + new String(digestInputStream.getMessageDigest().digest()));

		byteArrayOutputStream = new ByteArrayOutputStream();
		DigestOutputStream digestOutputStream = new DigestOutputStream(byteArrayOutputStream, hash);
		digestOutputStream.write(newInput);
		digestOutputStream.close();
		byte[] array = digestOutputStream.getMessageDigest().digest();
		System.out.print(array.length);
		for (byte b : array){
			System.out.println("out digest: " +b);
		}
		
		System.out.println("out digest: " +array.toString());
	}
}
