package server;

import common.Util;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Communicator implements Runnable {
    private final Thread thread;
    Server.UserAttributes userAttributes;
    private ConsoleHandler consoleHandler;
    private Server server;
    private Socket clientSocket;
    private Logger logger;
    private FileHandler logFileHandler;
    private String[] notificationBuffer;
    private DataOutputStream out;
    private DataInputStream in;
    private MessagingProtocol messagingProtocol;
    Communicator(Server server, Socket clientSocket, String threadName) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        logFileHandler = new FileHandler("serverLog.log");
        consoleHandler = new ConsoleHandler();
        logger = Util.generateLogger(consoleHandler,logFileHandler,Server.class.getName());
        int notificationBufferSize = 10;
        notificationBuffer = new String[notificationBufferSize];
        thread = new Thread(this,threadName);
        thread.start();
    }
    @Override
    public void run() {
        try {
            communicate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void communicate() throws IOException {
        //noinspection InfiniteLoopStatement
        while(true){
            in = new DataInputStream(
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
    private void notifyUsers(String newImageName) {
        ArrayList<Communicator> toRemove = new ArrayList<>();
        for(Communicator c:Server.communicators){
            if(c!=null){
                try {
                    c.messagingProtocol.notifyUser(newImageName,c.out);
                } catch (IOException e) {
                    System.out.println("Cant send notification ");
                    try {
                        c.out.close();
                        c.in.close();
                        c.clientSocket.close();
                        toRemove.add(c);
                        c.thread.interrupt();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        for(Communicator c : toRemove){
            Server.communicators.remove(c);
        }
    }
}
