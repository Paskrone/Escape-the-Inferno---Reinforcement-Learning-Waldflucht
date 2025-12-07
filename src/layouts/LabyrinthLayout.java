package layouts;

import environment.ForestConstants;

/**
 * Layout 4: Labyrinth
 * Komplexere Struktur mit vielen Hindernissen und nur 2 Ausgängen
 */
public class LabyrinthLayout implements Layout {
    
    @Override
    public int getWidth() {
        return 12;
    }
    
    @Override
    public int getHeight() {
        return 12;
    }
    
    @Override
    public int[][] createGrid() {
        int[][] grid = new int[getWidth()][getHeight()];
        
        // Ausgänge nur an zwei Stellen (links und rechts Mitte)
        grid[0][6] = ForestConstants.EXIT;
        grid[11][6] = ForestConstants.EXIT;
        
        // Labyrinth-Wände - Horizontale Wände
        for (int x = 2; x < 6; x++) grid[x][2] = ForestConstants.OBSTACLE;
        for (int x = 6; x < 10; x++) grid[x][4] = ForestConstants.OBSTACLE;
        for (int x = 2; x < 6; x++) grid[x][6] = ForestConstants.OBSTACLE;
        for (int x = 6; x < 10; x++) grid[x][8] = ForestConstants.OBSTACLE;
        
        // Labyrinth-Wände - Vertikale Wände
        for (int y = 2; y < 5; y++) grid[8][y] = ForestConstants.OBSTACLE;
        for (int y = 6; y < 9; y++) grid[4][y] = ForestConstants.OBSTACLE;
        
        // Feuer im Zentrum
        grid[5][5] = ForestConstants.FIRE;
        grid[6][5] = ForestConstants.FIRE;
        grid[7][5] = ForestConstants.FIRE;
        
        return grid;
    }
    
    @Override
    public int getStartX() {
        return 6;
    }
    
    @Override
    public int getStartY() {
        return 6;
    }
    
    @Override
    public int getMaxSteps() {
        return 200;
    }
    
    @Override
    public String getName() {
        return "Labyrinth";
    }
}

