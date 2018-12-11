package client;

import common.CryptoStandarts;
import common.Fields;
import common.Util;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Terminal implements Fields {
    private Scanner input;
    private ClientMessagingProtocol protocol;

    Terminal(ClientMessagingProtocol protocol){
        this.protocol = protocol;
        input = new Scanner(System.in);
    }
    boolean start() throws InvalidKeyException, NoSuchAlgorithmException,
            ParseException, IOException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        if(registration()){
            System.out.println("Registration completed.");
            System.out.print("Welcome to the image sharing platform.\n" +
                    "These are the commands that you can use:\n" +
                    "upload : for uploading an image to server\n" +
                    "list : for listing available images in the server\n" +
                    "cancel : to cancel current operation\n" +
                    "exit : for exiting from the platform\n");
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

    private boolean registration() throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeyException {
        System.out.print("Please enter a username:");
        protocol.client.setUserName(input.nextLine());
        protocol.client.sendData(protocol.registration(protocol.client.getUserName()));
        String in = protocol.client.readData();
        return protocol.verify(in);
    }
    private void sendImage() throws NoSuchAlgorithmException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        //noinspection InfiniteLoopStatement
        while(true){
            System.out.print("Please Enter Path of the Image:");
            try {
                String imagePath = input.nextLine();
                SecretKey secretKey = protocol.generateAESKey();
                String encodedImage = Util.encodeImage(imagePath);
                CryptoStandarts.CipherTextAttributes cta = protocol.encryptData(secretKey,encodedImage);
                String digest = protocol.hashData(encodedImage);
                String sign = protocol.sign(digest,protocol.client.getPrivateKey());
                String encryptedKey = protocol.encryptKey(secretKey,protocol.client.getServerPublicKey());

            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Image cannot be read, please check the path.");
            }
        }
    }
}
