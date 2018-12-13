package server;

import common.Fields;
import common.KeyGeneration;
import common.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Server implements Fields {
    public static String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn5Jk+6RV2Q6jtuf+D9gz19l7OB/YmosNf2xqh2tcXqKHCiBnqZd4Rw3+OjwnWGeOOGhtA+gjUhA9bN0NwcqgRczkA84MiVZmccFEiCRE4ic/T/M/zQBrY/AHAleQXDeUzXgHHB/6QJbO8YokrcEejeaiZh/F1cDL3QwPZf0QLZtpTkMMD/vPOKigItD7jUU5LN6qyEecjdzZNCXF5Yu7NTo8xQWjfFckpjLR/zixlanvDW3+8iYAiysAjnrFZDyWza5Dpp/Q4uUwONdr5qsL72asH+0suXYZg0DeQkdIXbdB8JKy3FYaYic9Z646JoTVgrN/kIx8nXXknuGghkoh2wIDAQAB";
    public static String privateKeyString = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCfkmT7pFXZDqO25/4P2DPX2Xs4H9iaiw1/bGqHa1xeoocKIGepl3hHDf46PCdYZ444aG0D6CNSED1s3Q3ByqBFzOQDzgyJVmZxwUSIJETiJz9P8z/NAGtj8AcCV5BcN5TNeAccH/pAls7xiiStwR6N5qJmH8XVwMvdDA9l/RAtm2lOQwwP+884qKAi0PuNRTks3qrIR5yN3Nk0JcXli7s1OjzFBaN8VySmMtH/OLGVqe8Nbf7yJgCLKwCOesVkPJbNrkOmn9Di5TA412vmqwvvZqwf7Sy5dhmDQN5CR0hdt0HwkrLcVhpiJz1nrjomhNWCs3+QjHyddeSe4aCGSiHbAgMBAAECggEATQdKH/9QQZtDhEOw7M0dGZEuXgVhdhixx1T6h6xXxocGUOkboU4xDXu/wTTQeWhjyu790Oj6Q977B9YdkfLSM1+Yog2RF6rRgRAemOmJJvJzKZDut3MAZBm2CHdkhF+AQT8zj2wQTaV++DILSqTyruUqd8nWojyTLH1p4X2rlZevkzJ56j14DGT0NDTmLRO2mS+SoVep6xZU2VMj1z5d78LzGLS9UJxIp5DqM+d/QBFb5LNIkEtOzO96hIuexs1VeoG3pZxK8+GAddLVlbEyBbNWoHQuYXCvkX6LaN8xWNkV6/nh97ytyPDapq38+77neUND64sZzkZ/WIwRV1kTYQKBgQDhNYm3Th8WTrwF0zyWj1sfh/HOV3HyN27uGgH6T8+ig6iQWQyAXAvUgP5F4ni2GdP3Uckgjxx4P2KpJ9mZ/gx5e2U5fdvQX+QQUIhVB4XmK0lt9O83/RoA5WFq3C1MrppWGXP+cbOpHiF0bBAcRJc2Rhp3PNfg6udSf6D28MxSnwKBgQC1Y4Keri/0bH1IovOBXN4P1FO6izp/jpYOMSdYqJZpC+8Qou+KNTk1ge1rixRO7rSg+DYoxbqX/hzkPBSZl99J9VzdLEMK5ohrwGeYyxYyG3cHkIIf7ycqf6f/K11SyVgJF7Iw2+8kij1+eZ8wBu0Ls38QTJeo/Vt7DSGS3cADRQKBgD9LX7gv8ZbAbCGq+6VJBxA2keQvOWwc3kV288VY9v8yx4ZCMLxGomCIHG6httFfMu6YgtFux06Yae8mrwaTmwftgUaGM+g9ewiGybo0EhLdaZbItw7iSJOl5Bo3ZVfe3quCHdKOPDM0r6xbzq9TK7hqPXfzlqy0+Gx8SE3+4T37AoGAcv/J+myY/rAhpgGZvHRyXTrScrx+tAxkWk8TkQQhiCwGv1dt4wPnZ2MecUZV880nO77iJ2tk56Q2EQV+UfqVmEA1Rgwf3TNXXmk3xQlM4yvChUs7FJ/9Bta0XfTSUABTDkC1uoBV16bFYgAdysc5VmfQsTa+GGe4rgUfOgvZBrkCgYAx26CImXQD7PmIwfleg0Pjyr3sMbDlJ/bMGaeag0UqZyEIohJfIDeNBmZSGEF3FbXlh5JXhul0xl3U4T4iSYWVJ2Sz6LxSa2TSObmcRXRCD8UFYYbYI0yCI2429BAaV4J7eGFxiq4KI+YLpIO6IcwuOKQd4esjmdgRU3+zGmtpuQ==";
    public static PublicKey publicKey;
    private PrivateKey privateKey;
    private int portNumber = 4444;
    private ServerSocket serverSocket = null;
    private Logger logger;
    private FileHandler logFileHandler;
    private ConsoleHandler consoleHandler;
    private String serverImagesDirectory = "ServerImages";

    private int threadNumber = 0;
    public static Communicator[] communicators;
    private static int communicatorsIndex = 0;
    private static int communicatorsCount = 10;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static class UserAttributes{
        private final String username;
        private final PublicKey publicKey;

        UserAttributes(String username, PublicKey publicKey){

            this.username = username;
            this.publicKey = publicKey;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public String getUsername() {
            return username;
        }


    }
    public static class ImageAttributes{
        private final String name;
        private String username;
        private String publicKeyOfUser;
        private final String encrypted;
        private final String symmetricKey;
        private final String iv;
        public ImageAttributes(String name,String publicKeyOfUser,String username, String encrypted, String symmetricKey, String iv){
            this.name = name;
            this.publicKeyOfUser = publicKeyOfUser;
            this.username = username;
            this.encrypted = encrypted;
            this.symmetricKey = symmetricKey;
            this.iv = iv;
        }

        public String getIv() {
            return iv;
        }

        public String getEncrypted() {
            return encrypted;
        }

        public String getSymmetricKey() {
            return symmetricKey;
        }

        public String getName() {
            return name;
        }

        public String getPublicKeyOfUser() {
            return publicKeyOfUser;
        }

        public String getUsername() {
            return username;
        }
    }
    Server() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        KeyPair keyPair = Util.readServerKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        logFileHandler = new FileHandler("serverLog.log");
        consoleHandler = new ConsoleHandler();
        serverSocket = new ServerSocket(portNumber);
        logger = Util.generateLogger(consoleHandler,logFileHandler,Server.class.getName());
        communicators = new Communicator[communicatorsCount];
    }
    void listen() throws IOException, ParseException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        //noinspection InfiniteLoopStatement
        while(true){
                logger.info("Port "+portNumber+" is listening."+"\n");
                Socket clientSocket = serverSocket.accept();
                logger.info("Client with IP address "+clientSocket.getRemoteSocketAddress().toString()+
                        " is connected."+"\n");
                Communicator communicator = new Communicator(this,clientSocket);
                Thread t = new Thread(communicator,"Communicator"+String.valueOf(threadNumber++));
                t.start();
                addCommunicator(communicator);
            }
    }
    private void addCommunicator(Communicator communicator){
        communicators[communicatorsIndex++] = communicator;
        if(communicatorsIndex == communicators.length-1){
            Communicator[] newBuffer = new Communicator[communicators.length*2];
            System.arraycopy(communicators,0,newBuffer,0,communicators.length);
            communicators = newBuffer;
        }
    }
    public String saveImage(ImageAttributes ia) throws IOException {
        File dir=new File(serverImagesDirectory);
        if(!dir.exists()){
            dir.mkdir();
        }

        int i=0;
        String index;
        File file;
        String name;
        while(true){

            if(i>0){
                index = "_"+String.valueOf(i);
            }else{
                index = "";
            }
            name = ia.getUsername()+index+"_"+ia.getName();

            file=new File(dir,name+".txt");
            if(!file.exists()){
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                break;
            }
            i++;
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        JSONObject jsonObject  =new JSONObject();
        jsonObject.put(fPublicKey,ia.getPublicKeyOfUser());
        jsonObject.put(fIV,ia.getIv());
        jsonObject.put(fSymmetricKey,ia.getSymmetricKey());
        jsonObject.put(fEncryptedImage,ia.encrypted);
        writer.write(jsonObject.toString());
        return name;
    }
}
