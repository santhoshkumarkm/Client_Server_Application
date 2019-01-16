package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
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
	final private static int NEW_FILE = 1, NEW_SUBFOLDER = 2, READ_FILE = 3, LOGOUT = 4;

	private static void login(String userState) {
		String name = Utilities.inputString("username", ".*", 1, 15);
		String password = Utilities.inputString("password", ".*", 1, 15);
		Base64.Encoder encoder = Base64.getEncoder();
		password = encoder.encodeToString(password.getBytes());
		String defaultUri = "http://localhost:8500/login/?" + "name=" + name + "&password=" + password + "&user="
				+ userState;
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(defaultUri);
			HttpResponse response = client.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					if (line.contains("Access granted") || line.contains("Root folder created")) {
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
		String defaultUri = "http://localhost:8500/access";

		System.out.println("Current user: " + "/" + name);
		HttpPost initialPost = new HttpPost(defaultUri + "/" + name + "?name=" + name);
		HttpResponse response = client.execute(initialPost);
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

		List<String> list = new ArrayList<String>();
		list.add("Save New File");
		list.add("Create New SubFolder");
		list.add("Read File");
		list.add("Logout");
		outer: while (true) {
			System.out.println("-----------------------------------------------------------");
			int option = Utilities.selectOption(list);
			switch (option) {
			case NEW_FILE: {
				HttpPost post = new HttpPost(defaultUri + "/create/file");
//			String fileUrl = Utilities.inputString("file name with full path", ".*[.]txt", 1, 100);
				String fileUrl = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/test folder/test_file 1.txt";
				File file = new File(fileUrl);
				String fileName = Utilities.inputString("name for your file", ".*", 1, 20) + ".txt";
				String saveLocation = Utilities
						.inputString("save folder location (Enter \"root\" to save at root folder)", ".*", 1, 20);
				StringBuilder stringBuilder = new StringBuilder();
				if (file.exists()) {
					BufferedReader bin = new BufferedReader(new FileReader(file));
					String s = "";
					while ((s = bin.readLine()) != null) {
						stringBuilder.append(s + "\n");
					}
					bin.close();
				} else {
					System.out.println("File not exists.");
					break;
				}
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("user", name));
				nameValuePairs.add(new BasicNameValuePair("File Name", fileName));
				nameValuePairs.add(new BasicNameValuePair("File Content", stringBuilder.toString()));
				nameValuePairs.add(new BasicNameValuePair("Subfolder", saveLocation));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				status = response.getStatusLine().getStatusCode();
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
				HttpPost post = new HttpPost(defaultUri + "/create/folder");
				String folderName = Utilities.inputString("folder name", ".*", 1, 10);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("Location", name));
				nameValuePairs.add(new BasicNameValuePair("File Name", folderName));
//				HttpPost post = new HttpPost(defaultUri);
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				status = response.getStatusLine().getStatusCode();
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
			case READ_FILE: {
				String fileUrl = Utilities.inputString(
						"file name (with folder path for subfolder files(Eg: sub_folder/file.txt))", ".*[.]txt", 1,
						100);

				HttpPost post = new HttpPost(defaultUri + "/read?" + "username=" + name + "&subfolder="
						+ fileUrl.substring(0, fileUrl.indexOf('/')) + "&filename="
						+ fileUrl.substring(fileUrl.indexOf('/') + 1, fileUrl.length()));
//				HttpPost post = new HttpPost(defaultUri);
				response = client.execute(post);
				status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String line = "";
					while ((line = br.readLine()) != null) {
						System.out.println(URLDecoder.decode(line, "UTF-8"));
					}
				} else {
					System.out.println("Unexpected response status: " + status);
				}
				break;
			}
			case LOGOUT: {
				break outer;
			}
			}
		}
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("Login");
		list.add("Sign Up");
		while (true) {
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

}