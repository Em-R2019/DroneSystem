/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.tue.dronesystem;

import java.util.List;
import roaf.gps.*;
import roaf.util.*;
import nl.tue.dronesystem.util.Utilities;

/**
 *
 * @author 20172805
 */

public class Drone {
    public Drone(int IdNumber, GeoPoint pos){
        this.id = IdNumber;
        this.truePosition = pos;
        this.range = 10;
    }
    public final int id;
    
    private GeoPoint currentPosition,truePosition,lastPosition,destination,oldDest;
    private int priority,category;
    private double deltaZ,range,dist,actTime,time,z,pressure,direction,goalZSpeed,goalSpeed,Zspeed;
    boolean firstpoint;
    int goodbyes;
    
    private final double vertdeceleration = 0.00985;    //[km/s^2]
    private final double vertmaxspeed = 0.011;          //[km/s]
    private double timer = 10;                          //[s]
    private final double c = 5.25607;                   // constant for calulating altitude
    private final double acceleration = 0.0065;         //[km/s^2] 
    private double maxSpeed = 0.022;                    //[km/s] == 80 km/h
    private int closestTMS = -1;                     
    private int status = 5;                     //1=flying 2=hovering 3=landing 4=ascending/descending 5=on ground 6=crashed
    private double T0 = 288;                            //[K]
    private double p0 = 1023.25;                        //[hPa]
    private int seq = 0;
    private Route route = new Route();
    private double speed = 0;                           //[km/s]
    private final double detectionRange = 0.01;         // [km]
    
    Environment E = DroneSystem.E;
    Fleet F = DroneSystem.F;
    
    public void step(double t,double ti){
        time = ti;
        checkObstacles();
        
        if (status != 6){
            calcPosition();
            DroneCom.Recieve(id);
            checkTMS();
            
            switch(status){
                case 1:
                    calcSpeedDirection(t);
                    
                    if (route.getTotalDistance() < maxSpeed*10 && closestTMS != -1 && status == 1){
                        if (closestTMS != -1){
                            DroneCom.StatusUpdate(closestTMS, id, seq, currentPosition, goalSpeed, status, time);
                            seq++;
                            timer = time + 10;
                        }
                    }
                    
                    break;
                case 2:
                    calcSpeedDirection(t);
                    break;
                case 3:
                    landing(t);
                    break;
                case 4:
                    calcSpeedDirection(t);
                    break;
                case 5:
                    if (time >= actTime){
                        if (oldDest == destination){
                            F.assignment(id);
                        }
                        else {
                            if (closestTMS != -1){
                                DroneCom.NewMission(closestTMS, id, seq, priority, category, destination, time);
                                seq++;
                            }
                            calcSpeedDirection(t);
                            actTime =  Double.POSITIVE_INFINITY;
                        }
                    }                
                    
                    break;
            }
            
            if (time >= timer){
                if (closestTMS != -1){
                    DroneCom.StatusUpdate(closestTMS, id, seq, currentPosition, goalSpeed, status, time);
                    seq++;
                    timer = time + 10.0;
                }
            }
        }
        
        if (DroneSystem.print){
            System.out.println("drone id: " + id);
            System.out.println("closest TMS: " + closestTMS);
            System.out.println("drone status: " + status);
            System.out.println("drone speed: " + speed);
            System.out.println("drone Zspeed: " + Zspeed);
            System.out.println("drone measured altitude: " + currentPosition.getElevation());
            System.out.println("drone true altitude: " + truePosition.getElevation());
        }

//        System.out.println("drone location: " + currentPosition);
//        System.out.println("drone destination: " + destination);

    }
    
    private void checkObstacles(){
        List<Double> drones = F.dronesInRange(id, detectionRange);
        
        if (drones.get(1)>0){
            lastPosition = currentPosition;
            if (closestTMS != -1){
                DroneCom.UnexpectedObject(closestTMS, id, seq, lastPosition, time);
                seq++;
            }
        }
        
        double elevation = E.getElevation(truePosition);
        
        if ( elevation > 0.099){
            System.out.println("drone " + id + " in no-fly zone");
        }
        else if (truePosition.getElevation() <= elevation && !(status == 3 || status == 5)){
            if (status != 6){
                if (closestTMS != -1){
                    DroneCom.UnexpectedObject(closestTMS, id, seq, currentPosition, time);
                    seq++;
                }
                
                System.out.println("drone " + id + " crashed");
                setStatus(6);Zspeed = 0;
                speed = 0;
            }
        }
    }
    
