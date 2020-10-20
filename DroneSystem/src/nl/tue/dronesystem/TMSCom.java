/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;

import java.util.List;
import roaf.util.*;
import nl.tue.dronesystem.util.Utilities;
import roaf.gps.Position;
import roaf.gps.Route;

/**
 *
 * @author 20172805
 */
public class TMSCom {
    
    static Communication Com = DroneSystem.Com;
    static Fleet F = DroneSystem.F;
    
    public static void weatherUpdate(int reciever, int id, int seq, double p0, double speed, double T0, double time){
        double [] doubles = {p0,T0,speed};        
        
        byte [] payload = Utilities.doublestoBytes(doubles);
        
        MavlinkPacket update = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,6,10,payload);
        Com.Send(reciever, id, update);
    }
    
    public static void addRoute(int reciever, int id, int seq, Route route){
        double [] doubles = new double[route.size()*3];
        int j = 0;
        
        for(int i = 0;i<route.size();i++){
            Position point = route.getPosition(i);
            doubles[j] = point.getLatitude();
            doubles[j+1] = point.getLongitude();
            doubles[j+2] = point.getElevation();
            j = j+3;
        }
        
        byte [] payload = Utilities.doublestoBytes(doubles);

        MavlinkPacket addRoute = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,7,10,payload);
        Com.Send(reciever, id, addRoute);
    }
    
    public static void newRoute(int reciever, int id, int seq, Route route){
        double [] doubles = new double[route.size()*3];
        int j = 0;
        
        for(int i = 0;i<route.size();i++){
            Position point = route.getPosition(i);
            doubles[j] = point.getLatitude();
            doubles[j+1] = point.getLongitude();
            doubles[j+2] = point.getElevation();
            j = j+3;
        }
        
        byte [] payload = Utilities.doublestoBytes(doubles);
        
        MavlinkPacket newRoute = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,8,10,payload);
        Com.Send(reciever, id, newRoute);
    }
    public static void NoDestination(int reciever, int id, int seq){
        byte [] payload = new byte [0];
        
        MavlinkPacket routeRequest = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,10,10,payload);
        Com.Send(reciever, id, routeRequest);
    }
    
    public static void Recieve(int id){
        TMS tms = F.getTMS(id);
        List<MavlinkPacket> list = Com.Recieve(id);
        
        if (list != null){
            for (MavlinkPacket packet : list){
                int sender = packet.getSystemId();
                int mssgId = packet.getMessageId();
                byte [] payload = packet.getPayload();

                switch (mssgId) {
                    case 1: 
                        double [] sPay = Utilities.bytestoDoubles(payload);
                        double lats = sPay[0];
                        double lons = sPay[1];
                        double alts = sPay[2];
                        GeoPoint position = new GeoPoint(lats,lons);
                        position.setElevation(alts);
                        double speed = sPay[3];
                        int status = (int) sPay[4];
                        double time = sPay[5];
                        
                        if (DroneSystem.print){
                            System.out.println("update " + sender);
                        }
                        
                        if (tms.droneList[sender] != null){
                            tms.updateDroneStatus(sender, status, speed, position, time);
                        }
                        
                        break;
                    case 2: 
                        double [] dPay = Utilities.bytestoDoubles(payload);
                        int priority = (int) dPay[0];
                        int category = (int) dPay[1];
                        double latd = dPay[2];
                        double lond = dPay[3];
                        double dtime = dPay[4];
                        GeoPoint destination = new GeoPoint(latd,lond);
                        
                        if (DroneSystem.print){
                            System.out.println("hello " + sender);
                        }
                        
                        tms.newDroneInfo(sender, priority, category, destination, dtime);
                        
                        if(tms.newDrones.add(sender)){
                            tms.newDrones.add(sender);
                        }
                        
                        break;
                    case 3:
                        double [] uPay = Utilities.bytestoDoubles(payload);
                        int upriority = (int) uPay[0];
                        int ucategory = (int) uPay[1];
                        double latu = uPay[2];
                        double lonu = uPay[3];
                        double utime = uPay[4];
                        GeoPoint udestination = new GeoPoint(latu,lonu);
                        
                        if (DroneSystem.print){
                            System.out.println("newMission " + sender);
                        }
                        
                        if (tms.droneList[sender] != null){
                            tms.updateDroneInfo(sender, upriority, ucategory, udestination, utime);
                            
                            if(tms.newDrones.add(sender)){
                                tms.newDrones.add(sender);
                            }
                        }
                        
                        else{
                            tms.newDroneInfo(sender, upriority, ucategory, udestination, utime);
                            
                            if(tms.newDrones.add(sender)){
                                tms.newDrones.add(sender);
                            }
                        }
                        
                        break;
                    case 4:
                        System.out.println("unexpected Object " + sender);
                        break;
                    case 5:
                        if (DroneSystem.print){
                            System.out.println("goodbye " + sender);
                        }
                        
                        if (tms.droneList[sender] != null){
                            tms.removeDroneInfo(sender);
                            if(!tms.newDrones.add(sender)){
                                tms.newDrones.remove(0);
                            }
                        }
                        break;
                    case 9:
                        if (DroneSystem.print){
                            System.out.println("routeRequest " + sender);
                        }
                        
                        if(tms.newDrones.add(sender)){
                            tms.newDrones.add(sender);
                        }
                }
            }
        }
    }
}
