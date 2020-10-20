/***********************************************************************
 *            The Real Object Application Framework (ROAF)             *
 *        Copyright (C) 2010 Kristof Beiglb�ck / kbeigl@roaf.de        *
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
package roaf.gps;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This Route implementation contains an ArrayList of Positions.
 *  <p> The Route class evolved from the article <br>
 *  "encapsulation vs. information hiding" by Paul Rogers <br>
 *  at www.javaworld.com/javaworld/jw-05-2001/jw-0518-encapsulation.html </p>
 *  
 * @author Kristof Beiglb�ck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class Route implements Serializable
{
	private static final long serialVersionUID = 3907932639713898100L;

	public Route() { sequence = new ArrayList<Position>(); }

//  TODO: public Route( List<Position> positions ) 
//  TODO: public Route( Route route ) 
//        see Link extending Route
    { 
//  	sequence = positions;
//  	calculateTotalDistance();
    }

//	TODO public methods .append .prepend .reverse

    /**
     * Reverse the order of the Route's positions.
     * Useful for external normalization of from and to.
     * Logically BoundBox and TotalDistance don't change.
     */
    public void reverse() { Collections.reverse( sequence ); }
    
    /**
     * The route will be added to this route,
     * IF the last Position of this route 
     * matches the first Position of route.
     * This should NOT work for Traces (override and hide),
     * since time stamps will most likely make no sense.
     */
    public boolean appendRoute( Route route ) // and prepend
    // implicit cast to Route gets rid of nodeIDs of Link etc.
    {
//    	if ( getNrOfElements() == 0 ) // empty Route
//    		...
		Position  last = sequence.get( sequence.size()-1 ); 
		Position first = route.sequence.get( 0 ); 
//		if ( !last.equals( first )) doesn't work > applies Object.equals
		if (!((last.getLatitude()  == first.getLatitude()) 
		   && (last.getLongitude() == first.getLongitude())))
		{
			System.out.println("end and start don't match?");
			return false;
		}
//		remove first Position of route due to redundancy
		route.sequence.remove(0);
		if (!sequence.addAll( route.sequence )) 
			return false;
//    	recalculate distance & bbox
		for (int pos = 0; pos < route.sequence.size(); pos++)
			expandBoundBox( route.sequence.get( pos ));
		calculateTotalDistance();
    	return true;
    }

    /** append Position implementation at the end of the existing chain. */
    public void appendPosition( Position point )
    {
    	// encapsulate a new object as new Position
    	sequence.add( point.getNewPosition() );	
		expandBoundBox( point );

//		maybe remove this and replace with calculateTotalDistance() 
		if (sequence.size()==1) 
			totalDistance = 0.0;
    	else if (sequence.size()> 1)
    		totalDistance += sequence.get(sequence.size()-2)
    			   .distance(sequence.get(sequence.size()-1));
    }
    
    protected void calculateTotalDistance()
    {
		totalDistance = 0.0;
		if ( sequence.size() < 2 ) return;
    	for ( int i = 1; i < sequence.size(); i++ ) 
    		totalDistance += sequence.get( i-1 )
    			   .distance(sequence.get( i  ));
    }

    /** return Position implementation. */
    public Position getPosition( int position ) 
	{
    	return sequence.get( position ).getNewPosition(); 
	}

//  TODO
//  public Position removePosition (int position) 
//    { // modify totalDistance .. }
//  public void     insertPosition (int position, Position newPoint )
//    { // modify totalDistance .. }

    public double getTotalDistance() { return totalDistance; }

    /* Convenience method to save some typing */
    public double getDistance( int from, int to) 
    { 
    	if ((from >= 0) || (to < getNrOfElements()))
    		return getPosition( from ).distance( getPosition(to) );
//    	else
    		return -1.0;
    }

    /* Convenience method to save some typing */
    public double getDirection( int from, int to) 
    { 
    	if ((from >= 0) || (to < getNrOfElements()))
    		return getPosition( from ).direction( getPosition(to) );
//    	else
    		return 0.0; // TODO return impossible direction (?)
    }

    public int size() { return sequence.size(); }

    /** @deprecated replaced by {@link #size()} */
    @Deprecated public int getNrOfElements() { return sequence.size(); }

    public Position[] getPositionArray()
    {
    	Position[] positionArray = sequence.toArray( new Position[0] );
    	return positionArray;
    }

	public Rectangle2D.Double getBoundBox() { return boundBox; }

	/** Modify bounding box to include the given Position. */
    private void expandBoundBox( Position point )
    {
    	double lat = point.getLatitude() ;
    	double lon = point.getLongitude();

    	if (boundBox == null)	// initial rectangle = point
    		boundBox = new Rectangle2D.Double( lon, lat, 0.0, 0.0);
    	else
    	{
    		double lonW = boundBox.x,
    		       latS = boundBox.y,
    		lonE = lonW + boundBox.width ,
    		latN = latS + boundBox.height;

    		if ( lat > latN ) latN = lat;
    		if ( lat < latS ) latS = lat;
    		if ( lon < lonW ) lonW = lon;
    		if ( lon > lonE ) lonE = lon;

    		boundBox.x = lonW ;
    		boundBox.y = latS ;
    		boundBox.width  = lonE - lonW ;
    		boundBox.height = latN - latS ;
    	}
    }

	/**
	 * boundBox orientation: <BR>
	 * Rectangle2D.Double( lonW, latS, lonE-lonW, latN-latS) <BR>
	 * Rectangle2D.Double( left, bottom, width, height) */
	private Rectangle2D.Double boundBox = null;
	/** the sequence can be a List of Positions (a route) or GPSinfos (a trace) */
	protected List<Position> sequence;	// generalize ArrayList to List
	private double totalDistance = 0;

//	@Override
//	public String toString() 
//	{
//		return getNrOfElements() + " elements, " + getTotalDistance() + " km";
//	}
        
        public void remove(int position){
            sequence.remove(position);
        }
}