//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.model.entity.oly;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.config.Config;
import l2.gameserver.data.StringHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.database.MysqlInitializer;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.entity.HeroDiary;
import l2.gameserver.model.entity.oly.CompetitionController.CompetitionResults;
import l2.gameserver.model.entity.oly.NoblesController.NobleRecord;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableInt;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HeroController {
  public static final int[] HERO_WEAPONS = new int[]{6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390};
  private static final String SQL_GET_HEROES = "SELECT  `oly_heroes`.`char_id` AS `char_id`, `oly_nobles`.`char_name` AS `name`, `oly_nobles`.`class_id` AS `class_id`, `oly_heroes`.`count` AS `count`, `oly_heroes`.`played` AS `played`, `oly_heroes`.`active` AS `active`, `oly_heroes`.`message` AS `message` FROM    `oly_heroes`,`oly_nobles` WHERE   `oly_heroes`.`char_id` = `oly_nobles`.`char_id`";
  private static final String SQL_SET_HEROES = "REPLACE INTO `oly_heroes` (`char_id`, `count`, `played`, `active`, `message`) VALUES (?, ?, ?, ?, ?)";
  private static HeroController _instance;
  private ArrayList<HeroController.HeroRecord> _currentHeroes = new ArrayList<>();
  private ArrayList<HeroController.HeroRecord> _allHeroes = new ArrayList<>();
  private static Map<Integer, List<HeroDiary>> _herodiary;

  public static HeroController getInstance() {
    if (_instance == null) {
      _instance = new HeroController();
    }

    return _instance;
  }

  private HeroController() {
    _herodiary = new ConcurrentHashMap<>();
    Map<Integer, String> _heroMessage = new ConcurrentHashMap<>();
    this.loadHeroes();
  }

  private synchronized Collection<NobleRecord> CalcHeroesContenders() {
    log.info("HeroController: Calculating heroes contenders.");
    HashMap<ClassId, NobleRecord> hero_contenders_map = new HashMap<>();
    Iterator var2 = NoblesController.getInstance().getNoblesRecords().iterator();

    NobleRecord nr;
    while(var2.hasNext()) {
      nr = (NobleRecord)var2.next();

      try {
        if (nr.comp_done >= Config.OLY_MIN_HERO_COMPS && nr.comp_win >= Config.OLY_MIN_HERO_WIN) {
          ClassId cid = null;
          ClassId[] var5 = ClassId.values();

          for (ClassId cid2 : var5) {
            if (cid2.getId() == nr.class_id && cid2.level() == 3) {
              cid = cid2;
            }
          }

          if (cid == null) {
            log.warn("HeroController: Not third or null ClassID for character '" + nr.char_name + "'");
          } else if (hero_contenders_map.containsKey(cid)) {
            NobleRecord nr2 = hero_contenders_map.get(cid);
            if (nr.points_current > nr2.points_current || nr.points_current == nr2.points_current && nr.comp_win > nr2.comp_win) {
              hero_contenders_map.put(cid, nr);
            }
          } else {
            hero_contenders_map.put(cid, nr);
          }
        }
      } catch (Exception var9) {
        log.warn("HeroController: Exception while claculating new heroes", var9);
      }
    }

    var2 = hero_contenders_map.values().iterator();

    while(var2.hasNext()) {
      nr = (NobleRecord)var2.next();
      Log.add(String.format("HeroController: %s(%d) pretended to be a hero. points_current = %d", nr.char_name, nr.char_id, nr.points_current), "olympiad");
    }

    return new ArrayList<>(hero_contenders_map.values());
  }

  private void loadHeroes() {
    Connection conn = null;
    Statement stmt = null;
    ResultSet rset = null;

    try {
      conn = DatabaseFactory.getInstance().getConnection();
      stmt = conn.createStatement();

      HeroController.HeroRecord nr;
      for(rset = stmt.executeQuery("SELECT  `oly_heroes`.`char_id` AS `char_id`, `oly_nobles`.`char_name` AS `name`, `oly_nobles`.`class_id` AS `class_id`, `oly_heroes`.`count` AS `count`, `oly_heroes`.`played` AS `played`, `oly_heroes`.`active` AS `active`, `oly_heroes`.`message` AS `message` FROM    `oly_heroes`,`oly_nobles` WHERE   `oly_heroes`.`char_id` = `oly_nobles`.`char_id`"); rset.next(); this._allHeroes.add(nr)) {
        int char_id = rset.getInt("char_id");
        String name = rset.getString("name");
        int class_id = rset.getInt("class_id");
        int count = rset.getInt("count");
        boolean played = rset.getInt("played") != 0;
        boolean active = rset.getInt("active") != 0;
        String message = rset.getString("message");
        nr = new HeroController.HeroRecord(char_id, name, class_id, count, active, played, message);
        if (played) {
          this._currentHeroes.add(nr);
        }
      }

      this.applyClanAndAlly();
    } catch (Exception var15) {
      log.warn("Exception while loading heroes", var15);
    } finally {
      DbUtils.closeQuietly(conn, stmt, rset);
    }

  }

  public void saveHeroes() {
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = DatabaseFactory.getInstance().getConnection();
      pstmt = conn.prepareStatement("REPLACE INTO `oly_heroes` (`char_id`, `count`, `played`, `active`, `message`) VALUES (?, ?, ?, ?, ?)");

      for (HeroRecord nr : this._allHeroes) {
        pstmt.setInt(1, nr.char_id);
        pstmt.setInt(2, nr.count);
        pstmt.setInt(3, nr.played ? 1 : 0);
        pstmt.setInt(4, nr.active ? 1 : 0);
        pstmt.setString(5, nr.message);
        pstmt.executeUpdate();
      }
    } catch (Exception var8) {
      log.warn("Exception while saving heroes", var8);
    } finally {
      DbUtils.closeQuietly(conn, pstmt);
    }

  }

  private void clearHeroes() {
    log.info("HeroController: Clearing previus season heroes.");
    MysqlInitializer.set("UPDATE `oly_heroes` SET `played` = 0, `active` = 0");
    if (!this._currentHeroes.isEmpty()) {
      Iterator var1 = this._currentHeroes.iterator();

      label58:
      while(true) {
        HeroController.HeroRecord nr;
        do {
          if (!var1.hasNext()) {
            break label58;
          }

          nr = (HeroController.HeroRecord)var1.next();
        } while(!nr.active);

        Player player = GameObjectsStorage.getPlayer(nr.char_id);
        if (player != null) {
          player.getInventory().unEquipItemInBodySlot(256);
          player.getInventory().unEquipItemInBodySlot(128);
          player.getInventory().unEquipItemInBodySlot(16384);
          player.getInventory().unEquipItemInBodySlot(65536);
          player.getInventory().unEquipItemInBodySlot(524288);
          player.getInventory().unEquipItemInBodySlot(262144);
          ItemInstance[] var4 = player.getInventory().getItems();
          int var5 = var4.length;

          int var6;
          ItemInstance item;
          for(var6 = 0; var6 < var5; ++var6) {
            item = var4[var6];
            if (item != null && item.isHeroWeapon()) {
              player.getInventory().destroyItem(item);
            }
          }

          var4 = player.getWarehouse().getItems();
          var5 = var4.length;

          for(var6 = 0; var6 < var5; ++var6) {
            item = var4[var6];
            if (item != null && !item.isEquipable() && item.isHeroWeapon()) {
              player.getWarehouse().destroyItem(item);
            }
          }

          player.unsetVar("CustomHeroEndTime");
          player.setHero(false);
          player.updatePledgeClass();
          player.broadcastUserInfo(true);
          removeAllHeroWeapons(player);
        }

        nr.played = false;
        nr.active = false;
      }
    }

    this.saveHeroes();
    this._currentHeroes.clear();
  }

  public synchronized void ComputeNewHeroNobleses() {
    log.info("HeroController: Computing new heroes.");

    try {
      NoblesController.getInstance().SaveNobleses();
      this.saveHeroes();
      Collection<NobleRecord> hContenders = this.CalcHeroesContenders();
      this.clearHeroes();

      for (NobleRecord hnr : hContenders) {
        HeroRecord hr = null;

        for (HeroRecord hr2 : this._allHeroes) {
          if (hnr.char_id == hr2.char_id) {
            hr = hr2;
          }
        }

        if (hr == null) {
          hr = new HeroRecord(hnr.char_id, hnr.char_name, hnr.class_id, 0, false, true, "");
          this._allHeroes.add(hr);
        }

        ++hr.count;
        hr.played = true;
        this._currentHeroes.add(hr);
      }

      this.saveHeroes();
      NoblesController.getInstance().TransactNewSeason();
      NoblesController.getInstance().ComputeRanks();
      NoblesController.getInstance().SaveNobleses();
      this.applyClanAndAlly();
    } catch (Exception var7) {
      log.warn("HeroController: Can't compute heroes.", var7);
    }

  }

  private void applyClanAndAlly() {

    for (HeroRecord hr : this._currentHeroes) {
      if (hr != null) {
        Entry<Clan, Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(hr.char_id);
        if (e.getKey() != null) {
          hr.clan_name = e.getKey().getName();
          hr.clan_crest = e.getKey().getCrestId();
        } else {
          hr.clan_name = "";
          hr.clan_crest = 0;
        }

        if (e.getValue() != null) {
          hr.ally_name = e.getValue().getAllyName();
          hr.ally_crest = e.getValue().getAllyCrestId();
        } else {
          hr.ally_name = "";
          hr.ally_crest = 0;
        }
      }
    }

  }

  public Collection<HeroController.HeroRecord> getCurrentHeroes() {
    return this._currentHeroes;
  }

  public boolean isCurrentHero(Player player) {
    if (player == null) {
      return false;
    } else {
      return !this._currentHeroes.isEmpty() && this.isCurrentHero(player.getObjectId());
    }
  }

  public boolean isInactiveHero(Player player) {
    return player != null && this.isInactiveHero(player.getObjectId());
  }

  public void activateHero(Player player) {
    if (player != null) {
      if (!this._currentHeroes.isEmpty()) {

        for (HeroRecord hr : this._currentHeroes) {
          if (hr.char_id == player.getObjectId() && hr.played) {
            hr.active = true;
            if (player.getBaseClassId() == player.getActiveClassId()) {
              addSkills(player);
            }

            player.setHero(true);
            player.unsetVar("CustomHeroEndTime");
            player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
            player.updatePledgeClass();
            player.getPlayer().sendUserInfo(true);
            if (player.getClan() != null && player.getClan().getLevel() >= 5) {
              player.getClan().incReputation(1000, true, "Hero:activateHero:" + player);
              player.getClan().broadcastToOtherOnlineMembers((new SystemMessage(1776)).addString(player.getName()).addNumber(Math.round(1000.0D * Config.RATE_CLAN_REP_SCORE)), player);
            }

            player.broadcastUserInfo(true);
            this.saveHeroes();
          }
        }

      }
    }
  }

  public boolean isCurrentHero(int obj_id) {
    if (this._currentHeroes.isEmpty()) {
      return false;
    } else {
      Iterator var2 = this._currentHeroes.iterator();

      HeroController.HeroRecord hr;
      do {
        if (!var2.hasNext()) {
          return false;
        }

        hr = (HeroController.HeroRecord)var2.next();
      } while(hr.char_id != obj_id);

      return hr.active && hr.played;
    }
  }

  public boolean isInactiveHero(int obj_id) {
    if (this._currentHeroes.isEmpty()) {
      return false;
    } else {
      Iterator var2 = this._currentHeroes.iterator();

      HeroController.HeroRecord hr;
      do {
        if (!var2.hasNext()) {
          return false;
        }

        hr = (HeroController.HeroRecord)var2.next();
      } while(hr.char_id != obj_id);

      return hr.played && !hr.active;
    }
  }

  public static void addSkills(Player player) {
    player.addSkill(SkillTable.getInstance().getInfo(395, 1));
    player.addSkill(SkillTable.getInstance().getInfo(396, 1));
    player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
    player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
    player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
  }

  public static void removeSkills(Player player) {
    player.removeSkillById(395);
    player.removeSkillById(396);
    player.removeSkillById(1374);
    player.removeSkillById(1375);
    player.removeSkillById(1376);
  }

  public void showHistory(Player player, int targetClassId, int page) {
    HeroController.HeroRecord hr = null;

    for (HeroRecord hr2 : this.getCurrentHeroes()) {
      if (hr2.active && hr2.played && hr2.class_id == targetClassId) {
        hr = hr2;
      }
    }

    if (hr != null) {
      NpcHtmlMessage html = new NpcHtmlMessage(player, null);
      html.setFile("oly/monument_hero_info.htm");
      html.replace("%title%", StringHolder.getInstance().getNotNull(player, "hero.history"));
      Collection<CompetitionResults> crs_list = hr.getCompetitions();
      CompetitionResults[] crs = crs_list.toArray(new CompetitionResults[0]);
      int allStatWinner = 0;
      int allStatLoss = 0;
      int allStatTie = 0;
      int max = crs.length;

      for(int var14 = 0; var14 < max; ++var14) {
        CompetitionResults h = crs[var14];
        if (h.result > 0) {
          ++allStatWinner;
        } else if (h.result < 0) {
          ++allStatLoss;
        } else if (h.result == 0) {
          ++allStatTie;
        }
      }

      html.replace("%wins%", String.valueOf(allStatWinner));
      html.replace("%ties%", String.valueOf(allStatTie));
      html.replace("%losses%", String.valueOf(allStatLoss));
      int min = 15 * (page - 1);
      max = 15 * page;
      MutableInt currentWinner = new MutableInt(0);
      MutableInt currentLoss = new MutableInt(0);
      MutableInt currentTie = new MutableInt(0);
      StringBuilder b = new StringBuilder(500);

      for(int i = 0; i < crs.length; ++i) {
        CompetitionResults h = crs[i];
        if (h.result > 0) {
          currentWinner.increment();
        } else if (h.result < 0) {
          currentLoss.increment();
        } else {
          currentTie.increment();
        }

        if (i >= min) {
          if (i >= max) {
            break;
          }

          b.append("<tr><td>");
          b.append(h.toString(player, currentWinner, currentLoss, currentTie));
          b.append("</td></tr");
        }
      }

      if (min > 0) {
        html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
        html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page - 1));
      } else {
        html.replace("%buttprev%", "");
      }

      if (crs.length > max) {
        html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
        html.replace("%prev_bypass%", "_match?class=" + targetClassId + "&page=" + (page + 1));
      } else {
        html.replace("%buttnext%", "");
      }

      html.replace("%list%", b.toString());
      player.sendPacket(html);
    }
  }

  public void loadDiary(int charId) {
    List<HeroDiary> diary = new ArrayList<>();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
      statement.setInt(1, charId);
      rset = statement.executeQuery();

      while(rset.next()) {
        long time = rset.getLong("time");
        int action = rset.getInt("action");
        int param = rset.getInt("param");
        HeroDiary d = new HeroDiary(action, time, param);
        diary.add(d);
      }

      _herodiary.put(charId, diary);
      if (Config.DEBUG) {
        log.info("Hero System: Loaded " + diary.size() + " diary entries for Hero(object id: #" + charId + ")");
      }
    } catch (SQLException var14) {
      log.warn("Hero System: Couldnt load Hero Diary for CharId: " + charId, var14);
    } finally {
      DbUtils.closeQuietly(con, statement, rset);
    }

  }

  public void showHeroDiary(Player activeChar, int heroclass, int page) {
    HeroController.HeroRecord hr = null;

    for (HeroRecord hr2 : this.getCurrentHeroes()) {
      if (hr2.active && hr2.played && hr2.class_id == heroclass) {
        hr = hr2;
      }
    }

    if (hr != null) {
      List<HeroDiary> mainlist = _herodiary.get(hr.char_id);
      if (mainlist != null) {
        NpcHtmlMessage html = new NpcHtmlMessage(activeChar, null);
        html.setFile("oly/monument_hero_info.htm");
        html.replace("%title%", StringHolder.getInstance().getNotNull(activeChar, "hero.diary"));
        html.replace("%heroname%", hr.name);
        html.replace("%message%", hr.message);
        List<HeroDiary> list = new ArrayList<>(mainlist);
        Collections.reverse(list);
        boolean color = true;
        StringBuilder fList = new StringBuilder(500);
        int counter = 0;
        int breakat = 0;

        for(int i = (page - 1) * 10; i < list.size(); ++i) {
          breakat = i;
          HeroDiary diary = list.get(i);
          Entry<String, String> entry = diary.toString(activeChar);
          fList.append("<tr><td>");
          if (color) {
            fList.append("<table width=270 bgcolor=\"131210\">");
          } else {
            fList.append("<table width=270>");
          }

          fList.append("<tr><td width=270><font color=\"LEVEL\">").append(entry.getKey()).append("</font></td></tr>");
          fList.append("<tr><td width=270>" + entry.getValue() + "</td></tr>");
          fList.append("<tr><td>&nbsp;</td></tr></table>");
          fList.append("</td></tr>");
          color = !color;
          ++counter;
          if (counter >= 10) {
            break;
          }
        }

        if (breakat < list.size() - 1) {
          html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
          html.replace("%prev_bypass%", "_diary?class=" + heroclass + "&page=" + (page + 1));
        } else {
          html.replace("%buttprev%", "");
        }

        if (page > 1) {
          html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">");
          html.replace("%next_bypass%", "_diary?class=" + heroclass + "&page=" + (page - 1));
        } else {
          html.replace("%buttnext%", "");
        }

        html.replace("%list%", fList.toString());
        activeChar.sendPacket(html);
      }

    }
  }

  public void addHeroDiary(int playerId, int id, int param) {
    this.insertHeroDiary(playerId, id, param);
    List<HeroDiary> list = _herodiary.get(playerId);
    if (list != null) {
      list.add(new HeroDiary(id, System.currentTimeMillis(), param));
    }

  }

  private void insertHeroDiary(int charId, int action, int param) {
    Connection con = null;
    PreparedStatement statement = null;

    try {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
      statement.setInt(1, charId);
      statement.setLong(2, System.currentTimeMillis());
      statement.setInt(3, action);
      statement.setInt(4, param);
      statement.execute();
      statement.close();
    } catch (SQLException var10) {
      log.error("SQL exception while saving DiaryData.", var10);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }

  }

  public void setHeroMessage(int charId, String message) {
    HeroController.HeroRecord hr = null;

    for (HeroRecord hr2 : this.getCurrentHeroes()) {
      if (hr2.active && hr2.played && hr2.char_id == charId) {
        hr = hr2;
      }
    }

    hr.message = Strings.stripSlashes(message);
  }

  public static boolean isHaveHeroWeapon(Player player) {
    int[] var1 = HERO_WEAPONS;
    int var2 = var1.length;

    for (int iid : var1) {
      if (player.getInventory().getCountOf(iid) > 0L) {
        return true;
      }
    }

    return false;
  }

  private static void removeAllHeroWeapons(Player player) {
    boolean removed = false;

    for (int i : HERO_WEAPONS) {
      int itemId;
      for (itemId = i; player.getInventory().destroyItemByItemId(itemId, 1L); removed = true) {
      }

      if (removed) {
        player.sendPacket((new SystemMessage(1726)).addItemName(itemId));
      }

      removed = false;
    }

  }

  public static void checkHeroWeaponary(Player player) {
    if (!player.isHero()) {
      removeAllHeroWeapons(player);
    }

  }

  public class HeroRecord {
    public int char_id;
    public int class_id;
    public int count;
    public boolean active;
    public boolean played;
    public String name;
    public String message;
    public String clan_name;
    public String ally_name;
    public int clan_crest;
    public int ally_crest;
    public Collection<CompetitionResults> competitions;

    private HeroRecord(int _char_id, String _name, int _class_id, int _count, boolean _active, boolean _played, String _message) {
      this.char_id = _char_id;
      this.name = _name;
      this.count = _count;
      this.class_id = _class_id;
      this.active = _active;
      this.played = _played;
      this.message = _message;
    }

    public Collection<CompetitionResults> getCompetitions() {
      if (this.competitions == null) {
        this.competitions = CompetitionController.getInstance().getCompetitionResults(this.char_id, OlyController.getInstance().getCurrentSeason() - 1);
      }

      return this.competitions;
    }
  }
}
