package server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Utilities {
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

	public static String[] queryToMap(String query) {
		query = query.substring(1);
//		Map<String, String> params = Utilities.queryToMap(uri.getQuery());
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		String[] user = new String[result.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : result.entrySet()) {
			user[i++] = entry.getValue();
		}
		return user;
	}
}