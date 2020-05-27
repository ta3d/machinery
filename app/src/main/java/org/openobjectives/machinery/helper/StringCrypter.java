/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.helper;

import com.trilead.ssh2.crypto.Base64;

import org.openobjectives.androidlib.crypto.AesCrypter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <B>Class: StringCrypter </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The Logic to crypt the saved data <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class StringCrypter {

    private static AesCrypter crypter = null;
    private static final String TAG = StringCrypter.class.getSimpleName();
    public static synchronized String encrypt(String string2encrypt,
                                              String key, String iv) throws InvalidKeyException,
            NoSuchPaddingException, NoSuchAlgorithmException,
            IllegalStateException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        // padding
        string2encrypt = padString(string2encrypt);
        // encrypt
        byte[] result = getAesCrypter(key, iv).encrypt(
                string2encrypt.getBytes());
        // base 64 encoding - by Spec Base64 wraps lines / line.seperators
        char[] encodedResult = Base64.encode(result);
        return new String(encodedResult);

    }

    public static synchronized String decrypt(String string2decrypt,
                                              String key, String iv) throws InvalidKeyException,
            NoSuchPaddingException, NoSuchAlgorithmException,
            IllegalStateException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException,
            UnsupportedEncodingException, IOException {
        byte[] result = getAesCrypter(key, iv).decrypt(
                Base64.decode(string2decrypt.toCharArray()));
        String resultString = null;
        // replace null_Bytes
        resultString = new String(result).replace('\0', ' ');
        // trim string
        resultString = resultString.trim();
        return resultString;
    }

    /**
     *
     * Initialize a new instanze of {@link AesCrypter}
     *
     * @return new AesCrypter
     */
    private static AesCrypter getAesCrypter(String key, String iv) {
        //we use overwrite eachtime to be sure that new key / iv is used
        //if (crypter == null) {
        // initialize
        crypter = new AesCrypter(key.getBytes());
        crypter.useInitializationVector(iv.getBytes());
        //}
        return crypter;
    }

    /**
     * Pads the given String up to 16 byte blocks
     *
     * @param in
     * @return padded String
     */
    private static String padString(String in) {
        int slen = (in.length() % 16);
        int i = (16 - slen);
        if ((i > 0) && (i < 16)) {
            StringBuffer buf = new StringBuffer(in.length() + i);
            buf.insert(0, in);
            for (i = (16 - slen); i > 0; i--) {
                buf.append(" ");
            }
            return buf.toString();
        } else {
            return in;
        }
    }

}
