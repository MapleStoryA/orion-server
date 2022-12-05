/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package client;

import constants.ServerEnvironment;
import database.DatabaseConnection;
import tools.Triple;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapleKeyLayout implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private boolean changed = false;
    private final Map<Integer, Triple<Byte, Integer, Byte>> keymap;

    public MapleKeyLayout() {
        keymap = new HashMap<>();
    }

    public MapleKeyLayout(Map<Integer, Triple<Byte, Integer, Byte>> keys) {
        keymap = keys;
    }

    public final Map<Integer, Triple<Byte, Integer, Byte>> Layout() {
        changed = true;
        return keymap;
    }

    public final void writeData(final MaplePacketLittleEndianWriter mplew) {
        Triple<Byte, Integer, Byte> binding;
        for (int x = 0; x < 90; x++) {
            binding = keymap.get(Integer.valueOf(x));
            if (binding != null) {
                mplew.write(binding.getLeft());
                mplew.writeInt(binding.getMid());
            } else {
                mplew.write(0);
                mplew.writeInt(0);
            }
        }
    }

    public final void saveKeys(final int charid, final MapleCharacter chr) throws SQLException {
        if (!changed || keymap.isEmpty()) {
            return;
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ?");
            ps.setInt(1, charid);
            ps.execute();
            ps.close();

            boolean first = true;
            StringBuilder query = new StringBuilder();
            if (ServerEnvironment.isDebugEnabled()) {
                // System.out.println("Saving key map...");
            }
            for (Entry<Integer, Triple<Byte, Integer, Byte>> keybinding : keymap.entrySet()) {
                int skill = keybinding.getValue().getMid().intValue();
                if (ExcludedKeyMap.fromKeyValue(skill) != null) {
                    continue;
                }
                if (ServerEnvironment.isDebugEnabled()) {
                    // System.out.println("Entry: " + keybinding.getValue());
                }
                if (first) {
                    first = false;
                    query.append("INSERT INTO keymap VALUES (");
                } else {
                    query.append(",(");
                }
                query.append("DEFAULT,");
                query.append(charid).append(",");
                query.append(keybinding.getKey().intValue()).append(",");
                query.append(keybinding.getValue().getLeft().byteValue()).append(",");
                query.append(skill).append(",");
                query.append(keybinding.getValue().getRight().byteValue()).append(")");
            }
            if (ServerEnvironment.isDebugEnabled()) {
                // System.out.println(query);
            }
            ps = con.prepareStatement(query.toString());
            ps.execute();
            con.commit();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("Error while saving key " + ex.getMessage());
            throw ex;
        }

    }

}
