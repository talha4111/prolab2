package prolab.salespredict.gui;

import prolab.salespredict.core.*;
import prolab.salespredict.data.DataLoader;
import prolab.salespredict.evaluation.Evaluator;
import prolab.salespredict.evaluation.Evaluator.EvaluationResult;
import prolab.salespredict.evaluation.Evaluator.CategoryStat;
import prolab.salespredict.model.UserRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainFrame extends JFrame {
    private List<UserRecord> fullData, trainData, testData;
    
    // Arayüz Bileşenleri
    private JLabel lblFileStatus, lblDataSplitInfo, lblResAccuracy, lblResTime, lblResMemory;
    private JTextField txtRatio, txtParam;
    private JRadioButton rbtnKnn, rbtnDt;
    private JButton btnRun;
    
    private JTable tableResults;
    private DefaultTableModel tableModel;
    private GraphPanel graphPanel;

    private EvaluationResult currentResult = null;
    private EvaluationResult knnResult = null;
    private EvaluationResult dtResult = null;
    
    // Karşılaştırma Tablosu Bileşenleri
    private JLabel lblKnnAcc, lblKnnTime, lblKnnMem, lblDtAcc, lblDtTime, lblDtMem;

    public MainFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("ProLab 2 - Satış Tahmin ve Analiz Platformu");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= ÜST KONTROL PANELİ (Yatay Tasarım - Özgünlük için) =================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setBackground(new Color(240, 244, 248)); // Hafif mavimsi ferah arkaplan
        topPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.fill = GridBagConstraints.BOTH;
        gbcTop.insets = new Insets(0, 10, 0, 10);
        gbcTop.weighty = 1.0;

        // 1. Veri Kartı
        JPanel cardData = createCard("📂 Veri İşlemleri");
        cardData.setLayout(new GridLayout(2, 2, 5, 5));
        JButton btnLoadAll = createFlatButton("Tüm Veriyi Yükle", new Color(41, 128, 185));
        JButton btnLoadTrain = createFlatButton("Eğitim Yükle", new Color(39, 174, 96));
        JButton btnLoadTest = createFlatButton("Test Yükle", new Color(142, 68, 173));
        lblFileStatus = new JLabel("Hazır", SwingConstants.CENTER);
        lblFileStatus.setForeground(Color.DARK_GRAY);
        cardData.add(btnLoadAll); cardData.add(btnLoadTrain);
        cardData.add(btnLoadTest); cardData.add(lblFileStatus);

        // 2. Ayarlar Kartı
        JPanel cardSettings = createCard("⚙️ Konfigürasyon");
        cardSettings.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(2, 5, 2, 5);
        g.gridx=0; g.gridy=0; cardSettings.add(new JLabel("Eğitim (%):"), g);
        g.gridx=1; txtRatio = new JTextField("80", 3); cardSettings.add(txtRatio, g);
        
        g.gridx=2; g.gridy=0; g.gridheight=2; 
        rbtnKnn = new JRadioButton("KNN", true); rbtnKnn.setOpaque(false);
        rbtnDt = new JRadioButton("Karar Ağacı"); rbtnDt.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(rbtnKnn); bg.add(rbtnDt);
        JPanel pnlRadio = new JPanel(new GridLayout(2,1)); pnlRadio.setOpaque(false);
        pnlRadio.add(rbtnKnn); pnlRadio.add(rbtnDt);
        cardSettings.add(pnlRadio, g);

        g.gridheight=1; g.gridx=0; g.gridy=1; cardSettings.add(new JLabel("K/Derinlik:"), g);
        g.gridx=1; txtParam = new JTextField("5", 3); cardSettings.add(txtParam, g);

        // 3. Sonuç Kartı (Karşılaştırmalı Özet)
        JPanel cardResult = createCard("📊 Algoritma Kıyaslama");
        cardResult.setLayout(new GridLayout(3, 4, 5, 2));
        
        Font fHead = new Font("Segoe UI", Font.BOLD, 11);
        JLabel h1 = new JLabel("Model"); h1.setFont(fHead); h1.setForeground(Color.GRAY);
        JLabel h2 = new JLabel("Doğruluk"); h2.setFont(fHead); h2.setForeground(Color.GRAY);
        JLabel h3 = new JLabel("Süre"); h3.setFont(fHead); h3.setForeground(Color.GRAY);
        JLabel h4 = new JLabel("Bellek"); h4.setFont(fHead); h4.setForeground(Color.GRAY);
        
        lblKnnAcc = new JLabel("-"); lblKnnAcc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblKnnTime = new JLabel("-");
        lblKnnMem = new JLabel("-");
        lblDtAcc = new JLabel("-"); lblDtAcc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDtTime = new JLabel("-");
        lblDtMem = new JLabel("-");

        cardResult.add(h1); cardResult.add(h2); cardResult.add(h3); cardResult.add(h4);
        cardResult.add(new JLabel("KNN:")); cardResult.add(lblKnnAcc); cardResult.add(lblKnnTime); cardResult.add(lblKnnMem);
        cardResult.add(new JLabel("Karar A:")); cardResult.add(lblDtAcc); cardResult.add(lblDtTime); cardResult.add(lblDtMem);

        // Çalıştır Butonu
        btnRun = createFlatButton("ANALİZİ BAŞLAT", new Color(230, 126, 34)); // Turuncu
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRun.setPreferredSize(new Dimension(180, 0));

        gbcTop.gridx=0; gbcTop.weightx=0.25; topPanel.add(cardData, gbcTop);
        gbcTop.gridx=1; gbcTop.weightx=0.25; topPanel.add(cardSettings, gbcTop);
        gbcTop.gridx=2; gbcTop.weightx=0.25; topPanel.add(cardResult, gbcTop);
        gbcTop.gridx=3; gbcTop.weightx=0.15; topPanel.add(btnRun, gbcTop);

        add(topPanel, BorderLayout.NORTH);

        // ================= ALT PANEL (Tablo Solda, Grafik Sağda) =================
        String[] columns = {"Kategori", "Toplam", "Doğru", "Yanlış", "Başarı(%)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableResults = new JTable(tableModel);
        tableResults.setRowHeight(30);
        tableResults.setSelectionBackground(new Color(236, 240, 241));
        tableResults.setShowGrid(false);
        tableResults.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = tableResults.getTableHeader();
        header.setPreferredSize(new Dimension(0, 35)); // Başlık alanını biraz genişletelim
        
        // Başlıklar için özel ve net bir görünüm (Renderer)
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new Color(52, 73, 94)); // Koyu mavi-gri
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        };
        tableResults.getTableHeader().setDefaultRenderer(headerRenderer);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(new Color(44, 62, 80)); // Tablo metinlerini daha koyu/okunabilir yapalım
        for(int i = 0; i < 5; i++) tableResults.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scrollTable = new JScrollPane(tableResults);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBorder(createTitleBorder("Detaylı Döküm"));
        tableContainer.setBackground(new Color(250, 252, 255));
        tableContainer.add(scrollTable, BorderLayout.CENTER);

        graphPanel = new GraphPanel();
        JPanel graphContainer = new JPanel(new BorderLayout());
        graphContainer.setBorder(createTitleBorder("Kategorik Başarı Dağılımı (Bar Chart)"));
        graphContainer.setBackground(new Color(250, 252, 255));
        graphContainer.add(graphPanel, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableContainer, graphContainer);
        mainSplit.setDividerLocation(350);
        mainSplit.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainSplit.setContinuousLayout(true);
        mainSplit.setBackground(new Color(240, 244, 248));
        add(mainSplit, BorderLayout.CENTER);

        // ================= BUTON AKSİYONLARI =================
        btnLoadAll.addActionListener(e -> loadData("ALL", btnLoadAll));
        btnLoadTrain.addActionListener(e -> loadData("TRAIN", btnLoadTrain));
        btnLoadTest.addActionListener(e -> loadData("TEST", btnLoadTest));
        btnRun.addActionListener(e -> runModel());
    }

    private JPanel createCard(String title) {
        JPanel pnl = new JPanel();
        pnl.setBackground(new Color(250, 252, 255));
        pnl.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(223, 230, 233), 2, true),
            title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(45, 52, 54)
        ));
        return pnl;
    }

    private TitledBorder createTitleBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(223, 230, 233), 1), 
            title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(45, 52, 54)
        );
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Windows temasında buton renklerinin şeffaf olmasını engeller
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        
        return b;
    }

    private void loadData(String targetType, JButton sourceBtn) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File(System.getProperty("user.dir")));
        fc.setFileFilter(new FileNameExtensionFilter("Veri Dosyaları", "xlsx", "csv"));
        
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            String fName = fc.getSelectedFile().getName();
            
            sourceBtn.setEnabled(false);
            lblFileStatus.setText("Okunuyor...");
            
            SwingWorker<List<UserRecord>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<UserRecord> doInBackground() throws Exception {
                    return new DataLoader().loadData(path);
                }
                @Override
                protected void done() {
                    try {
                        List<UserRecord> res = get();
                        if (targetType.equals("ALL")) {
                            fullData = res; trainData = null; testData = null;
                        } else if (targetType.equals("TRAIN")) {
                            trainData = res; fullData = null;
                        } else {
                            testData = res; fullData = null;
                        }
                        lblFileStatus.setText(fName);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                        lblFileStatus.setText("Hata!");
                    } finally {
                        sourceBtn.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }

    private void runModel() {
        if (fullData == null && (trainData == null || testData == null)) {
            JOptionPane.showMessageDialog(this, "Lütfen veri yükleyin!"); 
            //System.out.println("veri boş");
            return;
        }
        
        double ratio = 0.8;
        int param = 5;
        try {
            ratio = Double.parseDouble(txtRatio.getText()) / 100.0;
            param = Integer.parseInt(txtParam.getText());
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Geçersiz parametreler!"); return;
        }
        
        if (fullData != null) {
            // hocanın yönergede bahsettiği gibi veriyi %80 eğitim %20 test diye ayırıyoruz
            List<UserRecord> shuf = new ArrayList<>(fullData);
            Collections.shuffle(shuf); // rastgele karıştır
            
            int idx = (int)(shuf.size() * ratio);
            
            trainData = shuf.subList(0, idx);
            testData = shuf.subList(idx, shuf.size());
            
            //System.out.println("Eğitim: " + trainData.size() + " Test: " + testData.size());
        }

        btnRun.setEnabled(false);
        
        final boolean isKnn = rbtnKnn.isSelected();
        if (isKnn) {
            lblKnnAcc.setText("...");
            lblKnnTime.setText("...");
            lblKnnMem.setText("...");
        } else {
            lblDtAcc.setText("...");
            lblDtTime.setText("...");
            lblDtMem.setText("...");
        }
        
        final int fParam = param;
        
        SwingWorker<EvaluationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected EvaluationResult doInBackground() throws Exception {
                IClassifier model = isKnn ? new KNNClassifier(fParam) : new DecisionTreeClassifier(fParam);
                String mName = isKnn ? "KNN" : "Karar Ağacı";
                return new Evaluator().evaluateModel(model, trainData, testData, mName);
            }
            
            @Override
            protected void done() {
                try {
                    currentResult = get();
                    if (isKnn) {
                        knnResult = currentResult;
                        lblKnnAcc.setText(String.format("%%%s", String.format("%.1f", knnResult.overallAccuracy)));
                        lblKnnTime.setText(knnResult.timeMs + "ms");
                        lblKnnMem.setText(knnResult.memoryKb + "KB");
                        lblKnnAcc.setForeground(new Color(52, 152, 219));
                    } else {
                        dtResult = currentResult;
                        lblDtAcc.setText(String.format("%%%s", String.format("%.1f", dtResult.overallAccuracy)));
                        lblDtTime.setText(dtResult.timeMs + "ms");
                        lblDtMem.setText(dtResult.memoryKb + "KB");
                        lblDtAcc.setForeground(new Color(231, 76, 60));
                    }
                    
                    tableModel.setRowCount(0);
                    for (CategoryStat s : currentResult.categoryStats) {
                        tableModel.addRow(new Object[]{
                            s.name, s.support, s.correct, s.incorrect, String.format("%%%.1f", s.getAccuracy())
                        });
                    }
                    graphPanel.repaint();
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Hata: " + ex.getMessage());
                } finally {
                    btnRun.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * Özgün ve estetik grafik paneli. Yazı kaymalarını önlemek için FontMetrics kullanılır
     * ve yazılar DİKEY eksende yerleştirilir. Köşeleri yuvarlatılmış barlar kullanılmıştır.
     */
    class GraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentResult == null || currentResult.categoryStats.isEmpty()) return;
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            // Modern Arkaplan (Çok hafif renkli kırık beyaz)
            g2d.setColor(new Color(250, 252, 255));
            g2d.fillRect(0, 0, w, h);
            
            // Renk Paleti (Yeşil-Kırmızı yerine Modern Mavi-Turuncu)
            Color colCorrect = new Color(52, 152, 219);  // Mavi
            Color colIncorrect = new Color(231, 76, 60); // Kırmızımsı Turuncu
            
            // Legend
            g2d.setColor(colCorrect);
            g2d.fillRoundRect(30, 20, 15, 15, 5, 5);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString("Doğru", 55, 33);
            
            g2d.setColor(colIncorrect);
            g2d.fillRoundRect(120, 20, 15, 15, 5, 5);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("Yanlış", 145, 33);
            
            List<CategoryStat> stats = currentResult.categoryStats;
            int maxVal = 1;
            for (CategoryStat s : stats) {
                if (s.correct > maxVal) maxVal = s.correct;
                if (s.incorrect > maxVal) maxVal = s.incorrect;
            }
            
            int paddingBottom = 120; // Yazılar için geniş alt boşluk
            int paddingTop = 80;
            int paddingSides = 30;
            int graphW = w - 2 * paddingSides;
            int graphH = h - paddingBottom - paddingTop;
            
            // Eksen çizgisi
            g2d.setColor(new Color(189, 195, 199));
            g2d.drawLine(paddingSides, h - paddingBottom, w - paddingSides, h - paddingBottom);
            
            int numCats = stats.size();
            int slotW = graphW / numCats; 
            int barW = Math.min(35, slotW / 3); 
            
            FontMetrics fm = g2d.getFontMetrics();

            for (int i = 0; i < numCats; i++) {
                CategoryStat s = stats.get(i);
                
                int centerX = paddingSides + (i * slotW) + (slotW / 2);
                int cx = centerX - barW - 2; 
                int ix = centerX + 2;        
                
                int ch = (int) (((double) s.correct / maxVal) * graphH);
                int ih = (int) (((double) s.incorrect / maxVal) * graphH);
                
                // Mavi (Doğru) Bar (Yuvarlak Köşeli)
                g2d.setColor(colCorrect);
                g2d.fill(new RoundRectangle2D.Double(cx, h - paddingBottom - ch, barW, ch, 8, 8));
                
                // Sayı metninin ortalanması
                g2d.setColor(new Color(44, 62, 80));
                String cTxt = String.valueOf(s.correct);
                int cTxtW = fm.stringWidth(cTxt);
                g2d.drawString(cTxt, cx + (barW - cTxtW)/2, h - paddingBottom - ch - 5);
                
                // Kırmızı (Yanlış) Bar (Yuvarlak Köşeli)
                g2d.setColor(colIncorrect);
                g2d.fill(new RoundRectangle2D.Double(ix, h - paddingBottom - ih, barW, ih, 8, 8));
                
                String iTxt = String.valueOf(s.incorrect);
                int iTxtW = fm.stringWidth(iTxt);
                g2d.drawString(iTxt, ix + (barW - iTxtW)/2, h - paddingBottom - ih - 5);
                
                // Kategori ismini DİKEY (-90 derece) yazdır, böylece aska birbirine girmez.
                g2d.setColor(Color.DARK_GRAY);
                Graphics2D gRot = (Graphics2D) g2d.create();
                gRot.translate(centerX, h - paddingBottom + 15);
                gRot.rotate(Math.toRadians(-90));
                // Yazıyı hizalamak için uzunluğunu bulalım
                int nameW = fm.stringWidth(s.name);
                gRot.drawString(s.name, -nameW, 5); // Aşağı doğru uzamasını sağla
                gRot.dispose();
            }
        }
    }
}