package server;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args){
        try {
            Server server = new Server();
            server.listen();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
