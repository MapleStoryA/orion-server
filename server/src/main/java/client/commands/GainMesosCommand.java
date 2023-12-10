package client.commands;

import client.MapleClient;
import tools.ApiClass;

@ApiClass
class GainMesosCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        if (args.length < 1) {
            sendSyntaxMessage(c);
        }

        c.getPlayer().gainMeso(Integer.valueOf(args[0]), true);
    }

    @Override
    public String getTrigger() {
        return "mesos";
    }

    private void sendSyntaxMessage(MapleClient client) {
        client.getPlayer().dropMessage(5, "[Syntax] !" + getTrigger() + " <Amount>");
    }
}
