package ru.mikhailsv;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.function.ToLongFunction;
import java.util.logging.Logger;

/**
 * Thread safe instance of coordinator role.
 * It is made thread safe in order to write to it concurrently from
 * different worker threads that are being executed on Node.
 * In the end of work it counts and logs average time for msg to reach
 * the destination.
 */
public final class Coordinator {
    private final ConcurrentLinkedQueue<DataPackage> data = new ConcurrentLinkedQueue<>();
    private final Logger logger = LoggerSingleton.getLogger();
    private final AtomicInteger msgId = new AtomicInteger(0);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final TokenRingConfig config;

    Coordinator(TokenRingConfig config) {
        this.config = config;
    }

    public void saveMsg(DataPackage msg) {
        data.add(msg);
        logger.info(String.format("[Coordinator]: Saved new message [Id]=%d, [MsgData]=%s",
                msgId.incrementAndGet(), msg.getData()));
    }

    public boolean gotAllMessages() {
        return data.size() == config.getMsgCountToSend() * config.getNodesCount();
    }


    private long averageTimeImpl(ToLongFunction<DataPackage> func, ToLongFunction<Integer> converter) {
        double avgTime = data.stream().mapToLong(func).average().orElseThrow();
        return converter.applyAsLong((int) avgTime);
    }

    private void countNetDelay() {
        logger.info(String.format("[Coordinator]: Finished processing, [averageTime(sec)]=%d",
                averageTimeImpl(DataPackage::getTime, TimeUnit.NANOSECONDS::toSeconds)));
    }

    private void countBufferDelay() {
        logger.info(String.format("[Coordinator]: Finished processing, [averageBufferTime(millis)]=%d",
                averageTimeImpl(DataPackage::getBufferTime, TimeUnit.NANOSECONDS::toMillis)));
    }

    /**
     * Method must be called by worker that processes last message.
     */
    public void countAverageTime() {
        assert gotAllMessages();
        if (finished.compareAndSet(false, true)) {
            countNetDelay();
            countBufferDelay();
        }

    }
}
