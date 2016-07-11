package org.sipxcom.verify.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC connector
 */
public class DatabaseConnector {
    private static Connection con = null;
    public static void setDBConnection(){
        try{
            Class.forName(PropertyLoader.getProperty("db.driver"));
                    con = DriverManager.getConnection(PropertyLoader.getProperty("db.connection.url"), PropertyLoader.getProperty("db.user"), PropertyLoader.getProperty("db.password"));
            if(!con.isClosed()){
                System.out.println("Successfully connected to database\n");
            }
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
        }
    }

    public static List<String> getQuery(String query) throws SQLException {
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);
        List<String> values = new ArrayList<String>();
        while(rs.next()){
            values.add(rs.getString(1));
        }
        return values;
    }

    public static void executeUpdate(String query) throws SQLException {
        Statement statement = con.createStatement();
        statement.executeUpdate(query);

    }

    public static void closeConnection() throws SQLException {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
