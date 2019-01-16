package server;

import java.util.LinkedList;
import java.util.List;

public class LoginList {
	private static List<String[]> entryList = new LinkedList<String[]>();

	public static void addEntry(String name, String password) {
		String[] entry = { name, password };
		entryList.add(entry);
	}

	public static String checkEntry(String name, String password) {
		for (String[] s : entryList) {
			if (s[0].equals(name)) {
				if (s[1].equals(password)) {
					return "Access granted";
				} else {
					return "Wrong password";
				}
			}
		}
		return "User not registered";
	}

}
