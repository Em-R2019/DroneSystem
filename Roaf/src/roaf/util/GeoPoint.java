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
package roaf.util;

import roaf.gps.Position;

/**
 *  A GeoPoint representing a point on earth's surface,
 *  defined by latitude and longitude in decimal degrees
 *  or by phi and theta in radians. <br>
 *
 *  latitude - phi  <br>
 *  North (+) [0.0&ordm; ,&nbsp;+90.0&ordm;] deg or [0 to +pi/2] rad<br>
 *  South (-) ]0.0&ordm; ,&nbsp;-90.0&ordm;] deg or ]0 to +pi/2] rad<br>
 *
 *  longitude - theta  <br>
 *  East (+) [0.0&ordm; ,&nbsp;+180.0&ordm;] deg or [0 to +pi] rad<br>
 *  West (-) ]0.0&ordm; ,&nbsp;-180.0&ordm;[ deg or ]0 to +pi[ rad<br>
 *
 *  A map datum is not explicitly involved. (default WGS-84)
 *  
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class GeoPoint implements Position, java.io.Serializable
{
//	generated dec. 2008
    static final long serialVersionUID = -5559981222772287271L;

    /** a GeoPoint has to have latitude and longitude */
	public GeoPoint (double latitude, double longitude)
	{
		setLatitude(  latitude );
		setLongitude( longitude );
	}

	/** Create new GeoPoint with the same values. */
	public GeoPoint ( GeoPoint point)
	{
		setLatitude(  point.getLatitude() );
		setLongitude( point.getLongitude());
		setElevation( point.getElevation());
		setName(point.getName());
	}

	/** Create new GeoPoint with the same values. */
	public GeoPoint ( Position point)
	{
		setLatitude(  point.getLatitude() );
		setLongitude( point.getLongitude());
		setElevation( point.getElevation());
	}

	/**
	 * plausibility check and fitting values, if out of range.
	 * Can also be used to changeLatitude of a GeoPoint.
	 */
	public void setLatitude( double latitude )
	{
		latitude %= 360d;
		double absLat = Math.abs( latitude );

		if (( absLat > 90 ) && ( absLat <= 270 )) {
				 if (latitude >   90d) latitude =  180d - latitude ;
			else if (latitude <  -90d) latitude = -180d - latitude ;
		}
		else if ( absLat > 270 ) {
				 if (latitude >  270d) latitude = latitude - 360d ;
			else if (latitude < -270d) latitude = latitude + 360d ;
		}
		// )270,360( or )-270, -360( remaining
		this.latitude = latitude;
	}

	/**
	 * plausibility check and fitting values, if out of range.
	 * Can also be used to changeLongitude of a GeoPoint.
	 */
	public void setLongitude( double longitude )
	{
		longitude %=  360d;
		     if (longitude >   180) longitude -= 360;
		else if (longitude <= -180) longitude += 360;
		this.longitude = longitude;
	}

	public void setElevation( double elevation )
	{
		this.elevation = elevation;
	}

	public double getLatitude()  { return latitude; }
	public double getLongitude() { return longitude; }
	public double getElevation() { return elevation; }

	/**
	 * return a new unreferenced instance as (Position) for encapsulation
	 */
	public Position getNewPosition()
	{
		return (Position) new GeoPoint( this );
//		or improve by cloning or reflection for 'deeper' copies.
	}

//	phi and theta are not stored as member variables (redundant information)
//	add setPhi() and setTheta() to set lat and long if needed!
//	with deg = Math.toDegrees(rad);

	/** get latitude  in radians */
	public double getPhi()  { return Math.toRadians( latitude ); }
	/** get longitude in radians */
	public double getTheta(){ return Math.toRadians( longitude ); }

	public String getName() { return name; }
	public void   setName(String name) { this.name = name; }

