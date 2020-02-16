package l2.authserver.network.gamecomm.as2gs;

import l2.authserver.network.gamecomm.SendablePacket;

public class NotifyPwdCngResult extends SendablePacket {
    public static final int RESULT_OK = 1;
    public static final int RESULT_WRONG_OLD_PASSWORD = 2;
    public static final int RESULT_WRONG_NEW_PASSWORD = 3;
    public static final int RESULT_WRONG_ACCOUNT = 4;
    private int _requestor_oid;
    private int _result;

    public NotifyPwdCngResult(int requestor_oid, int result) {
        this._requestor_oid = requestor_oid;
        this._result = result;
    }

    protected void writeImpl() {
        this.writeC(161);
        this.writeD(this._requestor_oid);
        this.writeD(this._result);
    }
}
