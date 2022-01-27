package ru.mikhailsv;

import lombok.Data;

import java.io.File;

@Data
public final class TokenRingConfig {
    /**
     * Id of node that supposed to be a coordinator
     */
    private final int coordinatorId;
    /**
     * Amount of data packages that can be processed concurrently on one Node
     */
    private final int batchSize;
    /**
     * Amount of data packages that will be initialised at the start of a node
     * and sent to random peers via TokenRing
     */
    private final int msgCountToSend;
    /**
     * Number of nodes in TokenRing
     */
    private final int nodesCount;
    /**
     * Name of a file to write logs
     */
    private final File logFile;
    /**
     * Each message is processed for sleepTime millis
     */
    private final long sleepTime = 1;
}
