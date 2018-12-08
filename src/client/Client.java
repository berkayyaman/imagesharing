package client;

import common.KeyGenerator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class Client extends KeyGenerator {
    private String address = "localhost";
    private int port = 4444;
    private Socket socket;
    private DataOutputStream out;

    Client() throws IOException, NoSuchAlgorithmException {
        socket = new Socket(address,port);
        out = new DataOutputStream(socket.getOutputStream());
        generateKeyPair();
    }
    void sendData(String message) throws IOException {
        out.writeUTF(message);
        //out.close();
        //socket.close();
    }
    public String getPrivateKey(){
        return privateKey.toString();
    }
    public String getPublicKey(){
        return publicKey.toString();
    }
}
