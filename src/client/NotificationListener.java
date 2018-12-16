package client;

import common.Util;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class NotificationListener implements Runnable{
    private ClientMessagingProtocol protocol;
    private Client client;
    ArrayList<NameFormatting> imageList;
    static class NameFormatting{
        private final boolean notifyUser;
        private String name;
        private String username;
        NameFormatting(String name,String username,boolean notifyUser){
            this.name = name;
            this.username = username;
            this.notifyUser = notifyUser;
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
        imageList = new ArrayList<>();
    }

    @Override
    public void run() {

        //noinspection InfiniteLoopStatement
        while(true){
            try {
                String message = Util.receiveData(client.getIn());
                NameFormatting info;
                if(((info=protocol.checkIfNotification(message))!=null)){
                    if(info.notifyUser){
                        String print = "\n\n New Image Arrived From User \""
                                +info.getUsername()+"\" with name \""
                                +info.getName()+"\"\n\n";
                        System.out.println(print);
                        System.out.print(Terminal.lastMessage);
                    }
                    imageList.add(info);
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
