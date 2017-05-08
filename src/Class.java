import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;

public class Class {
	public String major, name, type, location, instructor, section;
	public int[] stime = new int[2], etime = new int[2];
	public int[] sdate = new int[3], edate = new int[3];
	public int number, classNumber;
	public boolean[] days = new boolean[5];
	public boolean enrolled;
	public float credits;
	public Color displayColor;
	public ArrayList<Class> subClasses;
	
	public Class() {
		subClasses = new ArrayList<Class>();
		displayColor = Color.getHSBColor((float)Math.random(), 1f, 1f);
	}
	
	public Class clone() {
		//note that subSections is not cloned
		Class tr = new Class();
		tr.major = major;
		tr.name = name;
		tr.type = type;
		tr.location = location;
		tr.instructor = instructor;
		tr.stime = stime;//be wary, array pointers
		tr.etime = etime;//be wary, array pointers
		tr.number = number;
		tr.section = section;
		tr.classNumber = classNumber;
		tr.days = days;
		tr.enrolled = enrolled;
		tr.credits = credits;
		tr.displayColor = displayColor;
		tr.sdate = sdate;//be wary, array pointers
		tr.edate = edate;//be wary, array pointers
		return tr;
	}
	public String toString() {
		String tr = "";
		tr = major + " " + number + " - " + name + "\n" + type + "\n" + location + "\n";
		tr += daysToString(new String[] {"Mo", "Tu", "We", "Th", "Fr"});
		tr += " " + stime[0] + ":" + stime[1] + " - " + etime[0] + ":" + etime[1];
		tr += "\n" + sdate[0] + "/" + sdate[1] + "/" + sdate[2] + " - " + edate[0] + "/" + edate[1] + "/" + edate[2];
		tr += "\n" + section + ", " + classNumber;
		return tr + "\n\n";
	}
	public String toIcalEvent() {
		String tr = "BEGIN:VEVENT\n"
				+ "DESCRIPTION:Instructor: " + instructor + "\\n" //the description becomes the notes section
					+ "Credits: " + credits + "\\n"
					+ "Section number: " + section + "\\n"
					+ "Class number: " + classNumber + "\\n"
					+ "\\nGenerated by Spire With Fewer Tears\n"; 
		
		//first occurrence of event - must find the first day the class occurs, not the first day of the semester
		Calendar fcal = Calendar.getInstance();
		fcal.set(sdate[2], sdate[0]-1, sdate[1]);//January is considered month 0, contrary to every other field.
		int day = fcal.get(Calendar.DAY_OF_WEEK);//Sunday is 1, Saturday is 7
		int sdateOffset = 0;
		for (int i = day-2; i < day-2 + 7; i++) {//convert day so it lines up with the 'days' boolean array
			//THIS WILL PRODUCE INCORRECT RESULTS IF THERE ARE WEEKEND CLASSES, I believe it will start the class 6 days after the first day of the semester
			if (i%7<5 && days[i%7]) {
				break;
			} else sdateOffset++;
		}
		fcal.set(Calendar.DATE, fcal.get(Calendar.DATE) + sdateOffset);//if this bleeds into the next month, the Calendar class will handle it
	
		int fYear = fcal.get(Calendar.YEAR), fMonth = fcal.get(Calendar.MONTH) + 1, fDate = fcal.get(Calendar.DATE);
		
		tr += "DTEND;TZID=" + IcalExport.timeZone + ":" + fYear + String.format("%02d", fMonth) + String.format("%02d", fDate) + "T" + String.format("%02d", etime[0]) + String.format("%02d", etime[1]) + "00\n";
		tr += "DTSTART;TZID=" + IcalExport.timeZone + ":" + fYear + String.format("%02d", fMonth) + String.format("%02d", fDate) + "T" + String.format("%02d", stime[0]) + String.format("%02d", stime[1]) + "00\n";
		
		tr += "LOCATION:" + location + "\n";
		//recurrence
		String days = daysToString(new String[] {"MO,", "TU,", "WE,", "TH,", "FR,"});
		days = days.substring(0, days.length()-1);//shave off final comma
		tr += "RRULE:FREQ=WEEKLY;UNTIL=" + edate[2] + String.format("%02d", edate[0]) + String.format("%02d", edate[1] + 1) + "T045959Z;BYDAY=" + days + "\n";
		//sequence could go here, set it to 0, I guess
		tr += "SUMMARY:" + major + " " + number + " -" + name + ", " + type + "\n";
		tr += "TRANSP:OPAQUE\n"; //this seems to indicate that the event is "busy" aka not free time
		String uidTail = major + "-" + number + name + "-" + type;
		uidTail = uidTail.replace(' ', '-');
		tr += "UID:SWFT-" + sdate[0]+"-"+sdate[1]+"-"+sdate[2] + "-" + uidTail + "\n";
		//date stamp
		Calendar cal = Calendar.getInstance();
		tr += "DTSTAMP:" + cal.get(Calendar.YEAR) + "" + String.format("%02d", cal.get(Calendar.MONTH) + 1) + "" + String.format("%02d", cal.get(Calendar.DATE)) + "T" + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE)) + String.format("%02d", cal.get(Calendar.SECOND)) + "\n";
		
		tr += "END:VEVENT";
		
		return tr;
	}
	/** 
	 * Converts the day array into a string based on the dayMap
	 * @param dayMap names of days, i.e. {"Mo", "Tu" ... }
	 * @return A string indicating the days, i.e. TuTh for a Tuesday, Thursday class
	 */
	private String daysToString(String[] dayMap) {
		String tr = "";
		for (int i = 0; i<5; i++) {
			if (days[i]) {
				switch(i) {
				case 0: tr += dayMap[0]; break;
				case 1: tr += dayMap[1]; break;
				case 2: tr += dayMap[2]; break;
				case 3: tr += dayMap[3]; break;
				case 4: tr += dayMap[4]; break;
				}
			}
		}
		return tr;
	}
	
	public static boolean[] parseDays(String s) {
		boolean[] tr = new boolean[5];
		while (s.length()>1) {
			switch(s.substring(0, 2)) {
			case "Mo": tr[0] = true; break;
			case "Tu": tr[1] = true; break;
			case "We": tr[2] = true; break;
			case "Th": tr[3] = true; break;
			case "Fr": tr[4] = true; break;
			}
			s = s.substring(2);
		}
		return tr;
	}
	public static int[] parseTime(String s) {
		int[] tr = new int[2];
		String[] time = s.split(":");
		tr[0] = Integer.parseInt(time[0]) + (time[1].substring(2).toUpperCase().equals("PM") && !time[0].equals("12") ? 12 : 0);
		tr[1] = Integer.parseInt(time[1].substring(0, 2));
		return tr;
	}

}
