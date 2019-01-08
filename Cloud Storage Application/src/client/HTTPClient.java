package client;
import server.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

public class HTTPClient {
	static CloseableHttpClient client = HttpClients.createDefault();

	private static void login(String userState) {
		String name = Utilities.inputString("username", ".*", 1, 15);
		String password = Utilities.inputString("password", ".*", 1, 15);
		String defaultUri = "http://localhost:8500/login/?" + "name=" + name + "&password=" + password + "&user="
				+ userState;
		HttpGet httpGet = null;
		try {
//			URI uri = new URIBuilder().setScheme("http")
//		        .setHost("localhost")
//		        .setPath("/login")
//		        .setParameter("name", name)
//		        .setParameter("password", password)
//		        .build();
			httpGet = new HttpGet(defaultUri);
			HttpResponse response = client.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					if (line.equals("Access granted") || line.equals("Root folder created")) {
						accessFolder(name);
					}
				}
			} else {
				System.out.println("Unexpected response status: " + status);
			}
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
	}

	static void accessFolder(String name) {
		String defaultUri = "http://localhost:8500/access/name";
		List<String> list = new ArrayList<String>();
		list.add("New File");
		list.add("New SubFolder");
		int option = Utilities.selectOption(list);
		switch (option) {
		case 1: {
			
			break;
		}
		case 2: {

			break;
		}
		}
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("Login");
		list.add("Sign Up");
		int option = Utilities.selectOption(list);
		switch (option) {
		case 1: {
			login("existing");
			break;
		}
		case 2: {
			login("new");
			break;
		}
		}
	}

}