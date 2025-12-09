package visualization;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Erweiterte Heatmap-Visualisierung für Neural Network Q-Learning
 * 
 * Features gegenüber Basis-Visualizer:
 * - Dynamische Q-Wert-Skala (passt sich an aktuelle Werte an)
 * - Visualisierung aller Zelltypen (Feuer, Hindernisse, Wasser)
 * - Policy-Pfeile NUR auf dem optimalen Pfad zum Exit
 * - Aktuelle Reh-Position
 */
public class NNHeatmapVisualizer extends HeatmapVisualizer {

    // Grid-Informationen
    private int[][] gridTypes;           // Zelltypen aus ForestEnvironment
    private int[][] bestActions;         // Beste Aktion pro Zelle
    private int deerX = -1, deerY = -1;  // Aktuelle Reh-Position
    private int startX = -1, startY = -1; // Startposition für Pfadberechnung
    private Set<Long> optimalPath;       // Positionen auf dem optimalen Pfad
    
    // Zelltyp-Konstanten (aus ForestConstants)
    private static final int EMPTY = 0;
    private static final int FIRE = 1;
    private static final int EXIT = 2;
    private static final int OBSTACLE = 3;
    private static final int WATER = 4;
    
    // Policy-Pfeile für Aktionen - MUSS mit ForestConstants.ACTION_* übereinstimmen!
    // ACTION_UP=0, ACTION_DOWN=1, ACTION_LEFT=2, ACTION_RIGHT=3
    private static final String[] ARROWS = {"↑", "↓", "←", "→"};
    
    // Farben für Zelltypen
    private static final Color FIRE_COLOR = new Color(255, 80, 80);      // Rot
    private static final Color OBSTACLE_COLOR = new Color(100, 100, 100); // Grau
    private static final Color WATER_COLOR = new Color(100, 180, 255);   // Hellblau
    private static final Color EXIT_COLOR = new Color(80, 220, 80);      // Grün
    private static final Color DEER_COLOR = new Color(255, 200, 50);     // Gold

