//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.utils;

import l2.commons.util.Rnd;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.MapRegionManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.templates.mapregion.RestartArea;
import l2.gameserver.templates.mapregion.RestartPoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeleportUtils {
  public static final Location DEFAULT_RESTART = new Location(17817, 170079, -3530);

  private TeleportUtils() {
  }

  public static Location getRestartLocation(Player player, RestartType restartType) {
    return getRestartLocation(player, player.getLoc(), restartType);
  }

  public static Location getRestartLocation(Player player, Location from, RestartType restartType) {
    Reflection r = player.getReflection();
    if (r != ReflectionManager.DEFAULT) {
      if (r.getCoreLoc() != null) {
        return r.getCoreLoc();
      }

      if (r.getReturnLoc() != null) {
        return r.getReturnLoc();
      }
    }

    Clan clan = player.getClan();
    if (clan != null) {
      if (restartType == RestartType.TO_CLANHALL && clan.getHasHideout() != 0) {
        return ResidenceHolder.getInstance().getResidence(clan.getHasHideout()).getOwnerRestartPoint();
      }

      if (restartType == RestartType.TO_CASTLE && clan.getCastle() != 0) {
        return ResidenceHolder.getInstance().getResidence(clan.getCastle()).getOwnerRestartPoint();
      }
    }

    if (player.getKarma() > 1) {
      if (player.getPKRestartPoint() != null) {
        return player.getPKRestartPoint();
      }
    } else if (player.getRestartPoint() != null) {
      return player.getRestartPoint();
    }

    RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, from);
    if (ra != null) {
      RestartPoint rp = ra.getRestartPoint().get(player.getRace());
      Location restartPoint = Rnd.get(rp.getRestartPoints());
      Location PKrestartPoint = Rnd.get(rp.getPKrestartPoints());
      return player.getKarma() > 1 ? PKrestartPoint : restartPoint;
    } else {
      log.warn("Cannot find restart location from coordinates: " + from + "!");
      return DEFAULT_RESTART;
    }
  }
}
