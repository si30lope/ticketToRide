import java.awt.Color;
import java.util.ArrayList;

/**
 * Models a player for Ticket to Ride
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class Player
{
    //player's color
    public Color color;
    
    //player's name (deprecated)
    public String name;
    
    //player's score
    public int score;
    
    //amount of tokens left
    public int tokens;
    
    //number of types of trains
    public int numRed;
    public int numGreen;
    public int numBlue;
    public int numYellow;
    public int numOrange;
    public int numPink;
    public int numWhite;
    public int numBlack;
    public int numWild;
    
    //destination cards completed
    public int destinationsCompleted;
    
    //int arrays to track stocks
    int[] playerStocks = new int[9];
    int[] lowestStock = new int[9];
    
    //ArrayList of destination cards
    public ArrayList<String> destinations;
    
    //ArrayList of routes claimed
    public ArrayList<Route> routesClaimed = new ArrayList<>();
    
    //ArrayList of cities claimed
    public ArrayList<ArrayList> citiesClaimed = new ArrayList<ArrayList>();

    /**
     * Constructor for objects of type Player
     * 
     * @param name the player's name (not used)
     * @param color the player's color
     */
    public Player(String name, Color color)
    {
        //initialize instance variables
        this.name = name;
        this.color = color;

        tokens = 45;
        numRed = numGreen = numBlue = numYellow = 0;
        numPink = numWhite = numBlack = numWild = numOrange = 0;

        score = 0;
        destinationsCompleted = 0;

        destinations = new ArrayList<String>();
    }

    /**
     * Determines if players completed their destination cards
     * 
     */
    public void calcDestinations(){
        for(Route r:routesClaimed){
            String c1 = r.startCity.toUpperCase();
            String c2 = r.endCity.toUpperCase();
            boolean found = false;
            int i = 0;
            if(citiesClaimed.isEmpty()){
                citiesClaimed.add(new ArrayList<String>());
            }
            //Looks if either city is in an existing path
            while(i<citiesClaimed.size()){
                //checks if first city is in the path
                if(citiesClaimed.get(i).contains(c1)){
                    found = true;
                    //looks for second city in paths to merge paths
                    for(ArrayList<String> cities : citiesClaimed){
                        if(cities.contains(c2)){
                            //merge paths, ignoring duiplicates
                            for(String c : cities){
                                if(!citiesClaimed.get(i).contains(c)){
                                    citiesClaimed.get(i).add(c);
                                }
                            }
                        }
                    }
                    if(!citiesClaimed.get(i).contains(c2))
                        citiesClaimed.get(i).add(c2);
                }
                //checks if second city is in the path
                if(citiesClaimed.get(i).contains(c2)){
                    found = true;
                    for(ArrayList<String> cities : citiesClaimed){
                        if(cities.contains(c1)){
                            for(String c : cities){
                                if(!citiesClaimed.get(i).contains(c)){
                                    citiesClaimed.get(i).add(c);
                                }
                            }
                        }
                    }
                    if(!citiesClaimed.get(i).contains(c1))
                        citiesClaimed.get(i).add(c1);
                }
                i++;
            }
            if(found == false){
                ArrayList<String> temp = new ArrayList<String>();
                temp.add(c1);
                temp.add(c2);
                citiesClaimed.add(temp);
            }
        }
        int destComp = 0;
        for(String s : destinations){
            String[] info = s.split(" ");
            String startC = info[0].replaceAll("-"," ");
            String endC = info[1].replaceAll("-"," ");
            int pointValue = Integer.parseInt(info[2]);
            boolean completed = false;
            for( ArrayList cities : citiesClaimed){
                if(cities.contains(startC) && cities.contains(endC)){
                    completed = true;
                    break;
                }
            }
            if(completed){
                score += pointValue;
                destComp += 1;
            }
            else{
                score -= pointValue;
            }

            destinationsCompleted = destComp;
        }
    }
}