package server.shutdown;

public interface ShutdownServerMBean extends Runnable {

    void shutdown();
}
