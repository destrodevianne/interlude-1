//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.telnet;

import l2.gameserver.Config;
import l2.gameserver.network.telnet.commands.TelnetServer;
import l2.gameserver.network.telnet.commands.*;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TelnetServerHandler extends SimpleChannelUpstreamHandler implements TelnetCommandHolder {
  private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");
  private Set<TelnetCommand> _commands = new LinkedHashSet<>();

  public TelnetServerHandler() {
    this._commands.add(new TelnetCommand("help", new String[]{"h"}) {
      public String getUsage() {
        return "help [command]";
      }

      public String handle(String[] args) {
        if (args.length != 0) {
          TelnetCommand cmdx = TelnetServerHandler.this.getCommand(args[0]);
          return cmdx == null ? "Unknown command.\n" : "usage:\n" + cmdx.getUsage() + "\n";
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("Available commands:\n");

          for (TelnetCommand cmd : TelnetServerHandler.this._commands) {
            sb.append(cmd.getCommand()).append("\n");
          }

          return sb.toString();
        }
      }
    });
    this.addHandler(new TelnetBan());
    this.addHandler(new TelnetConfig());
    this.addHandler(new TelnetDebug());
    this.addHandler(new TelnetPerfomance());
    this.addHandler(new TelnetSay());
    this.addHandler(new TelnetServer());
    this.addHandler(new TelnetStatus());
    this.addHandler(new TelnetWorld());
  }

  public void addHandler(TelnetCommandHolder handler) {

    this._commands.addAll(handler.getCommands());

  }

  public Set<TelnetCommand> getCommands() {
    return this._commands;
  }

  private TelnetCommand getCommand(String command) {
    Iterator var2 = this._commands.iterator();

    TelnetCommand cmd;
    do {
      if (!var2.hasNext()) {
        return null;
      }

      cmd = (TelnetCommand) var2.next();
    } while (!cmd.equals(command));

    return cmd;
  }

  private String tryHandleCommand(String command, String[] args) {
    TelnetCommand cmd = this.getCommand(command);
    if (cmd == null) {
      return "Unknown command.\n";
    } else {
      String response = cmd.handle(args);
      if (response == null) {
        response = "usage:\n" + cmd.getUsage() + "\n";
      }

      return response;
    }
  }

  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    String sb = "Welcome to L2 GameServer telnet console.\n" +
      "It is " + new Date() + " now.\n";
    e.getChannel().write(sb.replaceAll("\n", "\r\n"));
    if (!Config.TELNET_PASSWORD.isEmpty()) {
      e.getChannel().write("Password:");
      ctx.setAttachment(Boolean.FALSE);
    } else {
      e.getChannel().write("Type 'help' to see all available commands.\r\n");
      ctx.setAttachment(Boolean.TRUE);
    }

  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    String request = (String) e.getMessage();
    String response = null;
    boolean close = false;
    if (Boolean.FALSE.equals(ctx.getAttachment())) {
      if (Config.TELNET_PASSWORD.equals(request)) {
        ctx.setAttachment(Boolean.TRUE);
        request = "";
      } else {
        response = "Wrong password!\n";
        close = true;
      }
    }

    if (Boolean.TRUE.equals(ctx.getAttachment())) {
      if (request.isEmpty()) {
        response = "Type 'help' to see all available commands: ";
      } else if (request.toLowerCase().equals("exit")) {
        response = "Have a good day!\n";
        close = true;
      } else {
        Matcher m = COMMAND_ARGS_PATTERN.matcher(request);
        m.find();
        String command = m.group();

        ArrayList args;
        String arg;
        for (args = new ArrayList<>(); m.find(); args.add(arg)) {
          arg = m.group(1);
          if (arg == null) {
            arg = m.group(0);
          }
        }

        response = this.tryHandleCommand(command, (String[]) args.toArray(new String[0]));
      }
    }

    ChannelFuture future = e.getChannel().write(response.replaceAll("\n", "\r\n"));
    if (close) {
      future.addListener(ChannelFutureListener.CLOSE);
    }

  }

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    if (e.getCause() instanceof IOException) {
      e.getChannel().close();
    } else {
      log.error("exceptionCaught: eMessage={}, eClause={} eClass={}", ctx.getName(), e.getCause(), e.getClass());
    }

  }
}
