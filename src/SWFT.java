import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class SWFT {
	
	public static void main(String[] args) {
		if (args.length == 0) {
			java.util.Scanner scan = new java.util.Scanner(System.in);
			System.out.print("Type the name of the file that contains the pasted schedule: ");
			args = new String[] {scan.nextLine()};
			scan.close();
		}
		
		Schedule sched;
		try {
			sched = new Schedule(new File(args[0]));
		} catch (IOException e) {
			System.out.println("No file found: " + args[0]);
			return;
		}
		boolean[] outputs = new boolean[] {true, true};
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-noImg")) outputs[0] = false;
			if (args[i].equals("-noCal")) outputs[1] = false;
		}
		
		String outputName = args[0];
		if (Pattern.matches("^.+\\.[a-zA-Z]{3}$", outputName)) {
			outputName = outputName.substring(0, outputName.length()-4);
		}
		
		if (outputs[0]) {
			File f = new File(outputName + ".png");
			try {
				ImageIO.write(sched.getImage(), "PNG", f);
				System.out.println("Wrote image to " + outputName + ".png");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (outputs[1]) {
			String cal = IcalExport.exportClasses(sched);
			FileWriter fw;
			try {
				fw = new FileWriter(outputName + ".ics");
				fw.write(cal);
				fw.close();
				System.out.println("Wrote calendar to " + outputName + ".ics");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
