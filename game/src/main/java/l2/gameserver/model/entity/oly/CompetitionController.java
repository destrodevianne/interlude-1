//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.model.entity.oly;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.StringHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.model.entity.oly.NoblesController.NobleRecord;
import l2.gameserver.model.entity.oly.participants.SinglePlayerParticipant;
import l2.gameserver.model.entity.oly.participants.TeamParticipant;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public class CompetitionController {
  public static final int COMPETITION_PAUSE = 30000;
  public static final int COMPETITION_PREPARATION_DELAY = 60;
  public static final int BACKPORT_DELAY = 20;
  private static CompetitionController _instance;
  private ConcurrentLinkedQueue<Competition> _activeCompetitions = new ConcurrentLinkedQueue<>();
  private ScheduledFuture<?> _start_task;
  private int _start_fail_trys = 0;
  private static final String GET_COMP_RECORDS = "SELECT `oc`.`char_id` AS `char_obj_id`, `on1`.`char_name` AS `char_name`, `on1`.`class_id` AS `char_class_id`, `on2`.`char_id` AS `rival_obj_id`, `on2`.`char_name` AS `rival_name`, `on2`.`class_id` AS `rival_class_id`, `oc`.`result` AS `result`, `oc`.`rule` AS `rules`, `oc`.`elapsed_time` AS `elapsed_time`, `oc`.`mtime` AS `mtime` FROM `oly_comps` AS `oc` JOIN `oly_nobles` AS `on1` ON `oc`.`char_id` = `on1`.`char_id` JOIN `oly_nobles` AS `on2` ON `oc`.`rival_id` = `on2`.`char_id` WHERE `oc`.`char_id` = ? AND `oc`.`season` = ? ";
  private static final String ADD_COMP_RECORD = "INSERT INTO `oly_comps` (`season`, `char_id`, `rival_id`, `rule`, `result`, `elapsed_time`, `mtime`) VALUES (?, ?, ?, ?, ?, ?, ?)";

  public static CompetitionController getInstance() {
    if (_instance == null) {
      _instance = new CompetitionController();
    }

    return _instance;
  }

  private CompetitionController() {
  }

  public boolean isActiveCompetitionInPrgress() {
    return !this._activeCompetitions.isEmpty();
  }

  public Collection<Competition> getCompetitions() {
    return this._activeCompetitions;
  }

  private synchronized boolean TryCreateCompetitions(CompetitionType type, int cls_id) {
    if (!StadiumPool.getInstance().isStadiumAvailable()) {
      log.warn("OlyCompetitionController: not enough stadiums.");
      return false;
    } else if (!ParticipantPool.getInstance().isEnough(type, cls_id)) {
      ParticipantPool.getInstance().broadcastToEntrys(type, Msg.THE_MATCH_MAY_BE_DELAYED_DUE_TO_NOT_ENOUGH_COMBATANTS, cls_id);
      return false;
    } else {
      for (Player[][] participants = ParticipantPool.getInstance().retrieveEntrys(type, cls_id); OlyController.getInstance().isRegAllowed() && StadiumPool.getInstance().isStadiumAvailable() && participants != null && participants[0] != null && participants[1] != null; participants = ParticipantPool.getInstance().retrieveEntrys(type, cls_id)) {
        Stadium stadium = StadiumPool.getInstance().pollStadium();
        if (stadium == null) {
          log.error("OlyCompetitionController: stadium == null wtf?");
          return false;
        }

        this.StartCompetition(type, stadium, participants[0], participants[1]);
      }

      return true;
    }
  }

  private void StartCompetition(CompetitionType type, Stadium stadium, Player[] p0, Player[] p1) {
    Competition comp = new Competition(type, stadium);
    if (type == CompetitionType.TEAM_CLASS_FREE) {
      comp.setPlayers(new Participant[]{new TeamParticipant(Participant.SIDE_BLUE, comp, p0), new TeamParticipant(Participant.SIDE_RED, comp, p1)});
    } else if (type == CompetitionType.CLASS_FREE || type == CompetitionType.CLASS_INDIVIDUAL) {
      comp.setPlayers(new Participant[]{new SinglePlayerParticipant(Participant.SIDE_BLUE, comp, p0[0]), new SinglePlayerParticipant(Participant.SIDE_RED, comp, p1[0])});
    }

    comp.scheduleTask(new CompetitionController.StadiumTeleportTask(comp), 100L);
    comp.start();
    this._activeCompetitions.add(comp);
  }

  public void FinishCompetition(Competition comp) {
    if (comp != null) {
      try {
        comp.finish();
        if (comp.getState() != CompetitionState.INIT) {
          comp.teleportParticipantsBack();
        }

        StadiumPool.getInstance().putStadium(comp.getStadium());
        this._activeCompetitions.remove(comp);
      } catch (Exception var3) {
        var3.printStackTrace();
      }

    }
  }

  private boolean RunComps(CompetitionType type) {
    if (type == CompetitionType.CLASS_INDIVIDUAL) {
      boolean ret = false;
      ClassId[] var3 = ClassId.values();
      int var4 = var3.length;

      for (ClassId cid : var3) {
        if (cid.level() == 3) {
          this.TryCreateCompetitions(type, cid.getId());
        }
      }

      return ret;
    } else {
      return this.TryCreateCompetitions(type, 0);
    }
  }

  public void scheduleStartTask() {
    if (OlyController.getInstance().isRegAllowed()) {
      this._start_task = ThreadPoolManager.getInstance().schedule(
        new CompetitionController.CompetitionStarterTask(),
        Math.min(30000 * (this._start_fail_trys + 1), 60000)
      );
    }

  }

  public void cancelStartTask() {
    if (this._start_task != null) {
      this._start_task.cancel(true);
      this._start_task = null;
    }

  }

  public synchronized SystemMessage AddParticipationRequest(CompetitionType type, Player[] players) {
    if (!OlyController.getInstance().isRegAllowed()) {
      return Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS;
    } else {
      Player[] var3 = players;
      int var4 = players.length;

      for (int var5 = 0; var5 < var4; ++var5) {
        Player noble = var3[var5];
        if (!noble.isNoble()) {
          return (new SystemMessage(1501)).addName(noble);
        }

        if (noble.isInDuel()) {
          return new SystemMessage(1599);
        }

        if (noble.getBaseClassId() != noble.getClassId().getId() || noble.getClassId().getLevel() < 4) {
          return (new SystemMessage(1500)).addName(noble);
        }

        if (ParticipantPool.getInstance().isRegistred(noble)) {
          return (new SystemMessage(1502)).addName(noble);
        }

        if ((double) noble.getInventoryLimit() * 0.8D <= (double) noble.getInventory().getSize()) {
          return (new SystemMessage(1691)).addName(noble);
        }

        if (noble.isCursedWeaponEquipped()) {
          return (new SystemMessage(1857)).addName(noble).addItemName(noble.getCursedWeaponEquippedId());
        }

        if (NoblesController.getInstance().getPointsOf(noble.getObjectId()) < 1) {
          return (new SystemMessage(SystemMsg.S1)).addString((new CustomMessage("THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_REQUIREMENTS_ARE_NOT_MET_IN_ORDER_TO_PARTICIPATE_IN", noble)).toString());
        }

        if (Config.OLY_RESTRICT_CLASS_IDS.length > 0 && ArrayUtils.contains(Config.OLY_RESTRICT_CLASS_IDS, noble.getActiveClassId())) {
          return (new SystemMessage(SystemMsg.S1)).addString((new CustomMessage("olympiad.restrictedclasses", noble)).toString());
        }

        if (Config.OLY_RESTRICT_HWID && noble.getNetConnection().getHwid() != null && ParticipantPool.getInstance().isHWIDRegistred(noble.getNetConnection().getHwid())) {
          return (new SystemMessage(SystemMsg.S1)).addString((new CustomMessage("olympiad.iphwid.check", noble)).toString());
        }

        if (Config.OLY_RESTRICT_IP && noble.getNetConnection().getIpAddr() != null && ParticipantPool.getInstance().isIPRegistred(noble.getNetConnection().getIpAddr())) {
          return (new SystemMessage(SystemMsg.S1)).addString((new CustomMessage("olympiad.iphwid.check", noble)).toString());
        }
      }

      ParticipantPool.getInstance().createEntry(type, players);
      switch (type) {
        case CLASS_INDIVIDUAL:
          return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES;
        case CLASS_FREE:
          return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
        case TEAM_CLASS_FREE:
          return Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
        default:
          return null;
      }
    }
  }

  public void scheduleFinishCompetition(Competition comp, int count_down, long delay) {
    comp.scheduleTask(new CompetitionController.FinishCompetitionTask(comp, count_down), delay);
  }

  public void scheduleCompetitionPreparation(Competition comp) {
    comp.scheduleTask(new CompetitionController.CompetitionPreparationTask(comp), 1000L);
  }

  public void addCompetitionResult(int season, NobleRecord winner, int win_points, NobleRecord looser, int loose_points, CompetitionType type, boolean tie, boolean disconn, long elapsed_time) {
    if (winner != null && looser != null && type != null) {
      if (disconn) {
        Log.add(String.format("CompetitionResult: %s(%d) - %d disconnected against %s(%d) in %s", looser.char_name, looser.char_id, loose_points, winner.char_name, winner.char_id, type.name()), "olympiad");
      } else if (!tie) {
        Log.add(String.format("CompetitionResult: %s(%d) + %d win against %s(%d) - %d in %s", winner.char_name, winner.char_id, win_points, looser.char_name, looser.char_id, loose_points, type.name()), "olympiad");
      } else {
        Log.add(String.format("CompetitionResult: %s(%d) tie against %s(%d) in %s", winner.char_name, winner.char_id, looser.char_name, looser.char_id, type.name()), "olympiad");
      }

      Connection conn = null;
      PreparedStatement pstmt = null;

      try {
        conn = DatabaseFactory.getInstance().getConnection();
        pstmt = conn.prepareStatement("INSERT INTO `oly_comps` (`season`, `char_id`, `rival_id`, `rule`, `result`, `elapsed_time`, `mtime`) VALUES (?, ?, ?, ?, ?, ?, ?)");
        if (!disconn) {
          pstmt.setInt(1, season);
          pstmt.setInt(2, winner.char_id);
          pstmt.setInt(3, looser.char_id);
          pstmt.setInt(4, type.getTypeIdx());
          pstmt.setByte(5, (byte) (tie ? 0 : 1));
          pstmt.setInt(6, (int) elapsed_time);
          pstmt.setInt(7, (int) (System.currentTimeMillis() / 1000L));
          pstmt.executeUpdate();
        }

        pstmt.setInt(1, season);
        pstmt.setInt(2, looser.char_id);
        pstmt.setInt(3, winner.char_id);
        pstmt.setInt(4, type.getTypeIdx());
        pstmt.setByte(5, (byte) (tie ? 0 : -1));
        pstmt.setInt(6, (int) elapsed_time);
        pstmt.setInt(7, (int) (System.currentTimeMillis() / 1000L));
        pstmt.executeUpdate();
      } catch (Exception var17) {
        log.warn("Can't save competition result", var17);
      } finally {
        DbUtils.closeQuietly(conn, pstmt);
      }

    }
  }

  public Collection<CompetitionController.CompetitionResults> getCompetitionResults(int obj_id, int season) {
    ArrayList<CompetitionController.CompetitionResults> result = new ArrayList<>();
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rset = null;

    try {
      conn = DatabaseFactory.getInstance().getConnection();
      pstmt = conn.prepareStatement("SELECT `oc`.`char_id` AS `char_obj_id`, `on1`.`char_name` AS `char_name`, `on1`.`class_id` AS `char_class_id`, `on2`.`char_id` AS `rival_obj_id`, `on2`.`char_name` AS `rival_name`, `on2`.`class_id` AS `rival_class_id`, `oc`.`result` AS `result`, `oc`.`rule` AS `rules`, `oc`.`elapsed_time` AS `elapsed_time`, `oc`.`mtime` AS `mtime` FROM `oly_comps` AS `oc` JOIN `oly_nobles` AS `on1` ON `oc`.`char_id` = `on1`.`char_id` JOIN `oly_nobles` AS `on2` ON `oc`.`rival_id` = `on2`.`char_id` WHERE `oc`.`char_id` = ? AND `oc`.`season` = ? ");
      pstmt.setInt(1, obj_id);
      pstmt.setInt(2, season);
      rset = pstmt.executeQuery();

      while (rset.next()) {
        result.add(new CompetitionController
          .CompetitionResults(rset.getInt("char_obj_id"),
          rset.getInt("char_class_id"),
          rset.getString("char_name"),
          rset.getInt("rival_obj_id"),
          rset.getInt("rival_class_id"),
          rset.getString("rival_name"),
          CompetitionType.getTypeOf(rset.getByte("rules")),
          rset.getByte("result"),
          rset.getInt("elapsed_time"),
          rset.getLong("mtime")
        ));
      }
    } catch (Exception var11) {
      log.warn("Can't load competitions records", var11);
    } finally {
      DbUtils.closeQuietly(conn, pstmt, rset);
    }

    return result;
  }

  public void showCompetitionList(Player player) {
    if (!OlyController.getInstance().isRegAllowed()) {
      player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
    } else if (!player.isOlyParticipant() && !ParticipantPool.getInstance().isRegistred(player)) {
      StringBuilder sb = new StringBuilder();
      Stadium[] var3 = StadiumPool.getInstance().getAllStadiums();

      for (Stadium stadium : var3) {
        sb.append("<a action=\"bypass -h _olympiad?command=move_op_field&field=").append(stadium.getStadiumId() + 1).append("\">");
        sb.append((new CustomMessage("Olympiad.CompetitionState.ARENA", player)).toString()).append(stadium.getStadiumId() + 1);
        sb.append("&nbsp;&nbsp;&nbsp;");
        boolean isEmpty = true;

        for (Competition comp : this._activeCompetitions) {
          if (comp.getStadium() == stadium && comp.getState() != CompetitionState.INIT) {
            sb.append(comp._participants[0].getName()).append(" : ").append(comp._participants[1].getName());
            sb.append("&nbsp;");
            switch (comp.getState()) {
              case STAND_BY:
                sb.append((new CustomMessage("Olympiad.CompetitionState.STAND_BY", player)).toString());
                break;
              case PLAYING:
                sb.append((new CustomMessage("Olympiad.CompetitionState.PLAYING", player)).toString());
                break;
              case FINISH:
                sb.append((new CustomMessage("Olympiad.CompetitionState.FINISH", player)).toString());
            }

            isEmpty = false;
          }
        }

        if (isEmpty) {
          sb.append((new CustomMessage("Olympiad.CompetitionState.EMPTY", player)).toString());
        }

        sb.append("</a><br>");
      }

      NpcHtmlMessage html = new NpcHtmlMessage(player, null);
      html.setFile("oly/arenas.htm");
      html.replace("%arenas%", sb.toString());
      player.sendPacket(html);
    } else {
      player.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
    }
  }

  public void watchCompetition(Player player, int stadium_id) {
    if (!OlyController.getInstance().isRegAllowed()) {
      player.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
    } else if (player.getPet() == null && !player.isMounted()) {
      if (!player.isInStoreMode()) {
        if (stadium_id >= 1 && stadium_id <= 22) {
          if (!player.isOlyParticipant() && !ParticipantPool.getInstance().isRegistred(player)) {
            Stadium stadium = StadiumPool.getInstance().getStadium(stadium_id - 1);
            if (stadium.getObserverCount() > Config.OLY_MAX_SPECTATORS_PER_STADIUM) {
              player.sendMessage(new CustomMessage("CompetitionController.oly.ToManyObservers", player));
            } else {
              if (player.isOlyObserver()) {
                player.switchOlympiadObserverArena(stadium);
              } else {
                player.enterOlympiadObserverMode(stadium);
              }

            }
          } else {
            player.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
          }
        }
      }
    }
  }

  public static class CompetitionResults {
    int char_id;
    int rival_id;
    String char_name;
    String rival_name;
    byte result;
    int char_class_id;
    int rival_class_id;
    int elapsed_time;
    CompetitionType type;
    long mtime;

    private CompetitionResults(int _wid, int _wcid, String _wn, int _lid, int _lcid, String _ln, CompetitionType _type, byte _r, int _et, long _mtime) {
      this.char_id = _wid;
      this.char_class_id = _wcid;
      this.char_name = _wn;
      this.rival_id = _lid;
      this.rival_name = _ln;
      this.rival_class_id = _lcid;
      this.type = _type;
      this.result = _r;
      this.elapsed_time = _et;
      this.mtime = _mtime;
    }

    public String toString(Player player, MutableInt wins, MutableInt looses, MutableInt ties) {
      String main = null;
      if (this.result == 0) {
        main = StringHolder.getInstance().getNotNull(player, "hero.history.tie");
      } else if (this.result > 0) {
        main = StringHolder.getInstance().getNotNull(player, "hero.history.win");
      } else {
        main = StringHolder.getInstance().getNotNull(player, "hero.history.loss");
      }

      if (this.result > 0) {
        wins.increment();
      } else if (this.result == 0) {
        ties.increment();
      } else {
        looses.increment();
      }

      main = main.replace("%classId%", String.valueOf(this.rival_class_id));
      main = main.replace("%name%", this.rival_name);
      main = main.replace("%date%", TimeUtils.toHeroRecordFormat(this.mtime));
      main = main.replace("%time%", String.format("%02d:%02d", this.elapsed_time / 60, this.elapsed_time % 60));
      main = main.replace("%victory_count%", wins.toString());
      main = main.replace("%tie_count%", ties.toString());
      main = main.replace("%loss_count%", looses.toString());
      return main;
    }
  }

  public class FinishCompetitionTask implements Runnable {
    private Competition _game;
    private int _countdown;

    public FinishCompetitionTask(Competition game, int countdown) {
      this._game = game;
      this._countdown = countdown;
    }

    public void run() {
      if (this._game.getState() != CompetitionState.FINISH) {
        this._game.setState(CompetitionState.FINISH);
        this._game.ValidateWinner();
        this._game.scheduleTask(CompetitionController.this.new FinishCompetitionTask(this._game, 20), 100L);
      } else if (this._game.getState() == CompetitionState.FINISH) {
        if (this._countdown > 0) {
          this._game.broadcastPacket((new SystemMessage(1499)).addNumber(this._countdown), true, false);
          int dur = this._countdown > 5 ? this._countdown / 2 : 1;
          this._countdown -= dur;
          this._game.scheduleTask(CompetitionController.this.new FinishCompetitionTask(this._game, this._countdown), dur * 1000);
        } else {
          CompetitionController.getInstance().FinishCompetition(this._game);
        }
      }

    }
  }

  public class CompetitionPreparationTask implements Runnable {
    private Competition _game;
    private int _countdown;

    public CompetitionPreparationTask(Competition game) {
      this(game, 60);
    }

    public CompetitionPreparationTask(Competition game, int countdown) {
      this._game = game;
      this._countdown = countdown;
    }

    public void run() {
      if (this._countdown > 0) {
        if (this._countdown < 10 || this._countdown % 10 == 0) {
          this._game.broadcastPacket((new SystemMessage(1495)).addNumber(this._countdown), true, true);
        }

        long delay = 1000L;
        switch (this._countdown) {
          case 5:
            this._game.applyBuffs();
          case 1:
          case 2:
          case 3:
          case 4:
            --this._countdown;
            delay = 1000L;
            break;
          case 10:
            this._countdown -= 5;
            delay = 5000L;
            break;
          case 20:
          case 30:
          case 40:
          case 50:
            this._countdown -= 10;
            delay = 10000L;
            break;
          case 55:
          case 60:
            this._countdown -= 5;
            delay = 5000L;
        }

        this._game.scheduleTask(CompetitionController.this.new CompetitionPreparationTask(this._game, this._countdown), this._countdown > 0 ? delay : 2000L);
      } else {
        this._game.getStadium().setZonesActive(true);
        this._game.restoreHPCPMP();
        this._game.broadcastEverybodyOlympiadUserInfo();
        this._game.broadcastEverybodyEffectIcons();
        this._game.broadcastPacket(new PlaySound("ns17_f"), true, true);
        this._game.broadcastPacket(Msg.STARTS_THE_GAME, true, true);
        this._game.setState(CompetitionState.PLAYING);
        CompetitionController.getInstance().scheduleFinishCompetition(this._game, -1, Config.OLYMPIAD_COMPETITION_TIME);
      }

    }
  }

  public class StadiumTeleportTask implements Runnable {
    private Competition _game;
    private int _countdown;

    public StadiumTeleportTask(Competition game) {
      this(game, Config.OLYMPIAD_STADIUM_TELEPORT_DELAY);
    }

    public StadiumTeleportTask(Competition game, int countdown) {
      this._game = game;
      this._countdown = countdown;
      if (this._game.getState() == null) {
        this._game.setState(CompetitionState.INIT);
      }

    }

    public void run() {
      if (this._countdown > 0) {
        this._game.broadcastPacket((new SystemMessage(1492)).addNumber(this._countdown), true, false);
        long delay = 1000L;
        switch (this._countdown) {
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
            --this._countdown;
            delay = 1000L;
            break;
          case 15:
            this._countdown = 5;
            delay = 5000L;
            break;
          case 30:
          case 45:
            this._countdown -= 15;
            delay = 15000L;
        }

        if (this._game.ValidateParticipants()) {
          return;
        }

        this._game.scheduleTask(CompetitionController.this.new StadiumTeleportTask(this._game, this._countdown), this._countdown > 0 ? delay : 1000L);
      } else {
        if (this._game.ValidateParticipants()) {
          return;
        }

        this._game.getStadium().setZonesActive(false);
        this._game.teleportParticipantsOnStadium();
        this._game.setState(CompetitionState.STAND_BY);
        CompetitionController.getInstance().scheduleCompetitionPreparation(this._game);
        OlyController.getInstance().announceCompetition(this._game.getType(), this._game.getStadium().getStadiumId());
      }

    }
  }

  private class CompetitionStarterTask implements Runnable {
    private CompetitionStarterTask() {
    }

    public void run() {
      try {
        if (!OlyController.getInstance().isRegAllowed()) {
          return;
        }

        CompetitionType[] var1 = CompetitionType.values();
        int var2 = var1.length;

        for (CompetitionType type : var1) {
          if (CompetitionController.this.RunComps(type)) {
            CompetitionController.getInstance()._start_fail_trys = 0;
          } else if (CompetitionController.getInstance()._start_fail_trys < 5) {
            CompetitionController.getInstance()._start_fail_trys++;
          }
        }
      } catch (Exception e) {
        log.error("run: eMessage={}, eClause={} eClass={}", e.getMessage(), e.getCause(), e.getClass());
      } finally {
        CompetitionController.getInstance().scheduleStartTask();
      }

    }
  }
}
