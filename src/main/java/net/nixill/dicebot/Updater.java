package net.nixill.dicebot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Updater {
  private static Connection conn;
  private static Statement  stmt;
  
  public static void init() throws SQLException {
    conn = DriverManager.getConnection("jdbc:sqlite:config.db");
    stmt = conn.createStatement();
    
    // See what version they're already at
    int configVer = getVersion(conn);
    
    // And bring them up to date
    updateConfig(configVer);
    
    // Close the statement cause we don't reuse that
    stmt.close();
  }
  
  private static String verTableMake = "CREATE TABLE IF NOT EXISTS version ("
      + "name TEXT PRIMARY KEY ON CONFLICT IGNORE, value INT);";
  private static String verCheck     = "SELECT version FROM version;";
  
  private static int getVersion(Connection conn) throws SQLException {
    stmt.executeQuery(verTableMake);
    ResultSet res = stmt.executeQuery(verCheck);
    if (res.next()) {
      return res.getInt(1);
    } else {
      return 0;
    }
  }
  
  private static String verSet = "UPDATE version SET version = ?";
  
  // The methods below this line all deal with updating config.db
  private static void updateConfig(int from) throws SQLException {
    switch (from) {
      case 0:
        updateConfig0();
    }
    
    PreparedStatement psmt = conn.prepareStatement(verSet);
    psmt.setInt(1, 1);
    psmt.executeUpdate();
    psmt.close();
  }
  
  private static void updateConfig0() throws SQLException {
    // Config does not exist and needs to be made
    stmt.execute("INSERT INTO version VALUES ('version', 0);");
    stmt.execute("CREATE TABLE main ("
        + "name  VARCHAR PRIMARY KEY ON CONFLICT REPLACE, "
        + "value VARCHAR NOT NULL);");
    stmt.execute(
        "INSERT INTO main VALUES ('creator', '106621544809115648');");
    stmt.execute("INSERT INTO main VALUES ('debug', 'false');");
    stmt.execute("CREATE TABLE notifications ("
        + "channel LONG PRIMARY KEY ON CONFLICT REPLACE, "
        + "type INT NOT NULL);");
    stmt.execute("CREATE TABLE variables (" + "name TEXT (1, 21), "
        + "channel BIGINT, " + "value TEXT NOT NULL, "
        + "PRIMARY KEY (name, channel) ON CONFLICT REPLACE);");
  }
  
  public static Connection getConnection() {
    return conn;
  }
}