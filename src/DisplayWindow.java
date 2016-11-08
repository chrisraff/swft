import java.awt.*;
import javax.swing.*;
public  class DisplayWindow extends JFrame{
	private static final long serialVersionUID = 1L;
private Container c;

  public DisplayWindow(){
    super("Spire With Fewer Tears");
    c = this.getContentPane();
  }

  public void addPanel(JPanel p){
    p.setPreferredSize(new Dimension(800,600));
    c.add(p);
  }

  public void showFrame(){
    this.pack();
    this.setVisible(true);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}