    public void newMission(){
        F.assignment(id);
        
        if (closestTMS != -1){
            DroneCom.NewMission(closestTMS, id, seq, priority, category, destination, time);
            seq++;
        }
        
        setRoute(new Route());
    }
    
    private void checkTMS(){
        int newTMS = F.closestTMSInRange(id, range);
        
        if (newTMS != closestTMS){
            if (newTMS != -1){
                DroneCom.Hello(newTMS,id,seq,priority,category,destination, time);
                seq++;
                DroneCom.StatusUpdate(newTMS, id, seq, currentPosition, goalSpeed, status, time);
                seq++;
                timer = time + 10;
                goodbyes++;
                
            }
            
            if (closestTMS != -1){
                DroneCom.Goodbye(closestTMS,id,seq);
                seq++;
            }
            
            closestTMS = newTMS;
            
            if (goodbyes > 3){
                    newMission();
                    goodbyes = 0;
            }
            
            if (newTMS == -1){
                maxSpeed = 0.022;
            }
        }
    }
    
    private void calcSpeed(double t){
        
        if (goalSpeed > maxSpeed){
            goalSpeed = maxSpeed;
        }
        
        if (speed > goalSpeed){
            speed = speed - acceleration * t;
            
            if (speed < goalSpeed){
                speed = goalSpeed;
            }
        }
        
        else if (speed < goalSpeed){
            speed = speed + acceleration * t;
            
            if (speed > goalSpeed){
                speed = goalSpeed;
            }
        }
    }
    
    private void calcZSpeed(double t){
        
        if (goalZSpeed > vertmaxspeed){
            goalZSpeed = vertmaxspeed;
        }
        
        if (goalZSpeed < -vertmaxspeed){
            goalZSpeed = -vertmaxspeed;
        }
        
        if (Zspeed > goalZSpeed){
            Zspeed = Zspeed - vertdeceleration * t;
            
            if (Zspeed < goalZSpeed){
                Zspeed = goalZSpeed;
            }
        }
        else if (Zspeed < goalZSpeed){
            Zspeed = Zspeed + acceleration * t;
            
            if (Zspeed > goalZSpeed){
                Zspeed = goalZSpeed;
            }
        }
    }
    
    private void calcSpeedDirection(double t){
        if (route != null && route.size() > 0){
            double distance = Double.POSITIVE_INFINITY;
            Position nextPoint = route.getPosition(0);
            dist = nextPoint.distance(currentPosition);
            
            if (firstpoint){
                firstpoint = false;
                
                for (int i = 0; i < route.size();i++){
                    nextPoint = route.getPosition(i);
                    dist = nextPoint.distance(currentPosition);
                    
                    if (dist<=distance){
                        distance = currentPosition.distance(nextPoint);
                    }
                    else if (dist>distance){
                        nextPoint = route.getPosition(i-1);
                        dist = nextPoint.distance(currentPosition);
                        
                        for (int j = 0; j<i; j++){
                            route.remove(0);
                        }
                        
                        break;
                    }
                }
            }
            
            deltaZ = nextPoint.getElevation() - currentPosition.getElevation();
            
            if (dist < 0.0003 && Math.abs(deltaZ) < 0.0003){
                route.remove(0);
                
                if (route.size() > 0){
                    fly2Point(t,route.getPosition(0));
                }
                else if (destination.distance(currentPosition) < 0.0003){
                    firstpoint = true;
                    landing(t);
                }
                else {
                    firstpoint = true;
                    hover(t,0.01);
                }
            }
            else{
                fly2Point(t,nextPoint);
            }
        }
        else if (destination.distance(currentPosition) < 0.0003){
            landing(t);
        }
        else if (closestTMS == -1) {
            fly2Destination(t,0.05);
        }
        else {
            hover(t,0.01);
        }
    }
    
