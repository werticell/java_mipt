package ru.mikhailsv.util;

import ru.mikhailsv.DataPackage;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public final class BlockingMessageBuffer<T extends DataPackage> {
    private final LinkedBlockingQueue<Pair<T, Long>> buffer;

    public BlockingMessageBuffer(Collection<T> collection) {
        buffer = collection.stream()
                .map(msg -> new Pair<>(msg, System.nanoTime()))
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
    }

    public T take() throws InterruptedException {
        Pair<T, Long> result = buffer.take();
        T msg = result.getKey();
        msg.increaseBufferTime(System.nanoTime() - result.getValue());
        return msg;
    }

    public void add(T msg) {
        buffer.add(new Pair<>(msg, System.nanoTime()));
    }


}
