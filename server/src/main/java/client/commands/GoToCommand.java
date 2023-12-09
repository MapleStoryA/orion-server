package client.commands;

import client.MapleClient;
import java.util.HashMap;
import server.MaplePortal;
import server.maps.MapleMap;

public class GoToCommand implements Command {

    private static final HashMap<String, Integer> listOfMaps = new HashMap<>();

    static {
        listOfMaps.put("gmmap", 180000000);
        listOfMaps.put("southperry", 2000000);
        listOfMaps.put("amherst", 1010000);
        listOfMaps.put("henesys", 100000000);
        listOfMaps.put("ellinia", 101000000);
        listOfMaps.put("perion", 102000000);
        listOfMaps.put("kerning", 103000000);
        listOfMaps.put("lithharbour", 104000000);
        listOfMaps.put("sleepywood", 105040300);
        listOfMaps.put("florina", 110000000);
        listOfMaps.put("orbis", 200000000);
        listOfMaps.put("happyville", 209000000);
        listOfMaps.put("elnath", 211000000);
        listOfMaps.put("ludibrium", 220000000);
        listOfMaps.put("aquaroad", 230000000);
        listOfMaps.put("leafre", 240000000);
        listOfMaps.put("mulung", 250000000);
        listOfMaps.put("herbtown", 251000000);
        listOfMaps.put("omegasector", 221000000);
        listOfMaps.put("koreanfolktown", 222000000);
        listOfMaps.put("newleafcity", 600000000);
        listOfMaps.put("sharenian", 990000000);
        listOfMaps.put("pianus", 230040420);
        listOfMaps.put("horntail", 240060200);
        listOfMaps.put("chorntail", 240060201);
        listOfMaps.put("mushmom", 100000005);
        listOfMaps.put("griffey", 240020101);
        listOfMaps.put("manon", 240020401);
        listOfMaps.put("zakum", 280030000);
        listOfMaps.put("czakum", 280030001);
        listOfMaps.put("papulatus", 220080001);
        listOfMaps.put("showatown", 801000000);
        listOfMaps.put("zipangu", 800000000);
        listOfMaps.put("ariant", 260000100);
        listOfMaps.put("nautilus", 120000000);
        listOfMaps.put("boatquay", 541000000);
        listOfMaps.put("malaysia", 550000000);
        listOfMaps.put("taiwan", 740000000);
        listOfMaps.put("thailand", 500000000);
        listOfMaps.put("erev", 130000000);
        listOfMaps.put("ellinforest", 300000000);
        listOfMaps.put("kampung", 551000000);
        listOfMaps.put("singapore", 540000000);
        listOfMaps.put("amoria", 680000000);
        listOfMaps.put("timetemple", 270000000);
        listOfMaps.put("pinkbean", 270050100);
        listOfMaps.put("peachblossom", 700000000);
        listOfMaps.put("fm", 910000000);
        listOfMaps.put("freemarket", 910000000);
        listOfMaps.put("oxquiz", 109020001);
        listOfMaps.put("ola", 109030101);
        listOfMaps.put("fitness", 109040000);
        listOfMaps.put("snowball", 109060000);
        listOfMaps.put("cashmap", 741010200);
        listOfMaps.put("golden", 950100000);
        listOfMaps.put("phantom", 610010000);
        listOfMaps.put("cwk", 610030000);
        listOfMaps.put("rien", 140000000);
    }

    @Override
    public void execute(MapleClient c, String[] args) {
        if (args.length < 1) {
            c.getPlayer().dropMessage(6, "Syntax: !goto <mapname>");
        } else {
            String mapId = args[0];
            if (listOfMaps.containsKey(mapId)) {
                MapleMap target = c.getChannelServer().getMapFactory().getMap(listOfMaps.get(mapId));
                MaplePortal targetPortal = target.getPortal(0);
                c.getPlayer().changeMap(target, targetPortal);
            } else {
                if (mapId.equals("locations")) {
                    c.getPlayer().dropMessage(6, "Use !goto <location>. Locations are as follows:");
                    StringBuilder sb = new StringBuilder();
                    for (String s : listOfMaps.keySet()) {
                        sb.append(s).append(", ");
                    }
                    c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
                } else {
                    c.getPlayer()
                            .dropMessage(
                                    6,
                                    "Invalid command syntax - Use !goto <location>. For a list"
                                            + " of locations, use !goto locations.");
                }
            }
        }
    }

    @Override
    public String getTrigger() {
        return "goto";
    }
}
