package scripting.v1.base;

import client.MapleCharacter;
import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.event.GameEventManager;
import tools.Scripting;

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

    @Scripting
    public MapleClient getClient() {
        return client;
    }

    @Scripting
    public MapleCharacter getPlayer() {
        return this.player;
    }

    @Scripting
    public void sendPacket(byte[] packet) {
        dispatcher.dispatch(client, packet);
    }

    @Scripting
    public void broadcastPacket(byte[] packet) {
        player.getMap().broadcastMessage(packet);
    }

    @Scripting
    public void debug(String text) {
        log.info(text);
    }

    @Scripting
    public int random(int min, int max) {
        Random random = new Random();
        int randomNumber = random.nextInt(max + 1 - min) + min;
        return randomNumber;
    }

    public GameEventManager getGameEventManager(){
        return player.getChannelServer().getGameEventManager();
    }

}
