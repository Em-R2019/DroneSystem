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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import roaf.gps.Position;
import roaf.gps.Route;

/**
 * MapPanel draws a linear, cartesian, decimal grid
 * defined by a Rectangle2D.Double gridArea. 
 * Images and geometric primitives are displayed with
 * decimal values, regardless of the components size.
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class MapPanel extends JPanel
//	nice to have:  implements Scrollable interface
{
	private static final long serialVersionUID = 1767890952819655297L;

	/**
	 * The decimal area is minimal requirement. 
	 * Without the gridArea no grid,
	 * nor scale can be computed.
	 */
	public MapPanel( Rectangle2D.Double gridArea ) 
	{
		// TODO validate gridArea...
		this.gridArea = gridArea;
//		can't calculateScales at construction time!
	}

	public void setGridArea( Rectangle2D.Double gridArea )
	{
		// TODO validate gridArea...
//		nice to have: animation thread for a smooth change
//		animateCoordinateChange( from this.gridArea, to gridArea);
		this.gridArea = gridArea;
		calculateScales();
//		repaint();
//		implicitly recalculate and repaint:
		setPreferredSize( getPreferredSize() ); 
	}

//	                  better: getBoundBox()
	public Rectangle2D.Double getGridArea() { return gridArea; }

	public void showGrid(boolean show)
	{
		showGrid = show;
		repaint();
	}
	private boolean showGrid = true;

	/**
	 * Set coordinate implementation 
	 * for distance calculations.
//	TODO public void setMapScale( Position pos ) { }
                   = setCoordSystem
	 */

	/**
	 * calculate ratio
	 * distance on screen (pixel per inch) to
	 * distance in real world (km > inch)
	 * return factor
	 * example:
	 *    mapScale = 1:25.000 ( 1cm = 4km )
	 *    > 1 length on screen = 25.000 lengths on globe
	 *    > return (int) 25.000
//	TODO public int getMapScale() 
//	{
//		IF mapScale is set (distance method available)
//		return ~factorToRealDistance
//	}
//	TODO public void drawMapScale( Position pos )
	{
		draw i.e. 1m - 10m - 1km - 10km line on map 
		in horizontal AND vertical direction
		inside viewPort !?
	}
	 */

	/* Mechanism/s to draw decimal primitive shapes, geometry.
	 * Implemented with drawXX > calculateXX > paintXX for arrays of shapes. 
	 * No adding or removing of individual elements - yet.
	 * see JComponent container for sample terminology */
	
	/**
	 * To draw images. They all have to be supplied together. 
	 * Every image needs a corresponding decimal mapArea.
	 * Both arrays have to have equal lengths. To reset/remove images 
	 * call method with one or both arrays being null. 
	 */
	public void drawImages(Rectangle2D.Double[] imageAreas, Image[] images)
	{
		if (( imageAreas == null) || ( images == null)) // reset all 
		{
			this.images 	= null;
			this.imageAreas = null;
			imageRectangles = null;
		} else 
		{
			if ( imageAreas.length != images.length )
				System.err.println("Exception: " +
					"arrays have different nr of elements!");
			this.images 	= images;		// external can still exist!
			this.imageAreas = imageAreas;
			if (gridRectangle != null) dec2pixImages();
			// calcX will be invoked by setBounds otherwise.
		}
		repaint();
	}

	/** Draw image icons with their actual pixel size AND ORIENTATION. */
