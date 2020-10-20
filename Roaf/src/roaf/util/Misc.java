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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public class Misc 
{
  	public static String APPPATH = null;
  	static { APPPATH = getAppPath();}
  	public static final String uniPathSep = "/"; // universal
/*
	System.out.println("  srcPath " + srcPath);
	WORKSPACE = srcPath.substring(0, 
	srcPath.lastIndexOf(uniPathSep));
	int end = Misc.WORKSPACE.lastIndexOf("workspace") + 10;
	WORKSPACE = WORKSPACE.substring( 0, end-1 ) + uniPathSep; 
	System.out.println("WORKSPACE " + WORKSPACE);
//	add this for a valid URL
//	return "file:" + WORKSPACE + uniPathSep;
	return WORKSPACE;
*/
	private final static String BOOK_RESOURCES = APPPATH + "resources/";
	public final static String GPSPATH = BOOK_RESOURCES + "gps/HD/";
//	public final static String MAPPATH = BOOK_RESOURCES + "maps/HD/";
	public final static String ROSPATH = BOOK_RESOURCES + "ros/";
	public final static String NAVPATH = BOOK_RESOURCES + "navigation/";

    /**
     * utility: converts  Rad to a Deg/min/sec String <br>
     * 0.6377190660554178 rad = 36.538611° = 36°32'32"
     */
     public static String Rad2DegMinSec ( double radAngle ) 
     {
           double d = Math.abs( radAngle * 180d / Math.PI );
            int deg = (int) Math.floor( d );
            int min = (int) Math.floor((d - deg) *   60d);
            int sec = (int) Math.floor((d - deg) * 3600d) % 60;
         String sig = (radAngle < 0) ? "-" : "";
         return sig + deg + "\u00B0" + min + "'" + sec + "\"";
     }
     
     /**
      * utility: converts deg° min' sec" to decimal degrees <br>
      * 36°32'32" = 0.6377190660554178
      */
     public static double toDecimalDegrees(int deg,  int min,  int sec) 
     {
     	return ( deg +  min / 60d + sec / 3600d );
     }

     /**
      * utility: converts a direction in radians to a
      * String with compass direction like 36°32'32"N<br>
      */
      public static String bearing ( double dir ) 
      {
          return Rad2DegMinSec( dir ) + " (" + compassRose( dir ) + ")";
      }

     /**
      * utility: converts a direction in radians to a
      * String with compass direction like N NE NNE ...<br>
      */
      public static String compassRose ( double dir ) 
      {
          String rose;
          double nrSeg = 16;                 // nr of segments  (4 8 16)
              dir %= (Math.PI * 2);          // -2pi to +2pi
              dir -= (Math.PI / nrSeg);      // nrSeg 4 -> N = -45° to +45° (not 0° to 90°)
          if (dir < 0)                       //    0 to +2pi
                    dir = ( 2 * Math.PI + dir);
              dir = dir / Math.PI / 2;       //    0 to +1 (full circle)
          int seg = (int) ((dir * nrSeg) +1);
          switch (seg) {
            case  1: rose = "NNE"; break;
            case  2: rose = "NE" ; break;
            case  3: rose = "NEE"; break;
            case  4: rose =  "E" ; break;
            case  5: rose = "SEE"; break;
            case  6: rose = "SE" ; break;
            case  7: rose = "SSE"; break;
            case  8: rose =  "S" ; break;
            case  9: rose = "SSW"; break;
            case 10: rose = "SW" ; break;
            case 11: rose = "SWW"; break;
            case 12: rose =  "W" ; break;
            case 13: rose = "NWW"; break;
            case 14: rose = "NW" ; break;
            case 15: rose = "NNW"; break;
            case 16: rose =  "N" ; break;
            default: rose = "???";          // should never occur
          }
          return rose;
      }

      /** Returns an ImageIcon, or null if the path was invalid. */
      public static ImageIcon createImageIcon(
    		  String path, String description) 
//   change to: File/URL path, String description) 
      {
    	  URL imgURL;
    	  try {
    		  imgURL = new URL(path);
    	  }
    	  catch (MalformedURLException ex) {
    		  System.err.println("Malformed URL: " + path);
    		  return null;
    	  }
    	  if (imgURL != null) {
    		  return new ImageIcon(imgURL, description);
    	  }
    	  else {
    		  System.err.println("Couldn't find file: " + path);
    		  return null;
    	  }
      }

      /**
       * Connect Image object to external data source.
       * The image data is not being loaded by this method!
       * Swing loads the data, when needed. i.e. Graphics.drawImage()
       * For explicit loading use loadMapImage.
       */
      public static Image allocateMapImage( String file )
      {
    	  String mapImageFile = file;
    	  URL    mapImageURL = null;
    	  try {  mapImageURL = new URL(mapImageFile); }
    	  catch (MalformedURLException ex) {
    		  System.err.println("Malformed URL: " + mapImageFile);
    	  }
    	  return Toolkit.getDefaultToolkit().getImage(mapImageURL);
      }

      /**
       * Use this to actually load the image into the memory.
       * The method is waiting for the image to load completely
       * before it returns. This could lead to long delays 
       * in case of loading images via slow connections.
       */
      public static Image loadMapImage( String file, Component observer )
      {
    	  Image image = allocateMapImage(file);
    	  MediaTracker mt = new MediaTracker(observer);
    	  mt.addImage(image, 0);
    	  try { mt.waitForID(0); } 
    	  catch (InterruptedException e) {
    		  System.err.println("Problems with loading " + file);			
    	  }
    	  return image;
      }

      /**
       * see swing tutorial example: IconDemoApp.java
       * at java.sun.com/docs/books/tutorial/uiswing/components/icon.html
       * Resizes an image using a Graphics2D object backed by a BufferedImage.
       * @param srcImg - source image to scale
       * @param w - desired width
       * @param h - desired height
       * @return - the new resized image
       */
      public static Image getScaledImage(Image srcImg, int w, int h){
          BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
          Graphics2D g2 = resizedImg.createGraphics();
          g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          g2.drawImage(srcImg, 0, 0, w, h, null);
          g2.dispose();
          return resizedImg;
      }

      /** open a file chooser to browse file system */
      public static File[] chooseFile( 
    		  String path, FileFilter filter, boolean multiSelect, JFrame frame )
      {
    	  File[] gpxFiles = null;
    	  JFileChooser dialog = new JFileChooser();
    	  dialog.setFileFilter( filter );
    	  dialog.setCurrentDirectory( new File( path ));
    	  dialog.setMultiSelectionEnabled( multiSelect );
    	  if ( dialog.showOpenDialog( frame ) == JFileChooser.APPROVE_OPTION )
    	  {
    		  if ( !multiSelect )	// single file
    		  {
    			  gpxFiles = new File[1];
    			  gpxFiles[0] = dialog.getSelectedFile();
    		  }
    		  else
    			  gpxFiles = dialog.getSelectedFiles();
    	  }
    	  return  gpxFiles;
      }

      public static File chooseFolder( File gotoDirectory, JFrame frame )
      {
    	  JFileChooser dialog = new JFileChooser();
    	  dialog.setCurrentDirectory( gotoDirectory );
    	  dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	  if ( dialog.showOpenDialog( frame ) == JFileChooser.APPROVE_OPTION )
    	  {
    		  File dir = dialog.getSelectedFile();
    		  if ( dir.isDirectory() )
    			  return dir;
    	  }
    	  return null;
      }

      /**
       * Determine absolute path to the workspace folder.
       * Distinguish between a jar application and unpacked class files.
       */
//    TODO: find path (dir) of jar file - without using "workspace"!
      private static String getAppPath()
      {
//  	  Class c = this.getClass();	// safe against refactoring
    	  Class<Misc> c = Misc.class;	// works in static context
    	  URL res = c.getResource("Misc.class");
    	  URL codeSource = c.getProtectionDomain().getCodeSource().getLocation();
    	  String srcPath = codeSource.getPath(); // path without protocol

    	  boolean isJar = false;
    	  if (res.toString().startsWith("jar:") && srcPath.endsWith(".jar"))
    		  isJar = true;
    	  else 
    	  if (res.toString().startsWith("file:") && srcPath.endsWith(uniPathSep)) 
    	  {
    		  srcPath = srcPath.substring(0, srcPath.length() - 1);
    		  isJar = true;
    	  }
    	  if (isJar)
    		  return srcPath;
    	  return null; // couldn't be determined
      }
}