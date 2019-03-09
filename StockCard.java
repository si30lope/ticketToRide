
/**
 * Models a stock card
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class StockCard
{
    //the type of Stock
    protected String type;
    
    //the amount available
    protected int numAvailable;
    
    //the top number
    protected int topNum;
    protected int location; //where the card is in the array of stock cards
    //stocks in order by index: BRP, JCL, WM, NCS, LV, RL, LE, BO, PRR

    /**
     * Constructor for objects of class StockCard
     * 
     * @param t Name of stock
     */
    public StockCard(String t)
    {
        type = t;
        topNum = 1;
        switch(t){
            case "BRP":
            numAvailable = 2;
            location = 0;
            break;

            case "JCL":
            numAvailable = 3;
            location = 1;
            break;

            case "WM":
            numAvailable = 4;
            location = 2;
            break;

            case "NCS":
            numAvailable = 5;
            location = 3;
            break;

            case "LV":
            numAvailable = 6;
            location = 4;
            break;

            case "RL":
            numAvailable = 7;
            location = 5;
            break;

            case "LE":
            numAvailable = 8;
            location = 6;
            break;

            case "BO":
            numAvailable = 10;
            location = 7;
            break;

            case "PRR":
            numAvailable = 15;
            location = 8;
            break;
        }
    }

    /**
     * Called when card is removed from stock pile
     * 
     * @return number card taken
     */
    public void takeCard()
    {
        numAvailable--;
        topNum++;
    }
}