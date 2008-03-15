/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2008 Alexander Maryanovsky.
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

package free.jin.console.icc;

import java.util.List;
import java.util.regex.Pattern;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.console.ics.IcsCustomConsoleDesignation;

public class ChessclubCustomConsoleDesignation extends IcsCustomConsoleDesignation{
  
  
  
  /**
   * Creates a new <code>ChessclubCustomConsoleDesignation</code>
   */
  
  public ChessclubCustomConsoleDesignation(Connection connection, String name,
      String encoding, boolean isConsoleCloseable, List channels,
      Pattern messageRegex, boolean isIncludeShouts, boolean isIncludeCShouts){
    super(connection, name, encoding, isConsoleCloseable, channels,
        messageRegex, isIncludeShouts, isIncludeCShouts);
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  
  protected void addCShouts(){
    addAccepted("sshout", null, ANY_SENDER);
    
    I18n i18n = I18n.get(ChessclubCustomConsoleDesignation.class);
    
    addCommandType(new AbstractCommandType(i18n.getString("sshout.commandName")){
      protected void send(String userText){
        sendCommand("sshout " + userText);
      }
    });
  }
  
  
  
}
