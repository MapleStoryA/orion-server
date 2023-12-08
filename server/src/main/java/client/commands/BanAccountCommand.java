package client.commands;

import client.MapleCharacter;
import client.MapleClient;

public class BanAccountCommand implements Command {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MIN_REASON_LENGTH = 2;

    @Override
    public String getTrigger() {
        return "ban";
    }

    @Override
    public void execute(MapleClient client, String[] args) {
        if (args.length < 2) {
            sendSyntaxMessage(client);
            return;
        }

        String playerName = args[0];
        String banReason = args[1];

        if (!isValidLength(playerName, MIN_NAME_LENGTH)) {
            client.getPlayer().dropMessage(6, "Please provide the name of the player.");
            return;
        }

        if (!isValidLength(banReason, MIN_REASON_LENGTH)) {
            client.getPlayer().dropMessage(6, "Please provide the reason for the ban.");
            return;
        }

        banPlayer(client, playerName, banReason);
    }

    private void banPlayer(MapleClient client, String playerName, String banReason) {
        MapleCharacter target = client.getChannelServer().getPlayerStorage().getCharacterByName(playerName);

        if (target != null) {
            banOnlinePlayer(client, target, banReason);
        } else {
            banOfflinePlayer(client, playerName, banReason);
        }
    }

    private void banOnlinePlayer(MapleClient c, MapleCharacter target, String reason) {
        if (c.getPlayer().getGMLevel() > target.getGMLevel() || c.getPlayer().isAdmin()) {
            boolean banSuccess = target.ban(reason, c.getPlayer().isAdmin(), false);
            sendBanMessage(c, target.getName(), banSuccess, false);
        } else {
            c.getPlayer().dropMessage(6, "[" + getTrigger() + "] May not ban GMs...");
        }
    }

    private void banOfflinePlayer(MapleClient client, String playerName, String reason) {
        boolean banSuccess =
                MapleCharacter.ban(playerName, reason, false, client.getPlayer().getGMLevel());
        sendBanMessage(client, playerName, banSuccess, true);
    }

    private boolean isValidLength(String input, int minLength) {
        return input.length() >= minLength;
    }

    private void sendSyntaxMessage(MapleClient client) {
        client.getPlayer().dropMessage(5, "[Syntax] !" + getTrigger() + " <IGN> <Reason>");
    }

    private void sendBanMessage(MapleClient client, String playerName, boolean success, boolean isOffline) {
        String message = "[" + getTrigger() + "] " + (success ? "Successfully " : "Failed to ") + "ban " + playerName;
        if (isOffline) {
            message += " offline.";
        } else {
            message += ".";
        }
        client.getPlayer().dropMessage(6, message);
    }
}
