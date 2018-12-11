package server;

import common.Util;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.*;

public class Server {
    private PublicKey publicKey;
    private int portNumber = 4444;
    private ServerSocket serverSocket = null;
    private Logger logger;
    private FileHandler logFileHandler;
    private ConsoleHandler consoleHandler;
    static class ImageAttributes{
        private final String name;
        private final String encrypted;
        private final String sign;
        private final String encryptedKey;
        private final String iv;

        ImageAttributes(String name, String encrypted, String sign, String encryptedKey, String iv){

            this.name = name;
            this.encrypted = encrypted;
            this.sign = sign;
            this.encryptedKey = encryptedKey;
            this.iv = iv;
        }
    }
    Server() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        publicKey = Util.getServerPublicKey();
        logFileHandler = new FileHandler("serverLog.log");
        consoleHandler = new ConsoleHandler();
        serverSocket = new ServerSocket(portNumber);
        logger = Util.generateLogger(consoleHandler,logFileHandler,Server.class.getName());
    }
    void listen() throws IOException, ParseException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        //noinspection InfiniteLoopStatement
        while(true){
                logger.info("Port "+portNumber+" is listening."+"\n");
                Socket clientSocket = serverSocket.accept();
                logger.info("Client with IP address "+clientSocket.getRemoteSocketAddress().toString()+
                        " is connected."+"\n");
                //noinspection InfiniteLoopStatement
                while(true){
                    DataInputStream in = new DataInputStream(
                            new BufferedInputStream(clientSocket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    MessagingProtocol messagingProtocol = new MessagingProtocol(this,consoleHandler,logFileHandler);
                    try{
                        String message = in.readUTF();
                        logger.info("Message Received: " + message+"\n");
                        MessagingProtocol.ReturnProtocol answer = messagingProtocol.processInput(message);
                        
                        out.writeUTF(answer);
                        logger.info("Response sent: "+ answer+"\n");
                    }catch(SocketException s){
                        logger.info("Client with IP address "+clientSocket.getRemoteSocketAddress().toString()+
                                " is disconnected.\n");
                        break;
                    }
                }
            }
    }
    public PublicKey getPublicKey(){
        return publicKey;
    }
}
