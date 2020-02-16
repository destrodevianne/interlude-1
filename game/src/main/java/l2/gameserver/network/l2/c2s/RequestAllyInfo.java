//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.List;

public class RequestAllyInfo extends L2GameClientPacket {
  public RequestAllyInfo() {
  }

  protected void readImpl() {
  }

  protected void runImpl() {
    Player player = this.getClient().getActiveChar();
    if (player != null) {
      Alliance ally = player.getAlliance();
      if (ally != null) {
        Clan leaderclan = player.getAlliance().getLeader();
        int clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
        int[] online = new int[clancount + 1];
        int[] count = new int[clancount + 1];
        Clan[] clans = player.getAlliance().getMembers();

        for(int i = 0; i < clancount; ++i) {
          online[i + 1] = clans[i].getOnlineMembers(0).size();
          count[i + 1] = clans[i].getAllSize();
          online[0] += online[i + 1];
          count[0] += count[i + 1];
        }

        List<L2GameServerPacket> packets = new ArrayList(7 + 5 * clancount);
        packets.add(Msg._ALLIANCE_INFORMATION_);
        packets.add((new SystemMessage(492)).addString(player.getClan().getAlliance().getAllyName()));
        packets.add((new SystemMessage(493)).addNumber(online[0]).addNumber(count[0]));
        packets.add((new SystemMessage(494)).addString(leaderclan.getName()).addString(leaderclan.getLeaderName()));
        packets.add((new SystemMessage(495)).addNumber(clancount));
        packets.add(Msg._CLAN_INFORMATION_);

        for(int i = 0; i < clancount; ++i) {
          packets.add((new SystemMessage(497)).addString(clans[i].getName()));
          packets.add((new SystemMessage(498)).addString(clans[i].getLeaderName()));
          packets.add((new SystemMessage(499)).addNumber(clans[i].getLevel()));
          packets.add((new SystemMessage(493)).addNumber(online[i + 1]).addNumber(count[i + 1]));
          packets.add(Msg.__DASHES__);
        }

        packets.add(Msg.__EQUALS__);
        player.sendPacket(packets);
      }
    }
  }
}
