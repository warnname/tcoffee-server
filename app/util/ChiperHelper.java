package util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import play.Logger;
import play.Play;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import exception.QuickException;

/**
 * Quick encoder/decoder string data 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class ChiperHelper {
	
    private static DESKeySpec keySpec;
	private static SecretKeyFactory keyFactory;

	static {
        try {
        	String secret = Play.configuration.getProperty("application.secret");
    		keySpec = new DESKeySpec(secret.getBytes("UTF8"));
    		keyFactory = SecretKeyFactory.getInstance("DES");

		} catch (Exception e) {
			Logger.error(e, "Unable to initiliaze %s", ChiperHelper.class.getName());
		}
    }


    public static String encrypt(String input) 
    {
    	try {
    		SecretKey key = keyFactory.generateSecret(keySpec);
    		BASE64Encoder base64encoder = new BASE64Encoder();

    		// ENCODE plainTextPassword String
    		byte[] cleartext = input.getBytes("UTF8");      

    		Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
    		cipher.init(Cipher.ENCRYPT_MODE, key);
    		return base64encoder.encode(cipher.doFinal(cleartext));

    	}
    	catch( Exception e ) {
    		throw new QuickException(e, "Failure on encrypting data");
    	}
    }

    public static String decrypt(String secret)
    {
    	try {
    		// DECODE encryptedPwd String
    		SecretKey key = keyFactory.generateSecret(keySpec);
    		BASE64Decoder base64decoder = new BASE64Decoder();

    		byte[] encrypedPwdBytes = base64decoder.decodeBuffer(secret);

    		Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
    		cipher.init(Cipher.DECRYPT_MODE, key);
    		byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
    		
    		return new String(plainTextPwdBytes, "UTF-8"); 
    	}
    	catch( Exception e ) {
    		throw new QuickException(e, "Failure on decrypting data");
    	}
  }	
	
}
