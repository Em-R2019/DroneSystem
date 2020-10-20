/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;

import nl.tue.dronesystem.util.Utilities;
import roaf.util.GeoPoint;

/**
 *
 * @author 20172805
 */
public class Environment {
    public Environment(double p, double T, double size){
        this.p0 = p;
        this.T0 = T;
        this.size = size;
    }
    private double p0;
    private double T0;
    private final double size;
    double [] obstacleHeight;
    GeoPoint [] obstaclePos;
    int n;
    double [][] map;
    
    public void setPressure0(double p){
        this.p0 = p;
    }
    
    public void setTemperature0(double T){
        this.T0 = T;
    }
    
    public double getPressure(double z){
        double c = 5.25607;
        double p = p0 * Math.exp(-z/(c*T0));
        return p;
    }
    
    public double [] getAirinfo(){
        double [] aInfo = {p0,T0};
        return aInfo;
    }
    
    public double getElevation(GeoPoint point){
        int [] loc = Utilities.Geo2Int(point,size,n);
        double e = map[loc[0]][loc[1]];
        return e;
    }
    
    public double getElevation(int[] loc){
        double e = map[loc[0]][loc[1]];
        return e;
    }
        
    public void createElevation(Fleet F){
        n = 100;
        map = new double[n][n];
        
        GeoPoint [] TMS = F.getTMSposArray();
        
        for (GeoPoint tms : TMS){
            for (int i = 0; i < 50; i++){
                GeoPoint point = Utilities.copyGeopoint(tms);
                point.move(Utilities.randomDouble(-Math.PI,Math.PI), Utilities.randomDouble(-10,10));
                int [] loc = Utilities.Geo2Int(point,size,n);
                
                if (loc[0]>=0&&loc[0]<100&&loc[1]>=0&&loc[1]<100){
                    map[loc[0]][loc[1]] = Utilities.randomDouble(0,0.099);
                }
            }
        }
    } 
    
    public double[][] getMap(){
        return map;
    }

    public void setNoFlyZones(Fleet F){
        for (TMS tms : F.TMSList){
            GeoPoint point = Utilities.copyGeopoint(tms.location);
            point.move(Utilities.randomDouble(-Math.PI,Math.PI), Utilities.randomDouble(-6,6));
            
            int [] loc = Utilities.Geo2Int(point,size,n);
            double side = Utilities.randomDouble(0,0.125);
            int intSide = (int) Math.round(side / (size/n));
            
            loc[0] = loc[0] - intSide/2;
            loc[1] = loc[1] - intSide/2;
            
            for (int i = 0;i<intSide;i++){
                for (int j = 0 ;j<intSide;j++){
                    if (loc[0]+i<n && loc[1]+j<n &&loc[1]+j>=0 && loc[0]+i>=0){
                        map[loc[0]+i][loc[1]+j] = 0.100;
                    }
                }
            }
        }
        
        for (TMS tms : F.TMSList){
            tms.checkNoFlyzones(this);
        }
    }
}
