<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE> Jin Applet </TITLE>
</HEAD>

<BODY>

<CENTER>
<APPLET CODE="free.jin.JinApplet"
        ARCHIVE="jin.jar, libs/swingall.jar, libs/chess.jar, libs/util.jar, libs/jregex.jar, libs/timesealing.jar, servers/freechess.jar, libs/console.jar, plugins/fics/console.jar, libs/board.jar, plugins/fics/board.jar, libs/seek.jar, plugins/fics/seek.jar, libs/sound.jar, plugins/fics/sound.jar"
		WIDTH="400" HEIGHT="300">
		
	<PARAM NAME="port" value="5000">
	
	<PARAM NAME="server.classname"
	           VALUE="free.jin.freechess.FreechessServer">
			   
			   
			   
	<PARAM NAME="plugins.count" VALUE="4">
			   
	<PARAM NAME="plugins.0.classname"
	           VALUE="free.jin.console.fics.FreechessConsoleManager">

	<PARAM NAME="plugins.1.classname"
	           VALUE="free.jin.board.fics.FreechessBoardManager">
			   
	<PARAM NAME="plugins.2.classname"
	           VALUE="free.jin.seek.fics.FreechessSoughtGraphPlugin">

	<PARAM NAME="plugins.3.classname"
	           VALUE="free.jin.sound.fics.FreechessSoundManager">

			   
			   
	<PARAM NAME="resources.boards" VALUE="cold-marble gray-tiles green-marble pale-wood plain red-marble slate winter wooden-dark">
	
	<PARAM NAME="resources.pieces" VALUE="eboard xboard">
			   
<!--

	Uncomment this to allow saving preferences.
	
	<PARAM NAME="savePrefsUrl" VALUE="save_prefs.php">			   
	<PARAM NAME="reserveSpaceUrl" VALUE="reserve_space.php">			   
			   
	<?php
		require "load_prefs.php";
	?>
	
-->
			   
			   
	Please enable Java to run Jin
</APPLET>
</CENTER>

</BODY>