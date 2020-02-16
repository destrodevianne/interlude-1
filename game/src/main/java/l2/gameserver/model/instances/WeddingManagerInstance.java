//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.model.instances;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.instancemanager.CoupleManager;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Couple;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ItemFunctions;

public class WeddingManagerInstance extends NpcInstance {
  public WeddingManagerInstance(int objectId, NpcTemplate template) {
    super(objectId, template);
  }

  public void showChatWindow(Player player, int val, Object... arg) {
    String filename = "wedding/start.htm";
    String replace = "";
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile(filename);
    html.replace("%replace%", replace);
    html.replace("%npcname%", this.getName());
    player.sendPacket(html);
  }

  public void onBypassFeedback(Player player, String command) {
    if (canBypassCheck(player, this)) {
      String filename = "wedding/start.htm";
      String replace = "";
      if (player.getPartnerId() == 0) {
        filename = "wedding/nopartner.htm";
        this.sendHtmlMessage(player, filename, replace);
      } else {
        Player ptarget = GameObjectsStorage.getPlayer(player.getPartnerId());
        if (ptarget != null && ptarget.isOnline()) {
          if (player.isMaried()) {
            filename = "wedding/already.htm";
            this.sendHtmlMessage(player, filename, replace);
          } else if (command.startsWith("AcceptWedding")) {
            player.setMaryAccepted(true);
            Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
            couple.marry();
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2WeddingManagerMessage", player, new Object[0]));
            player.setMaried(true);
            player.setMaryRequest(false);
            ptarget.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2WeddingManagerMessage", ptarget, new Object[0]));
            ptarget.setMaried(true);
            ptarget.setMaryRequest(false);
            player.broadcastPacket(new L2GameServerPacket[]{new MagicSkillUse(player, player, 2230, 1, 1, 0L)});
            ptarget.broadcastPacket(new L2GameServerPacket[]{new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0L)});
            player.broadcastPacket(new L2GameServerPacket[]{new MagicSkillUse(player, player, 2025, 1, 1, 0L)});
            ptarget.broadcastPacket(new L2GameServerPacket[]{new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0L)});
            if (Config.WEDDING_GIVE_SALVATION_BOW) {
              ItemFunctions.addItem(player, 9140, 1L, false);
              ItemFunctions.addItem(ptarget, 9140, 1L, false);
            }

            if (Config.WEDDING_USE_COLOR) {
              if (player.getSex() == 1 && ptarget.getSex() == 0 || player.getSex() == 0 && ptarget.getSex() == 1) {
                player.setNameColor(Config.WEDDING_NORMAL_COLOR);
                ptarget.setNameColor(Config.WEDDING_NORMAL_COLOR);
              }

              if (player.getSex() == 1 && ptarget.getSex() == 1) {
                player.setNameColor(Config.WEDDING_LESBIAN_COLOR);
                ptarget.setNameColor(Config.WEDDING_LESBIAN_COLOR);
              }

              if (player.getSex() == 0 && ptarget.getSex() == 0) {
                player.setNameColor(Config.WEDDING_GAY_COLOR);
                ptarget.setNameColor(Config.WEDDING_GAY_COLOR);
              }

              player.broadcastUserInfo(true);
              ptarget.broadcastUserInfo(true);
            }

            if (Config.WEDDING_ANNOUNCE) {
              Announcements.getInstance().announceByCustomMessage("l2p.gameserver.model.instances.L2WeddingManagerMessage.announce", new String[]{player.getName(), ptarget.getName()});
            }

            filename = "wedding/accepted.htm";
            replace = ptarget.getName();
            this.sendHtmlMessage(ptarget, filename, replace);
          } else if (player.isMaryRequest()) {
            if (Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player)) {
              filename = "wedding/noformal.htm";
              this.sendHtmlMessage(player, filename, replace);
            } else {
              filename = "wedding/ask.htm";
              player.setMaryRequest(false);
              ptarget.setMaryRequest(false);
              replace = ptarget.getName();
              this.sendHtmlMessage(player, filename, replace);
            }
          } else if (command.startsWith("AskWedding")) {
            if (Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player)) {
              filename = "wedding/noformal.htm";
              this.sendHtmlMessage(player, filename, replace);
            } else if (ItemFunctions.getItemCount(player, Config.WEDDING_ITEM_ID_PRICE) < (long)Config.WEDDING_PRICE) {
              player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
              player.setMaryAccepted(true);
              ptarget.setMaryRequest(true);
              replace = ptarget.getName();
              filename = "wedding/requested.htm";
              ItemFunctions.removeItem(player, Config.WEDDING_ITEM_ID_PRICE, (long)Config.WEDDING_PRICE, true);
              this.sendHtmlMessage(player, filename, replace);
            }
          } else if (command.startsWith("DeclineWedding")) {
            player.setMaryRequest(false);
            ptarget.setMaryRequest(false);
            player.setMaryAccepted(false);
            ptarget.setMaryAccepted(false);
            player.sendMessage("You declined");
            ptarget.sendMessage("Your partner declined");
            replace = ptarget.getName();
            filename = "wedding/declined.htm";
            this.sendHtmlMessage(ptarget, filename, replace);
          } else if (player.isMaryAccepted()) {
            filename = "wedding/waitforpartner.htm";
            this.sendHtmlMessage(player, filename, replace);
          } else {
            this.sendHtmlMessage(player, filename, replace);
          }
        } else {
          filename = "wedding/notfound.htm";
          this.sendHtmlMessage(player, filename, replace);
        }
      }
    }
  }

  private static boolean isWearingFormalWear(Player player) {
    return player != null && player.getInventory() != null && player.getInventory().getPaperdollItemId(10) == 6408;
  }

  private void sendHtmlMessage(Player player, String filename, String replace) {
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile(filename);
    html.replace("%replace%", replace);
    html.replace("%npcname%", this.getName());
    player.sendPacket(html);
  }
}
