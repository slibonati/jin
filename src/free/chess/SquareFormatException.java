/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import free.util.FormatException;


/**
 * Thrown when the format of a chess Square is wrong.
 */

public class SquareFormatException extends FormatException{


  /**
   * Creates a new SquareFormatException for the given real Throwable and the given
   * message.
   */

  public SquareFormatException(Throwable realException, String message){
    super(realException, message);
  }



  /**
   * Creates a new SquareFormatException for the given real Throwable.
   */

  public SquareFormatException(Throwable realException){
    super(realException);
  }



  /**
   * Creates a new SquareFormatException with the given message.
   */

  public SquareFormatException(String message){
    super(message);
  }



  /**
   * Creates a new SquareFormatException.
   */

  public SquareFormatException(){
    super();
  }

}
