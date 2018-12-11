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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

class MessagingProtocol extends CryptoStandarts implements Fields {
    private JSONParser parser = new JSONParser();
    private Server server;
    private Path userRecordsPath;
    private Logger logger;
    MessagingProtocol(Server server, ConsoleHandler consoleHandler,FileHandler fileHandler) throws IOException {
        this.server = server;
        logger = Util.generateLogger(consoleHandler,fileHandler,MessagingProtocol.class.getName());
    }
    class ReturnProtocol{
        private final String answer;
        private final Server.ImageAttributes imageAttributes;

        ReturnProtocol(String answer, Server.ImageAttributes imageAttributes){

            this.answer = answer;
            this.imageAttributes = imageAttributes;
        }
    }
    ReturnProtocol processInput(String input) throws ParseException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        ReturnProtocol answer;
        userRecordsPath = Paths.get("userRecords.txt");
        JSONObject messageReceived = (JSONObject)parser.parse(input);
        answer = interpret(messageReceived);
        return answer;
    }

    private ReturnProtocol interpret(JSONObject messageReceived) throws InvalidKeyException, NoSuchAlgorithmException, IOException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        ReturnProtocol answer = null;
        JSONObject answerJSON = new JSONObject();
        if(messageReceived.get(fType).equals(fRegister)){
            String username = (String)messageReceived.get(this.fUsername);
            String publicKey = (String)messageReceived.get(this.fPublicKey);
            String certificate  = sign(username,publicKey,server.getPublicKey());
            String recordContent = username+" "+certificate+"\n";
            Files.write(userRecordsPath,recordContent.getBytes());//Certificate is recorded with fUsername
            logger.info("Certificate is generated from:"+"\n"+
                    "Username: "+username+"\n"+
                    "Client Public Key:"+publicKey+"\n"+
                    "Generated fCertificate: " + certificate+"\n");
            answerJSON.put(fType, fRegisterAccepted);
            answerJSON.put(this.fCertificate,certificate);
            answer = new ReturnProtocol(answerJSON.toString(),null);
        }else if(messageReceived.get(fType).equals(fPostImage)
                && messageReceived.get(fRequestType).equals(fPublicKey)){
            String name = (String)messageReceived.get(fImageName),
                    encryptedImage = (String)messageReceived.get(fEncryptedImage),
                    signature  = (String)messageReceived.get(fSignature),
                    encryptedKey = (String)messageReceived.get(fEncryptedKey),
                    iv = (String)messageReceived.get(fIV);
            Server.ImageAttributes imageAttributes =
                    new Server.ImageAttributes(name
                            ,encryptedImage,signature,encryptedKey,iv);
            answer = new ReturnProtocol(null,imageAttributes);
        }
        return answer;
    }
}
