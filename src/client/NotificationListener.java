package client;

import common.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

public class NotificationListener implements Runnable{
    private ClientMessagingProtocol protocol;
    private Client client;
    private Logger logger;

    static class NameFormatting{
        private final boolean notifyUser;
        private String name;
        private String username;
        NameFormatting(String name,String username,boolean notifyUser){
            this.name = name;
            this.username = username;
            this.notifyUser = notifyUser;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }
    }

    NotificationListener(ClientMessagingProtocol protocol, Client client, Logger logger){
        this.protocol = protocol;
        this.client = client;
        this.logger = logger;
    }

    @Override
    public void run() {
        JSONObject pks;
        //noinspection InfiniteLoopStatement
        while(true){
            try {
                String message;
                try{
                     message = Util.receiveData(client.getIn());
                }catch (SocketException s){
                    System.out.println("Notification listener is closing...");
                    System.out.println(Terminal.lastMessage);
                    break;
                }

                NameFormatting info;
                Util.ImageAttributes ia;
                if(((info=protocol.checkIfNotification(message))!=null)){
                    if(info.notifyUser){
                        String print = "\n\n New Image Arrived From User \""
                                +info.getUsername()+"\" with name \""
                                +info.getName()+"\"\n\n";
                        System.out.println(print);
                        System.out.print(Terminal.lastMessage);
                        logger.info("\nMessage Received: "+message+"\n");
                    }
                    Terminal.imageList.add(info);
                }else if((ia=protocol.giveSecureImage(message))!=null){
                    String[] nameParts = ia.getName().split("\\.");
                    client.saveImage(ia.getUsername()+"_"+nameParts[0],nameParts[1],ia.getImage());
                    System.out.println("\n\nImage Saved.\n\n");
                    System.out.print(Terminal.lastMessage);
                    logger.info("\nImage Received with name: "+ia.getName()+" from "+ia.getUsername()+"\n");
                }else if((pks = protocol.checkIfPublicKeyResponse(message))!=null){
                    protocol.pks = pks;
                }
            } catch (IOException | ParseException | BadPaddingException |
                    InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                    NoSuchPaddingException | SignatureException |
                    IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
    }

}
