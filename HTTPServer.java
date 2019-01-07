import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPServer {
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
		System.out.println("Server started in port 8500");
		server.createContext("/login", new LoginHandler());
		server.createContext("/signup", new SignUpHandler());
		server.setExecutor(null);
		server.start();
	}
}

class LoginHandler implements HttpHandler {
	public void handle(HttpExchange t) throws IOException {
		System.out.println("Client Connected");
		String response = "This is the response";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}

class SignUpHandler implements HttpHandler {
	public void handle(HttpExchange t) throws IOException {
		System.out.println("Client Connected");
		URI uri = t.getRequestURI();
		Map<String, String> params = queryToMap(uri.getQuery());
		
		Map.Entry<String,String> entry = params.entrySet().iterator().next();
		String name = entry.getValue();
		entry = params.entrySet().iterator().next();
		String password = entry.getValue();

		String path = "/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/" + name;
		File file = new File(path);
		OutputStream os = t.getResponseBody();
		if (file.exists()) {
			String unsuccessfulResponse = "Username already present";
			t.sendResponseHeaders(200, unsuccessfulResponse.length());
			os.write(unsuccessfulResponse.getBytes());

		} else {
			file.mkdir();
			String successfulResponse = "Root folder created";
			t.sendResponseHeaders(200, successfulResponse.length());
			os.write(successfulResponse.getBytes());
		}
		os.close();
	}

	public static Map<String, String> queryToMap(String query) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}
}