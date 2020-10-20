/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem.util;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import roaf.util.GeoPoint;

/**
 *
 * @author 20172805
 */

public class Utilities {
    public static byte[] byteCon(byte[] a,byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    
    public static List<Integer> allIndices(ArrayList<Integer> list, int i){
        int first = list.indexOf(i);
        if (first != -1){
            List<Integer> result = new ArrayList<>(0);
            result.add(first);
            int last = list.lastIndexOf(i);
            if (last != first){
                for (int j = first+1; j < last; j++){
                    if (list.get(j) == i){
                        result.add(j);
                    }
                }
                result.add(last);
            }
            return result;
        }
        else {
            return null;
        }
    }
    
    public static double randomDouble(double left, double right) {
        double generatedDouble = left + new Random().nextDouble() * (right - left);
        return generatedDouble;
    }
    
    public static byte [] doublestoBytes(double [] doubles){
        ByteBuffer bb = ByteBuffer.allocate(doubles.length * 8);
        for(double d : doubles) {
            bb.putDouble(d);
        }
        byte[] bytearray = bb.array();
        return bytearray;
    }
    
    public static int[] combine(int[] a, int[] b){
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    public static Color[] combineColor(Color[] a, Color[] b){
        int length = a.length + b.length;
        Color[] result = new Color[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    public static GeoPoint[] combineGPS(GeoPoint[] a, GeoPoint[] b){
        int length = a.length + b.length;
        GeoPoint[] result = new GeoPoint[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    public static byte [] DoublestoBytes(Double [] doubles){
        ByteBuffer bb = ByteBuffer.allocate(doubles.length * 8);
        for(double d : doubles) {
            bb.putDouble(d);
        }
        byte[] bytearray = bb.array();
        return bytearray;
    }
    
    public static double [] bytestoDoubles(byte [] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int blength = bytes.length;
        double[] doubles = new double[blength / 8];
        for(int i = 0; i < doubles.length; i++) {
            doubles[i] = bb.getDouble();
        }
        return doubles;
    }
    
    public static GeoPoint copyGeopoint(GeoPoint point){
        GeoPoint result = new GeoPoint(point.getLatitude(),point.getLongitude());
        result.setElevation(point.getElevation());
        return result;
    }
    
    public static int[] Geo2Int (GeoPoint point,double size,int n){
        double posSize = size/n;
        
        double lat = point.getLatitude();
        double lon = point.getLongitude();
        
        lat = size/2 + lat;
        lon = size/2 + lon;
        
        lat = Math.round(lat / posSize);
        lon = Math.round(lon / posSize);
        
        if (lat >= 100){lat = 99;}
        if (lon >= 100){lon = 99;}
        if (lat < 0){lat = 0;}
        if (lon < 0){lon = 0;}
        
        int [] result = {(int) lat, (int) lon};
        return result;
    }
    
    public static int[] Geo2Int (GeoPoint point,double size,int n,GeoPoint center){
        double posSize = size/n;

        double lat = point.getLatitude();
        double lon = point.getLongitude();

        lat = size/2 + lat - center.getLatitude();
        lon = size/2 + lon - center.getLongitude();
        
        lat = Math.round(lat / posSize);
        lon = Math.round(lon / posSize);

        if (lat >= 100){lat = 99;}
        if (lon >= 100){lon = 99;}
        if (lat < 0){lat = 0;}
        if (lon < 0){lon = 0;}
        
        int [] result = {(int) lat, (int) lon};
        return result;
    }
    
    public static GeoPoint Int2Geo (int[] loc, double size, int n){

        double posSize = size/n;
        double lat = loc[0] * posSize;
        double lon = loc[1] * posSize;
        
        lat = lat - size/2;
        lon = lon - size/2;
        
        GeoPoint point = new GeoPoint(lat,lon);
        return point;
    }
    
    public static GeoPoint Int2Geo (int[] loc, double size, int n, GeoPoint center){

        double posSize = size/n;
        double lat = loc[0] * posSize;
        double lon = loc[1] * posSize;

        lat = lat - size/2 + center.getLatitude();
        lon = lon - size/2 + center.getLongitude();
        
        GeoPoint point = new GeoPoint(lat,lon);

        return point;
    }
}