//	public void drawIcons(Point2D.Double[] positions, ImageIcon[] images)
//	{ }

	/**
	 * The x (lon) and y (lat) coordinates define 
	 * the upper left corner of the bounding box.
	 */
	public void drawBoundBoxes( 
			Rectangle2D.Double[] decimalAreas, Color[] colors )
	{
		if (( decimalAreas == null) || ( colors == null)) 
		{
			decimalRects = null; rectangleColors  = null;  
			  pixelRects = null;
		} else 
		{
			if ( decimalAreas.length != colors.length )
				System.err.println("throw Exception" +
					"(arrays have different nr of elements!)");
			decimalRects = decimalAreas;
			rectangleColors = colors;
			if (gridRectangle != null) dec2pixBoundBoxes();
		}
		repaint();
	}

	/** 
	 * Draws a filled circle with given radius around each position.
	 * Or resets and removes all positions from panel. 
	 */
	public void fillPositions
	( Position[] positions, int[] pixelRadius, Color[] colors )
	{
		if (( positions == null) || ( pixelRadius == null) || ( colors == null)) 
		{
			fullPosCircles = null; fullPosSize = null; 
			fullPosPoints  = null; fullPosColor = null;
		} else 
		{
			if ((  positions.length != colors.length )
			|| ( pixelRadius.length != colors.length ))
				System.err.println("throw Exception" +
					"(arrays have different nr of elements!)");
			 fullPosCircles = positions ;
			 fullPosSize = pixelRadius ;
			 fullPosColor = colors ;
			 if (gridRectangle != null) dec2pixFullPositions();
		}
		repaint();
	}

	/**
	 * Draws an empty circle with given radius and color around each
	 * position. Or resets and removes all positions from panel.
	 * problem: pixelRadius does not scale with zooming
	 * TODO: draw- and fillPositions back to decimal value (or metric!)
	 */
	public void drawPositions
	( Position[] positions, int[] pixelRadius, Color[] colors )
	{
		if (( positions == null) || ( pixelRadius == null) || ( colors == null)) 
		{
			positionPoints = null; positionCircles = null;
			positionColors = null; positionSizes = null;
		} else 
		{
			if ((  positions.length != colors.length )
			|| ( pixelRadius.length != colors.length ))
				System.err.println("throw Exception" +
					"(arrays have different nr of elements!)");
			this.positionCircles = positions ;
			positionSizes = pixelRadius ;
			positionColors = colors ;
			if (gridRectangle != null) dec2pixPositions();
		}
		repaint();
	}

//	drawFaces( .. )
//	drawText - would be useful for map graphs sequential node IDs and destinations ...

//	separate drawEdges rather than drawRoutes( .. drawEdges .. ) 
//	for clear external management
	public void drawEdges( Position[] from, Position[] to, Color[] colors )
	{
		if (( from == null) || ( to == null) || ( colors == null)) 
		{
			fromEdges = null; toEdges = null; edgeColors = null;
			fromX = null; fromY = null; toX = null;  toY = null; 
//			-> g.drawLine(fromX, fromY, toX, toY);
		} 
		else 
		{
			if ((  from.length != colors.length )
			||  (    to.length != colors.length ))
				System.err.println("throw Exception" +
					"(arrays have different nr of elements!)");

			fromEdges = from; toEdges = to; edgeColors = colors ;
			if (gridRectangle != null) dec2pixEdges();
		}
		repaint();
	}

//	improve MP with collection algebra ..
//	public void addRoute( Route positions, Color routeColor )

	/** Draw colored polylines. */
	public void drawRoutes( Route[] routes, Color[] colors )
	{
		routeColors = colors;
		if ( routes == null) 
		{
			routeColors = null; this.routes = null;     // dec source
			routesXPoints = null; routesYPoints = null; // pix target
		} else 
		{
			this.routes = routes;
			if (gridRectangle != null) dec2pixRoutes();
		}
		repaint();
	}

	/** trigger the re/calculation of map ranges and -scales. */
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);

//		Rectangle visibleRect = new Rectangle();
//		computeVisibleRect( visibleRect ); // clipBounds ...
		if (gridRectangle == null)		   // first call
			gridRectangle = new Rectangle();
		determineGridRectangle();
		calculateScales();
		if (      imageAreas != null) dec2pixImages();
		if (    decimalRects != null) dec2pixBoundBoxes();
		if (  fullPosCircles != null) dec2pixFullPositions();
		if ( positionCircles != null) dec2pixPositions();
		if (          routes != null) dec2pixRoutes();
		if (       fromEdges != null) dec2pixEdges();
	}

	/**
	 * Dimension preferredSize implicitly stores 
	 * the ratio width / height of the map projection.
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize) 
	{
		super.setPreferredSize(preferredSize);
		setBounds(0, 0, preferredSize.width, preferredSize.height);
	}

    /**
     * Adds the specified map mouse listener to
     * receive mouse position of this MapPanel.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     */
    public synchronized void addMapMouseListener(MapMouseListener listener) 
    {
        if (listener == null) return;
//      if (listener not in getMouseListeners()) ...
        if ( !mousePositionListener )
        {
            addMouseListener( mousePosition );
        	mousePositionListener = true;
        }
        mapMouseListeners.add( (MapMouseListener) listener );
    }
