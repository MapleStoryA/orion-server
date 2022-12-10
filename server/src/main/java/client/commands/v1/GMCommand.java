/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.commands.v1;

import client.MapleClient;
import client.skill.SkillFactory;
import constants.ServerConstants.PlayerGMRank;

@lombok.extern.slf4j.Slf4j
public class GMCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.GM;
    }

    public static class Hide extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            SkillFactory.getSkill(9001004).getEffect(1).applyTo(c.getPlayer());
            return 0;
        }
    }
}
