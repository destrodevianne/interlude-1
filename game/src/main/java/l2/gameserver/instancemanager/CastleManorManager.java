//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.instancemanager;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Manor;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.items.Warehouse;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.templates.manor.CropProcure;
import l2.gameserver.templates.manor.SeedProduction;
import l2.gameserver.utils.Log;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class CastleManorManager {
  private static CastleManorManager _instance;
  public static final int PERIOD_CURRENT = 0;
  public static final int PERIOD_NEXT = 1;
  protected static final String var_name = "ManorApproved";
  private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
  private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
  private static final int NEXT_PERIOD_APPROVE;
  private static final int NEXT_PERIOD_APPROVE_MIN;
  private static final int MANOR_REFRESH;
  private static final int MANOR_REFRESH_MIN;
  protected static final long MAINTENANCE_PERIOD;
  private boolean _underMaintenance;
  private boolean _disabled;

  public static CastleManorManager getInstance() {
    if (_instance == null) {
      log.info("Manor System: Initializing...");
      _instance = new CastleManorManager();
    }

    return _instance;
  }

  private CastleManorManager() {
    this.load();
    this.init();
    this._underMaintenance = false;
    this._disabled = !Config.ALLOW_MANOR;
    List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);

    for (Castle c : castleList) {
      c.setNextPeriodApproved(ServerVariables.getBool("ManorApproved"));
    }

  }

  private void load() {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);

      for(Iterator var5 = castleList.iterator(); var5.hasNext(); DbUtils.close(statement, rs)) {
        Castle castle = (Castle)var5.next();
        List<SeedProduction> production = new ArrayList<>();
        List<SeedProduction> productionNext = new ArrayList<>();
        List<CropProcure> procure = new ArrayList<>();
        List<CropProcure> procureNext = new ArrayList<>();
        statement = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
        statement.setInt(1, castle.getId());
        rs = statement.executeQuery();

        int cropId;
        long canBuy;
        long startBuy;
        while(rs.next()) {
          cropId = rs.getInt("seed_id");
          canBuy = rs.getLong("can_produce");
          startBuy = rs.getLong("start_produce");
          long price = rs.getLong("seed_price");
          int period = rs.getInt("period");
          if (period == 0) {
            production.add(new SeedProduction(cropId, canBuy, price, startBuy));
          } else {
            productionNext.add(new SeedProduction(cropId, canBuy, price, startBuy));
          }
        }

        DbUtils.close(statement, rs);
        castle.setSeedProduction(production, 0);
        castle.setSeedProduction(productionNext, 1);
        statement = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");
        statement.setInt(1, castle.getId());
        rs = statement.executeQuery();

        while(rs.next()) {
          cropId = rs.getInt("crop_id");
          canBuy = rs.getLong("can_buy");
          startBuy = rs.getLong("start_buy");
          int rewardType = rs.getInt("reward_type");
          long price = rs.getLong("price");
          int period = rs.getInt("period");
          if (period == 0) {
            procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
          } else {
            procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
          }
        }

        castle.setCropProcure(procure, 0);
        castle.setCropProcure(procureNext, 1);
        if (!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty()) {
          log.info("Manor System: Loaded data for " + castle.getName() + " castle");
        }
      }
    } catch (Exception var23) {
      log.error("Manor System: Error restoring manor data!", var23);
    } finally {
      DbUtils.closeQuietly(con, statement, rs);
    }

  }

  protected void init() {
    Calendar FirstDelay;
    if (ServerVariables.getString("ManorApproved", "").isEmpty()) {
      FirstDelay = Calendar.getInstance();
      FirstDelay.set(11, MANOR_REFRESH);
      FirstDelay.set(12, MANOR_REFRESH_MIN);
      FirstDelay.set(13, 0);
      FirstDelay.set(14, 0);
      Calendar periodApprove = Calendar.getInstance();
      periodApprove.set(11, NEXT_PERIOD_APPROVE);
      periodApprove.set(12, NEXT_PERIOD_APPROVE_MIN);
      periodApprove.set(13, 0);
      periodApprove.set(14, 0);
      boolean isApproved = periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && FirstDelay.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
      ServerVariables.set("ManorApproved", isApproved);
    }

    FirstDelay = Calendar.getInstance();
    FirstDelay.set(13, 0);
    FirstDelay.set(14, 0);
    FirstDelay.add(12, 1);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(new CastleManorManager.ManorTask(), FirstDelay.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 60000L);
  }

  public void setNextPeriod() {
    List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
    Iterator var2 = castleList.iterator();

    while(true) {
      Castle c;
      Clan clan;
      do {
        do {
          if (!var2.hasNext()) {
            return;
          }

          c = (Castle)var2.next();
        } while(c.getOwnerId() <= 0);

        clan = ClanTable.getInstance().getClan(c.getOwnerId());
      } while(clan == null);

      Warehouse cwh = clan.getWarehouse();

      for (CropProcure crop : c.getCropProcure(0)) {
        if (crop.getStartAmount() != 0L) {
          if (crop.getStartAmount() > crop.getAmount()) {
            log.info("Manor System [" + c.getName() + "]: Start Amount of Crop " + crop.getStartAmount() + " > Amount of current " + crop.getAmount());
            long count = crop.getStartAmount() - crop.getAmount();
            count = count * 90L / 100L;
            if (count < 1L && Rnd.get(99) < 90) {
              count = 1L;
            }

            if (count >= 1L) {
              int id = Manor.getInstance().getMatureCrop(crop.getId());
              cwh.addItem(id, count);
            }
          }

          if (crop.getAmount() > 0L) {
            c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice(), false, false);
            Log.add(c.getName() + "|" + crop.getAmount() * crop.getPrice() + "|ManorManager|" + crop.getAmount() + "*" + crop.getPrice(), "treasury");
          }

          c.setCollectedShops(0L);
          c.setCollectedSeed(0L);
        }
      }

      c.setSeedProduction(c.getSeedProduction(1), 0);
      c.setCropProcure(c.getCropProcure(1), 0);
      long manor_cost = c.getManorCost(0);
      if (c.getTreasury() < manor_cost) {
        c.setSeedProduction(this.getNewSeedsList(c.getId()), 1);
        c.setCropProcure(this.getNewCropsList(c.getId()), 1);
        Log.add(c.getName() + "|" + manor_cost + "|ManorManager Error@setNextPeriod", "treasury");
      } else {
        List<SeedProduction> production = new ArrayList<>();
        List<CropProcure> procure = new ArrayList<>();
        Iterator var14 = c.getSeedProduction(0).iterator();

        while(var14.hasNext()) {
          SeedProduction s = (SeedProduction)var14.next();
          s.setCanProduce(s.getStartProduce());
          production.add(s);
        }

        var14 = c.getCropProcure(0).iterator();

        while(var14.hasNext()) {
          CropProcure cr = (CropProcure)var14.next();
          cr.setAmount(cr.getStartAmount());
          procure.add(cr);
        }

        c.setSeedProduction(production, 1);
        c.setCropProcure(procure, 1);
      }

      c.saveCropData();
      c.saveSeedData();
      PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
      c.setNextPeriodApproved(false);
    }
  }

  public void approveNextPeriod() {
    List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);

    for (Castle c : castleList) {
      if (c.getOwnerId() > 0) {
        long manor_cost = c.getManorCost(1);
        if (c.getTreasury() < manor_cost) {
          c.setSeedProduction(this.getNewSeedsList(c.getId()), 1);
          c.setCropProcure(this.getNewCropsList(c.getId()), 1);
          manor_cost = c.getManorCost(1);
          if (manor_cost > 0L) {
            Log.add(c.getName() + "|" + -manor_cost + "|ManorManager Error@approveNextPeriod", "treasury");
          }

          Clan clan = c.getOwner();
          PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
        } else {
          c.addToTreasuryNoTax(-manor_cost, false, false);
          Log.add(c.getName() + "|" + -manor_cost + "|ManorManager", "treasury");
        }

        c.setNextPeriodApproved(true);
      }
    }

  }

  private List<SeedProduction> getNewSeedsList(int castleId) {
    List<SeedProduction> seeds = new ArrayList<>();
    List<Integer> seedsIds = Manor.getInstance().getSeedsForCastle(castleId);

    for (int sd : seedsIds) {
      seeds.add(new SeedProduction(sd));
    }

    return seeds;
  }

  private List<CropProcure> getNewCropsList(int castleId) {
    List<CropProcure> crops = new ArrayList<>();
    List<Integer> cropsIds = Manor.getInstance().getCropsForCastle(castleId);

    for (int cr : cropsIds) {
      crops.add(new CropProcure(cr));
    }

    return crops;
  }

  public boolean isUnderMaintenance() {
    return this._underMaintenance;
  }

  public void setUnderMaintenance(boolean mode) {
    this._underMaintenance = mode;
  }

  public boolean isDisabled() {
    return this._disabled;
  }

  public void setDisabled(boolean mode) {
    this._disabled = mode;
  }

  public SeedProduction getNewSeedProduction(int id, long amount, long price, long sales) {
    return new SeedProduction(id, amount, price, sales);
  }

  public CropProcure getNewCropProcure(int id, long amount, int type, long price, long buy) {
    return new CropProcure(id, amount, type, buy, price);
  }

  public void save() {
    List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);

    for (Castle c : castleList) {
      c.saveSeedData();
      c.saveCropData();
    }

  }

  static {
    NEXT_PERIOD_APPROVE = Config.MANOR_APPROVE_TIME;
    NEXT_PERIOD_APPROVE_MIN = Config.MANOR_APPROVE_MIN;
    MANOR_REFRESH = Config.MANOR_REFRESH_TIME;
    MANOR_REFRESH_MIN = Config.MANOR_REFRESH_MIN;
    MAINTENANCE_PERIOD = Config.MANOR_MAINTENANCE_PERIOD / '\uea60';
  }

  private class ManorTask extends RunnableImpl {
    private ManorTask() {
    }

    public void runImpl() throws Exception {
      int H = Calendar.getInstance().get(11);
      int M = Calendar.getInstance().get(12);
      if (ServerVariables.getBool("ManorApproved")) {
        if (H < CastleManorManager.NEXT_PERIOD_APPROVE || H > CastleManorManager.MANOR_REFRESH || H == CastleManorManager.MANOR_REFRESH && M >= CastleManorManager.MANOR_REFRESH_MIN) {
          ServerVariables.set("ManorApproved", false);
          CastleManorManager.this.setUnderMaintenance(true);
          log.info("Manor System: Under maintenance mode started");
        }
      } else if (CastleManorManager.this.isUnderMaintenance()) {
        if (H != CastleManorManager.MANOR_REFRESH || (long)M >= (long)CastleManorManager.MANOR_REFRESH_MIN + CastleManorManager.MAINTENANCE_PERIOD) {
          CastleManorManager.this.setUnderMaintenance(false);
          log.info("Manor System: Next period started");
          if (CastleManorManager.this.isDisabled()) {
            return;
          }

          CastleManorManager.this.setNextPeriod();

          try {
            CastleManorManager.this.save();
          } catch (Exception var4) {
            log.info("Manor System: Failed to save manor data: " + var4);
          }
        }
      } else if (H > CastleManorManager.NEXT_PERIOD_APPROVE && H < CastleManorManager.MANOR_REFRESH || H == CastleManorManager.NEXT_PERIOD_APPROVE && M >= CastleManorManager.NEXT_PERIOD_APPROVE_MIN) {
        ServerVariables.set("ManorApproved", true);
        log.info("Manor System: Next period approved");
        if (CastleManorManager.this.isDisabled()) {
          return;
        }

        CastleManorManager.this.approveNextPeriod();
      }

    }
  }
}
