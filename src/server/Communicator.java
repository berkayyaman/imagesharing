package server;

import common.Util;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
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
    Socket clientSocket;
    private Logger logger;
    private FileHandler logFileHandler;
    DataOutputStream out;
    public DataInputStream in;
    private MessagingProtocol messagingProtocol;
    Communicator(Server server, Socket clientSocket, String threadName,FileHandler logFileHandler) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        consoleHandler = new ConsoleHandler();
        this.logFileHandler = logFileHandler;
        logger = Util.generateLogger(consoleHandler,logFileHandler,threadName);
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
                MessagingProtocol.ReturnProtocol answer = messagingProtocol.processInput(message);
                if(answer.getImageAttributes() != null){
                    String savedImageName = server.saveImage(answer.getImageAttributes());
                    logger.info("Image Saved as Base64 String With Name "+ savedImageName+"\n");
                    notifyUsers(savedImageName,answer.getImageAttributes().getSymmetricKey());
                }
                if(answer.getAnswer() != null){
                    Util.sendData(answer.getAnswer(),out);
                }

            }catch(SocketException |EOFException s){
                logger.info("\n\nClient with IP address "+clientSocket.getRemoteSocketAddress().toString()+
                        " is disconnected.\n\n");
                break;
            } catch (SignatureException | ParseException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyUsers(String newImageName,String users) {
        ArrayList<Communicator> toRemove = new ArrayList<>();
        for(Communicator c:Server.communicators){
            if((c!=null) && (Util.checkIfInside(users,c.userAttributes.getUsername(),messagingProtocol.fAll))){
                try {
                    c.messagingProtocol.notifyUser(newImageName,c.out);
                    logger.info("\n\nNotification has been sent to user "+"\""+userAttributes.getUsername()+"\"\n\n");
                } catch (IOException e) {
                    logger.info("\n\nIt looks like user "+"\""+userAttributes.getUsername()+"\"\n" + "disconnected...\n"+
                            "Notification can't be sent.\n\n");
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
