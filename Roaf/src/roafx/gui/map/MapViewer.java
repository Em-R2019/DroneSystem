/***********************************************************************
 *            The Real Object Application Framework (ROAF)             *
 *        Copyright (C) 2010 Kristof Beiglböck / kbeigl@roaf.de        *
 *                                                                     *
 *  This file is part of the Real Object Application Framework, ROAF.  *
 *                                                                     *
 *  The ROAF is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published  * 
 *  by the Free Software Foundation, either version 3 of the License,  *
 *  or (at your option) any later version.                             *
 *                                                                     *
 *  The ROAF is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.               *
 *  See the GNU General Public License for more details.               *
 *                                                                     *
 *  You should have received a copy of the GNU General Public License  *
 *  along with the ROAF.  If not, see <http://www.gnu.org/licenses/>.  *
 ***********************************************************************/
package roafx.gui.map;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.File;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import roaf.gps.GPSunit;
import roaf.util.GPSpoint;
import roaf.util.Misc;

/**
 * The map viewer is a 'bare bone' for a map GUI.
 * An external application can access two MapPanels via 
 * their public API and register to listen to map events.
 * (This source can be copied out of the package as a template. Visibilities should work...)
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class MapViewer implements MapMouseListener, MapPortListener, MouseWheelListener
{
//	public static String lastPath = Misc.APPPATH;	// TEMPORARY
	public static String lastPath = "D:\\virtex\\workspace\\resources\\gps\\RGB-BUELL";

	{
//		TODOs -------------------------------------------------
//		ENABLE MULTIPLE LISTENERS IN MP AND MScrollP
//		public void mapPortChangedTo(ChangeEvent e, Double mapPortGrid) 

//		determine position > station > make move (on double click?)
//		smallMap.addMapMouseListener( this );
//	      bigMap.addMapMouseListener( this );

//		public void mapClickedAt(MouseEvent e, Point2D decPoint)
//		{
//			if (e.getSource() == smallMap)
//			{
//				showSmallImage = !showSmallImage;
//				if (showSmallImage)
//				     smallMap.drawImages(mapAreas, mapImages);
//			     	 smallMap.drawImages(mapAreas, smallImages);
//				else smallMap.drawImages(null, null);
//			}
//		}
//		private boolean showSmallImage = true;
	}

//	TODO: add loadImage (with box) without FileChooser
	private void menuLoadImage()
	{
		System.out.println( lastPath );
		FileFilter filter = new FileNameExtensionFilter(
				"load map image with gpx file.", "png", "jpg", "jpeg", "gif", "gpx");
		File gpxFiles[] = Misc.chooseFile( lastPath, filter, true, frame );

		String name = gpxFiles[0].getName();
		String path = gpxFiles[0].getAbsolutePath(); // + "\\";
		lastPath = path.substring( 0, path.length() - name.length() );

		if ( gpxFiles == null ) return;
		else if ( gpxFiles.length == 1 ) // only image file
		{
//			TODO: verify if image file (and not gpx!)
//			check if name.img has name.gpx in same directory
			String  imagePath = gpxFiles[0].getAbsolutePath();
			File    imageFile = gpxFiles[0];
			int i = imagePath.lastIndexOf('.');

			String gpxPath = imagePath.substring(0, i+1) + "gpx";
			File   gpxFile = new File ( gpxPath );
			if ( ! gpxFile.exists() ) return; // no image without box

			gpxFiles = new File[2];
			gpxFiles[0] = imageFile;
			gpxFiles[1] =   gpxFile;
		}
//		else
		if ( gpxFiles.length == 2 ) // image and gpx file
		{
			if ( gpxFiles[0].getAbsolutePath().endsWith("gpx") ) // swap order
			{
				File gpxFile = gpxFiles[0];
				gpxFiles[0]  = gpxFiles[1];
				gpxFiles[1]  = gpxFile;
			}
		}
		else return; // choice has to be one or two files

		gpxLoader.loadGPXfile( gpxFiles[1] );
		Rectangle2D.Double mapArea = gpxLoader.getGPXboundBox();
		if ( mapArea == null ) return; // can't draw image without box

//		maybe use ImageIcon (and MediaTracker)
		Image[] mapImages;
		Rectangle2D.Double[] mapAreas;
		mapAreas = new Rectangle2D.Double[1];
		mapAreas[0] = mapArea;
		mapImages = new Image[1];
		System.out.println( "file:" + gpxFiles[0].getAbsolutePath());
	    mapImages[0] = Misc.loadMapImage( "file:" + 
	    		gpxFiles[0].getAbsolutePath(), bigMap );

		System.out.println( "image area: " + mapAreas[0] );
		System.out.print  ( "image size: " ); // Dimension
	    System.out.println( " W:" + mapImages[0].getWidth ( bigMap )
						+ " x H:" + mapImages[0].getHeight( bigMap ));
	    double horPix = 
	    	mapAreas[0].width  / mapImages[0].getWidth ( bigMap );
	    double verPix = 
	    	mapAreas[0].height / mapImages[0].getHeight( bigMap );

	    System.out.println("ver img pixel: " + verPix );
	    System.out.println("hor img pixel: " + horPix );
//	    System.out.println("  pixel ratio: " + horPix / verPix + " h/v");
	    System.out.println("  pixel ratio: " + verPix / horPix + " v/h");

	    adjustBigMapRatio( verPix / horPix );

	      bigMap.drawImages( mapAreas, mapImages );
		smallMap.drawImages( mapAreas, mapImages );
	}

	/** 
	 * Adjust ratio of displayed map/area to projection implied in map image.
	 * Note: Image's map area is not regarded 
	 * ( abs( lat) would be relevant for projection ).
	 * (Image can be outside of MapPanel's gridArea)
	 * Adjusts MapPanels vertical (height) pixels.
	 */
