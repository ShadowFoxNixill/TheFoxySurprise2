package net.nixill.dicebot.variables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.nixill.dice.objects.DCEntity;
import net.nixill.dice.objects.DCValue;
import net.nixill.dice.operations.FunctionLoader;
import net.nixill.dice.operations.Functions;
import net.nixill.dice.parsing.ExpressionSplitter;
import net.nixill.dicebot.WrappedException;

public class DicebotLoader extends FunctionLoader {
  private Connection conn;
  
  public DicebotLoader(Connection c) throws SQLException {
    conn = c;
  }
  
  private DCEntity getVar(String name, boolean global) {
    // First we need to get what channel it's in
    long channel;
    if (global) {
      channel = 0;
    } else {
      channel = Long.parseLong(Functions.getThread("server"));
    }
    
    // Now we grab the variable from the database
    DCEntity out = null;
    try {
      PreparedStatement ps = conn.prepareStatement(
          "SELECT value FROM variables WHERE name = ? AND server = ?");
      ps.setString(1, name);
      ps.setLong(2, channel);
      
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        out = ExpressionSplitter.parse(rs.getString(1));
      } else {
        out = null;
      }
      
      rs.close();
      ps.close();
    } catch (SQLException ex) {
      throw new WrappedException(ex);
    }
    
    return out;
  }
  
  @Override
  public DCEntity load(String name) {
    return getVar(name, false);
  }
  
  @Override
  public DCEntity loadEnv(String name) {
    return getVar("_" + name, false);
  }
  
  @Override
  public DCEntity loadGlobal(String name) {
    return getVar("$" + name, true);
  }
  
  @Override
  public DCEntity lastResult(String name) {
    DCEntity out = getVar("^" + name, false);
    if (out != null) {
      return out;
    } else {
      return getVar(name, false);
    }
  }
  
  private void setVar(String name, DCEntity ent, boolean global) {
    // First we need to get what channel it's in
    long channel;
    if (global) {
      channel = 0;
    } else {
      channel = Long.parseLong(Functions.getThread("server"));
    }
    
    // Now we plop the variable into the database
    try {
      PreparedStatement ps = conn
          .prepareStatement("INSERT INTO variables VALUES (?, ?, ?)");
      ps.setString(1, name);
      ps.setLong(2, channel);
      ps.setString(3, ent.toCode());
      
      ps.execute();
      ps.close();
    } catch (SQLException ex) {
      throw new WrappedException(ex);
    }
  }
  
  private void killVar(String name, boolean global) {
    // First we need to get what channel it's in
    long channel;
    if (global) {
      channel = 0;
    } else {
      channel = Long.parseLong(Functions.getThread("server"));
    }
    
    // Now we plop the variable into the database
    try {
      PreparedStatement ps = conn.prepareStatement(
          "DELETE FROM variables WHERE name = ? AND server = ?");
      ps.setString(1, name);
      ps.setLong(2, channel);
      
      ps.execute();
      ps.close();
    } catch (SQLException ex) {
      throw new WrappedException(ex);
    }
  }
  
  @Override
  public void save(String name, DCEntity ent) {
    if (ent != null) {
      setVar(name, ent, false);
    } else {
      killVar(name, false);
    }
    killVar("^" + name, false);
  }
  
  @Override
  public void saveEnv(String name, DCEntity ent) {
    if (ent != null) {
      setVar("_" + name, ent, false);
    } else {
      killVar("_" + name, false);
    }
  }
  
  @Override
  public void saveGlobal(String name, DCEntity ent) {
    if (ent != null) {
      setVar("$" + name, ent, true);
    } else {
      killVar("$" + name, true);
    }
  }
  
  @Override
  public void saveResult(String name, DCValue result) {
    if (result != null) {
      setVar("^" + name, result, false);
    } else {
      killVar("^" + name, false);
    }
  }
  
}