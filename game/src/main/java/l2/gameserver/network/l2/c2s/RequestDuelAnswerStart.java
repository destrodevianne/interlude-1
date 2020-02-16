//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.EventHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Request;
import l2.gameserver.model.Request.L2RequestType;
import l2.gameserver.model.entity.events.EventType;
import l2.gameserver.model.entity.events.impl.DuelEvent;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;

public class RequestDuelAnswerStart extends L2GameClientPacket {
  private int _response;
  private int _duelType;

  public RequestDuelAnswerStart() {
  }

  protected void readImpl() {
    this._duelType = this.readD();
    this.readD();
    this._response = this.readD();
  }

  protected void runImpl() {
    Player activeChar = ((GameClient)this.getClient()).getActiveChar();
    if (activeChar != null) {
      Request request = activeChar.getRequest();
      if (request != null && request.isTypeOf(L2RequestType.DUEL)) {
        if (!request.isInProgress()) {
          request.cancel();
          activeChar.sendActionFailed();
        } else if (activeChar.isActionsDisabled()) {
          request.cancel();
          activeChar.sendActionFailed();
        } else {
          Player requestor = request.getRequestor();
          if (requestor == null) {
            request.cancel();
            activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
            activeChar.sendActionFailed();
          } else if (requestor.getRequest() != request) {
            request.cancel();
            activeChar.sendActionFailed();
          } else if (this._duelType != request.getInteger("duelType")) {
            request.cancel();
            activeChar.sendActionFailed();
          } else {
            DuelEvent duelEvent = (DuelEvent)EventHolder.getInstance().getEvent(EventType.PVP_EVENT, this._duelType);
            switch(this._response) {
              case -1:
                request.cancel();
                requestor.sendPacket((new SystemMessage2(SystemMsg.C1_IS_SET_TO_REFUSE_DUEL_REQUESTS_AND_CANNOT_RECEIVE_A_DUEL_REQUEST)).addName(activeChar));
                break;
              case 0:
                request.cancel();
                if (this._duelType == 1) {
                  requestor.sendPacket(SystemMsg.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
                } else {
                  requestor.sendPacket((new SystemMessage2(SystemMsg.C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL)).addName(activeChar));
                }
                break;
              case 1:
                if (!duelEvent.canDuel(requestor, activeChar, false)) {
                  request.cancel();
                  return;
                }

                SystemMessage2 msg1;
                SystemMessage2 msg2;
                if (this._duelType == 1) {
                  msg1 = new SystemMessage2(SystemMsg.YOU_HAVE_ACCEPTED_C1S_CHALLENGE_TO_A_PARTY_DUEL);
                  msg2 = new SystemMessage2(SystemMsg.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY);
                } else {
                  msg1 = new SystemMessage2(SystemMsg.YOU_HAVE_ACCEPTED_C1S_CHALLENGE_A_DUEL);
                  msg2 = new SystemMessage2(SystemMsg.C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL);
                }

                activeChar.sendPacket(msg1.addName(requestor));
                requestor.sendPacket(msg2.addName(activeChar));

                try {
                  duelEvent.createDuel(requestor, activeChar);
                } finally {
                  request.done();
                }
            }

          }
        }
      }
    }
  }
}