    /**
     * Konstruktor mit Exit-Liste
     */
    public NNHeatmapVisualizer(int w, int h, List<int[]> exits) {
        super(w, h);
        this.exitPositions.addAll(exits);
        this.gridTypes = new int[w][h];
        this.bestActions = new int[w][h];
        this.optimalPath = new HashSet<>();
        
        // Frame-Titel anpassen
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.setTitle("Neural Network Q-Learning Heatmap");
                frame.setSize(600, 650);  // Größer für mehr Details
            }
        });
    }

    /**
     * Konstruktor mit Grid-Typen
     */
    public NNHeatmapVisualizer(int w, int h, List<int[]> exits, int[][] gridTypes) {
        this(w, h, exits);
        setGridTypes(gridTypes);
    }

    /**
     * Setzt die Grid-Typen für erweiterte Visualisierung
     */
    public void setGridTypes(int[][] types) {
        if (types != null) {
            for (int x = 0; x < Math.min(width, types.length); x++) {
                for (int y = 0; y < Math.min(height, types[x].length); y++) {
                    this.gridTypes[x][y] = types[x][y];
                }
            }
        }
    }

    /**
     * Setzt die aktuelle Reh-Position
     */
    public void setDeerPosition(int x, int y) {
        this.deerX = x;
        this.deerY = y;
    }

    /**
     * Setzt die Startposition für die Pfadberechnung
     */
    public void setStartPosition(int x, int y) {
        this.startX = x;
        this.startY = y;
    }

    /**
     * Aktualisiert Q-Werte mit dynamischer Skala und berechnet beste Aktionen
     */
    @Override
    public void update(double[][][] Q) {
        double actualMin = Double.MAX_VALUE;
        double actualMax = Double.MIN_VALUE;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double best = Q[x][y][0];
                int bestAction = 0;
                
                for (int a = 1; a < Q[x][y].length; a++) {
                    if (Q[x][y][a] > best) {
                        best = Q[x][y][a];
                        bestAction = a;
                    }
                }
                
                maxQValues[x][y] = best;
                bestActions[x][y] = bestAction;
                
                // Nur nicht-terminale Zellen für Skala berücksichtigen
                if (!isExit(x, y) && gridTypes[x][y] != FIRE) {
                    actualMin = Math.min(actualMin, best);
                    actualMax = Math.max(actualMax, best);
                }
            }
        }
        
        // Dynamische Skala mit etwas Padding
        if (actualMin != Double.MAX_VALUE && actualMax != Double.MIN_VALUE) {
            double range = actualMax - actualMin;
            this.minQ = actualMin - range * 0.1;
            this.maxQ = actualMax + range * 0.1;
            
            // Mindestbereich verhindern
            if (maxQ - minQ < 1.0) {
                double mid = (maxQ + minQ) / 2;
                minQ = mid - 0.5;
                maxQ = mid + 0.5;
            }
        }
        
        // Optimalen Pfad berechnen
        computeOptimalPath();
        
        SwingUtilities.invokeLater(this::repaint);
    }

    /**
     * Berechnet den optimalen Pfad vom Start zu einem Exit
     * Folgt der greedy Policy (beste Aktion pro Zelle)
     */
    private void computeOptimalPath() {
        optimalPath.clear();
        
        // Startposition setzen (falls nicht gesetzt, Mitte verwenden)
        int x = (startX >= 0) ? startX : width / 2;
        int y = (startY >= 0) ? startY : height / 2;
        
        // Bewegungs-Deltas für Aktionen: UP, DOWN, LEFT, RIGHT
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};
        
        // Pfad verfolgen (max Schritte = width * height um Endlosschleifen zu vermeiden)
        int maxSteps = width * height;
        for (int step = 0; step < maxSteps; step++) {
            // Position zum Pfad hinzufügen
            optimalPath.add(positionKey(x, y));
            
            // Prüfen ob Exit erreicht
            if (isExit(x, y) || gridTypes[x][y] == EXIT) {
                break;
            }
            
            // Beste Aktion für aktuelle Position
            int action = bestActions[x][y];
            
            // Neue Position berechnen
            int newX = x + dx[action];
            int newY = y + dy[action];
            
            // Grenzen prüfen
            if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
                break;  // Außerhalb des Grids
            }
            
            // Hindernis oder Feuer prüfen
            if (gridTypes[newX][newY] == OBSTACLE || gridTypes[newX][newY] == FIRE) {
                break;  // Blockiert
            }
            
            // Zyklus-Erkennung (bereits besucht)
            if (optimalPath.contains(positionKey(newX, newY))) {
                break;  // Zyklus erkannt
            }
            
            x = newX;
            y = newY;
        }
    }

    /**
     * Erzeugt einen eindeutigen Schlüssel für eine Position
     */
    private long positionKey(int x, int y) {
        return (long) x * 10000 + y;
    }

    /**
     * Prüft ob eine Position auf dem optimalen Pfad liegt
     */
    private boolean isOnOptimalPath(int x, int y) {
        return optimalPath.contains(positionKey(x, y));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Hintergrund
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        int cellWidth = getWidth() / width;
        int cellHeight = (getHeight() - 50) / height;  // Platz für Legende

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int px = x * cellWidth;
                int py = y * cellHeight;
                
                // Hintergrundfarbe basierend auf Zelltyp oder Q-Wert
                Color cellColor = getCellColor(x, y);
                g.setColor(cellColor);
                g.fillRect(px, py, cellWidth, cellHeight);

                // Rahmen
                g.setColor(Color.BLACK);
                g.drawRect(px, py, cellWidth, cellHeight);

                // Reh-Position markieren
                if (x == deerX && y == deerY) {
                    g.setColor(DEER_COLOR);
                    int margin = 3;
                    g.fillOval(px + margin, py + margin, cellWidth - 2*margin, cellHeight - 2*margin);
                    g.setColor(Color.BLACK);
                    g.drawOval(px + margin, py + margin, cellWidth - 2*margin, cellHeight - 2*margin);
                }

                // Text/Symbol
                drawCellContent(g, x, y, px, py, cellWidth, cellHeight);
            }
        }
        
        // Legende zeichnen
        drawLegend(g, cellHeight * height + 5);
    }

    // Farbe für optimalen Pfad
    private static final Color PATH_COLOR = new Color(50, 50, 50);  // Dunkel für Pfad
    
    /**
     * Bestimmt die Hintergrundfarbe einer Zelle
     */
    private Color getCellColor(int x, int y) {
        int type = gridTypes[x][y];
        
        switch (type) {
            case FIRE:
                return FIRE_COLOR;
            case EXIT:
                return EXIT_COLOR;
            case OBSTACLE:
                return OBSTACLE_COLOR;
            case WATER:
                return WATER_COLOR;
            default:
                // Optimaler Pfad hervorheben
                if (isOnOptimalPath(x, y)) {
                    return PATH_COLOR;
                }
                // Q-Wert-basierte Farbe für andere Zellen
                float ratio = (float)((maxQValues[x][y] - minQ) / (maxQ - minQ));
                ratio = Math.max(0f, Math.min(1f, ratio));
                return getColor(ratio);
        }
    }

    /**
     * Zeichnet den Inhalt einer Zelle (Text/Pfeil)
     * Pfeile werden NUR auf dem optimalen Pfad angezeigt!
     */
    private void drawCellContent(Graphics g, int x, int y, int px, int py, int cellWidth, int cellHeight) {
        int type = gridTypes[x][y];
        String text;
        Color textColor = Color.BLACK;
        
        switch (type) {
            case FIRE:
                text = "🔥";
                break;
            case EXIT:
                text = "E";
                break;
            case OBSTACLE:
                text = "#";
                break;
            case WATER:
                text = "~";
                break;
            default:
                // Pfeil NUR auf optimalem Pfad anzeigen
                if (isOnOptimalPath(x, y)) {
                    int action = bestActions[x][y];
                    if (action >= 0 && action < ARROWS.length) {
                        text = ARROWS[action];
                        textColor = Color.WHITE;  // Weiße Pfeile für bessere Sichtbarkeit
                    } else {
                        text = "";
                    }
                } else {
                    // Andere Zellen: Q-Wert anzeigen (oder leer lassen)
                    text = String.format("%.0f", maxQValues[x][y]);
                    textColor = new Color(50, 50, 50);  // Dunkelgrau für Q-Werte
                }
        }
        
        // Text zentrieren
        g.setColor(textColor);
        FontMetrics fm = g.getFontMetrics();
        int textX = px + (cellWidth - fm.stringWidth(text)) / 2;
        int textY = py + (cellHeight + fm.getAscent()) / 2 - 2;
        g.drawString(text, textX, textY);
    }

    /**
     * Zeichnet die Legende am unteren Rand
     */
    private void drawLegend(Graphics g, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        int x = 10;
        
        // Q-Skala
        g.drawString(String.format("Q-Skala: %.1f bis %.1f", minQ, maxQ), x, y + 15);
        
        // Farbbalken
        int barWidth = 100;
        int barHeight = 12;
        int barX = x + 150;
        for (int i = 0; i < barWidth; i++) {
            float ratio = i / (float) barWidth;
            g.setColor(getColor(ratio));
            g.fillRect(barX + i, y + 5, 1, barHeight);
        }
        g.setColor(Color.WHITE);
        g.drawRect(barX, y + 5, barWidth, barHeight);
        g.drawString("niedrig", barX - 40, y + 15);
        g.drawString("hoch", barX + barWidth + 5, y + 15);
        
        // Symbole
        int symbolX = barX + barWidth + 60;
        g.drawString("↑↓←→ = Optimaler Pfad", symbolX, y + 15);
        
        // Zelltypen
        g.drawString("E=Exit  #=Hindernis  ~=Wasser  🔥=Feuer  ■=Pfad", x, y + 35);
    }
}

