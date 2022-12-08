package handling.session;

import handling.PacketProcessor;

public interface SocketProvider {
    void initSocket(int channel, int port, PacketProcessor.Mode mode);

    void unbindAll();
}
