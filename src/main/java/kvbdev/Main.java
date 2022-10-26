package kvbdev;

import kvbdev.messenger.client.LoggableMessengerClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        Properties properties = readPropertiesFromFile("client.properties");
        String host = properties.getProperty("host", "127.0.0.1");
        int port = Integer.parseInt(properties.getProperty("port", String.valueOf(13444)));


        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Введите ваше имя: ");
            String userName = scanner.nextLine();

            if (userName == null || userName.isEmpty() || userName.contains(" ")) {
                System.out.println("Имя задано неправильно.");
                return;
            }

            try (LoggableMessengerClient client = new LoggableMessengerClient(host, port, "file.log")) {
                System.out.println("Соединение установлено.");
                System.out.println("Вход как: " + userName);
                client.login(userName);
                System.out.println("Список участников: ");
                client.printUsers();

                while (!client.isClosed()) {
                    String userInput = scanner.nextLine();
                    client.send(userInput);
                    if ("/exit".equals(userInput)) break;
                }

                System.out.println("Завершение сеанса");
            }

        }

    }

    protected static Properties readPropertiesFromFile(String filename) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(filename)) {
            properties.load(in);
        }
        return properties;
    }

}
