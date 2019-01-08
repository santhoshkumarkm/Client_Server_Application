package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPServer {
	final public static String defaultLocation = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/";

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
		System.out.println("Server started in port 8500");
		server.createContext("/access", new AccessHandler());
		server.createContext("/login", new LoginHandler());
		server.setExecutor(null);
		server.start();
	}
}

class AccessHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange ex) throws IOException {
		URI uri = ex.getRequestURI();
		InputStream in = ex.getRequestBody();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		StringBuilder stringBuilder = new StringBuilder();
		String s = "";
		while ((s = bin.readLine()) != null) {
			stringBuilder.append(s + "\n");
		}
		String msg = "";
		if (create(stringBuilder.toString())) {
			msg = "success";
		} else
			msg = "not success";
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}

	private boolean create(String full) throws IOException {
		Map<String, String> params = Utilities.queryToMap(full);
		String fileLocation = "", fileName = "", content = "";
		int i = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (i == 0)
				fileLocation = entry.getValue();
			else {
				fileName = entry.getKey();
				content = entry.getValue();
			}
			i++;
		}
		File file = new File(HTTPServer.defaultLocation + "/" + fileLocation + "/" + fileName);
		if (!file.exists()) {
			if (fileName.contains(".txt")) {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write(content);
				fw.close();
			} else
				file.mkdir();
			return true;
		}
		return false;
	}

}

class LoginHandler implements HttpHandler {
	public void handle(HttpExchange ex) throws IOException {
		System.out.println("Client Connected");
		URI uri = ex.getRequestURI();
		Map<String, String> params = Utilities.queryToMap(uri.getQuery());
		String[] user = new String[params.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			user[i++] = entry.getValue();
		}
		String path = HTTPServer.defaultLocation + user[0];
		File file = new File(path);
		OutputStream os = ex.getResponseBody();
		String msg = "";
		if (file.exists() && user[2].equals("new")) {
			msg = "Username already present";
		} else if (file.exists() && user[2].equals("existing")) {
			msg = "Access granted";
		} else if (!file.exists() && user[2].equals("new")) {
			file.mkdir();
			msg = "Root folder created";
		} else if (!file.exists() && user[2].equals("existing")) {
			msg = "User not registered";
		}
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}

}