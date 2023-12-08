package client.commands;

import client.MapleClient;

public interface Command {

    void execute(MapleClient c, String[] splitted);

    String getTrigger();
}
