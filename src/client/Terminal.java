package client;

import common.Fields;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.util.Scanner;

public class Terminal implements Fields {
    private Client client;
    Scanner input;

    public Terminal(Client client){
        this.client = client;
        input = new Scanner(System.in);

    }
    public void start() throws IOException {
        client.sendData(registration());
    }

    private String registration() throws IOException {
        String username;
        System.out.print("Please enter a username:");
        username = input.nextLine();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(this.type,this.register);
        jsonObject.put(username.toString(), username.toString());
        jsonObject.put(this.publicKey,client.getPublicKey());
        return jsonObject.toString();
    }
}
