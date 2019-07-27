package net.nixill.dicebot.variables;

import java.sql.Connection;

import net.nixill.dice.objects.DCEntity;
import net.nixill.dice.objects.DCValue;
import net.nixill.dice.operations.FunctionLoader;

public class DicebotLoader extends FunctionLoader {
  Connection conn;
  
  public DicebotLoader(Connection c) {
    conn = c;
  }
  
  @Override
  public DCEntity load(String name) {
    
    return null;
  }
  
  @Override
  public DCEntity loadEnv(String name) {
    return null;
  }
  
  @Override
  public DCEntity loadGlobal(String name) {
    return null;
  }
  
  @Override
  public DCEntity lastResult(String name) {
    return null;
  }
  
  @Override
  public void save(String name, DCEntity ent) {
    
  }
  
  @Override
  public void saveEnv(String name, DCEntity ent) {
    
  }
  
  @Override
  public void saveGlobal(String name, DCEntity ent) {
    
  }
  
  @Override
  public void saveResult(String name, DCValue result) {
    
  }
  
}