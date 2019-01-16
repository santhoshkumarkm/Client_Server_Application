package server;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginList {
	private static Map<String, String> entryList = new LinkedHashMap<String, String>();

	public static void addEntry(String name, String password) {
		entryList.put(name, password);
	}

	public static String checkEntry(String name, String password) {
		for (Map.Entry<String, String> entry : entryList.entrySet()) {
			if (entry.getKey().equals(name)) {
				if (entry.getValue().equals(password))
					return "Access granted";
				else
					return "Wrong password";
			}
		}
		return "User not registered";
	}

}
