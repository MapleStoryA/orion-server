package client;

import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.output.OutPacket;
import tools.packet.MonsterBookPacket;

@lombok.extern.slf4j.Slf4j
public class MonsterBook implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private final Map<Integer, Integer> cards;
    private boolean changed = false;
    private int SpecialCard = 0, NormalCard = 0, BookLevel = 1;

    public MonsterBook(Map<Integer, Integer> cards) {
        this.cards = cards;

        for (Entry<Integer, Integer> card : cards.entrySet()) {
            if (GameConstants.isSpecialCard(card.getKey())) {

                SpecialCard += card.getValue();
            } else {
                NormalCard += card.getValue();
            }
        }
        calculateLevel();
    }

    public static final MonsterBook loadCards(final int charid) throws SQLException {
        var con = DatabaseConnection.getConnection();
        var ps = con.prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC");
        ps.setInt(1, charid);
        final ResultSet rs = ps.executeQuery();
        Map<Integer, Integer> cards = new LinkedHashMap<Integer, Integer>();
        int cardid, level;

        while (rs.next()) {
            cards.put(rs.getInt("cardid"), rs.getInt("level"));
        }
        rs.close();
        ps.close();
        con.close();
        return new MonsterBook(cards);
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public final int getTotalCards() {
        return SpecialCard + NormalCard;
    }

    public final int getLevelByCard(final int cardid) {
        return cards.get(cardid) == null ? 0 : cards.get(cardid);
    }

    public final void saveCards(final int charid) throws SQLException {
        if (!changed || cards.size() == 0) {
            return;
        }
        var con = DatabaseConnection.getConnection();
        var ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
        ps.setInt(1, charid);
        ps.execute();
        ps.close();

        boolean first = true;
        final StringBuilder query = new StringBuilder();

        for (final Entry<Integer, Integer> all : cards.entrySet()) {
            if (first) {
                first = false;
                query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
            } else {
                query.append(",(DEFAULT,");
            }
            query.append(charid);
            query.append(",");
            query.append(all.getKey()); // Card ID
            query.append(",");
            query.append(all.getValue()); // Card level
            query.append(")");
        }
        ps = con.prepareStatement(query.toString());
        ps.execute();
        ps.close();
        con.close();
    }

    private final void calculateLevel() {
        int Size = NormalCard + SpecialCard;
        BookLevel = 8;

        for (int i = 0; i < 8; i++) {
            if (Size <= GameConstants.getBookLevel(i)) {
                BookLevel = (i + 1);
                break;
            }
        }
    }

    public final void addCardPacket(final OutPacket packet) {
        packet.writeShort(cards.size());

        for (Entry<Integer, Integer> all : cards.entrySet()) {
            packet.writeShort(GameConstants.getCardShortId(all.getKey())); // Id
            packet.write(all.getValue()); // Level
        }
    }

    public final void addCharInfoPacket(final int bookcover, final OutPacket packet) {
        packet.writeInt(BookLevel);
        packet.writeInt(NormalCard);
        packet.writeInt(SpecialCard);
        packet.writeInt(NormalCard + SpecialCard);
        packet.writeInt(MapleItemInformationProvider.getInstance().getCardMobId(bookcover));
    }

    public final void updateCard(final MapleClient c, final int cardid) {
        c.getSession().write(MonsterBookPacket.changeCover(cardid));
    }

    public final void addCard(final MapleClient c, final int cardid) {
        changed = true;
        c.getPlayer()
                .getMap()
                .broadcastMessage(
                        c.getPlayer(),
                        MonsterBookPacket.showForeginCardEffect(c.getPlayer().getId()),
                        false);

        if (cards.containsKey(cardid)) {
            final int levels = cards.get(cardid);
            if (levels >= 5) {
                c.getSession().write(MonsterBookPacket.addCard(true, cardid, levels));
            } else {
                if (GameConstants.isSpecialCard(cardid)) {
                    SpecialCard += 1;
                } else {
                    NormalCard += 1;
                }
                c.getSession().write(MonsterBookPacket.addCard(false, cardid, levels));
                c.getSession().write(MonsterBookPacket.showGainCard());
                c.getSession().write(MaplePacketCreator.showSpecialEffect(13));
                cards.put(cardid, levels + 1);
                calculateLevel();
            }
            return;
        }
        if (GameConstants.isSpecialCard(cardid)) {
            SpecialCard += 1;
        } else {
            NormalCard += 1;
        }
        // New card
        cards.put(cardid, 1);
        c.getSession().write(MonsterBookPacket.addCard(false, cardid, 1));
        c.getSession().write(MonsterBookPacket.showGainCard());
        c.getSession().write(MaplePacketCreator.showSpecialEffect(13));
        calculateLevel();
    }
}
