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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

//class LoginHandler implements HttpHandler {
//	public void handle(HttpExchange ex) throws IOException {
//		File listFile = new File("/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/clientlist.txt");
//		LoginList loginList = null;
//		if (listFile.exists()) {
//			try {
//				loginList = (LoginList) Utilities.readFile(listFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			loginList = new LoginList();
//		}
//		System.out.println("A client is trying to connect");
//		URI uri = ex.getRequestURI();
//		String[] userAttributes = Utilities.queryToMap(uri.getQuery());
//		String name = userAttributes[0], password = userAttributes[1], userType = userAttributes[2];
//		File file = new File(HTTPServer.defaultLocation + name);
//		String msg = "";
//		if (userType.equals("new")) {
//			if (!file.exists()) {
//				loginList.addEntry(name, password);
//				file.mkdir();
//				try {
//					Utilities.writeFile(listFile, loginList);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				msg = "Root folder created for user " + name;
//			} else {
//				msg = "Username already present";
//			}
//		} else {
//			msg = loginList.checkEntry(name, password);
//		}
//		OutputStream os = ex.getResponseBody();
//		ex.sendResponseHeaders(200, msg.length());
//		os.write(msg.getBytes());
//		os.close();
//	}
//}

class LoginHandler implements HttpHandler {
	public void handle(HttpExchange ex) throws IOException {
		System.out.println("A client is trying to connect");
		URI uri = ex.getRequestURI();
		String[] userAttributes = Utilities.queryToMap(uri.getQuery());
		String name = userAttributes[0], password = userAttributes[1], userType = userAttributes[2],
				msg = "Not success";
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/online_file_storage", "root", null);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (userType.equals("new")) {
			File file = new File(HTTPServer.defaultLocation + name);
			if (!file.exists()) {
				try {
					PreparedStatement stmt = con.prepareStatement("insert into clients_info values(null,?,?)");
					stmt.setString(1, name);
					stmt.setString(2, String.valueOf(password));
					if (stmt.executeUpdate() == 1) {
						msg = "Root folder created for user " + name;
						file.mkdir();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			} else {
				msg = "Username already present";
			}
		} else {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("select * from clients_info");
				msg = "User not registered";
				while (rs.next()) {
					if (rs.getString(2).equals(name)) {
						if (rs.getString(3).equals(password)) {
							msg = "Access granted";
						} else {
							msg = "Password incorrect";
						}
						break;
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
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
		} else if (uri.getPath().contains("check")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			File file = new File(
					HTTPServer.defaultLocation + "/" + readFileAttributes[0] + "/" + readFileAttributes[1]);
			if (file.exists()) {
				msg = "Folder present";
				if (uri.getPath().contains("delete")) {
					file.delete();
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

class PrevilegeHandler implements HttpHandler {
	
	public void handle(HttpExchange ex) throws IOException {
		Connection con = null;
		URI uri = ex.getRequestURI();
		String uriPath = uri.getPath();
		String msg = "";
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/online_file_storage", "root", null);
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (uriPath.contains("change")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String location = readFileAttributes[0] + "/" + readFileAttributes[1];

			InputStream in = ex.getRequestBody();
			String userDetail = Utilities.stringBuilder(new BufferedReader(new InputStreamReader(in)));

			Map<String, String> result = new LinkedHashMap<String, String>();
			for (String param : userDetail.split("&")) {
				String pair[] = param.split("=");
				if (pair.length > 1) {
					result.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
				} else {
					result.put(URLDecoder.decode(pair[0], "UTF-8"), "");
				}
			}

			try {
				PreparedStatement stmt = con.prepareStatement("insert ignore into shared_files_info values(null,?)",
						Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, location);
				stmt.executeUpdate();
				ResultSet generatedKeys = stmt.getGeneratedKeys();
				long primaryKeyValue = 0;
				if (generatedKeys.next()) {
					primaryKeyValue = generatedKeys.getLong(1);
				} else {
					Statement stmt3 = con.createStatement();
					String qq = "select id from shared_files_info where filelocation='" + location + "'";
					ResultSet rs1 = stmt3.executeQuery(qq);
					if (rs1.next()) {
						primaryKeyValue = rs1.getLong(1);
					}
				}
				
				
//					int userId;
				for (Map.Entry<String, String> entry : result.entrySet()) {
					PreparedStatement stmt2 = con.prepareStatement("insert into shared_users_info values(?,?,?)");
//					Statement stmt4 = con.createStatement();
//					ResultSet rs2 = stmt4.executeQuery("select id from clients_info where name = '" + entry.getKey()+"'");
//					if (rs2.next()) {
//						userId = rs2.getInt(1);
//					}
//					stmt2.setInt(2, userId);
					stmt2.setInt(1, (int) primaryKeyValue);
					stmt2.setString(2, entry.getKey());
					stmt2.setString(3, entry.getValue());
					stmt2.executeUpdate();
				}
				msg = "Success";
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (uriPath.contains("check")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String userName = readFileAttributes[0];
			int fileId = Integer.valueOf(readFileAttributes[1]);
			try {
				Statement stmt = con.createStatement();
				rs = stmt.executeQuery(
						"select f.id, f.filelocation, u.username, u.privilege from shared_files_info f inner join shared_users_info u on f.id = u.id where u.username ='"
								+ userName + "'");
				while (rs.next()) {
					if (rs.getInt(1) == fileId) {
						if (rs.getString(3).equals(userName)) {
							if (rs.getString(4).equals("write")) {
								msg = "Write enabled:" + rs.getString(2);
								break;
							} else {
								msg = "Read only:" + rs.getString(2);
								break;
							}
						} else {
							msg = "Access denied";
						}
					} else {
						msg = "Access denied";
					}
				}
				rs.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (uriPath.contains("shared")) {
			String[] readFileAttributes = Utilities.queryToMap(uri.getQuery());
			String userName = readFileAttributes[0];
			try {
				Statement stmt = con.createStatement();
				rs = stmt.executeQuery(
						"select f.id, f.filelocation, u.username, u.privilege from shared_files_info f inner join shared_users_info u on f.id = u.id where u.username ='"
								+ userName + "'");
				String location = "";
				String temp;
				while (rs.next()) {
					location = rs.getString(2);
					temp = "File Id: " + rs.getInt(1) + " | " + "File name: \""
							+ location.substring(location.lastIndexOf('/') + 1, location.length()) + "\" | " + "Owner: "
							+ location.substring(0, location.indexOf('/'));
					msg += temp + "\n";
				}
				rs.beforeFirst();
				if (location.equals("")) {
					msg = "No files available";
				}
				msg = "Public Files...\n" + msg;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		OutputStream os = ex.getResponseBody();
		ex.sendResponseHeaders(200, msg.length());
		os.write(msg.getBytes());
		os.close();
	}
}
