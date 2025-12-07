package layouts;

import environment.ForestConstants;

/**
 * Layout 2: Der enge Pass
 * Reh muss durch einen engen Durchgang zwischen Hindernissen
 */
public class NarrowPassLayout implements Layout {
    
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
        
        // Vertikale Hindernismauer mit Durchgang
        for (int y = 0; y < 10; y++) {
            if (y != 4 && y != 5) {  // Durchgang bei y=4,5
                grid[5][y] = ForestConstants.OBSTACLE;
            }
        }
        
        // Feuer auf einer Seite
        grid[7][3] = ForestConstants.FIRE;
        grid[7][4] = ForestConstants.FIRE;
        grid[7][5] = ForestConstants.FIRE;
        grid[7][6] = ForestConstants.FIRE;
        
        return grid;
    }
    
    @Override
    public int getStartX() {
        return 2;
    }
    
    @Override
    public int getStartY() {
        return 5;
    }
    
    @Override
    public int getMaxSteps() {
        return 150;
    }
    
    @Override
    public String getName() {
        return "Narrow Pass";
    }
}

