package client;

import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MapleDiseaseValueHolder(MapleDisease disease, long startTime, long length) implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
}
