package client.commands;

import client.MapleClient;
import tools.helper.Api;

@Api
class GainMesosCommand implements Command {
    @Override
    public int execute(MapleClient c, String[] args) {
        if (args.length < 1) {
            sendSyntaxMessage(c);
        }

        c.getPlayer().gainMeso(Integer.valueOf(args[0]), true);
        return 1;
    }

    @Override
    public String getTrigger() {
        return "mesos";
    }

    private void sendSyntaxMessage(MapleClient client) {
        client.getPlayer().dropMessage(5, "[Syntax] !" + getTrigger() + " <Amount>");
    }
}
