//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.s2c;

public class AttackDeadTarget extends L2GameServerPacket {
  public AttackDeadTarget() {
  }

  protected void writeImpl() {
    this.writeC(10);
  }
}
