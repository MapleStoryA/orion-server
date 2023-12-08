package provider;

import java.awt.*;
import java.awt.image.BufferedImage;
import provider.WzXML.MapleDataType;

@lombok.extern.slf4j.Slf4j
public class MapleDataTool {

    public static String getString(MapleData data) {
        return ((String) data.getData());
    }

    public static String getString(MapleData data, String def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            return ((String) data.getData());
        } else {
            return String.valueOf(data.getData());
        }
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        return getString(data.getChildByPath(path), def);
    }

    public static double getDouble(MapleData data) {
        return ((Double) data.getData()).doubleValue();
    }

    public static float getFloat(MapleData data) {
        return ((Float) data.getData()).floatValue();
    }

    public static float getFloat(MapleData data, float def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return ((Float) data.getData()).floatValue();
        }
    }

    public static int getInt(MapleData data) {
        return ((Integer) data.getData()).intValue();
    }

    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            if (data.getType() == MapleDataType.STRING) {
                return Integer.parseInt(getString(data));
            } else if (data.getType() == MapleDataType.SHORT) {
                return Integer.valueOf(((Short) data.getData()).shortValue());
            } else {
                return ((Integer) data.getData()).intValue();
            }
        }
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    public static int getIntConvert(MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return getInt(data);
        }
    }

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        } else {
            return getInt(d);
        }
    }

    public static int getInt(String path, MapleData data, int def) {
        return getInt(data.getChildByPath(path), def);
    }

    public static int getIntConvert(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            try {
                return Integer.parseInt(getString(d));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getInt(d, def);
        }
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        return ((Point) data.getData());
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        final MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        String path = "";
        MapleDataEntity myData = data;
        while (myData != null) {
            path = myData.getName() + "/" + path;
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }
}
