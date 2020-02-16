//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.handler.admincommands.impl;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.MysqlInitializer;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.utils.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
public class AdminChangeAccessLevel implements IAdminCommandHandler {

  public AdminChangeAccessLevel() {
  }

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar) {
    AdminChangeAccessLevel.Commands command = (AdminChangeAccessLevel.Commands) comm;
    if (!activeChar.getPlayerAccess().CanGmEdit) {
      return false;
    } else {
      switch (command) {
        case admin_changelvl:
          int lvl;
          if (wordList.length == 2) {
            lvl = Integer.parseInt(wordList[1]);
            if (activeChar.getTarget().isPlayer()) {
              ((Player) activeChar.getTarget()).setAccessLevel(lvl);
            }
          } else if (wordList.length == 3) {
            lvl = Integer.parseInt(wordList[2]);
            Player player = GameObjectsStorage.getPlayer(wordList[1]);
            if (player != null) {
              player.setAccessLevel(lvl);
            }
          }
          break;
        case admin_moders:
          showModersPannel(activeChar);
          break;
        case admin_moders_add:
          if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer()) {
            Player modAdd = activeChar.getTarget().getPlayer();
            if (Config.gmlist.containsKey(modAdd.getObjectId())) {
              activeChar.sendMessage("Error: Moderator " + modAdd.getName() + " already in server access list.");
              showModersPannel(activeChar);
              return false;
            }

            String newFName = "m" + modAdd.getObjectId() + ".xml";
            if (!Files.copyFile("game/GMAccess.d/template/moderator.xml", "game/GMAccess.d/" + newFName)) {
              activeChar.sendMessage("Error: Failed to copy access-file.");
              showModersPannel(activeChar);
              return false;
            }

            StringBuilder res = new StringBuilder();

            try {
              BufferedReader in = new BufferedReader(new FileReader("game/GMAccess.d/" + newFName));

              while (true) {
                String str;
                if ((str = in.readLine()) == null) {
                  in.close();
                  res = new StringBuilder(res.toString().replaceFirst("ObjIdPlayer", "" + modAdd.getObjectId()));
                  Files.writeFile("game/GMAccess.d/" + newFName, res.toString());
                  break;
                }

                res.append(str).append("\n");
              }
            } catch (Exception var20) {
              activeChar.sendMessage("Error: Failed to modify object ID in access-file.");
              File fDel = new File("game/GMAccess.d/" + newFName);
              if (fDel.exists()) {
                fDel.delete();
              }

              showModersPannel(activeChar);
              return false;
            }

            File af = new File("game/GMAccess.d/" + newFName);
            if (!af.exists()) {
              activeChar.sendMessage("Error: Failed to read access-file for " + modAdd.getName());
              showModersPannel(activeChar);
              return false;
            }

            Config.loadGMAccess(af);
            modAdd.setPlayerAccess(Config.gmlist.get(modAdd.getObjectId()));
            activeChar.sendMessage("Moderator " + modAdd.getName() + " added.");
            showModersPannel(activeChar);
            break;
          }

          activeChar.sendMessage("Incorrect target. Please select a player.");
          showModersPannel(activeChar);
          return false;
        case admin_moders_del:
          if (wordList.length < 2) {
            activeChar.sendMessage("Please specify moderator object ID to delete moderator.");
            showModersPannel(activeChar);
            return false;
          }

          int oid = Integer.parseInt(wordList[1]);
          if (Config.gmlist.containsKey(oid)) {
            Config.gmlist.remove(oid);
            Player modDel = GameObjectsStorage.getPlayer(oid);
            if (modDel != null) {
              modDel.setPlayerAccess(null);
            }

            String fname = "m" + oid + ".xml";
            File f = new File("game/GMAccess.d/" + fname);
            if (f.exists() && f.isFile() && f.delete()) {
              if (modDel != null) {
                activeChar.sendMessage("Moderator " + modDel.getName() + " deleted.");
              } else {
                activeChar.sendMessage("Moderator with object ID " + oid + " deleted.");
              }

              showModersPannel(activeChar);
              break;
            }

            activeChar.sendMessage("Error: Can't delete access-file: " + fname);
            showModersPannel(activeChar);
            return false;
          }

          activeChar.sendMessage("Error: Moderator with object ID " + oid + " not found in server access lits.");
          showModersPannel(activeChar);
          return false;
        case admin_penalty:
          if (wordList.length < 2) {
            activeChar.sendMessage("USAGE: //penalty charName [count] [reason]");
            return false;
          }

          int count = 1;
          if (wordList.length > 2) {
            count = Integer.parseInt(wordList[2]);
          }

          String reason = "не указана";
          if (wordList.length > 3) {
            reason = wordList[3];
          }

          Player player = GameObjectsStorage.getPlayer(wordList[1]);
          int oId;
          if (player != null && player.getPlayerAccess().CanBanChat) {
            oId = player.getObjectId();
            int oldPenaltyCount = 0;
            String oldPenalty = player.getVar("penaltyChatCount");
            if (oldPenalty != null) {
              oldPenaltyCount = Integer.parseInt(oldPenalty);
            }

            player.setVar("penaltyChatCount", "" + (oldPenaltyCount + count), -1L);
          } else {
            oId = MysqlInitializer.simple_get_int("obj_Id", "characters", "`char_name`='" + wordList[1] + "'");
            if (oId > 0) {
              Integer oldCount = (Integer) MysqlInitializer.get("SELECT `value` FROM character_variables WHERE `obj_id` = " + oId + " AND `name` = 'penaltyChatCount'");
              MysqlInitializer.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (" + oId + ",'user-var','penaltyChatCount','" + (oldCount + count) + "',-1)");
            }
          }

          if (oId > 0) {
            if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD) {
              Announcements.getInstance().announceToAll(activeChar + " оштрафовал модератора " + wordList[1] + " на " + count + ", причина: " + reason + ".");
            } else {
              Announcements.shout(activeChar, activeChar + " оштрафовал модератора " + wordList[1] + " на " + count + ", причина: " + reason + ".", ChatType.CRITICAL_ANNOUNCE);
            }
          }
      }

      return true;
    }
  }

  private static void showModersPannel(Player activeChar) {
    NpcHtmlMessage reply = new NpcHtmlMessage(5);
    String html = "Moderators managment panel.<br>";
    File dir = new File("game/GMAccess.d/");
    if (dir.exists() && dir.isDirectory()) {
      html = html + "<p align=right>";
      html = html + "<button width=120 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_moders_add\" value=\"Add modrator\">";
      html = html + "</p><br>";
      html = html + "<center><font color=LEVEL>Moderators:</font></center>";
      html = html + "<table width=285>";
      File[] var4 = dir.listFiles();

      for (File f : var4) {
        if (!f.isDirectory() && f.getName().startsWith("m") && f.getName().endsWith(".xml")) {
          int oid = Integer.parseInt(f.getName().substring(1, 10));
          String pName = getPlayerNameByObjId(oid);
          boolean on = false;
          if (pName != null && !pName.isEmpty()) {
            on = GameObjectsStorage.getPlayer(pName) != null;
          } else {
            pName = "" + oid;
          }

          html = html + "<tr>";
          html = html + "<td width=140>" + pName;
          html = html + (on ? " <font color=\"33CC66\">(on)</font>" : "");
          html = html + "</td>";
          html = html + "<td width=45><button width=50 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_moders_log " + oid + "\" value=\"Logs\"></td>";
          html = html + "<td width=45><button width=20 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_moders_del " + oid + "\" value=\"X\"></td>";
          html = html + "</tr>";
        }
      }

      html = html + "</table>";
      reply.setHtml(html);
      activeChar.sendPacket(reply);
    } else {
      html = html + "Error: Can't open permissions folder.";
      reply.setHtml(html);
      activeChar.sendPacket(reply);
    }
  }

  private static String getPlayerNameByObjId(int oid) {
    String pName = null;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `obj_Id`=\"" + oid + "\" LIMIT 1");
      rset = statement.executeQuery();
      if (rset.next()) {
        pName = rset.getString(1);
      }
    } catch (Exception var9) {
      log.warn("SQL Error: " + var9);
      log.error("", var9);
    } finally {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return pName;
  }

  public Enum[] getAdminCommandEnum() {
    return AdminChangeAccessLevel.Commands.values();
  }

  private enum Commands {
    admin_changelvl,
    admin_moders,
    admin_moders_add,
    admin_moders_del,
    admin_penalty;

    Commands() {
    }
  }
}
