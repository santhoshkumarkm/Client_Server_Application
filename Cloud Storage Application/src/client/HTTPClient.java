package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HTTPClient {
	static CloseableHttpClient client;

	private static void login(String userState) {
		String name = ClientUtilities.inputString("username", ".*", 1, 20);
		String password = ClientUtilities.inputString("password", ".*", 1, 20);
		int hashPassword = password.hashCode();
		String defaultUri = "http://localhost:8500/login/?" + "name=" + name + "&password=" + hashPassword + "&user="
				+ userState;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(defaultUri);
			HttpResponse response = client.execute(httpPost);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					if (line.contains("Access granted") || line.contains("Root folder created")) {
						accessFolder(name, true, true);
						userState = "logout";
						defaultUri = "http://localhost:8500/login/?" + "name=" + name + "&password=" + hashPassword
								+ "&user=" + userState;
						HttpPost httpPost2 = new HttpPost(defaultUri);
						response = client.execute(httpPost2);
						status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {
							BufferedReader br2 = new BufferedReader(
									new InputStreamReader(response.getEntity().getContent()));
							line = "";
							while ((line = br2.readLine()) != null) {
								System.out.println(line);
							}
						} else {
							System.out.println("Logout unsuccessful");
						}
					}
				}
			} else {
				System.out.println("Unexpected response status: " + status);
			}
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			httpPost.releaseConnection();
		}
	}

	static boolean accessFolder(String name, boolean owner, boolean write) throws ClientProtocolException, IOException {
		String defaultUri = "http://localhost:8500/access";
		String exitOption;
		if (owner) {
			exitOption = "Logout";
		} else {
			exitOption = "Exit accessed folder";
		}
		List<String> list = new ArrayList<String>();
		if (write) {
			list.add("Upload File");
			list.add("New File");
			list.add("New SubFolder");
			list.add("Delete file/folder");
		}
		list.add("Open File");
		list.add("Open Folder");
		if (owner) {
			list.add("Share file/folder with other users");
			list.add("Access shared files by other users");
			list.add("View files & folders I have shared");
			list.add("Remove Share access");
			list.add("Find");
		}
		list.add("Go back directory");
		list.add(exitOption);
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

			int option = ClientUtilities.selectOption(list);
//			case UPLOAD_FILE: {
			if (write && option == 1) {
				HttpPost post = new HttpPost(defaultUri + "/create/file");
				String fileUrl = ClientUtilities.inputString("file name with full path", ".*[.]txt", 1, 1000);
//				String fileUrl = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/test folder/test_file 1.txt";
				File file = new File(fileUrl);
				String fileName = ClientUtilities.inputString("name for your file", ".*", 1, 255) + ".txt";
				StringBuilder stringBuilder = new StringBuilder();
				if (file.exists()) {
					BufferedReader bin = new BufferedReader(new FileReader(file));
					String s = "";
					while ((s = bin.readLine()) != null) {
						stringBuilder.append(s + "\n");
					}
					bin.close();
				} else {
					System.out.println("File not exists");
					continue;
				}
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("File location", name));
				nameValuePairs.add(new BasicNameValuePair("File name", fileName));
				nameValuePairs.add(new BasicNameValuePair("File content", stringBuilder.toString()));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
			}
//			case NEW_FILE: {
			else if (write && option == 2) {
				String uri = defaultUri + "/create/new/file";
				HttpPost post = new HttpPost(uri);
				String fileName = ClientUtilities.inputString("name for your file", ".*", 1, 255);
				fileName = fileName + ".txt";
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("File location", name));
				nameValuePairs.add(new BasicNameValuePair("File name", fileName));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				if (handleResponse(response)) {
					editor("NEW_EDIT_MODE", "", name, fileName, uri, defaultUri, post, response);
				}
			}
//			case NEW_SUBFOLDER: {
			else if (write && option == 3) {
				String uri = defaultUri + "/create/folder";
				HttpPost post = new HttpPost(uri);
				String folderName = ClientUtilities.inputString("sub-folder name", ".*", 1, 255);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("File location", name));
				nameValuePairs.add(new BasicNameValuePair("File name", folderName));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
			}
