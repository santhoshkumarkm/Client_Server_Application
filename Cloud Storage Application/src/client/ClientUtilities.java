package client;

import java.util.List;
import java.util.Scanner;

public class ClientUtilities {
	public static Scanner scan = new Scanner(System.in).useDelimiter("\n");

	public static String inputString(String name, String match, int minLength, int maxLength) {
		String string;
		while (true) {
			System.out.println("Enter " + name);
			if (scan.hasNext()) {
				string = scan.next();
				if (string.length() >= minLength && string.length() <= maxLength && string.matches(match)) {
					break;
				}
			}
			System.out.println("Invalid input");
		}
		return string;
	}

	public static int inputInt(String name, int min, int max) {
		int number;
		while (true) {
			System.out.println("Enter " + name);
			if (scan.hasNextInt()) {
				number = scan.nextInt();
				if (number >= min && number <= max)
					break;
				else
					System.out.println(name + " should be between " + min + " and " + max + ".");
			} else {
				scan.next();
				System.out.println("Invalid input.");
			}
		}
		return number;
	}

	public static int selectOption(List<String> list) {
		int selectedValue = 0;
		while (true) {
			System.out.println("-----------------------------------------------------------");
			System.out.println("Press any of the below number to begin");
			int index = 0;
			for (String a : list) {
				System.out.println((++index) + ". " + a);
			}
			System.out.println("-----------------------------------------------------------");
			if (scan.hasNextInt()) {
				selectedValue = scan.nextInt();
				if (selectedValue <= 0 || selectedValue > index) {
					System.out.println("Wrong input. Enter value correctly.");
					continue;
				} else {
					System.out.println("\"" + list.get(selectedValue - 1) + "\" selected.");
					System.out.println("-----------------------------------------------------------");
					break;
				}
			} else {
				scan.next();
				System.out.println("Wrong input. Enter value correctly.");
				continue;
			}
		}
		return selectedValue;
	}

}
