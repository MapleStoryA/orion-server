package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.Randomizer;
import server.maps.MapleReactor;
import tools.data.input.SeekableLittleEndianAccessor;

public class DamageReactorHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final int oid = slea.readInt();
    final int charPos = slea.readInt();
    final short stance = slea.readShort();
    final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);

    if (reactor == null || !reactor.isAlive()) {
      return;
    }
    if (reactor.getReactorId() == 5022000) {
      final String[] texts = {"What? The reactor didn't broke?",
          "Hmm..Maybe I should try and use another way instead.",
          "I wonder if the Antellion Relic will combine with it or not."};
      c.getPlayer().dropMessage(-3, texts[Randomizer.nextInt(texts.length)]);
      return;
    }
    reactor.hitReactor(charPos, stance, c);

  }

}
