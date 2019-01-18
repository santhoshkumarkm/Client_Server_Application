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
	private static String stringBuilder(BufferedReader bin) throws IOException {
		StringBuilder stringBuilderVar = new StringBuilder();
		String s = "";
		while ((s = bin.readLine()) != null) {
			stringBuilderVar.append(s + "\n");
		}
		return stringBuilderVar.toString();
	}

	@Override
	public void handle(HttpExchange ex) throws IOException {
		URI uri = ex.getRequestURI();
		String uriPath = uri.getPath();
		String msg = "";
		if (uri.getPath().contains("create")) {
			InputStream in = ex.getRequestBody();
			String s = stringBuilder(new BufferedReader(new InputStreamReader(in)));
			if (create(s, uriPath)) {
				msg = "Success";
			} else
				msg = "Not success";
		} else if (uri.getPath().contains("read")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			File file = new File(
					HTTPServer.defaultLocation + "/" + readFileAttributes[0] + "/" + readFileAttributes[1]);
			if (file.exists()) {
				msg = stringBuilder(new BufferedReader(new FileReader(file)));
			} else {
				msg = "<ZOHO--->File not found<---ZOHO>";
			}
		} else if (uri.getPath().contains("check")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			File file = new File(
					HTTPServer.defaultLocation + "/" + readFileAttributes[0] + "/" + readFileAttributes[1]);
			if (file.exists()) {
				msg = "Folder present";
				if(uri.getPath().contains("delete")) {
					file.delete();
					msg = "Delete successful";
				}
			} else {
				msg = "<ZOHO--->File not found<---ZOHO>";
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
					if (temp.equals(""))
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
		request = request.substring(0, request.length() - 1);
		String fileLocation = "", fileName = "", content = "";
		String fileAttributes[] = Utilities.queryToMap(request);
		fileLocation = fileAttributes[0];
		fileName = fileAttributes[1];
		File file = new File(HTTPServer.defaultLocation + "/" + fileLocation + "/" + fileName);
		if (!file.exists()) {
			if (uriPath.contains("folder")) {
				file.mkdir();
				return true;
			} else {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write(content);
				fw.close();
				return true;
			}
		} else {
			if (uriPath.contains("edit")) {
				file.delete();
				content = fileAttributes[2];
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
		File listFile = new File("/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/clientlist.txt");
		LoginList loginList = null;
		if (listFile.exists()) {
			try {
				loginList = (LoginList) Utilities.readFile(listFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			loginList = new LoginList();
		}
		System.out.println("Client Connected");
		URI uri = ex.getRequestURI();
		String[] userAttributes = Utilities.queryToMap(uri.getQuery());
		String name = userAttributes[0], userType = userAttributes[2], password = userAttributes[1];
		File file = new File(HTTPServer.defaultLocation + name);
		String msg = "";
		if (userType.equals("new")) {
			if (!file.exists()) {
				loginList.addEntry(name, password);
				file.mkdir();
				try {
					Utilities.writeFile(listFile, loginList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				msg = "Root folder created for user " + name;
			} else {
				msg = "Username already present";
			}
		} else {
			msg = loginList.checkEntry(name, password);
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}

}