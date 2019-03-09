import java.awt.Polygon;

/**
 * Models a train route for Ticket to Ride
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class Route
{
    //the Route's color
    public String color;
    
    //the Route's start city
    public String startCity;
    
    //the Route's end city
    public String endCity;
    
    //the Route's array of stocks
    public String[] stock;
    
    //the Route's clickable region
    public Polygon routeShape;
    
    //the Route's adjacent Route
    public Route adjacentRoute;
    
    //the Player who claimed the Route
    public Player claimedBy;
    
    //whether the Route has Stock or not
    public boolean hasStock;
    
    public boolean stock1; //BRP
    public boolean stock2; //JCL
    public boolean stock3; //WM
    public boolean stock4; //NCS
    public boolean stock5; //LV
    public boolean stock6; //RL
    public boolean stock7; //LE
    public boolean stock8; //BO
    public boolean stock9; //PRR
    public boolean claimed;
    
    //the Route's size
    public int size;
    
    /**
     * Constructor for objects of type Route
     * 
     * @param color the Route's color
     * @param start the Route's start city
     * @param end the Route's end city
     * @param hasStock if the Route has stock
     * @param size the Route's size
     * @param adj the Route's adjacent route
     * @param poly the Route's clickable region
     */
    public Route (String color, String start, 
    String end, boolean hasStock, int size, Route adj, Polygon poly)
    {
        this.color = color;
        startCity = start;
        endCity = end;
        this.hasStock = hasStock;
        this.size = size;
        adjacentRoute = adj;
        
        claimed = false;
        
        claimedBy = null;
        
        routeShape = poly;
    }
}