package networking;

import networking.packet.PacketProcessor;

public interface SocketProvider {
    void initSocket(int channel, int port, PacketProcessor.Mode mode);

    void shutdown();
}
