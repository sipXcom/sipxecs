package org.sipxcom.verify.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2/4/2016.
 */
public class DatabaseConnector {
    private static Connection con = null;
    public static void setDBConnection(){
        try{
            Class.forName(PropertyLoader.getProperty("db.driver"));
                    con = DriverManager.getConnection(PropertyLoader.getProperty("db.connection.url"), PropertyLoader.getProperty("db.user"), PropertyLoader.getProperty("db.password"));
            if(!con.isClosed()){
                System.out.println("Successfully connected to database");
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

}
