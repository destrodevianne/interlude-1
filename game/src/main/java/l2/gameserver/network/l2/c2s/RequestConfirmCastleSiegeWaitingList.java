//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.network.l2.c2s;

import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.CastleSiegeDefenderList;

public class RequestConfirmCastleSiegeWaitingList extends L2GameClientPacket {
  private boolean _approved;
  private int _unitId;
  private int _clanId;

  public RequestConfirmCastleSiegeWaitingList() {
  }

  protected void readImpl() {
    this._unitId = this.readD();
    this._clanId = this.readD();
    this._approved = this.readD() == 1;
  }

  protected void runImpl() {
    Player player = ((GameClient)this.getClient()).getActiveChar();
    if (player != null) {
      if (player.getClan() != null) {
        Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, this._unitId);
        if (castle != null && player.getClan().getCastle() == castle.getId()) {
          CastleSiegeEvent siegeEvent = (CastleSiegeEvent)castle.getSiegeEvent();
          SiegeClanObject siegeClan = siegeEvent.getSiegeClan("defenders_waiting", this._clanId);
          if (siegeClan == null) {
            siegeClan = siegeEvent.getSiegeClan("defenders", this._clanId);
          }

          if (siegeClan != null) {
            if ((player.getClanPrivileges() & 131072) != 131072) {
              player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST);
            } else if (siegeEvent.isRegistrationOver()) {
              player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED);
            } else {
              int allSize = siegeEvent.getObjects("defenders").size();
              if (allSize >= 20) {
                player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
              } else {
                siegeEvent.removeObject(siegeClan.getType(), siegeClan);
                if (this._approved) {
                  siegeClan.setType("defenders");
                } else {
                  siegeClan.setType("defenders_refused");
                }

                siegeEvent.addObject(siegeClan.getType(), siegeClan);
                SiegeClanDAO.getInstance().update(castle, siegeClan);
                player.sendPacket(new CastleSiegeDefenderList(castle));
              }
            }
          }
        } else {
          player.sendActionFailed();
        }
      }
    }
  }
}
