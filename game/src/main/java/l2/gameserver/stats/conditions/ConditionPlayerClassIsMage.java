//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;

public class ConditionPlayerClassIsMage extends Condition {
  private final boolean _value;

  public ConditionPlayerClassIsMage(boolean value) {
    this._value = value;
  }

  protected boolean testImpl(Env env) {
    if (env.character == null) {
      return false;
    } else {
      Player player = env.character.getPlayer();
      return player != null && player.isMageClass() == this._value;
    }
  }
}