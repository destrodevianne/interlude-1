//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.model.reward;

import java.util.ArrayList;
import java.util.List;
import l2.commons.math.SafeMath;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.Player;
import l2.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.ArrayUtils;

public class RewardData implements Cloneable {
  private ItemTemplate _item;
  private boolean _notRate;
  private boolean _onePassOnly;
  private long _mindrop;
  private long _maxdrop;
  private double _chance;
  private double _chanceInGroup;

  public RewardData(int itemId) {
    this._item = ItemHolder.getInstance().getTemplate(itemId);
    this.setNotRate(this._item.isArrow() || Config.NO_RATE_EQUIPMENT && this._item.isEquipment() || Config.NO_RATE_KEY_MATERIAL && this._item.isKeyMatherial() || Config.NO_RATE_RECIPES && this._item.isRecipe(), ArrayUtils.contains(Config.NO_RATE_ITEMS, itemId));
  }

  public RewardData(int itemId, long min, long max, double chance) {
    this(itemId);
    this._mindrop = min;
    this._maxdrop = max;
    this._chance = chance;
  }

  public boolean notRate() {
    return this._notRate;
  }

  public boolean onePassOnly() {
    return this._onePassOnly;
  }

  public void setNotRate(boolean notRate, boolean onePassOnly) {
    this._notRate = notRate || onePassOnly;
    this._onePassOnly = onePassOnly;
  }

  public int getItemId() {
    return this._item.getItemId();
  }

  public ItemTemplate getItem() {
    return this._item;
  }

  public long getMinDrop() {
    return this._mindrop;
  }

  public void setMinDrop(long mindrop) {
    this._mindrop = mindrop;
  }

  public long getMaxDrop() {
    return this._maxdrop;
  }

  public void setMaxDrop(long maxdrop) {
    this._maxdrop = maxdrop;
  }

  public double getChance() {
    return this._chance;
  }

  public void setChance(double chance) {
    this._chance = chance;
  }

  public double getChanceInGroup() {
    return this._chanceInGroup;
  }

  public void setChanceInGroup(double chance) {
    this._chanceInGroup = chance;
  }

  public String toString() {
    return "ItemID: " + this.getItem() + " Min: " + this.getMinDrop() + " Max: " + this.getMaxDrop() + " Chance: " + this.getChance() / 10000.0D + "%";
  }

  public RewardData clone() {
    return new RewardData(this.getItemId(), this.getMinDrop(), this.getMaxDrop(), this.getChance());
  }

  public boolean equals(Object o) {
    if (o instanceof RewardData) {
      RewardData drop = (RewardData)o;
      return drop.getItemId() == this.getItemId();
    } else {
      return false;
    }
  }

  public List<RewardItem> roll(Player player, double mod) {
    double rate = 1.0D;
    if (this._item.isAdena()) {
      rate = Config.RATE_DROP_ADENA * player.getRateAdena();
    } else {
      rate = Config.RATE_DROP_ITEMS * (player != null ? player.getRateItems() : 1.0D);
    }

    return this.roll(rate * mod);
  }

  public List<RewardItem> roll(double rate) {
    double mult = Math.ceil(rate);
    List<RewardItem> ret = new ArrayList(1);
    RewardItem t = null;

    for(int n = 0; (double)n < mult; ++n) {
      if ((double)Rnd.get(1000000) <= this._chance * Math.min(rate - (double)n, 1.0D)) {
        long count;
        if (this.getMinDrop() >= this.getMaxDrop()) {
          count = this.getMinDrop();
        } else {
          count = Rnd.get(this.getMinDrop(), this.getMaxDrop());
        }

        if (t == null) {
          ret.add(t = new RewardItem(this._item.getItemId()));
          t.count = count;
        } else {
          t.count = SafeMath.addAndLimit(t.count, count);
        }
      }
    }

    return ret;
  }
}
