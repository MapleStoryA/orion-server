package handling.login;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import provider.MapleData;
import provider.MapleDataTool;
import server.config.ServerConfig;

@Slf4j
public class LoginInformationProvider {

    private static final LoginInformationProvider instance = new LoginInformationProvider();
    protected final List<String> forbiddenName = new ArrayList<>();

    LoginInformationProvider() {
        log.info("Loading LoginInformationProvider :::");
        final MapleData nameData =
                ServerConfig.serverConfig().getDataProvider("wz/Etc").getData("ForbiddenName.img");
        for (final MapleData data : nameData.getChildren()) {
            forbiddenName.add(MapleDataTool.getString(data));
        }
    }

    public static LoginInformationProvider getInstance() {
        return instance;
    }

    public boolean isForbiddenName(final String in) {
        for (final String name : forbiddenName) {
            if (in.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
