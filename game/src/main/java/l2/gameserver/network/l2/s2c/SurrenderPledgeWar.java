//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.s2c;

public class SurrenderPledgeWar extends L2GameServerPacket {
  private String _pledgeName;
  private String _char;

  public SurrenderPledgeWar(String pledge, String charName) {
    this._pledgeName = pledge;
    this._char = charName;
  }

  protected final void writeImpl() {
    this.writeC(105);
    this.writeS(this._pledgeName);
    this.writeS(this._char);
  }
}
