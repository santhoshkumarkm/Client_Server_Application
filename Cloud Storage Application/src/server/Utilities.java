package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Utilities {
	public static Scanner scan = new Scanner(System.in).useDelimiter("\n");
	private static FileInputStream fin;
	private static ObjectInputStream oin;
	private static FileOutputStream fout;
	private static ObjectOutputStream oout;

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

	public static String[] queryToMap(String query) throws UnsupportedEncodingException {
		query = query.substring(1);
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
			} else {
				result.put(URLDecoder.decode(pair[0], "UTF-8"), "");
			}
		}
		String[] user = new String[result.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : result.entrySet()) {
			user[i++] = entry.getValue();
		}
		return user;
	}

	public static Object readFile(File file) throws Exception {
		fin = new FileInputStream(file);
		oin = new ObjectInputStream(fin);
		Object object = oin.readObject();
		if (oin != null)
			oin.close();
		if (fin != null)
			fin.close();
		return object;
	}

	public static void writeFile(File file, Object object) throws Exception {
		fout = new FileOutputStream(file);
		oout = new ObjectOutputStream(fout);
		oout.writeObject(object);
		if (fout != null)
			fout.close();
		if (oout != null)
			oout.close();
	}

	public static String stringBuilder(BufferedReader bin) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String s = "";
		while ((s = bin.readLine()) != null) {
			stringBuilder.append(s + "\n");
		}
		return stringBuilder.toString();
	}
}