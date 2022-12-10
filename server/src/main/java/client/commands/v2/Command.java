package client.commands.v2;

import client.MapleClient;

public interface Command {

    void execute(MapleClient c, String[] splitted);

    String getTrigger();
}
