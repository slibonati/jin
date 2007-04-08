/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.jin.console.fics;

import free.jin.I18n;
import free.jin.ServerUser;
import free.jin.console.SystemConsoleDesignation;
import free.jin.event.ChatEvent;



/**
 * The FICS-specific system console designation. 
 */

public class FreechessSystemConsoleDesignation extends SystemConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>FreechessSystemConsoleDesignation</code> with the
   * specified encoding.
   */
  
  public FreechessSystemConsoleDesignation(String encoding){
    super(encoding);
  }
  
  
  
  /**
   * Returns whether the specified chat event is a personal tell. 
   */
  
  protected boolean isPersonalTell(ChatEvent evt){
    String type = evt.getType();
    return type.equals("tell") || type.equals("say") || type.equals("ptell");
  }
  
  
  
  /**
   * Returns the text which should be added to the console for the specified
   * chat event.
   */

  protected String textForChat(ChatEvent evt){
    String type = evt.getType();
    ServerUser sender = evt.getSender();
    String title = evt.getSenderTitle();
    String rating = evt.getSenderRating() == -1 ? "----" : String.valueOf(evt.getSenderRating());
    String message = decode(evt.getMessage(), evt.getConnection());
    Object forum = evt.getForum();
    
    Object [] args = new Object[]{String.valueOf(sender), title, rating, String.valueOf(forum), message};
    
    I18n i18n = I18n.get(FreechessSystemConsoleDesignation.class);
    return i18n.getFormattedString(type + ".displayPattern", args);
  }
  
  
  
}