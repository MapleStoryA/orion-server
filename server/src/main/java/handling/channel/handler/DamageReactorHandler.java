package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import server.maps.MapleReactor;
import tools.helper.Randomizer;

@Slf4j
public class DamageReactorHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final int oid = packet.readInt();
        final int charPos = packet.readInt();
        final short stance = packet.readShort();
        final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);

        if (reactor == null || !reactor.isAlive()) {
            return;
        }
        if (reactor.getReactorId() == 5022000) {
            final String[] texts = {
                "What? The reactor didn't broke?",
                "Hmm..Maybe I should try and use another way instead.",
                "I wonder if the Antellion Relic will combine with it or not."
            };
            c.getPlayer().dropMessage(-3, texts[Randomizer.nextInt(texts.length)]);
            return;
        }
        reactor.hitReactor(charPos, stance, c);
    }
}
