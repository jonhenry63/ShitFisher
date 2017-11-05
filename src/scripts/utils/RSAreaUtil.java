package scripts.utils;

import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;

public class RSAreaUtil {

    public static RSArea getAreaBoundary(RSTile centerTile, int radius) {
        RSTile[] borderTiles = new RSTile[]{
                new RSTile(centerTile.getX() + radius, centerTile.getY() + radius),
                new RSTile(centerTile.getX() + radius, centerTile.getY() - radius),
                new RSTile(centerTile.getX() - radius, centerTile.getY() + radius),
                new RSTile(centerTile.getX() - radius, centerTile.getY() - radius)
        };
        return new RSArea(borderTiles);
    }
}
