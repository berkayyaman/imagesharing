package client;

import common.CryptoStandarts;
import common.Fields;
import common.Util;
import org.json.simple.parser.ParseException;
import server.Server;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Terminal implements Fields {
    private Scanner input;
    private ClientMessagingProtocol protocol;
    public static String lastMessage = "";
    Thread thread = null;
    Terminal(ClientMessagingProtocol protocol){
        this.protocol = protocol;
        input = new Scanner(System.in);
    }
    boolean start() throws InvalidKeyException, NoSuchAlgorithmException,
            ParseException, IOException, NoSuchPaddingException, BadPaddingException,
            IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, InvalidKeySpecException {

        if(registration()){
            thread = new Thread(new NotificationListener(protocol,protocol.client));
            thread.start();
            printMessage("Registration completed.\n");
            printMessage("Welcome to the image sharing platform.\n" +
                    "These are the commands that you can use:\n" +
                    "upload : for uploading an image to server\n" +
                    "list : for listing available images in the server\n" +
                    "cancel : to cancel current operation\n" +
                    "exit : for exiting from the platform\n\n");
            printMessage("Please Enter Your Command:");
            String command = input.nextLine();

            if(command.equals("upload")){
                sendImage();
            }

        }else{
            System.out.println("Certificate is not verified. Initiating new connection.");
            return false;
        }
        return true;
    }
    private static void printMessage(String message){
        lastMessage = message;
        System.out.print(message);
    }
    private boolean registration() throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, InvalidKeySpecException {
        printMessage("Please enter a username:");
        protocol.client.setUserName(input.nextLine());
        Util.sendData(protocol.registration(protocol.client.getUserName()),protocol.client.getOut());
        String in = Util.receiveData(protocol.client.getIn());
        return protocol.verify(in);
    }
    private void sendImage() {
        //noinspection InfiniteLoopStatement
        while(true){
            printMessage("Please Enter the Path of the Image:");
            try {
                String imagePath = input.nextLine();
                String extension  = imagePath.split("\\.")[1];
                File file = new File(imagePath);
                byte[] imageInBytes = Util.encodeImage(file,extension);
                protocol.sendImage(file.getName(),imageInBytes);
            } catch (IOException e) {
                System.out.println("Image cannot be read, please check the path.");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("There is a problem with the extension of the file.");
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
    }
}