//  only addMouseListener once
    private boolean mousePositionListener = false;
    private MousePosition mousePosition = new MousePosition();
//  private MapMouseListener mapMouseListener;
    private ArrayList<MapMouseListener> mapMouseListeners =
    	new ArrayList<MapMouseListener>();

	/**
	 * class to catch the mouse click, calculate 
	 * decimal position and propagate to listener.
	 */
    private class MousePosition extends MouseAdapter 
    {
//  	note: a click is press(x1,y1) & release(x2,y2) at one point (x1=x2,y1=y2)!
//    	maybe prefer mousePressed(e) to receive position as early as possible ?
    	public void mouseClicked(MouseEvent e) 
    	{
    		if ( !mapMouseListeners.isEmpty())
   			for (int li = 0; li < mapMouseListeners.size(); li++) 
   			{
   				Point2D.Double mapPoint = new Point2D.Double( 
   						horScale.pix2dec(e.getX()),
   						verScale.pix2dec(e.getY()));
   				mapMouseListeners.get(li).mapClickedAt( e, mapPoint );
   			}
    	}
    }

	/**
	 * Subtract border from drawing area 
	 * and set pixels of gridRectangle.
	 */
	private void determineGridRectangle()
	{
		gridRectangle.width  = getWidth();
		gridRectangle.height = getHeight();
//		honor preset sizes:
//		if (isMinimumSizeSet()) ...
//		if (isMaximumSizeSet()) ...
//		if (isPreferredSizeSet())
//		{
//			Dimension prefSize = getPreferredSize();
//			if (gridRectangle.width  > prefSize.width)
//				gridRectangle.width  = prefSize.width;
//			if (gridRectangle.height > prefSize.height)
//				gridRectangle.height = prefSize.height;
//		}
		Insets insets = getInsets();
		gridRectangle.x = insets.left;
		gridRectangle.y = insets.top;
		gridRectangle.width  -= (insets.left + insets.right);
		gridRectangle.height -= (insets.top  + insets.bottom);		
	}

	/**
	 * The decimal gridArea is fitted into 
	 * the integer (pixel) gridRectangle.
	 */
	private void calculateScales()
	{
		if (horScale == null)	// first call
			horScale = new MapScalePanel(
					gridArea.x, gridArea.getWidth(), 
					gridRectangle.x, gridRectangle.width, 
					MapScalePanel.HORIZONTAL);
		else
			horScale.setScale(
					gridArea.x, gridArea.getWidth(), 
					gridRectangle.x, gridRectangle.width);
		
		if (verScale == null)
			verScale = new MapScalePanel(
					gridArea.y, gridArea.getHeight(),
					gridRectangle.y, gridRectangle.height, 
					MapScalePanel.VERTICAL);
		else
			verScale.setScale(
					gridArea.y, gridArea.getHeight(), 
					gridRectangle.y, gridRectangle.height);

//		could be adjusted to MPs border: top left bottom right (+ color)
//		horScale.setBorder( border ); // 0, border.left, 0, border.right
//		verScale.setBorder( border ); // border.top, 0, border.bottom, 0
//		radical enforcement of background (implicitly override setBgrd())
		horScale.setBackground( getBackground() );
		verScale.setBackground( getBackground() );
//		preferredLength is only relevant for visual scales!
		horScale.setPreferredLength( getWidth() );
		verScale.setPreferredLength( getHeight() );

		horScale.repaint();
		verScale.repaint();
		repaint();
	}
	
	/* TODO: combine for kinds: round fill/draw square fill/draw */
	private void dec2pixPositions()
	{
		positionPoints = new Point[positionCircles.length];
		for (int i = 0; i < positionCircles.length; i++)
		{
//			if Point object is available -> re-use it!
			if (positionPoints[i] == null) 
				positionPoints[i] = new Point(); 
			positionPoints[i].x = horScale.dec2pix(
					positionCircles[i].getLongitude()) - positionSizes[i]/2;
			positionPoints[i].y = verScale.dec2pix(
					positionCircles[i].getLatitude())  - positionSizes[i]/2;
		}
	}
	
	private void dec2pixEdges()
	{
		fromX = new int[fromEdges.length]; toX = new int[fromEdges.length]; 
		fromY = new int[fromEdges.length]; toY = new int[fromEdges.length]; 
		for (int edge = 0; edge < fromEdges.length; edge++) 
		{
			fromX[edge] = horScale.dec2pix( fromEdges[edge].getLongitude());
			fromY[edge] = verScale.dec2pix( fromEdges[edge].getLatitude() );
			  toX[edge] = horScale.dec2pix(   toEdges[edge].getLongitude());
			  toY[edge] = verScale.dec2pix(   toEdges[edge].getLatitude() );
		}
	}

	private void dec2pixFullPositions()
	{
		fullPosPoints = new Point[fullPosCircles.length];
		for (int i = 0; i < fullPosCircles.length; i++)
		{
			if (fullPosPoints[i] == null) 
				fullPosPoints[i] = new Point(); 
			fullPosPoints[i].x = horScale.dec2pix(
					fullPosCircles[i].getLongitude()) - fullPosSize[i]/2;
			fullPosPoints[i].y = verScale.dec2pix(
					fullPosCircles[i].getLatitude())  - fullPosSize[i]/2;
		}
	}

	private void dec2pixRoutes()
	{
		int nrOfRoutes = routes.length;
		routesXPoints = new int[nrOfRoutes][];
		routesYPoints = new int[nrOfRoutes][];
		for (int route = 0; route < routes.length; route++) 
		{
			int routeLength = routes[route].getNrOfElements();			
			routeXPoints = new int[routeLength];
			routeYPoints = new int[routeLength];
			for (int pt = 0; pt < routeLength; pt++)
			{
				Position pos = routes[route].getPosition(pt);
				routeXPoints[pt] = horScale.dec2pix( pos.getLongitude());
				routeYPoints[pt] = verScale.dec2pix( pos.getLatitude());
			}
			routesXPoints[route] = routeXPoints;
			routesYPoints[route] = routeYPoints;
		}
	}

