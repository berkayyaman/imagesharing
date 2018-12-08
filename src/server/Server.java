package server;

import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;

public class Server {
    private PublicKey publicKey;
    private int portNumber = 4444;
    private ServerSocket serverSocket = null;

    public Server() throws IOException {
        serverSocket = new ServerSocket(portNumber);
    }
    public void listen() throws IOException, ParseException {
        while(true){
            Socket clientSocket = serverSocket.accept();
            //PrintWriter out =
            //        new PrintWriter(clientSocket.getOutputStream(), true);
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(clientSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            MessagingProtocol messagingProtocol = new MessagingProtocol(this);
            String outputLine = messagingProtocol.processInput(in.readUTF());
            out.writeUTF(outputLine);
        }
    }
    public String getPublicKey(){
        return publicKey.toString();
    }
}
