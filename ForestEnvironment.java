import java.util.ArrayList;
import java.util.List;
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
    //                    ZELLTYPEN
    // =====================================================
    public static final int EMPTY    = 0;  // Begehbarer Wald
    public static final int FIRE     = 1;  // Feuer (tödlich)
    public static final int EXIT     = 2;  // Ausgang (Ziel)
    public static final int OBSTACLE = 3;  // Hindernis (unpassierbar)
    public static final int WATER    = 4;  // Wasser (sicher, Feuer kann nicht durchbrennen)

    // =====================================================
    //                    AKTIONEN
    // =====================================================
    public static final int ACTION_UP    = 0;
    public static final int ACTION_DOWN  = 1;
    public static final int ACTION_LEFT  = 2;
    public static final int ACTION_RIGHT = 3;
    public static final int NUM_ACTIONS  = 4;

    // =====================================================
    //                    REWARDS
    // =====================================================
    public static final double REWARD_EXIT        = 100.0;   // Ausgang erreicht
    public static final double REWARD_FIRE        = -100.0;  // Im Feuer verbrannt
    public static final double REWARD_STEP        = -1.0;    // Zeitstrafe pro Schritt
    public static final double REWARD_WALL_HIT    = -2.0;    // Gegen Wand/Hindernis gelaufen
    public static final double REWARD_WATER       = 0.0;     // Neutrale Sicherheit im Wasser

    // =====================================================
    //                  SPIELFELD
    // =====================================================
    private final int width;
    private final int height;
    private int[][] grid;
    private int[][] initialGrid;  // Für Reset

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
    private boolean dynamicFireEnabled;
    private double fireSpreadProbability;
    private int fireSpreadInterval;  // Alle X Schritte breitet sich Feuer aus
    private Random random;

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
        this.random = new Random();
        
        // Standard-Startwerte
        this.startX = width / 2;
        this.startY = height / 2;
        this.maxSteps = width * height * 2;  // Maximale Schritte bevor Episode endet
        
        // Feuerausbreitung standardmäßig deaktiviert
        this.dynamicFireEnabled = false;
        this.fireSpreadProbability = 0.3;
        this.fireSpreadInterval = 3;
        
        initializeDefaultMap();
        reset();
    }

    /**
     * Erstellt eine Waldumgebung mit einem vordefinierten Layout
     */
    public ForestEnvironment(int layoutNumber) {
        this.random = new Random();
        this.dynamicFireEnabled = false;
        this.fireSpreadProbability = 0.3;
        this.fireSpreadInterval = 3;
        
        loadLayout(layoutNumber);
        reset();
    }

    // =====================================================
    //               MAP INITIALISIERUNG
    // =====================================================

    /**
     * Initialisiert eine Standard-Map mit Ausgängen an den Ecken
     */
    private void initializeDefaultMap() {
        // Alles als begehbaren Wald initialisieren
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = EMPTY;
            }
        }
        
        // Ausgänge an den vier Ecken
        grid[0][0] = EXIT;
        grid[width-1][0] = EXIT;
        grid[0][height-1] = EXIT;
        grid[width-1][height-1] = EXIT;
        
        // Initiales Feuer nahe der Mitte
        int midX = width / 2;
        int midY = height / 2;
        if (midX > 1) grid[midX - 1][midY] = FIRE;
        if (midX < width - 2) grid[midX + 1][midY] = FIRE;
        
        // Initial-Grid speichern
        copyGrid(grid, initialGrid);
    }

    /**
     * Lädt ein vordefiniertes Map-Layout
     */
    public void loadLayout(int layoutNumber) {
        switch (layoutNumber) {
            case 1:
                loadLayout1_SimpleEscape();
                break;
            case 2:
                loadLayout2_NarrowPass();
                break;
            case 3:
                loadLayout3_WaterRefuge();
                break;
            case 4:
                loadLayout4_Labyrinth();
                break;
            case 5:
                loadLayout5_Inferno();
                break;
            default:
                loadLayout1_SimpleEscape();
        }
        copyGrid(grid, initialGrid);
    }

    /**
     * Layout 1: Einfache Flucht - 10x10 Grid mit 4 Ausgängen
     */
    private void loadLayout1_SimpleEscape() {
        initializeGrid(10, 10);
        
        // Ausgänge an den Ecken
        grid[0][0] = EXIT;
        grid[9][0] = EXIT;
        grid[0][9] = EXIT;
        grid[9][9] = EXIT;
        
        // Feuer in der Mitte
        grid[4][4] = FIRE;
        grid[5][4] = FIRE;
        grid[4][5] = FIRE;
        grid[5][5] = FIRE;
        
        this.startX = 5;
        this.startY = 5;
        this.maxSteps = 100;
    }

    /**
     * Layout 2: Der enge Pass - Reh muss durch einen engen Durchgang
     */
    private void loadLayout2_NarrowPass() {
        initializeGrid(10, 10);
        
        // Ausgänge oben und unten
        grid[0][0] = EXIT;
        grid[9][0] = EXIT;
        grid[0][9] = EXIT;
        grid[9][9] = EXIT;
        
        // Vertikale Hindernismauer mit Durchgang
        for (int y = 0; y < 10; y++) {
            if (y != 4 && y != 5) {  // Durchgang bei y=4,5
                grid[5][y] = OBSTACLE;
            }
        }
        
        // Feuer auf einer Seite
        grid[7][3] = FIRE;
        grid[7][4] = FIRE;
        grid[7][5] = FIRE;
        grid[7][6] = FIRE;
        
        this.startX = 2;
        this.startY = 5;
        this.maxSteps = 150;
    }

    /**
     * Layout 3: Wasserrefugium - Wasser bietet Schutz vor Feuer
     */
    private void loadLayout3_WaterRefuge() {
        initializeGrid(10, 10);
        
        // Ausgänge
        grid[0][0] = EXIT;
        grid[9][0] = EXIT;
        grid[0][9] = EXIT;
        grid[9][9] = EXIT;
        
        // Wasser-Teich in der Mitte
        for (int x = 3; x <= 6; x++) {
            for (int y = 3; y <= 6; y++) {
                grid[x][y] = WATER;
            }
        }
        
        // Feuer umgibt den Teich teilweise
        grid[2][3] = FIRE;
        grid[2][4] = FIRE;
        grid[2][5] = FIRE;
        grid[2][6] = FIRE;
        grid[7][3] = FIRE;
        grid[7][4] = FIRE;
        
        this.startX = 5;
        this.startY = 5;
        this.maxSteps = 120;
    }

    /**
     * Layout 4: Labyrinth - Komplexere Struktur mit vielen Hindernissen
     */
    private void loadLayout4_Labyrinth() {
        initializeGrid(12, 12);
        
        // Ausgänge nur an zwei Stellen
        grid[0][6] = EXIT;
        grid[11][6] = EXIT;
        
        // Labyrinth-Wände
        // Horizontale Wände
        for (int x = 2; x < 6; x++) grid[x][2] = OBSTACLE;
        for (int x = 6; x < 10; x++) grid[x][4] = OBSTACLE;
        for (int x = 2; x < 6; x++) grid[x][6] = OBSTACLE;
        for (int x = 6; x < 10; x++) grid[x][8] = OBSTACLE;
        
        // Vertikale Wände
        for (int y = 2; y < 5; y++) grid[8][y] = OBSTACLE;
        for (int y = 6; y < 9; y++) grid[4][y] = OBSTACLE;
        
        // Feuer
        grid[5][5] = FIRE;
        grid[6][5] = FIRE;
        grid[7][5] = FIRE;
        
        this.startX = 6;
        this.startY = 6;
        this.maxSteps = 200;
    }

    /**
     * Layout 5: Inferno - Viel Feuer, wenige sichere Wege
     */
    private void loadLayout5_Inferno() {
        initializeGrid(15, 15);
        
        // Ausgänge an den Rändern
        grid[0][7] = EXIT;
        grid[14][7] = EXIT;
        grid[7][0] = EXIT;
        grid[7][14] = EXIT;
        
        // Viel Feuer
        for (int i = 0; i < 15; i++) {
            // Diagonale Feuerlinien
            if (i != 7) {  // Durchgang bei i=7
                grid[i][i] = FIRE;
                if (14 - i >= 0 && 14 - i < 15) grid[i][14 - i] = FIRE;
            }
        }
        
        // Sichere Wasserzonen
        grid[3][7] = WATER;
        grid[11][7] = WATER;
        grid[7][3] = WATER;
        grid[7][11] = WATER;
        
        // Zusätzliche Hindernisse
        grid[5][5] = OBSTACLE;
        grid[9][5] = OBSTACLE;
        grid[5][9] = OBSTACLE;
        grid[9][9] = OBSTACLE;
        
        this.startX = 7;
        this.startY = 7;
        this.maxSteps = 300;
    }

    /**
     * Initialisiert das Grid mit gegebener Größe
     */
    private void initializeGrid(int w, int h) {
        // Felder aktualisieren - Reflexion über die Feldgröße
        try {
            java.lang.reflect.Field widthField = this.getClass().getDeclaredField("width");
            java.lang.reflect.Field heightField = this.getClass().getDeclaredField("height");
            widthField.setAccessible(true);
            heightField.setAccessible(true);
            widthField.setInt(this, w);
            heightField.setInt(this, h);
        } catch (Exception e) {
            // Fallback - funktioniert nicht mit final fields
        }
        
        this.grid = new int[w][h];
        this.initialGrid = new int[w][h];
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                grid[x][y] = EMPTY;
            }
        }
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
            case ACTION_UP:    newY = Math.max(0, deerY - 1); break;
            case ACTION_DOWN:  newY = Math.min(getHeight() - 1, deerY + 1); break;
            case ACTION_LEFT:  newX = Math.max(0, deerX - 1); break;
            case ACTION_RIGHT: newX = Math.min(getWidth() - 1, deerX + 1); break;
        }
        
        double reward = REWARD_STEP;  // Grundlegende Zeitstrafe
        
        // Prüfen ob Bewegung möglich ist
        if (isObstacle(newX, newY)) {
            // Gegen Hindernis gelaufen - bleibe an alter Position
            reward = REWARD_WALL_HIT;
        } else {
            // Bewegung durchführen
            deerX = newX;
            deerY = newY;
            
            // Zelltyp der neuen Position prüfen
            int cellType = grid[deerX][deerY];
            
            switch (cellType) {
                case FIRE:
                    reward = REWARD_FIRE;
                    terminated = true;
                    escaped = false;
                    break;
                case EXIT:
                    reward = REWARD_EXIT;
                    terminated = true;
                    escaped = true;
                    break;
                case WATER:
                    reward = REWARD_WATER;
                    break;
            }
        }
        
        // Dynamische Feuerausbreitung
        if (dynamicFireEnabled && currentStep % fireSpreadInterval == 0) {
            spreadFire();
            
            // Prüfen ob Reh jetzt im Feuer steht
            if (grid[deerX][deerY] == FIRE && !terminated) {
                reward = REWARD_FIRE;
                terminated = true;
                escaped = false;
            }
        }
        
        // Maximale Schritte erreicht?
        if (currentStep >= maxSteps && !terminated) {
            terminated = true;
            escaped = false;
            reward = REWARD_FIRE / 2;  // Halbe Strafe für Zeitüberschreitung
        }
        
        return new StepResult(getState(), reward, terminated, escaped);
    }

    /**
     * Breitet das Feuer auf benachbarte Zellen aus
     */
    public void spreadFire() {
        List<int[]> newFireCells = new ArrayList<>();
        
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (grid[x][y] == FIRE) {
                    // Prüfe alle Nachbarn
                    int[][] neighbors = {
                        {x, y - 1}, // oben
                        {x, y + 1}, // unten
                        {x - 1, y}, // links
                        {x + 1, y}  // rechts
                    };
                    
                    for (int[] neighbor : neighbors) {
                        int nx = neighbor[0];
                        int ny = neighbor[1];
                        
                        if (isValidPosition(nx, ny) && isFlammable(nx, ny)) {
                            if (random.nextDouble() < fireSpreadProbability) {
                                newFireCells.add(new int[]{nx, ny});
                            }
                        }
                    }
                }
            }
        }
        
        // Neue Feuerzellen setzen
        for (int[] cell : newFireCells) {
            grid[cell[0]][cell[1]] = FIRE;
        }
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
            deerX / (double) getWidth(),
            deerY / (double) getHeight()
        };
    }

    /**
     * Gibt erweiterten Zustand für NN zurück (mit Feuer-Informationen)
     */
    public double[] getExtendedState() {
        double nearestExitDist = getNearestExitDistance();
        double nearestFireDist = getNearestFireDistance();
        double maxDist = Math.sqrt(getWidth() * getWidth() + getHeight() * getHeight());
        
        return new double[]{
            deerX / (double) getWidth(),
            deerY / (double) getHeight(),
            nearestExitDist / maxDist,
            nearestFireDist / maxDist,
            hasFireInDirection(ACTION_UP) ? 1.0 : 0.0,
            hasFireInDirection(ACTION_DOWN) ? 1.0 : 0.0,
            hasFireInDirection(ACTION_LEFT) ? 1.0 : 0.0,
            hasFireInDirection(ACTION_RIGHT) ? 1.0 : 0.0
        };
    }

    /**
     * Berechnet die Distanz zum nächsten Ausgang
     */
    public double getNearestExitDistance() {
        double minDist = Double.MAX_VALUE;
        
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (grid[x][y] == EXIT) {
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
        
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (grid[x][y] == FIRE) {
                    double dist = Math.sqrt(Math.pow(deerX - x, 2) + Math.pow(deerY - y, 2));
                    minDist = Math.min(minDist, dist);
                }
            }
        }
        
        return minDist == Double.MAX_VALUE ? getWidth() + getHeight() : minDist;
    }

    /**
     * Prüft ob in einer bestimmten Richtung Feuer ist (nächste 3 Zellen)
     */
    public boolean hasFireInDirection(int direction) {
        int dx = 0, dy = 0;
        switch (direction) {
            case ACTION_UP:    dy = -1; break;
            case ACTION_DOWN:  dy = 1; break;
            case ACTION_LEFT:  dx = -1; break;
            case ACTION_RIGHT: dx = 1; break;
        }
        
        for (int i = 1; i <= 3; i++) {
            int checkX = deerX + dx * i;
            int checkY = deerY + dy * i;
            
            if (isValidPosition(checkX, checkY) && grid[checkX][checkY] == FIRE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft ob eine Position gültig ist
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    /**
     * Prüft ob eine Zelle ein Hindernis ist
     */
    public boolean isObstacle(int x, int y) {
        if (!isValidPosition(x, y)) return true;
        return grid[x][y] == OBSTACLE;
    }

    /**
     * Prüft ob eine Zelle brennbar ist
     */
    public boolean isFlammable(int x, int y) {
        if (!isValidPosition(x, y)) return false;
        int cellType = grid[x][y];
        // Nur leere Zellen können brennen, nicht Wasser, Hindernisse oder Ausgänge
        return cellType == EMPTY;
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
        return grid.length;
    }

    public int getHeight() {
        return grid[0].length;
    }

    public int[][] getGrid() {
        return grid;
    }

    public int getCellType(int x, int y) {
        if (!isValidPosition(x, y)) return OBSTACLE;
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

    public void setDynamicFireEnabled(boolean enabled) {
        this.dynamicFireEnabled = enabled;
    }

    public boolean isDynamicFireEnabled() {
        return dynamicFireEnabled;
    }

    public void setFireSpreadProbability(double probability) {
        this.fireSpreadProbability = probability;
    }

    public void setFireSpreadInterval(int interval) {
        this.fireSpreadInterval = interval;
    }

    // =====================================================
    //                 STRING OUTPUT
    // =====================================================

    /**
     * Gibt das Grid als String aus (für Debugging)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ForestEnvironment ").append(getWidth()).append("x").append(getHeight());
        sb.append(" | Deer at (").append(deerX).append(",").append(deerY).append(")\n");
        
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (x == deerX && y == deerY) {
                    sb.append("🦌");
                } else {
                    switch (grid[x][y]) {
                        case EMPTY:    sb.append("🌲"); break;
                        case FIRE:     sb.append("🔥"); break;
                        case EXIT:     sb.append("🚪"); break;
                        case OBSTACLE: sb.append("🪨"); break;
                        case WATER:    sb.append("💧"); break;
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
        sb.append("ForestEnvironment ").append(getWidth()).append("x").append(getHeight());
        sb.append(" | Deer at (").append(deerX).append(",").append(deerY).append(")\n");
        
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (x == deerX && y == deerY) {
                    sb.append("D ");
                } else {
                    switch (grid[x][y]) {
                        case EMPTY:    sb.append(". "); break;
                        case FIRE:     sb.append("F "); break;
                        case EXIT:     sb.append("E "); break;
                        case OBSTACLE: sb.append("# "); break;
                        case WATER:    sb.append("~ "); break;
                        default:       sb.append("? "); break;
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // =====================================================
    //                  STEP RESULT
    // =====================================================

    /**
     * Ergebnis eines step()-Aufrufs
     */
    public static class StepResult {
        public final int[] state;
        public final double reward;
        public final boolean done;
        public final boolean escaped;

        public StepResult(int[] state, double reward, boolean done, boolean escaped) {
            this.state = state;
            this.reward = reward;
            this.done = done;
            this.escaped = escaped;
        }

        @Override
        public String toString() {
            return String.format("StepResult{state=[%d,%d], reward=%.1f, done=%b, escaped=%b}",
                    state[0], state[1], reward, done, escaped);
        }
    }

    // =====================================================
    //                    MAIN (TEST)
    // =====================================================

    public static void main(String[] args) {
        System.out.println("=== ForestEnvironment Test ===\n");
        
        // Test Layout 1
        System.out.println("Layout 1 - Simple Escape:");
        ForestEnvironment env1 = new ForestEnvironment(1);
        System.out.println(env1.toAsciiString());
        
        // Test Layout 2
        System.out.println("Layout 2 - Narrow Pass:");
        ForestEnvironment env2 = new ForestEnvironment(2);
        System.out.println(env2.toAsciiString());
        
        // Test Layout 3
        System.out.println("Layout 3 - Water Refuge:");
        ForestEnvironment env3 = new ForestEnvironment(3);
        System.out.println(env3.toAsciiString());
        
        // Simuliere ein paar Schritte
        System.out.println("\n=== Simulation ===");
        ForestEnvironment env = new ForestEnvironment(1);
        env.reset();
        
        System.out.println("Start:");
        System.out.println(env.toAsciiString());
        
        // Zufällige Schritte
        Random rand = new Random();
        for (int i = 0; i < 20 && !env.isTerminated(); i++) {
            int action = rand.nextInt(NUM_ACTIONS);
            String[] actionNames = {"UP", "DOWN", "LEFT", "RIGHT"};
            
            StepResult result = env.step(action);
            System.out.printf("Step %d: Action=%s, %s\n", i + 1, actionNames[action], result);
        }
        
        System.out.println("\nEnd State:");
        System.out.println(env.toAsciiString());
        System.out.println("Escaped: " + env.hasEscaped());
    }
}

