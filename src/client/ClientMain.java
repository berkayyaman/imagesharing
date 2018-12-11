package client;

import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ClientMain {

    public static void main(String[] args){
        //noinspection InfiniteLoopStatement
        while(true){
            try {
                Client client = new Client();
                ClientMessagingProtocol protocol = new ClientMessagingProtocol(client);
                Terminal terminal = new Terminal(protocol);
                if(terminal.start()){ //if returns false, connect again
                    break;
                }

            } catch (IOException e) {
                cantConnect();
            } catch (NoSuchAlgorithmException | InvalidKeyException | ParseException
                    | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
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