//			case DELETE: {
			else if (write && option == 4) {
				String fName = ClientUtilities.inputString("file/folder name (inc. extension)", ".*", 1, 260);
				String uri = defaultUri + "/check/delete?" + "location=" + name + "&subfolder=" + fName;
				HttpPost post = new HttpPost(uri);
				response = client.execute(post);
				handleResponse(response);
			}
//			case OPEN_FILE: {
			else if ((write && option == 5) || (!write && option == 1)) {
				String fileName = ClientUtilities.inputString("file name", ".*", 1, 255);
				fileName = fileName + ".txt";
				if (!write) {
					openFile(fileName, name, false);
				} else {
					openFile(fileName, name, true);
				}
			}
//			case CHANGE_DIRECTORY: {
			else if ((write && option == 6) || (!write && option == 2)) {
				String folderName = ClientUtilities.inputString("folder name", ".*", 1, 255);
				String uri = defaultUri + "/check?" + "location=" + name + "&subfolder=" + folderName;
				HttpPost post = new HttpPost(uri);
				response = client.execute(post);
				if (handleResponse(response)) {
					flag = accessFolder(name + "/" + folderName, owner, write);
				} else {
					System.out.println("No such folder");
				}
			}
//			case SHARE_FILE: {
			else if (owner && option == 7) {
				String fName = ClientUtilities.inputString("file/folder name inc. extension", ".*", 1, 260);
				String uri = "http://localhost:8500/previlege/sharefile?" + "location=" + name + "&subfolder=" + fName;
				HttpPost post = new HttpPost(uri);
				@SuppressWarnings("resource")
				Scanner scan = new Scanner(System.in);
				System.out
						.println("Enter user names to whom you want to share this file/folder\nEnter \"stop\" to end");
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				String sharedUserName, privilege;
				while (true) {
					System.out.println("Enter username...");
					sharedUserName = scan.next();
					if (sharedUserName.equalsIgnoreCase("stop")) {
						break;
					}
					privilege = ClientUtilities.inputString("privilage (\"read\" or \"write\")...", "(read|write)", 4,
							5);
					nameValuePairs.add(new BasicNameValuePair(sharedUserName, privilege));
				}
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
			}
//			case VIEW_SHARED_FILES: {
			else if (owner && option == 8) {
				String userName = name.contains("/") ? name.substring(0, name.indexOf('/')) : name;
				String uri = "http://localhost:8500/previlege/shared?" + "name=" + userName;
				HttpPost post = new HttpPost(uri);
				response = client.execute(post);
				String records = handleResponseString(response);
				System.out.println("-----------------------------------------------------------");
				if (records.contains("No files available")) {
					continue;
				}
				int fileId = ClientUtilities.inputInt("file id to proceed", 1, 20);
				uri = "http://localhost:8500/previlege/shared/check?" + "name=" + userName + "&id=" + fileId;
				post = new HttpPost(uri);
				response = client.execute(post);
				String writable = handleResponseString(response), location = "";
				if (!writable.contains("Access denied")) {
					location = writable.substring(writable.indexOf(':') + 1, writable.length());

					if (writable.contains("Write") && location.contains(".txt")) {
						System.out.println("Accessing shared file...");
						String fileName = location.substring(location.lastIndexOf('/') + 1, location.length());
						openFile(fileName, location.substring(0, location.lastIndexOf('/')), true);

					} else if (writable.contains("Read") && location.contains(".txt")) {
						System.out.println("Accessing shared file...");
						String fileName = location.substring(location.lastIndexOf('/') + 1, location.length());
						openFile(fileName, location.substring(0, location.lastIndexOf('/')), false);

					} else if (writable.contains("Write") && !location.contains(".txt")) {
						System.out.println("Accessing shared folder...");
						accessFolder(location, false, true);

					} else if (writable.contains("Read") && !location.contains(".txt")) {
						System.out.println("Accessing shared folder...");
						accessFolder(location, false, false);
					}
				}
			}
//			case MY_SHARED: {
			else if ((owner && option == 9) || (owner && option == 10)) {
				String userName = name.contains("/") ? name.substring(0, name.indexOf('/')) : name;
				String uri = "http://localhost:8500/previlege/myshared?" + "name=" + userName;
				HttpPost post = new HttpPost(uri);
				response = client.execute(post);
				handleResponseString(response);
				if (option == 10)
					System.out.println("-----------------------------------------------------------");
			}
