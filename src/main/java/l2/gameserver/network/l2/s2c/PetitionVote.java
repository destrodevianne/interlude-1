//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.s2c;

public class PetitionVote extends L2GameServerPacket {
  public PetitionVote() {
  }

  protected void writeImpl() {
    this.writeC(246);
  }
}
