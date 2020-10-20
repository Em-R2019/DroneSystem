/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.tue.dronesystem.util.Utilities;
import roaf.gps.Position;
import roaf.gps.Route;
import roaf.util.GeoPoint;

/**
 *
 * @author 20172805
 */
public class TMS {
    
    public TMS(GeoPoint loc, int idnumber, int nDrones, Environment E){
        this.id = idnumber;
        this.location = loc;
        this.E = E;    
        this.droneList = new droneInfo[nDrones];
    }
    
    int id;
    GeoPoint location;
    Environment E;
    double p0,T0;
    droneInfo[] droneList;
    List<Integer> newDrones = new ArrayList<>(0);
    Grid grid;
    double maxSpeed = 0.022;
    private final double range = 10;
    private int seq;
    Map map = DroneSystem.M;
    double time;
    boolean newNoFlyZone;
    
    public void step(double t,double ti, Environment e){
        
        E = e;
        time = ti;
        
        if (time ==0){
            createGrid(E);
        }
        
        updateWeather(E);
        TMSCom.Recieve(id);
        
        Set<Integer> set = new HashSet<>(newDrones); //removing duplicates
        newDrones.clear();
        newDrones.addAll(set);
        
        if(newNoFlyZone){
            for (droneInfo drone : droneList){
                if (drone != null){
                    if(newDrones.add(drone.droneID)){
                        System.out.println("newRoute for drone " + drone.droneID);
                        makeRoute(drone);
                    }
                }
            }
        }
        
        for (int droneID : newDrones){
            TMSCom.weatherUpdate(droneID, id, seq, p0, maxSpeed, T0, time);
            seq++;
            makeRoute(droneList[droneID]);
        }
        
        newDrones.clear();
        newNoFlyZone = false;
    }
    
    private GeoPoint calcEdgeDest(int droneID, GeoPoint dest){
        
        GeoPoint pos = Utilities.copyGeopoint(droneList[droneID].position);
        double direction = pos.direction(dest);
        double distance = 0;
        
        while (distance<range+0.0005){
            pos.move(direction, 0.005);
            distance = location.distance(pos);
        }
        
        return pos;
    }
    
    private void makeRoute(droneInfo drone){
        
        if (drone != null){
            GeoPoint dest = drone.destination;
            
            if (E.getElevation(dest) == 0.1){
                System.out.println("destination in no-fly zone");
                TMSCom.NoDestination(drone.droneID, id, seq);
                seq++;
            }
            else{
                double ts = range*2/100 / maxSpeed;
                
                if (location.distance(dest)<range){
                    GeoPoint[] destination = {dest};
                    calcRoute(drone.droneID, drone.position, destination, ts,true);
                }
                else{
                    dest = calcEdgeDest(drone.droneID,dest);
                    GeoPoint[] destination = {dest};
                    calcRoute(drone.droneID, drone.position, destination, ts,true);
                }
            }
        }
    }
    
    public void checkNoFlyzones(Environment E){
        
        int n = grid.nodes.length;
        int z = grid.nodes[0][0].length-1;

        for (int i = 0; i<n; i++){
            for (int j = 0; j<n; j++){
                int[] loc = {i,j};
                GeoPoint point = Utilities.Int2Geo(loc,range*2/111,n,location);
                double elevation = E.getElevation(point);
                
                if (elevation == 0.1){
                    if(grid.nodes[i][j][z].getValue()!=0){
                        newNoFlyZone = true;
                    }
                    
                    grid.updateGrid(i, j, new Value(5));
                }
            }
        }
    }
    
    private void calcRoute(int droneID, GeoPoint start, GeoPoint[] destination, double ts, boolean end){
        
        int[] posint = Utilities.Geo2Int(start, range*2/111, 100, location);
        
        for (GeoPoint dest:destination){
            int[] destint = Utilities.Geo2Int(dest, range*2/111, 100, location);
            grid.updateGrid(destint[0], destint[1], new Value(3));
        }
        
        grid.updateGrid(posint[0], posint[1], 4, new Value(2));
        List<Node> path = grid.shortestPath(time,ts);
        
        if (path.isEmpty()){
            path = grid.shortestPathOut(time,ts);
            end = false;
        }
        
        Route route = new Route();

        for (int i = 0; i<path.size();i++){
            Node node = path.get(i);
            double[] timeslot = {time,time+ts*2};
            int x = node.getX();int y = node.getY(); int z = node.getZ();
            
            if (grid.nodes[x][y][z].getValue() != 4){
                grid.updateGrid(x, y, z, new Value(4,timeslot));
            }
            else {
                grid.nodes[x][y][z].addTime(timeslot);
            }
            
            int[] loc = {x,y};
            GeoPoint nextpoint = Utilities.Int2Geo(loc, range*2/111,100, location);
            nextpoint.setElevation(node.getZ()*0.01 + 0.01);
           
            if (location.distance(nextpoint)<(range)){
                route.appendPosition(nextpoint);
            } 
            else if (location.distance(nextpoint)<=(range+0.005)){
                double dir = nextpoint.direction(location);
                double dist = location.distance(nextpoint) - range;
                nextpoint.move(dir,dist);
                route.appendPosition(nextpoint);
            }
            else if (i == path.size()-1 || i < 5) {
                route.appendPosition(nextpoint);
            }
            else if (location.distance(nextpoint)>(range+0.005)){
                route.appendPosition(nextpoint);
                for (int j = i; j<path.size();j++){
                    path.remove(j);
                }
                end = false;
                break;
            }
        }
        
        if (end){
            Position finaldestination = new GeoPoint(destination[0].getLatitude(),destination[0].getLongitude());
            finaldestination.setElevation(route.getPosition(route.size()-1).getElevation());
            route.appendPosition(finaldestination);
        }
        
        TMSCom.newRoute(droneID, id, seq, route);
        seq++;
        
        if (droneList[droneID].path != null){
            for (int j = 0; j<droneList[droneID].path.size(); j++){
                removeNode(droneID,droneList[droneID].path,0);
            }
        }
        
        droneList[droneID].route = route;
        droneList[droneID].path = path;
        removeNode(droneID,path,0);
        
        for (GeoPoint dest:destination){
            int[] destint = Utilities.Geo2Int(dest, range*2/111, 100, location);
            grid.updateGrid(destint[0], destint[1], new Value(1));
        }
    }
    
