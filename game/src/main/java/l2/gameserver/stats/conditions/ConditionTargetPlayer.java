//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.stats.conditions;

import l2.gameserver.model.Creature;
import l2.gameserver.stats.Env;

public class ConditionTargetPlayer extends Condition {
  private final boolean _flag;

  public ConditionTargetPlayer(boolean flag) {
    this._flag = flag;
  }

  protected boolean testImpl(Env env) {
    Creature target = env.target;
    return target != null && target.isPlayer() == this._flag;
  }
}
