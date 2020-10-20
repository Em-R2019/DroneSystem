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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * MapScalePanel is only visible and usable in this package,
 * since it was exclusively designed for MapPanel.
 * 
 * The Panel must keep it's scaling functionality,
 * even when NOT visible or added to parent...!
 * 
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
class MapScalePanel extends JPanel implements SwingConstants
{
	private static final long serialVersionUID = 1669610363664811497L;

	/**
	 * All values have to be provided at construction time.
	 * (all class members are package visible?).
	 */
	MapScalePanel( double scaleStart, double scaleLength, 
			     	  int pixelStart,    int pixelLength,
			     	  int orientation)
	{
		setOrientation(orientation);
		setScale(scaleStart, scaleLength, pixelStart, pixelLength);
	}
	
	/* 
	 * orientation has to be set with 
	 * constructor and can not modified.
	 */
	private void setOrientation(int orientation)
	{
		if (( orientation == HORIZONTAL ) || ( orientation == VERTICAL ))  
			this.orientation = orientation;
		else 
		{
//			TODO throw MappingException (add to package)...
			System.err.println
			("Only HORIZONTAL or VERTICAL orientation for MapScalePanel");
			System.exit(1);  // too radical
		}
	}

	void setPreferredLength( int length)
	{
		if (orientation == HORIZONTAL )
			setPreferredSize(new Dimension(length, size));
		if (orientation == VERTICAL )
			setPreferredSize(new Dimension(size*2, length));
	}

	void setScale( double scaleStart, double scaleLength, 
					  int pixelStart,    int pixelLength )
	{
		this.scaleStart  = scaleStart; 
		this.scaleLength = scaleLength; // always positiv?
		this.pixelStart  = pixelStart;
		this.pixelLength = pixelLength; // always positiv?

		calculateScale(scaleLength, pixelLength);		
	}

	/** Set lineSpace, scaleUnit, scaleFraction for drawing. */
	private void calculateScale( double scaleLength, int pixelLength )
	{
		double diff = pixelLength;
//		for (int i = 1; i >= -1; i--)	// +1..0..-1
		for (int i = 2; i >= -2; i--)	// +2..0..-2
		{
			double mag = Math.pow(10.0,getMagnitude(scaleLength)+i); 		
			for (int frac=1; frac<5; frac*=2) 	// 1 2 4
			{
				double lSpace = pixelLength / (scaleLength / mag ) / frac;
				if ( Math.abs(lSpace - prefLineSpace) < diff )
				{
					lineSpace = lSpace;
					scaleUnit = mag;
					scaleFraction = frac;
					decLineSpace  = mag / frac;
					firstNotch = // first full tick <= scaleStart 
						Math.floor( scaleStart / scaleUnit) * scaleUnit;
					firstPixel = dec2pix(firstNotch );
					diff = Math.abs(lSpace - prefLineSpace);
				}
			}		
		}
	}

	/**
	 * Project a decimal value on the pixel representation.
	 */
	int dec2pix( double decimal )
	{
//		first check if scale contains value at all?
//		and return what?
//		OR check pixel ranges in drawing! (~clipBounds, contains..)

		double offset = decimal - scaleStart;
		double pixelPosition = (( offset / scaleLength ) * pixelLength);
		if (orientation == HORIZONTAL)
			pixelPosition += pixelStart; 
		else // if (orientation == VERTICAL) (bottom up!)
			pixelPosition = pixelStart + pixelLength - pixelPosition;
		int pix = (int) Math.round( pixelPosition );
//		System.out.println("dec2pix(" + decimal + ") = " + pix);
//		System.out.println("pix2dec(" + pix + ") = " + pix2dec(pix));
		return pix;
	}

	/**
	 * Project a pixel on the decimal scale.
	 *  reverse function of dec2pix
	 *  (theoretically) with 
	 *  pixel == pix2dec( dec2pix( pixel ))
	 *  and vice versa...
	 */
	double pix2dec( int pixel )
	{
		pixel = pixel - pixelStart;
		double pixelPosition = (double) pixel / pixelLength ;
		if (orientation == HORIZONTAL)
			return scaleStart + pixelPosition * scaleLength ;
		else // if (orientation == VERTICAL) (bottom up!)
			return scaleStart - pixelPosition * scaleLength + scaleLength ;
	}

