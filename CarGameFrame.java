import javax.swing.JFrame;
public class CarGameFrame extends JFrame {
    CarGameFrame(){
        setTitle("Car Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        CarGamePanel gamePanel = new CarGamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
}
}