//	only works: load bounds > image
//	don't work: load image  > bounds
//	TODO: -> load bounds should conserve ratio, not given size
	public void adjustBigMapRatio( double v2hRatio )
	{
		if (normalMapSize == null) 
			normalMapSize = bigMap.getPreferredSize();
		double 
		horMapPixSize = bigMap.getGridArea().width  / normalMapSize.width ,
	    verMapPixSize = horMapPixSize * v2hRatio ,
	    vertMapPixel  = bigMap.getGridArea().height / verMapPixSize;

	    normalMapSize.setSize( normalMapSize.width , vertMapPixel ); 
//		apply normalMapSize/ratio and zoom factor:
	    changeBigMapSize();
	}

//	TODO: -> load bounds should conserve ratio, not given size
	private void menuLoadGPXbox()
	{
		FileFilter filter = new FileNameExtensionFilter(
				"load bound box from GPS eXchange files", "gpx");
		File gpxFiles[] = Misc.chooseFile( lastPath, filter, false, frame );

		String name = gpxFiles[0].getName();
		String path = gpxFiles[0].getAbsolutePath();
		lastPath = path.substring( 0, path.length() - name.length() );

		File gpxFile = gpxFiles[0];	// always one file
		System.out.println("load gpxFile: " + gpxFile);
		if ( gpxFile == null ) return;
		gpxLoader.loadGPXfile( gpxFile );
		Rectangle2D.Double gridArea = gpxLoader.getGPXboundBox();
		if ( gridArea == null ) return;
		this.gridArea = gridArea;

		  bigMap.setGridArea( gridArea );
		smallMap.setGridArea( gridArea );
	}
	public Rectangle2D.Double gridArea;

	/* The menu bar should offer more menu items and 
	 * an external management to show/hide them.     
	 * (Also the menu items should be 
	 *  accessible by external code!)             */
	private JMenuBar createMenuBar()
	{
		JMenuItem menuLoadGPXbox = new JMenuItem(); 
		menuLoadGPXbox.setText( "Load Grid (bounds)..." );
		menuLoadGPXbox.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ){
				menuLoadGPXbox(); }});

		JMenuItem menuLoadImageAndGPXfile = new JMenuItem(); 
		menuLoadImageAndGPXfile.setText( "Load Map Image..." );
		menuLoadImageAndGPXfile.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ){
				menuLoadImage(); }});

