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
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

/**
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class MapScrollPane extends JScrollPane 
{
	private static final long serialVersionUID = 3194707426537094510L;

	public MapScrollPane( MapPanel mapPanel )
	{
		super( mapPanel );
		this.mapPanel = mapPanel;
		if (!mapPanel.isPreferredSizeSet())
			System.err.println(
				"MapPanel's preferred size is not set -> no zoom!");
		MapListener mapListener = new MapListener();
		mapPanel.addMouseMotionListener( mapListener );
		mapPanel.addMouseListener( mapListener );
		setCornersAndHeaders();
	}

	public void showMapScales(boolean show) 
	{
		if (show) {
			setColumnHeaderView( mapPanel.horScale );
			   setRowHeaderView( mapPanel.verScale );
		} else {
			setColumnHeaderView( null );
			   setRowHeaderView( null );
		}
	}

	/* investigate: only when adding corner button the pane 
	 * can be scrolled with up/down/left/right pageUp/down keys
	 */
	private void setCornersAndHeaders()
	{
		JButton cornerButton = new JButton("o");
		cornerButton.setPreferredSize ( new Dimension( 33, 33 ));
		cornerButton.addActionListener( new ActionListener()  {
				public void actionPerformed(ActionEvent e) 
				{ cornerButton_actionPerformed(e); }});
//		no up/dw/left/right page u/d keys without button!
		setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerButton );

//		JPanel upperLeft  = new JPanel();
//		  this.upperLeft  = new JPanel();	// swing member!!!
//		upperLeft.setLayout( new BorderLayout() );
//		upperLeft.add( cornerButton, BorderLayout.CENTER );
//		setCorner(JScrollPane.UPPER_LEFT_CORNER, upperLeft );

//		JPanel lowerLeft  = new JPanel();
//		JPanel lowerRight = new JPanel();
//		JPanel upperRight = new JPanel();
//		 lowerLeft.setBackground(mapPanel.getBackground());
//		lowerRight.setBackground(mapPanel.getBackground());
//		upperRight.setBackground(mapPanel.getBackground());
//		setCorner(JScrollPane.LOWER_LEFT_CORNER,  lowerLeft);
//		setCorner(JScrollPane.UPPER_RIGHT_CORNER, upperRight);
//		setCorner(JScrollPane.LOWER_RIGHT_CORNER, lowerRight);
	}

	private void cornerButton_actionPerformed(ActionEvent e) 
	{
		System.out.println("switch to metric scale... ");
		/* nice to have
		 * MapPanel could implement 
		 * drawGeographicalGrid and drawDecimalGrid [km].
		 * Use this button to toggle both modes. */
	}

	/**
	 * Get the decimal grid currently displayed in the ViewPort.
	 */
	public Rectangle2D.Double getMapPort()
	{
		return mapPanel.pix2decRectangle( 
				getViewport().getViewRect() );
	}

    /**
     * Adds the specified map port listener to receive 
     * decimal grid of map panel displayed in the view port.
     * If listener is <code>null</code>,
     * no exception is thrown and no action is performed.
     */
	public synchronized void addMapPortListener( MapPortListener listener )
	{
        if (listener == null) return;
        if (mapPortListener.isEmpty())
            getViewport().addChangeListener( new MapPort() );
        mapPortListener.add( (MapPortListener) listener );
	}
	ArrayList<MapPortListener> mapPortListener = 
		new ArrayList<MapPortListener>();
	
	/**
	 * catch map port every time it changes
	 * and propagate to listener.
	 */
    private class MapPort implements ChangeListener 
    {
		@Override
		public void stateChanged(ChangeEvent e)
		{
	        if (!mapPortListener.isEmpty())
	        	for (int i = 0; i < mapPortListener.size(); i++) 
	        		mapPortListener.get(i).mapPortChangedTo(e, getMapPort());
		}
    }

	/** Get the decimal center point of the current ViewPort. */
	public Point2D.Double getMapCenter()
	{
		Rectangle viewRectangle = getViewport().getViewRect();
		Rectangle2D.Double decimalViewPort = 
			mapPanel.pix2decRectangle( viewRectangle );
		double x = decimalViewPort.x + decimalViewPort.width  / 2d;
		double y = decimalViewPort.y + decimalViewPort.height / 2d;
//		decimalViewPort.getCenterX(); // should also work 
//		decimalViewPort.getCenterY(); // to be tested
		return new Point2D.Double(x,y);
	}

	/**
	 * Center the map inside the ViewPort to a decimal coordinate.
	 * The displayed grid is restricted to the map's grid area.
	 */
//	TODO: optimize by NOT calling setViewPosition, 
//	if not really necessary. Looks terrible.. 
	public void centerMapTo(Point2D decPoint)
	{
		Rectangle viewRectangle = getViewport().getViewRect();
		int mapX = mapPanel.horScale.dec2pix(decPoint.getX()) 
				 - viewRectangle.width  / 2;
		int mapY = mapPanel.verScale.dec2pix(decPoint.getY())
				 - viewRectangle.height / 2;
//		TODO: add correction if square is too close to border...
		getViewport().setViewPosition( new Point(mapX, mapY) );
	}
	
	/**
	 * Implementation to drag the map panel
	 * by holding the right mouse button.
	 */
	private class MapListener extends MouseInputAdapter 
	{
		Point referencePoint;
//		MouseListener:
		public void mousePressed( MouseEvent e )
		{
			if (SwingUtilities.isRightMouseButton(e))
				referencePoint = e.getPoint();
		}

		public void mouseReleased( MouseEvent e )
		{
			if (SwingUtilities.isRightMouseButton(e))
				referencePoint = null;
		}

//		MouseMotionListener:
		public void mouseDragged( MouseEvent e )
		{
			if (SwingUtilities.isRightMouseButton(e))
				moveMap( e.getPoint() );
		}

		/* No decimal values needed to move the map! */
		private void moveMap(Point targetPoint)
		{
	    	if (referencePoint != null)
	    	{
	    		Rectangle newView = getViewport().getViewRect();
	    		newView.x += referencePoint.x - targetPoint.x;
	    		newView.y += referencePoint.y - targetPoint.y;
	    		mapPanel.scrollRectToVisible(newView);
	    	}
		}
	}
	private MapPanel mapPanel;
}