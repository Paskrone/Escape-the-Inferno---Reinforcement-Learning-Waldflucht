package layouts;

import environment.ForestConstants;

/**
 * Layout 0: Tutorial
 * Kleines 6x6 Grid - garantiert lösbar mit Q-Tabelle
 * 
 * Eigenschaften:
 * - Nur 36 Zustände (6x6) → kleine Q-Tabelle
 * - Ein Ausgang (oben rechts)
 * - Wenig Feuer (nur 2 Zellen)
 * - Klarer optimaler Pfad
 * - Agent startet unten links
 */
public class TutorialLayout implements Layout {
    
    @Override
    public int getWidth() {
        return 6;
    }
    
    @Override
    public int getHeight() {
        return 6;
    }
    
    @Override
    public int[][] createGrid() {
        int[][] grid = new int[getWidth()][getHeight()];
        
        // Alles als EMPTY initialisiert (default 0)
        
        // Ein Ausgang oben rechts
        grid[5][0] = ForestConstants.EXIT;
        
        // Minimales Feuer (2 Zellen) - blockiert direkten Weg
        grid[3][2] = ForestConstants.FIRE;
        grid[3][3] = ForestConstants.FIRE;
        
        // Ein kleines Hindernis
        grid[2][4] = ForestConstants.OBSTACLE;
        
        /*
         * Layout-Visualisierung:
         * 
         *   0 1 2 3 4 5
         * 0 . . . . . E   ← EXIT
         * 1 . . . . . .
         * 2 . . . F . .   ← FIRE
         * 3 . . . F . .   ← FIRE
         * 4 . . # . . .   ← OBSTACLE
         * 5 D . . . . .   ← DEER (Start)
         * 
         * Optimaler Pfad: 
         * (0,5) → rechts → rechts → hoch → hoch → hoch → rechts → rechts → rechts → hoch → hoch
         * = ca. 10 Schritte
         */
        
        return grid;
    }
    
    @Override
    public int getStartX() {
        return 0;  // Unten links
    }
    
    @Override
    public int getStartY() {
        return 5;
    }
    
    @Override
    public int getMaxSteps() {
        return 50;  // Großzügig für ein 6x6 Grid
    }
    
    @Override
    public String getName() {
        return "Tutorial";
    }
}

