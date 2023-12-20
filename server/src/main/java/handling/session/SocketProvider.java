package handling.session;

import handling.packet.PacketProcessor;

public interface SocketProvider {
    void initSocket(int channel, int port, PacketProcessor.Mode mode);

    void shutdown();
}
