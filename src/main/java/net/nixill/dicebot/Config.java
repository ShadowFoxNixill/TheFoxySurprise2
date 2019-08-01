package net.nixill.dicebot;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import discord4j.core.object.util.Snowflake;

public class Config {
  private static Connection conn;
  private static Statement  stmt;
  
  private static boolean   debug;
  private static Snowflake creator;
  
  public static void init(Connection c) throws SQLException {
    conn = c;
    stmt = conn.createStatement();
    
    // Get the creator by their ID (should be me but if you're hosting your
    // own bot you can change it to you :3)
    ResultSet rs = stmt
        .executeQuery("SELECT value FROM main WHERE name = 'creator';");
    rs.next();
    creator = Snowflake.of(rs.getLong(1));
    rs.close();
    
    // Get whether or not the bot is in debug mode (should be yes for PTB
    // and no for the real deal)
    rs = stmt.executeQuery("SELECT value FROM main WHERE name = 'debug';");
    rs.next();
    debug = rs.getBoolean(1);
    rs.close();
    
    // not closing connection because it's also used by the loader
    stmt.close();
  }
  
  public static boolean isDebug() {
    return debug;
  }
  
  public static Snowflake creator() {
    return creator;
  }
}