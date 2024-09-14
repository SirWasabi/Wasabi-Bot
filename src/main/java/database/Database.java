package database;

import java.sql.*;

public class Database {

    public Connection conn = null;

    public void connectToDatabase(String filename) {

        String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/" + filename;

        try {
            conn = DriverManager.getConnection(url);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(String sql) throws SQLException {
        Statement stmt = null;
        stmt = conn.createStatement();
		stmt.executeUpdate(sql);
        stmt.close();
    }

    public ResultSet executeQuery(String sql) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
