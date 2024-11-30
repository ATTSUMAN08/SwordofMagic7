package swordofmagic7;

import java.sql.*;

public class MySQL {

    public static void connect() {
        Connection con;
        PreparedStatement ps;
        ResultSet rs;

        try {
            con = DriverManager.getConnection("jdbc:mysql://192.168.0.18/swordofmagic7",
                    "root",
                    ""
            );
        } catch (SQLException e) {
            SomCore.plugin.getLogger().warning("Could not connect to MySQL database! " + e);
        }

    }
}