	protected void paintComponent(Graphics g) 
	{		
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight()); }
		drawScale(g);
	}

	private void drawScale(Graphics g) 
	{
		int height = getHeight(), width  = getWidth();
		g.setColor( Color.BLACK );		// setFrameColor
		if (orientation == HORIZONTAL)
			g.drawRect(pixelStart, 1, pixelLength, height-2);
		if (orientation == VERTICAL)
			g.drawRect(1, pixelStart, width-2, pixelLength);

		g.setColor( Color.BLACK );					 // setLineAndFontColor
		g.setFont(new Font("SansSerif", Font.PLAIN, 10));	// setScaleFont
		double start = firstNotch, draw = start;
		double   end = scaleStart + scaleLength;
		int tickNr = 0; // first tick at full unit
		while(draw <= end)
		{
			int to = 10;
			int  tickAt = dec2pix(draw);
			if ((tickAt > pixelStart ) 
			&&  (tickAt < pixelStart + pixelLength))
			{
				if (tickNr%scaleFraction == 0)						// full
				{
//					adjust rounding with magnitude with formatDouble
					if (orientation == HORIZONTAL)
						g.drawString(roundDouble(draw,3) + "", tickAt+5, 15);
					if (orientation == VERTICAL)
						g.drawString(roundDouble(draw,4) + "", 5, tickAt-5);
				}
				else if (tickNr%(scaleFraction/2) == 0) to = 15;	// half
				else if (tickNr%(scaleFraction/4) == 0) to = 20;	// quarter

				if (orientation == HORIZONTAL)
					g.drawLine(tickAt, height, tickAt, to);
				else if (orientation == VERTICAL)
					g.drawLine( width, tickAt, to, tickAt);
			}
			draw += decLineSpace;
			tickNr++;
		}		
	}

	/** Helper method to cut digits off double values. */
//	TODO: change to:
//	static String formatDouble( double decimal, int digits )
//	convert to String and allign lengths (8.719 8.72 8.721)
	static Double roundDouble( double decimal, int digits )
	{
		double mag = Math.pow( 10.0, digits);
//		return Math.floor(decimal * mag) / mag;
		return Math.round(decimal * mag) / mag;
	}

//	public void setTicksAt(int preferredLineSpace) 
//	{
//		this.ticksAt = preferredLineSpace;
//		setScale(..);
//		repaint();
//	}
//	public static void main(String[] args)
//	{
//		generate H and V scale
//		convert decimal value into pixel value
//	}
	
	private int getMagnitude( double decimal )
	{
		if (decimal == 0) return 999;	// TODO throw Exception
		int trunc = Math.abs((int) decimal), mag = 1, sig = 1;
		if (trunc >= 1)	{ mag = 0; sig = -1; }
		while ((trunc < 1 ) || (trunc > 10)) 
			trunc = Math.abs((int)(decimal*Math.pow(10.0,sig*mag++)));
		return (sig > 0)? -sig * --mag: -sig * mag;
	}
	
	/** allign with fontsizes */
	int  size = 25;
	private int orientation = HORIZONTAL; // default

	/** the actual values and range of the scale */
	double scaleStart, scaleLength;
	double firstNotch;	 // first full tick left of grid

	/** the pixel values and range of the scale */
	int pixelStart, pixelLength;
	int firstPixel = 0;  //	pixel position of firstNotch
	
	/** the suggested nr of pixel between two lines (vert and horiz).
	 * default: pixels in one half inch > independant of screenresolutions*/
	int prefLineSpace = Toolkit.getDefaultToolkit().getScreenResolution()/2;
	/** lineSpace defines the number of pixels between two lines */
	double decLineSpace = 0.0;	// decimal for precise calculation of lineSpace:
	double lineSpace = prefLineSpace;    // default 
	/** scaleUnit is a decimal value 10^(+-)n as the unit of the scale */
	private double scaleUnit = 1.0 ;
	/** scaleFraction defines the fractions of the scaleUnit between two lines */
	private int scaleFraction = 1 ;
	/* here's the picture:
		---+<lineSpace>+<lineSpace>+<lineSpace>+<lineSpace>+--- 
		---+<scaleUnit>+-----------+-- scaleFraction = 1 --+---
		---+<----- scaleUnit ----->+---------------- = 2 --+---
		---+<----------------- scaleUnit ----------- = 4 ->+---
	 */	
}