package ui;

import experiments.ExperimentPlan;
import experiments.ExperimentSummary;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

/** Draws the on-screen and exported charts from the same statistical summaries. */
final class ExperimentChart extends JPanel {
    private static final Color INK = new Color(25, 41, 63);
    private static final Color GRID = new Color(228, 233, 240);
    private static final Color[] COLORS = {
            new Color(37, 99, 235), new Color(218, 119, 20), new Color(22, 153, 111)
    };
    private static final String[] TITLES = {
            "Среден QBER (%) спрямо f · праг 8% · 100 серии / комбинация",
            "Отказани серии (%) спрямо f · праг 8% · при f=0: фалшив отказ",
            "Среден дял пресети битове (%) спрямо f · очакване ≈50%"
    };
    private List<ExperimentSummary> summaries = Collections.emptyList();

    ExperimentChart() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 275));
    }

    void setSummaries(List<ExperimentSummary> values) {
        summaries = List.copyOf(values);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        draw((Graphics2D) graphics, getWidth(), getHeight(), 0);
    }

    BufferedImage renderImage(int type) {
        BufferedImage image = new BufferedImage(1500, 520, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            draw(graphics, image.getWidth(), image.getHeight(), type);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void draw(Graphics2D original, int width, int height, int type) {
        Graphics2D graphics = (Graphics2D) original.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(INK);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            graphics.drawString(TITLES[type], 20, 23);
            if (summaries.isEmpty()) {
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                graphics.drawString("Стартирайте експериментите, за да видите графиката.", 20, 60);
                return;
            }
            int[] sizes = ExperimentPlan.sizes();
            int panelWidth = width / sizes.length;
            for (int panel = 0; panel < sizes.length; panel++) {
                drawPanel(graphics, panel * panelWidth + 52, (panel + 1) * panelWidth - 20,
                        height, sizes[panel], type);
            }
        } finally {
            graphics.dispose();
        }
    }

    private void drawPanel(Graphics2D graphics, int left, int right, int height, int n, int type) {
        int top = 65;
        int bottom = height - 70;
        double maximum = type == 0 ? 40 : 100;
        graphics.setStroke(new BasicStroke(1));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        graphics.setColor(INK);
        graphics.drawString("N = " + n, left, 47);
        for (int tick = 0; tick <= 4; tick++) {
            int y = bottom - (bottom - top) * tick / 4;
            graphics.setColor(GRID);
            graphics.drawLine(left, y, right, y);
            graphics.setColor(INK);
            graphics.drawString(String.valueOf((int) (maximum * tick / 4)), left - 30, y + 4);
        }
        graphics.drawLine(left, top, left, bottom);
        graphics.drawLine(left, bottom, right, bottom);
        for (double f : ExperimentPlan.eveProbabilities()) {
            int x = left + (int) ((right - left) * f);
            graphics.drawString(String.valueOf(f), x - 8, bottom + 18);
        }
        graphics.drawString("f · вероятност за прихващане", left, bottom + 37);
        if (type == 0) {
            int y = bottom - (int) ((bottom - top) * 8 / maximum);
            graphics.setColor(new Color(150, 155, 165));
            graphics.drawLine(left, y, right, y);
        }
        double[] noises = ExperimentPlan.noiseProbabilities();
        for (int line = 0; line < noises.length; line++) {
            graphics.setColor(COLORS[line]);
            graphics.setStroke(new BasicStroke(2));
            int previousX = -1;
            int previousY = -1;
            for (ExperimentSummary summary : summaries) {
                if (summary.getNSent() != n || summary.getNoiseProbability() != noises[line]) continue;
                double value = metric(summary, type);
                if (!Double.isFinite(value)) continue;
                int x = left + (int) ((right - left) * summary.getEveProbability());
                int y = bottom - (int) ((bottom - top) * value / maximum);
                if (previousX >= 0) graphics.drawLine(previousX, previousY, x, y);
                graphics.fillOval(x - 3, y - 3, 6, 6);
                previousX = x;
                previousY = y;
            }
            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            graphics.drawString("noise=" + noises[line], left + line * (right - left) / noises.length,
                    height - 10);
        }
    }

    private static double metric(ExperimentSummary summary, int type) {
        if (type == 0) return summary.getAverageQber();
        if (type == 1) return summary.getRejectRate();
        return summary.getMeanSiftedPercent();
    }
}
