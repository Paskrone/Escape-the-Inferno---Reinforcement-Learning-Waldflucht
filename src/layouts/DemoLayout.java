package layouts;

import environment.ForestConstants;

/**
 * Layout 6: Demo-Layout - Das ultimative Demonstrationsbeispiel
 * 
 * Features:
 * - Großes 20x20 Grid
 * - Mehrere Feuerquellen verteilt über die Map
 * - Hindernisse als Mauern und Barrieren
 * - Flüsse (Wasser) als sichere Zonen, die Feuer blockieren
 * - Mehrere Exits an strategischen Positionen
 * - Das Reh startet in der Mitte des Chaos
 */
public class DemoLayout implements Layout {
    
    @Override
    public int getWidth() {
        return 20;
    }
    
    @Override
    public int getHeight() {
        return 20;
    }
    
    @Override
    public int[][] createGrid() {
        int[][] grid = new int[getWidth()][getHeight()];
        
        // =====================================================
        //                    EXITS (Grün)
        // =====================================================
        // Ecken
        grid[0][0] = ForestConstants.EXIT;
        grid[19][0] = ForestConstants.EXIT;
        grid[0][19] = ForestConstants.EXIT;
        grid[19][19] = ForestConstants.EXIT;
        // Zusätzliche Exits an den Kanten
        grid[10][0] = ForestConstants.EXIT;   // Oben Mitte
        grid[0][10] = ForestConstants.EXIT;   // Links Mitte
        
        // =====================================================
        //              FLÜSSE / WASSER (Blau)
        //        Sichere Zonen - blockieren Feuerausbreitung
        // =====================================================
        // Horizontaler Fluss oben
        for (int x = 3; x <= 8; x++) {
            grid[x][4] = ForestConstants.WATER;
        }
        
        // Vertikaler Fluss links
        for (int y = 6; y <= 12; y++) {
            grid[4][y] = ForestConstants.WATER;
        }
        
        // Diagonaler Fluss unten rechts
        for (int i = 0; i < 6; i++) {
            grid[14 + i][14 + i] = ForestConstants.WATER;
            if (14 + i + 1 < 20) {
                grid[14 + i][15 + i] = ForestConstants.WATER;
            }
        }
        
        // Teich in der Mitte-rechts
        for (int x = 14; x <= 16; x++) {
            for (int y = 8; y <= 10; y++) {
                grid[x][y] = ForestConstants.WATER;
            }
        }
        
        // =====================================================
        //              HINDERNISSE (Grau)
        //              Mauern und Barrieren
        // =====================================================
        // Vertikale Mauer links
        for (int y = 2; y <= 8; y++) {
            grid[7][y] = ForestConstants.OBSTACLE;
        }
        
        // Horizontale Mauer oben
        for (int x = 10; x <= 16; x++) {
            grid[x][3] = ForestConstants.OBSTACLE;
        }
        
        // L-förmiges Hindernis unten links
        for (int x = 2; x <= 6; x++) {
            grid[x][15] = ForestConstants.OBSTACLE;
        }
        for (int y = 15; y <= 18; y++) {
            grid[6][y] = ForestConstants.OBSTACLE;
        }
        
        // Kleine Hindernisgruppe rechts
        grid[17][6] = ForestConstants.OBSTACLE;
        grid[18][6] = ForestConstants.OBSTACLE;
        grid[17][7] = ForestConstants.OBSTACLE;
        grid[18][7] = ForestConstants.OBSTACLE;
        
        // Zentrale Hindernisse
        grid[10][10] = ForestConstants.OBSTACLE;
        grid[11][10] = ForestConstants.OBSTACLE;
        grid[10][11] = ForestConstants.OBSTACLE;
        
        // =====================================================
        //              FEUERQUELLEN (Rot)
        //          Verteilt über die gesamte Map
        // =====================================================
        // Feuercluster oben links
        grid[2][2] = ForestConstants.FIRE;
        grid[3][2] = ForestConstants.FIRE;
        grid[2][3] = ForestConstants.FIRE;
        
        // Feuercluster oben rechts
        grid[16][1] = ForestConstants.FIRE;
        grid[17][1] = ForestConstants.FIRE;
        grid[17][2] = ForestConstants.FIRE;
        
        // Feuercluster Mitte
        grid[9][8] = ForestConstants.FIRE;
        grid[9][9] = ForestConstants.FIRE;
        grid[8][9] = ForestConstants.FIRE;
        
        // Feuercluster unten
        grid[12][16] = ForestConstants.FIRE;
        grid[13][16] = ForestConstants.FIRE;
        grid[12][17] = ForestConstants.FIRE;
        grid[13][17] = ForestConstants.FIRE;
        
        // Einzelne Feuer als Gefahrenpunkte
        grid[5][7] = ForestConstants.FIRE;
        grid[15][5] = ForestConstants.FIRE;
        grid[3][12] = ForestConstants.FIRE;
        grid[18][12] = ForestConstants.FIRE;
        grid[8][18] = ForestConstants.FIRE;
        
        return grid;
    }
    
    @Override
    public int getStartX() {
        return 10;  // Mitte
    }
    
    @Override
    public int getStartY() {
        return 12;  // Etwas unterhalb der Mitte
    }
    
    @Override
    public int getMaxSteps() {
        return 300;  // Mehr Schritte für großes Grid
    }
    
    @Override
    public String getName() {
        return "Demo - Escape the Inferno";
    }
}

