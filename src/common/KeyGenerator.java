package common;


import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class KeyGenerator {
    protected PrivateKey privateKey;
    protected PublicKey publicKey;
    protected void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
        keyPairGen.initialize(2048);
        this.privateKey = keyPairGen.generateKeyPair().getPrivate();
        this.publicKey = keyPairGen.generateKeyPair().getPublic();
    }
}
