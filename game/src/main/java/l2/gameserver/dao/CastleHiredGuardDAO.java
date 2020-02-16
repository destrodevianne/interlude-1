//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
public class CastleHiredGuardDAO {
  private static final CastleHiredGuardDAO _instance = new CastleHiredGuardDAO();
  public static final String SELECT_SQL_QUERY = "SELECT * FROM castle_hired_guards WHERE residence_id=?";
  public static final String INSERT_SQL_QUERY = "INSERT INTO castle_hired_guards(residence_id, item_id, x, y, z) VALUES (?, ?, ?, ?, ?)";
  public static final String DELETE_SQL_QUERY = "DELETE FROM castle_hired_guards WHERE residence_id=?";
  public static final String DELETE_SQL_QUERY2 = "DELETE FROM castle_hired_guards WHERE residence_id=? AND item_id=? AND x=? AND y=? AND z=?";

  public CastleHiredGuardDAO() {
  }

  public static CastleHiredGuardDAO getInstance() {
    return _instance;
  }

  public void load(Castle r) {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM castle_hired_guards WHERE residence_id=?");
      statement.setInt(1, r.getId());
      rset = statement.executeQuery();

      while (rset.next()) {
        int itemId = rset.getInt("item_id");
        Location loc = new Location(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
        ItemInstance item = ItemFunctions.createItem(itemId);
        item.spawnMe(loc);
        r.getSpawnMerchantTickets().add(item);
      }
    } catch (Exception e) {
      log.error("load: eMessage={}, eClause={} eClass={}", e.getMessage(), e.getCause(), e.getClass());
    } finally {
      DbUtils.closeQuietly(con, statement, rset);
    }

  }

  public void insert(Residence residence, int itemId, Location loc) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO castle_hired_guards(residence_id, item_id, x, y, z) VALUES (?, ?, ?, ?, ?)");
      statement.setInt(1, residence.getId());
      statement.setInt(2, itemId);
      statement.setInt(3, loc.x);
      statement.setInt(4, loc.y);
      statement.setInt(5, loc.z);
      statement.execute();
    } catch (Exception var10) {
      log.error("CastleHiredGuardDAO:insert(Residence, int, Location): " + var10, var10);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  public void delete(Residence residence, ItemInstance item) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM castle_hired_guards WHERE residence_id=? AND item_id=? AND x=? AND y=? AND z=?");
      statement.setInt(1, residence.getId());
      statement.setInt(2, item.getItemId());
      statement.setInt(3, item.getLoc().x);
      statement.setInt(4, item.getLoc().y);
      statement.setInt(5, item.getLoc().z);
      statement.execute();
    } catch (Exception var9) {
      log.error("CastleHiredGuardDAO:delete(Residence): " + var9, var9);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  public void delete(Residence residence) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM castle_hired_guards WHERE residence_id=?");
      statement.setInt(1, residence.getId());
      statement.execute();
    } catch (Exception var8) {
      log.error("CastleHiredGuardDAO:delete(Residence): " + var8, var8);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }
}
