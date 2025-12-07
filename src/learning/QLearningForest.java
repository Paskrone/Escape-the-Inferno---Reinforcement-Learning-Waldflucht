package learning;

import environment.*;
import layouts.*;
import visualization.HeatmapVisualizer;

/**
 * Q-Learning mit ForestEnvironment
 * Trainiert einen Agenten auf beliebigen Layouts mit einer Q-Tabelle
 * 
 * Q-Tabelle: Q[x][y][action] → Erwarteter Wert für Aktion in Position (x,y)
 */
public class QLearningForest {

    // =====================================================
    //                  HYPERPARAMETER
    // =====================================================
    private static final double ALPHA   = 0.1;    // Lernrate
    private static final double GAMMA   = 0.95;   // Discount-Faktor
    private static final double EPSILON = 0.1;    // Exploration (10%)
    private static final int EPISODES   = 5000;   // Trainings-Episoden

    // =====================================================
    //                  INSTANZVARIABLEN
    // =====================================================
    private ForestEnvironment env;
    private Layout layout;
    private double[][][] Q;  // Q-Tabelle: Q[x][y][action]
    private HeatmapVisualizer heatmap;

    /**
     * Erstellt einen Q-Learning Agenten für ein gegebenes Layout
     */
    public QLearningForest(Layout layout) {
        this.layout = layout;
        this.env = new ForestEnvironment(layout);
        this.Q = new double[env.getWidth()][env.getHeight()][ForestConstants.NUM_ACTIONS];
    }

