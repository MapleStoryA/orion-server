package handling.world.respawn;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import client.MapleDiseaseValueHolder;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory;
import client.status.MonsterStatusEffect;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import handling.world.buddy.BuddyManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import tools.MaplePacketCreator;
import tools.packet.PetPacket;

public class RespawnWorker implements Runnable {

    public static final int CHANNELS_PER_THREAD = 3;

    private final List<Integer> cservs = new ArrayList<>(CHANNELS_PER_THREAD);
    private final ArrayList<MonsterStatusEffect> effects = new ArrayList<>();
    private final ArrayList<MapleMapItem> items = new ArrayList<>();
    private final ArrayList<MapleCharacter> chrs = new ArrayList<>();
    private final ArrayList<MapleMonster> mobs = new ArrayList<>();
    private final ArrayList<MapleDiseaseValueHolder> dis = new ArrayList<>();
    private final ArrayList<MapleCoolDownValueHolder> cd = new ArrayList<>();
    private final ArrayList<MaplePet> pets = new ArrayList<>();
    private int numTimes = 0;
    private ArrayList<MapleMap> maps = new ArrayList<>();

    public RespawnWorker(Integer[] chs, int c) {
        StringBuilder s = new StringBuilder("[Respawn Worker] Registered for channels ");
        for (int i = 1; (i <= CHANNELS_PER_THREAD) && (chs.length >= c + i); i++) {
            cservs.add(
                    Integer.valueOf(WorldServer.getInstance().getChannel(c + i).getChannel()));
            s.append(c + i).append(" ");
        }
    }

    @Override
    public void run() {
        numTimes++;
        long now = System.currentTimeMillis();

        for (Integer cser : cservs) {
            final ChannelServer cserv = WorldServer.getInstance().getChannel(cser);
            if (cserv != null && !cserv.hasFinishedShutdown()) {
                maps = cserv.getMapFactory().getAllLoadedMaps(maps);
                for (MapleMap map : maps) {
                    handleMap(map, numTimes, map.getCharactersSize(), now, effects, items, chrs, mobs, dis, cd, pets);
                }
            }
        }
        if (BuddyManager.canPrune(now)) {
            BuddyManager.prepareRemove();
        }
    }

    public static void handleMap(
            final MapleMap map,
            final int numTimes,
            final int size,
            final long now,
            ArrayList<MonsterStatusEffect> effects,
            ArrayList<MapleMapItem> items,
            ArrayList<MapleCharacter> chrs,
            ArrayList<MapleMonster> monsters,
            ArrayList<MapleDiseaseValueHolder> dis,
            ArrayList<MapleCoolDownValueHolder> cd,
            ArrayList<MaplePet> pets) {
        if (map.getItemsSize() > 0) {
            items = map.getAllItemsThreadsafe(items);
            for (MapleMapItem item : items) {
                if (item.shouldExpire(now)) {
                    item.expire(map);
                } else if (item.shouldFFA(now)) {
                    item.setDropType((byte) 2);
                }
            }
        }
        if (map.characterSize() > 0) {
            map.respawn(false, now);
            boolean hurt = map.canHurt(now);
            chrs = map.getCharactersThreadsafe(chrs);
            for (MapleCharacter chr : chrs) {
                handleCooldowns(chr, numTimes, hurt, now, dis, cd, pets);
            }

            if (map.getMobsSize() > 0) {
                monsters = map.getAllMonstersThreadsafe(monsters);
                for (MapleMonster mons : monsters) {
                    if (mons.isAlive() && mons.shouldKill(now)) {
                        map.killMonster(mons);
                    } else if (mons.isAlive() && mons.shouldDrop(now)) {
                        mons.doDropItem(now);
                    }
                }
            }
        }
    }

    public static void handleCooldowns(
            final MapleCharacter chr,
            final int numTimes,
            final boolean hurt,
            final long now,
            ArrayList<MapleDiseaseValueHolder> dis,
            ArrayList<MapleCoolDownValueHolder> cd,
            ArrayList<MaplePet> pets) {
        if (chr.getCooldownSize() > 0) {
            cd = chr.getCooldowns(cd);
            for (MapleCoolDownValueHolder m : cd) {
                if (m.getStartTime() + m.getLength() < now) {
                    final int skil = m.getSkillId();
                    chr.removeCooldown(skil);
                    chr.getClient().getSession().write(MaplePacketCreator.skillCooldown(skil, 0));
                }
            }
        }
        if (chr.isAlive()) {
            if (
            /*(chr.getJob() == 131 || chr.getJob() == 132) && */ chr.canBlood(now)) {
                chr.doDragonBlood();
            }
            if (chr.canRecover(now)) {
                chr.doRecovery();
            }
            if (chr.canFairy(now)) {
                chr.doFairy();
            }
            // if (chr.canFish(now)) { chr.doFish(now); }
        }
        if (chr.getDiseaseSize() > 0) {
            dis = chr.getAllDiseases(dis);
            for (MapleDiseaseValueHolder m : dis) {
                if (m.startTime() + m.length() < now) {
                    chr.dispelDebuff(m.disease());
                }
            }
        }
        if (numTimes % 7 == 0 && chr.getMount() != null && chr.getMount().canTire(now)) {
            chr.getMount().increaseFatigue();
        }
        if (numTimes % 13 == 0) { // we're parsing through the characters anyway (:
            pets = chr.getSummonedPets(pets);
            for (MaplePet pet : pets) {
                if (pet.getPetItemId() == 5000054 && pet.getSecondsLeft() > 0) {
                    pet.setSecondsLeft(pet.getSecondsLeft() - 1);
                    if (pet.getSecondsLeft() <= 0) {
                        chr.unequipPet(pet, true, true);
                        return;
                    }
                }
                int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getPetItemId());
                if (new Random().nextInt(15) > 2) {
                    continue;
                }
                if (newFullness <= 5) {
                    pet.setFullness(15);
                    chr.unequipPet(pet, true, true);
                } else {
                    pet.setFullness(newFullness);
                    chr.getClient()
                            .getSession()
                            .write(PetPacket.updatePet(
                                    pet,
                                    chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition())));
                }
            }
        }
        if (hurt && chr.isAlive()) {
            if (chr.getInventory(MapleInventoryType.EQUIPPED)
                            .findById(chr.getMap().getHPDecProtect())
                    == null) {
                if (chr.getMapId() == 749040100
                        && chr.getInventory(MapleInventoryType.CASH).findById(5451000) == null) { // minidungeon
                    chr.addHP(-chr.getMap().getHPDec());
                } else if (chr.getMapId() != 749040100) {
                    chr.addHP(-(chr.getMap().getHPDec()
                            - (chr.getBuffedValue(MapleBuffStat.HP_LOSS_GUARD) == null
                                    ? 0
                                    : chr.getBuffedValue(MapleBuffStat.HP_LOSS_GUARD)
                                            .intValue())));
                }
            }
        }
    }
}
