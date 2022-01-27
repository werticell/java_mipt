package ru.mikhailsv;

import ru.mikhailsv.util.BlockingMessageBuffer;

import java.util.List;
import java.util.logging.Logger;


public class Peer {
    protected final int nodeId;
    protected final TokenRingConfig config;
    protected final Logger logger = LoggerSingleton.getLogger();

    protected BlockingMessageBuffer<DataPackage> inbox;

    Peer(int nodeId, TokenRingConfig config, List<DataPackage> inboxContent) {
        this.nodeId = nodeId;
        this.config = config;
        inbox = new BlockingMessageBuffer<>(inboxContent);
    }

    public final BlockingMessageBuffer<DataPackage> getInbox() {
        return inbox;
    }

    private int nextPeer() {
        return (nodeId + 1) % config.getNodesCount();
    }

    // Send methods
    private void sendImpl(Integer peerId, DataPackage msg) {
        TcpManager.send(peerId, msg);
    }

    protected final void sendNext(DataPackage msg) {
        sendImpl(nextPeer(), msg);
    }

    protected final void sendToCoordinator(DataPackage msg) {
        sendImpl(nextPeer(), msg.newDestinationCopy(config.getCoordinatorId()));
    }

    // Receive methods
    protected final DataPackage receiveBlocking() throws InterruptedException {
        return inbox.take();
    }

}
