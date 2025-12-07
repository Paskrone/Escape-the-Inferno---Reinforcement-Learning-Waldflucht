package layouts;

/**
 * Interface für Map-Layouts im ForestEnvironment
 * Jedes Layout definiert Größe, Grid-Konfiguration und Startbedingungen
 */
public interface Layout {
    
    /**
     * @return Breite des Grids
     */
    int getWidth();
    
    /**
     * @return Höhe des Grids
     */
    int getHeight();
    
    /**
     * Erstellt und initialisiert das Grid mit Zelltypen
     * @return 2D-Array mit Zelltypen
     */
    int[][] createGrid();
    
    /**
     * @return Start-X-Position des Agenten
     */
    int getStartX();
    
    /**
     * @return Start-Y-Position des Agenten
     */
    int getStartY();
    
    /**
     * @return Maximale Anzahl Schritte pro Episode
     */
    int getMaxSteps();
    
    /**
     * @return Name des Layouts für Anzeige
     */
    String getName();
}

