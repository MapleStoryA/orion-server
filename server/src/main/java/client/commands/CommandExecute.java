package client.commands;

import client.MapleClient;
import constants.ServerConstants;

public abstract class CommandExecute {


    public abstract int execute(MapleClient c, String[] splitted);

    public ServerConstants.CommandType getType() {
        return ServerConstants.CommandType.NORMAL;
    }

}