package common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.*;

public class Util {
    public static Logger generateLogger(ConsoleHandler consoleHandler,FileHandler fileHandler, String name) throws IOException {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.ALL);
        fileHandler.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(fileHandler);
        //logger.addHandler(consoleHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        return logger;
    }
    public static PublicKey getServerPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path publicKeyPath = Paths.get("serverPublicKey.txt");
        String data = Files.readString(publicKeyPath);
        byte[] publicKeyAsBytes = Base64.getDecoder().decode(data);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyAsBytes));
    }
    public static byte[] convertToBytes(String data){
        String[] byteValues = data.substring(1, data.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];
        for (int i=0, len=bytes.length; i<len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }
        return bytes;
    }
    public static String encodeImage(String imagePath) throws ArrayIndexOutOfBoundsException, IOException {
        String extension  = imagePath.split(".")[1];
        File imageFile = new File(imagePath);
        BufferedImage image = ImageIO.read(imageFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image,extension,bos);
        return new String(Base64.getEncoder().encode(bos.toByteArray()));
    }
    public static String convertToBase64String(byte[] data){
        return new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8);
    }
}
