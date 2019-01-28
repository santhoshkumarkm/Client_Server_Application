package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientInfoDao {
	static Connection con;

	public ClientInfoDao() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/online_file_storage", "root", null);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int addClient(String name, String password) {
		PreparedStatement stmt = null;
		int returnValue = 0;
		try {
			stmt = con.prepareStatement("insert into clients_info values(null,?,?)");
			stmt.setString(1, name);
			stmt.setString(2, String.valueOf(password));
			returnValue = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
		return returnValue;
	}

	public String checkClient(String name, String password) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select * from clients_info");
			while (rs.next()) {
				if (rs.getString(2).equals(name)) {
					if (rs.getString(3).equals(password)) {
						return "Access granted";
					} else {
						return "Password incorrect";
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
		return "User not registered";
	}

	public long insertSharedFile(String location) {
		PreparedStatement stmt = null;
		Statement stmt2 = null;
		long primaryKeyValue = 0;
		ResultSet generatedKeys = null, rs = null;
		try {
			stmt = con.prepareStatement("insert ignore into shared_files_info values(null,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, location);
			stmt.executeUpdate();
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				primaryKeyValue = generatedKeys.getLong(1);
			} else {
				stmt2 = con.createStatement();
				String qq = "select id from shared_files_info where filelocation='" + location + "'";
				rs = stmt2.executeQuery(qq);
				if (rs.next()) {
					primaryKeyValue = rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (generatedKeys != null)
					generatedKeys.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			try {
				if (stmt2 != null)
					stmt2.close();
			} catch (Exception e) {
			}

		}
		return primaryKeyValue;
	}

	public String insertSharedUsers(int fileId, String name, String privilege) {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement("insert ignore into shared_users_info values(?,?,?)");
			int userId = getUserId(name);
			if (userId == 0)
				return "Error found";
			else {
				stmt.setInt(2, userId);
				stmt.setInt(1, fileId);
				stmt.setString(3, privilege);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}
		return "Success";
	}

	private int getUserId(String name) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("select id from clients_info where name = '" + name + "'");
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

		return 0;
	}

	public String getFileUserInfo(String userName, int fileId) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(
					"select f.id, f.filelocation, c.name, u.privilege from shared_users_info u inner join shared_files_info f on f.id = u.file_id inner join clients_info c on u.user_id = c.id where c.name ='"
							+ userName + "'");
			while (rs.next()) {
				if (rs.getInt(1) == fileId) {
					if (rs.getString(3).equals(userName)) {
						if (rs.getString(4).equals("write")) {
							return "Write enabled:" + rs.getString(2);
						} else {
							return "Read only:" + rs.getString(2);
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}
		return "Access denied";
	}

	public String getSharedFilesForAnUser(String userName) {
		Statement stmt = null;
		String location = "";
		String temp, msg = "";
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(
					"select f.id, f.filelocation, c.name, u.privilege from shared_users_info u inner join shared_files_info f on f.id = u.file_id inner join clients_info c on u.user_id = c.id where c.name ='"
							+ userName + "'");
			while (rs.next()) {
				location = rs.getString(2);
				temp = "File Id: " + rs.getInt(1) + " | " + "File name: \""
						+ location.substring(location.lastIndexOf('/') + 1, location.length()) + "\" | " + "Owner: "
						+ location.substring(0, location.indexOf('/')) + " | " + "Access Type: " + rs.getString(4);
				msg += "\n" + temp;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}
		if (location.equals("")) {
			msg = "No files available";
		}
		return "Public Files..." + msg;
	}

	public String getSharedFilesByAnUser(String userName) {
		Statement stmt = null;
		String location = "";
		String temp, msg = "";
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(
					"select f.id, f.filelocation, c.name, u.privilege from shared_users_info u inner join shared_files_info f on f.id = u.file_id inner join clients_info c on u.user_id = c.id where f.filelocation like '"
							+ userName + "%'");
			while (rs.next()) {
				location = rs.getString(2);
				temp = "File Id: " + rs.getInt(1) + " | " + "File name: \""
						+ location.substring(location.lastIndexOf('/') + 1, location.length()) + "\" | " + "Shared to: "
						+ rs.getString(3) + " | " + "Access Type: " + rs.getString(4);
				msg += "\n" + temp;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}
		if (location.equals("")) {
			msg = "No files available";
		}
		return "Public Files..." + msg;
	}

	public String closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Logout successful";
	}
}
