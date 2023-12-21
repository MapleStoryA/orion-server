package server.autosave;

import handling.channel.ChannelServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoSaveRunnable implements Runnable {

    private final ChannelServer channel;

    public AutoSaveRunnable(ChannelServer channel) {
        super();
        this.channel = channel;
    }

    @Override
    public void run() {
        AutoSaver.getInstance().executeSave(channel);
    }
}
