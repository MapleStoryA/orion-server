package server.base.shutdown;

public interface ShutdownServerMBean extends Runnable {

    void shutdown();
}
