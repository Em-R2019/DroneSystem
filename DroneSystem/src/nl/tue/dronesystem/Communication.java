/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;
import java.util.List;
import java.util.ArrayList;
import nl.tue.dronesystem.util.Utilities;

/**
 *
 * @author 20172805
 */
public class Communication {
    public Communication(double step,double mavlatency){
        this.timeStep = step;
        this.MAVlatency = mavlatency;
    }
    double timeStep; //[s]
    double MAVlatency; // [s/km]
    double time; //[s]
    
    List<MavlinkPacket> packets = new ArrayList<>(0);
    ArrayList<Integer> rec = new ArrayList<>(0);
    ArrayList<Double> wait = new ArrayList<>(0);
    
    Fleet F = DroneSystem.F;
    
    public void Send(int reciever, int sender, MavlinkPacket packet){
        double latency = MAVlatency + time;
        
        latency = Math.round(latency*1000);
        latency = latency/1000;
        
        packets.add(packet);
        wait.add(latency);
        rec.add(reciever);
    }
    
    public List<MavlinkPacket> Recieve(int reciever){
        List<Integer> indices = new ArrayList<>(0);
        
        if (rec != null){
            indices = Utilities.allIndices(rec,reciever);
        }
        
        if (indices != null){
            List<MavlinkPacket> result = new ArrayList<>();
            List<Integer> remove = new ArrayList();
            
            for (int i = 0; i < indices.size(); i++){
                int indice = indices.get(i);
                if (wait.get(indice) <= time){
                    result.add(packets.get(indice));
                    remove.add(indice);
                }
            }
            
            //System.out.println("wait: " + wait);
            //System.out.println("rec: " + rec);
            //System.out.println("ind:" + indices.size());
            if (DroneSystem.print){
                System.out.println("reciever: "+ reciever);
            }
            //System.out.println("remove: " + remove);
            
            for (int i = 0; i < remove.size(); i++){
                wait.remove((int)remove.get(i)-i);
                rec.remove((int)remove.get(i)-i);
                packets.remove((int)remove.get(i)-i);
            }
            
            return result;
        }
        else {
            return null;
        }
    }
    
    void step(double time){
        this.time = time;
    }
}
