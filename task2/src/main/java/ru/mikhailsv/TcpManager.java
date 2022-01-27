package ru.mikhailsv;

import java.util.HashMap;
import java.util.Objects;


public final class TcpManager {
    private static Instance instance;

    private TcpManager() {
    }

    private static final class Instance {
        private final HashMap<Integer, Node> routes = new HashMap<>();

        /**
         * Not thread safe registry for TCP clients. It assumes that all clients are registered by single thread.
         */
        public void registerPeer(Integer peerId, Node node) {
            routes.put(peerId, node);
        }

        public void send(Integer peerId, DataPackage msg) {
            assert routes.containsKey(peerId);
            routes.get(peerId).getInbox().add(msg);
        }

        public DataPackage receive() {
            return null; // TODO
        }
    }

    /**
     * Not thread safe singleton object creation.
     */
    public static void createInstance() {
        if (Objects.isNull(instance)) {
            instance = new Instance();
        }
    }

    public static void registerPeer(Integer peerId, Node node) {
        instance.registerPeer(peerId, node);
    }


    public static void send(Integer peerId, DataPackage msg) {
        Objects.requireNonNull(instance);
        instance.send(peerId, msg);
    }

    public static DataPackage receive() {
        Objects.requireNonNull(instance);
        return instance.receive();
    }
}