    private void ascend(double t, double z){
        goalSpeed = 0;
        calcSpeed(t);
        deltaZ = z - currentPosition.getElevation();
        goalZSpeed = deltaZ / t;
        calcZSpeed(t);
        
        if (status != 4){
           setStatus(4);
        }
    }
    
    private void hover(double t,double elevation){
        goalSpeed = 0;
        calcSpeed(t);
        deltaZ = elevation - currentPosition.getElevation();
        goalZSpeed = deltaZ / t; 
        calcZSpeed(t);
        
        if (status != 2){
            setStatus(2);
            if (closestTMS != -1){
                DroneCom.RouteRequest(closestTMS,id,seq);
                seq++;
            }
        }
    }
    
    private void fly2Destination(double t, double altitude){
        deltaZ = altitude - currentPosition.getElevation();
        
        if (Math.abs(deltaZ) > 0.001){
            ascend(t,altitude);
        }
        else {
            direction = currentPosition.direction(destination);
            dist = currentPosition.distance(destination);
            goalSpeed = dist/t;
            calcSpeed(t);
            deltaZ = altitude - currentPosition.getElevation();
            goalZSpeed = deltaZ/t;
            calcZSpeed(t);
            if (status != 1){
                setStatus(1);
            }
        }
    }
    
    private void landing(double t){
        goalSpeed = 0;
        calcSpeed(t);
        
        if (speed == 0){ 
            goalZSpeed = destination.getElevation() - currentPosition.getElevation() / t; 
            calcZSpeed(t);
        }
        else {
            goalZSpeed = 0;
            calcZSpeed(t);
        }
        
        if (status != 3){
            setStatus(3);
        }
        
        if (currentPosition.getElevation() < 0.0005 && Zspeed == 0){
            if (status != 5){
                oldDest = destination;
                setStatus(5);
                actTime = time + 10;
            }
        }
    }
    
    private void fly2Point(double t,Position nextPoint){
        deltaZ = nextPoint.getElevation() - currentPosition.getElevation();
        dist = nextPoint.distance(currentPosition);
        
        goalSpeed = dist / t;
        calcSpeed(t);
        goalZSpeed = deltaZ / t; 
        calcZSpeed(t);
        
        direction = currentPosition.direction(nextPoint);
        
        if (status != 1){
            setStatus(1);
        }
    }
    
    public void setWeather(double p, double T,double speed){
        T0 = T;
        p0 = p;
        maxSpeed = speed;
    }
    
    private double calcAltitude(){
        z = truePosition.getElevation();
        pressure = DroneSystem.E.getPressure(z);
        double altitude = c * T0 * Math.log(p0/pressure);
        
        if (altitude < 0){
            altitude = 0;
        }
        
        return altitude;
    }
    
    private void calcPosition(){
        currentPosition = Utilities.copyGeopoint(truePosition);
        currentPosition.move(Utilities.randomDouble(-Math.PI,Math.PI), Utilities.randomDouble(-0.0003,0.0003));
        currentPosition.setElevation(calcAltitude());
    }
    
    public void move(double t){
        truePosition.move(direction, speed*t);
        truePosition.setElevation(truePosition.getElevation()+(Zspeed*t));
        
        if (truePosition.getElevation() < 0){
            truePosition.setElevation(0);
        }
    }
    
    public GeoPoint getPosition() { // gets the current position 
        return currentPosition;
    }
    
    public GeoPoint gettruePosition() { // gets the current position 
        return truePosition;
    }

    public void setDestination(GeoPoint dest) { // set the destination of the drone 
        this.destination = dest;
    }
    
    public void setRoute(Route r){
        route = r;
        firstpoint = true;
    }
    
    public void addToRoute(GeoPoint location) { // add a location to the route of the drone 
        route.appendPosition(location);
    }

    public void setStatus(int st) { // sets the status of the drone 
        status = st;
        
        if (closestTMS != -1){
            DroneCom.StatusUpdate(closestTMS, id, seq, currentPosition, goalSpeed, status, time);
            seq++;
            timer = time + 10;
        }
    }
}
