package handling.channel;

import client.MapleCharacter;
import client.MapleCharacterHelper;
import handling.world.WorldServer;
import handling.world.helper.CharacterTransfer;
import handling.world.helper.CheaterData;
import handling.world.helper.FindCommand;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.Timer.PingTimer;

@lombok.extern.slf4j.Slf4j
public class PlayerStorage {

    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = mutex.readLock(), wL = mutex.writeLock();
    private final ReentrantReadWriteLock mutex2 = new ReentrantReadWriteLock();
    private final Lock rL2 = mutex2.readLock(), wL2 = mutex2.writeLock();
    private final Map<String, MapleCharacter> nameToChar = new HashMap<String, MapleCharacter>();
    private final Map<Integer, MapleCharacter> idToChar = new HashMap<Integer, MapleCharacter>();
    private final Map<Integer, CharacterTransfer> pendingCharacter = new HashMap<>();
    private final int channel;

    public PlayerStorage(int channel) {
        this.channel = channel;
        // Prune once every 15 minutes
        PingTimer.getInstance().schedule(new PersistingTask(), 900000);
    }

    public final Collection<MapleCharacter> getAllCharacters() {
        rL.lock();
        try {
            return Collections.unmodifiableCollection(idToChar.values());
        } finally {
            rL.unlock();
        }
    }

    public final void registerPlayer(final MapleCharacter chr) {
        wL.lock();
        try {
            nameToChar.put(chr.getName().toLowerCase(), chr);
            idToChar.put(chr.getId(), chr);
        } finally {
            wL.unlock();
        }
        FindCommand.register(chr.getId(), chr.getName(), channel);
        WorldServer.getInstance().registerConnectedClient(chr.getAccountData());
    }

    public final void registerPendingPlayer(final CharacterTransfer chr, final int playerid) {
        wL2.lock();
        try {
            pendingCharacter.put(playerid, chr); // new Pair(System.currentTimeMillis(), chr));
        } finally {
            wL2.unlock();
        }
    }

    public final void deregisterPlayer(final MapleCharacter chr) {
        wL.lock();
        try {
            nameToChar.remove(chr.getName().toLowerCase());
            idToChar.remove(chr.getId());
        } finally {
            wL.unlock();
        }
        FindCommand.forceDeregister(chr.getId(), chr.getName());
    }

    public final void deregisterPlayer(final int idz, final String namez) {
        wL.lock();
        try {
            nameToChar.remove(namez.toLowerCase());
            idToChar.remove(idz);
        } finally {
            wL.unlock();
        }
        FindCommand.forceDeregister(idz, namez);
    }

    public final void deregisterPendingPlayer(final int charid) {
        wL2.lock();
        try {
            pendingCharacter.remove(charid);
        } finally {
            wL2.unlock();
        }
    }

    public final CharacterTransfer getPendingCharacter(final int charid) {
        final CharacterTransfer toreturn;
        rL2.lock();
        try {
            toreturn = pendingCharacter.get(charid); // .right;
        } finally {
            rL2.unlock();
        }
        if (toreturn != null) {
            deregisterPendingPlayer(charid);
        }
        return toreturn;
    }

    public final MapleCharacter getCharacterByName(final String name) {
        rL.lock();
        try {
            return nameToChar.get(name.toLowerCase());
        } finally {
            rL.unlock();
        }
    }

    public final MapleCharacter getCharacterById(final int id) {
        rL.lock();
        try {
            return idToChar.get(id);
        } finally {
            rL.unlock();
        }
    }

    public final int getConnectedClients() {
        return idToChar.size();
    }

    public final List<CheaterData> getCheaters() {
        final List<CheaterData> cheaters = new ArrayList<CheaterData>();

        rL.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (chr.getCheatTracker().getPoints() > 0) {
                    cheaters.add(new CheaterData(
                            chr.getCheatTracker().getPoints(),
                            MapleCharacterHelper.makeMapleReadable(chr.getName())
                                    + " ("
                                    + chr.getCheatTracker().getPoints()
                                    + ") "
                                    + chr.getCheatTracker().getSummary()));
                }
            }
        } finally {
            rL.unlock();
        }
        return cheaters;
    }

    public final List<CheaterData> getReports() {
        final List<CheaterData> cheaters = new ArrayList<CheaterData>();

        rL.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (chr.getReportPoints() > 0) {
                    cheaters.add(new CheaterData(
                            chr.getReportPoints(),
                            MapleCharacterHelper.makeMapleReadable(chr.getName())
                                    + " ("
                                    + chr.getReportPoints()
                                    + ") "
                                    + chr.getReportSummary()));
                }
            }
        } finally {
            rL.unlock();
        }
        return cheaters;
    }

    public final void disconnectAll() {
        disconnectAll(false);
    }

    public final void disconnectAll(final boolean checkGM) {
        wL.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (!chr.isGameMaster() || !checkGM) {
                    chr.getClient().disconnect(false, false, true);
                    chr.getClient().getSession().close();
                    FindCommand.forceDeregister(chr.getId(), chr.getName());
                    itr.remove();
                } else {
                    chr.getClient().getPlayer().saveToDB(false, false);
                }
            }
        } finally {
            wL.unlock();
        }
    }

    public final String getOnlinePlayers(final boolean byGM) {
        final StringBuilder sb = new StringBuilder();

        if (byGM) {
            rL.lock();
            try {
                final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
                while (itr.hasNext()) {
                    sb.append(MapleCharacterHelper.makeMapleReadable(itr.next().getName()));
                    sb.append(", ");
                }
            } finally {
                rL.unlock();
            }
        } else {
            rL.lock();
            try {
                final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
                MapleCharacter chr;
                while (itr.hasNext()) {
                    chr = itr.next();

                    if (!chr.isGameMaster()) {
                        sb.append(MapleCharacterHelper.makeMapleReadable(chr.getName()));
                        sb.append(", ");
                    }
                }
            } finally {
                rL.unlock();
            }
        }
        return sb.toString();
    }

    public final void broadcastPacket(final byte[] data) {
        rL.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            while (itr.hasNext()) {
                itr.next().getClient().getSession().write(data);
            }
        } finally {
            rL.unlock();
        }
    }

    public final void broadcastSmegaPacket(final byte[] data) {
        rL.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (chr.getClient().isLoggedIn() && chr.getSuperMegaEnabled()) {
                    chr.getClient().getSession().write(data);
                }
            }
        } finally {
            rL.unlock();
        }
    }

    public final void broadcastGMPacket(final byte[] data) {
        rL.lock();
        try {
            final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (chr.getClient().isLoggedIn() && chr.isGameMaster()) {
                    chr.getClient().getSession().write(data);
                }
            }
        } finally {
            rL.unlock();
        }
    }

    public class PersistingTask implements Runnable {

        @Override
        public void run() {
            wL2.lock();
            try {
                final long currenttime = System.currentTimeMillis();
                final Iterator<Map.Entry<Integer, CharacterTransfer>> itr =
                        pendingCharacter.entrySet().iterator();

                while (itr.hasNext()) {
                    if (currenttime - itr.next().getValue().getTransferTime() > 40000) { // 40 sec
                        itr.remove();
                    }
                }
                PingTimer.getInstance().schedule(new PersistingTask(), 900000);
            } finally {
                wL2.unlock();
            }
        }
    }
}
