package visualization;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HeatmapVisualizer extends JPanel {

    protected int width, height;
    protected List<int[]> exitPositions;  // Liste aller Exit-Positionen
    protected double[][] maxQValues;

    protected JFrame frame;

    /**
     * Konstruktor mit einzelnem Goal (für Rückwärtskompatibilität)
     */
    public HeatmapVisualizer(int w, int h, int goalX, int goalY) {
        this(w, h);
        this.exitPositions.add(new int[]{goalX, goalY});
    }

    /**
     * Konstruktor mit Liste von Exit-Positionen
     */
    public HeatmapVisualizer(int w, int h, List<int[]> exits) {
        this(w, h);
        this.exitPositions.addAll(exits);
    }

    /**
     * Basis-Konstruktor
     */
    protected HeatmapVisualizer(int w, int h) {
        this.width = w;
        this.height = h;
        this.exitPositions = new ArrayList<>();
        this.maxQValues = new double[w][h];

        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Q-Learning Heatmap");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(this);
            frame.setSize(500, 500);
            frame.setVisible(true);
        });
    }

    // Q-Werte aktualisieren und Fenster repainten
    public void update(double[][][] Q) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double best = Q[x][y][0];
                for (int a = 1; a < Q[x][y].length; a++) best = Math.max(best, Q[x][y][a]);
                maxQValues[x][y] = best;
            }
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    protected double minQ = -10;  // feste Skala
    protected double maxQ = 10;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = getWidth() / width;
        int cellHeight = getHeight() / height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float ratio = (float)((maxQValues[x][y] - minQ) / (maxQ - minQ));
                ratio = Math.max(0f, Math.min(1f, ratio)); // clamp
                
                // Exits grün einfärben
                if (isExit(x, y)) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(getColor(ratio));
                }
                g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                g.setColor(Color.BLACK);
                g.drawRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                // Text: "E" für alle Exits, sonst Q-Wert
                String text = isExit(x, y) ? "E" : String.format("%.1f", maxQValues[x][y]);
                g.setColor(Color.BLACK);
                g.drawString(text, x * cellWidth + cellWidth / 4, y * cellHeight + cellHeight / 2);
            }
        }
    }

    /**
     * Prüft ob eine Position ein Exit ist
     */
    protected boolean isExit(int x, int y) {
        for (int[] exit : exitPositions) {
            if (exit[0] == x && exit[1] == y) {
                return true;
            }
        }
        return false;
    }

    protected Color getColor(float ratio) {
        // Clamp ratio zwischen 0 und 1
        ratio = Math.max(0f, Math.min(1f, ratio));
        // Blau Rot
        return new Color(ratio, 0f, 1f - ratio);
    }
}

