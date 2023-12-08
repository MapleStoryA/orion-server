package client;

import java.io.Serializable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MapleDiseaseValueHolder implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private long startTime;
    private long length;
    private MapleDisease disease;

    public MapleDiseaseValueHolder(
            final MapleDisease disease, final long startTime, final long length) {
        this.disease = disease;
        this.startTime = startTime;
        this.length = length;
    }
}
