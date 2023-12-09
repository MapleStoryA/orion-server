package client.commands;

import client.MapleClient;

public class ChangeJobCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        c.getPlayer().changeJob(Integer.parseInt(args[0]));
    }

    @Override
    public String getTrigger() {
        return "job";
    }
}