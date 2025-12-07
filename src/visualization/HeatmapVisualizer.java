package visualization;

import javax.swing.*;
import java.awt.*;

public class HeatmapVisualizer extends JPanel {

    private int width, height;
    private int goalX, goalY;
    private double[][] maxQValues;

    private JFrame frame;

    public HeatmapVisualizer(int w, int h, int goalX, int goalY) {
        this.width = w;
        this.height = h;
        this.goalX = goalX;
        this.goalY = goalY;
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

    private double minQ = -10;  // feste Skala
    private double maxQ = 10;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = getWidth() / width;
        int cellHeight = getHeight() / height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float ratio = (float)((maxQValues[x][y] - minQ) / (maxQ - minQ));
                ratio = Math.max(0f, Math.min(1f, ratio)); // clamp
                g.setColor(getColor(ratio));
                g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                g.setColor(Color.BLACK);
                g.drawRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                String text = (x == goalX && y == goalY) ? "G" : String.format("%.1f", maxQValues[x][y]);
                g.setColor(Color.BLACK);
                g.drawString(text, x * cellWidth + cellWidth / 4, y * cellHeight + cellHeight / 2);
            }
        }
    }

    private Color getColor(float ratio) {
        // Clamp ratio zwischen 0 und 1
        ratio = Math.max(0f, Math.min(1f, ratio));
        // Blau Rot
        return new Color(ratio, 0f, 1f - ratio);
    }
}

