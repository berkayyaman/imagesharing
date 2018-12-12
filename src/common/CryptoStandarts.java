package common;

import client.Client;
import server.Server;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    public String sign(String username, String clientPublicKey, PrivateKey key)
            throws NoSuchAlgorithmException, InvalidKeyException,SignatureException {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(key);
        privateSignature.update((username+clientPublicKey).getBytes(UTF_8));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }
    public String sign(String data,PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return sign(data,"",key);
    }

    public String encrypyWithPublicKey(byte[] data, PublicKey key)
            throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        return new String(Base64.getEncoder().encode(cipher.doFinal(data)));
    }

    public SecretKey decryptWithPrivateKey(String encrypted, PrivateKey privateKey) throws IllegalBlockSizeException,
            NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, SignatureException {
        Cipher decrypt=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decrypt.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedMessage = Base64.getDecoder().decode(encrypted);
        byte[] decryptedMessage = decrypt.doFinal(encryptedMessage);
        return new SecretKeySpec(decryptedMessage,"AES");
    }
    public CipherTextAttributes encryptData(SecretKey key, byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //byte[] plainText  = data.getBytes(UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE,key);
        String iv = new String(Base64.getEncoder().encode(cipher.getIV()));
        String cipherText = new String(Base64.getEncoder().encode(cipher.doFinal(data)));
        return new CipherTextAttributes(cipherText,iv);
    }
    public byte[] decryptData(SecretKey key,byte[] data,String iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(iv.getBytes()));

        cipher.init(Cipher.DECRYPT_MODE,key,ivParameterSpec);
        return cipher.doFinal(data);
    }
    public String hashData(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new String(Base64.getEncoder().encode(digest.digest(data.getBytes(UTF_8))));
    }
    public String hashData(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new String(Base64.getEncoder().encode(digest.digest(data)));
    }
    protected Server.ImageAttributes getVerifiedImage(PrivateKey privateKey, String name,
                                                      String encryptedImage, String signature,
                                                      String encryptedKey, String iv, Server.UserAttributes userAttributes)
            throws NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException, InvalidKeySpecException, InvalidAlgorithmParameterException, SignatureException {
        SecretKey aesKey = decryptWithPrivateKey(encryptedKey,privateKey);
        byte[] encryptedImageBytes = Base64.getDecoder().decode(encryptedImage);
        byte[] decryptedImage = decryptData(aesKey,encryptedImageBytes,iv);
        String hashedImage = hashData(decryptedImage);
        System.out.println("Name "+ name);
        System.out.println("ei "+encryptedImage);
        System.out.println("sign "+signature);
        System.out.println("ek "+ encryptedKey);
        if(userAttributes == null){
            System.out.println("YES");
        }
        System.out.println("un "+userAttributes.getUsername());
        System.out.println("upk "+ userAttributes.getPublicKey());
        if(verifySignature(hashedImage,signature,userAttributes.getPublicKey())){
            String serverSignature = sign(hashedImage,privateKey);
            return new Server.ImageAttributes(name,userAttributes.getUsername(),encryptedImage,serverSignature,
                    Util.convertToBase64String(aesKey.getEncoded()),iv);
        }
        else{
            return null;
        }
    }
    protected boolean verifySignature(String data,String signature,PublicKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");

        publicSignature.initVerify(key);
        publicSignature.update(data.getBytes(UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return publicSignature.verify(signatureBytes);
    }
}
