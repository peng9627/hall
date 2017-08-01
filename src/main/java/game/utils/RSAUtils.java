package game.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * created date 16-7-7
 * author pengyi
 */
public class RSAUtils {

    /**
     * 得到公钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 得到密钥字符串（经过base64编码）
     *
     * @return 秘钥字符串
     */
    public static String getKeyString(Key key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        return Base64.getEncoder().encode(keyBytes).toString();
    }

    /**
     * 加密，返回BASE64编码的字符串
     *
     * @param key   秘钥
     * @param bytes 　需要加密的字符串
     * @return 加密后的字符串
     */
    public static byte[] encrypt(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] result = null;
            for (int i = 0; i < bytes.length; i += 117) {
                byte[] enBytes = cipher.doFinal(ByteUtils.subarray(bytes, i, i + 117));
                result = ByteUtils.addAll(result, enBytes);
            }

            if (result != null) {
                Base64.getEncoder().encode(result);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 对明文密文进行解密
     *
     * @param key   秘钥
     * @param bytes 　需要解密的数据
     * @return 解密后的字符串
     */
    public static byte[] decrypt(Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] result = null;
            bytes = Base64.getDecoder().decode(bytes);
            for (int i = 0; i < bytes.length; i += 128) {
                byte[] doFinal = cipher.doFinal(ByteUtils.subarray(bytes, i, i + 128));
                result = ByteUtils.addAll(result, doFinal);
            }
            return result;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
