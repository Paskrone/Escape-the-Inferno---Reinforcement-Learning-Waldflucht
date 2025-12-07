package layouts;

import environment.ForestConstants;

/**
 * Layout 5: Inferno
 * Viel Feuer, wenige sichere Wege - das schwierigste Layout
 */
public class InfernoLayout implements Layout {
    
    @Override
    public int getWidth() {
        return 15;
    }
    
    @Override
    public int getHeight() {
        return 15;
    }
    
    @Override
    public int[][] createGrid() {
        int[][] grid = new int[getWidth()][getHeight()];
        
        // Ausgänge an den Rändern (Mitte jeder Seite)
        grid[0][7] = ForestConstants.EXIT;
        grid[14][7] = ForestConstants.EXIT;
        grid[7][0] = ForestConstants.EXIT;
        grid[7][14] = ForestConstants.EXIT;
        
        // Diagonale Feuerlinien
        for (int i = 0; i < 15; i++) {
            if (i != 7) {  // Durchgang bei i=7
                grid[i][i] = ForestConstants.FIRE;
                if (14 - i >= 0 && 14 - i < 15) {
                    grid[i][14 - i] = ForestConstants.FIRE;
                }
            }
        }
        
        // Sichere Wasserzonen
        grid[3][7] = ForestConstants.WATER;
        grid[11][7] = ForestConstants.WATER;
        grid[7][3] = ForestConstants.WATER;
        grid[7][11] = ForestConstants.WATER;
        
        // Zusätzliche Hindernisse
        grid[5][5] = ForestConstants.OBSTACLE;
        grid[9][5] = ForestConstants.OBSTACLE;
        grid[5][9] = ForestConstants.OBSTACLE;
        grid[9][9] = ForestConstants.OBSTACLE;
        
        return grid;
    }
    
    @Override
    public int getStartX() {
        return 7;
    }
    
    @Override
    public int getStartY() {
        return 7;
    }
    
    @Override
    public int getMaxSteps() {
        return 300;
    }
    
    @Override
    public String getName() {
        return "Inferno";
    }
}

