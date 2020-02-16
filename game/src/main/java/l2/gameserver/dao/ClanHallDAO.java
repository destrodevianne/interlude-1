//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.dao;

import l2.commons.dao.JdbcEntityState;
import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.entity.residence.ClanHall;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
public class ClanHallDAO {
  private static final ClanHallDAO _instance = new ClanHallDAO();
  public static final String SELECT_SQL_QUERY = "SELECT siege_date, own_date, last_siege_date, auction_desc, auction_length, auction_min_bid, cycle, paid_cycle FROM clanhall WHERE id = ?";
  public static final String UPDATE_SQL_QUERY = "UPDATE clanhall SET siege_date=?, last_siege_date=?, own_date=?, auction_desc=?, auction_length=?, auction_min_bid=?, cycle=?, paid_cycle=? WHERE id=?";

  public ClanHallDAO() {
  }

  public static ClanHallDAO getInstance() {
    return _instance;
  }

  public void select(ClanHall clanHall) {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT siege_date, own_date, last_siege_date, auction_desc, auction_length, auction_min_bid, cycle, paid_cycle FROM clanhall WHERE id = ?");
      statement.setInt(1, clanHall.getId());
      rset = statement.executeQuery();
      if (rset.next()) {
        clanHall.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
        clanHall.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date"));
        clanHall.getOwnDate().setTimeInMillis(rset.getLong("own_date"));
        clanHall.setAuctionLength(rset.getInt("auction_length"));
        clanHall.setAuctionMinBid(rset.getLong("auction_min_bid"));
        clanHall.setAuctionDescription(rset.getString("auction_desc"));
        clanHall.setCycle(rset.getInt("cycle"));
        clanHall.setPaidCycle(rset.getInt("paid_cycle"));
      }
    } catch (Exception var9) {
      log.error("ClanHallDAO.select(ClanHall):" + var9, var9);
    } finally {
      DbUtils.closeQuietly(con, statement, rset);
    }

  }

  public void update(ClanHall c) {
    if (c.getJdbcState().isUpdatable()) {
      c.setJdbcState(JdbcEntityState.STORED);
      this.update0(c);
    }
  }

  private void update0(ClanHall c) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clanhall SET siege_date=?, last_siege_date=?, own_date=?, auction_desc=?, auction_length=?, auction_min_bid=?, cycle=?, paid_cycle=? WHERE id=?");
      statement.setLong(1, c.getSiegeDate().getTimeInMillis());
      statement.setLong(2, c.getLastSiegeDate().getTimeInMillis());
      statement.setLong(3, c.getOwnDate().getTimeInMillis());
      statement.setString(4, c.getAuctionDescription());
      statement.setInt(5, c.getAuctionLength());
      statement.setLong(6, c.getAuctionMinBid());
      statement.setInt(7, c.getCycle());
      statement.setInt(8, c.getPaidCycle());
      statement.setInt(9, c.getId());
      statement.execute();
    } catch (Exception var8) {
      log.error("ClanHallDAO#update0(ClanHall): " + var8, var8);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }
}
