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
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.util.Arrays;
import javax.swing.*;
import nl.tue.dronesystem.util.Utilities;
import roaf.gps.Route;
import roaf.util.GeoPoint;
import roafx.gui.map.MapPanel;

public class Map{
    
    Map(double s) {
       this.size = s;
       frame = new JFrame("Map frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
    }
    
    static JFrame frame;
    double size;
    
    Color[] colT;
    GeoPoint [] GeoT;
    int[] intT;
    int[] intTMS2;
    GeoPoint [] tms;
    Color[] colTMS;
    Route[] routes = new Route[0];
    
    public void create(double [][] map, GeoPoint [] tms){
        this.tms = tms;

        Rectangle2D.Double rect = new Rectangle2D.Double(-size/2,-size/2, size, size);
        MapPanel panel = new MapPanel(rect);
        panel.setBackground(Color.BLACK);
        panel.setPreferredSize( new Dimension(360, 360) );
        panel.setMinimumSize  ( panel.getPreferredSize()  );
        panel.showGrid( false );
        
        
        int n = map.length;

        int[] intObs = new int[n*n];
        double k = 425/0.25/n*0.5;
        Arrays.fill(intObs, (int) k);
        
        int[] intTMS1 = new int[tms.length];
        Arrays.fill(intTMS1, 10);
        
        intTMS2 = new int[tms.length];
        double i = 170*0.5/size;
        Arrays.fill(intTMS2, (int)i);
        
        intT = Utilities.combine(intObs,intTMS1);
        
        
        Color[] colObs = new Color[n*n];
        int j = 0;
        
        colTMS = new Color[tms.length];
        Arrays.fill(colTMS, Color.YELLOW);
        
        
        GeoPoint [] elevation = new GeoPoint[n*n];
        
        for (int r= 0; r<n;r++){
            for (int w = 0; w<n; w++){
                int hc = (int)Math.round(map[r][w]*2550);
                int [] loc = {r,w};

                elevation[j] = Utilities.Int2Geo(loc, size, n);
                if (hc > 0){
                    colObs[j] = new Color(hc,hc,hc);
                }
                else{
                    colObs[j] = new Color(0,0,0,0);
                }
                j++;
            }
        }
        
        colT = Utilities.combineColor(colObs,colTMS);
        
        GeoT = Utilities.combineGPS(elevation, tms);
        
       
        panel.fillPositions(GeoT, intT, colT);
        panel.drawPositions(tms,intTMS2,colTMS);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    
    
    public void update(GeoPoint [] drones, Fleet F) {
        
        Rectangle2D.Double rect = new Rectangle2D.Double(-size/2,-size/2, size, size);
        MapPanel panel = new MapPanel(rect);
        panel.setBackground(Color.BLACK);
        panel.setPreferredSize( new Dimension(360, 360) );
        panel.setMinimumSize  ( panel.getPreferredSize()  );
        panel.showGrid( false );

        int[] intD = new int[drones.length];
        Arrays.fill(intD, 10);
        
        Color[] colD = new Color[drones.length];
        for (int i =0; i<drones.length;i++){
            int c = (int)Math.round(F.getLocation(i).getElevation()*1000*1.9375);
            //System.out.println(c);
            if (c>155){c = 155;}
            if (c<0){c = 0;}
            colD[i] = new Color(255-c,c+100,0);
        }
        
        
        GeoPoint[] GeoTotal = Utilities.combineGPS(GeoT, drones);

        Color [] colTotal = Utilities.combineColor(colT, colD);
        
        int[] intTotal = Utilities.combine(intT, intD);
        

        panel.fillPositions(GeoTotal, intTotal, colTotal);
        panel.drawPositions(tms,intTMS2,colTMS);
        Color [] routeColor = new Color[routes.length];
        Arrays.fill(routeColor, Color.RED);
        panel.drawRoutes(routes,routeColor);
        
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
    }
    
    public void setRoutes(Route[] routes){
        this.routes = routes;
    }
}
