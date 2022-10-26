package kvbdev.messenger.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class MessengerClientTest {
    static final String TEST_STRING = "TEST_STRING";
    static final int TEST_LINES_COUNT = 5;
    static long TICK_VALUE = 25;

    static final String TEST_HOST = "127.0.0.1";
    static final int TEST_PORT = 34777;

    private interface TestConsumer extends Consumer<String> {
    }

    ConnectionReader connectionReader;
    ServerSocket serverSocket;
    Socket toClientSocket;
    TestConsumer testConsumer;
    Thread testServerThread;
    MessengerClient sut;


    @BeforeEach
    void setUp() throws IOException {
        testConsumer = mock(TestConsumer.class);
        serverSocket = new ServerSocket(TEST_PORT);

        testServerThread = new Thread(() -> {
            try {
                toClientSocket = serverSocket.accept();
                connectionReader = new ConnectionReader(toClientSocket.getInputStream(), testConsumer);
                connectionReader.start();
            } catch (IOException ignored) {
            }
        });
        testServerThread.start();

        sut = new MessengerClient(TEST_HOST, TEST_PORT);
    }

    @AfterEach
    void tearDown() throws IOException {
        testServerThread.interrupt();
        testServerThread = null;
        connectionReader.interrupt();
        connectionReader = null;
        sut.close();
        sut = null;
        testConsumer = null;
        serverSocket.close();
        serverSocket = null;
        toClientSocket.close();
        toClientSocket = null;
    }

    @Test
    void setOnMessageListener_success() throws InterruptedException, IOException {
        Thread.sleep(TICK_VALUE);

        String testLines = (TEST_STRING + "\n").repeat(TEST_LINES_COUNT);
        sut.setOnMessageListener(testConsumer);

        try (OutputStream toClient = toClientSocket.getOutputStream()) {
            toClient.write(testLines.getBytes());
            Thread.sleep(TICK_VALUE);
        }
        verify(testConsumer, times(TEST_LINES_COUNT)).accept(eq(TEST_STRING));
    }

    @Test
    void send_success() throws InterruptedException {
        sut.send(TEST_STRING);
        Thread.sleep(TICK_VALUE);
        verify(testConsumer, times(1)).accept(eq(TEST_STRING));
    }

    @Test
    void keepAlive_send_success() throws InterruptedException {
        String expected = "/echo";
        sut.keepAlive();
        Thread.sleep(TICK_VALUE);
        verify(testConsumer, atLeastOnce()).accept(eq(expected));
    }

    @Test
    void login_send_success() throws InterruptedException {
        String expected = "/login " + TEST_STRING;
        sut.login(TEST_STRING);
        Thread.sleep(TICK_VALUE);
        verify(testConsumer, times(1)).accept(eq(expected));
    }

    @Test
    void printUsers_send_success() throws InterruptedException {
        String expected = "/users";
        sut.printUsers();
        Thread.sleep(TICK_VALUE);
        verify(testConsumer, times(1)).accept(eq(expected));
    }

    @Test
    void isClosed_after_closed_success() throws IOException {
        sut.close();
        assertThat(sut.isClosed(), is(true));
    }

    @Test
    void isClosed_when_socket_is_closed_success() throws IOException {
        sut.socket.close();
        assertThat(sut.isClosed(), is(true));
    }

    @Test
    void isClosed_when_input_closed_success() throws IOException {
        sut.socket.shutdownInput();
        assertThat(sut.isClosed(), is(true));
    }

    @Test
    void close_success() throws IOException {
        sut.close();
    }
}