//	private void dec2pixRoute()
//	{
//		int routeLength = route.getNrOfElements();
//		routeXPoints = new int[routeLength];
//		routeYPoints = new int[routeLength];
//		for (int i = 0; i < routeLength; i++)
//		{
//			Position pos = route.getPosition(i);
//			routeXPoints[i] = horScale.dec2pix( pos.getLongitude());
//			routeYPoints[i] = verScale.dec2pix( pos.getLatitude());
//		}
//	}

	/** 
	 * Convert decimal imageAreas 
	 * to pixel imageRectangles for painting.
	 */
	private void dec2pixImages()
	{
		imageRectangles = new Rectangle[imageAreas.length];
		for (int i = 0; i < imageAreas.length; i++)
			imageRectangles[i] = 
				dec2pixRectangle( imageAreas[i]);
	}
	
	private void dec2pixBoundBoxes()
	{
		pixelRects = new Rectangle[decimalRects.length];
		for (int i = 0; i < decimalRects.length; i++)
			this.pixelRects[i] = 
				dec2pixRectangle( decimalRects[i] );
	}

	/**
	 * The x (lat) and y (lon) values define the lower (S) left (W) corner
	 * and the positive lengths cover the region up (N) and right (E).
	 */
	private Rectangle dec2pixRectangle( Rectangle2D.Double decimalArea )
	{
//		if (decimalArea == null) then ..
		Rectangle pixelRectangle = new Rectangle();
		pixelRectangle.x   = horScale.dec2pix(decimalArea.x);
		int pixelRectRight = horScale.dec2pix(decimalArea.x + decimalArea.width);
		pixelRectangle.width  = pixelRectRight  - pixelRectangle.x;

		pixelRectangle.y    = verScale.dec2pix(decimalArea.y + decimalArea.height);
		int pixelRectBottom = verScale.dec2pix(decimalArea.y);
		pixelRectangle.height = pixelRectBottom - pixelRectangle.y; 

		return pixelRectangle;
	}
	
	/**
	 *  reverse function of decimalToPixelRectangle (theoretically) with 
	 *  pixel == pixelToDecimalRectangle( decimalToPixelRectangle( pixel ))
	 *  and vice versa decimal == ...
	 */
	Rectangle2D.Double pix2decRectangle( Rectangle pixelRectangle )
	{
//		if (pixelRectangle == null) then what??
		Rectangle2D.Double decimalArea = new Rectangle2D.Double();

		if ((horScale == null) || (verScale == null))
			return new Rectangle2D.Double();

		double decAreaRight;
		decimalArea.x = horScale.pix2dec( pixelRectangle.x );
		decAreaRight =  horScale.pix2dec( pixelRectangle.x + pixelRectangle.width );
		decimalArea.width = decAreaRight - decimalArea.x;

		double decAreaNorth;
		decimalArea.y = verScale.pix2dec( pixelRectangle.y + pixelRectangle.height);
		decAreaNorth  = verScale.pix2dec( pixelRectangle.y);
		decimalArea.height = decAreaNorth - decimalArea.y;

		return decimalArea ;
	}
	
