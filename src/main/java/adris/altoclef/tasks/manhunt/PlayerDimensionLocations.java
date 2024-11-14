package adris.altoclef.tasks.manhunt;

import adris.altoclef.util.Dimension;
import java.util.HashMap;
import java.util.Map;

public class PlayerDimensionLocations {
    private String playerName;
    private Map<Dimension, double[]> dimensionCoordinates;

    public PlayerDimensionLocations playerDimensionLocations(String playerName) {
        this.playerName = playerName;
        this.dimensionCoordinates = new HashMap<>();
        return this;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void updateLocation(Dimension dimension, double x, double y, double z) {
        dimensionCoordinates.put(dimension, new double[]{x, y, z});
    }

    public double[] getLastLocation(Dimension dimension) {
        return dimensionCoordinates.getOrDefault(dimension, new double[]{0, 0, 0});
    }
}
