//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.VillageMasterInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2.gameserver.network.l2.s2c.PledgeStatusChanged;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.utils.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.StringTokenizer;

@Slf4j
public class AdminPledge implements IAdminCommandHandler {
  public AdminPledge() {
  }

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar) {
    AdminPledge.Commands command = (AdminPledge.Commands)comm;
    if (activeChar.getPlayerAccess() != null && activeChar.getPlayerAccess().CanEditPledge && activeChar.getTarget() != null && activeChar.getTarget().isPlayer()) {
      Player target = (Player)activeChar.getTarget();
      if (fullString.startsWith("admin_pledge")) {
        StringTokenizer st = new StringTokenizer(fullString);
        st.nextToken();
        String action = st.nextToken();
        Clan clan;
        if (action.equals("create")) {
          try {
            if (target == null) {
              activeChar.sendPacket(Msg.INVALID_TARGET);
              return false;
            }

            if (target.getPlayer().getLevel() < 10) {
              activeChar.sendPacket(Msg.YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN);
              return false;
            }

            String pledgeName = st.nextToken();
            if (pledgeName.length() > 16) {
              activeChar.sendPacket(Msg.CLAN_NAMES_LENGTH_IS_INCORRECT);
              return false;
            }

            if (!Util.isMatchingRegexp(pledgeName, Config.CLAN_NAME_TEMPLATE)) {
              activeChar.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
              return false;
            }

            clan = ClanTable.getInstance().createClan(target, pledgeName);
            if (clan != null) {
              target.sendPacket(clan.listAll());
              target.sendPacket(new IStaticPacket[]{new PledgeShowInfoUpdate(clan), Msg.CLAN_HAS_BEEN_CREATED});
              target.updatePledgeClass();
              target.sendUserInfo(true);
              return true;
            }

            activeChar.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
            return false;
          } catch (Exception e) {
            log.error("addTestTask: eMessage={}, eClass={}, eCause={}", e.getMessage(), e.getClass(), this.getClass().getSimpleName());
          }
        } else {
          int rep;
          switch (action) {
            case "setlevel":
              if (target.getClan() == null) {
                activeChar.sendPacket(Msg.INVALID_TARGET);
                return false;
              }

              try {
                rep = Integer.parseInt(st.nextToken());
                clan = target.getClan();
                activeChar.sendMessage("You set level " + rep + " for clan " + clan.getName());
                clan.setLevel(rep);
                clan.updateClanInDB();
                if (rep == 5) {
                  target.sendPacket(Msg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
                }

                PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
                PledgeStatusChanged ps = new PledgeStatusChanged(clan);
                Iterator var13 = clan.getOnlineMembers(0).iterator();

                while (var13.hasNext()) {
                  Player member = (Player) var13.next();
                  member.updatePledgeClass();
                  member.sendPacket(new IStaticPacket[]{Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps});
                  member.broadcastUserInfo(true);
                }

                return true;
              } catch (Exception var15) {
              }
              break;
            case "resetcreate":
              if (target.getClan() == null) {
                activeChar.sendPacket(Msg.INVALID_TARGET);
                return false;
              }

              target.getClan().setExpelledMemberTime(0L);
              activeChar.sendMessage("The penalty for creating a clan has been lifted for " + target.getName());
              break;
            case "resetwait":
              target.setLeaveClanTime(0L);
              activeChar.sendMessage("The penalty for leaving a clan has been lifted for " + target.getName());
              break;
            case "addrep":
              try {
                rep = Integer.parseInt(st.nextToken());
                if (target.getClan() == null || target.getClan().getLevel() < 5) {
                  activeChar.sendPacket(Msg.INVALID_TARGET);
                  return false;
                }

                target.getClan().incReputation(rep, false, "admin_manual");
                activeChar.sendMessage("Added " + rep + " clan points to clan " + target.getClan().getName() + ".");
              } catch (NumberFormatException var17) {
                activeChar.sendMessage("Please specify a number of clan points to add.");
              }
              break;
            case "setleader":
              clan = target.getClan();
              if (target.getClan() == null) {
                activeChar.sendPacket(Msg.INVALID_TARGET);
                return false;
              }

              String newLeaderName = null;
              if (st.hasMoreTokens()) {
                newLeaderName = st.nextToken();
              } else {
                newLeaderName = target.getName();
              }

              SubUnit mainUnit = clan.getSubUnit(0);
              UnitMember newLeader = mainUnit.getUnitMember(newLeaderName);
              if (newLeader == null) {
                activeChar.sendPacket(Msg.INVALID_TARGET);
                return false;
              }

              VillageMasterInstance.setLeader(activeChar, clan, mainUnit, newLeader);
              break;
          }
        }
      }

      return false;
    } else {
      return false;
    }
  }

  public Enum[] getAdminCommandEnum() {
    return AdminPledge.Commands.values();
  }

  private static enum Commands {
    admin_pledge;

    private Commands() {
    }
  }
}
