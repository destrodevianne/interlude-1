//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.s2c;

public class CharacterCreateFail extends L2GameServerPacket {
  public static final L2GameServerPacket REASON_CREATION_FAILED = new CharacterCreateFail(0);
  public static final L2GameServerPacket REASON_TOO_MANY_CHARACTERS = new CharacterCreateFail(1);
  public static final L2GameServerPacket REASON_NAME_ALREADY_EXISTS = new CharacterCreateFail(2);
  public static final L2GameServerPacket REASON_16_ENG_CHARS = new CharacterCreateFail(3);
  private int _error;

  private CharacterCreateFail(int errorCode) {
    this._error = errorCode;
  }

  protected final void writeImpl() {
    this.writeC(26);
    this.writeD(this._error);
  }
}
