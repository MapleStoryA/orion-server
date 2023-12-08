package database;

import client.MapleJob;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.skill.EvanSkillPoints;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import tools.data.output.COutPacket;

@Setter
@Getter
public class CharacterData implements Serializable {
    private int id;
    private int accountId;
    private int world;
    private String name;
    private short level;
    private int exp;
    private short str, dex, luk, int_;
    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int meso;
    private int hpApUsed;
    private int mpApUsed;
    private int job;
    private byte skinColor;
    private int gender; // gender (0 = male, 1 = female)
    private short fame;
    private int hair;
    private int face;
    private int ap;
    private int map;
    private int spawnPoint;
    private int gm;
    private int party;
    private int buddyCapacity;
    private LocalDate createdate;
    private int guildId;
    private int guildRank;
    private int allianceRank;
    private int monsterBookCover;
    private int dojo_pts;
    private int dojoRecord;
    private String pets;
    private int subCategory;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private int marriageId;
    private int familyId;
    private int seniorId;
    private int junior1;
    private int junior2;
    private int currentRep;
    private int totalRep;
    private int occupationId;
    private int occupationEXP;
    private int reborns;
    private int jumpLevel;
    private int jumpExp;
    private int factionId;
    private int gainedMsi;
    private int donatorPoints;
    private int playerAutoReborn;
    private int playerSuperDragon;
    private int agentPoints;
    private int contributedFP;
    private int sp;

    private List<PetData> petData = new ArrayList<>();
    private MapleInventory[] inventory = new MapleInventory[MapleInventoryType.values().length];
    private EvanSkillPoints evanSkillPoints = null;

    public boolean isEvan() {
        return (getJob() == 2001 || getJob() / 100 == 22);
    }

    public boolean isGameMasterJob() {
        return this.getJob() == MapleJob.GM.getId() || this.getJob() == MapleJob.SUPERGM.getId();
    }

    public final void connectData(final COutPacket packet) {
        packet.writeShort(str); // str
        packet.writeShort(dex); // dex
        packet.writeShort(int_); // int
        packet.writeShort(luk); // luk
        packet.writeShort(hp); // hp
        packet.writeShort(maxHp); // maxhp
        packet.writeShort(mp); // mp
        packet.writeShort(maxHp); // maxmp
    }

    public final MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }
}
