import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;

public class HTTPClient {
	static CloseableHttpClient client = HttpClients.createDefault();

	static void signUp() {
//		String uri = "http://localhost:8500/signup/";
//		HttpPost httpPost = new HttpPost(uri);
		String name = Utilities.inputString("username", ".*", 1, 15);
		String password = Utilities.inputString("password", ".*", 1, 15);
		String defaultUri = "http://localhost:8500/signup?" + "name=" + name + "&password=" + password;
		HttpGet httpGet = new HttpGet(defaultUri);
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//		nameValuePairs.add(new BasicNameValuePair("user", name));
//		nameValuePairs.add(new BasicNameValuePair("password", password));
		try {
//			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = br.readLine()) != null) {
					if(line.equals("Root folder created")) {
						accessFolder(name);
						System.out.println(line);
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

	private static void login() {
		String name = Utilities.inputString("username", ".*", 1, 15);
		String password = Utilities.inputString("password", ".*", 1, 15);
		String defaultUri = "http://localhost:8500/login?" + "name=" + name + "&password=" + password;
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
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				System.err.println("Method failed: ");
			}
			System.out.println(response.getStatusLine());
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
	}
	
	static void accessFolder(String name) {
		String defaultUri = "http://localhost:8500/login/access?" + "name=" + name;
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
			login();
			break;
		}
		case 2: {
			signUp();
			break;
		}
		}
	}

}
