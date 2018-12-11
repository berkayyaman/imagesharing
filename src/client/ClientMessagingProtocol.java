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
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
        jsonObject.put(this.fPublicKey,client.getPublicKey());
        return jsonObject.toString();
    }
    boolean verify(String input) throws ParseException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        JSONObject messageReceived = (JSONObject)parser.parse(input);
        if(messageReceived.get(fType).equals(fRegisterAccepted)){
            String certificate = (String)messageReceived.get(this.fCertificate);
            return sign(client.getUserName(), Util.convertToBase64String(client.getPublicKey().getEncoded()), client.getServerPublicKey()).equals(certificate);
        }
        return false;
    }
}
