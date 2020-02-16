//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.handler.admincommands.impl;

import java.util.Iterator;
import java.util.Map;
import l2.commons.text.PrintfFormat;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.QuestManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;

public class AdminQuests implements IAdminCommandHandler {
  private static final PrintfFormat fmtHEAD = new PrintfFormat("<center><font color=\"LEVEL\">%s [id=%d]</font><br><edit var=\"new_val\" width=100 height=12></center><br>");
  private static final PrintfFormat fmtRow = new PrintfFormat("<tr><td>%s</td><td>%s</td><td width=30>%s</td></tr>");
  private static final PrintfFormat fmtSetButton = new PrintfFormat("<button value=\"Set\" action=\"bypass -h admin_quest %d %s %s %s %s\" width=30 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
  private static final PrintfFormat fmtFOOT = new PrintfFormat("<br><br><br><center><button value=\"Clear Quest\" action=\"bypass -h admin_quest %d CLEAR %s\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"> <button value=\"Quests List\" action=\"bypass -h admin_quests %s\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center>");
  private static final PrintfFormat fmtListRow = new PrintfFormat("<tr><td><a action=\"bypass -h admin_quest %d %s\">%s</a></td><td>%s</td></tr>");
  private static final PrintfFormat fmtListNew = new PrintfFormat("<tr><td><edit var=\"new_quest\" width=100 height=12></td><td><button value=\"Add\" action=\"bypass -h admin_quest $new_quest STATE 2 %s\" width=40 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");

  public AdminQuests() {
  }

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar) {
    AdminQuests.Commands command = (AdminQuests.Commands)comm;
    if (!activeChar.getPlayerAccess().CanEditCharAll) {
      return false;
    } else {
      switch(command) {
        case admin_quests:
          return ShowQuestList(this.getTargetChar(wordList, 1, activeChar), activeChar);
        case admin_quest:
          if (wordList.length < 2) {
            activeChar.sendMessage("USAGE: //quest id|name [SHOW|STATE|VAR|CLEAR] ...");
            return true;
          } else {
            Quest _quest = QuestManager.getQuest2(wordList[1]);
            if (_quest == null) {
              activeChar.sendMessage("Quest " + wordList[1] + " undefined");
              return true;
            } else {
              if (wordList.length >= 3 && !wordList[2].equalsIgnoreCase("SHOW")) {
                if (wordList[2].equalsIgnoreCase("STATE")) {
                  return this.cmd_State(_quest, wordList, activeChar);
                }

                if (wordList[2].equalsIgnoreCase("VAR")) {
                  return this.cmd_Var(_quest, wordList, activeChar);
                }

                if (wordList[2].equalsIgnoreCase("CLEAR")) {
                  return this.cmd_Clear(_quest, wordList, activeChar);
                }

                return this.cmd_Show(_quest, wordList, activeChar);
              }

              return this.cmd_Show(_quest, wordList, activeChar);
            }
          }
        default:
          return true;
      }
    }
  }

