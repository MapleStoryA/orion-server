package provider;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import provider.WzXML.XMLWZFile;

@Slf4j
public class MapleDataProviderFactory {

    private static MapleDataProvider getWZ(Object in, boolean provideImages) {
        if (in instanceof File fileIn) {

            return new XMLWZFile(fileIn);
        }
        throw new IllegalArgumentException("Can't create data provider for input " + in);
    }

    public static MapleDataProvider getDataProvider(Object in) {
        return getWZ(in, false);
    }

    public static MapleDataProvider getImageProvidingDataProvider(Object in) {
        return getWZ(in, true);
    }
}