//	TODO: move ID to Node!!
	public Integer getID() { return ID; }
	public void   setID( Integer ID ) { this.ID = ID; }
	private Integer ID = -1;

	 /**
	  * from: Haversine formula - R. W. Sinnott, "Virtues of the Haversine", <br>
	  *       Sky and Telescope, vol 68, no 2, 1984 <br>
	  *       www.census.gov/cgi-bin/geo/gisfaq?Q5.1 <br>
	  * <p>
	  * Since the earths radius varies between about 6,378km (equatorial)
	  * and 6,356km (polar), errors might be up to about 0.11% at the
	  * equator (0.24% at poles). If thats not good enough, the ellipsoidal
	  * (or oblate spheroidal) shape of the earth could be approximated by
	  * using R = 6378 - 22.sin((lat1+lat2)/2). </p>
	  * another source says:
	  * "The Haversine formula assumes the earth is a perfect sphere which
	  * it is not, it is more ellipsoidal. Therefore this formula will work
	  * well for small distances and as we are working with just UK post codes
	  * then this is acceptable error margin. If you are looking to develop a
	  * project that uses world wide distances then you would probably be best
	  * looking at the ***Vencety formula***. "
	  *
	 public double distance( Position pos )
	 {
		 final int EARTH_RADIUS = 6371;   // [km]
		 double dLat, dLong;   // deltas
		 double a, c;          // temporary values
		 double dist;
		 double thisTheta = getTheta();
		 double   thisPhi = getPhi();
//		 Position has no getTheta() nor getPhi() method!
		 double  posTheta = Math.toRadians( pos.getLongitude() );
		 double    posPhi = Math.toRadians( pos.getLatitude() );

		 dLat  = posPhi   - thisPhi;
		 dLong = posTheta - thisTheta;
		 a = Math.sin(dLat/2)  * Math.sin(dLat/2) +
		     Math.cos(thisPhi) * Math.cos(posPhi) *
		     Math.sin(dLong/2) * Math.sin(dLong/2)   ;
		 c = Math.atan2(Math.sqrt(a) * 2, Math.sqrt(1-a));
		 dist = EARTH_RADIUS * c;
		 return dist;
	 }
	  */

	/** earth radius in [km] for distance and move methods */
	private double radius = 6378.14;

	/**
	 * distance in kilometer [km]
	 * implementation by Larry Bogan:
	 * www.go.ednet.ns.ca/~larry/bsc/jslatlng.html
	 */
	public double distance( Position pos )
	{
		double granularity = 1e-7;	// identical positions result in NaN
		if ((Math.abs(this.latitude  - pos.getLatitude())  < granularity )
		 &  (Math.abs(this.longitude - pos.getLongitude()) < granularity ))
			return 0;				// or use equals method instead?

		double f = 1/298.257,
		lat1  = getPhi(), long1 = getTheta(),
		lat2  = Math.toRadians( pos.getLatitude() ),
		long2 = Math.toRadians( pos.getLongitude() ),
		F = (lat1+lat2)/2,
		G = (lat1-lat2)/2,
		lambda = (long1-long2)/2,
		S = Math.pow(Math.sin(G) * Math.cos(lambda),2)
		  + Math.pow(Math.cos(F) * Math.sin(lambda),2),
		C = Math.pow(Math.cos(G) * Math.cos(lambda),2)
		  + Math.pow(Math.sin(F) * Math.sin(lambda),2),
		omega = Math.atan(Math.sqrt(S/C)),
		R = Math.sqrt(S*C)/omega,
		D = 2*omega*radius,
		H1 =(3*R-1)/2/C,
		H2 =(3*R+1)/2/S,
		s = D*(1 + f * H1 * Math.pow(Math.sin(F) * Math.cos(G),2)
				 - f * H2 * Math.pow(Math.cos(F) * Math.sin(G),2));
		return s;
	}

	/**
	 * implementation by Larry Bogan:
	 * www.go.ednet.ns.ca/~larry/bsc/jslatlng.html
	 * distance in kilometer [km]
	 */
	public void move( double direction, double distance )
	{
		double radian = 180/Math.PI,
		e = 0.08181922,
		lt0 = getPhi(),
		R = radius*(1-e*e)/Math.pow((1-e*e*Math.pow(Math.sin(lt0),2)),1.5),
		psi = distance / R,
		phi = Math.PI/2 - lt0,

		arccos = Math.cos(psi) * Math.cos(phi)
			   + Math.sin(psi) * Math.sin(phi) * Math.cos(direction),
//			   + Math.sin(psi) * Math.sin(phi) * Math.cos(-direction),
		latA = (Math.PI/2 - Math.acos(arccos)) * radian,

		lg0 = getTheta(),
		arcsin = Math.sin(direction) * Math.sin(psi) / Math.sin(phi),
//		arcsin = Math.sin(-direction) * Math.sin(psi) / Math.sin(phi),
		longA = ( lg0 + Math.asin(arcsin) ) * radian;

		setLatitude (  latA );
		setLongitude( longA );
	}

	public double direction ( Position pos )
	{
//		TODO Achtung bei Garmins MapSource bearing in andere Richtung!!
//		von (49.03081, -12.10321) nach (49.29674, -11.55337)
//		Rgbg-Neumarkt hier: 53° garmin: 307°  (vgl. JavaGPS)
		double x, y ,
		thisTheta = getTheta(),
		  thisPhi = getPhi(),
		 posTheta = Math.toRadians( pos.getLongitude()),
		   posPhi = Math.toRadians( pos.getLatitude() );

		y = Math.sin(thisTheta - posTheta) * Math.cos(posPhi);
		x = Math.cos(thisPhi)  * Math.sin(posPhi) -
		Math.sin(thisPhi)  * Math.cos(posPhi) *
		Math.cos(thisTheta - posTheta);
		double dir = Math.atan2(-y, x);
		return dir;  // clockwise heading.. North = 0° E = 90° S = 180° W = -90° = 270°
	}

	public double degreeDirection ( Position pos )
	{
		double dir = Math.toDegrees( direction( pos ));
		if   ( dir < 0 ) dir += 360;
		return dir;
	}

    /**
     * Determines whether or not two GeoPoints are equal.
     * Two instances of <code>GeoPoint</code> are equal if the values of
     * their <code>latitude</code> and <code>longitude</code> member fields,
     * representing their position in the coordinate space, are the same.
     */
//	 public boolean equals(Object gp)	// add instanceOf for a deeper check ..
	 public boolean equals(GeoPoint gp)
	 {
		 return (latitude == gp.latitude ) && (longitude == gp.longitude);
	 }

	/** prints GeoPoint variable as "(36.538611N/121.7975W)"
	 *  N and E have positive, S and W negative coordinates. */
	public String toString()
	{
		double  latAbs = Math.abs( getLatitude() ) ;
		double longAbs = Math.abs( getLongitude() ) ;
		return "(" +  latAbs + ( ( getLatitude()  > 0) ? "N/" : "S/")
				   + longAbs + ( ( getLongitude() > 0) ? "E)" : "W)") ;
	}

	/** a position on the globe's surface in decimal degrees */
	private double latitude, longitude, elevation = 0;

	/** name for GeoPoint like "Hamburg, Germany" */
	private String name;
}