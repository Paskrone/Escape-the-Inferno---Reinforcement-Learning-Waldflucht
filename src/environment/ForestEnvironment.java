package environment;

import layouts.*;
import java.util.Random;

/**
 * ForestEnvironment - Die Spielwelt für das "Escape the Inferno" Reinforcement Learning Projekt
 * 
 * Ein Reh muss aus einem brennenden Wald entkommen. Die Umgebung unterstützt:
 * - Verschiedene Zelltypen (Wald, Feuer, Ausgang, Hindernis, Wasser)
 * - Dynamische Feuerausbreitung
 * - Mehrere vordefinierte Map-Layouts
 * - Konfigurierbares Reward-System
 */
public class ForestEnvironment {

    // =====================================================
    //                  SPIELFELD
    // =====================================================
    private int width;
    private int height;
    private int[][] grid;
    private int[][] initialGrid;  // Für Reset
    private Layout layout;

    // =====================================================
    //                  AGENT (REH)
    // =====================================================
    private int deerX;
    private int deerY;
    private int startX;
    private int startY;

    // =====================================================
    //               SPIELZUSTAND
    // =====================================================
    private boolean terminated;
    private boolean escaped;
    private int currentStep;
    private int maxSteps;

    // =====================================================
    //            FEUERAUSBREITUNG
    // =====================================================
    private final FireSpreadManager fireManager;

    // =====================================================
    //                 KONSTRUKTOREN
    // =====================================================

    /**
     * Erstellt eine neue Waldumgebung mit Standardeinstellungen
     */
    public ForestEnvironment(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[width][height];
        this.initialGrid = new int[width][height];
        this.fireManager = new FireSpreadManager();
        
        // Standard-Startwerte
        this.startX = width / 2;
        this.startY = height / 2;
        this.maxSteps = width * height * 2;
        
        initializeDefaultMap();
        reset();
    }

    /**
     * Erstellt eine Waldumgebung mit einem vordefinierten Layout
     */
    public ForestEnvironment(Layout layout) {
        this.layout = layout;
        this.fireManager = new FireSpreadManager();
        loadLayout(layout);
        reset();
    }

    /**
     * Erstellt eine Waldumgebung mit einer Layout-Nummer (1-5)
     */
    public ForestEnvironment(int layoutNumber) {
        this(getLayoutByNumber(layoutNumber));
    }

    // =====================================================
    //               LAYOUT MANAGEMENT
    // =====================================================

    /**
     * Gibt das Layout anhand der Nummer zurück
     */
    public static Layout getLayoutByNumber(int number) {
        return switch (number) {
            case 0 -> new TutorialLayout();       // Einfachstes Layout für Q-Tabelle
            case 1 -> new SimpleEscapeLayout();
            case 2 -> new NarrowPassLayout();
            case 3 -> new WaterRefugeLayout();
            case 4 -> new LabyrinthLayout();
            case 5 -> new InfernoLayout();
            case 6 -> new DemoLayout();           // Großes Demo-Layout (20x20)
            default -> new SimpleEscapeLayout();
        };
    }

    /**
     * Lädt ein Layout in die Umgebung
     */
    public void loadLayout(Layout layout) {
        this.layout = layout;
        this.width = layout.getWidth();
        this.height = layout.getHeight();
        this.grid = layout.createGrid();
        this.initialGrid = new int[width][height];
        this.startX = layout.getStartX();
        this.startY = layout.getStartY();
        this.maxSteps = layout.getMaxSteps();
        
        copyGrid(grid, initialGrid);
    }

