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

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A GPSunit represents a virtual GPS device to ... <BR>
 * - keep and provide the universal time        <BR>
 * - provide the current Position at all times  <BR>
 * - provide the current speed and direction    <BR>
 * - replay a trace                             <BR>
 * - record/log a live trace of all events      <BR>
 * - simulate two- and three dimensional moves
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class GPSunit
{
	/**
	 * Set date, time, latitude, longitude and implementation 
	 * of coordinate system of GPSunit at construction time.
	 * Simulates the external information retrieved and derived from
	 * satellites in the real world, when a GPS device is switched on.
	 * Create a live trace for the units entire lifetime.
	 * No external modification of live trace possible!
	 */
	public GPSunit( GPSinfo initialGPSinfo) 
	{
		liveTrace = new GPStrace();
		setGPSinfo( initialGPSinfo );
	}

	/**
	 * Provide time & space!
	 * Re/set date, time, latitude, longitude and
	 * implementation of GPSunit's coordinate system.
	 * Set unit to GPS device. No moves between waypoints.
	 */
	public void setGPSinfo( GPSinfo gpsInfo )
	{
		currentGPSinfo = gpsInfo.getNewGPSinfo();
//		reset values from old coordinates:
		lastGPSinfo = currentGPSinfo;
		direction = 0; speed = 0; slope = 0;
//		OR better: determine dir, speed and slope from lastGPSinfo
//		lastGPSinfo .. currentGPSinfo;
//		=> direction = ..; speed = ..; slope = ..;
//		stop PLAYBACK mode ? <-> used inside ReplayThread ( contradiction )
//		TODO: reconsider AND TEST side effects on other modes!
//		mode = GPSmode.GPSDEVICE;

//		set date (again), units clock and appendToLiveTrace
		setGPSdate( gpsInfo.getDate() );
	}

	/** Reset GPSunit's clock - date and time. */
	public void setGPSdate( Date newTime )
	{
//		set speed, direction, slope to 0 ?
		currentGPSinfo.setDate( newTime );
//		PC clock plus timeOffset define unit's NOW.
		timeOffset2pc = System.currentTimeMillis() - newTime.getTime();
		appendToLiveTrace( currentGPSinfo );
	}
	
	/** re/set GPSunit's latitude, longitude, elevation. */
//	invoked by ReplayThread.run() and getGPSinfo !
//	maybe private?
	public void setPosition( Position position )
	{
//		set speed, direction, slope to 0 ?
		currentGPSinfo.setLatitude ( position.getLatitude() );
		currentGPSinfo.setLongitude( position.getLongitude());
		currentGPSinfo.setElevation( position.getElevation() );
		appendToLiveTrace( currentGPSinfo );
	}

	/** 
	 * Provides current Date and Position of GPSunit at all times. 
	 * And implicitly executes move from last to current time! 
	 * (~ Heisenberg relation: unit only moves, when observed)
	 */
	public GPSinfo getGPSinfo()
	{
		currentGPSinfo.setDate( new Date( System.currentTimeMillis() - timeOffset2pc ) );
//		do not reset clock: setGPSdate( new Date( ... ) ) !

//		constant motion ..
		double deltaTime = (double) ( 
				currentGPSinfo.getDate().getTime() -
				   lastGPSinfo.getDate().getTime()) / 1000d; // [sec]
		double deltaDistance = speed * deltaTime    / 1000d; // [km] 

		Position pos = lastGPSinfo.getNewPosition();
		pos.move(direction, deltaDistance);        // [rad],[km]
		double height = slope * deltaDistance * 1000d;
//		System.out.println("height " + height);
		pos.setElevation( lastGPSinfo.getElevation() + height );
		setPosition( pos ); // and add to livetrace

		return currentGPSinfo.getNewGPSinfo();
	}

	/**
	 * Simulate constant motion (speed [m/s]) in constant direction from 
	 * current GPSinfo. 'Steer' GPSunit by calling this method repeatedly.
	 * External move is ignored in REPLAY mode.
	 */
	public boolean move( double direction, double slope, double speed )
	{
		     if ( mode == GPSmode.PLAYBACK ) return false;
		else if ( mode == GPSmode.SIMULATION ) 
			simulateMove(direction, slope, speed);
		return true;
	}

	/** Simulate two dimensional move on plane (slope = 0). */
//	add method to keep current slope!?
	public boolean move( double direction, double speed )
	{
		return move( direction, 0, speed ); // slope forced to 0
	}

	/**
	 * Simulate three dimensional motion.
	 * Note: For vertical moves 'up/down the wall' use: 
	 *  pos.setElevation( pos.getElevation() +/- height )
	 * (and set direction = speed = 0)
	 */
	private void simulateMove( double direction, double slope, double speed )
	{ // i.e. set the three move simulation parameters
//		System.out.println("simulateMove( " + direction + ", " + slope + ", " + speed + ")" );
		changeSlope( slope );       // implicitly make move once and change slope
//		this.slope = slope;
		this.direction = direction; //    then change other parameters explicitly
		this.speed = speed; 
	}

	/**
	 * Simulate constant horizontal motion (speed [m/s]) 
	 * immediately beginning from current GPSinfo. 
	 * 'Steer' GPSunit by calling this method repeatedly.
	 */
	private void simulateMove( double direction, double speed ) 
	{
		simulateMove(direction, speed, 0); // slope = 0
	}

	/** Change slope from current position. */
//	changeXXX is analog to simulateMove (for single parameters)
	public void changeSlope(double slope) 
	{
		lastGPSinfo = getGPSinfo(); // implicitly make move
		if (Double.isInfinite(slope) || (Double.isNaN(slope))) {
//			System.err.println( "slope = " + slope 
//				+ " -> unspecified behavior -> set slope to 0" ); 
			this.slope = 0;
		}
		else
			this.slope = slope;
	}
	/** Change direction [rad] from current position. */
	public void changeDirection(double direction) 
	{ 
		lastGPSinfo = getGPSinfo(); // implicitly make move
		this.direction = direction; 
	}

	/** Change speed [m/s] from current position. */
//	TODO: watch for speed without direction (= wrong direction)
	public void changeSpeed(double speed) 
	{
		lastGPSinfo = getGPSinfo(); // implicitly make move
		this.speed = speed;
	}

	public double getDirection() { return direction; }
	public double getSpeed() { return speed; }
	public double getSlope() { return slope; }

	final public void interruptReplay() { replayTrace = null; }

	/** Play back trace immediately. */
	public void replayGPStrace( GPStrace trace ) 
	{
		replayTrace = trace;
		mode = GPSmode.PLAYBACK;

//		TODO: how can replay be interrupted ?
//		USE Thread variable/member ?
//		replayThread = new Thread( this, "replaythread" );
//		replayThread.start();
//		OR anonymous Thread without reference (to kill?)
		new Thread( new ReplayThread(), mode.toString() ).start();
	}

	private class ReplayThread implements Runnable
	{
		public void run()
		{
//			TODO move replay trace into separate method & implement concurrency with live trace
			if (replayTrace != null)
			{
				GPSinfo initialGPS = replayTrace.getGPSinfo(0);
				initialGPS.setDate( getGPSinfo().getDate() );   // conserve time
				setGPSinfo( initialGPS );                       //  & reset unit
				for (int pos = 1; pos < replayTrace.size(); pos++) 
				{
//					if ( GPSmode != REPLAY ) END/KILL THREAD
//					recheck for every new element and end replay if:
					if (replayTrace == null) break;
//					FOLGENDE ZWEI TRACES GRAFISCH BEGUTACHTEN!! lehrreich!
					GPSinfo gps0 = replayTrace.getGPSinfo( pos );
//					TODO: the following should be better, adopt duration... 
//					(geringere Fehlerfortpflanzung!)
//					GPSinfo gps0 = getGPSinfo();
					long duration = 0; 
					double direction = 0, distance = 0, speed = 0, slope = 0, height = 0;
//					System.out.println(
//						gps0.getDate() + " " + gps0.getElevation() + "m " +
//						gps0.getLatitude() + " / " + gps0.getLongitude());
					if ( pos+1 < replayTrace.size() )
					{
						GPSinfo gps1 = replayTrace.getGPSinfo( pos+1 );
//						optimize duration with timestamps from first point to PC clock
						duration  = gps0.duration ( gps1 );                 // [ms]
						direction = gps0.direction( gps1 );                 // [rad]
						speed     = gps0.speed    ( gps1 );                 // [m/s]
//						simulateMove( direction, speed);                    // 2D
						height = gps1.getElevation() - gps0.getElevation(); // [m]
						distance  = gps0.distance ( gps1 ) * 1000d;         // [m]
						slope = height / distance;
						simulateMove( direction, slope, speed);             // 3D
					}
					else // last GPSinfo of trace
					{
						simulateMove( direction, 0); // stop moving
						duration = 0;
					}
					try { Thread.sleep( duration ); } 
					catch (InterruptedException e) { e.printStackTrace(); }
				}
			}
			mode = GPSmode.SIMULATION; // end of PLAYBACK 
		}
	}

	/**
	 * Method body can be used to analyze new GPSinfo.
	 * TODO: implement GPS recording modes: 
	 * a. < [m]  b. < [sec]  c. < [rad]
	 * d. [auto (consider speed)]
	 */
	private void appendToLiveTrace( GPSinfo gps )
	{
//		a. new Position less than x meters  from last Position 
//		b. new Date  is less than x seconds from last Date
//		c. direction is less than x radians from last
//		then REPLACE last by new gps
//		else APPEND  new gps
		liveTrace.appendGPSinfo( gps );
	}

	public GPStrace getLiveTrace() { return liveTrace.getGPStrace(); }

	private GPStrace replayTrace = null, liveTrace = null;  

	/** possible modes of GPSunit. */
	public enum GPSmode { SIMULATION, PLAYBACK, GPSDEVICE }
	/** current mode of GPSunit. */
	private GPSmode mode = GPSmode.SIMULATION;	// default

//	TODO: external client should NOT have to set and see mode!
//	the pending usage of setGPSinfo(gpsInfo) > mode = GPSDEVICE
//	has to implicitly determine direction and speed!
//	public void setGPSmode (GPSmode mode) 
//	{
//		check validity ..
//		TODO: -> end PLAYBACK etc.
//		this.mode = mode;
//	}

	public  GPSmode getGPSmode() { return mode; }

	/**
	 * Loads a GPX file, validates the structure and 
	 * converts the contained traces into GPStrace[],
	 * (routes into Route[]) waypoints into GPSinfo[]
	 * and the boundBox into boundBox - if available.
	 */
	public boolean loadGPXfile( File gpxFile )
	{
		Document traceDOM = loadGPX2DOM( gpxFile );
		if ( traceDOM != null )
		{
			System.out.println( "Successfully loaded into GPSunit: " + gpxFile);  
			Node rootNode = traceDOM;
			if ((rootNode.getChildNodes().getLength() == 1)
			&&  (rootNode.getChildNodes().item(0).getNodeType() == 1)	// = Element
			&&  (rootNode.getChildNodes().item(0).getNodeName() == "gpx"))
			{
				System.out.println("Successfully loaded into DOM document.");
				if ( !parseDOM( traceDOM ) )
				{
					System.err.println(
							"No relevant gpx information found in " + gpxFile );
					return false;
				}
			}
		}
		else
		{
			System.err.println( "File could not be loaded: " + gpxFile );  
			return false;
		}
		return true;
	}

	/** Get gpxTraces after loading gpx file. <BR>
	 *  Check against null in case gpx doesn't have 'trk's. */
	public GPStrace[] getGPXtraces() { return gpxTraces; }

	/** Get gpxPoints after loading gpx file. <BR>
	 *  Check against null in case gpx doesn't have any 'wpt'. */
	public Position[] getGPXpoints() { return gpxPoints; }

	/** Get gpxBoundBox[] after loading gpx file. <BR>
	 *  Check against null in case gpx doesn't have 'bounds'. */
	public Rectangle2D.Double getGPXboundBox() { return gpxBoundBox; }

	private GPStrace[] gpxTraces;
	private Position[] gpxPoints;
//	private Route[] gpxRoutes;
	/**
	 * gpxBoundBox orientation: <BR>
	 * Rectangle2D.Double( lonW, latS, lonE-lonW, latN-latS)
	 */
	private Rectangle2D.Double gpxBoundBox;

	/**
	 * Parse and convert to gpxTraces, gpxRoutes, gpxPoints
	 * and the bounds into gpxBoundBox - if available.
	 * Returns true if any one of the above is found.
	 */
	private boolean parseDOM( Node root )
	{
//		reset values before parsing
		gpxTraces = null; // gpxRoutes = null; 
//		TODO: automatically create routes from traces, if timestamps are missing
		gpxBoundBox = null;  gpxPoints = null;

		Node rootNode = root;
		Element gpxNode = (Element) rootNode.getChildNodes().item(0);
//		(temporary) helper method - may be removed
//		printNode( gpxNode );

		NodeList trkNodes = null, wptNodes = null, metaNodes = null;
		if (gpxNode.hasChildNodes())
		{
			trkNodes = gpxNode.getElementsByTagName( "trk" );
			if  (trkNodes  != null) extractGPXtraces( trkNodes );
//			if ((gpxTraces != null) && (gpxTraces.length > 1))
//				System.out.println( "created gpxTraces[" + gpxTraces.length + "]");

			wptNodes = gpxNode.getElementsByTagName( "wpt" );
			if  (wptNodes  != null) extractGPXpoints( wptNodes );
//			if ((gpxPoints != null) && (gpxPoints.length > 1))
//				System.out.println( "created new gpxPoints[" + gpxPoints.length + "]");

			metaNodes = gpxNode.getElementsByTagName( "metadata" );
			if  (metaNodes != null) extractGPXboundBox( metaNodes );
//			if ((gpxPoints != null) && (gpxPoints.length > 1))
//				System.out.println( "created new gpxPoints[" + gpxPoints.length + "]");

			if ((gpxTraces   != null) 
			||  (gpxPoints   != null) 
			||  (gpxBoundBox != null)) return true;
		}
		return false;
	}

	/** Extract gpxBoundBox[] from <metadata> in gpx file */
	private void extractGPXboundBox( NodeList metaNodes ) 
	{
		if (( metaNodes != null ) && ( metaNodes.getLength() > 0)) 
		{	// only one metadata block
			Element metaData = (Element) metaNodes.item(0);
			NodeList bounds = metaData.getElementsByTagName("bounds");
			if (bounds.getLength() == 1)
			{
				Element box = (Element) bounds.item(0);
				double 
				latN = Double.parseDouble( box.getAttribute("maxlat")),
				lonE = Double.parseDouble( box.getAttribute("maxlon")),
				latS = Double.parseDouble( box.getAttribute("minlat")),
				lonW = Double.parseDouble( box.getAttribute("minlon"));
				gpxBoundBox = new Rectangle2D.Double( 
						lonW, latS, lonE-lonW, latN-latS);
				System.out.println( "gpxBoundBox: " + gpxBoundBox );
			}
		}
	}

	/** Extract gpxPoints[] from <wpt>s in gpx file */
	private void extractGPXpoints( NodeList wptNodes ) 
	{
		if ((wptNodes != null) && ( wptNodes.getLength() > 0)) 
		{
		gpxPoints = new Position[wptNodes.getLength()];
		for (int i = 0; i < wptNodes.getLength(); i++) 
		{
//			Position gps = currentGPSinfo.getNewPosition(); // ignore time stamp
			GPSinfo  gps = currentGPSinfo.getNewGPSinfo();
			Element wayPoint = (Element) wptNodes.item(i);

			gps.setLongitude( Double.parseDouble(wayPoint.getAttribute("lon")) );
			gps.setLatitude ( Double.parseDouble(wayPoint.getAttribute("lat")) );

//			eventuell nur singular notwendig ? statt NodeList ?
			NodeList wayPtElevs = wayPoint.getElementsByTagName("ele");
			if (wayPtElevs.getLength() == 1)
				gps.setElevation( Double.parseDouble( 
						wayPtElevs.item(0).getFirstChild().getNodeValue()));
/*
			NodeList wayPtTimes = wayPoint.getElementsByTagName("time");
			if (wayPtTimes.getLength() == 1)
				gps.setDate( convertGPXtimeStamp2Date(
						wayPtTimes.item(0).getFirstChild().getNodeValue()));

			NodeList wayPtNames = wayPoint.getElementsByTagName("name");
			if (wayPtNames.getLength() == 1)
				System.out.println( "point name: " // unused 
						+ wayPtNames.item(0).getFirstChild().getNodeValue());
*/
			gpxPoints[i] = gps;
			System.out.println( "add Position #" + i + ": " + gps);
		}}
	}

	/** Extract gpxTraces[] from <trk>s in gpx file */
	private void extractGPXtraces( NodeList trkNodes ) 
	{
		if ((trkNodes != null) && ( trkNodes.getLength() > 0)) 
		{
		gpxTraces = new GPStrace[trkNodes.getLength()];
		for (int i = 0; i < trkNodes.getLength(); i++) 
		{
		System.out.println( "track #" + i  + " -----" );
		Element track = (Element) trkNodes.item(i);
/*
		String trackName = "";	// is not stored in class (remains in DOM)
		NodeList trkNames = track.getElementsByTagName( "name" );
		if ((trkNames != null) && (trkNames.getLength() == 1))
			trackName = trkNames.item(0).getFirstChild().getNodeValue();
		System.out.println( "track name: " + trackName );
*/
		System.out.print( "track #" + i + " contains ");
		NodeList trkSegNodes = track.getElementsByTagName( "trkseg" );
		System.out.println( trkSegNodes.getLength() + " <trkseg>" );
		if (trkSegNodes.getLength() > 0)
		{
		gpxTraces[i] = new GPStrace();
//		int capacity = 0;
		System.out.println( "created GPStrace[" + i + "]");

		for (int j = 0; j < trkSegNodes.getLength(); j++) 
		{
		Element  trkSegment = (Element) trkSegNodes.item(j);
		NodeList trkPoints = trkSegment.getElementsByTagName( "trkpt" );

		System.out.print( "track segment #" + j + " contains ");
		System.out.println( trkPoints.getLength() + " <trkpt>s" );

//		capacity += trkPoints.getLength();
//		ArrayList method that could be implemented:
//		gpsTraces[i].ensureCapacity( capacity ); 

		for (int k = 0; k < trkPoints.getLength(); k++) 
		{   // create new instance
			GPSinfo gps = currentGPSinfo.getNewGPSinfo();
			Element trackPoint = (Element) trkPoints.item(k);

			gps.setLatitude ( Double.parseDouble(trackPoint.getAttribute("lat")) );
			gps.setLongitude( Double.parseDouble(trackPoint.getAttribute("lon")) );

			NodeList trkPtElevs = trackPoint.getElementsByTagName("ele");
			if (trkPtElevs.getLength() == 1)
				gps.setElevation( Double.parseDouble( 
						trkPtElevs.item(0).getFirstChild().getNodeValue()));

			NodeList trkPtTimes = trackPoint.getElementsByTagName("time");
			if (trkPtTimes.getLength() == 1)
				gps.setDate( convertGPXtimeStamp2Date(
						trkPtTimes.item(0).getFirstChild().getNodeValue()));

			gpxTraces[i].appendGPSinfo( gps );
//			System.out.println( "appendGPSinfo #" + k + ": " + gps);
		}
//		ArrayList method could be implemented:
//		gpsTraces[i].trimToSize();
		}}}}
	}

	/**
	 * The UTC time and date supplied in the gpx format <BR>
	 * "2007-08-04T16:49:37Z" (YYYY-MM-DDThh:mm:ssZ), <BR> 
	 * where T stands for time and Z for zero meridian.
	 */
	private Date convertGPXtimeStamp2Date( String gpxTimestamp )
	{
		gpxFormat.setTimeZone( TimeZone.getTimeZone("UTC") );
		try { return gpxFormat.parse( gpxTimestamp ); }
		catch (ParseException e) { e.printStackTrace(); }
		return null;
	}

	private Document loadGPX2DOM( File gpxFile ) 
	{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      factory.setValidating(true);   
//      factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return  builder.parse( gpxFile );
        } catch (SAXParseException spe) {
            // Error generated by the parser
            System.out.println("\n** Parsing error" + ", line " +
                spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage());
            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) x = spe.getException();
            x.printStackTrace();
        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;
            if (sxe.getException() != null) x = sxe.getException();
            x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
        return null;
	}

	final private DateFormat gpxFormat = 
		new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );

	/** PC clock plus timeOffset define unit's NOW. */
//	should survive computer hybernation! 
	private long timeOffset2pc;
	/** 
	 * To simulate motion the GPSunit needs  
	 * direction [0.0 = north], slope and speed in [m/s]. 
	 * At creation the unit is not moving.
	 * slope = height / distance
	 * horizontal: slope = 0 
	 * Percentage on road signs 10 % = 10m / 100m (up: 0.1 down: -0.1) 
	 */
	private double direction = 0, speed = 0, slope = 0;
	/** Store last GPSinfo with every change of constant motion in
	 *  order to determine current GPSinfo for external clients. */
	private GPSinfo lastGPSinfo;
	/** The implementation of currentGPSinfo is kept as a 
	 * single instance over the GPSunits entire lifetime.
	 * It holds place and time, provides the coordinate 
	 * system with conversion formulas. */
	private GPSinfo currentGPSinfo;
}