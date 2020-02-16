//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.data.xml.parser;

import l2.commons.data.xml.AbstractDirParser;
import l2.commons.geometry.Polygon;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.DoorHolder;
import l2.gameserver.templates.DoorTemplate;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class DoorParser extends AbstractDirParser<DoorHolder> {
  private static final DoorParser _instance = new DoorParser();

  public static DoorParser getInstance() {
    return _instance;
  }

  protected DoorParser() {
    super(DoorHolder.getInstance());
  }

  public File getXMLDir() {
    return new File(Config.DATAPACK_ROOT, "data/doors/");
  }

  public boolean isIgnored(File f) {
    return false;
  }

  public String getDTDFileName() {
    return "doors.dtd";
  }

  private StatsSet initBaseStats() {
    StatsSet baseDat = new StatsSet();
    baseDat.set("level", 0);
    baseDat.set("baseSTR", 0);
    baseDat.set("baseCON", 0);
    baseDat.set("baseDEX", 0);
    baseDat.set("baseINT", 0);
    baseDat.set("baseWIT", 0);
    baseDat.set("baseMEN", 0);
    baseDat.set("baseShldDef", 0);
    baseDat.set("baseShldRate", 0);
    baseDat.set("baseAccCombat", 38);
    baseDat.set("baseEvasRate", 38);
    baseDat.set("baseCritRate", 38);
    baseDat.set("baseAtkRange", 0);
    baseDat.set("baseMpMax", 0);
    baseDat.set("baseCpMax", 0);
    baseDat.set("basePAtk", 0);
    baseDat.set("baseMAtk", 0);
    baseDat.set("basePAtkSpd", 0);
    baseDat.set("baseMAtkSpd", 0);
    baseDat.set("baseWalkSpd", 0);
    baseDat.set("baseRunSpd", 0);
    baseDat.set("baseHpReg", 0);
    baseDat.set("baseCpReg", 0);
    baseDat.set("baseMpReg", 0);
    return baseDat;
  }

  protected void readData(Element rootElement) {
    Iterator iterator = rootElement.elementIterator();

    while(true) {
      Element doorElement;
      do {
        if (!iterator.hasNext()) {
          return;
        }

        doorElement = (Element)iterator.next();
      } while(!"door".equals(doorElement.getName()));

      StatsSet doorSet = this.initBaseStats();
      StatsSet aiParams = null;
      doorSet.set("door_type", doorElement.attributeValue("type"));
      Element posElement = doorElement.element("pos");
      int x = Integer.parseInt(posElement.attributeValue("x"));
      int y = Integer.parseInt(posElement.attributeValue("y"));
      int z = Integer.parseInt(posElement.attributeValue("z"));
      Location doorPos;
      doorSet.set("pos", doorPos = new Location(x, y, z));
      Polygon shape = new Polygon();
      Element shapeElement = doorElement.element("shape");
      int minz = Integer.parseInt(shapeElement.attributeValue("minz"));
      int maxz = Integer.parseInt(shapeElement.attributeValue("maxz"));
      shape.add(Integer.parseInt(shapeElement.attributeValue("ax")), Integer.parseInt(shapeElement.attributeValue("ay")));
      shape.add(Integer.parseInt(shapeElement.attributeValue("bx")), Integer.parseInt(shapeElement.attributeValue("by")));
      shape.add(Integer.parseInt(shapeElement.attributeValue("cx")), Integer.parseInt(shapeElement.attributeValue("cy")));
      shape.add(Integer.parseInt(shapeElement.attributeValue("dx")), Integer.parseInt(shapeElement.attributeValue("dy")));
      shape.setZmin(minz);
      shape.setZmax(maxz);
      doorSet.set("shape", shape);
      doorPos.setZ(minz + 32);
      Iterator i = doorElement.elementIterator();

      while(true) {
        while(i.hasNext()) {
          Element n = (Element)i.next();
          if ("set".equals(n.getName())) {
            doorSet.set(n.attributeValue("name"), n.attributeValue("value"));
          } else if ("ai_params".equals(n.getName())) {
            if (aiParams == null) {
              aiParams = new StatsSet();
              doorSet.set("ai_params", aiParams);
            }

            Iterator aiParamsIterator = n.elementIterator();

            while(aiParamsIterator.hasNext()) {
              Element aiParamElement = (Element)aiParamsIterator.next();
              aiParams.set(aiParamElement.attributeValue("name"), aiParamElement.attributeValue("value"));
            }
          }
        }

        doorSet.set("uid", doorElement.attributeValue("id"));
        doorSet.set("name", doorElement.attributeValue("name"));
        doorSet.set("baseHpMax", doorElement.attributeValue("hp"));
        doorSet.set("basePDef", doorElement.attributeValue("pdef"));
        doorSet.set("baseMDef", doorElement.attributeValue("mdef"));
        doorSet.set("collision_height", maxz - minz & '\ufff0');
        doorSet.set("collision_radius", Math.max(50, Math.min(doorPos.x - shape.getXmin(), doorPos.y - shape.getYmin())));
        DoorTemplate template = new DoorTemplate(doorSet);
        this.getHolder().addTemplate(template);
        break;
      }
    }
  }
}
