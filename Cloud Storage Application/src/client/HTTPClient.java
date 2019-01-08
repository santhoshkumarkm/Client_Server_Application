package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import server.Utilities;

public class HTTPClient {
	static CloseableHttpClient client = HttpClients.createDefault();
	final private static int NEW_FILE = 1, NEW_SUBFOLDER = 2;

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

	static void accessFolder(String name) throws ClientProtocolException, IOException {
		String defaultUri = "http://localhost:8500/access/name";

		System.out.println("Current path: " + "/" + name);
		List<String> list = new ArrayList<String>();
		list.add("Save New File");
		list.add("New SubFolder");
		int option = Utilities.selectOption(list);
		switch (option) {
		case NEW_FILE: {
//			String fileUrl = Utilities.inputString("file name with full path", ".*[.]txt", 1, 100);
			String fileUrl = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/san/test_file 1.txt";
			File file = new File(fileUrl);
			String fileName = Utilities.inputString("name for your file", ".*", 1, 20) + ".txt";
			String saveLocation = Utilities.inputString("save folder name", ".*", 1, 20);
			if (!saveLocation.equals(name)) {
				saveLocation = name +"//" + saveLocation;
			}
			HttpPost post = new HttpPost(defaultUri);
			BufferedReader bin = new BufferedReader(new FileReader(file));
			StringBuilder stringBuilder = new StringBuilder();
			String s = "";
			while ((s = bin.readLine()) != null) {
				stringBuilder.append(s + "\n");
			}
			bin.close();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("Location", saveLocation));
			nameValuePairs.add(new BasicNameValuePair(fileName, stringBuilder.toString()));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} else {
				System.out.println("Unexpected response status: " + status);
			}
			break;
		}
		case NEW_SUBFOLDER: {
			String folderName = Utilities.inputString("folder name", ".*", 1, 10);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("Location", name));
			nameValuePairs.add(new BasicNameValuePair(folderName, ""));
			HttpPost post = new HttpPost(defaultUri);
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					if (line.equals(""))
						accessFolder(name);
				}
			} else {
				System.out.println("Unexpected response status: " + status);
			}
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