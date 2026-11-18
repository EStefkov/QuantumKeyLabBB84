package ui;

import experiments.*;
import simulation.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;

/** Standalone Swing teaching laboratory. No external UI dependencies. */
public final class QuantumKeyLab extends JFrame {
    private static final Color INK = new Color(25, 41, 63), BLUE = new Color(37, 99, 235);
    private static final Color BG = new Color(243, 246, 250), GREEN = new Color(222, 245, 234);
    private static final Color RED = new Color(255, 226, 228), PURPLE = new Color(236, 229, 255);
    private final JTextField n = new JTextField("20"), eve = new JTextField("0.5");
    private final JTextField noise = new JTextField("0"), seed = new JTextField("42");
    private final JLabel status = new JLabel("Готово за симулация"), stageLabel = new JLabel("Изберете „Стъпка“ или „Изпълни“.");
    private final JLabel[] metrics = new JLabel[4];
    private final JTextArea explanation = new JTextArea(), log = new JTextArea();
    private final DefaultTableModel traceModel = model("№", "Alice бит", "Alice база", "Eve", "Eve база", "Eve бит", "Bob база", "Bob бит", "Позиция", "Грешка");
    private final DefaultTableModel batchModel = model("N", "f", "Шум", "Серии", "QBER %", "SD QBER", "Отказ %", "Засичане %", "Фалшив отказ %");
    private final JTable traceTable = new JTable(traceModel);
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<JButton> actions = new ArrayList<>();
    private final ExperimentChart chart = new ExperimentChart();
    private SimulationResult result;
    private SimulationConfig lastConfig;
    private List<ExperimentRunRecord> records = Collections.emptyList();
    private int stage;
    private boolean busy;

