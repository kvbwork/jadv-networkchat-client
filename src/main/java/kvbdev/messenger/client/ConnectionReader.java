package kvbdev.messenger.client;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConnectionReader extends Thread {
    protected final Scanner input;
    protected volatile Consumer<String> receiver;

    public ConnectionReader(InputStream input, Consumer<String> onReceive) {
        super("ConnectionReader");
        this.receiver = onReceive;
        this.input = new Scanner(input);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String text = input.nextLine();
                if (text == null) break;
                receiver.accept(text);
            }
        } catch (NoSuchElementException ex) {
            input.close();
        }
    }

    public void setReceiver(Consumer<String> receiver) {
        this.receiver = receiver;
    }
}
