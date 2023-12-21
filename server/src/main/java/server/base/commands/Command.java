package server.base.commands;

import client.MapleClient;

public interface Command {

    int execute(MapleClient c, String[] args);

    default String getTrigger() {
        String clazzName = getClass().getName().toLowerCase();
        int lastDotIndex = clazzName.lastIndexOf('.');
        int dollarIndex = clazzName.indexOf('$');
        if (dollarIndex != -1 && dollarIndex > lastDotIndex) {
            return clazzName.substring(dollarIndex + 1);
        } else {
            return clazzName.substring(lastDotIndex + 1);
        }
    }

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
