package client.commands;

import client.MapleClient;

public interface Command {

    void execute(MapleClient c, String[] args);

    String getTrigger();

    static int getOptionalIntArg(String[] args, int position, int defaultValue) {
        if (args.length > position) {
            try {
                return Integer.parseInt(args[position]);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
