package com.tsanikgr.whist_multiplayer.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography implements ICrypto {

	private final Log log = new Log(this);
	private boolean isReady = false;
	protected static int numIterations = 1024;
	private SecretKey secretKey = null;
	private Cipher cipher = null;
	private final byte[] salt;
	private final String ivSeparator;

	/***********************************************************/
	/** Cryptography module initialisation */
	/***********************************************************/
	public Cryptography(String password, byte[] salt, String ivSeparator) {
		if (salt == null) throw new RuntimeException("Salt cannot be null");
		if (ivSeparator == null) throw new RuntimeException("IV separator cannot be null");
		this.salt = salt;
		this.ivSeparator = ivSeparator;
		try {
			char[] pass = password.toCharArray();
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(pass, salt, 1024, 256);
			secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			isReady = true;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException e) {
			log.e(e).append("Error in constructor").print();
		}
	}

	@Override
	public boolean isReady() {
		return isReady;
	}

	@Override
	public void changePassword(String password) {
		try {
			// check if we have a specific DEFAULT_SALT from the device, otherwise use
			// the static variable
			char[] pass = password.toCharArray();
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(pass, salt, 1024, 256);
			secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			isReady = true;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException e) {
			log.e(e).append("Cannot change password.").print();
		}
	}

	/***********************************************************/
	/** Public interface for encryption - decryption */
	/***********************************************************/
	@Override
	public String encrypt(String cleartext) {
		if (cleartext == null) return null;
		if (!isReady) {
			log.e().append("Cryptography not ready").print();
			return null;
		}
		try {
			return encrypt(cleartext.getBytes("UTF-8")).toString();
		} catch (Exception e) {
			log.e(e).append("Failed to encrypt").print();
			return null;
		}
	}

	@Override
	public String decrypt(String base64) {
		if (!isReady) {
			log.e().append("Cryptography not ready").print();
			return null;
		}
		CryptStorage c = new CryptStorage(base64, ivSeparator);
		if (c.getIv() == null || c.getCiphertext() == null) {
			log.e().append("Could not get IV.").print();
			return null;
		}
		try {
			byte[] b = decrypt(c);
			if (b == null) return null;
			String s = new String(b, "UTF-8");
			// log.i("Decrypted message:\n" + s);
			return s;
		} catch (UnsupportedEncodingException e) {
			log.e(e).append("Cannot decrypt string.").print();
			return null;
		}
	}

	/***********************************************************/
	/** Private enctyption decryption */
	/***********************************************************/
	// String -> Base64 String -> byte[]
	private CryptStorage encrypt(byte[] cleartext) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return new CryptStorage(cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV(), cipher.doFinal(cleartext), ivSeparator);
		} catch (InvalidKeyException | InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException e) {
			log.e(e).append("Failed to encrypt.").print();
			return null;
		}
	}

	//byte[] -> Base64 String -> String
	private byte[] decrypt(CryptStorage storage) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(storage.getIv()));
			return cipher.doFinal(storage.getCiphertext());
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
				 BadPaddingException e) {
			log.e(e).append("Failed to decrypt.").print();
			return null;
		}
	}

	/*********************************************************************************************/
	/** Stores the InitialisationVector and the encrypted text in a single class */
	/*********************************************************************************************/
	private static class CryptStorage {
		String ivSeparator;
		private byte[] iv = null;
		private byte[] ciphertext = null;

		//called when encryption is just done
		private CryptStorage(byte[] iv, byte[] ciphertext, String ivSeparator) {
			this.iv = iv;
			this.ciphertext = ciphertext;
			this.ivSeparator = ivSeparator;
		}

		//called when creating it from a text file that contains: iv + separator + encrypted text
		//splits the iv from the encrypted text
		private CryptStorage(String base64, String separator) {
			int loc = base64.indexOf(separator);
			if (loc > 0 && loc < base64.length()) {
				iv = Base64.decode(base64.substring(0, loc));
				ciphertext = Base64.decode(base64.substring(loc + separator.length()));
			}
		}

		@Override
		//returns iv + separator + encrypted text
		public String toString() {
			// log.i("getString() " + Base64.encodeToString(iv, true) +
			// separator + Base64.encodeToString(ciphertext, true));
			return Base64.encodeToString(iv, true) + ivSeparator + Base64.encodeToString(ciphertext, true);
		}

		private byte[] getCiphertext() {
			return ciphertext;
		}

		private byte[] getIv() {
			return iv;
		}
	}

	/*********************************************************************************************/
	/** Fast Byte64 encoding/decoding */
	/*********************************************************************************************/
	private static final class Base64 {

		private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
		private static final int[] IA = new int[256];
		static {
			Arrays.fill(IA, -1);
			for (int i = 0, iS = CA.length; i < iS; i++) {
				IA[CA[i]] = i;
			}
			IA['='] = 0;
		}

		// ****************************************************************************************
		// *  char[] version
		// ****************************************************************************************

		/** Encodes a raw byte array into a BASE64 <code>char[]</code> representation i accordance with RFC 2045.
		 * @param sArr The bytes to convert. If <code>null</code> or length 0 an empty array will be returned.
		 * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
		 * No line separator will be in breach of RFC 2045 which specifies max 76 per line but will be a
		 * little faster.
		 * @return A BASE64 encoded array. Never <code>null</code>.
		 */
		public final static char[] encodeToChar(byte[] sArr, boolean lineSep)
		{
			// Check special case
			int sLen = sArr != null ? sArr.length : 0;
			if (sLen == 0) return new char[0];

			int eLen = (sLen / 3) * 3; // Length of even 24-bits.
			int cCnt = ((sLen - 1) / 3 + 1) << 2; // Returned character count
			int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0); // Length of returned array
			char[] dArr = new char[dLen];

			// Encode even 24-bits
			for (int s = 0, d = 0, cc = 0; s < eLen;) {
				// Copy next three bytes into lower 24 bits of int, paying attension to sign.
				int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

				// Encode the int into four chars
				dArr[d++] = CA[(i >>> 18) & 0x3f];
				dArr[d++] = CA[(i >>> 12) & 0x3f];
				dArr[d++] = CA[(i >>> 6) & 0x3f];
				dArr[d++] = CA[i & 0x3f];

				// Add optional line separator
				if (lineSep && ++cc == 19 && d < dLen - 2) {
					dArr[d++] = '\r';
					dArr[d++] = '\n';
					cc = 0;
				}
			}

			// Pad and encode last bits if source isn't even 24 bits.
			int left = sLen - eLen; // 0 - 2.
			if (left > 0) {
				// Prepare the int
				int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

				// Set last four chars
				dArr[dLen - 4] = CA[i >> 12];
				dArr[dLen - 3] = CA[(i >>> 6) & 0x3f];
				dArr[dLen - 2] = left == 2 ? CA[i & 0x3f] : '=';
				dArr[dLen - 1] = '=';
			}
			return dArr;
		}

		/** Decodes a BASE64 encoded char array. All illegal characters will be ignored and can handle both arrays with
		 * and without line separators.
		 * @param sArr The source array. <code>null</code> or length 0 will return an empty array.
		 * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
		 * (including '=') isn't divideable by 4.  (I.e. definitely corrupted).
		 */
		public final static byte[] decode(char[] sArr)
		{
			// Check special case
			int sLen = sArr != null ? sArr.length : 0;
			if (sLen == 0) return new byte[0];

			// Count illegal characters (including '\r', '\n') to know what size the returned array will be,
			// so we don't have to reallocate & copy it later.
			int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
			for (int i = 0; i < sLen; i++) {
				if (IA[sArr[i]] < 0) {
					sepCnt++;
				}
			}

			// Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
			if ((sLen - sepCnt) % 4 != 0) return null;

			int pad = 0;
			for (int i = sLen; i > 1 && IA[sArr[--i]] <= 0;) {
				if (sArr[i] == '=') {
					pad++;
				}
			}

			int len = ((sLen - sepCnt) * 6 >> 3) - pad;

			byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

			for (int s = 0, d = 0; d < len;) {
				// Assemble three bytes into an int from four "valid" characters.
				int i = 0;
				for (int j = 0; j < 4; j++) { // j only increased if a valid char was found.
					int c = IA[sArr[s++]];
					if (c >= 0) {
						i |= c << (18 - j * 6);
					} else {
						j--;
					}
				}
				// Add the bytes
				dArr[d++] = (byte) (i >> 16);
				if (d < len) {
					dArr[d++] = (byte) (i >> 8);
					if (d < len) {
						dArr[d++] = (byte) i;
					}
				}
			}
			return dArr;
		}

		/** Decodes a BASE64 encoded char array that is known to be resonably well formatted. The method is about twice as
		 * fast as {@link #decode(char[])}. The preconditions are:<br>
		 * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
		 * + Line separator must be "\r\n", as specified in RFC 2045
		 * + The array must not contain illegal characters within the encoded string<br>
		 * + The array CAN have illegal characters at the beginning and end, those will be dealt with appropriately.<br>
		 * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
		 * @return The decoded array of bytes. May be of length 0.
		 */
		public final static byte[] decodeFast(char[] sArr)
		{
			// Check special case
			int sLen = sArr.length;
			if (sLen == 0) return new byte[0];

			int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

			// Trim illegal chars from start
			while (sIx < eIx && IA[sArr[sIx]] < 0) {
				sIx++;
			}

			// Trim illegal chars from end
			while (eIx > 0 && IA[sArr[eIx]] < 0) {
				eIx--;
			}

			// get the padding count (=) (0, 1 or 2)
			int pad = sArr[eIx] == '=' ? (sArr[eIx - 1] == '=' ? 2 : 1) : 0; // Count '=' at end.
			int cCnt = eIx - sIx + 1; // Content count including possible separators
			int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

			int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
			byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

			// Decode all but the last 0 - 2 bytes.
			int d = 0;
			for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
				// Assemble three bytes into an int from four "valid" characters.
				int i = IA[sArr[sIx++]] << 18 | IA[sArr[sIx++]] << 12 | IA[sArr[sIx++]] << 6 | IA[sArr[sIx++]];

				// Add the bytes
				dArr[d++] = (byte) (i >> 16);
				dArr[d++] = (byte) (i >> 8);
				dArr[d++] = (byte) i;

				// If line separator, jump over it.
				if (sepCnt > 0 && ++cc == 19) {
					sIx += 2;
					cc = 0;
				}
			}

			if (d < len) {
				// Decode last 1-3 bytes (incl '=') into 1-3 bytes
				int i = 0;
				for (int j = 0; sIx <= eIx - pad; j++) {
					i |= IA[sArr[sIx++]] << (18 - j * 6);
				}

				for (int r = 16; d < len; r -= 8) {
					dArr[d++] = (byte) (i >> r);
				}
			}

			return dArr;
		}

		// ****************************************************************************************
		// *  byte[] version
		// ****************************************************************************************

		/** Encodes a raw byte array into a BASE64 <code>byte[]</code> representation i accordance with RFC 2045.
		 * @param sArr The bytes to convert. If <code>null</code> or length 0 an empty array will be returned.
		 * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
		 * No line separator will be in breach of RFC 2045 which specifies max 76 per line but will be a
		 * little faster.
		 * @return A BASE64 encoded array. Never <code>null</code>.
		 */
		public final static byte[] encodeToByte(byte[] sArr, boolean lineSep)
		{
			// Check special case
			int sLen = sArr != null ? sArr.length : 0;
			if (sLen == 0) return new byte[0];

			int eLen = (sLen / 3) * 3; // Length of even 24-bits.
			int cCnt = ((sLen - 1) / 3 + 1) << 2; // Returned character count
			int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0); // Length of returned array
			byte[] dArr = new byte[dLen];

			// Encode even 24-bits
			for (int s = 0, d = 0, cc = 0; s < eLen;) {
				// Copy next three bytes into lower 24 bits of int, paying attension to sign.
				int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

				// Encode the int into four chars
				dArr[d++] = (byte) CA[(i >>> 18) & 0x3f];
				dArr[d++] = (byte) CA[(i >>> 12) & 0x3f];
				dArr[d++] = (byte) CA[(i >>> 6) & 0x3f];
				dArr[d++] = (byte) CA[i & 0x3f];

				// Add optional line separator
				if (lineSep && ++cc == 19 && d < dLen - 2) {
					dArr[d++] = '\r';
					dArr[d++] = '\n';
					cc = 0;
				}
			}

			// Pad and encode last bits if source isn't an even 24 bits.
			int left = sLen - eLen; // 0 - 2.
			if (left > 0) {
				// Prepare the int
				int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

				// Set last four chars
				dArr[dLen - 4] = (byte) CA[i >> 12];
				dArr[dLen - 3] = (byte) CA[(i >>> 6) & 0x3f];
				dArr[dLen - 2] = left == 2 ? (byte) CA[i & 0x3f] : (byte) '=';
				dArr[dLen - 1] = '=';
			}
			return dArr;
		}

		/** Decodes a BASE64 encoded byte array. All illegal characters will be ignored and can handle both arrays with
		 * and without line separators.
		 * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
		 * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
		 * (including '=') isn't divideable by 4. (I.e. definitely corrupted).
		 */
		public final static byte[] decode(byte[] sArr)
		{
			// Check special case
			int sLen = sArr.length;

			// Count illegal characters (including '\r', '\n') to know what size the returned array will be,
			// so we don't have to reallocate & copy it later.
			int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
			for (byte aSArr : sArr) {
				if (IA[aSArr & 0xff] < 0) {
					sepCnt++;
				}
			}

			// Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
			if ((sLen - sepCnt) % 4 != 0) return null;

			int pad = 0;
			for (int i = sLen; i > 1 && IA[sArr[--i] & 0xff] <= 0;) {
				if (sArr[i] == '=') {
					pad++;
				}
			}

			int len = ((sLen - sepCnt) * 6 >> 3) - pad;

			byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

			for (int s = 0, d = 0; d < len;) {
				// Assemble three bytes into an int from four "valid" characters.
				int i = 0;
				for (int j = 0; j < 4; j++) { // j only increased if a valid char was found.
					int c = IA[sArr[s++] & 0xff];
					if (c >= 0) {
						i |= c << (18 - j * 6);
					} else {
						j--;
					}
				}

				// Add the bytes
				dArr[d++] = (byte) (i >> 16);
				if (d < len) {
					dArr[d++] = (byte) (i >> 8);
					if (d < len) {
						dArr[d++] = (byte) i;
					}
				}
			}

			return dArr;
		}

		/** Decodes a BASE64 encoded byte array that is known to be resonably well formatted. The method is about twice as
		 * fast as {@link #decode(byte[])}. The preconditions are:<br>
		 * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
		 * + Line separator must be "\r\n", as specified in RFC 2045
		 * + The array must not contain illegal characters within the encoded string<br>
		 * + The array CAN have illegal characters at the beginning and end, those will be dealt with appropriately.<br>
		 * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
		 * @return The decoded array of bytes. May be of length 0.
		 */
		public final static byte[] decodeFast(byte[] sArr)
		{
			// Check special case
			int sLen = sArr.length;
			if (sLen == 0) return new byte[0];

			int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

			// Trim illegal chars from start
			while (sIx < eIx && IA[sArr[sIx] & 0xff] < 0) {
				sIx++;
			}

			// Trim illegal chars from end
			while (eIx > 0 && IA[sArr[eIx] & 0xff] < 0) {
				eIx--;
			}

			// get the padding count (=) (0, 1 or 2)
			int pad = sArr[eIx] == '=' ? (sArr[eIx - 1] == '=' ? 2 : 1) : 0; // Count '=' at end.
			int cCnt = eIx - sIx + 1; // Content count including possible separators
			int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

			int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
			byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

			// Decode all but the last 0 - 2 bytes.
			int d = 0;
			for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
				// Assemble three bytes into an int from four "valid" characters.
				int i = IA[sArr[sIx++]] << 18 | IA[sArr[sIx++]] << 12 | IA[sArr[sIx++]] << 6 | IA[sArr[sIx++]];

				// Add the bytes
				dArr[d++] = (byte) (i >> 16);
				dArr[d++] = (byte) (i >> 8);
				dArr[d++] = (byte) i;

				// If line separator, jump over it.
				if (sepCnt > 0 && ++cc == 19) {
					sIx += 2;
					cc = 0;
				}
			}

			if (d < len) {
				// Decode last 1-3 bytes (incl '=') into 1-3 bytes
				int i = 0;
				for (int j = 0; sIx <= eIx - pad; j++) {
					i |= IA[sArr[sIx++]] << (18 - j * 6);
				}

				for (int r = 16; d < len; r -= 8) {
					dArr[d++] = (byte) (i >> r);
				}
			}

			return dArr;
		}

		// ****************************************************************************************
		// * String version
		// ****************************************************************************************

		/** Encodes a raw byte array into a BASE64 <code>String</code> representation i accordance with RFC 2045.
		 * @param sArr The bytes to convert. If <code>null</code> or length 0 an empty array will be returned.
		 * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
		 * No line separator will be in breach of RFC 2045 which specifies max 76 per line but will be a
		 * little faster.
		 * @return A BASE64 encoded array. Never <code>null</code>.
		 */
		public final static String encodeToString(byte[] sArr, boolean lineSep)
		{
			// Reuse char[] since we can't create a String incrementally anyway and StringBuffer/Builder would be slower.
			return new String(encodeToChar(sArr, lineSep));
		}

		/** Decodes a BASE64 encoded <code>String</code>. All illegal characters will be ignored and can handle both strings with
		 * and without line separators.<br>
		 * <b>Note!</b> It can be up to about 2x the speed to call <code>decode(str.toCharArray())</code> instead. That
		 * will create a temporary array though. This version will use <code>str.charAt(i)</code> to iterate the string.
		 * @param str The source string. <code>null</code> or length 0 will return an empty array.
		 * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
		 * (including '=') isn't divideable by 4.  (I.e. definitely corrupted).
		 */
		public final static byte[] decode(String str)
		{
			// Check special case
			int sLen = str != null ? str.length() : 0;
			if (sLen == 0) return new byte[0];

			// Count illegal characters (including '\r', '\n') to know what size the returned array will be,
			// so we don't have to reallocate & copy it later.
			int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
			for (int i = 0; i < sLen; i++) {
				if (IA[str.charAt(i)] < 0) {
					sepCnt++;
				}
			}

			// Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
			if ((sLen - sepCnt) % 4 != 0) return null;

			// Count '=' at end
			int pad = 0;
			for (int i = sLen; i > 1 && IA[str.charAt(--i)] <= 0;) {
				if (str.charAt(i) == '=') {
					pad++;
				}
			}

			int len = ((sLen - sepCnt) * 6 >> 3) - pad;

			byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

			for (int s = 0, d = 0; d < len;) {
				// Assemble three bytes into an int from four "valid" characters.
				int i = 0;
				for (int j = 0; j < 4; j++) { // j only increased if a valid char was found.
					int c = IA[str.charAt(s++)];
					if (c >= 0) {
						i |= c << (18 - j * 6);
					} else {
						j--;
					}
				}
				// Add the bytes
				dArr[d++] = (byte) (i >> 16);
				if (d < len) {
					dArr[d++] = (byte) (i >> 8);
					if (d < len) {
						dArr[d++] = (byte) i;
					}
				}
			}
			return dArr;
		}

		/** Decodes a BASE64 encoded string that is known to be resonably well formatted. The method is about twice as
		 * fast as {@link #decode(String)}. The preconditions are:<br>
		 * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
		 * + Line separator must be "\r\n", as specified in RFC 2045
		 * + The array must not contain illegal characters within the encoded string<br>
		 * + The array CAN have illegal characters at the beginning and end, those will be dealt with appropriately.<br>
		 * @param s The source string. Length 0 will return an empty array. <code>null</code> will throw an exception.
		 * @return The decoded array of bytes. May be of length 0.
		 */
		public final static byte[] decodeFast(String s)
		{
			// Check special case
			int sLen = s.length();
			if (sLen == 0) return new byte[0];

			int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

			// Trim illegal chars from start
			while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0) {
				sIx++;
			}

			// Trim illegal chars from end
			while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0) {
				eIx--;
			}

			// get the padding count (=) (0, 1 or 2)
			int pad = s.charAt(eIx) == '=' ? (s.charAt(eIx - 1) == '=' ? 2 : 1) : 0; // Count '=' at end.
			int cCnt = eIx - sIx + 1; // Content count including possible separators
			int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

			int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
			byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

			// Decode all but the last 0 - 2 bytes.
			int d = 0;
			for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
				// Assemble three bytes into an int from four "valid" characters.
				int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6
						| IA[s.charAt(sIx++)];

				// Add the bytes
				dArr[d++] = (byte) (i >> 16);
				dArr[d++] = (byte) (i >> 8);
				dArr[d++] = (byte) i;

				// If line separator, jump over it.
				if (sepCnt > 0 && ++cc == 19) {
					sIx += 2;
					cc = 0;
				}
			}

			if (d < len) {
				// Decode last 1-3 bytes (incl '=') into 1-3 bytes
				int i = 0;
				for (int j = 0; sIx <= eIx - pad; j++) {
					i |= IA[s.charAt(sIx++)] << (18 - j * 6);
				}

				for (int r = 16; d < len; r -= 8) {
					dArr[d++] = (byte) (i >> r);
				}
			}

			return dArr;
		}
	}
}