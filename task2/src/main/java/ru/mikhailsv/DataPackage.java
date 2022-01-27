package ru.mikhailsv;

import lombok.Data;

@Data
public final class DataPackage {
    private final int destinationNode;
    private final String data;
    /**
     * Stores either time when DataPackage was created or time when DataPackage reached its destination.
     */
    private long time;
    /**
     * Stores total time that package been in buffer waiting to be processed.
     */
    private long bufferTime = 0;

    DataPackage(int destinationNode, String data) {
        this.destinationNode = destinationNode;
        this.data = data;
        time = System.nanoTime();
    }

    DataPackage(int destinationNode, String data, long time, long bufferTime) {
        this.destinationNode = destinationNode;
        this.data = data;
        this.time = time;
        this.bufferTime = bufferTime;
    }

    public DataPackage newDestinationCopy(int newDestination) {
        return new DataPackage(newDestination, data, time, bufferTime);
    }

    /**
     * When performed on DataPackage commits travel time of a package
     * assuming {@link DataPackage#time} to store startTime (when a package was created)
     */
    public void commitTravelTime() {
        time = System.nanoTime() - time;
    }

    public void increaseBufferTime(long delay) {
        bufferTime += delay;
    }

}
