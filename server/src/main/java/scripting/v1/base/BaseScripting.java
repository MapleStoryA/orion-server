package scripting.v1.base;

import client.MapleCharacter;
import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import tools.ApiClass;

import java.util.Random;

@Slf4j
public class BaseScripting {

    protected final MapleClient client;

    protected final PacketDispatcher dispatcher;

    protected MapleCharacter player;

    protected Object continuation;

    public BaseScripting(MapleClient client) {
        super();
        this.client = client;
        this.dispatcher = new RealPacketDispatcher();
        this.player = client.getPlayer();
    }

    static class RealPacketDispatcher implements PacketDispatcher {

        @Override
        public void dispatch(MapleClient client, byte[] packet) {
            client.sendPacket(packet);
        }
    }

    interface PacketDispatcher {
        void dispatch(MapleClient client, byte[] packet);
    }

    @ApiClass
    public MapleClient getClient() {
        return client;
    }

    @ApiClass
    public MapleCharacter getPlayer() {
        return this.player;
    }

    @ApiClass
    public void sendPacket(byte[] packet) {
        dispatcher.dispatch(client, packet);
    }

    @ApiClass
    public void broadcastPacket(byte[] packet) {
        player.getMap().broadcastMessage(packet);
    }

    @ApiClass
    public void debug(String text) {
        log.info(text);
    }

    @ApiClass
    public int random(int min, int max) {
        Random random = new Random();
        int randomNumber = random.nextInt(max + 1 - min) + min;
        return randomNumber;
    }

}
