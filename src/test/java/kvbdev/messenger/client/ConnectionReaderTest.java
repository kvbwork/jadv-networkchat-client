package kvbdev.messenger.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConnectionReaderTest {

    static final String TEST_STRING = "TEST_STRING";
    static final int TEST_LINES_COUNT = 5;
    static long TICK_VALUE = 25;

    private interface TestConsumer extends Consumer<String> {
    }

    InputStream testInput;
    TestConsumer testConsumer;
    ConnectionReader sut;

    @BeforeEach
    void setUp() {
        String lines = (TEST_STRING + "\n").repeat(TEST_LINES_COUNT);
        testInput = new ByteArrayInputStream((lines).getBytes());
        testConsumer = mock(TestConsumer.class);
        sut = new ConnectionReader(testInput, testConsumer);
        sut.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        sut.interrupt();
        sut = null;
        testConsumer = null;
        testInput.close();
        testInput = null;
    }

    @Test
    void receive_line_success() throws InterruptedException {
        Thread.sleep(TICK_VALUE);
        verify(testConsumer, times(TEST_LINES_COUNT)).accept(eq(TEST_STRING));
    }

    @Test
    void stop_thread_after_input_end_success() throws InterruptedException {
        Thread.sleep(TICK_VALUE);
        assertThat(sut.isAlive(), is(false));
    }
}