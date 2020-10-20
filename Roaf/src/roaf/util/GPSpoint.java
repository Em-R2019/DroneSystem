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
package roaf.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import roaf.gps.GPSinfo;
import roaf.gps.Position;

/**
 * @author Kristof Beiglb�ck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class GPSpoint extends GeoPoint implements GPSinfo
{
//	generated dec. 2008 with command line tool: serialver.exe
    static final long serialVersionUID = -2118381783859986383L;

    /**
	 * A GPSpoint represents a GeoPoint with an additional Date.
	 */
	public GPSpoint(double latitude, double longitude, Date timestamp)
	{
		super(latitude, longitude);	// create a GeoPoint
//		catch timestamp == null
		setDate( timestamp );
	}

	public GPSpoint ( GPSpoint point ) 
	{
		super( point.getLatitude(), point.getLongitude());
		setElevation( point.getElevation());
		setName(point.getName());
		setDate(point.getDate());
	}

	/** return a new unreferenced instance as (GPSinfo) for encapsulation */
	public GPSinfo getNewGPSinfo()  { return (GPSinfo)  new GPSpoint( this ); }

	/** return a new unreferenced instance as (Position) for encapsulation */
	public Position getNewPosition(){ return (Position) new GPSpoint( this ); }

	public void setDate ( Date timeStamp ) { this.timeStamp = timeStamp; }

//	TODO: return new Date instance ? referenced one keeps ticking !
	public Date getDate() { return timeStamp; }

	public long duration( GPSinfo gps ) 
	{
//		if (this.getDate().after( gps.getDate() )) ..
		return gps.getDate().getTime() - this.getDate().getTime(); 
	}

	/** Determines speed in [m/s] to another GPSpoint. */
	public double speed( GPSinfo gps ) 
	{
		return (this.distance( gps ) * 1000d )  // [m]
		     / (this.duration( gps ) / 1000d ); // [s] 
	}

//	initialize with current PC time NOW
	private Date timeStamp = new Date();


	/* @see roaf.book.gps.GeoPoint#setLongitude(double)
	 */
	@Override
	public void setLongitude(double longitude)
	{
		super.setLongitude(longitude);
		setNaturalTimeZone();
	}

	/**
	 * Every point described by latitude and longitude implicitly belongs
	 * to a time zone and should be looked up internally (private).
	 * Since there is no formula to determine (changing) political borders of
	 * TimeZones the 'natural' time zone is only guessed by the longitude
	 * in relation to the zero meridian. 
	 */
	private void setNaturalTimeZone()
	{
		double lon = getLongitude();
//		-7.5� to  7.5� is NaturalTimeZone "GMT"
//		 7.5� to 22.5� is NaturalTimeZone "GMT+01:00" etc.
		String timeZone;	// rounded to full hours ":00"
		if ((lon > 0) && (lon < 180))
			timeZone = "GMT+" + (int)(lon/15 + .5) + ":00";
		else
			timeZone = "GMT"  + (int)(lon/15 - .5) + ":00";

		naturalTimeZone = TimeZone.getTimeZone( timeZone );
	}

	/**
	 * The 'natural TimeZone' is purely based on the offset from zero meridian
	 * at a given longitude. Natural in the sense that 'high noon' marks the
	 * highest point of the sun. It may not match the political time zones,
	 * but it evens out for a jet plane doing a global round trip.
	 */
	public  TimeZone getNaturalTimeZone() { return naturalTimeZone; }
	private TimeZone naturalTimeZone;

	public static void main(String[] args)
	{
		Calendar cal = new GregorianCalendar();	// = set current PC time
//		= Date now = new Date() = new Date(System.currentTimeMillis())
		GPSpoint point = new GPSpoint( 49.03081, -12.10321, cal.getTime());
		System.out.println( "What time is it NOW?" );
		System.out.println( formatTime2TimeZone( point.getDate(), null) );
		System.out.println( formatTime2TimeZone( point.getDate(), TimeZone.getTimeZone("UTC")));
		System.out.println( formatTime2TimeZone( point.getDate(), TimeZone.getTimeZone("America/Los_Angeles")));

//		conference call:
		GPSpoint[] location = new GPSpoint[4];
		location[0] = new GPSpoint(  53,   10, cal.getTime() ); location[0].setName("Hamburg ");
		location[1] = new GPSpoint(  34, -118, cal.getTime() ); location[1].setName("Los Angeles ");
		location[2] = new GPSpoint( -22,  -43, cal.getTime() ); location[2].setName("Rio de Janeiro ");
		location[3] = new GPSpoint( -37,  144, cal.getTime() ); location[3].setName("Melbourne ");
		for (int i = 0; i < location.length; i++) {
			System.out.println( "It is now " + formatTime2TimeZone( 
					location[i].getDate(), location[i].getNaturalTimeZone()) + "in " + 
					location[i].getName());
		}
	}

	/**
	 * This method is only for demo purposes in main.
	 * Use TimeZone = null for the current local PC format.
	 */
	public static String formatTime2TimeZone( Date timestamp, TimeZone timeZone )
	{
		if (timeZone == null)
			return timestamp + " (local PC format)";
//		else:
		DateFormat dateFormat = new SimpleDateFormat();
		dateFormat.setTimeZone( timeZone );
		return dateFormat.format( timestamp ) +
		" (" + dateFormat.getTimeZone().getDisplayName() + ") "; 
	}

	/** 
	 * print GPSpoint variable as 
	 * "Thu Nov 05 15:06:49 CET 2009:(51.5332926379222N/0.17299780173150456E)" 
	 */
	public String toString() 
	{
		return getDate() + ":" + super.toString();
	}
        
        
}