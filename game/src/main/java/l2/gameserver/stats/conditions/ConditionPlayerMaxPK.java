//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;

public class ConditionPlayerMaxPK extends Condition {
  private final int _pk;

  public ConditionPlayerMaxPK(int pk) {
    this._pk = pk;
  }

  protected boolean testImpl(Env env) {
    if (env.character.isPlayer()) {
      return ((Player)env.character).getPkKills() <= this._pk;
    } else {
      return false;
    }
  }
}
