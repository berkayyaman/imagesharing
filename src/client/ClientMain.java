package client;

import common.Util;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ClientMain {

    public static final boolean EXIT = false;
    public static final boolean REPEAT = true;

    public static void main(String[] args){
        Logger logger = null;
        try {
            logger = Util.generateLogger(new ConsoleHandler(),new FileHandler("ClientLogs/ClientLog.log"),ClientMain.class.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //noinspection InfiniteLoopStatement
        while(true){
            Terminal terminal = null;
            try {
                Client client = new Client();
                ClientMessagingProtocol protocol = new ClientMessagingProtocol(client,logger);
                terminal = new Terminal(protocol,logger);
                if(terminal.start() == EXIT){ //if returns false, connect again
                    if(terminal.thread != null){
                        terminal.thread.interrupt();
                    }
                    client.in.close();
                    client.out.close();
                    client.socket.close();
                    logger.info("\r\nProgram is closing...\r\n");
                    System.exit(0);
                }else{
                    if(terminal.thread != null){
                        terminal.thread.interrupt();
                    }
                    client.in.close();
                    client.out.close();
                    client.socket.close();
                }
                
            } catch (IOException e) {
                cantConnect();
                if (terminal != null && terminal.thread != null) {
                    terminal.thread.interrupt();
                }
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
