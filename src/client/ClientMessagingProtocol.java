package client;

import common.CryptoStandarts;
import common.Fields;
import common.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

public class ClientMessagingProtocol extends CryptoStandarts implements Fields{
    Client client;
    private Logger logger;

    public ClientMessagingProtocol(Client client, Logger logger){
        this.client = client;
        this.logger = logger;
    }
    private JSONParser parser = new JSONParser();
    String processInput(String input) throws ParseException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        JSONObject messageReceived = (JSONObject)parser.parse(input);
        return interpret(messageReceived);
    }

    private String interpret(JSONObject messageReceived) throws InvalidKeyException, NoSuchAlgorithmException {
        JSONObject answer = new JSONObject();

        return "";
    }
    String registration(String username) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(this.fType,this.fRegister);
        jsonObject.put(this.fUsername, username);
        jsonObject.put(this.fPublicKey,Util.convertToBase64String(client.getPublicKey().getEncoded()));
        logger.info("\nMessage Sent:"+
                jsonObject.toString()+"\n");
        return jsonObject.toString();
    }
    boolean verify(String input) throws ParseException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, IOException, InvalidKeySpecException {

        JSONObject messageReceived = (JSONObject)parser.parse(input);
        logger.info("\nMessage Received: "+input+"\n");
        if(messageReceived.get(fType).equals(fRegisterAccepted)){
            String certificate = (String)messageReceived.get(this.fCertificate);
            String cpk = Util.convertToBase64String(client.getPublicKey().getEncoded());

            return verifySignature(client.getUserName()+cpk,certificate,Client.serverPublicKey);
        }
        return false;
    }
    void sendImage(String name,byte[] image) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        SecretKey secretKey = generateAESKey();
        CryptoStandarts.CipherTextAttributes cta = encryptData(secretKey,image);
        String digest = hashData(image);

        String signature = sign(digest,client.getPrivateKey());
        byte[] sk = secretKey.getEncoded();
        String encryptedKey = encrypyWithPublicKey(sk, Client.serverPublicKey);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(fType,fPostImage);
        jsonObject.put(fImageName,name);
        jsonObject.put(fEncryptedImage,cta.getCipherText());
        jsonObject.put(fSignature,signature);
        jsonObject.put(fSymmetricKey,encryptedKey);
        jsonObject.put(fIV,cta.getIv());
        logger.info("\nImage is sent...\n");
        Util.sendData(jsonObject.toString(),client.getOut());
    }
    Util.ImageAttributes giveSecureImage(String message) throws ParseException,
            BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchPaddingException, SignatureException,
            IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
        JSONObject messageReceived = (JSONObject)parser.parse(message);
        if(messageReceived.get(fType).equals(fPostImage)){
            String imageName = (String)messageReceived.get(fImageName);
            String certificate = (String)messageReceived.get(fPublicKey);
            String userName = (String)messageReceived.get(fUsername);
            String encrypted = (String)messageReceived.get(fEncryptedImage);
            String iv = (String)messageReceived.get(fIV);
            String signature = (String)messageReceived.get(fSignature);
            String encryptedKey = (String)messageReceived.get(fSymmetricKey);

            return getVerifiedImage(client.getPrivateKey(),imageName,
                    encrypted,signature,encryptedKey,iv,Client.serverPublicKey,userName,false);
        }
        return null;

    }
    NotificationListener.NameFormatting checkIfNotification(String message) throws ParseException {
        JSONObject messageReceived = (JSONObject)parser.parse(message);
        String name,username;
        if(messageReceived.get(fType).equals(fNewImage)) {
            name = (String) messageReceived.get(fImageName);
            username = (String) messageReceived.get(fUsername);

            if ((name != null) && (username != null)) {
                if (username.equals(client.getUserName())) {
                    return new NotificationListener.NameFormatting(name, username, false);
                } else {
                    return new NotificationListener.NameFormatting(name, username, true);
                }
            }
        }
        return null;
    }
    void sendDownloadRequest(String userName,String name){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(fType,fDownload);
        jsonObject.put(fUsername,userName);
        jsonObject.put(fImageName,name);
        logger.info("Message is sending: "+jsonObject.toString());
        try {
            Util.sendData(jsonObject.toString(),client.getOut());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
