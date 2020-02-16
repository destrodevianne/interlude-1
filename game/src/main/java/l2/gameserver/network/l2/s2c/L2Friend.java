//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class L2Friend extends L2GameServerPacket {
  private boolean _add;
  private boolean _online;
  private String _name;
  private int _object_id;

  public L2Friend(Player player, boolean add) {
    this._add = add;
    this._name = player.getName();
    this._object_id = player.getObjectId();
    this._online = true;
  }

  public L2Friend(String name, boolean add, boolean online, int object_id) {
    this._name = name;
    this._add = add;
    this._object_id = object_id;
    this._online = online;
  }

  protected final void writeImpl() {
    this.writeC(251);
    this.writeD(this._add ? 1 : 3);
    this.writeD(0);
    this.writeS(this._name);
    this.writeD(this._online ? 1 : 0);
    this.writeD(this._object_id);
  }
}