  private boolean cmd_Clear(Quest _quest, String[] wordList, Player activeChar) {
    Player targetChar = this.getTargetChar(wordList, 3, activeChar);
    QuestState qs = targetChar.getQuestState(_quest.getName());
    if (qs == null) {
      activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "]");
      return false;
    } else {
      qs.exitCurrentQuest(true);
      return ShowQuestList(targetChar, activeChar);
    }
  }

  private boolean cmd_Show(Quest _quest, String[] wordList, Player activeChar) {
    Player targetChar = this.getTargetChar(wordList, 3, activeChar);
    QuestState qs = targetChar.getQuestState(_quest.getName());
    if (qs == null) {
      activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "]");
      return false;
    } else {
      return ShowQuestState(qs, activeChar);
    }
  }

  private static boolean ShowQuestState(QuestState qs, Player activeChar) {
    Map<String, String> vars = qs.getVars();
    int id = qs.getQuest().getQuestIntId();
    String char_name = qs.getPlayer().getName();
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append(fmtHEAD.sprintf(qs.getQuest().getClass().getSimpleName(), id));
    replyMSG.append("<table width=260>");
    replyMSG.append(fmtRow.sprintf("PLAYER: ", char_name, ""));
    replyMSG.append(fmtRow.sprintf("STATE: ", qs.getStateName(), fmtSetButton.sprintf(id, "STATE", "$new_val", char_name, "")));

    for (String key : vars.keySet()) {
      if (!key.equalsIgnoreCase("<state>")) {
        replyMSG.append(fmtRow.sprintf(key + ": ", vars.get(key), fmtSetButton.sprintf(id, "VAR", key, "$new_val", char_name)));
      }
    }

    replyMSG.append(fmtRow.sprintf("<edit var=\"new_name\" width=50 height=12>", "~new var~", fmtSetButton.sprintf(id, "VAR", "$new_name", "$new_val", char_name)));
    replyMSG.append("</table>");
    replyMSG.append(fmtFOOT.sprintf(id, char_name, char_name));
    replyMSG.append("</body></html>");
    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
    vars.clear();
    return true;
  }

  private static boolean ShowQuestList(Player targetChar, Player activeChar) {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    StringBuilder replyMSG = new StringBuilder("<html><body><table width=260>");
    QuestState[] var4 = targetChar.getAllQuestsStates();
    int var5 = var4.length;

    for (QuestState qs : var4) {
      if (qs != null && qs.getQuest().getQuestIntId() != 255) {
        replyMSG.append(fmtListRow.sprintf(qs.getQuest().getQuestIntId(), targetChar.getName(), qs.getQuest().getName(), qs.getStateName()));
      }
    }

    replyMSG.append(fmtListNew.sprintf(new Object[]{targetChar.getName()}));
    replyMSG.append("</table></body></html>");
    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
    return true;
  }

  private boolean cmd_Var(Quest _quest, String[] wordList, Player activeChar) {
    if (wordList.length < 5) {
      activeChar.sendMessage("USAGE: //quest id|name VAR varname newvalue [target]");
      return false;
    } else {
      Player targetChar = this.getTargetChar(wordList, 5, activeChar);
      QuestState qs = targetChar.getQuestState(_quest.getName());
      if (qs == null) {
        activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "], init quest by command:");
        activeChar.sendMessage("//quest id|name STATE 1|2|3 [target]");
        return false;
      } else {
        if (!wordList[4].equalsIgnoreCase("~") && !wordList[4].equalsIgnoreCase("#")) {
          qs.set(wordList[3], wordList[4]);
        } else {
          qs.unset(wordList[3]);
        }

        return ShowQuestState(qs, activeChar);
      }
    }
  }

  private boolean cmd_State(Quest _quest, String[] wordList, Player activeChar) {
    if (wordList.length < 4) {
      activeChar.sendMessage("USAGE: //quest id|name STATE 1|2|3 [target]");
      return false;
    } else {

      int state;
      try {
        state = Integer.parseInt(wordList[3]);
      } catch (Exception var7) {
        activeChar.sendMessage("Wrong State ID: " + wordList[3]);
        return false;
      }

      Player targetChar = this.getTargetChar(wordList, 4, activeChar);
      QuestState qs = targetChar.getQuestState(_quest.getName());
      if (qs == null) {
        activeChar.sendMessage("Init Quest [" + _quest.getName() + "] for " + targetChar.getName());
        qs = _quest.newQuestState(targetChar, state);
        qs.set("cond", "1");
      } else {
        qs.setState(state);
      }

      return ShowQuestState(qs, activeChar);
    }
  }

  private Player getTargetChar(String[] wordList, int wordListIndex, Player activeChar) {
    if (wordListIndex >= 0 && wordList.length > wordListIndex) {
      Player player = World.getPlayer(wordList[wordListIndex]);
      if (player == null) {
        activeChar.sendMessage("Can't find player: " + wordList[wordListIndex]);
      }

      return player;
    } else {
      GameObject my_target = activeChar.getTarget();
      return my_target != null && my_target.isPlayer() ? (Player)my_target : activeChar;
    }
  }

  public Enum[] getAdminCommandEnum() {
    return AdminQuests.Commands.values();
  }

  private enum Commands {
    admin_quests,
    admin_quest;

    Commands() {
    }
  }
}
