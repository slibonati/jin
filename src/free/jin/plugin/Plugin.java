/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin.plugin;

import free.jin.*;
import javax.swing.JMenu;
import free.util.MemoryFile;


/**
 * The base class for all plugins. All subclasses must have a no-arg constructor
 * to work properly.
 */

public abstract class Plugin{



  /**
   * The plugin's context.
   */

  private PluginContext context;



  /**
   * The connection.
   */

  private Connection conn;



  /**
   * The preferences.
   */

  private Preferences prefs;




  /**
   * Sets the plugin's context. The plugin may override this method to check
   * that the context has the properties (usually the right type of Connection)
   * required by the plugin and throw a <code>PluginStartException</code> if
   * not.
   */

  public void setContext(PluginContext context) throws PluginStartException{
    if (this.context != null)
      throw new IllegalStateException("PluginContext already set");

    this.context = context;
  }



  /**
   * Returns the connection to the server.
   */

  public Connection getConn(){
    if (conn == null)
      conn = context.getConnection();

    return conn;
  }



  /**
   * Returns the <code>Preferences</code> object the plugin should use for
   * getting/setting its preferences for the user of the current session.
   */

  public Preferences getPrefs(){
    if (prefs == null)
      prefs = context.getPreferences(this);

    return prefs;
  }
  
  
  
  /**
   * Returns the resources of the specified type. See
   * <code>JinContext.loadResources</code> for more information.
   */
   
  public ClassLoader [] loadResources(String resourceType){
    return context.getJinContext().loadResources(resourceType); 
  }



  /**
   * Returns the <code>UIProvider</code>.
   */

  public UIProvider getUIProvider(){
    return context.getJinContext().getUIProvider();
  }



  /**
   * Creates a UI container with the specified id for this plugin. See
   * {@link UIProvider#createPluginUIContainer(Plugin, String)} for more
   * information.
   */

  public PluginUIContainer createContainer(String id){
    return getUIProvider().createPluginUIContainer(this, id);
  }



  /**
   * Returns the user's <code>MemoryFile</code> with the specified name. See
   * {@link #setFile(String, MemoryFile)} for an explanation on the user files
   * mechanism.
   */

  public MemoryFile getFile(String filename){
    return getUser().getFile(getId() + "/" + filename);
  }



  /**
   * Sets the user file with the specified name to be the specified
   * <code>MemoryFile</code>. If a file with the specified name already exists,
   * it is deleted. The specified file may also be <code>null</code> if you wish
   * to use this method just for its side-effect. This mechanism (see also the
   * {@link #getFile(String) method) allows plugins to store somewhat larger
   * amounts of information than what the preferences mechanism is intended for.
   * It also allows storing the data in whatever format is convenient to the
   * plugin (where Preferences supports only a finite amount of types and
   * handles the format internally). Typically, you only need to create and set
   * a file only once - you can then obtain the <code>MemoryFile</code> object
   * via the <code>getFile</code> method and write into it. There is no need to
   * replace the file each time you want to change it. You can, however, use it
   * in both manners.
   */

  public void setFile(String filename, MemoryFile file){
    getUser().setFile(getId() + "/" + filename, file);
  }



  /**
   * Returns the <code>User</code> object representing the account we're using
   * to connect to the server.
   */

  public User getUser(){
    return context.getUser();
  }



  /**
   * Returns the <code>Server</code> object representing the server we're
   * connected to.
   */

  public Server getServer(){
    return getUser().getServer();
  }



  /**
   * Returns the plugin with the specified id that is running in the same
   * context as this plugin.
   */

  public Plugin getPlugin(String id){
    return context.getPlugin(id);
  }
  



  /**
   * Returns this plugin's id. Ids need to be unique across all plugins that
   * can be run for a single <code>User</code> object.
   */

  public abstract String getId();



  /**
   * Returns the plugin's name. This should be something descriptive that can
   * be shown to the user.
   */

  public abstract String getName();


  
  /**
   * Returns whether this plugin has a preferences panel it wants to be
   * displayed to the user. The default implementation returns
   * <code>false</code>.
   */

  public boolean hasPreferencesUI(){
    return false;
  }



  /**
   * Return a PreferencesPanel for changing the plugin's preferences. This
   * method will never be called if <code>hasPreferencesUI</code> returns false.
   * The default implementation throws an <code>IllegalStateException</code>,
   * since it's not supposed to be called.
   */

  public PreferencesPanel getPreferencesUI(){
    throw new IllegalStateException("This plugin has no preferences ui");
  }



  /**
   * Creates and returns a menu for this plugin. Returns <code>null</code> to
   * indicate that the plugin doesn't have a menu. The actual placing of the
   * menu is handled by the context. The default implementation returns
   * <code>null</code>.
   */

  public JMenu createPluginMenu(){
    return null;
  }



  /**
   * Returns whatever <code>getName</code> does.
   */

  public String toString(){
    return getName();
  }



  /**
   * This method is invoked to signal to the plugin to start its activity. The
   * default implementation does nothing. The plugin may throw a
   * <code>PluginStartException</code> to indicate that something is wrong and
   * it cannot properly start.
   */

  public void start() throws PluginStartException{

  }



  /**
   * Asks the plugin to save any unsaved information it has into preferences
   * or whatever other medium it uses. This method is usually called just before
   * the <code>stop</code> method. The default implementation does nothing.
   */

  public void saveState(){

  }



  /**
   * This method is invoked to signal to the plugin to stop its activity. The
   * default implementation does nothing.
   */

  public void stop(){

  }


}