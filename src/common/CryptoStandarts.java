package common;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

public class CryptoStandarts extends KeyGeneration{
    public class CipherTextAttributes {
        private String cipherText;
        private String iv;
        CipherTextAttributes(String cipherText, String iv){
            this.cipherText = cipherText;
            this.iv = iv;
        }

        public String getCipherText() {
            return cipherText;
        }

        public String getIv() {
            return iv;
        }
    }
    public String sign(String username, String clientPublicKey, Key key)
            throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        return new String(Base64.getEncoder().encode(cipher.doFinal((username+clientPublicKey).getBytes())));
    }
    public String sign(String data,Key key) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        return sign(data,"",key);
    }
    public String encryptKey(SecretKey data, PublicKey serverPublicKey) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        String dataAsString = Util.convertToBase64String(data.getEncoded());
        return sign(dataAsString,"",serverPublicKey);
    }
    public CipherTextAttributes encryptData(SecretKey key, String data)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] plainText  = data.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        String iv = new String(Base64.getEncoder().encode(cipher.getIV()));
        String cipherText = new String(Base64.getEncoder().encode(cipher.doFinal()));
        return new CipherTextAttributes(cipherText,iv);
    }
    public String hashData(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new String(Base64.getEncoder().encode(digest.digest(data.getBytes(StandardCharsets.UTF_8))));
    }
}
