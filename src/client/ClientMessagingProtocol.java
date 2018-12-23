package client;

import common.CryptoStandarts;
import common.Fields;
import common.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Logger;

public class ClientMessagingProtocol extends CryptoStandarts implements Fields{
    Client client;
    private Logger logger;
    JSONObject pks = null;

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
    String registration(String username,String password) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        JSONObject jsonObject = new JSONObject();
        String filename = username+".txt";
        if(Util.checkIfFileExists(filename)){
            client.updateKeyPair(filename);
        }else{
            client.saveKeyPair(filename);
        }
        jsonObject.put(this.fPublicKey,Util.convertToBase64String(client.getPublicKey().getEncoded()));
        jsonObject.put(this.fType,this.fRegister);
        jsonObject.put(this.fUsername, username);
        jsonObject.put(this.fPassword,encryptWithPublicKey(password.getBytes(),Client.serverPublicKey));


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
            if(verifySignature(client.getUserName()+cpk,certificate,Client.serverPublicKey)){
                JSONArray jsonArray = (JSONArray)messageReceived.get(fImages);
                if(jsonArray != null){
                    for(int i=0;i<jsonArray.size();i++){
                        JSONObject jsonObject = (JSONObject)jsonArray.get(i);
                        System.out.println(jsonObject.toString());
                        Terminal.imageList.add(i,
                                new NotificationListener.NameFormatting((String)jsonObject.get(fImageName),
                                        (String)jsonObject.get(fUsername),false));
                    }
                }

                return true;
            }else{
                return false;
            }
        }else if(messageReceived.get(fType).equals(fRegisterRejected) && messageReceived.get(fMessage).equals(fWrongPassword)){
            System.out.println("\nWrong Password...\n");
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }
    void sendImage(String name, byte[] image, String[] users) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, InvalidKeySpecException {
        SecretKey secretKey = generateAESKey();
        CryptoStandarts.CipherTextAttributes cta = encryptData(secretKey,image);
        String digest = hashData(cta.getCipherText());

        String signature = sign(digest,client.getPrivateKey());
        byte[] sk = secretKey.getEncoded();


        JSONObject publicKeyRequest = new JSONObject();
        JSONArray allowedUsers = new JSONArray();
        boolean allUsers = false;
        for(String user:users){
            if(user.equals("all")){
                allUsers = true;
            }
            allowedUsers.add(user);
        }
        JSONObject jsonObject = new JSONObject();
        JSONObject encryptedKeys = new JSONObject();
        ArrayList<String> validUsers = new ArrayList<>();
        if(!allUsers){
            allowedUsers.add(client.getUserName());
            publicKeyRequest.put(fType,fPublicKeyRequest);
            publicKeyRequest.put(fAllowed,allowedUsers);
            Util.sendData(publicKeyRequest.toString(),client.getOut());
            while(true){
                if(pks!=null){
                    for(int i=0;i<allowedUsers.size();i++){
                        String username = (String)allowedUsers.get(i);
                        try{
                            PublicKey userPublicKey = Util.convertToPublicKey(Base64.getDecoder().decode((String)pks.get(username)));
                            encryptedKeys.put(username,encryptWithPublicKey(sk,userPublicKey));
                            validUsers.add(username);
                            System.out.println("Symmetric key is encrypted for user "+ username);
                        }catch(NullPointerException e){
                            System.out.println("There is no user named as "+username);
                        }
                    }
                    pks = null;
                    break;
                }
            }
        }else{
            encryptedKeys.put(fAll,encryptWithPublicKey(sk, Client.serverPublicKey));
        }

        if(allUsers || (validUsers.size()>0)){
            jsonObject.put(fType,fPostImage);
            jsonObject.put(fImageName,name);
            jsonObject.put(fEncryptedImage,cta.getCipherText());
            jsonObject.put(fSignature,signature);
            jsonObject.put(fSymmetricKey,encryptedKeys);
            jsonObject.put(fIV,cta.getIv());

            logger.info("\nImage is sent for users:\n");
            System.out.println("\nImage is sent for users:");
            for(String user:validUsers){
                logger.info(user+"\n");
                System.out.println(user);
            }
            Util.sendData(jsonObject.toString(),client.getOut());
        }else{
            logger.info("\nThere is no valid username entered...\n");
            System.out.println("There is no valid username entered...");
        }

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
            System.out.println(messageReceived.get(fSymmetricKey));
            String encryptedKeys;
            try{
                 encryptedKeys = (String)messageReceived.get(fSymmetricKey);
            }catch (ClassCastException e){
                JSONObject jo = (JSONObject)messageReceived.get(fSymmetricKey);
                encryptedKeys = jo.toString();
            }
            JSONObject ekJson = (JSONObject)parser.parse(encryptedKeys);
            String encryptedKey = (String)ekJson.get(fAll);
            if(encryptedKey == null){
                encryptedKey = (String)ekJson.get(client.getUserName());
            }
            System.out.println(encryptedKey);
            return getVerifiedImage(client.getPrivateKey(),imageName,
                    encrypted,signature,encryptedKey,iv,Client.serverPublicKey,userName,false);
        }   //TODO
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
    JSONObject checkIfPublicKeyResponse(String message) throws ParseException {
        System.out.println(message);
        JSONObject jo;
        JSONParser parser = new JSONParser();
        jo = (JSONObject) parser.parse(message);
        if(jo.get(fType).equals(fPublicKey)){
            if(jo.get(fAllowed) != null){
                return (JSONObject) jo.get(fAllowed);
            }
        }
        return new JSONObject();
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
