package server;

import common.Fields;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class MessagingProtocol implements Fields {
    private JSONParser parser = new JSONParser();
    private Server server;
    public MessagingProtocol(Server server){
        this.server = server;
    }
    String processInput(String input) throws ParseException {
        String answer;
        try{
            JSONObject messageReceived = (JSONObject)parser.parse(input);
            answer = interpret(messageReceived);
        }catch(NullPointerException | NoSuchAlgorithmException |InvalidKeyException ex){
            return null;
        }
        return answer;
    }
    private String sign(String username, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] keyBytes   = server.getPublicKey().getBytes(StandardCharsets.UTF_8);
        String algorithm  = "RawBytes";
        SecretKeySpec key = new SecretKeySpec(keyBytes, algorithm);

        mac.init(key);

        byte[] data = (publicKey+username).getBytes();
        mac.update(data);
        return Arrays.toString(mac.doFinal());

    }
    private String interpret(JSONObject messageReceived) throws InvalidKeyException, NoSuchAlgorithmException {
        JSONObject answer = new JSONObject();
        if(messageReceived.get(type).equals(register)){
            String username = (String)messageReceived.get(this.username);
            String publicKey = (String)messageReceived.get(this.publicKey);
            answer.put(type,registerAccepted);
            answer.put(certificate,sign(username,publicKey));

        }else if(messageReceived.get(type).equals(request)
                && messageReceived.get(requestType).equals(publicKey)){
            answer.put(type,response);
            answer.put(responseType,publicKey);
            answer.put(publicKey,server.getPublicKey());
        }
        return answer.toString();
    }
}
