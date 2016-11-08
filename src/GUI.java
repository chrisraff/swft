import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//this thing is useless
public class GUI extends JPanel {
	private static final long serialVersionUID = 1L;
	public BufferedImage image;
	
	public GUI(BufferedImage image) {
		DisplayWindow window = new DisplayWindow();
		this.image = image;
		window.addPanel(this);
		window.setPreferredSize(new Dimension(image.getWidth(), image.getHeight())); //not working
		window.showFrame();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}
	
//	public static void main(String[] args) {
//		
//		
//		GUI gui = new GUI(output);
//	}
}
