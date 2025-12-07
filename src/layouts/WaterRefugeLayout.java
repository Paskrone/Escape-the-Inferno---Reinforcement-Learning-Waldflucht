package layouts;

import environment.ForestConstants;

/**
 * Layout 3: Wasserrefugium
 * Wasser-Teich in der Mitte bietet Schutz vor Feuer
 */
public class WaterRefugeLayout implements Layout {
    
    @Override
    public int getWidth() {
        return 10;
    }
    
    @Override
    public int getHeight() {
        return 10;
    }
    
    @Override
    public int[][] createGrid() {
        int[][] grid = new int[getWidth()][getHeight()];
        
        // Ausgänge an den Ecken
        grid[0][0] = ForestConstants.EXIT;
        grid[9][0] = ForestConstants.EXIT;
        grid[0][9] = ForestConstants.EXIT;
        grid[9][9] = ForestConstants.EXIT;
        
        // Wasser-Teich in der Mitte
        for (int x = 3; x <= 6; x++) {
            for (int y = 3; y <= 6; y++) {
                grid[x][y] = ForestConstants.WATER;
            }
        }
        
        // Feuer umgibt den Teich teilweise
        grid[2][3] = ForestConstants.FIRE;
        grid[2][4] = ForestConstants.FIRE;
        grid[2][5] = ForestConstants.FIRE;
        grid[2][6] = ForestConstants.FIRE;
        grid[7][3] = ForestConstants.FIRE;
        grid[7][4] = ForestConstants.FIRE;
        
        return grid;
    }
    
    @Override
    public int getStartX() {
        return 5;
    }
    
    @Override
    public int getStartY() {
        return 5;
    }
    
    @Override
    public int getMaxSteps() {
        return 120;
    }
    
    @Override
    public String getName() {
        return "Water Refuge";
    }
}

