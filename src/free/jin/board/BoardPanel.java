/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.board;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import free.chess.*;
import free.jin.event.*;
import free.jin.Game;
import free.jin.board.event.UserMoveEvent;
import free.jin.board.event.UserMoveListener;
import free.jin.sound.SoundManager;
import free.chess.event.MoveListener;
import free.chess.event.MoveEvent;
import free.workarounds.FixedJPanel;
import free.workarounds.FixedJTable;
import free.util.swing.NonEditableTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.border.EmptyBorder;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * A panel which displays a chess board and all related information, such
 * as opponents' names, clock, opponents' ratings etc. 
 * To use BoardPanel, you must register it as a GameListener to some source of
 * GameEvents, or alternatively, call the methods defined in GameListener
 * directly.
 */

public class BoardPanel extends FixedJPanel implements MoveListener, GameListener, 
    ActionListener, AdjustmentListener, PropertyChangeListener{


  /**
   * The <code>BoardManager</code> this BoardPanel is used by.
   */

  protected final BoardManager boardManager;




  /**
   * The Game this BoardPanel is displaying.
   */

  protected final Game game;




  /**
   * Is this BoardPanel active, i.e. is the game displayed is in progress?
   */

  private boolean isActive = true;




  /**
   * Is this BoardPanel flipped.
   */

  protected boolean isFlipped = false;



  /**
   * The JBoard showing the current position.
   */

  protected JBoard board;



  /**
   * True if we're highlighting our own moves. False if only opponent's moves.
   */

  private boolean highlightOwnMoves;

  
  

  /**
   * The list of made moves.
   */

  protected final Vector madeMoves = new Vector();




  /**
   * The actual position in the game, this may differ than the one on the board
   * because the one on the board may include moves not yet inspected by the
   * server.
   */

  protected final Position realPosition;




  /**
   * The number of the move after which the position displayed on the board 
   * occurs.
   */

  protected int displayedMoveNumber = 0;




  /**
   * The JLabel displaying information about the player with the white pieces.
   */

  protected JLabel whiteLabel;




  /**
   * The JLabel displaying information about the player with the black pieces.
   */

  protected JLabel blackLabel;




  /**
   * The Container of the action buttons (the button panel).
   */

  protected JPanel buttonPanel;




  /**
   * The AbstractChessClock displaying the time on white's clock.
   */

  protected AbstractChessClock whiteClock;




  /**
   * The AbstractChessClock displaying the time on black's clock.
   */

  protected AbstractChessClock blackClock;




  /**
   * The JLabel displaying information about the game.
   */

  protected JLabel gameLabel;



  /**
   * The JTable displaying the move list.
   */

  protected JTable moveListTable;




  /**
   * A boolean specifying whether the moveListTable selection is already being
   * changed and so the listener should ignore any calls to avoid endless
   * recursion.
   */

  private boolean isMoveListTableSelectionUpdating = false;




  /**
   * Needed because JDK 1.1 doesn't allow setting the selected table cell with
   * one method (and one event fired). The selection listener ignores the call
   * when this is set to true.
   */

  private boolean settingMoveListTableSelection = false;




  /**
   * The TableModel of the JTable displaying the move list.
   */

  protected TableModel moveListTableModel;



  /**
   * The JScrollPane in which we put the moveListTable.
   */

  protected JScrollPane moveListTableScrollPane;



  /**
   * The scrollbar which lets you scroll through all the positions that occurred
   * in the game.
   */

  protected JScrollBar positionScrollBar;




  /**
   * A boolean specifying whether the positionScrollBar is being changed
   * programmatically. When it is, the method(s) listening to events from it,
   * should probably ignore them.
   */

  protected boolean isPositionScrollBarUpdating = false;




  /**
   * True when the board position is being updated programmatically (and not as
   * a result of user interaction) and thus events from it should probably be
   * ignored.
   */

  protected boolean isBoardPositionUpdating = false;




  /**
   * The Timer we use to update the clock.
   */

  private Timer timer;




  /**
   * The last time the white clock was updated.
   */

  private long lastWhiteUpdateTimestamp;




  /**
   * The last time the black clock was updated.
   */

  private long lastBlackUpdateTimestamp;




  /**
   * The move made when we've fired a UserMadeMove and it hasn't been echoed to
   * us yet. Null if none.
   */

  private Move moveEnRoute = null;




  /**
   * The queued move - the user may make a move when it's not his turn, and it
   * will then be saved here until his opponent makes a move.
   */

  private Move queuedMove = null;
  



  /**
   * Creates a new <code>BoardPanel</code> which will be used by the given
   * <code>BoardManager</code>, will display the given Game and will have the
   * given move input mode.
   */

  public BoardPanel(BoardManager boardManager, Game game){
    this.game = game;
    this.boardManager = boardManager;
    this.realPosition = game.getInitialPosition();

    boardManager.addPropertyChangeListener(this);

    init(game);
  }





  /**
   * Adds the given UserMoveListener to the list of listeners receiving events about
   * moves made on the board by the user. 
   */

  public void addUserMoveListener(UserMoveListener listener){
    listenerList.add(UserMoveListener.class, listener);
  }




  /**
   * Removes the given UserMoveListener from the list of listeners receiving events
   * about moves made by the user on the board.
   */

  public void removeUserMoveListener(UserMoveListener listener){
    listenerList.remove(UserMoveListener.class, listener); 
  }





  /**
   * Dispatches the given UserMoveEvent to all interested UserMoveListeners of this BoardPanel.
   */

  protected void fireUserMadeMove(UserMoveEvent evt){
    Object [] listenerList = this.listenerList.getListenerList();
    for (int i=0;i<listenerList.length;i+=2){
      if (listenerList[i]==UserMoveListener.class){
        UserMoveListener listener = (UserMoveListener)listenerList[i+1];
        listener.userMadeMove(evt);
      }
    }
  }




  /**
   * This method is called by the constructor, it's meant to initialize all the
   * components and add them. The default implementation calls 3 methods - 
   * createComponents(Game), createTimer(Game) and addComponents(Game, boolean).
   */

  protected void init(Game game){
    isFlipped = game.isBoardInitiallyFlipped();
    highlightOwnMoves = boardManager.isHighlightingOwnMoves();
    timer = createTimer(game);
    createComponents(game);
    
//    addComponents(game, isFlipped); 
    // We're not adding them because the layout depends on the size, which is unknown at this point.
    // See the doLayout() method.
  }






  /**
   * Creates the Timer which will be used to update the clocks.
   */

  protected Timer createTimer(Game game){
    return new Timer(100, this);
  }




  /**
   * This method is meant to create all the sub-components used by the BoardPanel.
   * The default implementation delegates the creation to the following methods:
   * <UL>
   *   <LI> {@link #createBoard(Game)}
   *   <LI> {@link #createGameLabel(Game)}
   *   <LI> {@link #createWhiteLabel(Game)}
   *   <LI> {@link #createBlackLabel(Game)}
   *   <LI> {@link #createWhiteClock(Game)}
   *   <LI> {@link #createBlackClock(Game)}
   *   <LI> {@link #createButtonPanel(Game)}
   *   <LI> {@link #createMoveListTableModel(Game)}
   *   <LI> {@link #createMoveListTable(Game, TableModel)}
   *   <LI> {@link #createPositionScrollBar(Game)}
   * </UL>
   */

  protected void createComponents(Game game){
    board = createBoard(game);
    configureBoard(game, board);
    board.getPosition().addMoveListener(this);
    gameLabel = createGameLabel(game);
    whiteLabel = createWhiteLabel(game);
    blackLabel = createBlackLabel(game);
    whiteClock = createWhiteClock(game);
    blackClock = createBlackClock(game);
    buttonPanel = createButtonPanel(game);
    moveListTableModel = createMoveListTableModel(game);
    moveListTable = createMoveListTable(game, moveListTableModel);
    moveListTableScrollPane = createMoveListTableScrollPane(game, moveListTable);

    moveListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
     
      public void valueChanged(ListSelectionEvent evt){
        moveListTableSelectionChanged();
      }
      
    });

    moveListTable.getColumnModel().addColumnModelListener(new TableColumnModelListener(){
      public void columnAdded(TableColumnModelEvent e){}
      public void columnMarginChanged(ChangeEvent e){}
      public void columnMoved(TableColumnModelEvent e){}
      public void columnRemoved(TableColumnModelEvent e){}
      public void columnSelectionChanged(ListSelectionEvent e){
        moveListTableSelectionChanged();
      }
    });

    positionScrollBar = createPositionScrollBar();
    positionScrollBar.addAdjustmentListener(this);

    updateClockActiveness();
  }




  /**
   * Creates and returns the JBoard.
   */

  protected JBoard createBoard(Game game){
    return new JinBoard(game.getInitialPosition());
  }





  /**
   * Configures the board.
   */

  protected void configureBoard(Game game, JBoard board){
    int moveInputMode;
    if (game.getGameType() == Game.MY_GAME)
      if (game.isPlayed())
        if (game.getUserPlayer() == Player.WHITE_PLAYER)
          moveInputMode = JBoard.WHITE_PIECES_MOVE;
        else
          moveInputMode = JBoard.BLACK_PIECES_MOVE;
      else 
        moveInputMode = JBoard.CURRENT_PLAYER_MOVES; 
    else // This counts for both ISOLATED_BOARD and OBSERVED_GAME.
      moveInputMode = JBoard.NO_PIECES_MOVE;

    board.setMoveInputMode(moveInputMode);

    if (isFlipped())
      board.setFlipped(true);

    board.setPiecePainter(boardManager.getPiecePainter());
    board.setBoardPainter(boardManager.getBoardPainter());
    board.setMoveInputStyle(boardManager.getMoveInputStyle());
    board.setDraggedPieceStyle(boardManager.getDraggedPieceStyle());
    board.setMoveHighlightingStyle(boardManager.getMoveHighlightingStyle());
    board.setManualPromote(!boardManager.isAutoPromote());
    board.setMoveHighlightingColor(boardManager.getMoveHighlightingColor());
    board.setDragSquareHighlightingColor(boardManager.getDragSquareHighlightingColor());


    ActionListener escapeListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        JBoard board = BoardPanel.this.board;
        if (board.isMovingPiece())
          board.cancelMovingPiece();
        else if (queuedMove != null){
          setQueuedMove(null);
          isBoardPositionUpdating = true;
          board.getPosition().copyFrom(realPosition);
          if (isMoveEnRoute())
            board.getPosition().makeMove(moveEnRoute);
          isBoardPositionUpdating = false;
        }
      }
    };

    // We have no choice but to use WHEN_FOCUSED and to give the focus to the
    // JBoard when it is pressed because WHEN_IN_FOCUSED_WINDOW doesn't work
    // in MS VM.
    board.registerKeyboardAction(escapeListener,
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, MouseEvent.BUTTON1_MASK), JComponent.WHEN_FOCUSED);
    board.registerKeyboardAction(escapeListener, 
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

    board.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent evt){
        if (!BoardPanel.this.board.hasFocus())
          BoardPanel.this.board.requestFocus();
      }
    });
  }




  /**
   * Creates the JLabel displaying information about the game.
   */

  protected JLabel createGameLabel(Game game){
    JLabel gameLabel = new JLabel((game.isRated() ? "Rated" : "Unrated") + " " + game.getTCString()+ " " + game.getVariant().getName());
    gameLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
    return gameLabel;
  }




  /**
   * Creates the JLabel displaying information about the player with the white
   * pieces.
   */

  protected JLabel createWhiteLabel(Game game){
    String title = game.getWhiteTitles();
    JLabel whiteLabel = new JLabel(game.getWhiteName()+title+" "+game.getWhiteRating());
    whiteLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    return whiteLabel;
  }





  /**
   * Creates the JLabel displaying information about the player with the black
   * pieces.
   */

  protected JLabel createBlackLabel(Game game){
    String title = game.getBlackTitles();
    JLabel blackLabel = new JLabel(game.getBlackName()+title+" "+game.getBlackRating());
    blackLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    return blackLabel;
  }





  /**
   * Creates the AbstractChessClock which will display the amount of time remaining
   * on white's clock.
   */

  protected AbstractChessClock createWhiteClock(Game game){
    return new JChessClock(game.getWhiteTime());
  }




  /**
   * Creates the AbstractChessClock which will display the amount of time remaining
   * on black's clock.
   */

  protected AbstractChessClock createBlackClock(Game game){
    return new JChessClock(game.getBlackTime());
  }




  /**
   * Creates the button panel - a panel with various action buttons on it.
   * If the given Game is not of type Game.MY_GAME, this method returns null.
   * If the given Game is a played game, the actual creation is delegated to the
   * {@link #createPlayedButtonPanel(Game)} method, otherwise (it's an examined game)
   * the creation is delegated to the {@link #createExaminedButtonPanel(Game)} method.
   */

  protected JPanel createButtonPanel(Game game){
    JPanel buttonPanel;
    if (game.getGameType() != Game.MY_GAME)
      return null;
    else if (game.isPlayed())
      buttonPanel = new PlayedGameButtonPanel(boardManager, game, this);
    else
      buttonPanel = new ExaminedGameButtonPanel(boardManager, game);

    return buttonPanel;
  }




  /**
   * Creates the TableModel for the JTable which will be used for displaying
   * the move list. If you override this method, you should also see if you need
   * to override {@link #addMoveToListTable(Move)} and {@link #updateMoveListTable()}.
   */

  protected TableModel createMoveListTableModel(Game game){
    return new NonEditableTableModel(new String[]{"Move No.", "White", "Black"}, 0);
  }




  /**
   * Creates the JTable which will display the move list. The JTable must be
   * created with the given TableModel.
   */

  protected JTable createMoveListTable(Game game, TableModel moveListTableModel){
    JTable table = new FixedJTable(moveListTableModel);
    table.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    table.setCellSelectionEnabled(true);
    table.getTableHeader().setPreferredSize(new Dimension(150, 18));
    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    return table;
  }




  /**
   * Creates the JScrollPane in which we put the moveListTable.
   */

  protected JScrollPane createMoveListTableScrollPane(Game game, JTable moveListTable){
    JScrollPane scrollPane = new JScrollPane(moveListTable);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    return scrollPane;
  }




  /**
   * A Runnable we SwingUtilities.invokeLater() to set the move list table vertical
   * scrollbar value.
   */

  private class MoveListScrollBarUpdater implements Runnable{
    public void run(){
      JScrollBar vScroller = moveListTableScrollPane.getVerticalScrollBar();
      int selectedRow = moveListTable.getSelectedRow();
      int selectedColumn = moveListTable.getSelectedColumn();
      if ((selectedRow==-1)||(selectedColumn==-1))
        if (displayedMoveNumber==0)
          vScroller.setValue(vScroller.getMinimum());
        else
          vScroller.setValue(vScroller.getMaximum());
      else{
        moveListTable.scrollRectToVisible(moveListTable.getCellRect(selectedRow, selectedColumn, true));
      }
    }
  }





  /**
   * Adds a single move to the move list TableModel.
   */

  protected void addMoveToListTable(Move move){
    DefaultTableModel model = (DefaultTableModel)moveListTableModel;
    int rowCount = moveListTable.getRowCount();
    if ((rowCount == 0) || move.getPlayer().isWhite()){
      model.setNumRows(++rowCount);
      model.setValueAt(rowCount+".", rowCount-1, 0);
    }
    if (move.getPlayer().isWhite())
      model.setValueAt(move, rowCount-1, 1);
    else{
      model.setValueAt(move, rowCount-1, 2);
    }

    if (displayedMoveNumber == 0){
      moveListTable.clearSelection();
      positionScrollBar.setValues(0, 1, 0, madeMoves.size() + 1); 
    }
    else{
      boolean isFirstMoveBlack = ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
      int visualMoveNumber = isFirstMoveBlack ? displayedMoveNumber + 1 : displayedMoveNumber;
      int row = (visualMoveNumber - 1) / 2;
      int column = 2 - (visualMoveNumber%2);

      isPositionScrollBarUpdating = true;
      isMoveListTableSelectionUpdating = true;
      setMoveListTableSelection(row, column);
      positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
      isMoveListTableSelectionUpdating = false;
      isPositionScrollBarUpdating = false;
    }

    SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
  }



  /**
   * Brings the the move list table up to date with the current move list and
   * displayed position.
   */

  protected void updateMoveListTable(){
    DefaultTableModel model = (DefaultTableModel)moveListTableModel;
    int moveCount = madeMoves.size();
    boolean isFirstMoveBlack = (moveCount > 0) && ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
    int numRows = isFirstMoveBlack ? 1+moveCount/2 : (moveCount+1)/2;
    model.setNumRows(numRows);
    for (int i = 0; i < numRows; i++)
      model.setValueAt((i+1)+".", i, 0);

    int row = 0;
    int column = isFirstMoveBlack ? 2 : 1;
    for (int i = 0; i < moveCount; i++){
      Object move = madeMoves.elementAt(i);
      model.setValueAt(move, row, column);
      column++;
      if (column == 3){
        row++;
        column = 1;
      }
    }

    if (column == 2) // There's an extra, empty cell, which we need to clear.
      model.setValueAt(null, row, column);


    if (displayedMoveNumber == 0){
      moveListTable.clearSelection();
      positionScrollBar.setValues(0, 1, 0, madeMoves.size() + 1);
    }
    else{
      int visualMoveNumber = isFirstMoveBlack ? displayedMoveNumber + 1 : displayedMoveNumber;
      row = (visualMoveNumber - 1) / 2;
      column = 2 - (visualMoveNumber%2);

      isPositionScrollBarUpdating = true;
      isMoveListTableSelectionUpdating = true;
      setMoveListTableSelection(row, column);
      positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
      isMoveListTableSelectionUpdating = false;
      isPositionScrollBarUpdating = false;
    }

    SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
  }




  /**
   * Sets the specified cell to be the selected cell in the move list table.
   */

  protected void setMoveListTableSelection(int row, int column){
    int oldRow = moveListTable.getSelectedRow();
    int oldColumn = moveListTable.getSelectedColumn();

    if (oldRow != row){
      settingMoveListTableSelection = true;
      moveListTable.setColumnSelectionInterval(column, column);
      settingMoveListTableSelection = false;
      moveListTable.setRowSelectionInterval(row, row);
    }
    else if (oldColumn != column){
      settingMoveListTableSelection = true;
      moveListTable.setRowSelectionInterval(row, row);
      settingMoveListTableSelection = false;
      moveListTable.setColumnSelectionInterval(column, column);
    }
  }





  /**
   * Updates the move highlighting on the board. The boolean argument specifies
   * whether the last made move (on the board, not the real position) is a move
   * made by the user and should therefore be ignored if
   * isHighlightingOwnMoves() returns false. Note that the callers don't 
   * really determine whether the last move is the user's - they only pass
   * true when it's a move that has just been made by the user.
   */

  private void updateMoveHighlighting(boolean isOwnMove){
    if ((displayedMoveNumber == 0) || (isOwnMove && !highlightOwnMoves))
      board.setHighlightedMove(null);
    else{
      Move move = (Move)madeMoves.elementAt(displayedMoveNumber - 1);
      board.setHighlightedMove(move);
    }
  }




  /**
   * Creates the JScrollBar which controls the position displayed on the board.
   */

  protected JScrollBar createPositionScrollBar(){
    JScrollBar scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);
    return scrollbar;
  }



  /**
   * True when we need to re-add all the components.
   */

  private boolean reAddComponents = true;




  /**
   * Are we currently in vertical layout (width < height)? null for when we
   * don't know yet.
   */

  private boolean isVerticalLayout;



  /**
   * The panel containing all the components displaying information about the
   * game (horizontal mode).
   */

  private JPanel infoBox;



  /**
   * The top info panel in vertical mode.
   */

  private JPanel topInfoBox;



  /**
   * The bottom info panel in vertical mode.
   */

  private JPanel bottomInfoBox;




  /**
   * Adds all the components created by {@link #createComponents(Game)} to this
   * BoardPanel. The isFlipped flag specifies if the layout of the components
   * should be flipped because black should be displayed at the bottom. This method
   * can be called many times because the user may want to flip the board, so
   * it shouldn't do any one time initializations.
   */

  protected void addComponents(Game game, boolean flipped){
    setLayout(null); // See the doLayout() method

    add(board);

    if (isVerticalLayout){
      topInfoBox = new JPanel();
      topInfoBox.setBorder(new EmptyBorder(5,5,5,5));
      topInfoBox.setLayout(new BoxLayout(topInfoBox, BoxLayout.X_AXIS));

      bottomInfoBox = new JPanel();
      bottomInfoBox.setBorder(new EmptyBorder(5,5,5,5));
      bottomInfoBox.setLayout(new BoxLayout(bottomInfoBox, BoxLayout.X_AXIS));

      whiteClock.setMaximumSize(whiteClock.getPreferredSize());
      blackClock.setMaximumSize(blackClock.getPreferredSize());

      int labelWidth = Math.max(whiteLabel.getPreferredSize().width,
                                  blackLabel.getPreferredSize().width);
      int labelHeight = Math.max(whiteLabel.getPreferredSize().height,
                                  blackLabel.getPreferredSize().height);
      whiteLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));
      blackLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));
      whiteLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
      blackLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));


      if (flipped){
        whiteLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        whiteClock.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        blackLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        blackClock.setAlignmentY(Component.TOP_ALIGNMENT);

        topInfoBox.add(whiteLabel);
        topInfoBox.add(Box.createHorizontalGlue());
        topInfoBox.add(whiteClock);

        bottomInfoBox.add(blackLabel);
        bottomInfoBox.add(Box.createHorizontalGlue());
        bottomInfoBox.add(blackClock);
      }
      else{
        whiteLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        whiteClock.setAlignmentY(Component.TOP_ALIGNMENT);

        blackLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        blackClock.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        topInfoBox.add(blackLabel);
        topInfoBox.add(Box.createHorizontalGlue());
        topInfoBox.add(blackClock);

        bottomInfoBox.add(whiteLabel);
        bottomInfoBox.add(Box.createHorizontalGlue());
        bottomInfoBox.add(whiteClock);
      }

      add(topInfoBox);
      add(bottomInfoBox);
    }
    else{
      whiteClock.setMaximumSize(new Dimension(Integer.MAX_VALUE, whiteClock.getPreferredSize().height));
      blackClock.setMaximumSize(new Dimension(Integer.MAX_VALUE, blackClock.getPreferredSize().height));

      Box whiteLabelBox = Box.createHorizontalBox();
      whiteLabelBox.add(whiteLabel);
      whiteLabelBox.add(Box.createHorizontalGlue());

      Box blackLabelBox = Box.createHorizontalBox();
      blackLabelBox.add(blackLabel);
      blackLabelBox.add(Box.createHorizontalGlue());

      Box gameLabelBox = Box.createHorizontalBox();
      gameLabelBox.add(gameLabel);
      gameLabelBox.add(Box.createHorizontalGlue());

      JPanel upperBox = new JPanel(null);
      upperBox.setLayout(new BoxLayout(upperBox, BoxLayout.Y_AXIS));
      JPanel middleBox = new JPanel(null);
      middleBox.setLayout(new BoxLayout(middleBox, BoxLayout.Y_AXIS));
      JPanel bottomBox = new JPanel(null);
      bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));

      if (flipped){
        upperBox.add(whiteLabelBox);
        upperBox.add(Box.createVerticalStrut(10));
        upperBox.add(whiteClock);
        middleBox.add(Box.createVerticalStrut(10));
        middleBox.add(gameLabelBox);
        middleBox.add(Box.createVerticalStrut(10));
        middleBox.add(positionScrollBar);
        middleBox.add(Box.createVerticalStrut(5));
        middleBox.add(moveListTableScrollPane);
        middleBox.add(Box.createVerticalStrut(10));
        if (buttonPanel != null){
          middleBox.add(buttonPanel);
          middleBox.add(Box.createVerticalStrut(10));
        }
        bottomBox.add(blackClock);
        bottomBox.add(Box.createVerticalStrut(10));
        bottomBox.add(blackLabelBox);
      }
      else{
        upperBox.add(blackLabelBox);
        upperBox.add(Box.createVerticalStrut(10));
        upperBox.add(blackClock);
        middleBox.add(Box.createVerticalStrut(10));
        middleBox.add(gameLabelBox);
        middleBox.add(Box.createVerticalStrut(10));
        middleBox.add(positionScrollBar);
        middleBox.add(Box.createVerticalStrut(10));
        middleBox.add(moveListTableScrollPane);
        middleBox.add(Box.createVerticalStrut(10));
        if (buttonPanel != null){
          middleBox.add(buttonPanel);
          middleBox.add(Box.createVerticalStrut(10));
        }
        bottomBox.add(whiteClock);
        bottomBox.add(Box.createVerticalStrut(10));
        bottomBox.add(whiteLabelBox);
      }
      infoBox = new JPanel(null);
      infoBox.setBorder(new EmptyBorder(5, 5, 5, 5));
      infoBox.setLayout(new BorderLayout());

      infoBox.add(upperBox, BorderLayout.NORTH);
      infoBox.add(middleBox, BorderLayout.CENTER);
      infoBox.add(bottomBox, BorderLayout.SOUTH);

      add(infoBox);
    }

  }



  /**
   * Sets reAddComponents to true.
   */

  public void addNotify(){
    reAddComponents = true;

    super.addNotify();
  }



  /**
   * Lays out this BoardPanel.
   */

  public void doLayout(){
    Dimension size = getSize();

    boolean newIsVerticalLayout = size.width < size.height;
    if (reAddComponents || (isVerticalLayout != newIsVerticalLayout)){
      removeAll();
      reAddComponents = false;
      isVerticalLayout = newIsVerticalLayout;
      addComponents(game, isFlipped());
    }

    if (isVerticalLayout){
      int infoBoxHeight = (size.height - size.width) / 2;
      topInfoBox.setBounds(0, 0, size.width, infoBoxHeight);
      board.setBounds(0, infoBoxHeight, size.width, size.width);
      bottomInfoBox.setBounds(0, size.height - infoBoxHeight, size.width, infoBoxHeight);
    }
    else{
      board.setBounds(0, 0, size.height, size.height);
      infoBox.setBounds(size.height, 0, size.width-size.height, size.height);
    }
  }




  /**
   * Returns true if the BoardPanel is flipped (black at bottom).
   */

  public final boolean isFlipped(){
    // Final because we directly set the isFlipped *variable* in init(Game) and
    // thus must not let anyone redefine the meaning of "flipped".

    return isFlipped;
  }




  /**
   * Sets the flipped state of this BoardPanel.
   */

  public void setFlipped(boolean b){
    if (isFlipped() != b){
      isFlipped = b;
      board.setFlipped(isFlipped);
      reAddComponents = true;
      revalidate();
    }
  }



  /**
   * Returns the Game displayed by this BoardPanel.
   */

  public Game getGame(){
    return game;
  }




  /**
   * Returns the title the parent frame or internal frame of this BoardPanel
   * should have. Whoever adds the BoardPanel may consult with this method.
   */

  public String getTitle(){
    if (isActive()){
      if (game.getGameType()==Game.MY_GAME){
        if (game.isPlayed())
          return game.toString();
        else
          return "Examining "+game.toString();
      }
      else
        return "Observing "+game.toString();
    }
    else
      return "Was "+game.toString();
  }




  /**
   * Updates the clock's activeness according to the current player to move on the
   * board.
   */

  protected void updateClockActiveness(){
    if (board.getPosition().getCurrentPlayer().equals(Player.WHITE_PLAYER)){
      whiteClock.setActive(true);
      blackClock.setActive(false);
    }
    else{
      blackClock.setActive(true);
      whiteClock.setActive(false);
    }
  }




  /**
   * Returns the SoundManager plugin this BoardPanel should use to produce game
   * related sounds, or <code>null</code> if none should be used.
   */

  private SoundManager getSoundManager(){
    String pluginName = boardManager.getProperty("sound-manager-plugin.name");
    if (pluginName == null)
      return null;

    return (SoundManager)boardManager.getPluginContext().getPlugin(pluginName);
  }




  /**
   * Plays the sound for the given event name.
   */

  protected void playSound(String eventName){
    SoundManager soundManager = getSoundManager();
    if (soundManager == null)
      return;

    soundManager.playEventSound(eventName);
  }




  /**
   * Returns if there is currently a move enRoute. This simply checks that the
   * <code>moveInRoute</code> variable is non-null.
   */

  private boolean isMoveEnRoute(){
    return moveEnRoute != null;
  }




  /**
   * Sets the queued move to the specified Move. Null is a valid value, which
   * clears the queued move.
   */

  private void setQueuedMove(Move move){
    if (queuedMove != null)
      board.setShaded(queuedMove.getEndingSquare(), false);

    queuedMove = move;
    if (queuedMove != null)
      board.setShaded(queuedMove.getEndingSquare(), true);

    board.setEditable((queuedMove == null) && (displayedMoveNumber == madeMoves.size()));
  }




  /**
   * Sets the currently displayed move to the specified move.
   */

  private void setDisplayedMove(int moveNum){
    if ((moveNum < 0) || (moveNum > madeMoves.size()))
      throw new IllegalArgumentException("displayed move number out of range");

    displayedMoveNumber = moveNum;

    board.setEditable((queuedMove == null) && (displayedMoveNumber == madeMoves.size()));
  }




  /**
   * GameListener implementation. 
   */

  public void gameStarted(GameStartEvent evt){
    if (evt.getGame() != game)
      return;
  }



  /**
   * GameListener implementation. Makes the appropriate move on the board.
   */

  public void moveMade(MoveMadeEvent evt){
    if (evt.getGame() != game)
      return;

    Move move = evt.getMove();

    // If for example, the user is observing a game, and is looking at a
    // position other than the last one, we don't want to update the board when
    // a new move arrives.
    boolean shouldUpdateBoard = true; 

    if (displayedMoveNumber != madeMoves.size())
      shouldUpdateBoard = false;

    madeMoves.addElement(move);
    realPosition.makeMove(move);

    if (!isMoveEnRoute()){ // This is not the server echoeing our own move
      if (evt.isNew())
        playAudioClipForMove(move);

      if (shouldUpdateBoard){
        isBoardPositionUpdating = true;
        board.getPosition().copyFrom(realPosition);
        isBoardPositionUpdating = false;
      }
    }

    if (shouldUpdateBoard){
      setDisplayedMove(madeMoves.size());
      updateMoveHighlighting(isMoveEnRoute());
    }

    // queuedMove.getPlayer() == realPosition.getCurrentPlayer() makes sure
    // that we only send the queued on the correct move, not when getting *any* response
    if ((queuedMove != null) && (queuedMove.getPlayer() == realPosition.getCurrentPlayer())){ 
      UserMoveEvent evt2 = new UserMoveEvent(this, queuedMove);
      isBoardPositionUpdating = true;
      board.getPosition().copyFrom(realPosition);
      board.getPosition().makeMove(queuedMove);
      isBoardPositionUpdating = false;
      moveEnRoute = queuedMove;
      setQueuedMove(null);
      fireUserMadeMove(evt2);
    }
    else
      moveEnRoute = null;

    timer.stop();
    updateClockActiveness();
    addMoveToListTable(move);
  }




  /**
   * GameListener implementation. Sets the appropriate position on the board.
   */

  public void positionChanged(PositionChangedEvent evt){
    if (evt.getGame() != game)
      return;

    madeMoves.removeAllElements();
    realPosition.copyFrom(evt.getPosition());

    isBoardPositionUpdating = true;
    board.getPosition().copyFrom(realPosition);
    isBoardPositionUpdating = false;

    setDisplayedMove(0);

    updateMoveHighlighting(false);

    moveEnRoute = null;      // We shouldn't keep state between 
    setQueuedMove(null);     // such drastic position changes

    updateClockActiveness();
    updateMoveListTable();
  }




  /**
   * GameListener implementation. Reverts the position on the board to the position
   * that occured on it "the amount of moves taken back" moves ago.
   */

  public void takebackOccurred(TakebackEvent evt){
    if (evt.getGame()!=game)
      return;

    int takebackCount = evt.getTakebackCount();
    int numMadeMoves = madeMoves.size()-takebackCount;
    for (int i = madeMoves.size()-1; i >= numMadeMoves; i--)
      madeMoves.removeElementAt(i);

    realPosition.copyFrom(game.getInitialPosition());
    for (int i = 0; i < numMadeMoves; i++)
      realPosition.makeMove((Move)madeMoves.elementAt(i));

    moveEnRoute = null;
    setQueuedMove(null);

    // Try not to change the board if possible. If, however we were displaying the position
    // after a move that was taken back, we have to update the board.
    if (displayedMoveNumber >= madeMoves.size()){
      isBoardPositionUpdating = true;
      board.getPosition().copyFrom(realPosition);
      isBoardPositionUpdating = false;
      setDisplayedMove(madeMoves.size());
      updateMoveHighlighting(false);
    }

    updateClockActiveness();
    updateMoveListTable();
  }




  /**
   * GameListener implementation. Returns the position on the board to its current
   * "real" state.
   */

  public void illegalMoveAttempted(IllegalMoveEvent evt){
    if (evt.getGame() != game)
      return;

    if (!isMoveEnRoute()) // We didn't make this move. 
      return;             // It could've been sent by one of the other plugins.

    moveEnRoute = null;
    setQueuedMove(null);

    isBoardPositionUpdating = true;
    board.getPosition().copyFrom(realPosition);
    isBoardPositionUpdating = false;

    updateClockActiveness();
    setDisplayedMove(madeMoves.size());
    updateMoveListTable();
    timer.start();
  }




  /**
   * GameListener implementation. Adjusts the clock labels and the timer
   * appropriately.
   */

  public void clockAdjusted(ClockAdjustmentEvent evt){
    if (evt.getGame() != game)
      return;

    Player player = evt.getPlayer();
    int time = evt.getTime();
    boolean isRunning = evt.isClockRunning();

    if (player.equals(Player.WHITE_PLAYER)){
      whiteClock.setTime(time);
      lastWhiteUpdateTimestamp = System.currentTimeMillis();
    }
    else{
      blackClock.setTime(time);
      lastBlackUpdateTimestamp = System.currentTimeMillis();
    }

    if (isRunning){
      if (!timer.isRunning()){
        int delay = time%timer.getDelay();
        if (delay < 0)
          delay += timer.getDelay();
        
        timer.setInitialDelay(delay);
        timer.start();
      }
    }
    else{
      if (timer.isRunning()&&realPosition.getCurrentPlayer().equals(player)){
        timer.stop();
      }
    }
  }




  /**
   * GameListener implementation.
   */

  public void boardFlipped(BoardFlipEvent evt){
    if (evt.getGame()!=game)
      return;

    setFlipped(evt.isFlipped());
  }




  /**
   * GameListener implementation.
   */

  public void gameEnded(GameEndEvent evt){
    if (evt.getGame()!=game)
      return;

    if (timer.isRunning())
      timer.stop();

    setInactive();
  }





  /**
   * Plays the audio clip appropriate for the given <code>Move</code>.
   */

  protected void playAudioClipForMove(Move move){
    if (move instanceof ChessMove){
      ChessMove cMove = (ChessMove)move;
      if (cMove.isCapture())
        playSound("Capture");
      else if (cMove.isCastling())
        playSound("Castling");
      else
        playSound("Move");
    }
    else
      playSound("Move");
  }





  /**
   * This is called when the user makes a move on the board (MoveListener implementation).
   */

  public void moveMade(MoveEvent evt){
    if (isBoardPositionUpdating)
      return;

    Position source = evt.getPosition();
    Move move = evt.getMove();

    if (source == board.getPosition()){
      playAudioClipForMove(move);
      if (isMoveEnRoute() || !isUserTurn())
        setQueuedMove(move);
      else{
        UserMoveEvent evt2 = new UserMoveEvent(this, evt.getMove());
        fireUserMadeMove(evt2);
        moveEnRoute = evt.getMove();
        timer.stop();
        updateClockActiveness();
      }
    }
  }




  /**
   * Returns true if this is currently the user's turn to move. If this is an
   * observed game, returns false. If this is a game examined by the user returns
   * true. If this is a game played by the user returns whether it's currently
   * the user's turn.
   */

  protected boolean isUserTurn(){
    if (game.getGameType() == Game.OBSERVED_GAME)
      return false;
    else if (game.getGameType() == Game.ISOLATED_BOARD)
      return false;
    else{ // MY_GAME
      if (game.isPlayed()){
        Player userPlayer = game.getUserPlayer();
        return realPosition.getCurrentPlayer().equals(userPlayer);
      }
      else
        return true;
    }
  }





  /**
   * ActionListener implementation. This is called by the Timer and updates the
   * clocks as necessary.
   */

  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();

    if (source == timer){
      if (!timer.isRunning())
        return; // These are residue from invokeLater calls done by the Timer before it was stopped :-)
      Player curPlayer = realPosition.getCurrentPlayer();
      AbstractChessClock clockToUpdate = curPlayer.equals(Player.WHITE_PLAYER) ? whiteClock : blackClock;
      long lastUpdateTimestamp = (clockToUpdate == whiteClock ? lastWhiteUpdateTimestamp : lastBlackUpdateTimestamp);

      long now = System.currentTimeMillis();
      clockToUpdate.setTime((int)(clockToUpdate.getTime()-(now-lastUpdateTimestamp)));
      if (clockToUpdate == whiteClock)
        lastWhiteUpdateTimestamp = now;
      else
        lastBlackUpdateTimestamp = now;
    }
  }




  /**
   * This method is called when either the row or column selection in the moveListTable
   * changes. Changes the board position to the position after the selected move.
   */

  protected void moveListTableSelectionChanged(){
    if (settingMoveListTableSelection)
      return;

    if (isMoveListTableSelectionUpdating)
      return;

    isMoveListTableSelectionUpdating = true;

    int row = moveListTable.getSelectedRow();
    int column = moveListTable.getSelectedColumn();

    if ((row == -1) || (column == -1))
      return;

    boolean isFirstMoveBlack = ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();

    int moveNum = column + row*2;
    if (isFirstMoveBlack && (moveNum > 0))
      moveNum--;

    if (moveNum == madeMoves.size() + 1) // The user pressed the last empty cell
      moveNum--;

    if (moveNum > madeMoves.size()) // Shouldn't happen
      throw new IllegalStateException();

    Position pos = game.getInitialPosition();
    for (int i = 0; i < moveNum; i++){
      Move move = (Move)madeMoves.elementAt(i);
      pos.makeMove(move);
    }

    board.clearShaded();
    
    if ((moveNum == madeMoves.size()) && (queuedMove != null)){
      pos.makeMove(queuedMove);
      board.setShaded(queuedMove.getEndingSquare(), true);
    }

    isBoardPositionUpdating = true;
    board.getPosition().copyFrom(pos);
    isBoardPositionUpdating = false;
    setDisplayedMove(moveNum);

    if (!isPositionScrollBarUpdating)
      positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 

    board.setEditable(displayedMoveNumber == madeMoves.size());

    updateMoveHighlighting(false);

    SwingUtilities.invokeLater(new MoveListScrollBarUpdater());

    isMoveListTableSelectionUpdating = false;
  }



  /**
   * AdjustmentListener implementation. Listens to events from the positionScrollBar
   * and updates the position on the board accordingly.
   */

  public void adjustmentValueChanged(AdjustmentEvent evt){
    Object source = evt.getSource();
    if (source == positionScrollBar){
      if (isPositionScrollBarUpdating)
        return;

       isPositionScrollBarUpdating = true;

      int moveNum = positionScrollBar.getValue();

      boolean isFirstMoveBlack = ((Move)madeMoves.elementAt(0)).getPlayer().isBlack();
      int visualMoveNumber = isFirstMoveBlack ? moveNum + 1 : moveNum;
      int row = (visualMoveNumber-1)/2;
      int column = (visualMoveNumber == 0) ? 0 : 2 - (visualMoveNumber%2);

      if (!isMoveListTableSelectionUpdating){
        setMoveListTableSelection(row, column);
      }

      isPositionScrollBarUpdating = false;
    }
  }




  /**
   * <code>PropertyChangeListener</code> implementation.
   */

  public void propertyChange(PropertyChangeEvent evt){
    Object src = evt.getSource();
    String propertyName = evt.getPropertyName();
    if (src == boardManager){
      if ("piecePainter".equals(propertyName))
        board.setPiecePainter(boardManager.getPiecePainter());
      else if ("boardPainter".equals(propertyName))
        board.setBoardPainter(boardManager.getBoardPainter());
      else if ("moveInputStyle".equals(propertyName))
        board.setMoveInputStyle(boardManager.getMoveInputStyle());
      else if ("draggedPieceStyle".equals(propertyName))
        board.setDraggedPieceStyle(boardManager.getDraggedPieceStyle());
      else if ("moveHighlightingStyle".equals(propertyName))
        board.setMoveHighlightingStyle(boardManager.getMoveHighlightingStyle());
      else if ("autoPromote".equals(propertyName))
        board.setManualPromote(!boardManager.isAutoPromote());
      else if ("moveHighlightingColor".equals(propertyName))
        board.setMoveHighlightingColor(boardManager.getMoveHighlightingColor());
      else if ("dragSquareHighlightingColor".equals(propertyName))
        board.setDragSquareHighlightingColor(boardManager.getDragSquareHighlightingColor());
      else if ("highlightingOwnMoves".equals(propertyName))
        highlightOwnMoves = boardManager.isHighlightingOwnMoves();
    }
  }




  /**
   * Returns true if this BoardPanel is active, false otherwise.
   */

  public boolean isActive(){
    return isActive;
  }




  /**
   * Makes the BoardPanel inactive. Calling this method will make the BoardPanel
   * stop notifying about moves made by the user. It will also change the
   * move input mode to {@link #ALL_PIECES_MOVE}.
   */

  public void setInactive(){
    this.isActive = false;
    board.getPosition().removeMoveListener(this);
    board.setMoveInputMode(JBoard.ALL_PIECES_MOVE);
  }



  /**
   * This method is called by the <code>BoardManager</code> when this
   * <code>BoardPanel</code> is no longer required.
   */

  public void done(){
    boardManager.removePropertyChangeListener(this); 
  }



}