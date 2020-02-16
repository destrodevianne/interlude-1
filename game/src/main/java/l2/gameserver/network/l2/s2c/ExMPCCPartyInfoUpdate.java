//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Party;
import l2.gameserver.model.Player;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket {
  private Party _party;
  Player _leader;
  private int _mode;
  private int _count;

  public ExMPCCPartyInfoUpdate(Party party, int mode) {
    this._party = party;
    this._mode = mode;
    this._count = this._party.getMemberCount();
    this._leader = this._party.getPartyLeader();
  }

  protected void writeImpl() {
    this.writeEx(90);
    this.writeS(this._leader.getName());
    this.writeD(this._leader.getObjectId());
    this.writeD(this._count);
    this.writeD(this._mode);
  }
}
