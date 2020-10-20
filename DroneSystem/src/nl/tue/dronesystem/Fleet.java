/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;
import java.util.ArrayList;
import java.util.List;
import nl.tue.dronesystem.util.Utilities;
import static nl.tue.dronesystem.util.Utilities.randomDouble;
import roaf.gps.Route;
import roaf.util.*;

/**
 *
 * @author 20172805
 */
public class Fleet {
    
    public Fleet(double s){
        this.size = s/2 - size/100;
    }

    Drone [] droneFleet;
    TMS [] TMSList;
    GeoPoint[] tmsPosList;
    GeoPoint[] dronePosList;
    double size; //degrees
    
    public void createTMS(int nTMS,Environment E,int nDrones){
        TMSList = new TMS[nTMS];
        tmsPosList = new GeoPoint[nTMS];
        
        for (int i = 0; i < nTMS; i++){
            GeoPoint pos = new GeoPoint(Utilities.randomDouble(-size,size),randomDouble(-size,size));
            TMSList[i] = new TMS(pos,i+1000,nDrones,E);
            tmsPosList[i] = pos;
        }
    }
    
    public void createFleet(int nDrones){
        droneFleet = new Drone[nDrones];
        dronePosList  = new GeoPoint[nDrones];
        
        for (int i = 0; i < nDrones; i++){
            GeoPoint pos = new GeoPoint(Utilities.randomDouble(-size,size),randomDouble(-size,size));
            pos.setElevation(0);
            droneFleet[i] = new Drone(i,pos);
            dronePosList[i] = pos;
            
            assignment(i);
        }
    }
    
    public Drone getDrone(int id){
        return droneFleet[id];
    }
    
    public TMS getTMS(int id){
        id = id-1000;
        return TMSList[id];
    }
    
    public GeoPoint getLocation(int id){
        if (id<1000){
            GeoPoint dPos = dronePosList[id];
            return dPos;
        }
        else {
            id = id-1000;
            GeoPoint tmsPos = tmsPosList[id];
            return tmsPos;
        }
    }
    
    public int closestTMSInRange(int id, double range){
        GeoPoint dPos = getLocation(id);
        int tmsId = -1;
        int i = 0;
        double closest = range;
        
        for (GeoPoint tmsPos : tmsPosList){
            double dist = tmsPos.distance(dPos);
            if (dist < closest){
                tmsId = i+1000;
                closest = dist;
            }
            i++;
        }
            
        return tmsId;
    }
    
    public List<Double> dronesInRange(int id, double range){
        GeoPoint dPos = getLocation(id);
        List<Double> droneDistDir = new ArrayList<>();
        double dist;
        double dir;
        
        for (GeoPoint dPos2 : dronePosList){
            dist = dPos2.distance(dPos);
            dir = dPos.direction(dPos2);
            
            if (dist < range){
                droneDistDir.add(dist);
                droneDistDir.add(dir);
            }
        }
        
        return droneDistDir;
    }
    
    public GeoPoint[] getTMSposArray(){
        return tmsPosList;
    }
    
    private void updateDroneposlist(){
        int i = 0;
        
        for (Drone d : droneFleet){
            dronePosList[i] = d.gettruePosition();
            i++;
        }
    }
    
    public GeoPoint[] getDroneposArray(){     
        return dronePosList;
    }
    
    public void assignment(int id){
        Drone d = droneFleet[id];
        GeoPoint dest = new GeoPoint(Utilities.randomDouble(-size,size),randomDouble(-size,size));
        
        d.setDestination(dest);
    }
    
    public void step(double t, double time, Map M,Environment E){ 
        for (Drone d : droneFleet){
            d.step(t,time);
        }
        
        List<Route> routesList =  new ArrayList<>(0);
        
        for (TMS tms : TMSList){
            tms.step(t,time,E);
            routesList.addAll(tms.getAllRoutes());
        }
        
        Route[] routes = routesList.toArray(new Route[routesList.size()]);
        M.setRoutes(routes);
        
        for (Drone d : droneFleet){
            d.move(t);
        }
        
        updateDroneposlist();
    }
}
