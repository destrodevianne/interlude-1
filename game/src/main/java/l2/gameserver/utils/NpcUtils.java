//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.utils;

import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.GameObjectTasks.DeleteTask;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class NpcUtils {
  public NpcUtils() {
  }

  public static NpcInstance spawnSingle(int npcId, int x, int y, int z) {
    return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0L, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, int x, int y, int z, long despawnTime) {
    return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, despawnTime, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, long despawnTime) {
    return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.DEFAULT, despawnTime, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, Location loc) {
    return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0L, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, Location loc, long despawnTime) {
    return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection) {
    return spawnSingle(npcId, loc, reflection, 0L, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime) {
    return spawnSingle(npcId, loc, reflection, despawnTime, (String)null);
  }

  public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime, String title) {
    NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
    if (template == null) {
      throw new NullPointerException("Npc template id : " + npcId + " not found!");
    } else {
      NpcInstance npc = template.getNewInstance();
      npc.setHeading(loc.h < 0 ? Rnd.get(65535) : loc.h);
      npc.setSpawnedLoc(loc);
      npc.setReflection(reflection);
      npc.setCurrentHpMp((double)npc.getMaxHp(), (double)npc.getMaxMp(), true);
      if (title != null) {
        npc.setTitle(title);
      }

      npc.spawnMe(npc.getSpawnedLoc());
      if (despawnTime > 0L) {
        ThreadPoolManager.getInstance().schedule(new DeleteTask(npc), despawnTime);
      }

      return npc;
    }
  }
}
