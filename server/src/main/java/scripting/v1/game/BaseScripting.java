package scripting.v1.game;

import client.MapleCharacter;
import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.game.helper.ScriptingApi;

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


    @ScriptingApi
    public MapleClient getClient() {
        return client;
    }

    @ScriptingApi
    public MapleCharacter getPlayer() {
        return this.player;
    }

    @ScriptingApi
    public void sendPacket(byte[] packet) {
        dispatcher.dispatch(client, packet);
    }

    @ScriptingApi
    public void broadcastPacket(byte[] packet) {
        player.getMap().broadcastMessage(packet);
    }

    @ScriptingApi
    public void debug(String text) {
        log.info(text);
    }

    @ScriptingApi
    public int random(int min, int max) {
        Random random = new Random();
        int randomNumber = random.nextInt(max + 1 - min) + min;
        return randomNumber;
    }
}
