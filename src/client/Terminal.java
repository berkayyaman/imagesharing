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
    public static boolean writing = false;
    Terminal(ClientMessagingProtocol protocol){
        this.protocol = protocol;
        input = new Scanner(System.in);
    }
    boolean start() throws InvalidKeyException, NoSuchAlgorithmException,
            ParseException, IOException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, InvalidKeySpecException {
        if(registration()){

            ClientMain.lock(writing);
            System.out.println("Registration completed.");
            System.out.println("Welcome to the image sharing platform.\n" +
                    "These are the commands that you can use:\n" +
                    "upload : for uploading an image to server\n" +
                    "list : for listing available images in the server\n" +
                    "cancel : to cancel current operation\n" +
                    "exit : for exiting from the platform\n");
            System.out.print("Please Enter Your Command:");
            String command = input.nextLine();
            ClientMain.unlock(writing);
            if(command.equals("upload")){
                sendImage();
            }

        }else{
            System.out.println("Certificate is not verified. Initiating new connection.");
            return false;
        }
        return true;
    }

    private boolean registration() throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, InvalidKeySpecException {
        System.out.print("Please enter a username:");
        protocol.client.setUserName(input.nextLine());
        Util.sendData(protocol.registration(protocol.client.getUserName()),protocol.client.getOut());
        String in = Util.receiveData(protocol.client.getIn());
        return protocol.verify(in);
    }
    private void sendImage() {
        //noinspection InfiniteLoopStatement
        while(true){

            printMessage("Please Enter the Path of the Image:",writing);

            try {
                ClientMain.lock(writing);
                String imagePath = input.nextLine();
                ClientMain.unlock(writing);
                String extension  = imagePath.split("\\.")[1];
                File file = new File(imagePath);
                byte[] imageInBytes = Util.encodeImage(file,extension);
                protocol.sendImage(file.getName(),imageInBytes);
            } catch (IOException e) {
                printMessageln("Image cannot be read, please check the path.",writing);
            } catch (ArrayIndexOutOfBoundsException e) {
                printMessageln("There is a problem with the extension of the file.",writing);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
    }
    static void printMessageln(String messsage,boolean lock){
        ClientMain.lock(lock);
        System.out.println(messsage);
        ClientMain.unlock(lock);
    }
    static void printMessage(String messsage,boolean lock){
        ClientMain.lock(lock);
        System.out.print(messsage);
        ClientMain.unlock(lock);
    }
}
