package ru.mikhailsv;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class Node extends Peer implements Runnable {
    private Optional<Coordinator> coordinatorInstance = Optional.empty();
    private final Semaphore workerAccountant = new Semaphore(config.getBatchSize(), /*fair=*/true);
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private Thread mainWorkerThread;


    Node(int nodeId, TokenRingConfig config, List<DataPackage> inboxContent) {
        super(nodeId, config, inboxContent);
        if (amICoordinator()) {
            coordinatorInstance = Optional.of(new Coordinator(config));
        }
    }

    private boolean amICoordinator() {
        return nodeId == config.getCoordinatorId();
    }

    private boolean isDestinationReached(int desired) {
        return desired == nodeId;
    }

    // In ThreadPool
    private void maybeFinishProcessing() {
        Coordinator coordinator = coordinatorInstance.orElseThrow();
        if (coordinator.gotAllMessages()) {
            coordinator.countAverageTime();
            Objects.requireNonNull(mainWorkerThread);
            mainWorkerThread.interrupt();
        }
    }

    // In ThreadPool
    private void saveOnCoordinator(DataPackage msg) {
        msg.commitTravelTime();
        if (amICoordinator()) {
            coordinatorInstance.orElseThrow().saveMsg(msg);
            maybeFinishProcessing();
        } else {
            sendToCoordinator(msg);
        }
    }

    // In ThreadPool
    private void forwardNext(DataPackage msg) {
        if (isDestinationReached(msg.getDestinationNode())) {
            saveOnCoordinator(msg);
        } else {
            sendNext(msg);
        }
    }

    // In ThreadPool
    private void processMsg(DataPackage msg) {
        try {
            Thread.sleep(config.getSleepTime());
        } catch (InterruptedException e) {
            logger.warning(String.format("[Node-%d worker]: Thread was interrupted while processing message", nodeId));
        }
        forwardNext(msg);
    }

    /**
     * Method that polls node inbox.
     * - If it is being run on coordinator node than it is terminated after last message has been saved to coordinator.
     * Some worker thread sends mainThread an interrupt when he ended up to process the last message.
     * - If not, it runs till it is interrupted by RingProcessor, which knows that coordinator had
     * saved all the messages already.
     */
    @Override
    public void run() {
        mainWorkerThread = Thread.currentThread();
        while (true) {
            try {
                workerAccountant.acquire();
                DataPackage msg = receiveBlocking();
                executor.submit(() -> {
                    processMsg(msg);
                    workerAccountant.release();
                });
            } catch (InterruptedException e) {
                logger.info(String.format("[Node-%d]: Finishing processing messages", nodeId));
                executor.shutdown();
                break;
            }
        }
    }
}
