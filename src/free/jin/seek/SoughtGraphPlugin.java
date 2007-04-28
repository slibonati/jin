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

package free.jin.seek;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.MatchConnection;
import free.jin.Preferences;
import free.jin.Seek;
import free.jin.SeekConnection;
import free.jin.ServerUser;
import free.jin.action.JinAction;
import free.jin.event.ConnectionListener;
import free.jin.event.SeekEvent;
import free.jin.event.SeekListener;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginContext;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.plugin.PluginUIListener;
import free.jin.seek.event.SeekSelectionEvent;
import free.jin.seek.event.SeekSelectionListener;
import free.jin.ui.UIProvider;
import free.util.AWTUtilities;


/**
 * The plugin which implements the SoughtGraph. Even though I haven't put this
 * plugin in one of the server specific packages, it's pretty specific to the
 * ICS derived servers (ICC, FICS, chess.net).
 */

public class SoughtGraphPlugin extends Plugin implements SeekListener, SeekSelectionListener,
    PluginUIListener, ConnectionListener{
  
  
  
  /**
   * The panel for issuing seeks.
   */
  
  private IssueSeekPanel issueSeekPanel;
  
  
  
  /**
   * The panel for issuing match offers. May be <code>null</code> if there is
   * no such panel (if, for example, the connection is not an instance of
   * <code>MatchConnection</code>).
   */
  
  private IssueMatchPanel issueMatchPanel;
  
  
  
  /**
   * The tabbed pane holding the issue seek and match panels. May be
   * <code>null</code> if there is no issueMatchPanel.
   */
  
  private JTabbedPane issueTabbedPane;
  
  
  
  /**
   * The seek graph.
   */

  private SoughtGraph soughtGraph;
  
  
  
  /**
   * The container of our UI.
   */
  
  private PluginUIContainer uiContainer;
  
  
  
  /**
   * Sets the plugin context - return <code>false</code> if the connection is
   * not an instance of <code>SeekConnection</code>.
   */

  public boolean setContext(PluginContext context){
    if (!(context.getConnection() instanceof SeekConnection))
      return false;

    return super.setContext(context);
  }
  
  
  
  /**
   * Returns the connection to the server, cast to a
   * <code>SeekConnection</code>.
   */
  
  private SeekConnection getSeekConn(){
    return (SeekConnection)getConn();
  }


 
  /**
   * Starts this plugin.
   */

  public void start(){
    createUI();
    registerListeners();
    exportAction(new FindGameAction());
  }



  /**
   * Stops this plugin.
   */

  public void stop(){
    unregisterListeners();
    savePrefs();
  }
  
  
  
  /**
   * Saves the plugin's preferences.
   */
  
  private void savePrefs(){
    issueSeekPanel.savePrefs();
    
    if (issueMatchPanel != null){
      issueMatchPanel.savePrefs();
      
      Preferences prefs = getPrefs();
      prefs.setString("visibleIssuePanel", issueSeekPanel.isVisible() ? "seek" : "match");
    }
  }



  /**
   * Creates the UI.
   */

  protected void createUI(){
    I18n i18n = getI18n();
    
    int xGap = 10;
    int yGap = 10;
    
    uiContainer = createContainer("", UIProvider.HIDEABLE_CONTAINER_MODE);
    uiContainer.setTitle(i18n.getString("uiContainerTitle"));

    URL iconImageURL = SoughtGraphPlugin.class.getResource("icon.gif");
    if (iconImageURL!= null)
      uiContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    uiContainer.addPluginUIListener(this);

    issueSeekPanel = createIssueSeekPanel();
    issueMatchPanel = createIssueMatchPanel();
    soughtGraph = new SoughtGraph(this);
    
    issueSeekPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    if (issueMatchPanel != null)
      issueMatchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JComponent issuePanel;
    
    JLabel soughtGraphLabel = i18n.createLabel("soughtGraphLabel");
    soughtGraphLabel.setFont(soughtGraphLabel.getFont().deriveFont(Font.BOLD));
    
    if (issueMatchPanel != null){
      issueTabbedPane = new JTabbedPane(JTabbedPane.TOP);
      issueTabbedPane.addTab(i18n.getString("issueSeekTab.text"), issueSeekPanel);
      issueTabbedPane.addTab(i18n.getString("issueMatchTab.text"), issueMatchPanel);
      issueTabbedPane.setSelectedComponent(
          getPrefs().getString("visibleIssuePanel", "seek").equals("seek") ?
              (Component)issueSeekPanel : (Component)issueMatchPanel);
      
      issueTabbedPane.getModel().addChangeListener(new ChangeListener(){
        public void stateChanged(ChangeEvent e){
          if (!uiContainer.isVisible())
            return;
          
          if (!AWTUtilities.fitsInto(issueTabbedPane.getMinimumSize(), issueTabbedPane.getSize()))
            uiContainer.pack();
        }
      });
      
      issuePanel = issueTabbedPane;
    }
    else{
      JLabel issueSeekLabel = i18n.createLabel("issueSeekLabel");
      issueSeekLabel.setFont(issueSeekLabel.getFont().deriveFont(Font.BOLD));

      issuePanel = new JPanel();
      issuePanel.setLayout(new BorderLayout(xGap, yGap));
      issuePanel.add(issueSeekLabel, BorderLayout.PAGE_START);
      issuePanel.add(issueSeekPanel, BorderLayout.CENTER);
    }
    
    JPanel soughtGraphWrapper = new JPanel(new BorderLayout(xGap, yGap)){
      public Dimension getPreferredSize(){
        // The sought graph doesn't really have a preferred size, as much as a
        // preferred width:height ratio
        int height = Math.max(issueTabbedPane.getPreferredSize().height, getMinimumSize().height);
        int width = (int)(1.5*height);
        
        return new Dimension(width, height);
      }
    };
    soughtGraphWrapper.add(soughtGraphLabel, BorderLayout.PAGE_START);
    soughtGraphWrapper.add(soughtGraph, BorderLayout.CENTER);
    soughtGraph.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker()));
    
    
    JPanel content = new JPanel(new BorderLayout(xGap, yGap));
    content.add(issuePanel, BorderLayout.LINE_START);
    content.add(soughtGraphWrapper, BorderLayout.CENTER);
    
    content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    uiContainer.getContentPane().setLayout(new BorderLayout());
    uiContainer.getContentPane().add(content, BorderLayout.CENTER);
  }
  
  
  
  /**
   * Creates the <code>IssueSeekPanel</code>. This method allows subclasses to
   * provide their own, custom, versions of <code>IssueSeekPanel</code>.
   */
  
  protected IssueSeekPanel createIssueSeekPanel(){
    return new IssueSeekPanel(this, uiContainer,
        Preferences.createWrapped(getPrefs(), "issueSeekPanel."));
  }
  
  
  
  /**
   * Creates the <code>IssueMatchPanel</code>. This method allows subclasses to
   * provide their own, custom, versions of <code>IssueMatchPanel</code>.
   * Returns <code>null</code> if there is no <code>IssueMatchPanel</code> (if,
   * for example, the connection is not an instance of
   * <code>MatchConnection</code>).
   */
  
  protected IssueMatchPanel createIssueMatchPanel(){
    if (getConn() instanceof MatchConnection)
      return new IssueMatchPanel(this, uiContainer,
          Preferences.createWrapped(getPrefs(), "issueMatchPanel."));
    else
      return null;
  }
  
  
  
  /**
   * Returns whether we have UI for issuing match offers.
   * 
   * @see #displayMatchUI(ServerUser)
   */
  
  public boolean hasMatchUI(){
    return issueMatchPanel != null;
  }
  
  
  
  /**
   * Sets the UI up to issue a match offer to the specified player (may be
   * <code>null</code>, to indicate a blank opponent).
   * 
   * @see #hasMatchUI()
   */
  
  public void displayMatchUI(ServerUser opponent){
    if (!hasMatchUI())
      throw new IllegalArgumentException("No UI for matching");
    
    issueTabbedPane.setSelectedComponent(issueMatchPanel);
    issueMatchPanel.prepareFor(opponent);
    uiContainer.setActive(true);
  }
  
  
  
  /**
   * Gets called when the seek graph container is made visible.
   */

  public void pluginUIShown(PluginUIEvent evt){
    SeekConnection conn = getSeekConn();
    
    for (Iterator i = conn.getSeeks().iterator(); i.hasNext();){
      Seek seek = (Seek)i.next();
      soughtGraph.addSeek(seek);
    }
    
    conn.getSeekListenerManager().addSeekListener(this);
  }



  /**
   * Gets called when the seek graph container is made invisible.
   */

  public void pluginUIHidden(PluginUIEvent evt){
    soughtGraph.removeAllSeeks();

    SeekConnection conn = getSeekConn();
    conn.getSeekListenerManager().removeSeekListener(this);
  }
  
  
  
  public void pluginUIClosing(PluginUIEvent evt){}
  public void pluginUIActivated(PluginUIEvent evt){}
  public void pluginUIDeactivated(PluginUIEvent evt){}
  public void pluginUIDisposed(PluginUIEvent evt){}
  public void pluginUITitleChanged(PluginUIEvent evt){}
  public void pluginUIIconChanged(PluginUIEvent evt){}
  




  /**
   * Registers the necessary listeners.
   */

  protected void registerListeners(){
    soughtGraph.addSeekSelectionListener(this);
    getConn().getListenerManager().addConnectionListener(this);
  }




  /**
   * Unregisters all the listeners registered by this SoughtGraphPlugin.
   */

  protected void unregisterListeners(){
    SeekConnection conn = getSeekConn();

    soughtGraph.removeSeekSelectionListener(this);
    conn.getSeekListenerManager().removeSeekListener(this); // Just in case.
    getConn().getListenerManager().removeConnectionListener(this);
  }
  
  
  
  /**
   * SeekListener implementation. Gets called when a seek is added.
   */

  public void seekAdded(SeekEvent evt){
    soughtGraph.addSeek(evt.getSeek());
  }



  /**
   * SeekListener implementation. Gets called when a seek is removed.
   */

  public void seekRemoved(SeekEvent evt){
    soughtGraph.removeSeek(evt.getSeek());
  }



  /**
   * SeekSelectionListener implementation. Gets called when the user selects a
   * Seek. This method asks the SeekConnection to accept the selected seek.
   */

  public void seekSelected(SeekSelectionEvent evt){
    Seek seek = evt.getSeek();
    SeekConnection conn = getSeekConn();
    
    // Is it our own seek?
    if (seek.getSeeker().equals(getConn().getUser()))
      conn.withdraw(seek);
    else
      conn.accept(seek);
  }
  
  
  
  /**
   * Remove all seeks on disconnection. This just seems to make more sense than
   * leaving them on.   
   */
  
  public void connectionLost(Connection conn){
    soughtGraph.removeAllSeeks();
  }
  
  
  
  // The rest of ConnectionListener's methods.
  public void connectingFailed(Connection conn, String reason){}
  public void connectionAttempted(Connection conn, String hostname, int port){}
  public void connectionEstablished(Connection conn){}
  public void loginFailed(Connection conn, String reason){}
  public void loginSucceeded(Connection conn){}



  /**
   * Returns the string <code>"seek"</code>.
   */

  public String getId(){
    return "seek";
  }



  /**
   * Returns the name of this plugin.
   */

  public String getName(){
    return getI18n().getString("pluginName");
  }
  
  
  
  /**
   * An action which displays/hides our UI.
   */
  
  private class FindGameAction extends JinAction{
    
    
    
    /**
     * Returns the id of this action.
     */
      
    public String getId(){
      return "findgame";
    }
    
    
    
    /**
     * Displays or hides the UI.
     */
    
    public void actionPerformed(ActionEvent evt){
      if (uiContainer.isVisible())
        uiContainer.setVisible(false);
      else
        uiContainer.setActive(true);
    }
    
    
    
  }



}
