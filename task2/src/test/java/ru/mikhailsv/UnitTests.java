package ru.mikhailsv;

import ru.mikhailsv.util.BlockingMessageBuffer;

import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

public class UnitTests {
    private final File testFile = new File("testLogPath");

    @Test
    void testDataPackage() throws InterruptedException {
        var msg = new DataPackage(42, "testData");
        Thread.sleep(200);
        DataPackage newMsg = msg.newDestinationCopy(137);

        Assertions.assertThat(newMsg.getTime()).isEqualTo(msg.getTime());
        newMsg.commitTravelTime();
        Assertions.assertThat(newMsg.getTime()).isNotEqualTo(msg.getTime());

        var buffer = new BlockingMessageBuffer<>(Collections.singleton(newMsg));
        Thread.sleep(200);
        DataPackage newMsgCopy = buffer.take();
        Assertions.assertThat(newMsgCopy.getBufferTime()).isGreaterThan(0);
    }

    @Test
    void testCoordinator() throws IOException {
        LoggerSingleton.createInstance(testFile);
        TokenRingConfig config = new TokenRingConfig(1, 3, 1,
                2, testFile);
        var coordinator = new Coordinator(config);
        var msg1 = new DataPackage(42, "gd1", 2, 3);
        var msg2 = new DataPackage(24, "gd2", 10, 7);

        coordinator.saveMsg(msg1);
        coordinator.saveMsg(msg2);
        Assertions.assertThat(coordinator.gotAllMessages()).isTrue();

        assertDoesNotThrow(coordinator::countAverageTime);
    }


    @Test
    void testNode() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LoggerSingleton.createInstance(testFile);
        TokenRingConfig config = new TokenRingConfig(1, 3, 1,
                2, testFile);
        var node = new Node(1, config, Collections.emptyList());

        Method method1 = Node.class.getDeclaredMethod("amICoordinator");
        method1.setAccessible(true);
        Assertions.assertThat((boolean) method1.invoke(node)).isTrue();

        Method method2 = Node.class.getDeclaredMethod("isDestinationReached", int.class);
        method2.setAccessible(true);
        Assertions.assertThat((boolean) method2.invoke(node, 1)).isTrue();
    }
}
