package layouts;

import environment.ForestConstants;

/**
 * Layout 1: Einfache Flucht
 * 10x10 Grid mit 4 Ausgängen an den Ecken und Feuer in der Mitte
 */
public class SimpleEscapeLayout implements Layout {
    
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
        
        // Alles als EMPTY initialisiert (default 0)
        
        // Ausgänge an den Ecken
        grid[0][0] = ForestConstants.EXIT;
        grid[9][0] = ForestConstants.EXIT;
        grid[0][9] = ForestConstants.EXIT;
        grid[9][9] = ForestConstants.EXIT;
        
        // Feuer in der Mitte
        grid[4][4] = ForestConstants.FIRE;
        grid[5][4] = ForestConstants.FIRE;
        grid[4][5] = ForestConstants.FIRE;
        grid[5][5] = ForestConstants.FIRE;
        
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
        return 100;
    }
    
    @Override
    public String getName() {
        return "Simple Escape";
    }
}

