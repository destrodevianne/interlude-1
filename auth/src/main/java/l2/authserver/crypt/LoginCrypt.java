package l2.authserver.crypt;

import java.io.IOException;
import l2.commons.util.Rnd;

public class LoginCrypt {
    private static final byte[] STATIC_BLOWFISH_KEY = new byte[]{107, 96, -53, 91, -126, -50, -112, -79, -52, 43, 108, 85, 108, 108, 108, 108};
    private NewCrypt _staticCrypt;
    private NewCrypt _crypt;
    private boolean _static = true;

    public LoginCrypt() {
    }

    public void setKey(byte[] key) {
        this._staticCrypt = new NewCrypt(STATIC_BLOWFISH_KEY);
        this._crypt = new NewCrypt(key);
    }

    public boolean decrypt(byte[] raw, int offset, int size) throws IOException {
        this._crypt.decrypt(raw, offset, size);
        return NewCrypt.verifyChecksum(raw, offset, size);
    }

    public int encrypt(byte[] raw, int offset, int size) throws IOException {
        size += 4;
        if (this._static) {
            size += 4;
            size += 8 - size % 8;
            NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
            this._staticCrypt.crypt(raw, offset, size);
            this._static = false;
        } else {
            size += 8 - size % 8;
            NewCrypt.appendChecksum(raw, offset, size);
            this._crypt.crypt(raw, offset, size);
        }

        return size;
    }
}
