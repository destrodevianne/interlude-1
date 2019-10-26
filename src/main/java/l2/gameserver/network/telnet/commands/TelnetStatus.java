//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.telnet.commands;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import l2.commons.lang.StatsUtils;
import l2.gameserver.Config;
import l2.gameserver.GameTimeController;
import l2.gameserver.Shutdown;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.World;
import l2.gameserver.network.telnet.TelnetCommand;
import l2.gameserver.network.telnet.TelnetCommandHolder;
import l2.gameserver.tables.GmListTable;
import l2.gameserver.utils.Util;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class TelnetStatus implements TelnetCommandHolder {
  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetStatus() {
    this._commands.add(new TelnetCommand("status", new String[]{"s"}) {
      public String getUsage() {
        return "status";
      }

      public String handle(String[] args) {
        StringBuilder sb = new StringBuilder();
        int[] stats = World.getStats();
        sb.append("Server Status: ").append("\n");
        sb.append("Players: ................. ").append(stats[12]).append("/").append(Config.MAXIMUM_ONLINE_USERS).append("\n");
        sb.append("     Online: ............. ").append(stats[12] - stats[13]).append("\n");
        sb.append("     Offline: ............ ").append(stats[13]).append("\n");
        sb.append("     GM: ................. ").append(GmListTable.getAllGMs().size()).append("\n");
        sb.append("Objects: ................. ").append(stats[10]).append("\n");
        sb.append("Characters: .............. ").append(stats[11]).append("\n");
        sb.append("Summons: ................. ").append(stats[18]).append("\n");
        sb.append("Npcs: .................... ").append(stats[15]).append("/").append(stats[14]).append("\n");
        sb.append("Monsters: ................ ").append(stats[16]).append("\n");
        sb.append("Minions: ................. ").append(stats[17]).append("\n");
        sb.append("Doors: ................... ").append(stats[19]).append("\n");
        sb.append("Items: ................... ").append(stats[20]).append("\n");
        sb.append("Reflections: ............. ").append(ReflectionManager.getInstance().getAll().length).append("\n");
        sb.append("Regions: ................. ").append(stats[0]).append("\n");
        sb.append("     Active: ............. ").append(stats[1]).append("\n");
        sb.append("     Inactive: ........... ").append(stats[2]).append("\n");
        sb.append("     Null: ............... ").append(stats[3]).append("\n");
        sb.append("Game Time: ............... ").append(TelnetStatus.getGameTime()).append("\n");
        sb.append("Real Time: ............... ").append(TelnetStatus.getCurrentTime()).append("\n");
        sb.append("Start Time: .............. ").append(TelnetStatus.getStartTime()).append("\n");
        sb.append("Uptime: .................. ").append(TelnetStatus.getUptime()).append("\n");
        sb.append("Shutdown: ................ ").append(Util.formatTime(Shutdown.getInstance().getSeconds())).append("/").append(Shutdown.getInstance().getMode()).append("\n");
        sb.append("Threads: ................. ").append(Thread.activeCount()).append("\n");
        sb.append("RAM Used: ................ ").append(StatsUtils.getMemUsedMb()).append("\n");
        return sb.toString();
      }
    });
  }

  public Set<TelnetCommand> getCommands() {
    return this._commands;
  }

  public static String getGameTime() {
    int t = GameTimeController.getInstance().getGameTime();
    int h = t / 60;
    int m = t % 60;
    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    Calendar cal = Calendar.getInstance();
    cal.set(11, h);
    cal.set(12, m);
    return format.format(cal.getTime());
  }

  public static String getUptime() {
    return DurationFormatUtils.formatDurationHMS(ManagementFactory.getRuntimeMXBean().getUptime());
  }

  public static String getStartTime() {
    return (new Date(ManagementFactory.getRuntimeMXBean().getStartTime())).toString();
  }

  public static String getCurrentTime() {
    return (new Date()).toString();
  }
}
