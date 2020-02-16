//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.tables.ClanTable;

public class AdminClanHall implements IAdminCommandHandler {
  public AdminClanHall() {
  }

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar) {
    AdminClanHall.Commands command = (AdminClanHall.Commands)comm;
    if (!activeChar.getPlayerAccess().CanEditNPC) {
      return false;
    } else {
      ClanHall clanhall = null;
      if (wordList.length > 1) {
        clanhall = (ClanHall)ResidenceHolder.getInstance().getResidence(ClanHall.class, Integer.parseInt(wordList[1]));
      }

      if (clanhall == null) {
        this.showClanHallSelectPage(activeChar);
        return true;
      } else {
        switch(command) {
          case admin_clanhall:
            this.showClanHallSelectPage(activeChar);
            break;
          case admin_clanhallset:
            GameObject target = activeChar.getTarget();
            Player player = activeChar;
            if (target != null && target.isPlayer()) {
              player = (Player)target;
            }

            if (player.getClan() == null) {
              activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            } else {
              clanhall.changeOwner(player.getClan());
            }
            break;
          case admin_clanhalldel:
            clanhall.changeOwner((Clan)null);
            break;
          case admin_clanhallteleportself:
            Zone zone = clanhall.getZone();
            if (zone != null) {
              activeChar.teleToLocation(zone.getSpawn());
            }
        }

        this.showClanHallPage(activeChar, clanhall);
        return true;
      }
    }
  }

  public void showClanHallSelectPage(Player activeChar) {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=268><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
    replyMSG.append("<td width=180><center><font color=\"LEVEL\">Clan Halls:</font></center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
    replyMSG.append("</tr></table><br>");
    replyMSG.append("<table width=268>");
    replyMSG.append("<tr><td width=130>ClanHall Name</td><td width=58>Town</td><td width=80>Owner</td></tr>");
    replyMSG.append("</table>");
    replyMSG.append("</body></html>");
    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public void showClanHallPage(Player activeChar, ClanHall clanhall) {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
    replyMSG.append("<td width=180><center>ClanHall Name</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_clanhall\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<center>");
    replyMSG.append("<br><br><br>ClanHall: " + clanhall.getName() + "<br>");
    replyMSG.append("Location: &^" + clanhall.getId() + ";<br>");
    replyMSG.append("ClanHall Owner: ");
    Clan owner = clanhall.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(clanhall.getOwnerId());
    if (owner == null) {
      replyMSG.append("none");
    } else {
      replyMSG.append(owner.getName());
    }

    replyMSG.append("<br><br><br>");
    replyMSG.append("<table>");
    replyMSG.append("<tr><td><button value=\"Open Doors\" action=\"bypass -h admin_clanhallopendoors " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
    replyMSG.append("<td><button value=\"Close Doors\" action=\"bypass -h admin_clanhallclosedoors " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
    replyMSG.append("</table>");
    replyMSG.append("<br>");
    replyMSG.append("<table>");
    replyMSG.append("<tr><td><button value=\"Give ClanHall\" action=\"bypass -h admin_clanhallset " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
    replyMSG.append("<td><button value=\"Take ClanHall\" action=\"bypass -h admin_clanhalldel " + clanhall.getId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
    replyMSG.append("</table>");
    replyMSG.append("<br>");
    replyMSG.append("<table><tr>");
    replyMSG.append("<td><button value=\"Teleport self\" action=\"bypass -h admin_clanhallteleportself " + clanhall.getId() + " \" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
    replyMSG.append("</table>");
    replyMSG.append("</center>");
    replyMSG.append("</body></html>");
    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public Enum[] getAdminCommandEnum() {
    return AdminClanHall.Commands.values();
  }

  private static enum Commands {
    admin_clanhall,
    admin_clanhallset,
    admin_clanhalldel,
    admin_clanhallteleportself;

    private Commands() {
    }
  }
}
