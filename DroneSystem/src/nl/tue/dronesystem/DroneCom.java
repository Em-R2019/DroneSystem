/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;

import java.util.List;
import roaf.util.*;
import nl.tue.dronesystem.util.Utilities;
import roaf.gps.Route;

/**
 *
 * @author 20172805
 */
public class DroneCom {
    
    static Communication Com = DroneSystem.Com;
    static Fleet F = DroneSystem.F;
    
    public static void StatusUpdate(int reciever, int id, int seq, GeoPoint position, double speed, int status, double time){
        double altitude = position.getElevation();
        double latitude = position.getLatitude();
        double longitude = position.getLongitude();
        
        double [] doubles = {latitude,longitude,altitude,speed,status,time};
        byte [] payload = Utilities.doublestoBytes(doubles);
        
        MavlinkPacket update = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,1,10,payload);
        Com.Send(reciever, id, update);
    }
    
    public static void Hello(int reciever, int id, int seq, int priority, int category, GeoPoint destination, double time){
        double destLat = destination.getLatitude();
        double destLong = destination.getLongitude();

        double [] doubles = {priority,category,destLat,destLong,time};

        byte [] payload = Utilities.doublestoBytes(doubles);

        MavlinkPacket hello = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,2,10,payload);
        Com.Send(reciever, id, hello);
    }
    
    public static void NewMission(int reciever, int id, int seq, int priority, int category, GeoPoint destination, double time){
        double destLat = destination.getLatitude();
        double destLong = destination.getLongitude();

        double [] doubles = {priority,category,destLat,destLong,time};

        byte [] payload = Utilities.doublestoBytes(doubles);
        
        MavlinkPacket newMission = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,3,10,payload);
        Com.Send(reciever, id, newMission);
    }
    
    public static void UnexpectedObject(int reciever, int id, int seq, GeoPoint position, double time){
        double [] doubles = {position.getElevation(),position.getLatitude(),position.getLongitude(), time};
        
        byte [] payload = Utilities.doublestoBytes(doubles);

        MavlinkPacket UO = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,4,10,payload);
        Com.Send(reciever, id, UO);
    }
    
    public static void Goodbye(int reciever, int id, int seq){
        byte [] payload = new byte [0];
        
        MavlinkPacket goodbye = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,5,10,payload);
        Com.Send(reciever, id, goodbye);
    }
    
    public static void RouteRequest(int reciever, int id, int seq){
        byte [] payload = new byte [0];
        
        MavlinkPacket routeRequest = MavlinkPacket.createUnsignedMavlink2Packet(seq,id,1,9,10,payload);
        Com.Send(reciever, id, routeRequest);
    }
    
    public static void Recieve(int id){
        Drone d = F.getDrone(id);
        List<MavlinkPacket> list = Com.Recieve(id);
        
        if (list != null){
            for (MavlinkPacket packet : list){
                int mssgId = packet.getMessageId();
                int sender = packet.getSystemId();
                byte [] payload = packet.getPayload();

                switch (mssgId) {
                    case 6: 
                        double [] sPay = Utilities.bytestoDoubles(payload);
                        double p0 = sPay[0];
                        double T0 = sPay[1];
                        double maxSpeed = sPay[2];
                        
                        d.setWeather(p0, T0,maxSpeed);
                        
                        if (DroneSystem.print){
                            System.out.println("weatherUpdate" + id);
                        }
                        break;
                    case 7: 
                        double [] dPay = Utilities.bytestoDoubles(payload);
                        GeoPoint point = new GeoPoint(0d,0d);
                        
                        for (int i = 0; i < dPay.length; i = i+3){
                             point.setElevation(dPay[i+2]);
                             point.setLatitude(dPay[i]);
                             point.setLongitude(dPay[i+1]);
                             d.addToRoute(point);
                        }
                        
                        if (DroneSystem.print){
                            System.out.println("addRoute" + id);
                        }
                        break;
                    case 8:
                        double [] cPay = Utilities.bytestoDoubles(payload);
                        GeoPoint cpoint = new GeoPoint(0d,0d);
                        Route route = new Route();
                        
                        for (int i = 0; i < cPay.length; i = i+3){
                             cpoint.setElevation(cPay[i+2]);
                             cpoint.setLatitude(cPay[i]);
                             cpoint.setLongitude(cPay[i+1]);
                             route.appendPosition(cpoint);
                        }
                        
                        d.setRoute(route);
                        
                        if (DroneSystem.print){
                            System.out.println("newRoute" + id);
                        }
                        break;
                    case 10: 
                        d.newMission();
                        break;
                }
            }
        }
    }
}
