package client;

import client.Client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ClientMain {

    public static void main(String[] args){
        try {
            Client client = new Client();
            Terminal terminal = new Terminal(client);
            terminal.start();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
