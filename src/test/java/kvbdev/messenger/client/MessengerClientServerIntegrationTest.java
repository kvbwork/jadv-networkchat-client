package kvbdev.messenger.client;

import kvbdev.messenger.server.MessengerServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class MessengerClientServerIntegrationTest {
    static final String TEST_STRING = "TEST_STRING";
    static long TICK_VALUE = 25;

    static final String TEST_HOST = "127.0.0.1";
    static final int TEST_PORT = 34777;

    static final String USER1_NAME = "TEST_USER1";
    static final String USER2_NAME = "TEST_USER2";
    static final String USER3_NAME = "TEST_USER3";

    private interface TestConsumer extends Consumer<String> {
    }

    MessengerServer messengerServer;

    TestConsumer testConsumer1;
    TestConsumer testConsumer2;
    TestConsumer testConsumer3;

    MessengerClient client1;
    MessengerClient client2;
    MessengerClient client3;

    @BeforeEach
    void setUp() throws IOException {
        messengerServer = new MessengerServer(TEST_PORT);
        messengerServer.start();

        testConsumer1 = mock(TestConsumer.class);
        client1 = new MessengerClient(TEST_HOST, TEST_PORT);
        client1.setOnMessageListener(testConsumer1);

        testConsumer2 = mock(TestConsumer.class);
        client2 = new MessengerClient(TEST_HOST, TEST_PORT);
        client2.setOnMessageListener(testConsumer2);

        testConsumer3 = mock(TestConsumer.class);
        client3 = new MessengerClient(TEST_HOST, TEST_PORT);
        client3.setOnMessageListener(testConsumer3);
    }

    @AfterEach
    void tearDown() throws IOException {
        client1.close();
        client2.close();
        client3.close();

        client1 = null;
        client2 = null;
        client3 = null;

        testConsumer1 = null;
        testConsumer2 = null;
        testConsumer3 = null;

        messengerServer.close();
        messengerServer = null;
    }

    @Test
    void sayAll_success() throws InterruptedException {
        client1.login(USER1_NAME);
        client2.login(USER2_NAME);
        client3.login(USER3_NAME);

        client1.send(TEST_STRING);
        Thread.sleep(TICK_VALUE);

        verify(testConsumer1, never()).accept(anyString());

        verify(testConsumer2, times(1)).accept(contains(USER1_NAME));
        verify(testConsumer2, times(1)).accept(contains(TEST_STRING));

        verify(testConsumer3, times(1)).accept(contains(USER1_NAME));
        verify(testConsumer3, times(1)).accept(contains(TEST_STRING));
    }

    @Test
    void whisper_success() throws InterruptedException {
        client1.login(USER1_NAME);
        client2.login(USER2_NAME);
        client3.login(USER3_NAME);

        String whisper = "@" + USER2_NAME + " " + TEST_STRING;
        client1.send(whisper);
        Thread.sleep(TICK_VALUE);

        verify(testConsumer1, never()).accept(anyString());

        verify(testConsumer2, times(1)).accept(contains(USER1_NAME));
        verify(testConsumer2, times(1)).accept(contains(USER2_NAME));
        verify(testConsumer2, times(1)).accept(contains(TEST_STRING));

        verify(testConsumer3, never()).accept(anyString());
    }

    @Test
    void reconnect_success() throws IOException {
        client1.login(USER1_NAME);
        client2.login(USER2_NAME);
        client3.login(USER3_NAME);

        MessengerClient client4 = new MessengerClient(TEST_HOST, TEST_PORT);
        TestConsumer testConsumer4 = mock(TestConsumer.class);
        client4.setOnMessageListener(testConsumer4);
        client4.login(USER2_NAME);

        String whisper = "@" + USER2_NAME + " " + TEST_STRING;
        client1.send(whisper);

        verify(testConsumer1, never()).accept(anyString());
        verify(testConsumer2, never()).accept(anyString());
        verify(testConsumer3, never()).accept(anyString());

        verify(testConsumer4, times(1)).accept(contains(USER1_NAME));
        verify(testConsumer4, times(1)).accept(contains(USER2_NAME));
        verify(testConsumer4, times(1)).accept(contains(TEST_STRING));
    }

    @Test
    void client_close_after_server_disconnect_success() throws IOException {
        boolean isAliveBefore = !client1.isClosed();
        messengerServer.close();
        client1.keepAlive();
        boolean isAliveAfter = !client1.isClosed();

        assertThat(isAliveBefore, is(true));
        assertThat(isAliveAfter, is(false));
    }

}