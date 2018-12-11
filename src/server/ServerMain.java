package server;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ServerMain {

    public static void main(String[] args){
        try {
            Server server = new Server();
            server.listen();
        } catch (IOException | ParseException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
