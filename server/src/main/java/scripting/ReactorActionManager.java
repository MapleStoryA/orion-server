package scripting;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.world.WorldServer;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.MapleItemInformationProvider;
import server.carnival.MapleCarnivalFactory;
import server.carnival.MapleCarnivalFactory.MCSkill;
import server.life.MapleMonster;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.helper.Api;
import tools.helper.Randomizer;

@Slf4j
public class ReactorActionManager extends AbstractPlayerInteraction {

    private final MapleReactor reactor;

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c, reactor.getReactorId(), c.getPlayer().getMapId());
        this.reactor = reactor;
    }

    @Override
    public void spawnNpc(int npcId) {
        spawnNpc(npcId, getPosition());
    }

    // summon one monster on reactor location
    @Override
    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    // summon monsters on reactor location
    @Override
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    @Api
    public void dropItems() {
        dropItems(false, 0, 0, 0, 0);
    }

    @Api
    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }

    private void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        final List<ReactorDropEntry> chances =
                ReactorScriptManager.getInstance().getDrops(reactor.getReactorId());
        final List<ReactorDropEntry> items = new LinkedList<ReactorDropEntry>();
        if (meso) {
            if (Math.random() < (1 / (double) mesoChance)) {
                items.add(new ReactorDropEntry(0, mesoChance, -1));
            }
        }

        int numItems = 0;
        // narrow list down by chances
        final Iterator<ReactorDropEntry> iter = chances.iterator();
        // for (DropEntry d : chances){
        while (iter.hasNext()) {
            boolean success = false;
            ReactorDropEntry d = iter.next();
            if (Math.random() < (1 / (double) d.chance)) {
                if (d.questid <= 0) {
                    success = true;
                } else if (getPlayer().getQuestStatus(d.questid) == 1) {
                    success = true;
                }
                if (success) {
                    numItems++;
                    items.add(d);
                }
            }
        }

        // if a minimum number of drops is required, add meso
        while (items.size() < minItems) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
            numItems++;
        }
        final Point dropPos = reactor.getPosition();

        dropPos.x -= (12 * numItems);

        int range, mesoDrop;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (final ReactorDropEntry d : items) {
            if (d.itemId == 0) {
                range = maxMeso - minMeso;
                mesoDrop = Randomizer.nextInt(range)
                        + minMeso
                                * WorldServer.getInstance()
                                        .getChannel(getClient().getChannel())
                                        .getMesoRate();
                reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, reactor, getPlayer(), false, (byte) 0);
            } else {
                IItem drop;
                if (GameConstants.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                    drop = new Item(d.itemId, (byte) 0, (short) 1, (byte) 0);
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemId));
                }
                reactor.getMap().spawnItemDrop(reactor, getPlayer(), drop, dropPos, false, false);
            }
            dropPos.x += 25;
        }
    }

    @Api
    public void dispelAllMonsters(final int num) { // dispels all mobs, cpq
        final MCSkill skill = MapleCarnivalFactory.getInstance().getGuardian(num);
        if (skill != null) {
            for (MapleMonster mons : getMap().getAllMonstersThreadsafe()) {
                mons.dispelSkill(skill.getSkill());
            }
        }
    }

    @Api
    public void killAll() {
        reactor.getMap().killAllMonsters(true);
    }

    @Api
    public void dropSingleItem(int itemId) {
        Item drop;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemId) != MapleInventoryType.EQUIP) {
            drop = new Item(itemId, (byte) 0, (short) 1, (byte) 0);
        } else {
            drop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
        }
        reactor.getMap().spawnItemDrop(reactor, getPlayer(), drop, reactor.getPosition(), false, false);
    }

    public void killMonster(int monsId) {
        reactor.getMap().killMonster(monsId);
    }

    // returns slightly above the reactor's position for monster spawns
    public Point getPosition() {
        Point pos = reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    public MapleReactor getReactor() {
        return reactor;
    }
}
