package common;

import client.ClientMain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.Server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.*;

public class Util {
    public static class ImageAttributes{
        private final String name;
        private String username;
        private String publicKeyOfUser;
        public final String image;
        private final String symmetricKey;
        private final String iv;
        private String hashedImage;

        public ImageAttributes(String name, String publicKeyOfUser, String username,
                               String image, String symmetricKey, String iv, String hashedImage){
            this.name = name;
            this.publicKeyOfUser = publicKeyOfUser;
            this.username = username;
            this.image = image;
            this.symmetricKey = symmetricKey;
            this.iv = iv;
            this.hashedImage = hashedImage;
        }

        public String getIv() {
            return iv;
        }

        public String getImage() {
            return image;
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

        public String getHashedImage() {
            return hashedImage;
        }
    }
    public static boolean checkIfInside(String users,String username,String allUsers){
        try {
            JSONParser parser = new JSONParser();
            JSONObject userPks = (JSONObject) parser.parse(users);

            if((userPks.get(username)!=null) || (userPks.get(allUsers)!=null)){
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static Logger generateLogger(ConsoleHandler consoleHandler,FileHandler fileHandler, String name) throws IOException {
        Logger logger = Logger.getLogger(name);

        if(name.equals(ClientMain.class.getName())){
            logger.setUseParentHandlers(false);
            Handler[] handlers = logger.getHandlers();
            for(Handler h:handlers){
                logger.removeHandler(h);
            }
        }
        logger.setLevel(Level.ALL);
        fileHandler.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        return logger;
    }
    public static KeyPair readServerKeyPair() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        PublicKey publicKey = convertToPublicKey(Util.decodeBase64String(Server.publicKeyString));
        PrivateKey privateKey = convertToPrivateKey(Util.decodeBase64String(Server.privateKeyString));

        return new KeyPair(publicKey,privateKey);

    }
    public static PublicKey convertToPublicKey(byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //byte[] publicKeyAsBytes = publicKeyAsString.getBytes();//Base64.getDecoder().decode(publicKeyAsString);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
    }
    public static PrivateKey convertToPrivateKey(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //byte[] privateKeyAsBytes = privateKeyAsString.getBytes();//Base64.getDecoder().decode(privateKeyAsString);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
    }
    public static byte[] encodeImage(File file,String extension) throws ArrayIndexOutOfBoundsException, IOException {
        BufferedImage image = ImageIO.read(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image,extension,bos);
        return bos.toByteArray();
    }
    public static String convertToBase64String(byte[] data){
        return new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8);
    }
    public static byte[] decodeBase64String(String base64String){
        return Base64.getDecoder().decode(base64String);
    }

    public static void sendData(String message, DataOutputStream out) throws IOException {
            int limit = 65500;
            String firstPart;
            while(true){
                firstPart = message.substring(0,Math.min(limit,message.length()));
                if(message.length() != firstPart.length()) {
                    message = message.substring(firstPart.length());
                    out.writeUTF(firstPart);
                }else{

                    out.writeUTF(firstPart);
                    out.writeUTF(Fields.OVER);
                    break;
                }

            }
    }
    public static String receiveData(DataInputStream in) throws IOException {
        String input;
        StringBuilder message = new StringBuilder();
        while(!(input = in.readUTF()).equals(Fields.OVER)){
            message.append(input);
        }//TODO eof hatası alınıyor
        return message.toString();
    }

    public static String checkIfForAll(String keys,String allUsers){
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject)parser.parse(keys);
            String user;
            if((user = (String)obj.get(allUsers)) != null){
                return user;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean checkIfFileExists(String fileName){
        File file = new File(fileName);
        return file.exists();
    }
}
