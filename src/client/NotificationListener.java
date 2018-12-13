package client;

import common.Util;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class NotificationListener implements Runnable{
    private ClientMessagingProtocol protocol;
    private Client client;

    static class NameFormatting{
        private String name;
        private String username;

        NameFormatting(String name, String username) {
            this.name = name;
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }
    }

    NotificationListener(ClientMessagingProtocol protocol,Client client){
        this.protocol = protocol;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            String message = Util.receiveData(client.getIn());
            NameFormatting info;
            if((info=protocol.checkIfNotification(message))!=null){
                String print = "\n\n New Image Arrived From User \""
                        +info.getUsername()+"\" with name \""
                        +info.getName()+"\"\n\n";
                System.out.println(print);
                System.out.print(Terminal.lastMessage);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

}