    public List<Route> getAllRoutes(){
        
        List<Route> routesList = new ArrayList<>(0);
        
        for (droneInfo d : droneList){
            if (d != null){
                if (d.route.size() > 0){
                    routesList.add(d.route);
                }
            }
        }

        return routesList;
    }
    
    public Route getRoute(int droneID){
        return droneList[droneID].route;
    }
    
    private void updateWeather(Environment E){
        
        double[] air = E.getAirinfo();
        
        if (air[0] != p0 || air[1] != T0){
            p0 = air[0];
            T0 = air[1];
            
            for (droneInfo d : droneList){
                if (d != null){
                    TMSCom.weatherUpdate(d.droneID,id,seq,p0,0.018,T0,time);
                    System.out.println("weather change update for drone " + d.droneID);
                }
            }
        }
    }
    
    public GeoPoint getLocation(){
        return location;
    }
    
    public void newDroneInfo(int droneID, int priority, int category, GeoPoint destination, double lastTime){
        droneList[droneID] = new droneInfo(droneID, priority, category, destination, lastTime);
    }
    
    public void updateDroneInfo(int droneID, int priority, int category, GeoPoint destination, double lastTime){
        droneList[droneID].priority = priority;
        droneList[droneID].category = category;
        droneList[droneID].destination = destination;
        droneList[droneID].lastTime = lastTime;
    }
    
    public void updateDroneStatus(int droneID, int status, double speed, GeoPoint position, double lastTime){
        droneList[droneID].status = status;
        droneList[droneID].speed = speed;
        droneList[droneID].position = position;
        droneList[droneID].lastTime = lastTime;
        Route route = droneList[droneID].route;
        
        if(route.size() > 0){
            for (int i = 0; i<route.size();i++){
                Position pos = route.getPosition(i);
                
                if (pos.distance(position)<0.02){
                    for (int j = 0; j<i; j++){
                        droneList[droneID].route.remove(0);
                        
                        if (droneList[droneID].path.size()>0){
                            removeNode(droneID, droneList[droneID].path,0);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    private void removeNode(int droneID, List<Node> path, int n){
        
        Node node = path.get(n);
        int[] loc = {node.getX(),node.getY()};
        GeoPoint point = Utilities.Int2Geo(loc,range*2/111,100,location);
        double elevation = E.getElevation(point);
        
        if (node.getZ()*0.01 + 0.01 <= elevation){
            if(elevation == 0.01){
                grid.updateGrid(node.getX(), node.getY(), node.getZ(),new Value(5));
            }
            else{ 
                grid.updateGrid(node.getX(), node.getY(), node.getZ(),new Value(0));
            }
        }
        else{ 
            grid.updateGrid(node.getX(), node.getY(), node.getZ(),new Value(1));
        }
        
        droneList[droneID].path.remove(n);
    }
    
    public void removeDroneInfo(int droneID){
        
        if (droneList[droneID] != null){
            if(droneList[droneID].path != null){
                int i = 0;
                
                while (i<droneList[droneID].path.size()){
                    removeNode(droneID, droneList[droneID].path,0);
                    i++;
                }
            }
        }

        droneList[droneID].position = null;
        droneList[droneID].route = null;
        droneList[droneID] = null;
    }
    
    private void createGrid(Environment E){
        
        int n = 100;
        int z = 8;
        Value[][][] nodes = new Value[n][n][z];
        double deltaZ = 0.01;
        
        for (int i = 0; i<n; i++){
            for (int j = 0; j<n; j++){
                int[] loc = {i,j};
                GeoPoint point = Utilities.Int2Geo(loc,range*2/111,n,location);
                double elevation = E.getElevation(point);
                
                for (int h = 0; h<z; h++){
                    if (h*deltaZ +deltaZ <= elevation){
                        nodes[i][j][h] = new Value(0);
                    }
                    else {
                        nodes[i][j][h] = new Value(1);
                    }
                }
            }
        }
        
        grid = new Grid(nodes);
    }
}

class droneInfo{
    public droneInfo(int droneID, int priority, int category, GeoPoint destination, double lastTime){
        this.droneID = droneID;
        this.priority = priority;
        this.category = category;
        this.destination = destination;
        this.lastTime = lastTime;
        route = new Route();
    }
    int droneID,priority,category,status;
    double lastTime,speed;
    GeoPoint destination,position;
    Route route;
    List<Node> path;
}