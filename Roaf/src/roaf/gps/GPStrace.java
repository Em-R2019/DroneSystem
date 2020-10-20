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

import java.util.Iterator;

/**
 * @author Kristof Beiglb�ck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class GPStrace extends Route 
{
	private static final long serialVersionUID = 7001035212536946139L;

	public boolean appendRoute(Route route) 
	{
		System.err.println( "You can't append a Route to a Trace!");
		return false;
	}

	/**
     * This method can be used as long as the 
     * Position is a GPSinfo implementation. 
     * For more type safety during compile time 
     * use appendGPSinfo(GPSinfo point) instead.
     * 
	 * @see roaf.gps.Route#appendPosition(roaf.gps.Position)
	 */
	@Override
	public void appendPosition(Position point)
	{
		if ( point instanceof GPSinfo )
			appendGPSinfo( (GPSinfo) point);
		else // throw Exception ?
			System.err.println( point + 
			" is not an instance of GPSinfo and was NOT appended!");
	}

    public void appendGPSinfo(GPSinfo point)
    {
		super.appendPosition(point);
		if ( sequence.size() == 1 ) 
			totalDuration = 0;
    	else if ( sequence.size() > 1 )
    	{
    		GPSinfo earlierPoint = 
    			(GPSinfo) sequence.get(sequence.size()-2);
    		totalDuration += earlierPoint.duration( point );
    	}
    }

    /** return GPSinfo implementation. */
    public GPSinfo getGPSinfo(int position) 
	{
    	GPSinfo gps = (GPSinfo) sequence.get( position );
    	return  gps.getNewGPSinfo(); 
	}

    /** Returns the duration of the entire trace in milliseconds. */
    public  long getTotalDuration() { return totalDuration; }
    
//  public double getAverageSpeed() { .. } // [ meters / millis ] ?

    /* Convenience method to save some typing */
    public long getDuration( int from, int to) 
    {
    	if ((from >= 0) || (to < getNrOfElements()))
    	{
    		GPSinfo gpsFrom = (GPSinfo) getPosition( from );
    		return  gpsFrom.duration((GPSinfo) getPosition(to));
    	}
//    	else
    		return 0;
    }

    /* Convenience method to save some typing.
	 * Determines speed in [m/s] from GPSinfo to another. */
    public double getSpeed(int from, int to) 
    { 
    	if ((from >= 0) || (to < getNrOfElements()))
    	{
    		GPSinfo gpsFrom = (GPSinfo) getPosition( from );
    		return  gpsFrom.speed((GPSinfo) getPosition(to));
    	}
//    	else
    		return 0.0;
    }

//  TODO: test method
    public GPStrace getGPStrace()
    {
    	GPStrace fillTrace = new GPStrace();
    	for (Iterator<Position> iterator = sequence.iterator(); iterator.hasNext();)
    		fillTrace.appendGPSinfo( (GPSinfo) iterator.next() );
    	return fillTrace;
    }
    

    private long totalDuration;
}