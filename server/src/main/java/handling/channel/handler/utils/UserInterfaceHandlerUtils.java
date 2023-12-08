package handling.channel.handler.utils;

import client.MapleClient;
import scripting.EventManager;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class UserInterfaceHandlerUtils {

    public static final void ShipObjectRequest(final int mapid, final MapleClient c) {
        // BB 00 6C 24 05 06 00 - Ellinia
        // BB 00 6E 1C 4E 0E 00 - Leafre

        EventManager em;
        int effect = 3; // 1 = Coming, 3 = going, 1034 = balrog

        switch (mapid) {
            case 101000300: // Ellinia Station >> Orbis
            case 200000111: // Orbis Station >> Ellinia
                em = c.getChannelServer().getEventSM().getEventManager("Boats");
                if (em != null && em.getProperty("docked").equals("true")) {
                    effect = 1;
                }
                break;
            case 200000121: // Orbis Station >> Ludi
            case 220000110: // Ludi Station >> Orbis
                em = c.getChannelServer().getEventSM().getEventManager("Trains");
                if (em != null && em.getProperty("docked").equals("true")) {
                    effect = 1;
                }
                break;
            case 200000151: // Orbis Station >> Ariant
            case 260000100: // Ariant Station >> Orbis
                em = c.getChannelServer().getEventSM().getEventManager("Geenie");
                if (em != null && em.getProperty("docked").equals("true")) {
                    effect = 1;
                }
                break;
            case 240000110: // Leafre Station >> Orbis
            case 200000131: // Orbis Station >> Leafre
                em = c.getChannelServer().getEventSM().getEventManager("Flight");
                if (em != null && em.getProperty("docked").equals("true")) {
                    effect = 1;
                }
                break;
            case 200090010: // During the ride to Orbis
            case 200090000: // During the ride to Ellinia
                em = c.getChannelServer().getEventSM().getEventManager("Boats");
                if (em != null && em.getProperty("haveBalrog").equals("true")) {
                    effect = 1;
                } else {
                    return; // shyt, fixme!
                }
                break;
            default:
                log.info("Unhandled ship object, MapID : " + mapid);
                break;
        }
        c.getSession().write(MaplePacketCreator.boatPacket(effect));
    }
}
