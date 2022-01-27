package ru.mikhailsv;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public final class App {
    private static final int NODES_COUNT = 10;

    private App() {
    }

    public static void main(String[] args) {
        try {
            var config = new TokenRingConfig((new Random()).nextInt(NODES_COUNT), 3, 2,
                    NODES_COUNT, new File("logPath"));
            runTokenRing(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTokenRing(TokenRingConfig config) throws IOException, InterruptedException {
        RingProcessor processor = new RingProcessor(config);
        processor.start();
    }
}