//		maybe add small and big map image ?

		JMenu fileMenu = new JMenu();
        fileMenu.setText("File");
        fileMenu.add( menuLoadGPXbox );
		fileMenu.add( menuLoadImageAndGPXfile );

		JMenuBar menuBar = new JMenuBar();
        menuBar.add( fileMenu );
		return menuBar;
	}
	
	/** GPSunit used as utility to load GPX infos from file system */
	private void createGPXloader()
	{
		GPSpoint initialize = new GPSpoint(49.03081, 12.10321, new Date());
		initialize.setName("Regensburg"); initialize.setElevation( 256.8093262 );
		gpxLoader = new GPSunit ( initialize );
	}
	public static GPSunit gpxLoader; // static or not?

	@Override
	public void mapClickedAt(MouseEvent e, Point2D decPoint) 
	{
//		maybe change to mouseDragged analog to scroll pane?
		if ((e.getSource() == smallMap)
		 && (SwingUtilities.isRightMouseButton(e)))
			scrollPane.centerMapTo( decPoint );
	}

	public double 
	zoomFactor = 1.0, zoomIncrement = 0.1, zoomMax = 0.1, zoomMin = 10.0;
	/** store value for 100% and ratio w/h */
	private Dimension normalMapSize;
	/* No decimal values needed to zoom the map! */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (normalMapSize == null) 
			normalMapSize = bigMap.getPreferredSize();

		int  notches = e.getWheelRotation();
		if ((notches < 0) && (zoomFactor >= zoomMax))
			zoomFactor -= zoomIncrement;  // test with   factor *= 1.1
		else if (zoomFactor <= zoomMin)
			zoomFactor += zoomIncrement;  // test with division /= 1.1

		changeBigMapSize();
	}

	/** apply zoom and normalMapSize */
	private void changeBigMapSize() 
	{
		//		store center
				Point2D.Double decPoint = scrollPane.getMapCenter();
		//		map jumps back to (0,0)
				bigMap.setPreferredSize( new Dimension(
						(int) (normalMapSize.width  * zoomFactor),
						(int) (normalMapSize.height * zoomFactor)));
		//		recenter
				scrollPane.centerMapTo( decPoint );
	}

	/* Only implemented for one bound box!
	 * If more boxes are needed -> redesign. */
	@Override
	public void mapPortChangedTo(ChangeEvent e, Double mapPortGrid) 
	{
		lastMapPortArea = mapPortGrid;
		Color[] colors = new Color[1];
		colors[0] = Color.BLACK;
		Rectangle2D.Double[] decimalAreas = new Rectangle2D.Double[1];
		decimalAreas[0] = mapPortGrid;
//		TODO: don't draw if height || width == 0 (else it's a line)
		smallMap.drawBoundBoxes( decimalAreas, colors);
	}
	public Rectangle2D.Double lastMapPortArea = new Rectangle2D.Double();

	private void createMapPanels()
	{
		Rectangle2D.Double initialGrid = 
			new Rectangle2D.Double( -50, -50, 100, 100);

		smallMap = new MapPanel( initialGrid );
                smallMap.setBackground(Color.WHITE);
                smallMap.setPreferredSize( new Dimension(controls, 200) );
                smallMap.setMinimumSize  ( smallMap.getPreferredSize()  );
		smallMap.showGrid( false );
		smallMap.addMapMouseListener( this );

		bigMap = new MapPanel( initialGrid );
		bigMap.setBackground ( Color.WHITE );
		bigMap.setBorder( border );
		bigMap.setPreferredSize( new Dimension( 600, 600 ));
		bigMap.addMouseWheelListener( this );
//		bigMap.addMapMouseListener( this );

		scrollPane = new MapScrollPane( bigMap );
		scrollPane.showMapScales(true);
		scrollPane.addMapPortListener( this );
//		investigate (move to end of gui construction?):
//	    scrollPane.requestFocusInWindow();
	}
	public MapPanel bigMap, smallMap;
	public MapScrollPane scrollPane;

	private void layoutComponents( JComponent windowPane )
	{
		controlPanel = new JPanel();
		controlPanel.setBorder( border );
		controlPanel.setPreferredSize(new Dimension(controls, 200));
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));

		splitPaneVertical = new JSplitPane
			(JSplitPane.VERTICAL_SPLIT, smallMap, controlPanel ); 
//		default 0 -> TOP component size is fixed
		splitPaneVertical.setResizeWeight(0.0); 
		splitPaneVertical.setDividerSize(8);	// 0 = invisible ??
		splitPaneVertical.setOneTouchExpandable(true);
		splitPaneVertical.setContinuousLayout(true);
		splitPaneVertical.setPreferredSize( smallMap.getPreferredSize() );

		JSplitPane splitPaneHorizontal = new JSplitPane();
		splitPaneHorizontal.add(scrollPane, JSplitPane.LEFT);
		splitPaneHorizontal.setRightComponent(splitPaneVertical);
//		1 -> RIGHT component size is fixed
		splitPaneHorizontal.setResizeWeight(1.0);
		splitPaneHorizontal.setDividerSize(8);
		splitPaneHorizontal.setOneTouchExpandable(true);
		splitPaneHorizontal.setContinuousLayout(true);

		windowPane.add( splitPaneHorizontal, BorderLayout.CENTER );

		statusBar = new JPanel(); // only height is honored
//		statusBar.setPreferredSize( new Dimension( 100, 25 ));
		windowPane.add( statusBar, BorderLayout.SOUTH );
	}
	public JPanel statusBar, controlPanel;
	public JSplitPane splitPaneVertical;

	private int controls = 200; // right side
	private int fz = 5; 		// frame size 
	private Border border =	
		BorderFactory.createMatteBorder(fz,fz,fz,fz,Color.LIGHT_GRAY);

	public static MapViewer createAndShowGUI() 
	{
		MapViewer gui = new MapViewer();
		gui.createGPXloader();
		try { UIManager.setLookAndFeel(
			  UIManager.getSystemLookAndFeelClassName()); } 
		catch (Exception e) { System.err.println(
				"Using the default look and feel."); }
		JFrame.setDefaultLookAndFeelDecorated(true);
		gui.frame = new JFrame("MapViewer");
		int pos = 50;
		gui.frame.setLocation(pos, pos);
//		don't use System.exit or EXIT_ON_CLOSE 
//		-> GUI shouldn't terminate rest of application
//		gui.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int screenWidth  = gui.frame.getToolkit().getScreenSize().width;
		int screenHeight = gui.frame.getToolkit().getScreenSize().height;
		gui.frame.setPreferredSize(
			  new Dimension(screenWidth-2*pos, screenHeight-2*pos));
		JComponent windowPane = (JComponent) gui.frame.getContentPane();
		gui.createMapPanels();
		gui.layoutComponents( windowPane );
		gui.menuBar = gui.createMenuBar();
		gui.frame.setJMenuBar( gui.menuBar );
		gui.frame.pack();
		gui.frame.setVisible(true);
		return gui;
	}
	public JMenuBar menuBar;
	public JFrame   frame; // reference for file chooser

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() { createAndShowGUI(); }
		});
	}
}