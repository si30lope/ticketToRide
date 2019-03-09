import java.util.ArrayList;
import java.util.Collections;

/**
 * reads in TrainCards through a text file
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class TrainCards
{
    private ArrayList<TrainCard> deck;
    /**
     * Constructor for objects of class TrainCard
     * @param deckFile: name of deckFile to be read from 
     */
    public TrainCards(String deckFile)
    {

        deck = new ArrayList<TrainCard>();
        final int numOfTrainCards = 12;
        final int numWildTrainCards = 14;

        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("green"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("blue"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("red"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("white"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("orange"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("black"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("pink"));
        }
        for(int i = 0; i < numOfTrainCards; i++){
            deck.add(new TrainCard("yellow"));
        }

        for(int i = 0; i < numWildTrainCards; i++){
            deck.add(new TrainCard("wild"));
        }
        Collections.shuffle(deck);
    }

    /**
     * resets the deck
     */
    public void resetTrainDeck(){
        Collections.shuffle(deck);
    }

    /**
     * accepts an arraylist of type TrainCard, shuffles it
     * and returns it 
     * @param ArrayList<DestCard> shuf: arraylist to be shuffled
     */
    public ArrayList resetCurrentTrainDeck(ArrayList<TrainCard> shuf){
        Collections.shuffle(shuf);
        return shuf;
    }

    /**
     * returns an instance of a shuffled, legal deck
     */
    public ArrayList getTrainDeck(){
        ArrayList<TrainCard> newDeck = new ArrayList<TrainCard>();
        for(int i = 0; i < deck.size(); i++){
            newDeck.add(deck.get(i));
        }
        return newDeck;
    }
}