/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.dronesystem;


/**
 *
 * @author 20172805
 */
public class DroneSystem {

    public static final double timeStep = 2; //[s]
    public static final double MAVlatency = 0.2; //[s]
    public static double p0 = 1023.25; //[hPa] at sea level
    public static double T0 = 288; //[K]
    public static final double totalTime = 500; //[s]
    public static final double size = 0.25; // 0.01 degrees = 1.11 km
    public static final int nDrones = 5;
    public static boolean print = false; // to print messages recieved and drone status
    
    static Communication Com = new Communication(timeStep,MAVlatency);
    static Environment E = new Environment(p0,T0,size);
    static Fleet F = new Fleet(size);
    static Map M = new Map(size); 
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        F.createTMS(2,E,nDrones);
        E.createElevation(F);
        F.createFleet(nDrones); 
        M.create(E.map,F.getTMSposArray());

        int n = 0;
        double nofly = 100; // time at which nofly zones appear
        double weather = 300; // time at which temperature and pressure change
        int printstep = 0;
        
        for (double t = 0; t < totalTime; t = t+timeStep){
            t = Math.round(t*1000);
            t = t/1000;
            
            Com.step(t);
            F.step(timeStep,t,M,E);
            M.update(F.getDroneposArray(),F);
            
            if (t > nofly){
                E.setNoFlyZones(F);
                M.create(E.getMap(), F.getTMSposArray());
                nofly = totalTime;
                System.out.println("NOFLYZONE");
            }
            
            if (t > weather){
                E.setPressure0(1000);
                E.setTemperature0(250);
                weather = totalTime;
                System.out.println("WEATHER CHANGE");
            }
            
            if (print){
                System.out.println("step" + n);
                System.out.println("time:" + t + "s");
            }
            else if (n>printstep){   // every 10 steps time is printed
                System.out.println("step" + n);
                System.out.println("time:" + t + "s");
                printstep = n + 10;
            }
            
            n++;
        }        
    }
}
