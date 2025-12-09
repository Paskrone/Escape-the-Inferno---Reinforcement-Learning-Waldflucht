package learning;

import environment.*;
import layouts.*;
import nn.*;
import visualization.NNHeatmapVisualizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Deep Q-Learning mit ForestEnvironment
 * Verwendet ein neuronales Netz (FFN) zur Q-Wert-Approximation statt einer Q-Tabelle
 * 
 * Vorteile gegenüber Q-Tabelle:
 * - Generalisierung auf ähnliche Zustände
 * - Skaliert besser auf größere State-Spaces
 * - Kann mit kontinuierlichen/erweiterten Features umgehen
 */
public class QLearningForest_NN {

    // =====================================================
    //                  HYPERPARAMETER
    // =====================================================
    private static final double GAMMA        = 0.95;   // Discount-Faktor
    private static final double EPSILON_START = 0.9;   // Initiale Exploration
    private static final double EPSILON_END   = 0.05;  // Minimale Exploration
    private static final double EPSILON_DECAY = 0.995; // Decay pro Episode
    private static final int EPISODES         = 5000;  // Trainings-Episoden
    private static final double LEARNING_RATE = 0.01;  // NN Lernrate

    // Heatmap-Update-Intervalle (häufiger am Anfang, seltener später)
    private static final int[] HEATMAP_INTERVALS = {10, 50, 100, 200};  // Update alle X Episoden
    private static final int[] INTERVAL_THRESHOLDS = {100, 500, 2000};  // Ab Episode X nächstes Intervall

    // =====================================================
    //                  INSTANZVARIABLEN
    // =====================================================
    private ForestEnvironment env;
    private Layout layout;
    private FFN network;
    private LossFunction lossFunction;
    private NNHeatmapVisualizer heatmap;
    private double epsilon;

    // Netzwerk-Architektur
    private static final int INPUT_SIZE = 8;   // Extended State: x, y, exitDist, fireDist, 4x fireDirection
    private static final int HIDDEN_SIZE = 64;
    private static final int OUTPUT_SIZE = ForestConstants.NUM_ACTIONS;  // 4 Aktionen

    /**
     * Erstellt einen Deep Q-Learning Agenten für ein gegebenes Layout
     */
    public QLearningForest_NN(Layout layout) {
        this.layout = layout;
        this.env = new ForestEnvironment(layout);
        this.epsilon = EPSILON_START;
        this.lossFunction = new MeanSquaredError();
        
        // Neural Network initialisieren
        int[] layerSizes = {INPUT_SIZE, HIDDEN_SIZE, HIDDEN_SIZE, OUTPUT_SIZE};
        this.network = new FFN(layerSizes, "relu", "none", 32);
        
        System.out.println("Neural Network initialisiert:");
        System.out.println("  Input:  " + INPUT_SIZE + " (Extended State)");
        System.out.println("  Hidden: " + HIDDEN_SIZE + " x 2 (ReLU)");
        System.out.println("  Output: " + OUTPUT_SIZE + " (Q-Werte für Aktionen)");
    }

