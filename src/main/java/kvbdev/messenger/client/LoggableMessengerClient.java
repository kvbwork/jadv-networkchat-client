package kvbdev.messenger.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class LoggableMessengerClient extends MessengerClient {
    protected PrintWriter log;
    protected DateTimeFormatter dateTimeFormatter;

    public LoggableMessengerClient(String host, int port, String logFileName) throws IOException {
        super(host, port);
        this.log = new PrintWriter(new FileOutputStream(logFileName, true));
        this.dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

        setOnMessageListener(text -> {
            System.out.println(text);
            logMessage(text);
        });
    }

    @Override
    public void send(String text) {
        super.send(text);
        if (isCommand(text)) return;
        logMessage(userName + ": " + text);
    }

    protected void logMessage(String text) {
        String str = LocalDateTime.now().format(dateTimeFormatter) + " | " + text;
        log.println(str);
        log.flush();
    }

    protected boolean isCommand(String text) {
        return text.startsWith("/");
    }

    @Override
    public void close() throws IOException {
        super.close();
        log.flush();
        log.close();
    }
}
