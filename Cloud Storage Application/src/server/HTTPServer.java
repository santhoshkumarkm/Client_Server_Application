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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPServer {
	final public static String defaultLocation = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/";

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
		System.out.println("Server started in port 8500");
		server.createContext("/login", new LoginHandler());
		server.createContext("/access", new AccessHandler());
		server.createContext("/previlege", new PrevilegeHandler());
		server.setExecutor(null);
		server.start();
	}
}

class LoginHandler implements HttpHandler {
	ClientInfoDao dao = new ClientInfoDao();

	public void handle(HttpExchange ex) throws IOException {
		URI uri = ex.getRequestURI();
		String[] userAttributes = Utilities.queryToMap(uri.getQuery());
		String name = userAttributes[0], password = userAttributes[1], userType = userAttributes[2],
				msg = "Not success";
		if (userType.equals("new")) {
			System.out.println("A new client is trying to connect");
			File file = new File(HTTPServer.defaultLocation + name);
			if (!file.exists()) {
				if (dao.addClient(name, password) == 1) {
					msg = "Root folder created for user " + name;
					file.mkdir();
				}
			} else {
				msg = "Username already present";
			}
		} else if (userType.equals("existing")) {
			System.out.println("An existing client is trying to connect");
			msg = dao.checkClient(name, password);
		} else {
			msg = dao.closeConnection();
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}
}

class AccessHandler implements HttpHandler {
	ClientInfoDao dao = new ClientInfoDao();
	HashMapUtil hashMapUtil = new HashMapUtil();
	File hashMapFile = new File("/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/HashMap.txt");

	@Override
	public void handle(HttpExchange ex) throws IOException {
		URI uri = ex.getRequestURI();
		String uriPath = uri.getPath();
		String msg = "";
		if (uri.getPath().contains("create")) {
			InputStream in = ex.getRequestBody();
			String s = Utilities.stringBuilder(new BufferedReader(new InputStreamReader(in)));
			if (create(s, uriPath)) {
				msg = "Success";
			} else {
				msg = "Not success";
			}
		} else if (uri.getPath().contains("read")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			File file = new File(
					HTTPServer.defaultLocation + "/" + readFileAttributes[0] + "/" + readFileAttributes[1]);
			if (file.exists()) {
				msg = Utilities.stringBuilder(new BufferedReader(new FileReader(file)));
			} else {
				msg = "<ERROR--->File not found<---ERROR>";
			}
		} else if (uri.getPath().contains("find")) {
			InputStream in = ex.getRequestBody();
			String request = Utilities.stringBuilder(new BufferedReader(new InputStreamReader(in)));
			request = request.substring(0, request.length() - 1);
			String[] readFileAttributes = Utilities.queryToMap(request);

			LinkedHashMap<Integer, ArrayList<Integer>> fileAndPosMap = hashMapUtil.findWord(readFileAttributes);
			if (fileAndPosMap != null) {
				msg = "Present in :";
				HashMapObject hashMapObject = (HashMapObject) Utilities.readFile(hashMapFile);
				for (Map.Entry<Integer, ArrayList<Integer>> entry : fileAndPosMap.entrySet()) {
					msg += "\n" + "File name: " + hashMapObject.getFiles().get(entry.getKey()) + "\tCount: "
							+ entry.getValue().size();
				}
			} else {
				msg = "Not found";
			}
		} else if (uri.getPath().contains("check")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String location = readFileAttributes[0] + "/" + readFileAttributes[1];
			File file = new File(HTTPServer.defaultLocation + "/" + location);
			if (file.exists()) {
				msg = "Folder present";
				if (uri.getPath().contains("delete")) {
					file.delete();
					dao.deleteSharedFile(location);
					msg = "Delete successful";
				}
			} else {
				msg = "<ERROR--->File not found<---ERROR>";
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
		String fileLocation = "", fileName = "", content = "", filePath;
		String fileAttributes[] = Utilities.queryToMap(request);
		fileLocation = fileAttributes[0];
		fileName = fileAttributes[1];
		filePath = fileLocation + "/" + fileName;
		File file = new File(HTTPServer.defaultLocation + "/" + filePath);
		if (!file.exists()) {
			if (uriPath.contains("folder")) {
				file.mkdir();
				return true;
			} else if (uriPath.contains("file")) {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write(content);
				fw.close();
				return true;
			}
		} else if (uriPath.contains("edit")) {
			file.delete();
			content = fileAttributes[2];
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			hashMapUtil.setFileInfo(filePath, content);
			hashMapUtil.addWords();
			fw.close();
			return true;
		}
		return false;
	}
}

class PrevilegeHandler implements HttpHandler {
	ClientInfoDao dao = new ClientInfoDao();

	public void handle(HttpExchange ex) throws IOException {
		URI uri = ex.getRequestURI();
		String uriPath = uri.getPath();
		String msg = "";
		if (uriPath.contains("sharefile")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String location = readFileAttributes[0] + "/" + readFileAttributes[1];
			File file = new File(HTTPServer.defaultLocation + "/" + location);
			if (file.exists()) {
				InputStream in = ex.getRequestBody();
				String userDetail = Utilities.stringBuilder(new BufferedReader(new InputStreamReader(in)));

				Map<String, String> result = new LinkedHashMap<String, String>();
				for (String param : userDetail.split("&")) {
					String pair[] = param.split("=");
					result.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1].trim(), "UTF-8"));
				}
				int primaryKeyValue = 0;
				primaryKeyValue = (int) dao.insertSharedFile(location);
				for (Map.Entry<String, String> entry : result.entrySet()) {
					msg += '\n' + entry.getKey() + " : "
							+ dao.insertSharedUsers(primaryKeyValue, entry.getKey(), entry.getValue());
				}
			} else {
				msg = "<ERROR--->File not found<---ERROR>";
			}
		} else if (uriPath.contains("removesharedfile")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			int fileId = Integer.valueOf(readFileAttributes[0]);
			InputStream in = ex.getRequestBody();
			String userDetail = Utilities.stringBuilder(new BufferedReader(new InputStreamReader(in)));
			String arr[] = Utilities.queryToMap(userDetail);
			for (String user : arr) {
				msg += "\n" + user + " : " + dao.removeSharedUsers(fileId, user);
			}
		} else if (uriPath.contains("myshared")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String userName = readFileAttributes[0];
			msg = dao.getSharedFilesByAnUser(userName);

		} else if (uriPath.contains("check")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String userName = readFileAttributes[0];
			int fileId = Integer.valueOf(readFileAttributes[1]);
			msg = dao.getFileUserInfo(userName, fileId);

		} else if (uriPath.contains("shared")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String userName = readFileAttributes[0];
			msg = dao.getSharedFilesForAnUser(userName);
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}
}