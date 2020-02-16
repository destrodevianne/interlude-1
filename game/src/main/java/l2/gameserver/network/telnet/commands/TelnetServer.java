//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.telnet.commands;

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;
import l2.gameserver.GameServer;
import l2.gameserver.Shutdown;
import l2.gameserver.config.GameServerConfiguration;
import l2.gameserver.network.telnet.TelnetCommand;
import l2.gameserver.network.telnet.TelnetCommandHolder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class TelnetServer implements TelnetCommandHolder {
  private Set<TelnetCommand> _commands = new LinkedHashSet<>();

  public TelnetServer() {
    this._commands.add(new TelnetCommand("version", "ver") {
      public String getUsage() {
        return "version";
      }

      public String handle(String[] args) {
        return "Rev." + GameServerConfiguration.getInstance().getVersion().getRevisionNumber() + " Builded : " + GameServerConfiguration.getInstance().getVersion().getBuildDate() + "\n";
      }
    });
    this._commands.add(new TelnetCommand("uptime") {
      public String getUsage() {
        return "uptime";
      }

      public String handle(String[] args) {
        return DurationFormatUtils.formatDurationHMS(ManagementFactory.getRuntimeMXBean().getUptime()) + "\n";
      }
    });
    this._commands.add(new TelnetCommand("restart") {
      public String getUsage() {
        return "restart <seconds>|now>";
      }

      public String handle(String[] args) {
        if (args.length == 0) {
          return null;
        } else {
          StringBuilder sb = new StringBuilder();
          if (NumberUtils.isNumber(args[0])) {
            int val = NumberUtils.toInt(args[0]);
            Shutdown.getInstance().schedule(val, 2);
            sb.append("Server will restart in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
            sb.append("Type \"abort\" to abort restart!\n");
          } else if (args[0].equalsIgnoreCase("now")) {
            sb.append("Server will restart now!\n");
            Shutdown.getInstance().schedule(0, 2);
          } else {
            String[] hhmm = args[0].split(":");
            Calendar date = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            date.set(11, Integer.parseInt(hhmm[0]));
            date.set(12, hhmm.length > 1 ? Integer.parseInt(hhmm[1]) : 0);
            date.set(13, 0);
            date.set(14, 0);
            if (date.before(now)) {
              date.roll(5, true);
            }

            int seconds = (int)(date.getTimeInMillis() / 1000L - now.getTimeInMillis() / 1000L);
            Shutdown.getInstance().schedule(seconds, 2);
            sb.append("Server will restart in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
            sb.append("Type \"abort\" to abort restart!\n");
          }

          return sb.toString();
        }
      }
    });
    this._commands.add(new TelnetCommand("shutdown") {
      public String getUsage() {
        return "shutdown <seconds>|now|<hh:mm>";
      }

      public String handle(String[] args) {
        if (args.length == 0) {
          return null;
        } else {
          StringBuilder sb = new StringBuilder();
          if (NumberUtils.isNumber(args[0])) {
            int val = NumberUtils.toInt(args[0]);
            Shutdown.getInstance().schedule(val, 0);
            sb.append("Server will shutdown in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
            sb.append("Type \"abort\" to abort shutdown!\n");
          } else if (args[0].equalsIgnoreCase("now")) {
            sb.append("Server will shutdown now!\n");
            Shutdown.getInstance().schedule(0, 0);
          } else {
            String[] hhmm = args[0].split(":");
            Calendar date = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            date.set(11, Integer.parseInt(hhmm[0]));
            date.set(12, hhmm.length > 1 ? Integer.parseInt(hhmm[1]) : 0);
            date.set(13, 0);
            date.set(14, 0);
            if (date.before(now)) {
              date.roll(5, true);
            }

            int seconds = (int)(date.getTimeInMillis() / 1000L - now.getTimeInMillis() / 1000L);
            Shutdown.getInstance().schedule(seconds, 0);
            sb.append("Server will shutdown in ").append(Shutdown.getInstance().getSeconds()).append(" seconds!\n");
            sb.append("Type \"abort\" to abort shutdown!\n");
          }

          return sb.toString();
        }
      }
    });
    this._commands.add(new TelnetCommand("abort") {
      public String getUsage() {
        return "abort";
      }

      public String handle(String[] args) {
        Shutdown.getInstance().cancel();
        return "Aborted.\n";
      }
    });
  }

  public Set<TelnetCommand> getCommands() {
    return this._commands;
  }
}
