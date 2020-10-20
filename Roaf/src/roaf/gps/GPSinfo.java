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
package roaf.gps;

import java.util.Date;

/**
 * Point information (most likely) provided by all GPS devices.
 * GPSinfo is supplementing time and date information to a Position,
 * which implies the derived values for duration and speed to 
 * another GPSinfo. 
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public interface GPSinfo extends Position 
{
	/**
	 * This method is a substitute to instantiate a new object 
	 * implementing a GPSinfo. It is left to the implementing 
	 * class to copy itself completely (clone)  
	 * or only with regard to the GPSinfo's attributes
	 * (or a reference to itself without encapsulation!).
	 */
	GPSinfo getNewGPSinfo();

	/**
	 * Add timeStamp to Position. 
	 * The Date holds a UTC time stamp and the programmer
	 * has to be aware of the deviation to the local time.
	 */
	void setDate ( Date timeStamp );

	/** Retrieve the time stamp in a regular Date class. */
	Date getDate();
	
	/** Determines speed in [m/s] to another GPSinfo. */
	double speed( GPSinfo gpsInfo );
	
	/**
	 * duration in milliseconds from this GPSinfo to another.
	/* Positive time indicates that this GPSinfo has 
	 * an earlier time stamp than the argument GPSinfo.
	 */
	long duration( GPSinfo gpsInfo );
}