    public QuantumKeyLab() {
        super("Quantum Key Lab · BB84");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 760));
        setSize(1320, 860);
        setLocationRelativeTo(null);
        UIManager.put("Label.foreground", INK);
        JPanel shell = new JPanel(new BorderLayout(20, 18));
        shell.setBackground(BG); shell.setBorder(new EmptyBorder(24, 26, 18, 26));
        setContentPane(shell);
        JPanel heading = new JPanel(new BorderLayout()); heading.setOpaque(false);
        JLabel title = new JLabel("Quantum Key Lab"); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 29)); title.setForeground(INK);
        heading.add(title, BorderLayout.WEST);
        JLabel badge = new JLabel("BB84  /  УЧЕБНА СИМУЛАЦИЯ"); badge.setForeground(BLUE);
        heading.add(badge, BorderLayout.EAST); shell.add(heading, BorderLayout.NORTH);
        JPanel controls = new JPanel(); controls.setBackground(Color.WHITE);
        controls.setBorder(new EmptyBorder(18, 18, 18, 18)); controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setPreferredSize(new Dimension(245, 0));
        JLabel controlTitle = new JLabel("Настройки на канала"); controlTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        controls.add(controlTitle); controls.add(Box.createVerticalStrut(20));
        field(controls, "Брой сигнали · N (1–100 000)", n);
        field(controls, "Прихващане от Eve · f (0–1)", eve);
        field(controls, "Шум · noise (0–1)", noise);
        field(controls, "Seed · цяло число", seed);
        JLabel threshold = new JLabel("Праг за отказ: QBER > 8%"); threshold.setForeground(BLUE); controls.add(threshold);
        controls.add(Box.createVerticalStrut(16));
        controls.add(button("Изпълни симулация", () -> simulate(false), true));
        controls.add(Box.createVerticalStrut(8)); controls.add(button("Стъпка →", () -> simulate(true), false));
        controls.add(Box.createVerticalStrut(18));
        controls.add(button("Учебен пример · N=20", () -> preset(20, .5, 0, 42), false));
        controls.add(Box.createVerticalStrut(8)); controls.add(button("Граница · шум = 1", () -> preset(20, 0, 1, 42), false));
        controls.add(Box.createVerticalStrut(8)); controls.add(button("Граница · празна извадка", this::emptyDemo, false));
        controls.add(Box.createVerticalStrut(18));
        controls.add(button("Запази настройки", () -> configFile(false), false));
        controls.add(Box.createVerticalStrut(8)); controls.add(button("Зареди настройки", () -> configFile(true), false));
        controls.add(Box.createVerticalGlue());
        JLabel note = new JLabel("<html>Възпроизводим учебен режим.<br>Не генерира сигурен краен ключ.</html>"); note.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11)); controls.add(note);
        shell.add(controls, BorderLayout.WEST);
        JPanel center = new JPanel(new BorderLayout(0, 14)); center.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0)); cards.setOpaque(false);
        String[] names = {"ПРЕСЕТИ БИТОВЕ", "ТЕСТОВА ИЗВАДКА", "QBER", "КАНДИДАТ-МАТЕРИАЛ"};
        for (int i = 0; i < 4; i++) {
            JPanel card = new JPanel(new BorderLayout(0, 10)); card.setBackground(Color.WHITE); card.setBorder(new EmptyBorder(15, 14, 15, 14));
            JLabel label = new JLabel(names[i]); label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            metrics[i] = new JLabel("—"); metrics[i].setForeground(BLUE); metrics[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
            card.add(label, BorderLayout.NORTH); card.add(metrics[i], BorderLayout.CENTER); cards.add(card);
        }
        center.add(cards, BorderLayout.NORTH);
        tabs.addTab("  01  Учебен изглед  ", teaching());
        tabs.addTab("  02  Експерименти  ", experiments());
        tabs.addTab("  03  Проверки и помощ  ", help());
        center.add(tabs, BorderLayout.CENTER); shell.add(center, BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(2, 0, 0, 0)); shell.add(status, BorderLayout.SOUTH);
        for (JTextField f : inputFields()) {
            f.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidateRun(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidateRun(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidateRun(); }
            });
        }
    }
    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    }
    private void field(JPanel panel, String label, JTextField field) {
        JLabel l = new JLabel(label); l.setAlignmentX(0); panel.add(l); panel.add(Box.createVerticalStrut(5));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); field.setAlignmentX(0); panel.add(field); panel.add(Box.createVerticalStrut(14));
    }
    private JButton button(String title, Runnable action, boolean primary) {
        JButton b = new JButton(title); b.setAlignmentX(0); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (primary) { b.setBackground(BLUE); b.setForeground(Color.WHITE); b.setOpaque(true); b.setBorderPainted(false); }
        b.addActionListener(e -> { try { action.run(); } catch (Exception ex) { error(ex); } }); actions.add(b); return b;
    }
    private static void style(JTable table) {
        table.setRowHeight(29); table.setFillsViewportHeight(true); table.setShowVerticalLines(false);
        table.setGridColor(new Color(231, 235, 242)); table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    private JPanel teaching() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        stageLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15)); panel.add(stageLabel, BorderLayout.NORTH);
        style(traceTable); traceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) {
                    Color color = Color.WHITE;
                    if (result != null && row < result.getTrace().size()) {
                        BitTrace bit = result.getTrace().get(row);
                        if (stage >= 3 && bit.isBasesMatched()) color = GREEN;
                        if (stage >= 4 && bit.isSelectedForTest()) color = PURPLE;
                        if (stage >= 2 && bit.isError() && col == 7) color = RED;
                    }
                    c.setBackground(color); c.setForeground(INK);
                }
                setHorizontalAlignment(SwingConstants.CENTER); return c;
            }
        });
        panel.add(new JScrollPane(traceTable), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 8)); bottom.setOpaque(false);
        JLabel legend = new JLabel("Зелено: еднакви базиси     Лилаво: тестова позиция     Червено: различен бит при Bob"); legend.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11)); bottom.add(legend, BorderLayout.NORTH);
        explanation.setEditable(false); explanation.setLineWrap(true); explanation.setWrapStyleWord(true);
        explanation.setRows(5); explanation.setBackground(BG); explanation.setBorder(new EmptyBorder(10, 10, 10, 10)); bottom.add(new JScrollPane(explanation), BorderLayout.CENTER);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT)); tools.setOpaque(false);
        tools.add(button("Нова последователност", this::reset, false));
        tools.add(button("Експорт на резултат и trace", this::exportSingle, false));
        bottom.add(tools, BorderLayout.SOUTH); panel.add(bottom, BorderLayout.SOUTH); return panel;
    }
    private JPanel experiments() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(16,16,16,16));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); bar.setOpaque(false);
        bar.add(button("Стартирай 3 600 серии", this::batch, true));
        bar.add(button("Експорт на сериите", this::exportBatch, false));
        bar.add(new JLabel("36 комбинации × 100 повторения · 100 загряващи на комбинация")); panel.add(bar, BorderLayout.NORTH);
        JTable table = new JTable(batchModel); style(table); table.setAutoCreateRowSorter(true);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), chart); split.setResizeWeight(.52); split.setBorder(null);
        panel.add(split, BorderLayout.CENTER); return panel;
    }
    private JPanel help() {
        JPanel panel = new JPanel(new BorderLayout(0,12)); panel.setBorder(new EmptyBorder(16,16,16,16)); panel.setBackground(Color.WHITE);
        JTextArea text = new JTextArea("КАК ДА ИЗПОЛЗВАТЕ ЛАБОРАТОРИЯТА\n\n" +
            "1. Изберете N, вероятност за Eve, шум и seed. За учебната таблица използвайте N=20.\n" +
            "2. „Стъпка“ разкрива: изпращане → измерване → пресяване → извадка → оценка.\n" +
            "   Симулацията се изчислява веднъж; етапите разкриват един и същ trace.\n" +
            "3. „Изпълни симулация“ показва крайния резултат веднага.\n" +
            "4. Демонстрирайте шум=1 и празна извадка чрез готовите примери вляво.\n" +
            "5. В „Експерименти“ стартирайте всички 36 комбинации. Експортирайте новите данни\n" +
            "   в отделна папка, за да запазите старите измервания.\n\n" +
            "МОДЕЛ И ОГРАНИЧЕНИЯ\n" +
            "QBER = грешки в теста / тестови битове × 100. Прагът е фиксиран: >8% → отказ.\n" +
            "Тестът е ceil(20% от пресетите позиции). Тестовите битове се изключват.\n" +
            "Остатъчните грешки са контрол само за симулатора, не публична оценка на канала.\n" +
            "При отказ кандидат-материалът не се предоставя като използваем ключ.\n" +
            "Няма съгласуване на грешки, privacy amplification или автентичен класически канал.\n" +
            "Seed управлява java.util.Random само за възпроизводими учебни опити.\n\n" +
            "При експорт конфигурацията и средата се записват заедно с CSV.\n" +
            "За окончателно предаване синхронизирайте доклада и презентацията с избрания CSV.");
        text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true); text.setBackground(BG); text.setBorder(new EmptyBorder(12,12,12,12));
        panel.add(new JScrollPane(text), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0,8)); bottom.setOpaque(false);
        bottom.add(button("Изпълни автоматизираните тестове", this::tests, false), BorderLayout.NORTH);
        log.setEditable(false); log.setRows(7); bottom.add(new JScrollPane(log), BorderLayout.CENTER); panel.add(bottom, BorderLayout.SOUTH); return panel;
    }
    private JTextField[] inputFields() {
        return new JTextField[]{n, eve, noise, seed};
    }

    private void setBusy(boolean value) {
        busy = value;
        for (JButton button : actions) button.setEnabled(!value);
        for (JTextField field : inputFields()) field.setEnabled(!value);
    }

    private SimulationConfig config() {
        int count;
        try { count = Integer.parseInt(n.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("N трябва да е цяло число."); }
        if (count > 100000) throw new IllegalArgumentException("Интерфейсът допуска най-много 100 000 сигнала.");
        try { return new SimulationConfig(count, Double.parseDouble(eve.getText().trim().replace(',', '.')), Double.parseDouble(noise.getText().trim().replace(',', '.')), Long.parseLong(seed.getText().trim())); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Проверете f, noise и целочисления seed."); }
    }
    private void invalidateRun() { if (result != null) reset(); }
    private void reset() {
        result = null; lastConfig = null; stage = 0; traceModel.setRowCount(0);
        for (JLabel metric : metrics) metric.setText("—");
        stageLabel.setText("Изберете „Стъпка“ или „Изпълни“."); explanation.setText("");
        status.setText("Готово за нова симулация");
    }
    private void preset(int count, double f, double err, long s) {
        n.setText(String.valueOf(count)); eve.setText(String.valueOf(f)); noise.setText(String.valueOf(err)); seed.setText(String.valueOf(s)); reset(); tabs.setSelectedIndex(0); simulate(false);
    }
    private void emptyDemo() {
        for (long s=0; s<10000; s++) if (new BB84Simulator().run(new SimulationConfig(1,0,0,s)).getNSifted()==0) { preset(1,0,0,s); return; }
        throw new IllegalStateException("Не е намерен пример без съвпаднали базиси.");
    }
    private void simulate(boolean step) {
        if (busy) return;
        if (result != null) { stage = step ? Math.min(5,stage+1) : 5; render(); return; }
        SimulationConfig c = config();
        work("Симулиране на канала…", () -> new BB84Simulator().run(c), r -> {
            lastConfig=c; result=r; stage=step?1:5; tabs.setSelectedIndex(0); render();
        });
    }
    private void render() {
        String[] labels={"", "01 / Изпращане · Alice подготвя битове и базиси", "02 / Измерване · Eve и Bob", "03 / Пресяване · остават еднаквите базиси", "04 / Извадка · случайни 20%, закръглени нагоре", "05 / Оценка · QBER и кандидат-материал"};
        stageLabel.setText(labels[stage]); traceModel.setRowCount(0);
        // Keep the teaching table responsive; simulation and exports retain the complete trace.
        for (BitTrace b : result.getTrace().subList(0, Math.min(5000,result.getTrace().size()))) {
            String position = stage<3 ? "—" : !b.isBasesMatched()?"Отпада":stage<4?"Пресета":b.isSelectedForTest()?"Тест":"Кандидат";
            traceModel.addRow(new Object[]{b.getIndex(),b.getAliceBit(),b.getAliceBasis(),stage>=2?(b.isEveIntercepted()?"Прихваща":"—"):"…",stage>=2?value(b.getEveBasis()):"…",stage>=2?value(b.getEveBit()):"…",stage>=2?b.getBobBasis():"…",stage>=2?b.getBobBit():"…",position,stage>=2?(b.isError()?"Да":"Не"):"…"});
        }
        metrics[0].setText(stage>=3?String.valueOf(result.getNSifted()):"—");
        metrics[1].setText(stage>=4?String.valueOf(result.getNTest()):"—");
        metrics[2].setText(stage>=5?percent(result.getQberPercent()):"—");
        metrics[3].setText(stage>=5?String.valueOf(result.getCandidateBits()):"—");
        String info = "N="+lastConfig.getnSent()+" · f="+lastConfig.getEveProbability()+" · noise="+lastConfig.getNoiseProbability()+" · seed="+lastConfig.getSeed()+"\n";
        if(stage==1) info+="Alice избира независимо случаен бит и базис Z/X за всеки сигнал.";
        if(stage==2) info+="При еднакъв базис измерването връща подготвения бит; при различен е случайно. Eve измерва и изпраща ново състояние. Шумът може да обърне бита на Bob.";
        if(stage==3) info+="Запазват се само позициите с еднакви базиси на Alice и Bob. Дял след пресяване: "+percent(result.getSiftedPercent())+".";
        if(stage==4) info+="Лилавите позиции се сравняват публично и се изключват от оставащия материал.";
        if(stage==5) {
            long errors=result.getTrace().stream().filter(b->b.isCandidateBit()&&b.isError()).count();
            String decision="reject".equals(result.getDecision())?"ОТКАЗ — QBER надвишава 8%. Материалът не е използваем ключ.":"accept".equals(result.getDecision())?"ПРИЕТ — QBER не надвишава 8%. Това не доказва сигурност на ключа.":"НЕДОСТАТЪЧНИ ДАННИ — липсват пресети битове или тестова извадка.";
            info+=decision+"\nГрешки в теста: "+result.getTestErrors()+" / "+result.getNTest()+". Остатъчни грешки (контрол само за симулатора): "+errors+" / "+result.getCandidateBits()+".";
        }
        if(result.getTrace().size()>5000) info+="\nТаблицата показва първите 5000 позиции; експортът съдържа всички.";
        explanation.setText(info); explanation.setCaretPosition(0); status.setText(labels[stage]+" · seed="+lastConfig.getSeed());
    }
    private static Object value(Object x) { return x==null?"—":x; }
    private static String percent(double x) { return Double.isFinite(x)?String.format(Locale.US,"%.2f%%",x):"N/A"; }
    private void batch() {
        work("Изпълняват се 36 комбинации · загряване и 3600 измерени серии…", () -> {
            ExperimentRunner runner=new ExperimentRunner(); List<ExperimentSummary> summary=runner.runAll();
            return new BatchData(summary,runner.getRunRecords());
        }, data -> {
            records=data.runs; batchModel.setRowCount(0);
            for(ExperimentSummary s:data.summaries) batchModel.addRow(new Object[]{s.getNSent(),s.getEveProbability(),s.getNoiseProbability(),s.getRuns(),percent(s.getAverageQber()),percent(s.getStandardDeviation()),percent(s.getRejectRate()),percent(s.getDetectionRate()),percent(s.getFalseRejectionRate())});
            chart.setSummaries(data.summaries); status.setText("Завършени 3600 серии. Експортирайте данните в избрана папка.");
        });
    }
    private static final class BatchData {
        final List<ExperimentSummary> summaries; final List<ExperimentRunRecord> runs;
        BatchData(List<ExperimentSummary> s,List<ExperimentRunRecord> r){summaries=s;runs=r;}
    }
    private <T> void work(String message, Callable<T> task, java.util.function.Consumer<T> done) {
        setBusy(true); status.setText(message);
        new SwingWorker<T,Void>() {
            protected T doInBackground() throws Exception { return task.call(); }
            protected void done() {
                setBusy(false);
                try { T value=get(); status.setText("Готово"); done.accept(value); }
                catch(Exception ex){error(ex.getCause()==null?ex:ex.getCause());}
            }
        }.execute();
    }
    private Path directory() {
        JFileChooser chooser=new JFileChooser(); chooser.setDialogTitle("Изберете папка за експорт"); chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION?chooser.getSelectedFile().toPath():null;
    }
    private boolean overwrite(Path dir, String... files) {
        for(String name:files) if(Files.exists(dir.resolve(name))) return JOptionPane.showConfirmDialog(this,"В папката има съществуващи резултати. Да се заменят ли?","Експорт",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
        return true;
    }
    private void exportSingle() {
        if(result==null||stage<5) throw new IllegalStateException("Първо завършете симулацията до етап Оценка.");
        Path dir=directory(); if(dir==null||!overwrite(dir,"run_summary.csv","bit_trace.csv","bb84_config.properties","environment.txt"))return;
        SimulationResult snapshot=result; SimulationConfig c=lastConfig;
        work("Записване на резултата…",()->{
            Files.createDirectories(dir);
            new CsvExporter().exportRuns(Collections.singletonList(new ExperimentRunRecord(c.getnSent(),c.getEveProbability(),c.getNoiseProbability(),1,c.getSeed(),snapshot,0)),dir.resolve("run_summary.csv").toString());
            List<String> lines=new ArrayList<>(); lines.add("Index,AliceBit,AliceBasis,EveIntercepted,EveBasis,EveBit,BobBasis,BobBit,BasesMatched,Error,SelectedForTest,Candidate");
            for(BitTrace b:snapshot.getTrace()) lines.add(b.getIndex()+","+b.getAliceBit()+","+b.getAliceBasis()+","+b.isEveIntercepted()+","+(b.getEveBasis()==null?"N/A":b.getEveBasis())+","+(b.getEveBit()==null?"N/A":b.getEveBit())+","+b.getBobBasis()+","+b.getBobBit()+","+b.isBasesMatched()+","+b.isError()+","+b.isSelectedForTest()+","+b.isCandidateBit());
            Files.write(dir.resolve("bit_trace.csv"),lines,StandardCharsets.UTF_8); saveConfig(dir.resolve("bb84_config.properties"),c); environment(dir);
            return dir;
        },p->status.setText("Записани run_summary.csv, bit_trace.csv и настройки в "+p));
    }
    private void exportBatch() {
        if(records.isEmpty())throw new IllegalStateException("Първо изпълнете пакетните експерименти.");
        Path dir=directory(); if(dir==null||!overwrite(dir,"bb84_experiment_runs.csv","run_summary.csv","environment.txt","qber.png","rejection.png","sifting.png"))return;
        List<ExperimentRunRecord> snapshot=new ArrayList<>(records);
        java.awt.image.BufferedImage[] images=new java.awt.image.BufferedImage[3];
        for(int i=0;i<3;i++)images[i]=chart.renderImage(i);
        work("Експорт на серии, статистика и PNG графики…",()->{
            Files.createDirectories(dir); new CsvExporter().exportRuns(snapshot,dir.resolve("bb84_experiment_runs.csv").toString());
            SummaryExporter.write(snapshot,dir.resolve("run_summary.csv")); environment(dir);
            String[] names={"qber.png","rejection.png","sifting.png"};
            for(int i=0;i<3;i++)javax.imageio.ImageIO.write(images[i],"png",dir.resolve(names[i]).toFile());
            return dir;
        },p->status.setText("Записани 3600 серии, обобщение и 3 PNG графики в "+p));
    }
    private static void environment(Path dir)throws IOException {
        String text="OS: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"\nArchitecture: "+System.getProperty("os.arch")+"\nAvailable processors: "+Runtime.getRuntime().availableProcessors()+"\nJava: "+System.getProperty("java.version")+"\nJava vendor: "+System.getProperty("java.vendor")+"\nLibraries: JDK Swing (no third-party Java libraries)\nCPU model and physical RAM: record manually for the measurement machine\nMode: educational seeded java.util.Random\nThreshold: QBER > 8 percent\nTest fraction: ceil(0.2 * N_sifted)\nBatch timing: System.nanoTime, 100 warm-up calls per configuration, 100 measured calls\nSingle-run export: DurationNs=N/A (not benchmarked)\nTimestamp: "+java.time.Instant.now()+"\n";
        Files.writeString(dir.resolve("environment.txt"),text,StandardCharsets.UTF_8);
    }
    private static void saveConfig(Path path,SimulationConfig c)throws IOException {
        Properties p=new Properties();p.setProperty("N",String.valueOf(c.getnSent()));p.setProperty("f",String.valueOf(c.getEveProbability()));p.setProperty("noise",String.valueOf(c.getNoiseProbability()));p.setProperty("seed",String.valueOf(c.getSeed()));
        try(Writer w=Files.newBufferedWriter(path,StandardCharsets.UTF_8)){p.store(w,"BB84 educational simulation; fixed threshold 8%; test fraction 20%");}
    }
    private void configFile(boolean load) {
        JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new File("bb84_config.properties"));
        if((load?chooser.showOpenDialog(this):chooser.showSaveDialog(this))!=JFileChooser.APPROVE_OPTION)return;
        try {
            Path path=chooser.getSelectedFile().toPath();
            if(load){Properties p=new Properties();try(Reader r=Files.newBufferedReader(path,StandardCharsets.UTF_8)){p.load(r);}
                SimulationConfig c=new SimulationConfig(Integer.parseInt(p.getProperty("N")),Double.parseDouble(p.getProperty("f")),Double.parseDouble(p.getProperty("noise")),Long.parseLong(p.getProperty("seed")));
                if(c.getnSent()>100000)throw new IllegalArgumentException("N е над лимита на интерфейса.");
                n.setText(String.valueOf(c.getnSent()));eve.setText(String.valueOf(c.getEveProbability()));noise.setText(String.valueOf(c.getNoiseProbability()));seed.setText(String.valueOf(c.getSeed()));reset();
            }else{if(Files.exists(path)&&JOptionPane.showConfirmDialog(this,"Да заменя ли настройките?","Запис",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;saveConfig(path,config());}
            status.setText((load?"Заредени: ":"Записани: ")+path);
        }catch(Exception ex){error(ex);}
    }
    private void tests() {
        work("Изпълняване на автоматизираните проверки…",()->{
            Path temp=Files.createTempDirectory("bb84-tests-");
            String javaExecutable=Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").startsWith("Windows")?"java.exe":"java").toString();
            StringJoiner cp=new StringJoiner(File.pathSeparator);
            for(String entry:System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator)))cp.add(Path.of(entry).toAbsolutePath().toString());
            Process process=new ProcessBuilder(javaExecutable,"-cp",cp.toString(),"BB84SimulatorTest").directory(temp.toFile()).redirectErrorStream(true).start();
            String output=new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
            if(process.waitFor()!=0)throw new IllegalStateException(output);
            return output+"\nЖурнал: "+temp.resolve("test_journal.csv");
        },out->{log.setText(out);log.setCaretPosition(0);status.setText("Автоматизираните тестове преминаха успешно.");});
    }
    private void error(Throwable e) { status.setText("Грешка: "+e.getMessage()); JOptionPane.showMessageDialog(this,e.getMessage(),"Проверете входа",JOptionPane.ERROR_MESSAGE); }

}
