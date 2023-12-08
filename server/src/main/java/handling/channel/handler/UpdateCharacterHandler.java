package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class UpdateCharacterHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.updatePetAuto();


    }

}
