//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;

public class Debug implements IVoicedCommandHandler {
  private final String[] _commandList = new String[]{"debug"};

  public Debug() {
  }

  public String[] getVoicedCommandList() {
    return this._commandList;
  }

  public boolean useVoicedCommand(String command, Player player, String args) {
    if (!Config.ALT_DEBUG_ENABLED) {
      return false;
    } else {
      if (player.isDebug()) {
        player.setDebug(false);
        player.sendMessage(new CustomMessage("voicedcommandhandlers.Debug.Disabled", player, new Object[0]));
      } else {
        player.setDebug(true);
        player.sendMessage(new CustomMessage("voicedcommandhandlers.Debug.Enabled", player, new Object[0]));
      }

      return true;
    }
  }
}
