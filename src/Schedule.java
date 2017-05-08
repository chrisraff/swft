import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Schedule {
	ArrayList<Class> classes = new ArrayList<Class>();
	private int numParentClasses;
	
	public Schedule(File rawSchedule) throws IOException {
		Scanner scan = new Scanner(rawSchedule);
		if(!scan.hasNextLine()) {
			System.out.println("File is empty!");
			scan.close();
			return;
		}
		String carryOver = scan.next();
		numParentClasses = 0;
		
		while (scan.hasNext()) {
			Class nc = new Class();
			nc.major = carryOver; //System.out.println("Debug: " + carryOver);
			nc.number = scan.next();
			scan.next();// "-"
			nc.name = scan.nextLine();
			scan.nextLine();//header
			nc.enrolled = scan.nextLine().equals("Enrolled");
			nc.credits = scan.nextFloat();
			scan.nextLine();//fix cursor
			scan.nextLine();//header
			scan.nextLine();//empty line
			scan.nextLine();//header
			scan.nextLine();//header
			carryOver = scan.next();
			
			Class parentClass = null;
			ArrayList<Class> unParented = new ArrayList<Class>();
			while (isNumber(carryOver)) {
				Class toAdd = nc.clone();
				//beware of credit stacking due to lectures and discussions having the same credit count
				
				toAdd.classNumber = carryOver;
				scan.nextLine();//fix cursor
				toAdd.section = scan.nextLine();//section number 01 02 ... and nonsense for discussions
				toAdd.type = scan.nextLine();
				toAdd.days = Class.parseDays(scan.next());
				toAdd.stime = Class.parseTime(scan.next());
				scan.next(); // " - "
				toAdd.etime = Class.parseTime(scan.next());
				scan.nextLine();//cursor alignment
				toAdd.location = scan.nextLine();
				toAdd.instructor = scan.nextLine();
				//If a class has multiple instructors, they will each be on their own line and all (hopefully) except the last will have a comma and space at the end.
				while (toAdd.instructor.endsWith(", ")) {
					toAdd.instructor += scan.nextLine();
				}
				String dates = scan.nextLine();//start date to end date
				toAdd.sdate[0] = Integer.parseInt(dates.substring(0, 2));
				toAdd.sdate[1] = Integer.parseInt(dates.substring(3, 5));
				toAdd.sdate[2] = Integer.parseInt(dates.substring(6, 10));
				toAdd.edate[0] = Integer.parseInt(dates.substring(13, 15));
				toAdd.edate[1] = Integer.parseInt(dates.substring(16, 18));
				toAdd.edate[2] = Integer.parseInt(dates.substring(19, 23));
				
				//handle the organization of classes into parent or sub classes
				if (toAdd.type.equals("Lecture")) {
					parentClass = toAdd;
					
					for (Class c: unParented)
						parentClass.subClasses.add(c);
					numParentClasses++;
					
				} else if (parentClass != null) {
					parentClass.subClasses.add(toAdd);
				} else {
					unParented.add(toAdd); //this is for situations where other sections (like laboratories) are listed before the lecture
				}
				
				classes.add(toAdd);
				
				System.out.println(toAdd);//print the added class to console for debugging purposes
				
				//skip optional trailing information that is irrelevant (only known example so far is "URL")
				while (scan.hasNext()) {
					carryOver = scan.next();
					// Sometimes there are extra information entries trailing after "Start/End date".
					// I suppose I will have to catch them on a case by case basis for now 
					if (!carryOver.equals("URL"))
						break;
				}
				if (!scan.hasNext()) {
					carryOver = "I'm not a number"; //this should never matter because scan.hasNext() will be false and the parse while loop will end
				}
			}
		}
		scan.close();
	}
		
	public BufferedImage getImage() {
		//set colors so they're evenly spaced between parent classes
		float hue = (float)Math.random();
		for (Class c: classes) {
			if(c.type.equals("Lecture")) {
				c.displayColor = Color.getHSBColor(hue, 1, 1);
				for (Class sc: c.subClasses)
					sc.displayColor = Color.getHSBColor(hue, .75f, 1);
				hue = (hue + (1f/((float)numParentClasses))) % 1f;
			}
		}
		
		//System.out.println(classes);
		
		int gX = 25, gY = 10, gsX = 200, gsY = 50, h = 12; //set dimensions for grid. h is the number of hours to show
		BufferedImage output = new BufferedImage(2*gX + gsX*5, 2*gY + h*gsY + 30, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g = (Graphics2D) output.getGraphics();
		Font header = new Font("Calibri", Font.BOLD, 16), location = new Font("Calibri", Font.PLAIN, 12);
		g.setFont(header);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.fillRect(0, 0, output.getWidth(), output.getHeight());
		
		//begin drawing grid
		g.setColor(Color.gray);
		for (int i = 1; i < 5; i++) {
			g.drawLine(gX + i*gsX, gY, gX + i*gsX, gY + h*gsY);
		}
		for (int i = 0; i <= h; i++) {
			g.drawLine(gX, gY + gsY*i, gX + 5*gsX, gY + gsY*i);
			if (i%2 == 0)
				g.drawString(i + 8 + "", gX/2 - 5, gY + gsY*i + 5);
		}
		g.setColor(Color.black);
		g.drawRect(gX, gY, 5*gsX, h*gsY);
		//finished drawing grid
		
		for (Class c: classes) {
			g.setColor(c.displayColor);
			float stime = (float)(c.stime[0]) + (float)(c.stime[1])/60;
			float dur = (float)(c.etime[0]) + (float)(c.etime[1])/60 - stime;
			for (int i = 0; i < c.days.length; i++) {
				if (c.days[i]) {
					g.fillRect(gX + i*gsX + 1, gY + (int)(stime*gsY - 8*gsY), gsX - 1, (int)(dur*gsY));
					Color swap = g.getColor();
					//inverted -> g.setColor(new Color(255-swap.getRed(), 255-swap.getGreen(), 255-swap.getBlue()));
					
					//shadow text
					int sOffX = 1, sOffY = 1;
					g.translate(sOffX, sOffY);
					g.setColor(Color.darkGray);
					g.drawString(c.major + " " + c.number, gX + gsX/30 + gsX*i, gY + gsY/10 +10 + (int)(stime*gsY - 8*gsY));
					g.setFont(location);
					g.drawString(c.location, gX + gsX/30 + gsX*i, gY + gsY/10 +10 +10 + (int)(stime*gsY - 8*gsY));
					g.drawString(c.stime[0] + ":" + String.format("%02d", c.stime[1]) + " - " + c.etime[0] + ":" + String.format("%02d", c.etime[1]), gX + gsX/30 + gsX*i, gY + gsY/10 +10 +10*2 + (int)(stime*gsY - 8*gsY));
					g.setFont(header);
					g.setColor(swap);
					g.translate(-sOffX, -sOffY);
					
					g.setColor(Color.white);
					g.drawString(c.major + " " + c.number, gX + gsX/30 + gsX*i, gY + gsY/10 +10 + (int)(stime*gsY - 8*gsY));
					g.setFont(location);
					g.drawString(c.location, gX + gsX/30 + gsX*i, gY + gsY/10 +10 +10 + (int)(stime*gsY - 8*gsY));
					g.drawString(c.stime[0] + ":" + String.format("%02d", c.stime[1]) + " - " + c.etime[0] + ":" + String.format("%02d", c.etime[1]), gX + gsX/30 + gsX*i, gY + gsY/10 +10 +10*2 + (int)(stime*gsY - 8*gsY));
					g.setFont(header);
					g.setColor(swap);
				}
			}
		}
		
		g.setColor(Color.gray);
		g.drawString("Spire With Fewer Tears", gX, (int)(gY*1.5) + h*gsY + 15);
		
		return output;
	}
		
	private static boolean isNumber(String s) {
		try {
			Integer.parseInt(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Class> getClasses() {
		return (ArrayList<Class>)classes.clone();
	}
}
