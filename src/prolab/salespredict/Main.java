package prolab.salespredict;

import prolab.salespredict.gui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    // Arayüzü güvenli bir şekilde (Thread-safe) başlat
    SwingUtilities.invokeLater(() -> {
      MainFrame frame = new MainFrame();
      frame.setLocationRelativeTo(null); // Pencereyi ekranın ortasında aç
      frame.setVisible(true);
    });
  }
}
