package common;

import server.Server;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.*;

public class Util {
    public static Logger generateLogger(ConsoleHandler consoleHandler,FileHandler fileHandler, String name) throws IOException {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.ALL);
        fileHandler.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.FINE);
        //logger.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        return logger;
    }
    public static KeyPair readServerKeyPair() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        PublicKey publicKey = convertToPublicKey(Util.decodeBase64String(Server.publicKeyString));
        PrivateKey privateKey = convertToPrivateKey(Util.decodeBase64String(Server.privateKeyString));

        return new KeyPair(publicKey,privateKey);

    }
    public static byte[] concatByteArrays(byte[] a,byte[] b){
        byte[] result = new byte[a.length+b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
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

    public static void sendData(String message, DataOutputStream out) throws IOException, SocketException {
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
        }
        return message.toString();
    }
}
