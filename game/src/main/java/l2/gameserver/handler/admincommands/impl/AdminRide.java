//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;

public class AdminRide implements IAdminCommandHandler {
  public AdminRide() {
  }

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar) {
    AdminRide.Commands command = (AdminRide.Commands)comm;
    if (!activeChar.getPlayerAccess().Rider) {
      return false;
    } else {
      switch(command) {
        case admin_ride:
          if (!activeChar.isMounted() && activeChar.getPet() == null) {
            if (wordList.length != 2) {
              activeChar.sendMessage("Incorrect id.");
              return false;
            }

            activeChar.setMount(Integer.parseInt(wordList[1]), 0, 85);
            break;
          }

          activeChar.sendMessage("Already Have a Pet or Mounted.");
          return false;
        case admin_ride_wyvern:
        case admin_wr:
          if (!activeChar.isMounted() && activeChar.getPet() == null) {
            activeChar.setMount(12621, 0, 85);
            break;
          }

          activeChar.sendMessage("Already Have a Pet or Mounted.");
          return false;
        case admin_ride_strider:
        case admin_sr:
          if (activeChar.isMounted() || activeChar.getPet() != null) {
            activeChar.sendMessage("Already Have a Pet or Mounted.");
            return false;
          }

          activeChar.setMount(12526, 0, 85);
          break;
        case admin_unride:
        case admin_ur:
          activeChar.setMount(0, 0, 0);
      }

      return true;
    }
  }

  public Enum[] getAdminCommandEnum() {
    return AdminRide.Commands.values();
  }

  private static enum Commands {
    admin_ride,
    admin_ride_wyvern,
    admin_ride_strider,
    admin_unride,
    admin_wr,
    admin_sr,
    admin_ur;

    private Commands() {
    }
  }
}
