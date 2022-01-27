package ru.mikhailsv;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public final class RingProcessor {
    private final TokenRingConfig config;
    private final Logger logger;

    private final List<Thread> nodeList = new ArrayList<>();

    RingProcessor(TokenRingConfig config) throws IOException {
        this.config = config;
        logger = LoggerSingleton.createInstance(config.getLogFile());
        init();
    }

    private List<DataPackage> generateRandomData(Random rndGenerator) {
        List<DataPackage> result = new ArrayList<>();
        for (int i = 0; i < config.getMsgCountToSend(); ++i) {
            result.add(new DataPackage(rndGenerator.nextInt(config.getNodesCount()),
                    String.format("greatData[%d]", rndGenerator.nextInt(config.getNodesCount() * 3))));
        }
        return result;
    }

    private void init() {
        TcpManager.createInstance();
        Random rndGenerator = new Random();
        for (int peerId = 0; peerId < config.getNodesCount(); ++peerId) {
            Node newNode = new Node(peerId, config, generateRandomData(rndGenerator));
            TcpManager.registerPeer(peerId, newNode);
            nodeList.add(new Thread(newNode));
        }
    }


    private void runNodes() {
        for (Thread thread : nodeList) {
            thread.start();
        }
    }

    private void requestStop() {
        for (Thread thread : nodeList) {
            thread.interrupt();
        }
    }

    private void joinThreads() throws InterruptedException {
        for (Thread thread : nodeList) {
            thread.join();
        }
    }

    private Thread getCoordinatorThread() {
        return nodeList.get(config.getCoordinatorId());
    }

    public void start() throws InterruptedException {
        logger.info(String.format("[RingProcessor]: Starting processing, TokenRing params: "
                        + "[nodesAmount]=%d,\t[msgCountOnNode]=%d,\t[coordinatorId]=%d",
                config.getNodesCount(), config.getMsgCountToSend(), config.getCoordinatorId()));

        runNodes();
        getCoordinatorThread().join(); // ensure Coordinator to end
        requestStop();
        joinThreads();
    }
}