    /**
     * Initialisiert eine Standard-Map mit Ausgängen an den Ecken
     */
    private void initializeDefaultMap() {
        // Alles als begehbaren Wald initialisieren
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = ForestConstants.EMPTY;
            }
        }
        
        // Ausgänge an den vier Ecken
        grid[0][0] = ForestConstants.EXIT;
        grid[width-1][0] = ForestConstants.EXIT;
        grid[0][height-1] = ForestConstants.EXIT;
        grid[width-1][height-1] = ForestConstants.EXIT;
        
        // Initiales Feuer nahe der Mitte
        int midX = width / 2;
        int midY = height / 2;
        if (midX > 1) grid[midX - 1][midY] = ForestConstants.FIRE;
        if (midX < width - 2) grid[midX + 1][midY] = ForestConstants.FIRE;
        
        copyGrid(grid, initialGrid);
    }

    // =====================================================
    //                 SPIELMECHANIK
    // =====================================================

    /**
     * Setzt die Umgebung auf den Ausgangszustand zurück
     */
    public void reset() {
        copyGrid(initialGrid, grid);
        this.deerX = startX;
        this.deerY = startY;
        this.terminated = false;
        this.escaped = false;
        this.currentStep = 0;
    }

    /**
     * Führt eine Aktion aus und gibt das Ergebnis zurück
     * 
     * @param action Die auszuführende Aktion (0=UP, 1=DOWN, 2=LEFT, 3=RIGHT)
     * @return StepResult mit nextState, reward und done-Flag
     */
    public StepResult step(int action) {
        if (terminated) {
            return new StepResult(getState(), 0, true, escaped);
        }

        currentStep++;
        
        // Neue Position berechnen
        int newX = deerX;
        int newY = deerY;
        
        switch (action) {
            case ForestConstants.ACTION_UP:    newY = Math.max(0, deerY - 1); break;
            case ForestConstants.ACTION_DOWN:  newY = Math.min(height - 1, deerY + 1); break;
            case ForestConstants.ACTION_LEFT:  newX = Math.max(0, deerX - 1); break;
            case ForestConstants.ACTION_RIGHT: newX = Math.min(width - 1, deerX + 1); break;
        }
        
        double reward = ForestConstants.REWARD_STEP;  // Grundlegende Zeitstrafe
        
        // Prüfen ob Bewegung möglich ist
        if (isObstacle(newX, newY)) {
            reward = ForestConstants.REWARD_WALL_HIT;
        } else {
            // Bewegung durchführen
            deerX = newX;
            deerY = newY;
            
            // Zelltyp der neuen Position prüfen
            int cellType = grid[deerX][deerY];
            
            switch (cellType) {
                case ForestConstants.FIRE:
                    reward = ForestConstants.REWARD_FIRE;
                    terminated = true;
                    escaped = false;
                    break;
                case ForestConstants.EXIT:
                    reward = ForestConstants.REWARD_EXIT;
                    terminated = true;
                    escaped = true;
                    break;
                case ForestConstants.WATER:
                    reward = ForestConstants.REWARD_WATER;
                    break;
            }
        }
        
        // Dynamische Feuerausbreitung
        if (fireManager.shouldSpread(currentStep)) {
            fireManager.spreadFire(grid, width, height);
            
            // Prüfen ob Reh jetzt im Feuer steht
            if (grid[deerX][deerY] == ForestConstants.FIRE && !terminated) {
                reward = ForestConstants.REWARD_FIRE;
                terminated = true;
                escaped = false;
            }
        }
        
        // Maximale Schritte erreicht?
        if (currentStep >= maxSteps && !terminated) {
            terminated = true;
            escaped = false;
            reward = ForestConstants.REWARD_FIRE / 2;  // Halbe Strafe für Zeitüberschreitung
        }
        
        return new StepResult(getState(), reward, terminated, escaped);
    }

    // =====================================================
    //                 HILFSMETHODEN
    // =====================================================

    /**
     * Gibt den aktuellen Zustand als Array zurück [x, y]
     */
    public int[] getState() {
        return new int[]{deerX, deerY};
    }

    /**
     * Gibt den normalisierten Zustand für NN-Input zurück
     */
    public double[] getNormalizedState() {
        return new double[]{
            deerX / (double) width,
            deerY / (double) height
        };
    }

    /**
     * Gibt erweiterten Zustand für NN zurück (mit Feuer-Informationen)
     */
    public double[] getExtendedState() {
        double nearestExitDist = getNearestExitDistance();
        double nearestFireDist = getNearestFireDistance();
        double maxDist = Math.sqrt(width * width + height * height);
        
        return new double[]{
            deerX / (double) width,
            deerY / (double) height,
            nearestExitDist / maxDist,
            nearestFireDist / maxDist,
            hasFireInDirection(ForestConstants.ACTION_UP) ? 1.0 : 0.0,
            hasFireInDirection(ForestConstants.ACTION_DOWN) ? 1.0 : 0.0,
            hasFireInDirection(ForestConstants.ACTION_LEFT) ? 1.0 : 0.0,
            hasFireInDirection(ForestConstants.ACTION_RIGHT) ? 1.0 : 0.0
        };
    }

    /**
     * Berechnet die Distanz zum nächsten Ausgang
     */
    public double getNearestExitDistance() {
        double minDist = Double.MAX_VALUE;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == ForestConstants.EXIT) {
                    double dist = Math.sqrt(Math.pow(deerX - x, 2) + Math.pow(deerY - y, 2));
                    minDist = Math.min(minDist, dist);
                }
            }
        }
        
        return minDist == Double.MAX_VALUE ? 0 : minDist;
    }

    /**
     * Berechnet die Distanz zum nächsten Feuer
     */
    public double getNearestFireDistance() {
        double minDist = Double.MAX_VALUE;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == ForestConstants.FIRE) {
                    double dist = Math.sqrt(Math.pow(deerX - x, 2) + Math.pow(deerY - y, 2));
                    minDist = Math.min(minDist, dist);
                }
            }
        }
        
        return minDist == Double.MAX_VALUE ? width + height : minDist;
    }

    /**
     * Prüft ob in einer bestimmten Richtung Feuer ist (nächste 3 Zellen)
     */
    public boolean hasFireInDirection(int direction) {
        int dx = 0, dy = 0;
        switch (direction) {
            case ForestConstants.ACTION_UP:    dy = -1; break;
            case ForestConstants.ACTION_DOWN:  dy = 1; break;
            case ForestConstants.ACTION_LEFT:  dx = -1; break;
            case ForestConstants.ACTION_RIGHT: dx = 1; break;
        }
        
        for (int i = 1; i <= 3; i++) {
            int checkX = deerX + dx * i;
            int checkY = deerY + dy * i;
            
            if (isValidPosition(checkX, checkY) && grid[checkX][checkY] == ForestConstants.FIRE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft ob eine Position gültig ist
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Prüft ob eine Zelle ein Hindernis ist
     */
    public boolean isObstacle(int x, int y) {
        if (!isValidPosition(x, y)) return true;
        return grid[x][y] == ForestConstants.OBSTACLE;
    }

    /**
     * Kopiert ein Grid in ein anderes
     */
    private void copyGrid(int[][] source, int[][] dest) {
        for (int x = 0; x < source.length; x++) {
            System.arraycopy(source[x], 0, dest[x], 0, source[x].length);
        }
    }

    // =====================================================
    //               GETTER & SETTER
    // =====================================================

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getGrid() {
        return grid;
    }

    public int getCellType(int x, int y) {
        if (!isValidPosition(x, y)) return ForestConstants.OBSTACLE;
        return grid[x][y];
    }

    public void setCellType(int x, int y, int type) {
        if (isValidPosition(x, y)) {
            grid[x][y] = type;
        }
    }

    public int getDeerX() {
        return deerX;
    }

    public int getDeerY() {
        return deerY;
    }

    public void setStartPosition(int x, int y) {
        this.startX = x;
        this.startY = y;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public boolean hasEscaped() {
        return escaped;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public FireSpreadManager getFireManager() {
        return fireManager;
    }

    public void setDynamicFireEnabled(boolean enabled) {
        fireManager.setEnabled(enabled);
    }

    public boolean isDynamicFireEnabled() {
        return fireManager.isEnabled();
    }

    public void setFireSpreadProbability(double probability) {
        fireManager.setSpreadProbability(probability);
    }

    public void setFireSpreadInterval(int interval) {
        fireManager.setSpreadInterval(interval);
    }

    public Layout getLayout() {
        return layout;
    }

    // =====================================================
    //                 STRING OUTPUT
    // =====================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ForestEnvironment ").append(width).append("x").append(height);
        if (layout != null) {
            sb.append(" [").append(layout.getName()).append("]");
        }
        sb.append(" | Deer at (").append(deerX).append(",").append(deerY).append(")\n");
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == deerX && y == deerY) {
                    sb.append("🦌");
                } else {
                    switch (grid[x][y]) {
                        case ForestConstants.EMPTY:    sb.append("🌲"); break;
                        case ForestConstants.FIRE:     sb.append("🔥"); break;
                        case ForestConstants.EXIT:     sb.append("🚪"); break;
                        case ForestConstants.OBSTACLE: sb.append("🪨"); break;
                        case ForestConstants.WATER:    sb.append("💧"); break;
                        default:       sb.append("? "); break;
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * ASCII-Version für Konsolen ohne Emoji-Support
     */
    public String toAsciiString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ForestEnvironment ").append(width).append("x").append(height);
        if (layout != null) {
            sb.append(" [").append(layout.getName()).append("]");
        }
        sb.append(" | Deer at (").append(deerX).append(",").append(deerY).append(")\n");
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == deerX && y == deerY) {
                    sb.append("D ");
                } else {
                    switch (grid[x][y]) {
                        case ForestConstants.EMPTY:    sb.append(". "); break;
                        case ForestConstants.FIRE:     sb.append("F "); break;
                        case ForestConstants.EXIT:     sb.append("E "); break;
                        case ForestConstants.OBSTACLE: sb.append("# "); break;
                        case ForestConstants.WATER:    sb.append("~ "); break;
                        default:       sb.append("? "); break;
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // =====================================================
    //                    MAIN (TEST)
    // =====================================================

    public static void main(String[] args) {
        System.out.println("=== ForestEnvironment Test ===\n");
        
        // Test alle Layouts
        for (int i = 0; i <= 5; i++) {
            Layout layout = getLayoutByNumber(i);
            System.out.println("Layout " + i + " - " + layout.getName() + ":");
            ForestEnvironment env = new ForestEnvironment(layout);
            System.out.println(env.toAsciiString());
        }
        
        // Simuliere ein paar Schritte
        System.out.println("\n=== Simulation ===");
        ForestEnvironment env = new ForestEnvironment(new SimpleEscapeLayout());
        env.reset();
        
        System.out.println("Start:");
        System.out.println(env.toAsciiString());
        
        // Zufällige Schritte
        Random rand = new Random();
        for (int i = 0; i < 20 && !env.isTerminated(); i++) {
            int action = rand.nextInt(ForestConstants.NUM_ACTIONS);
            StepResult result = env.step(action);
            System.out.printf("Step %d: Action=%s, %s\n", i + 1, ForestConstants.ACTION_NAMES[action], result);
        }
        
        System.out.println("\nEnd State:");
        System.out.println(env.toAsciiString());
        System.out.println("Escaped: " + env.hasEscaped());
    }
}

