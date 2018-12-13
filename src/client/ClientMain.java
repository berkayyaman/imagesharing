package client;

import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class ClientMain {
    public static NotificationListener notificationListener;
    public static void main(String[] args){

        //noinspection InfiniteLoopStatement
        while(true){
            Terminal terminal = null;
            try {
                Client client = new Client();
                ClientMessagingProtocol protocol = new ClientMessagingProtocol(client);
                terminal = new Terminal(protocol);
                if(terminal.start()){ //if returns false, connect again
                    break;
                }
                
            } catch (IOException e) {
                cantConnect();
            } catch (NoSuchAlgorithmException | InvalidKeyException | ParseException
                    | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            if (terminal != null && terminal.thread != null) {
                terminal.thread.interrupt();
            }
        }
    }
    private static void cantConnect(){
        System.out.println("Can't connect to server, trying again.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
