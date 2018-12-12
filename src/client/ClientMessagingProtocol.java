package client;

import common.CryptoStandarts;
import common.Fields;
import common.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.Server;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientMessagingProtocol extends CryptoStandarts implements Fields{
    Client client;

    public ClientMessagingProtocol(Client client){
        this.client = client;
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
        return jsonObject.toString();
    }
    boolean verify(String input) throws ParseException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, SignatureException, IOException, InvalidKeySpecException {

        JSONObject messageReceived = (JSONObject)parser.parse(input);
        if(messageReceived.get(fType).equals(fRegisterAccepted)){
            String certificate = (String)messageReceived.get(this.fCertificate);
            String cpk = Util.convertToBase64String(client.getPublicKey().getEncoded());

            return verifySignature(client.getUserName()+cpk,certificate,Client.serverPublicKey);

            /*Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(Client.serverPublicKey);

            publicSignature.update((client.getUserName()+cpk).getBytes(UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(certificate);

            return publicSignature.verify(signatureBytes);*/


        }
        return false;
    }
    void sendImage(String name,byte[] image) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
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
        jsonObject.put(fEncryptedKey,encryptedKey);
        jsonObject.put(fIV,cta.getIv());

        try {
            Util.sendData(jsonObject.toString(),client.getOut());
        } catch (IOException e) {
            System.out.println("Image cannot be sent.");
            e.printStackTrace();
        }

    }
}
