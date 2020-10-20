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

/**
 * Universal position with latitude, longitude and elevation,
 * regardless of the underlying coordinate system (default is WGS84).
 * The implementation of distance and direction define transformation
 * (projection) from radians (lat, lon) to kilometers [km].
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public interface Position
{
	/**
	 * This method is a substitute to instantiate a new object
	 * implementing a Position. ( new Position(lat,lon) doesn't
	 * work with an interface.) It is up to the implementing class
	 * to copy itself completely (clone) or only with regard
	 * to the Position attributes latitude, longitude and altitude.
	 * (or a reference to itself, which would not be encapsulated!).
	 */
//	TODO: use .clone in implementing class?
	Position getNewPosition();

	/**
	 *  Latitude / Phi [-90°, 90°] <br>
	 *  south pole = -90° &nbsp; equator = 0° &nbsp; north pole = +90° <br>
	 */
	void setLatitude(double latitude);

	/**
	 *  Longitude / Theta ]-180°, 180°] <br>
	 *  negativ = West &nbsp; 0 = Greenwich, London &nbsp; positiv = East <br>
	 */
	void setLongitude(double longitude);

	/** elevation in meters above sea level */
	void setElevation(double elevation);

	/** get latitude in decimal degrees */
	double getLatitude();

	/** get longitude in decimal degrees */
	double getLongitude();

	/** elevation in meters above sea level */
	double getElevation();

	/** distance in kilometers [km] from this position to another. */
	double distance(Position pos);
//	useful or redundant?
	/* return Position in distance and direction. */
//	Position getDistant( double distance, double direction );

	/**
	 * Move this position in the given direction [rad] and
	 * distance [km] and by setting the new coordinates.
	 */
	void move( double direction, double distance );

	/**
	 * calculate (initial) bearing (in radians clockwise)
	 * from this Positions to the other (pos) <br>
	 */
	double direction(Position pos);
}