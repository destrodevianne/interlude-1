//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.telnet.commands;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.telnet.TelnetCommand;
import l2.gameserver.network.telnet.TelnetCommandHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TelnetDebug implements TelnetCommandHolder {
  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetDebug() {
    this._commands.add(new TelnetCommand("dumpnpc", "dnpc") {
      public String getUsage() {
        return "dumpnpc";
      }

      public String handle(String[] args) {
        StringBuilder sb = new StringBuilder();
        int total = 0;
        int maxId = 0;
        int maxCount = 0;
        TIntObjectHashMap<List<NpcInstance>> npcStats = new TIntObjectHashMap();

        for (GameObject obj : GameObjectsStorage.getAllObjects()) {
          if (obj.isCreature() && obj.isNpc()) {
            NpcInstance npcx = (NpcInstance) obj;
            int id = npcx.getNpcId();
            List<NpcInstance> listx;
            if ((listx = npcStats.get(id)) == null) {
              listx = new ArrayList<>();
              npcStats.put(id, listx);
            }

            listx.add(npcx);
            if (listx.size() > maxCount) {
              maxId = id;
              maxCount = listx.size();
            }

            ++total;
          }
        }

        sb.append("Total NPCs: ").append(total).append("\n");
        sb.append("Maximum NPC ID: ").append(maxId).append(" count : ").append(maxCount).append("\n");
        TIntObjectIterator itr = npcStats.iterator();

        while (itr.hasNext()) {
          itr.advance();
          int idx = itr.key();
          List<NpcInstance> list = (List) itr.value();
          sb.append("=== ID: ").append(idx).append(" ").append(" Count: ").append(list.size()).append(" ===").append("\n");

          for (NpcInstance npc : list) {
            try {
              sb.append("AI: ");
              if (npc.hasAI()) {
                sb.append(npc.getAI().getClass().getName());
              } else {
                sb.append("none");
              }

              sb.append(", ");
              if (npc.getReflectionId() > 0) {
                sb.append("ref: ").append(npc.getReflectionId());
                sb.append(" - ").append(npc.getReflection().getName());
              }

              sb.append("loc: ").append(npc.getLoc());
              sb.append(", ");
              sb.append("spawned: ");
              sb.append(npc.isVisible());
              sb.append("\n");
            } catch (Exception e) {
              log.error("Exception: eMessage={}, eClass={}, eCause={}", e.getMessage(), e.getClass(), this.getClass().getSimpleName());
              e.printStackTrace();
            }
          }
        }

        try {
          (new File("stats")).mkdir();
          FileUtils.writeStringToFile(new File("stats/NpcStats-" + (new SimpleDateFormat("MMddHHmmss")).format(System.currentTimeMillis()) + ".txt"), sb.toString());
        } catch (IOException e) {
          log.error("Exception: eMessage={}, eClass={}, eCause={}", e.getMessage(), this.getClass().getSimpleName(), e.getCause());
        }

        return "NPC stats saved.\n";
      }
    });
    this._commands.add(new TelnetCommand("asrestart") {
      public String getUsage() {
        return "asrestart";
      }

      public String handle(String[] args) {
        AuthServerCommunication.getInstance().restart();
        return "Restarted.\n";
      }
    });
  }

  public Set<TelnetCommand> getCommands() {
    return this._commands;
  }
}
