package kvbdev.messenger.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MessengerClient implements Closeable {
    protected long keepAliveDelaySeconds = 30;
    protected ScheduledExecutorService scheduler;

    protected Socket socket;
    protected ConnectionReader connectionReader;
    protected OutputStreamWriter connectionWriter;

    protected String userName = "";

    public MessengerClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        connectionReader = new ConnectionReader(socket.getInputStream(), System.out::println);
        connectionWriter = new OutputStreamWriter(socket.getOutputStream());

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(
                this::keepAlive,
                keepAliveDelaySeconds,
                keepAliveDelaySeconds,
                TimeUnit.SECONDS
        );

        connectionReader.start();
    }

    public void setOnMessageListener(Consumer<String> onMessageListener) {
        this.connectionReader.setReceiver(onMessageListener);
    }

    public void send(String text) {
        if (isClosed()) return;
        try {
            connectionWriter.append(text).append("\n");
            connectionWriter.flush();
        } catch (IOException e) {
            try {
                close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void keepAlive() {
        send("/echo");
    }

    public void login(String name) {
        this.userName = name;
        send("/login " + userName);
    }

    public void printUsers() {
        send("/users");
    }

    public boolean isClosed() {
        return socket.isClosed() || socket.isInputShutdown();
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdownNow();
        connectionReader.interrupt();
        connectionWriter.close();
        socket.close();
    }
}
