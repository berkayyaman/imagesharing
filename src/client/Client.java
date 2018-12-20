package client;

import common.KeyGeneration;
import common.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Client extends KeyGeneration {
    private String address = "localhost";
    private int port = 4444;
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String userName;
    private String password;
    public static PublicKey serverPublicKey;
    private static final String clientImagesDirectory = "ClientImages";
    Client() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        socket = new Socket(address,port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        KeyPair keyPair = generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        serverPublicKey = Util.readServerKeyPair().getPublic();
    }
    public PrivateKey getPrivateKey(){
        return privateKey;
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void saveImage(String fileName,String extension,String encodedImage) throws IOException {
        File dir=new File(clientImagesDirectory);
        if(!dir.exists()){
            dir.mkdir();
        }
        File file = new File(dir,fileName+"."+extension);
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(in);

        ImageIO.write(bufferedImage, extension,file);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
