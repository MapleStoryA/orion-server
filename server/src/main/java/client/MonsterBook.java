package client;

import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.packet.MonsterBookPacket;

@Slf4j
public class MonsterBook implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private final Map<Integer, Integer> cards;
    private boolean changed = false;
    private int specialCard = 0, normalCard = 0, bookLevel = 1;

    public MonsterBook(Map<Integer, Integer> cards) {
        this.cards = cards;

        for (Entry<Integer, Integer> card : cards.entrySet()) {
            if (GameConstants.isSpecialCard(card.getKey())) {
                specialCard += card.getValue();
            } else {
                normalCard += card.getValue();
            }
        }
        calculateLevel();
    }

    public static final MonsterBook loadCards(final int charid) throws SQLException {
        try (var con = DatabaseConnection.getConnection()) {
            var ps = con.prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC");
            ps.setInt(1, charid);
            final ResultSet rs = ps.executeQuery();
            Map<Integer, Integer> cards = new LinkedHashMap<>();
            while (rs.next()) {
                cards.put(rs.getInt("cardid"), rs.getInt("level"));
            }
            rs.close();
            ps.close();
            return new MonsterBook(cards);
        }
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public final int getTotalCards() {
        return specialCard + normalCard;
    }

    public final int getLevelByCard(final int cardid) {
        return cards.get(cardid) == null ? 0 : cards.get(cardid);
    }

    public final void saveCards(final int characterID) throws SQLException {
        if (!changed || cards.size() == 0) {
            return;
        }
        try (var con = DatabaseConnection.getConnection()) {
            var ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
            ps.setInt(1, characterID);
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
                query.append(characterID);
                query.append(",");
                Integer cardId = all.getKey();
                query.append(cardId);
                query.append(",");
                Integer cardLevel = all.getValue();
                query.append(cardLevel);
                query.append(")");
            }
            ps = con.prepareStatement(query.toString());
            ps.execute();
            ps.close();
        }
    }

    private void calculateLevel() {
        int Size = normalCard + specialCard;
        bookLevel = 8;

        for (int i = 0; i < 8; i++) {
            if (Size <= GameConstants.getBookLevel(i)) {
                bookLevel = (i + 1);
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

    public final void addCharInfoPacket(final int bookCover, final OutPacket packet) {
        packet.writeInt(bookLevel);
        packet.writeInt(normalCard);
        packet.writeInt(specialCard);
        packet.writeInt(normalCard + specialCard);
        packet.writeInt(MapleItemInformationProvider.getInstance().getCardMobId(bookCover));
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
                    specialCard += 1;
                } else {
                    normalCard += 1;
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
            specialCard += 1;
        } else {
            normalCard += 1;
        }
        // New card
        cards.put(cardid, 1);
        c.getSession().write(MonsterBookPacket.addCard(false, cardid, 1));
        c.getSession().write(MonsterBookPacket.showGainCard());
        c.getSession().write(MaplePacketCreator.showSpecialEffect(13));
        calculateLevel();
    }
}