    /**
     * Trainiert den Agenten mit Q-Learning
     */
    public void train() throws InterruptedException {
        // Heatmap für Visualisierung initialisieren
        heatmap = new HeatmapVisualizer(env.getWidth(), env.getHeight(), 
                                         findExitX(), findExitY());
        
        int escapeCount = 0;
        int recentEscapes = 0;
        
        System.out.println("Training gestartet...\n");
        System.out.println("Layout: " + layout.getName());
        System.out.println("Grid-Größe: " + env.getWidth() + "x" + env.getHeight());
        System.out.println("Max Steps: " + env.getMaxSteps());
        System.out.println();
        
        for (int ep = 1; ep <= EPISODES; ep++) {
            env.reset();
            
            while (!env.isTerminated()) {
                int[] state = env.getState();
                int x = state[0];
                int y = state[1];
                
                // Epsilon-Greedy: Exploration vs. Exploitation
                int action = chooseAction(x, y);
                
                // Aktion ausführen und Ergebnis erhalten
                StepResult result = env.step(action);
                
                int[] nextState = result.state();
                int nx = nextState[0];
                int ny = nextState[1];
                double reward = result.reward();
                
                // Q-Update (Bellman-Gleichung)
                double oldQ = Q[x][y][action];
                double maxNextQ = result.done() ? 0.0 : maxQ(nx, ny);
                double newQ = oldQ + ALPHA * (reward + GAMMA * maxNextQ - oldQ);
                Q[x][y][action] = newQ;
            }
            
            if (env.hasEscaped()) {
                escapeCount++;
                recentEscapes++;
            }
            
            // Fortschritt alle 100 Episoden anzeigen
            if (ep % 100 == 0) {
                double totalRate = (escapeCount / (double) ep) * 100;
                double recentRate = recentEscapes;  // von 100
                
                String status = recentRate >= 90 ? "🔥" : 
                               recentRate >= 70 ? "✓" : 
                               recentRate >= 50 ? "~" : "✗";
                
                System.out.printf("Episode %5d | Gesamt: %5.1f%% | Letzte 100: %3.0f%% %s%n",
                        ep, totalRate, recentRate, status);
                
                heatmap.update(Q);
                recentEscapes = 0;
                Thread.sleep(10);
            }
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TRAINING ABGESCHLOSSEN");
        System.out.println("=".repeat(50));
        System.out.printf("Erfolgsrate: %.1f%% (%d/%d Episoden)%n", 
                (escapeCount / (double) EPISODES) * 100, escapeCount, EPISODES);
    }

    /**
     * Wählt eine Aktion mit Epsilon-Greedy Strategie
     */
    private int chooseAction(int x, int y) {
        // Mit Wahrscheinlichkeit EPSILON: zufällige Aktion (Exploration)
        if (Math.random() < EPSILON) {
            return (int) (Math.random() * ForestConstants.NUM_ACTIONS);
        }
        // Sonst: beste bekannte Aktion (Exploitation)
        return argmaxQ(x, y);
    }

    /**
     * Gibt die Aktion mit dem höchsten Q-Wert zurück
     */
    private int argmaxQ(int x, int y) {
        int bestAction = 0;
        double bestValue = Q[x][y][0];
        
        for (int a = 1; a < ForestConstants.NUM_ACTIONS; a++) {
            if (Q[x][y][a] > bestValue) {
                bestValue = Q[x][y][a];
                bestAction = a;
            }
        }
        return bestAction;
    }

    /**
     * Gibt den maximalen Q-Wert für einen Zustand zurück
     */
    private double maxQ(int x, int y) {
        double max = Q[x][y][0];
        for (int a = 1; a < ForestConstants.NUM_ACTIONS; a++) {
            max = Math.max(max, Q[x][y][a]);
        }
        return max;
    }

    /**
     * Findet die X-Position des Exits (für Heatmap)
     */
    private int findExitX() {
        for (int x = 0; x < env.getWidth(); x++) {
            for (int y = 0; y < env.getHeight(); y++) {
                if (env.getCellType(x, y) == ForestConstants.EXIT) {
                    return x;
                }
            }
        }
        return env.getWidth() - 1;  // Fallback
    }

    /**
     * Findet die Y-Position des Exits (für Heatmap)
     */
    private int findExitY() {
        for (int x = 0; x < env.getWidth(); x++) {
            for (int y = 0; y < env.getHeight(); y++) {
                if (env.getCellType(x, y) == ForestConstants.EXIT) {
                    return y;
                }
            }
        }
        return 0;  // Fallback
    }

    /**
     * Testet die gelernte Policy (greedy, ohne Exploration)
     */
    public void testGreedy() {
        env.reset();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("GREEDY TEST (Ohne Exploration)");
        System.out.println("=".repeat(50));
        System.out.println("\nStartposition:");
        System.out.println(env.toAsciiString());
        
        int steps = 0;
        double totalReward = 0;
        
        while (!env.isTerminated() && steps < 50) {
            int[] state = env.getState();
            int action = argmaxQ(state[0], state[1]);
            
            StepResult result = env.step(action);
            totalReward += result.reward();
            
            System.out.printf("Step %2d: %-6s von (%d,%d) → (%d,%d) | Reward: %+6.1f%n", 
                    ++steps, 
                    ForestConstants.ACTION_NAMES[action],
                    state[0], state[1],
                    result.state()[0], result.state()[1],
                    result.reward());
        }
        
        System.out.println("\nEndposition:");
        System.out.println(env.toAsciiString());
        
        System.out.println();
        if (env.hasEscaped()) {
            System.out.println("🎉 ENTKOMMEN in " + steps + " Schritten!");
        } else {
            System.out.println("💀 GESCHEITERT nach " + steps + " Schritten!");
        }
        System.out.printf("Total Reward: %.1f%n", totalReward);
    }

    /**
     * Zeigt die Q-Tabelle für eine Position
     */
    public void printQValues(int x, int y) {
        System.out.printf("\nQ-Werte für Position (%d, %d):%n", x, y);
        for (int a = 0; a < ForestConstants.NUM_ACTIONS; a++) {
            System.out.printf("  %6s: %+8.2f%n", ForestConstants.ACTION_NAMES[a], Q[x][y][a]);
        }
    }

    // =====================================================
    //                      MAIN
    // =====================================================
    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      Q-LEARNING MIT FORESTENVIRONMENT            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Layout auswählen (0 = Tutorial, 1 = SimpleEscape, etc.)
        int layoutNumber = 0;  // Tutorial-Layout
        if (args.length > 0) {
            try {
                layoutNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Ungültiges Layout, verwende Tutorial (0)");
            }
        }

        Layout layout = getLayout(layoutNumber);
        System.out.println("Gewähltes Layout: " + layout.getName() + " (#" + layoutNumber + ")\n");

        // Agent erstellen und trainieren
        QLearningForest agent = new QLearningForest(layout);
        agent.train();
        
        // Gelerntes Verhalten testen
        agent.testGreedy();
        
        // Optional: Q-Werte für Startposition anzeigen
        agent.printQValues(layout.getStartX(), layout.getStartY());
    }

    /**
     * Hilfsmethode: Layout nach Nummer erstellen
     */
    private static Layout getLayout(int number) {
        return switch (number) {
            case 0 -> new TutorialLayout();
            case 1 -> new SimpleEscapeLayout();
            case 2 -> new NarrowPassLayout();
            case 3 -> new WaterRefugeLayout();
            case 4 -> new LabyrinthLayout();
            case 5 -> new InfernoLayout();
            default -> {
                System.out.println("Layout " + number + " existiert nicht, verwende Tutorial");
                yield new TutorialLayout();
            }
        };
    }
}

