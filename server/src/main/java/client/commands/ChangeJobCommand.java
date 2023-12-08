package client.commands;

import client.MapleClient;

public class ChangeJobCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] splitted) {
        c.getPlayer().changeJob(Integer.parseInt(splitted[1]));
    }

    @Override
    public String getTrigger() {
        return "job";
    }
}
