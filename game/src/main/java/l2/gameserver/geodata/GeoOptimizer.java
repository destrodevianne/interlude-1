//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package l2.gameserver.geodata;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.model.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoOptimizer {
  private static final Logger log = LoggerFactory.getLogger(GeoOptimizer.class);
  public static int[][][] checkSums;
  private static final byte version = 1;

  public GeoOptimizer() {
  }

  public static GeoOptimizer.BlockLink[] loadBlockMatches(String fileName) {
    File f = new File(Config.DATAPACK_ROOT, fileName);
    if (!f.exists()) {
      return null;
    } else {
      try {
        FileChannel roChannel = (new RandomAccessFile(f, "r")).getChannel();
        int count = (int)((roChannel.size() - 1L) / 6L);
        ByteBuffer buffer = roChannel.map(MapMode.READ_ONLY, 0L, roChannel.size());
        roChannel.close();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.get() != 1) {
          return null;
        } else {
          GeoOptimizer.BlockLink[] links = new GeoOptimizer.BlockLink[count];

          for(int i = 0; i < links.length; ++i) {
            links[i] = new GeoOptimizer.BlockLink(buffer.getShort(), buffer.get(), buffer.get(), buffer.getShort());
          }

          return links;
        }
      } catch (Exception var7) {
        log.error("", var7);
        return null;
      }
    }
  }

  public static class BlockLink {
    public final int blockIndex;
    public final int linkBlockIndex;
    public final byte linkMapX;
    public final byte linkMapY;

    public BlockLink(short _blockIndex, byte _linkMapX, byte _linkMapY, short _linkBlockIndex) {
      this.blockIndex = _blockIndex & '\uffff';
      this.linkMapX = _linkMapX;
      this.linkMapY = _linkMapY;
      this.linkBlockIndex = _linkBlockIndex & '\uffff';
    }

    public BlockLink(int _blockIndex, byte _linkMapX, byte _linkMapY, int _linkBlockIndex) {
      this.blockIndex = _blockIndex & '\uffff';
      this.linkMapX = _linkMapX;
      this.linkMapY = _linkMapY;
      this.linkBlockIndex = _linkBlockIndex & '\uffff';
    }
  }

  public static class CheckSumLoader extends RunnableImpl {
    private final int geoX;
    private final int geoY;
    private final int rx;
    private final int ry;
    private final byte[][][] region;
    private final String fileName;

    public CheckSumLoader(int _geoX, int _geoY, byte[][][] _region) {
      this.geoX = _geoX;
      this.geoY = _geoY;
      this.rx = this.geoX + Config.GEO_X_FIRST;
      this.ry = _geoY + Config.GEO_Y_FIRST;
      this.region = _region;
      this.fileName = "geodata/checksum/" + this.rx + "_" + this.ry + ".crc";
    }

    private boolean loadFromFile() {
      File GeoCrc = new File(Config.DATAPACK_ROOT, this.fileName);
      if (!GeoCrc.exists()) {
        return false;
      } else {
        try {
          FileChannel roChannel = (new RandomAccessFile(GeoCrc, "r")).getChannel();
          if (roChannel.size() != 262144L) {
            roChannel.close();
            return false;
          } else {
            ByteBuffer buffer = roChannel.map(MapMode.READ_ONLY, 0L, roChannel.size());
            roChannel.close();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int[] _checkSums = new int[65536];

            for(int i = 0; i < 65536; ++i) {
              _checkSums[i] = buffer.getInt();
            }

            GeoOptimizer.checkSums[this.geoX][this.geoY] = _checkSums;
            return true;
          }
        } catch (Exception var6) {
          GeoOptimizer.log.error("", var6);
          return false;
        }
      }
    }

    private void saveToFile() {
      GeoOptimizer.log.info("Saving checksums to: " + this.fileName);

      try {
        File f = new File(Config.DATAPACK_ROOT, this.fileName);
        if (f.exists()) {
          f.delete();
        }

        FileChannel wChannel = (new RandomAccessFile(f, "rw")).getChannel();
        ByteBuffer buffer = wChannel.map(MapMode.READ_WRITE, 0L, 262144L);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int[] _checkSums = GeoOptimizer.checkSums[this.geoX][this.geoY];

        for(int i = 0; i < 65536; ++i) {
          buffer.putInt(_checkSums[i]);
        }

        wChannel.close();
      } catch (Exception var6) {
        GeoOptimizer.log.error("", var6);
      }

    }

    private void gen() {
      GeoOptimizer.log.info("Generating checksums for " + this.rx + "_" + this.ry);
      int[] _checkSums = new int[65536];
      CRC32 crc32 = new CRC32();

      for(int i = 0; i < 65536; ++i) {
        crc32.update(this.region[i][0]);
        _checkSums[i] = (int)(~crc32.getValue());
        crc32.reset();
      }

      GeoOptimizer.checkSums[this.geoX][this.geoY] = _checkSums;
    }

    public void runImpl() throws Exception {
      if (!this.loadFromFile()) {
        this.gen();
        this.saveToFile();
      }

    }
  }

  public static class GeoBlocksMatchFinder extends RunnableImpl {
    private final int geoX;
    private final int geoY;
    private final int rx;
    private final int ry;
    private final int maxScanRegions;
    private final String fileName;

    public GeoBlocksMatchFinder(int _geoX, int _geoY, int _maxScanRegions) {
      this.geoX = _geoX;
      this.geoY = _geoY;
      this.rx = this.geoX + Config.GEO_X_FIRST;
      this.ry = this.geoY + Config.GEO_Y_FIRST;
      this.maxScanRegions = _maxScanRegions;
      this.fileName = "geodata/matches/" + this.rx + "_" + this.ry + ".matches";
    }

    private boolean exists() {
      return (new File(Config.DATAPACK_ROOT, this.fileName)).exists();
    }

    private void saveToFile(GeoOptimizer.BlockLink[] links) {
      GeoOptimizer.log.info("Saving matches to: " + this.fileName);

      try {
        File f = new File(Config.DATAPACK_ROOT, this.fileName);
        if (f.exists()) {
          f.delete();
        }

        FileChannel wChannel = (new RandomAccessFile(f, "rw")).getChannel();
        ByteBuffer buffer = wChannel.map(MapMode.READ_WRITE, 0L, (long)(links.length * 6 + 1));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte)1);

        for(int i = 0; i < links.length; ++i) {
          buffer.putShort((short)links[i].blockIndex);
          buffer.put(links[i].linkMapX);
          buffer.put(links[i].linkMapY);
          buffer.putShort((short)links[i].linkBlockIndex);
        }

        wChannel.close();
      } catch (Exception var6) {
        GeoOptimizer.log.error("", var6);
      }

    }

    private void calcMatches(int[] curr_checkSums, int mapX, int mapY, List<GeoOptimizer.BlockLink> putlinks, boolean[] notready) {
      int[] next_checkSums = GeoOptimizer.checkSums[mapX][mapY];
      if (next_checkSums != null) {
        for(int blockIdx = 0; blockIdx < 65536; ++blockIdx) {
          if (notready[blockIdx]) {
            int startIdx2 = next_checkSums == curr_checkSums ? blockIdx + 1 : 0;

            for(int blockIdx2 = startIdx2; blockIdx2 < 65536; ++blockIdx2) {
              if (curr_checkSums[blockIdx] == next_checkSums[blockIdx2] && GeoEngine.compareGeoBlocks(this.geoX, this.geoY, blockIdx, mapX, mapY, blockIdx2)) {
                putlinks.add(new GeoOptimizer.BlockLink(blockIdx, (byte)mapX, (byte)mapY, blockIdx2));
                notready[blockIdx] = false;
                break;
              }
            }
          }
        }

      }
    }

    private GeoOptimizer.BlockLink[] gen() {
      GeoOptimizer.log.info("Searching matches for " + this.rx + "_" + this.ry);
      long started = System.currentTimeMillis();
      boolean[] notready = new boolean[65536];

      for(int i = 0; i < 65536; ++i) {
        notready[i] = true;
      }

      List<GeoOptimizer.BlockLink> links = new ArrayList<>();
      int[] _checkSums = GeoOptimizer.checkSums[this.geoX][this.geoY];
      int n = 0;

      for(int mapX = this.geoX; mapX < World.WORLD_SIZE_X; ++mapX) {
        int startgeoY = mapX == this.geoX ? this.geoY : 0;

        for(int mapY = startgeoY; mapY < World.WORLD_SIZE_Y; ++mapY) {
          this.calcMatches(_checkSums, mapX, mapY, links, notready);
          ++n;
          if (this.maxScanRegions > 0 && this.maxScanRegions == n) {
            return (GeoOptimizer.BlockLink[])links.toArray(new GeoOptimizer.BlockLink[links.size()]);
          }
        }
      }

      started = System.currentTimeMillis() - started;
      GeoOptimizer.log.info("Founded " + links.size() + " matches for " + this.rx + "_" + this.ry + " in " + (float)started / 1000.0F + "s");
      return (GeoOptimizer.BlockLink[])links.toArray(new GeoOptimizer.BlockLink[links.size()]);
    }

    public void runImpl() throws Exception {
      if (!this.exists()) {
        GeoOptimizer.BlockLink[] links = this.gen();
        this.saveToFile(links);
      }

    }
  }
}
