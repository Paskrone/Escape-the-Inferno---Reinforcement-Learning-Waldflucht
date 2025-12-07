package environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Verwaltet die dynamische Feuerausbreitung im ForestEnvironment
 */
public class FireSpreadManager {
    
    private final Random random;
    private boolean enabled;
    private double spreadProbability;
    private int spreadInterval;  // Alle X Schritte breitet sich Feuer aus
    
    public FireSpreadManager() {
        this.random = new Random();
        this.enabled = false;
        this.spreadProbability = 0.3;
        this.spreadInterval = 3;
    }
    
    /**
     * Breitet das Feuer auf benachbarte Zellen aus
     * 
     * @param grid Das aktuelle Grid
     * @param width Breite des Grids
     * @param height Höhe des Grids
     */
    public void spreadFire(int[][] grid, int width, int height) {
        if (!enabled) return;
        
        List<int[]> newFireCells = new ArrayList<>();
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == ForestConstants.FIRE) {
                    // Prüfe alle Nachbarn
                    int[][] neighbors = {
                        {x, y - 1}, // oben
                        {x, y + 1}, // unten
                        {x - 1, y}, // links
                        {x + 1, y}  // rechts
                    };
                    
                    for (int[] neighbor : neighbors) {
                        int nx = neighbor[0];
                        int ny = neighbor[1];
                        
                        if (isValidPosition(nx, ny, width, height) && isFlammable(grid, nx, ny, width, height)) {
                            if (random.nextDouble() < spreadProbability) {
                                newFireCells.add(new int[]{nx, ny});
                            }
                        }
                    }
                }
            }
        }
        
        // Neue Feuerzellen setzen
        for (int[] cell : newFireCells) {
            grid[cell[0]][cell[1]] = ForestConstants.FIRE;
        }
    }
    
    /**
     * Prüft ob in diesem Schritt Feuer ausgebreitet werden soll
     */
    public boolean shouldSpread(int currentStep) {
        return enabled && currentStep % spreadInterval == 0;
    }
    
    private boolean isValidPosition(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    private boolean isFlammable(int[][] grid, int x, int y, int width, int height) {
        if (!isValidPosition(x, y, width, height)) return false;
        // Nur leere Zellen können brennen, nicht Wasser, Hindernisse oder Ausgänge
        return grid[x][y] == ForestConstants.EMPTY;
    }
    
    // =====================================================
    //               GETTER & SETTER
    // =====================================================
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public double getSpreadProbability() {
        return spreadProbability;
    }
    
    public void setSpreadProbability(double probability) {
        this.spreadProbability = probability;
    }
    
    public int getSpreadInterval() {
        return spreadInterval;
    }
    
    public void setSpreadInterval(int interval) {
        this.spreadInterval = interval;
    }
}

