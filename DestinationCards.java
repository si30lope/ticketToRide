import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Collections;

/**
 * reads in a file and constructs a legal deck of 
 * destination cards
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class DestinationCards
{
    private ArrayList<DestCard> deck;
    /**
     * Constructor for objects of class DestCard
     * @param String deckFile: name of file to read cards from
     */
    public DestinationCards(String deckFile)
    {
        try{
            // scanner to read in a file
            deck = new ArrayList<DestCard>();
            String start = "";
            String end = "";
            int value = 0;
            Scanner inFile = new Scanner(new File(deckFile));
            for(int i = 0; i < 50; i++){
                start = inFile.next();
                end = inFile.next();
                value = inFile.nextInt();
                deck.add(new DestCard(start, end, value));

            }
            inFile.close();

        }
        catch(FileNotFoundException exception){
            System.out.println("File not found" + exception);
        }

        Collections.shuffle(deck);

    }

    /**
     * resets the deck
     */
    public void resetDestinationDeck(){
        Collections.shuffle(deck);
    }

    /**
     * accepts an arraylist of type destination card, 
     * shuffles it and returns it 
     * @param ArrayList<DestCard> shuf: arraylist to be shuffled
     */    
    public ArrayList resetCurrentDestinationDeck(ArrayList<DestCard> shuf){
        Collections.shuffle(shuf);
        return shuf;
    }

    /**
     * returns an instance of a shuffled, legal
     * destination card deck
     */
    public ArrayList getDestinationDeck(){
        ArrayList<DestCard> newDeck = new ArrayList<DestCard>();
        for(int i = 0; i < deck.size(); i++){
            newDeck.add(deck.get(i));
        }
        return newDeck;
    }
}