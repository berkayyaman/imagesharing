package client;

public class NotificationListener {
    private Terminal terminal;
    private ClientMessagingProtocol protocol;

    NotificationListener(Terminal terminal,ClientMessagingProtocol protocol){

        this.terminal = terminal;
        this.protocol = protocol;
    }
}
