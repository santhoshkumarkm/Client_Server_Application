package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

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
		String uriPath = uri.getPath();
		String msg = "";
		if (uri.getPath().contains("create")) {
			InputStream in = ex.getRequestBody();
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			StringBuilder stringBuilder = new StringBuilder();
			String s = "";
			while ((s = bin.readLine()) != null) {
				stringBuilder.append(s + "\n");
			}
			bin.close();
			if (create(stringBuilder.toString(), uriPath)) {
				msg = "success";
			} else
				msg = "not success";
		} else if (uri.getPath().contains("read")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			File file;
			if (readFileAttributes[1].equals("root")) {
				file = new File(HTTPServer.defaultLocation + "/" + readFileAttributes[0] + "/" + readFileAttributes[2]);
			} else {
				file = new File(HTTPServer.defaultLocation + "/" + readFileAttributes[0] + "/" + readFileAttributes[1]
						+ "?/" + readFileAttributes[2]);
			}
			if (file.exists()) {
				BufferedReader bin = new BufferedReader(new FileReader(file));
				StringBuilder stringBuilder = new StringBuilder();
				String s = "";
				while ((s = bin.readLine()) != null) {
					stringBuilder.append(s + "\n");
				}
				bin.close();
				msg = stringBuilder.toString();
			} else {
				msg = "File not found";
			}
		} else {
			String[] userRootAttributes = Utilities.queryToMap(uri.getQuery());
			File file = new File(HTTPServer.defaultLocation + "/" + userRootAttributes[0]);
			if (file.exists()) {
				for (File f : file.listFiles()) {
					String temp = "";
					if (f.isDirectory() && f.listFiles().length != 0) {
						for (File f1 : f.listFiles()) {
							temp = temp + f1.getName() + ",";
						}
					}
					if(temp.equals("")) 
						msg = msg + f.getName() + "\n";
					else
						msg = msg + f.getName() + "-->" + temp + "\n";
				}
			}
			if (msg.equals(""))
				msg = "No files currently present";
			else
				msg = "Files present...\n" + msg;
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}

	private boolean create(String request, String uriPath) throws IOException {
		String fileLocation = "", fileName = "", content = "";
		String fileAttributes[] = Utilities.queryToMap(request);
		fileLocation = fileAttributes[0];
		fileName = fileAttributes[1];
		File file;
		if (uriPath.contains("folder")) {
			file = new File(HTTPServer.defaultLocation + "/" + fileLocation + "/" + fileName);
			if (!file.exists()) {
				file.mkdirs();
				return true;
			}
		} else {
			content = fileAttributes[2];
			if (!fileAttributes[3].contains("root") && !fileAttributes[3].contains("ROOT"))
				fileLocation = fileLocation + "/" + fileAttributes[3];
			file = new File(HTTPServer.defaultLocation + "/" + fileLocation + "/" + fileName);
			if (new File(HTTPServer.defaultLocation + "/" + fileLocation).exists() && !file.exists()) {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write(content);
				fw.close();
				return true;
			}
		}
		return false;
	}

}

class LoginHandler implements HttpHandler {
	public void handle(HttpExchange ex) throws IOException {
		System.out.println("Client Connected");
		URI uri = ex.getRequestURI();
		String[] userAttributes = Utilities.queryToMap(uri.getQuery());
		String name = userAttributes[0], userType = userAttributes[2], password = userAttributes[1];
		File file = new File(HTTPServer.defaultLocation + name);
		String msg = "";
		if (userType.equals("new")) {
			if (!file.exists()) {
				LoginList.addEntry(name, password);
				file.mkdir();
				msg = "Root folder created for user " + name;
			} else {
				msg = "Username already present";
			}
		} else {
			msg = LoginList.checkEntry(name, password);
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}

}