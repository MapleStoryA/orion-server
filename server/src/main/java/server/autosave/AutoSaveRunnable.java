package server.autosave;

import handling.channel.ChannelServer;

public class AutoSaveRunnable implements Runnable {

  private ChannelServer channel;

  public AutoSaveRunnable(ChannelServer channel) {
    super();
    this.channel = channel;
  }

  @Override
  public void run() {
    AutoSaver.getInstance().executeSave(channel);
  }

}
