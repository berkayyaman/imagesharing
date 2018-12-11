package client;

import common.KeyGeneration;
import common.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Client extends KeyGeneration {
    private String address = "localhost";
    private int port = 4444;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private PublicKey serverPublicKey;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String userName;

    Client() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        serverPublicKey = Util.getServerPublicKey();
        socket = new Socket(address,port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        KeyPair keyPair = generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }
    void sendData(String message) throws IOException {
        out.writeUTF(message);
        //out.close();
        //socket.close();
    }
    String readData() throws IOException {
        return in.readUTF();
    }
    public PrivateKey getPrivateKey(){
        return privateKey;
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
