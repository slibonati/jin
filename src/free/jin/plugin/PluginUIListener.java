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

import java.util.EventListener;


/**
 * The listener interface for plugin ui events.
 */

public interface PluginUIListener extends EventListener{



  /**
   * This method is invoked when the plugin ui is made visible.
   */

  void pluginUIShown(PluginUIEvent evt);



  /**
   * This method is invoked when the plugin ui is made invisible.
   */

  void pluginUIHidden(PluginUIEvent evt);



  /**
   * This method is invoked when the user performs a ui closing operation.
   */

  void pluginUIClosing(PluginUIEvent evt);



  /**
   * This method is invoked when the plugin ui becomes "active".
   */

  void pluginUIActivated(PluginUIEvent evt);



  /**
   * This method is invoked when the plugin ui becomes "inactive".
   */

  void pluginUIDeactivated(PluginUIEvent evt);
  
  
  
  /**
   * This method is invoked when the plugin ui is disposed.
   */
  
  void pluginUIDisposed(PluginUIEvent evt);
  
  
  
  /**
   * This method is invoked when the title of the plugin ui changes.
   */
  
  void pluginUITitleChanged(PluginUIEvent evt);
  
  
  
  /**
   * This method is invoked when the icon of the plugin ui changes.
   */
  
  void pluginUIIconChanged(PluginUIEvent evt);



}