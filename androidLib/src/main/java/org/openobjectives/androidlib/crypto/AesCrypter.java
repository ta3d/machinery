package org.openobjectives.androidlib.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AesCrypter {
	
	private final SecretKey key;
	private Cipher cipher = null;
	private IvParameterSpec iv = null;
	
	/**
	 * initiales a AesCrypter object with the given secret (symmetric) key. The key is
	 * used for encryption and decryption.
	 * 
	 * @param secret
	 */
	public AesCrypter( byte[] secret ){
		SecretKey key = new SecretKeySpec( secret, "AES");
        this.key = key; 
	}
	
    private Cipher getCipher() throws 
    	NoSuchAlgorithmException, NoSuchPaddingException
	{
	    if( cipher == null) {
	        cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding");
	    }
	    
	    return cipher;
	}
   
    /**
     * 
     * @param bytes2decrypt
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws IllegalStateException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */
    public byte[] decrypt(byte[] bytes2decrypt) throws 
	    NoSuchPaddingException, NoSuchAlgorithmException, IllegalStateException, 
	    IllegalBlockSizeException, BadPaddingException, InvalidKeyException, 
	    InvalidAlgorithmParameterException 
	{        
	    byte[] decrypted = null;
	    
	    // Initialize the cipher for encryption
	    Cipher cipher = getCipher();
	    cipher.init( Cipher.DECRYPT_MODE, key, iv);
	    // do decrypt in one go        
	    decrypted = cipher.doFinal( bytes2decrypt ) ;
	    
	    return decrypted;
	}
	
	/**
	 * 
	 * @param bytes2encrypt
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalStateException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	public byte[] encrypt(byte[] bytes2encrypt) throws 
	    NoSuchPaddingException, NoSuchAlgorithmException, IllegalStateException, 
	    IllegalBlockSizeException, BadPaddingException, InvalidKeyException, 
	    InvalidAlgorithmParameterException 
	{
	    byte[] encrypted = null;
	    
	    // Initialize the cipher for encryption
	    Cipher cipher = getCipher();
	    cipher.init( Cipher.ENCRYPT_MODE, key, iv);
	    // do encrypt in one go        
	    encrypted = cipher.doFinal( bytes2encrypt );
	    
	    return encrypted;
	}
	
    /**
     * @param spec
     */
    public void useInitializationVector( byte[] val) {
        iv = new IvParameterSpec( val);
    }


}
