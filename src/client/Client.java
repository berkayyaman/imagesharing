package client;

import common.Fields;
import common.KeyGeneration;
import common.Util;
import server.Server;

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
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String userName;
    static PublicKey serverPublicKey;
    Client() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        socket = new Socket(address,port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        KeyPair keyPair = generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        serverPublicKey = Util.readServerKeyPair().getPublic();
    }
    public PrivateKey getPrivateKey(){
        return privateKey;
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }
}
