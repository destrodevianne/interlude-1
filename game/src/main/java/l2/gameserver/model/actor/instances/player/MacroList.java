//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.model.actor.instances.player;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.instances.player.Macro.L2MacroCmd;
import l2.gameserver.network.l2.s2c.SendMacroList;
import l2.gameserver.utils.Strings;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Slf4j
public class MacroList {
  private final Player player;
  private final Map<Integer, Macro> _macroses = new HashMap<>();
  private int _revision;
  private int _macroId;

  public MacroList(Player player) {
    this.player = player;
    this._revision = 1;
    this._macroId = 1000;
  }

  public int getRevision() {
    return this._revision;
  }

  public Macro[] getAllMacroses() {
    return this._macroses.values().toArray(new Macro[0]);
  }

  public Macro getMacro(int id) {
    return this._macroses.get(id - 1);
  }

  public void registerMacro(Macro macro) {
    if (macro.id == 0) {
      for (macro.id = this._macroId++; this._macroses.get(macro.id) != null; macro.id = this._macroId++) {
      }

      this._macroses.put(macro.id, macro);
      this.registerMacroInDb(macro);
    } else {
      Macro old = this._macroses.put(macro.id, macro);
      if (old != null) {
        this.deleteMacroFromDb(old);
      }

      this.registerMacroInDb(macro);
    }

    this.sendUpdate();
  }

  public void deleteMacro(int id) {
    Macro toRemove = this._macroses.get(id);
    if (toRemove != null) {
      this.deleteMacroFromDb(toRemove);
    }

    this._macroses.remove(id);
    this.sendUpdate();
  }

  public void sendUpdate() {
    ++this._revision;
    Macro[] all = this.getAllMacroses();
    if (all.length == 0) {
      this.player.sendPacket(new SendMacroList(this._revision, all.length, null));
    } else {

      for (Macro m : all) {
        this.player.sendPacket(new SendMacroList(this._revision, all.length, m));
      }
    }

  }

  private void registerMacroInDb(Macro macro) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
      statement.setInt(1, this.player.getObjectId());
      statement.setInt(2, macro.id);
      statement.setInt(3, macro.icon);
      statement.setString(4, macro.name);
      statement.setString(5, macro.descr);
      statement.setString(6, macro.acronym);
      StringBuilder sb = new StringBuilder();

      for (L2MacroCmd cmd : macro.commands) {
        sb.append(cmd.type).append(',');
        sb.append(cmd.d1).append(',');
        sb.append(cmd.d2);
        if (cmd.cmd != null && cmd.cmd.length() > 0) {
          sb.append(',').append(cmd.cmd);
        }

        sb.append(';');
      }

      statement.setString(7, sb.toString());
      statement.execute();
    } catch (Exception var12) {
      log.error("could not store macro: " + macro.toString(), var12);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  private void deleteMacroFromDb(Macro macro) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
      statement.setInt(1, this.player.getObjectId());
      statement.setInt(2, macro.id);
      statement.execute();
    } catch (Exception var8) {
      log.error("could not delete macro:", var8);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  public void restore() {
    this._macroses.clear();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
      statement.setInt(1, this.player.getObjectId());
      rset = statement.executeQuery();

      while (rset.next()) {
        int id = rset.getInt("id");
        int icon = rset.getInt("icon");
        String name = Strings.stripSlashes(rset.getString("name"));
        String descr = Strings.stripSlashes(rset.getString("descr"));
        String acronym = Strings.stripSlashes(rset.getString("acronym"));
        List<L2MacroCmd> commands = new ArrayList<>();
        StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");

        while (st1.hasMoreTokens()) {
          StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
          int type = Integer.parseInt(st.nextToken());
          int d1 = Integer.parseInt(st.nextToken());
          int d2 = Integer.parseInt(st.nextToken());
          String cmd = "";
          if (st.hasMoreTokens()) {
            cmd = st.nextToken();
          }

          L2MacroCmd mcmd = new L2MacroCmd(commands.size(), type, d1, d2, cmd);
          commands.add(mcmd);
        }

        Macro m = new Macro(id, icon, name, descr, acronym, commands.toArray(new L2MacroCmd[0]));
        this._macroses.put(m.id, m);
      }
    } catch (Exception var20) {
      log.error("could not restore shortcuts:", var20);
    } finally {
      DbUtils.closeQuietly(con, statement, rset);
    }

  }
}