//	???
//	private Point dec2pixPositions( Point2D.Double decimalPosition )
//	{ return new Point(); }

	protected void paintComponent(Graphics g)
	{
		if (isOpaque()){
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
//		--------------------------------------------------------
//		(not only) in conjunction with scrollbars
/*		Rectangle clipBounds = g.getClipBounds();
		System.out.println("MapPanel.clipBounds: " + clipBounds );
		Rectangle bounds = getBounds();
		System.out.println("MapPanel.getBounds : " + bounds );		
		int d = 30;	// no use drawing under the border
		g.setColor(Color.BLUE);
		g.drawRect(clipBounds.x+d, clipBounds.y+d, clipBounds.width-2*d, clipBounds.height-2*d);
		g.setColor(Color.RED);
		g.drawRect(bounds.x+d+1, bounds.y+d+1, bounds.width-2*d-2, bounds.height-2*d-2);
 */		
//		TODO: evaluate cutting down gridRectangle? to clipBounds!!
//		   ...wait until performance gets apparent!
//		--------------------------------------------------------
//		now paint all shapes
		paintImages(g);
		if (showGrid) paintGrid(g);
		paintBoundBoxes(g);
		paintPositions(g);
		paintFullPositions(g);
		paintEdges(g);
		paintRoutes(g);
                paintText(g);
	}
	
        public void paintText(Graphics g){
            if (text == null){
                return;
            }
            g.drawString(text, tx, ty);
    }
        
	private void paintImages(Graphics g)
	{
		if (imageRectangles != null)
			for (int i = 0; i < imageRectangles.length; i++)
				g.drawImage( images[i], 
					imageRectangles[i].x, 	  imageRectangles[i].y, 
					imageRectangles[i].width, imageRectangles[i].height, 
					this);
	}

	private void paintGrid(Graphics g)
	{
		g.setColor(gridColor);
		
		double start = horScale.firstNotch, draw = start;
		double   end = horScale.scaleStart + horScale.scaleLength;
		while(draw <= end)
		{
			int  x = horScale.dec2pix(draw);
			if ((x > horScale.pixelStart) 
			&&  (x <(horScale.pixelStart + horScale.pixelLength)))
				g.drawLine(x, gridRectangle.y, x, 
				   gridRectangle.height + gridRectangle.y);
//			g.drawString( MapScalePanel.roundDouble(draw,4) + "", 
//					horScale.dec2pix(draw)+5, 40);
			draw += horScale.decLineSpace;
		}
		start = verScale.firstNotch; draw = start;
		  end = verScale.scaleStart + verScale.scaleLength;
		while(draw <= end)
		{
			int  y = verScale.dec2pix(draw);
			if ((y > verScale.pixelStart) 
			&&  (y <(verScale.pixelStart + verScale.pixelLength)))
				g.drawLine(gridRectangle.x, y, 
				   gridRectangle.width + gridRectangle.x, y);
//			g.drawString( MapScalePanel.roundDouble(draw,3) + "", 
//					40, verScale.dec2pix(draw));
			draw += verScale.decLineSpace;
		}
	}
	
	private void paintBoundBoxes(Graphics g)
	{
//		also see java tutorial: SelectionDemo.java for XOR rectangle
		if (pixelRects == null) return;
		for (int i = 0; i < pixelRects.length; i++)
		{
			g.setColor( rectangleColors[i]);
			g.drawRect( pixelRects[i].x,     pixelRects[i].y, 
					    pixelRects[i].width, pixelRects[i].height );
		}
	}

	private void paintPositions(Graphics g)
	{
		if ( positionPoints == null) return;
		for (int i = 0; i < positionPoints.length; i++) 
		{
			g.setColor( positionColors[i] );
			g.drawOval( positionPoints[i].x, positionPoints[i].y, 
					    positionSizes[i],  positionSizes[i] );
		}
	}

	private void paintEdges(Graphics g)
	{
		if ( fromX == null) return;
		for (int edge = 0; edge < fromX.length; edge++) 
		{
			g.setColor( edgeColors[edge] );
			g.drawLine( fromX[edge], fromY[edge], toX[edge], toY[edge] ); 
		}
	}

	private void paintFullPositions(Graphics g)
	{
		if ( fullPosPoints == null) return;
		for (int i = 0; i < fullPosPoints.length; i++) 
		{
			g.setColor( fullPosColor[i] );
			g.fillOval( fullPosPoints[i].x, fullPosPoints[i].y, 
					    fullPosSize[i], fullPosSize[i] );
		}
	}
//        @Override 
//        protected void paintComponent(Graphics gr){
//            super.paintComponent(gr); 
//            gr.drawString("string literal or a string variable", 0,10);
//        }

	private void paintRoutes(Graphics g)
	{
		if ( routesXPoints == null) return;
		for (int route = 0; route < routesXPoints.length; route++) 
		{
			g.setColor( routeColors[route] );
			g.drawPolyline( routesXPoints[route], 
				routesYPoints[route], routesXPoints[route].length);
		}
	}

//	private void paintRoute(Graphics g)
//	{
//		if ( routeXPoints == null) return;
//		g.setColor( routeColor );
//		g.drawPolyline(routeXPoints, routeYPoints, routeXPoints.length);
//	}
/*
	 * ticksAt is the desired horizontal- and verticalSpace value in pixels.
	 * When grid is scaled the actual values might deviate from ticksAt value. 
	 * Default value is set to (screen pixels per half an inch).
	public void setTicksAt(int ticksAt)
	{
		this.ticksAt = ticksAt;
		calculateGrid( gridArea, drawingRectangle );
		repaint();
	}
	 * preferred # of pixels between two (vert and horiz) lines. 
	 * default:  # of pixels per half inch
	private int ticksAt = Toolkit.getDefaultToolkit().getScreenResolution() / 2;
 */
//	drawEdges ( = lines )
        public void setText(String text, int x, int y){
            this.text = text;
            this.tx = x;
            this.ty = y;
        }
        
	private int[] fromX, fromY, toX, toY; 
	private Position[] fromEdges, toEdges; 
	private Color[] edgeColors;
//  deprecated: Route
//	private Color routeColor;
//	private Route route;
//	new: Routes
    private Color[] routeColors;
    private Route[] routes;
//  points for each route
    private int[] routeXPoints, routeYPoints;
//  routes_Points[route#][pix coordinates]
    private int[][] routesXPoints, routesYPoints;
//  draw / fillPositions
    private Color[] positionColors, fullPosColor;
    private Position[] positionCircles, fullPosCircles ;
    private int[] positionSizes, fullPosSize;
    private Point[] positionPoints, fullPosPoints;	//  paint
//  drawSquares ..
//  fillSquares ..
//  private boolean[] rectangleFilled;
//  drawBoundBoxes = Rectangles 
    private String text;
    private int tx, ty;
    private Rectangle2D.Double[] decimalRects;
    private Color[] rectangleColors;
//  paintRectangles
    private Rectangle[] pixelRects;
//  drawImages
    private Image[] images;
    private Rectangle2D.Double[] imageAreas;
//  paintImages
    private Rectangle[] imageRectangles;
//  paintGrid
	private Color gridColor = Color.GRAY;
//  calculateScales
	MapScalePanel horScale, verScale;	// package visible!
//	constructor and setter
	private Rectangle2D.Double gridArea;
//	setBounds & determineGridRectangle
	private Rectangle gridRectangle;	
	// if (gridRectangle != null) indicates that scales are set etc.	
}