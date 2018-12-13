package server;

import common.Util;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Communicator implements Runnable {
    protected Server.UserAttributes userAttributes;
    private ConsoleHandler consoleHandler;
    private Server server;
    private Socket clientSocket;
    private Logger logger;
    private FileHandler logFileHandler;
    public String[] notificationBuffer;
    private int notificationBufferIndex=0;
    private int notificationBufferSize = 10;
    DataOutputStream out;
    MessagingProtocol messagingProtocol;
    public Communicator(Server server,Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        logFileHandler = new FileHandler("serverLog.log");
        consoleHandler = new ConsoleHandler();
        logger = Util.generateLogger(consoleHandler,logFileHandler,Server.class.getName());
        notificationBuffer = new String[notificationBufferSize];
    }
    @Override
    public void run() {
        try {
            communicate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void communicate() throws IOException {
        //noinspection InfiniteLoopStatement
        while(true){
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(clientSocket.getInputStream()));
            out = new DataOutputStream(clientSocket.getOutputStream());
            messagingProtocol = new MessagingProtocol(server,this,consoleHandler,logFileHandler);
            try{
                String message = Util.receiveData(in);
                logger.info("Message Received: " + message+"\n");
                MessagingProtocol.ReturnProtocol answer = messagingProtocol.processInput(message);
                if(answer.getImageAttributes() != null){
                    String savedImageName = server.saveImage(answer.getImageAttributes());
                    logger.info("Image Saved as Base64 String With Name "+ savedImageName+"\n");
                    notifyUsers(savedImageName);
                }
                if(answer.getAnswer() != null){
                    Util.sendData(answer.getAnswer(),out);
                    logger.info("Response sent: "+ answer.getAnswer()+"\n");
                }

            }catch(SocketException s){
                logger.info("Client with IP address "+clientSocket.getRemoteSocketAddress().toString()+
                        " is disconnected.\n");
                break;
            } catch (SignatureException | ParseException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
    }
    public void addToNotificationBuffer(String imageName){
        notificationBuffer[notificationBufferIndex++] = imageName;
        if(notificationBufferIndex == notificationBuffer.length-1){
            String[] newBuffer = new String[notificationBuffer.length*2];
            System.arraycopy(notificationBuffer,0,newBuffer,0,notificationBuffer.length);
            notificationBuffer = newBuffer;
        }
    }
    public void emptyNotificationBuffer(){
        while (notificationBufferIndex != 0){
            //notifyUser(notificationBuffer[--notificationBufferIndex]);
        }
        notificationBuffer = new String[notificationBufferSize];
    }
    private void notifyUsers(String newImageName) throws IOException {
        for(Communicator c:Server.communicators){
            if(c!=null){
                c.messagingProtocol.notifyUser(newImageName,c.out);
            }
        }
    }
}