//			case REMOVE_SHARED: {
			if (owner && option == 10) {
				String fileId = ClientUtilities.inputString("file id", ".*", 1, 260);
				String uri = "http://localhost:8500/previlege/removesharedfile?" + "&id=" + fileId;
				HttpPost post = new HttpPost(uri);
				@SuppressWarnings("resource")
				Scanner scan = new Scanner(System.in);
				System.out.println("Enter user names\nEnter \"stop\" to end");
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				String sharedUserName;
				while (true) {
					System.out.println("Enter username...");
					sharedUserName = scan.next();
					if (sharedUserName.equalsIgnoreCase("stop")) {
						break;
					}
					nameValuePairs.add(new BasicNameValuePair(sharedUserName, "remove"));
				}
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
			}
//			case FIND: {
			else if (owner && option == 11) {
				String words = ClientUtilities.inputString("the word you want to search", ".*", 1, 20);
				String uri = defaultUri + "/file/find?";
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				for (String word : words.split(" ")) {
					nameValuePairs.add(new BasicNameValuePair("word", word));
				}
				HttpPost post = new HttpPost(uri);
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(post);
				handleResponse(response);
			}
//			case GO_BACK_DIRECTORY: {
			else if ((owner && option == 12) || (!owner && write && option == 7) || (!write && option == 3)) {
				return false;
			}
//			case LOG_OUT: {
			else if ((owner && option == 13) || (!owner && write && option == 8) || (!write && option == 4)) {
				return true;
			}
		}
		return true;
	}

	static String handleResponseString(HttpResponse response) throws UnsupportedOperationException, IOException {
		int status = response.getStatusLine().getStatusCode();
		String line = "", returnLine = "";
		if (status >= 200 && status < 300) {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			int count = 0;
			while ((line = br.readLine()) != null) {
				System.out.println(URLDecoder.decode(line, "UTF-8"));
				if (count == 0) {
					returnLine = URLDecoder.decode(line, "UTF-8");
					count++;
				}
			}
			br.close();
		} else {
			System.out.println("Unexpected response status: " + status);
		}
		return returnLine;
	}

	static boolean handleResponse(HttpResponse response) throws UnsupportedOperationException, IOException {
		int status = response.getStatusLine().getStatusCode();
		String line = "";
		if (status >= 200 && status < 300) {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			while ((line = br.readLine()) != null) {
				System.out.println(URLDecoder.decode(line, "UTF-8"));
				if (line.equals("Folder present") || line.equals("Success") || line.equals("Write enabled")) {
					br.close();
					return true;
				}
			}
			br.close();
		} else {
			System.out.println("Unexpected response status: " + status);
		}
		return false;
	}

	private static void openFile(String fileName, String location, boolean editable)
			throws ClientProtocolException, IOException {

		String uri = "http://localhost:8500/access" + "/read?" + "location=" + location + "&filename=" + fileName;
		HttpPost post = new HttpPost(uri);
		HttpResponse response = client.execute(post);
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

		if (paragraph.contains("<ERROR--->File not found<---ERROR>")) {
			System.out.println("File not found");
		} else {
			if (editable) {
				editor("DISPLAY_MODE", paragraph, location, fileName, uri, "http://localhost:8500/access", post,
						response);
			} else {
				editor("READ_ONLY_MODE", paragraph, location, fileName, uri, "http://localhost:8500/access", post,
						response);
			}
		}
	}

	private static void editor(String mode, String paragraph, String name, String fileName, String uri,
			String defaultUri, HttpPost post, HttpResponse response) throws ClientProtocolException, IOException {
		TextEditor textEditor = new TextEditor(mode, paragraph, fileName);
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
		if (textEditor.isEdited()) {
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
	}

	public static void main(String[] args) {
		client = HttpClients.createDefault();
		List<String> list = new ArrayList<String>();
		list.add("Login");
		list.add("Sign Up");
		while (true) {
			int option = ClientUtilities.selectOption(list);
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