import java.awt.Color;

/**
 * skeleton of a single Destination Card
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class DestCard
{
    protected String start;
    protected String end;
    protected int pointValue;
    /**
     * Constructor for objects of class DestCard
     * @param String x: holds starting city of destination card
     * @param String y: holds ending city of destination card
     * @param int z: holds point value of destination card
     */
    public DestCard(String x, String y, int z){
        start = x;
        end = y;
        pointValue = z;
    }
}
