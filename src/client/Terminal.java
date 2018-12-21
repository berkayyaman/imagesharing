package client;

import common.Fields;
import common.Util;
import org.json.simple.parser.ParseException;

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
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

public class Terminal implements Fields {
    private Scanner input;
    private ClientMessagingProtocol protocol;
    static String lastMessage = "";
    Thread thread = null;
    static ArrayList<NotificationListener.NameFormatting> imageList;
    private Logger logger;
    private NotificationListener notificationListener;
    Terminal(ClientMessagingProtocol protocol,Logger logger){
        this.protocol = protocol;
        input = new Scanner(System.in);
        this.logger = logger;
        imageList = new ArrayList<>();
    }
    boolean start() throws InvalidKeyException,
            IOException,
            InvalidAlgorithmParameterException, SignatureException, InvalidKeySpecException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, ParseException {


        //noinspection LoopStatementThatDoesntLoop

            if(registration()){
                logger.info("\n Certificate is verified.. Notification listener is starting...\n");
                printMessage("Registration completed.\n");

                notificationListener = new NotificationListener(protocol, protocol.client,logger);
                thread = new Thread(notificationListener);
                thread.start();
                while(true) {
                    printMessage("Welcome to the image sharing platform.\n" +
                            "These are the commands that you can use:\n" +
                            "upload : for uploading an image to server\n" +
                            "list : for listing available images in the server\n" +
                            "cancel : to cancel current operation\n" +
                            "exit : for exiting from the platform\n\n");
                    printMessage("Please Enter Your Command:");
                    String command = input.nextLine();

                    switch (command) {
                        case "upload":
                            if (!sendImage()) {
                                return ClientMain.EXIT;
                            }
                            break;
                        case "list":
                            if(!listImages()){
                                return ClientMain.EXIT;
                            }
                            break;
                        case "cancel":
                            break;
                        case "exit":

                            return ClientMain.EXIT;
                        default:
                            System.out.println("\n\nUnknown command.\n\n");
                            try {
                                Thread.sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }else{
                System.out.println("Certificate is not verified. Initiating new connection.");
                return ClientMain.REPEAT;
            }
    }
    private static void printMessage(String message){
        lastMessage = message;
        System.out.print(message);
    }
    private boolean registration() throws IOException, ParseException,
            NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
            SignatureException, InvalidKeySpecException {
        printMessage("Please enter a username:");
        protocol.client.setUserName(input.nextLine());
        printMessage("Please enter your password:");
        protocol.client.setPassword(input.nextLine());
        Util.sendData(protocol.registration(protocol.client.getUserName(),protocol.client.getPassword()),protocol.client.getOut());

        String in = Util.receiveData(protocol.client.getIn());
        return protocol.verify(in);
    }
    private boolean listImages(){
        int i = 0;
        for(NotificationListener.NameFormatting n:Terminal.imageList){
            System.out.println(i+" Name: "+
                    n.getName()+
                    " Username: "+n.getUsername());
            i++;
        }
        printMessage("Please enter index of the image that you want to download :");
        String keyInput = input.nextLine();
        //noinspection Duplicates
        switch (keyInput){
            case "cancel":
                return true;
            case "exit":
                return false;
            default:
                break;
        }
        try{
            int index = Integer.parseInt(keyInput);
            NotificationListener.NameFormatting image = Terminal.imageList.get(index);
            protocol.sendDownloadRequest(image.getUsername(),image.getName());
        } catch (NumberFormatException n){
            printMessage("Wrong entry.\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IndexOutOfBoundsException n){
            printMessage("Wrong index.\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    private boolean sendImage() {
        //noinspection InfiniteLoopStatement
        while(true){
            printMessage("Please Enter the Path of the Image:");
            try {
                String imagePath = input.nextLine();

                //noinspection Duplicates
                switch (imagePath){
                    case "cancel":
                        return true;
                    case "exit":
                        return false;
                    default:
                        break;
                }

                String extension  = imagePath.split("\\.")[1];
                File file = new File(imagePath);
                byte[] imageInBytes = Util.encodeImage(file,extension);
                try{
                    protocol.sendImage(file.getName(),imageInBytes);
                }catch (IOException i){
                    System.out.println("\n Server is closed... Program is closing...\n");
                    return false;
                }
                System.out.println("\nImage Sent.\n");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
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
