package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
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
	final private static int NEW_FILE = 1, NEW_SUBFOLDER = 2, OPEN_FILE = 3, CHANGE_DIRECTORY = 4,
			GO_BACK_DIRECTORY =5, LOG_OUT = 6;

	private static void login(String userState) {
		String name = Utilities.inputString("username", ".*", 1, 15);
		String password = Utilities.inputString("password", ".*", 1, 15);
		int hashPassword = password.hashCode();
		String defaultUri = "http://localhost:8500/login/?" + "name=" + name + "&password=" + hashPassword + "&user="
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

	static boolean handleResponse(HttpResponse response) throws UnsupportedOperationException, IOException {
		int status = response.getStatusLine().getStatusCode();
		String line = "";
		if (status >= 200 && status < 300) {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			while ((line = br.readLine()) != null) {
				System.out.println(URLDecoder.decode(line, "UTF-8"));
				if (line.equals("Folder present")) {
					return true;
				}
			}
		} else {
			System.out.println("Unexpected response status: " + status);
		}
		return false;
	}

	static boolean accessFolder(String name) throws ClientProtocolException, IOException {
		String defaultUri = "http://localhost:8500/access";
		List<String> list = new ArrayList<String>();
		list.add("Save New File");
		list.add("Create New SubFolder");
		list.add("Open File");
		list.add("Change current directory");
		list.add("Go back directory");
		list.add("Logout");
		boolean flag = false;
		while (true) {
			if (flag) {
				break;
			}
			System.out.println("-----------------------------------------------------------");
			System.out.println("Current directory: " + "/" + name);
			System.out.println("-----------------------------------------------------------");
			HttpPost initialPost = new HttpPost(defaultUri + "/" + name + "?name=" + name);
			HttpResponse response = client.execute(initialPost);
			handleResponse(response);

			int option = Utilities.selectOption(list);
			switch (option) {
			case NEW_FILE: {
				HttpPost post = new HttpPost(defaultUri + "/create/file");
				String fileUrl = Utilities.inputString("file name with full path", ".*[.]txt", 1, 100);
//				String fileUrl = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/test folder/test_file 1.txt";
				File file = new File(fileUrl);
				String fileName = Utilities.inputString("name for your file", ".*", 1, 20) + ".txt";
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
				nameValuePairs.add(new BasicNameValuePair("File location", name));
				nameValuePairs.add(new BasicNameValuePair("File name", fileName));
				nameValuePairs.add(new BasicNameValuePair("File content", stringBuilder.toString()));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
				break;
			}
			case NEW_SUBFOLDER: {
				HttpPost post = new HttpPost(defaultUri + "/create/folder");
				String folderName = Utilities.inputString("sub-folder name", ".*", 1, 10);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("File location", name));
				nameValuePairs.add(new BasicNameValuePair("File name", folderName));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
				break;
			}
//			case READ_FILE: {
//				String fileName = Utilities.inputString("file name (inc. extension)", ".*[.]txt", 1, 100);
//				String uri = defaultUri + "/read?" + "location=" + name + "&filename=" + fileName;
//				HttpPost post = new HttpPost(uri);
//				response = client.execute(post);
//				handleResponse(response);
//				break;
//			}
			case OPEN_FILE: {
				String fileName = Utilities.inputString("file name (inc. extension)", ".*[.]txt", 1, 100);
				String uri = defaultUri + "/read?" + "location=" + name + "&filename=" + fileName;
				HttpPost post = new HttpPost(uri);
				response = client.execute(post);
				int status = response.getStatusLine().getStatusCode();
				String line = "", paragraph = "";

				if (status >= 200 && status < 300) {
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					while ((line = br.readLine()) != null) {
						paragraph = paragraph + line + "\n";
					}
				} else {
					System.out.println("Unexpected response status: " + status);
				}
				

				TextEditor textEditor = new TextEditor(paragraph, fileName);
				textEditor.start();
				while (textEditor.getStatus()) {
					try {
						Thread.currentThread();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				String edited = textEditor.getEditedParagraph();
				if(textEditor.getEdit()) {
					uri = defaultUri + "/create/file/edit";
					post = new HttpPost(uri);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("File location", name));
					nameValuePairs.add(new BasicNameValuePair("File name", fileName));
					nameValuePairs.add(new BasicNameValuePair("File content", edited));
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					response = client.execute(post);
					handleResponse(response);					
				}
				break;
			}
			case CHANGE_DIRECTORY: {
				String folderName = Utilities.inputString("folder name", ".*", 1, 20);
				String uri = defaultUri + "/check?" + "location=" + name + "&subfolder=" + folderName;
				HttpPost post = new HttpPost(uri);
				response = client.execute(post);
				if (handleResponse(response)) {
					flag = accessFolder(name + "/" + folderName);
				} else {
					System.out.println("No such folder");
				}
				break;
			}
			case GO_BACK_DIRECTORY: {
				return false;
			}
			case LOG_OUT: {
				return true;
			}
			}
		}
		return true;
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
			case 2:
				login("new");
			}
		}
	}

}