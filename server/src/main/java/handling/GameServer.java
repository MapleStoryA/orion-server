package handling;

import handling.session.SocketProvider;
import handling.session.SocketProviderFactory;

@lombok.extern.slf4j.Slf4j
public class GameServer {

    private final SocketProvider socketProvider;
    private final SocketThread socketThread;

    class SocketThread extends Thread {
        SocketProvider socketProvider;
        PacketProcessor.Mode mode;

        public SocketThread(SocketProvider socketProvider, PacketProcessor.Mode mode) {
            this.socketProvider = socketProvider;
            this.mode = mode;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            socketProvider.initSocket(channel, port, mode);
        }
    }

    protected final int channel, port;


    public GameServer(int channel, int port, PacketProcessor.Mode mode) {
        this.channel = channel;
        this.port = port;
        this.socketProvider = SocketProviderFactory.getSocketProvider();
        this.socketThread = new SocketThread(socketProvider, mode);
        this.socketThread.start();
    }

    protected void unbindAll() {
        socketProvider.unbindAll();
    }

    public void onStart() {

    }

    public void shutdown() {
    }

}
