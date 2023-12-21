package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.MaplePortal;
import tools.MaplePacketCreator;
import tools.packet.MapleUserPackets;

@Slf4j
public class MapleDoor extends AbstractMapleMapObject {

    private final WeakReference<MapleCharacter> owner;
    private final MapleMap town;
    private final MapleMap target;
    private final int skillId;
    private final int ownerId;
    private final Point targetPosition;
    private MaplePortal townPortal;

    public MapleDoor(final MapleCharacter owner, final Point targetPosition, final int skillId) {
        super();
        this.owner = new WeakReference<>(owner);
        this.ownerId = owner.getId();
        this.target = owner.getMap();
        this.targetPosition = targetPosition;
        setPosition(this.targetPosition);
        this.town = this.target.getReturnMap();
        this.townPortal = getFreePortal();
        this.skillId = skillId;
    }

    public MapleDoor(final MapleDoor origDoor) {
        super();
        this.owner = new WeakReference<>(origDoor.owner.get());
        this.town = origDoor.town;
        this.townPortal = origDoor.townPortal;
        this.target = origDoor.target;
        this.targetPosition = origDoor.targetPosition;
        this.townPortal = origDoor.townPortal;
        this.skillId = origDoor.skillId;
        this.ownerId = origDoor.ownerId;
        setPosition(townPortal.getPosition());
    }

    public final int getSkill() {
        return skillId;
    }

    public final int getOwnerId() {
        return ownerId;
    }

    private final MaplePortal getFreePortal() {
        final List<MaplePortal> freePortals = new ArrayList<>();

        for (final MaplePortal port : town.getPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        freePortals.sort(new Comparator<>() {

            @Override
            public int compare(final MaplePortal o1, final MaplePortal o2) {
                if (o1.getId() < o2.getId()) {
                    return -1;
                } else if (o1.getId() == o2.getId()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (final MapleMapObject obj : town.getAllDoorsThreadsafe()) {
            final MapleDoor door = (MapleDoor) obj;
            /// hmm
            if (door.getOwner() != null
                    && door.getOwner().getParty() != null
                    && getOwner() != null
                    && getOwner().getParty() != null
                    && getOwner().getParty().getMemberById(door.getOwnerId()) != null) {
                freePortals.remove(door.getTownPortal());
            }
        }
        if (freePortals.size() <= 0) {
            return null;
        }
        return freePortals.iterator().next();
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
        if (getOwner() == null) {
            return;
        }
        if (target.getId() == client.getPlayer().getMapId()
                || getOwnerId() == client.getPlayer().getId()
                || (getOwner() != null
                        && getOwner().getParty() != null
                        && getOwner()
                                        .getParty()
                                        .getMemberById(client.getPlayer().getId())
                                != null)) {
            client.getSession()
                    .write(MaplePacketCreator.spawnDoor(
                            getOwnerId(),
                            town.getId() == client.getPlayer().getMapId() ? townPortal.getPosition() : targetPosition,
                            true));
            if (getOwner() != null
                    && getOwner().getParty() != null
                    && (getOwnerId() == client.getPlayer().getId()
                            || getOwner()
                                            .getParty()
                                            .getMemberById(client.getPlayer().getId())
                                    != null)) {
                client.getSession()
                        .write(MapleUserPackets.partyPortal(town.getId(), target.getId(), skillId, targetPosition));
            }
            client.getSession()
                    .write(MaplePacketCreator.spawnPortal(town.getId(), target.getId(), skillId, targetPosition));
        }
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
        if (getOwner() == null) {
            return;
        }
        if (target.getId() == client.getPlayer().getMapId()
                || getOwnerId() == client.getPlayer().getId()
                || (getOwner() != null
                        && getOwner().getParty() != null
                        && getOwner()
                                        .getParty()
                                        .getMemberById(client.getPlayer().getId())
                                != null)) {
            if (getOwner().getParty() != null
                    && (getOwnerId() == client.getPlayer().getId()
                            || getOwner()
                                            .getParty()
                                            .getMemberById(client.getPlayer().getId())
                                    != null)) {
                client.getSession().write(MapleUserPackets.partyPortal(999999999, 999999999, 0, new Point(-1, -1)));
            }
            client.getSession().write(MaplePacketCreator.removeDoor(getOwnerId(), false));
            client.getSession().write(MaplePacketCreator.removeDoor(getOwnerId(), true));
        }
    }

    public final void warp(final MapleCharacter chr, final boolean toTown) {
        if (chr.getId() == getOwnerId()
                || (getOwner() != null
                        && getOwner().getParty() != null
                        && getOwner().getParty().getMemberById(chr.getId()) != null)) {
            if (!toTown) {
                chr.changeMap(target, targetPosition);
            } else {
                chr.changeMap(town, townPortal);
            }
        } else {
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public final MapleCharacter getOwner() {
        return owner.get();
    }

    public final MapleMap getTown() {
        return town;
    }

    public final MaplePortal getTownPortal() {
        return townPortal;
    }

    public final MapleMap getTarget() {
        return target;
    }

    public final Point getTargetPosition() {
        return targetPosition;
    }

    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
