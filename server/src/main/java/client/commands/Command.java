package client.commands;

import client.MapleClient;

public interface Command {

    void execute(MapleClient c, String[] splitted);

    String getTrigger();

    static int getOptionalIntArg(String[] splitted, int position, int defaultValue) {
        if (splitted.length > position) {
            try {
                return Integer.parseInt(splitted[position]);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
