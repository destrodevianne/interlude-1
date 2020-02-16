//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.entity.residence.Residence;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SiegePlayerDAO {
  private static final SiegePlayerDAO _instance = new SiegePlayerDAO();
  public static final String INSERT_SQL_QUERY = "INSERT INTO siege_players(residence_id, object_id, clan_id) VALUES (?,?,?)";
  public static final String DELETE_SQL_QUERY = "DELETE FROM siege_players WHERE residence_id=? AND object_id=? AND clan_id=?";
  public static final String DELETE_SQL_QUERY2 = "DELETE FROM siege_players WHERE residence_id=?";
  public static final String SELECT_SQL_QUERY = "SELECT object_id FROM siege_players WHERE residence_id=? AND clan_id=?";

  public SiegePlayerDAO() {
  }

  public static SiegePlayerDAO getInstance() {
    return _instance;
  }

  public List<Integer> select(Residence residence, int clanId) {
    List<Integer> set = new ArrayList<>();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT object_id FROM siege_players WHERE residence_id=? AND clan_id=?");
      statement.setInt(1, residence.getId());
      statement.setInt(2, clanId);
      rset = statement.executeQuery();

      while(rset.next()) {
        set.add(rset.getInt("object_id"));
      }
    } catch (Exception var11) {
      log.error("SiegePlayerDAO.select(Residence, int): " + var11, var11);
    } finally {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return set;
  }

  public void insert(Residence residence, int clanId, int playerId) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO siege_players(residence_id, object_id, clan_id) VALUES (?,?,?)");
      statement.setInt(1, residence.getId());
      statement.setInt(2, playerId);
      statement.setInt(3, clanId);
      statement.execute();
    } catch (Exception var10) {
      log.error("SiegePlayerDAO.insert(Residence, int, int): " + var10, var10);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  public void delete(Residence residence, int clanId, int playerId) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM siege_players WHERE residence_id=? AND object_id=? AND clan_id=?");
      statement.setInt(1, residence.getId());
      statement.setInt(2, playerId);
      statement.setInt(3, clanId);
      statement.execute();
    } catch (Exception var10) {
      log.error("SiegePlayerDAO.delete(Residence, int, int): " + var10, var10);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  public void delete(Residence residence) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM siege_players WHERE residence_id=?");
      statement.setInt(1, residence.getId());
      statement.execute();
    } catch (Exception var8) {
      log.error("SiegePlayerDAO.delete(Residence): " + var8, var8);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }
}
