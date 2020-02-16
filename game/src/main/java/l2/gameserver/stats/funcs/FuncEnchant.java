//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.stats.funcs;

import l2.gameserver.Config;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.item.ItemType;
import l2.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FuncEnchant extends Func {
  public FuncEnchant(Stats stat, int order, Object owner, double value) {
    super(stat, order, owner);
  }

  private int getItemImpliedEnchantLevel(ItemInstance item) {
    if (item == null) {
      return 0;
    } else {
      int enchLvlLim = -1;
      if (item.isArmor()) {
        enchLvlLim = Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ARMOR;
      } else if (item.isWeapon()) {
        enchLvlLim = item.getTemplate().isMageItem() ? Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_MAGE : Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_PHYS;
      } else if (item.isAccessory()) {
        enchLvlLim = Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ACCESSORY;
      }

      int enchant = item.getEnchantLevel();
      if (enchLvlLim < 0) {
        return enchant;
      } else {
        int itemOwnerId = item.getOwnerId();
        Player player = GameObjectsStorage.getPlayer(itemOwnerId);
        return player != null && player.isOlyParticipant() ? Math.min(enchant, enchLvlLim) : enchant;
      }
    }
  }

  public void calc(Env env) {
    ItemInstance item = (ItemInstance)this.owner;
    int enchant = this.getItemImpliedEnchantLevel(item);
    int overenchant = Math.max(0, enchant - 3);
    switch(this.stat) {
      case SHIELD_DEFENCE:
      case MAGIC_DEFENCE:
      case POWER_DEFENCE:
        env.value += (double)(enchant + overenchant * 2);
        return;
      case MAGIC_ATTACK:
        switch(item.getTemplate().getCrystalType().cry) {
          case 0:
          case 1458:
            env.value += (double)(2 * (enchant + overenchant));
            break;
          case 1459:
            env.value += (double)(3 * (enchant + overenchant));
            break;
          case 1460:
            env.value += (double)(3 * (enchant + overenchant));
            break;
          case 1461:
            env.value += (double)(3 * (enchant + overenchant));
            break;
          case 1462:
            env.value += (double)(4 * (enchant + overenchant));
        }

        return;
      case POWER_ATTACK:
        ItemType itemType = item.getItemType();
        boolean isBow = itemType == WeaponType.BOW;
        boolean isDSword = (itemType == WeaponType.DUALFIST || itemType == WeaponType.DUAL || itemType == WeaponType.BIGSWORD || itemType == WeaponType.SWORD) && item.getTemplate().getBodyPart() == 16384;
        switch(item.getTemplate().getCrystalType().cry) {
          case 0:
          case 1458:
            if (isBow) {
              env.value += (double)(4 * (enchant + overenchant));
            } else {
              env.value += (double)(2 * (enchant + overenchant));
            }
            break;
          case 1459:
          case 1460:
            if (isBow) {
              env.value += (double)(6 * (enchant + overenchant));
            } else if (isDSword) {
              env.value += (double)(4 * (enchant + overenchant));
            } else {
              env.value += (double)(3 * (enchant + overenchant));
            }
            break;
          case 1461:
            if (isBow) {
              env.value += (double)(8 * (enchant + overenchant));
            } else if (isDSword) {
              env.value += (double)(5 * (enchant + overenchant));
            } else {
              env.value += (double)(4 * (enchant + overenchant));
            }
            break;
          case 1462:
            if (isBow) {
              env.value += (double)(10 * (enchant + overenchant));
            } else if (isDSword) {
              env.value += (double)(6 * (enchant + overenchant));
            } else {
              env.value += (double)(5 * (enchant + overenchant));
            }
        }

        return;
      case POWER_ATTACK_SPEED:
      case MAGIC_ATTACK_SPEED:
      case MAX_HP:
      case MAX_MP:
      case MAX_CP:
        env.value += 1.5D * (double)(enchant + overenchant);
        return;
      default:
    }
  }
}
