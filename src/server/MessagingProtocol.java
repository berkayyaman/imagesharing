package server;

import common.CryptoStandarts;
import common.Fields;
import common.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

class MessagingProtocol extends CryptoStandarts implements Fields {
    private JSONParser parser = new JSONParser();
    private Server server;
    private Communicator communicator;
    private Path userRecordsPath;
    private Logger logger;
    MessagingProtocol(Server server,Communicator communicator, ConsoleHandler consoleHandler, FileHandler fileHandler) throws IOException {
        this.server = server;
        this.communicator = communicator;
        logger = Util.generateLogger(consoleHandler,fileHandler,MessagingProtocol.class.getName());
    }
    class ReturnProtocol{
        private final String answer;
        private final Server.ImageAttributes imageAttributes;

        ReturnProtocol(String answer, Server.ImageAttributes imageAttributes){

            this.answer = answer;
            this.imageAttributes = imageAttributes;
        }

        public Server.ImageAttributes getImageAttributes() {
            return imageAttributes;
        }

        public String getAnswer() {
            return answer;
        }
    }
    ReturnProtocol processInput(String input) throws ParseException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidAlgorithmParameterException, SignatureException {
        ReturnProtocol answer;
        userRecordsPath = Paths.get("userRecords.txt");
        JSONObject messageReceived = (JSONObject)parser.parse(input);
        answer = interpret(messageReceived);
        return answer;
    }

    private ReturnProtocol interpret(JSONObject messageReceived) throws InvalidKeyException, NoSuchAlgorithmException, IOException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, SignatureException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        ReturnProtocol answer = null;
        JSONObject answerJSON = new JSONObject();
        if(messageReceived.get(fType).equals(fRegister)){
            String username = (String)messageReceived.get(this.fUsername);
            String publicKey = (String)messageReceived.get(this.fPublicKey);
            String certificate  = sign(username,publicKey,server.getPrivateKey());
            PublicKey pk = Util.convertToPublicKey(Base64.getDecoder().decode(publicKey));
            communicator.userAttributes = new Server.UserAttributes(username,pk);
            String recordContent = username+" "+certificate+"\n";
            Files.write(userRecordsPath,recordContent.getBytes());//Certificate is recorded with fUsername
            logger.info("Certificate is generated from:"+"\n"+
                    "Username: "+username+"\n"+
                    "Client Public Key:"+publicKey+"\n"+
                    "Generated fCertificate: " + certificate+"\n");
            answerJSON.put(fType, fRegisterAccepted);
            answerJSON.put(this.fCertificate,certificate);
            answer = new ReturnProtocol(answerJSON.toString(),null);
        }else if(messageReceived.get(fType).equals(fPostImage)){
            String name = (String)messageReceived.get(fImageName),
                    encryptedImage = (String)messageReceived.get(fEncryptedImage),
                    signature  = (String)messageReceived.get(fSignature),
                    encryptedKey = (String)messageReceived.get(fSymmetricKey),
                    iv = (String)messageReceived.get(fIV);
            Server.ImageAttributes ia = getVerifiedImage(server.getPrivateKey(),name,
                    encryptedImage,signature,encryptedKey,iv,communicator.userAttributes);
            answer = new ReturnProtocol(null,ia);
        }
        return answer;
    }
    void notifyUser(String imageName, DataOutputStream out) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(fType,fNewImage);

        String[] parts = imageName.split("_");
        int lnt = parts.length;
        String imageField = parts[lnt-1];
        if(lnt==3) imageField +=parts[1];

        jsonObject.put(fImageName,imageField);
        jsonObject.put(fUsername,parts[0]);
        Util.sendData(jsonObject.toString(),out);
        System.out.println("Notification Sent");
    }
}
