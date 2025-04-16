import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        SwingUtilities.invokeLater(() -> new GUI(bank));
    }
}