    /**
     * Trainiert den Agenten mit Deep Q-Learning
     */
    public void train() throws InterruptedException {
        // NN-Heatmap für Visualisierung initialisieren (mit Grid-Typen)
        heatmap = new NNHeatmapVisualizer(env.getWidth(), env.getHeight(), findAllExits(), env.getGrid());
        heatmap.setStartPosition(layout.getStartX(), layout.getStartY());
        
        int escapeCount = 0;
        int recentEscapes = 0;
        
        System.out.println("\nTraining gestartet...\n");
        System.out.println("Layout: " + layout.getName());
        System.out.println("Grid-Größe: " + env.getWidth() + "x" + env.getHeight());
        System.out.println("Max Steps: " + env.getMaxSteps());
        System.out.println("Epsilon: " + EPSILON_START + " → " + EPSILON_END);
        System.out.println();
        
        for (int ep = 1; ep <= EPISODES; ep++) {
            env.reset();
            
            while (!env.isTerminated()) {
                // State als Extended-Feature-Vector
                double[] state = env.getExtendedState();
                
                // Epsilon-Greedy: Exploration vs. Exploitation
                int action = chooseAction(state);
                
                // Aktion ausführen und Ergebnis erhalten
                StepResult result = env.step(action);
                
                double[] nextState = env.getExtendedState();
                double reward = result.reward();
                boolean done = result.done();
                
                // Q-Learning Update mit Neural Network
                updateNetwork(state, action, reward, nextState, done);
            }
            
            if (env.hasEscaped()) {
                escapeCount++;
                recentEscapes++;
            }
            
            // Epsilon Decay
            epsilon = Math.max(EPSILON_END, epsilon * EPSILON_DECAY);
            
            // Dynamisches Heatmap-Update-Intervall
            int interval = getHeatmapInterval(ep);
            if (ep % interval == 0) {
                double totalRate = (escapeCount / (double) ep) * 100;
                
                String status = recentEscapes >= interval * 0.9 ? "🔥" : 
                               recentEscapes >= interval * 0.7 ? "✓" : 
                               recentEscapes >= interval * 0.5 ? "~" : "✗";
                
                double recentRate = (recentEscapes / (double) interval) * 100;
                System.out.printf("Episode %5d | Gesamt: %5.1f%% | Letzte %3d: %5.1f%% %s | ε=%.3f%n",
                        ep, totalRate, interval, recentRate, status, epsilon);
                
                // Heatmap aus NN-Predictions berechnen
                heatmap.update(computeQFromNetwork());
                recentEscapes = 0;
                Thread.sleep(50);  // Kurze Pause für Visualisierung
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
    private int chooseAction(double[] state) {
        // Mit Wahrscheinlichkeit EPSILON: zufällige Aktion (Exploration)
        if (Math.random() < epsilon) {
            return (int) (Math.random() * ForestConstants.NUM_ACTIONS);
        }
        // Sonst: beste bekannte Aktion (Exploitation)
        return argmaxQ(state);
    }

    /**
     * Gibt die Aktion mit dem höchsten Q-Wert zurück
     */
    private int argmaxQ(double[] state) {
        double[] qValues = network.predictQ(state);
        int bestAction = 0;
        double bestValue = qValues[0];
        
        for (int a = 1; a < ForestConstants.NUM_ACTIONS; a++) {
            if (qValues[a] > bestValue) {
                bestValue = qValues[a];
                bestAction = a;
            }
        }
        return bestAction;
    }

    /**
     * Aktualisiert das Neural Network mit Q-Learning Update
     */
    private void updateNetwork(double[] state, int action, double reward, double[] nextState, boolean done) {
        // Aktuelle Q-Werte für den State
        double[] qValues = network.predictQ(state);
        
        // Target berechnen (Bellman-Gleichung)
        double targetValue;
        if (done) {
            targetValue = reward;
        } else {
            double[] nextQValues = network.predictQ(nextState);
            double maxNextQ = nextQValues[0];
            for (int a = 1; a < ForestConstants.NUM_ACTIONS; a++) {
                maxNextQ = Math.max(maxNextQ, nextQValues[a]);
            }
            targetValue = reward + GAMMA * maxNextQ;
        }
        
        // Target-Vektor erstellen (nur gewählte Aktion wird geupdated)
        double[] target = qValues.clone();
        target[action] = targetValue;
        
        // Netzwerk trainieren
                network.trainMiniBatchFromAction(state, target, LEARNING_RATE, lossFunction, 0);
    }

    /**
     * Berechnet Q-Werte für alle Positionen aus dem Neural Network (für Heatmap)
     */
    private double[][][] computeQFromNetwork() {
        double[][][] Q = new double[env.getWidth()][env.getHeight()][ForestConstants.NUM_ACTIONS];
        
        for (int x = 0; x < env.getWidth(); x++) {
            for (int y = 0; y < env.getHeight(); y++) {
                // Vereinfachter State für Heatmap (normalisierte Position + Dummy-Werte)
                double[] state = createStateForPosition(x, y);
                double[] qValues = network.predictQ(state);
                Q[x][y] = qValues.clone();
            }
        }
        
        return Q;
    }

    /**
     * Erstellt einen State-Vektor für eine bestimmte Position
     * WICHTIG: Muss identisch zu env.getExtendedState() sein für konsistente Vorhersagen!
     */
    private double[] createStateForPosition(int x, int y) {
        double maxDist = Math.sqrt(env.getWidth() * env.getWidth() + env.getHeight() * env.getHeight());
        
        // Distanzen berechnen
        double nearestExitDist = Double.MAX_VALUE;
        double nearestFireDist = Double.MAX_VALUE;
        
        for (int ex = 0; ex < env.getWidth(); ex++) {
            for (int ey = 0; ey < env.getHeight(); ey++) {
                int cellType = env.getCellType(ex, ey);
                double dist = Math.sqrt(Math.pow(x - ex, 2) + Math.pow(y - ey, 2));
                
                if (cellType == ForestConstants.EXIT) {
                    nearestExitDist = Math.min(nearestExitDist, dist);
                }
                if (cellType == ForestConstants.FIRE) {
                    nearestFireDist = Math.min(nearestFireDist, dist);
                }
            }
        }
        
        if (nearestExitDist == Double.MAX_VALUE) nearestExitDist = 0;
        if (nearestFireDist == Double.MAX_VALUE) nearestFireDist = maxDist;
        
        // Fire-Directions berechnen (wie in ForestEnvironment.getExtendedState)
        double fireUp = hasFireInDirection(x, y, 0, -1) ? 1.0 : 0.0;
        double fireDown = hasFireInDirection(x, y, 0, 1) ? 1.0 : 0.0;
        double fireLeft = hasFireInDirection(x, y, -1, 0) ? 1.0 : 0.0;
        double fireRight = hasFireInDirection(x, y, 1, 0) ? 1.0 : 0.0;
        
        return new double[]{
            x / (double) env.getWidth(),
            y / (double) env.getHeight(),
            nearestExitDist / maxDist,
            nearestFireDist / maxDist,
            fireUp, fireDown, fireLeft, fireRight
        };
    }
    
    /**
     * Prüft ob in einer bestimmten Richtung Feuer ist (nächste 3 Zellen)
     */
    private boolean hasFireInDirection(int x, int y, int dx, int dy) {
        for (int i = 1; i <= 3; i++) {
            int checkX = x + dx * i;
            int checkY = y + dy * i;
            
            if (checkX >= 0 && checkX < env.getWidth() && 
                checkY >= 0 && checkY < env.getHeight() &&
                env.getCellType(checkX, checkY) == ForestConstants.FIRE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Findet alle Exit-Positionen (für Heatmap)
     */
    private List<int[]> findAllExits() {
        List<int[]> exits = new ArrayList<>();
        for (int x = 0; x < env.getWidth(); x++) {
            for (int y = 0; y < env.getHeight(); y++) {
                if (env.getCellType(x, y) == ForestConstants.EXIT) {
                    exits.add(new int[]{x, y});
                }
            }
        }
        return exits;
    }

    /**
     * Bestimmt das Heatmap-Update-Intervall basierend auf der Episode
     * Frühe Episoden: häufigere Updates (alle 10)
     * Späte Episoden: seltenere Updates (alle 200)
     */
    private int getHeatmapInterval(int episode) {
        for (int i = 0; i < INTERVAL_THRESHOLDS.length; i++) {
            if (episode <= INTERVAL_THRESHOLDS[i]) {
                return HEATMAP_INTERVALS[i];
            }
        }
        return HEATMAP_INTERVALS[HEATMAP_INTERVALS.length - 1];
    }

    /**
     * Testet die gelernte Policy (greedy, ohne Exploration)
     */
    public void testGreedy() {
        env.reset();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("GREEDY TEST (Neural Network)");
        System.out.println("=".repeat(50));
        System.out.println("\nStartposition:");
        System.out.println(env.toAsciiString());
        
        int steps = 0;
        double totalReward = 0;
        
        while (!env.isTerminated() && steps < 50) {
            double[] state = env.getExtendedState();
            int action = argmaxQ(state);
            int[] positionBefore = env.getState();
            
            StepResult result = env.step(action);
            totalReward += result.reward();
            
            System.out.printf("Step %2d: %-6s von (%d,%d) → (%d,%d) | Reward: %+6.1f%n", 
                    ++steps, 
                    ForestConstants.ACTION_NAMES[action],
                    positionBefore[0], positionBefore[1],
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
     * Zeigt die Q-Werte für eine Position
     */
    public void printQValues(int x, int y) {
        double[] state = createStateForPosition(x, y);
        double[] qValues = network.predictQ(state);
        
        System.out.printf("\nQ-Werte (NN) für Position (%d, %d):%n", x, y);
        for (int a = 0; a < ForestConstants.NUM_ACTIONS; a++) {
            System.out.printf("  %6s: %+8.2f%n", ForestConstants.ACTION_NAMES[a], qValues[a]);
        }
    }

    // =====================================================
    //                      MAIN
    // =====================================================
    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   DEEP Q-LEARNING MIT NEURAL NETWORK             ║");
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
        QLearningForest_NN agent = new QLearningForest_NN(layout);
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

