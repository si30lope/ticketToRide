import java.awt.*;
import java.util.ArrayList;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import java.awt.event.*; 
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Models the board game Ticket to Ride
 * 
 * @author Jonathan Pratico, Matt Pigliavento, Sara Lopez, 
 * Serene Medina, Grant Boughton 
 * @version 1.0
 */
public class Board extends JFrame implements MouseListener
{
    //array of Route objects
    public Route[] routes = new Route[95];

    //array of Player objects
    public Player[] players = new Player[5];

    //stores the image of the game board
    public BufferedImage image;
    
    //stores the current position in the
    //players array, stores number of
    //players
    public int playerIndex, numPlayers;

    //array of Polygons to store clickable 
    //regions
    public Polygon[] buttons;
    
    //array of stock cards
    StockCard[] stocks = new StockCard[9];

    //whether we can claim a route or not
    public boolean claimRoute;
    
    //initializing decks
    DestinationCards dDeck = new DestinationCards("Destinations.txt");
    TrainCards tDeck = new TrainCards("trainDeck.txt");
    ArrayList<DestCard> destDeck = dDeck.getDestinationDeck();
    ArrayList<TrainCard> trainDeck = tDeck.getTrainDeck();
    ArrayList<TrainCard> discards = new ArrayList<TrainCard>();
    ArrayList<TrainCard> flippedCards = new ArrayList<TrainCard>();

    //points for root sizes (0 not used)
    public int[] routePointValues = {0, 1, 2, 4, 7, 10, 15, 18};
    
    //tracks number of face-up wild cards
    public int wild = 0;
    
    //Polygon to hold the display destination button
    public Polygon destButton;
    
    //whether or not it's the last turn
    public boolean lastTurn = false;
    
    //tracks number of last turns
    public int lastTurnCounter = 0;
    
    //whether or not the board is clickable 
    public boolean canClick = true;
    
    /**
     * Main method for class Board
     * 
     * @param args an array of command line arguments 
     */
    public static void main(String[] args)
    {
        //set up frame
        Board frame = new Board();
        frame.setTitle("Ticket to Ride");
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        //start the game
        frame.start();
    }

    /**
     * Paints the frame
     * 
     * @param g a Graphics object
     */
    public void paint(Graphics g)
    {
        //draw the player's hand as strings
        g.setColor(Color.GRAY);
        g.fillRect(750, 80, 200, 220);
        g.setColor(Color.YELLOW);
        g.drawString("Player " 
            + (playerIndex + 1) + "'s hand:", 750, 100);
        g.drawString("Red cards: " 
            + players[playerIndex].numRed, 750, 120);
        g.drawString("Green cards: " 
            + players[playerIndex].numGreen, 750, 140);
        g.drawString("Blue cards: " 
            + players[playerIndex].numBlue, 750, 160);
        g.drawString("Yellow cards: " 
            + players[playerIndex].numYellow, 750, 180);
        g.drawString("Orange cards: " 
            + players[playerIndex].numOrange, 750, 200);
        g.drawString("Black cards: " 
            + players[playerIndex].numBlack, 750, 220);
        g.drawString("White cards: " 
            + players[playerIndex].numWhite, 750, 240);
        g.drawString("Pink cards: " 
            + players[playerIndex].numPink, 750, 260);
        g.drawString("Wild cards: " 
            + players[playerIndex].numWild, 750, 280);
            
        //draw face-up cards a strings
        g.setColor(Color.GRAY);
        g.fillRect(750, 300, 200, 220);
        g.setColor(Color.YELLOW);
        if (!flippedCards.isEmpty())
        {
            g.drawString("Flipped cards: " 
                + flippedCards.get(0).color, 750, 320);
            g.drawString("Flipped cards: " 
                + flippedCards.get(1).color, 750, 340);
            g.drawString("Flipped cards: " 
                + flippedCards.get(2).color, 750, 360);
            g.drawString("Flipped cards: " 
                + flippedCards.get(3).color, 750, 380);
            g.drawString("Flipped cards: " 
                + flippedCards.get(4).color, 750, 400);
        }

        //draws the player's stocks as strings
        g.setColor(Color.GRAY);
        g.fillRect(750, 420, 200, 220);
        g.setColor(Color.YELLOW);
        g.drawString("BRP Stock Cards: " 
            + players[playerIndex].playerStocks[0], 750, 440);
        g.drawString("JCL Stock Cards: " 
            + players[playerIndex].playerStocks[1], 750, 460);
        g.drawString("WM Stock Cards: " 
            + players[playerIndex].playerStocks[2], 750, 480);
        g.drawString("NCS Stock Cards: " 
            + players[playerIndex].playerStocks[3], 750, 500);
        g.drawString("LV Stock Cards: " 
            + players[playerIndex].playerStocks[4], 750, 520);
        g.drawString("RL Stock Cards: " 
            + players[playerIndex].playerStocks[5], 750, 540);
        g.drawString("LE Stock Cards: " 
            + players[playerIndex].playerStocks[6], 750, 560);
        g.drawString("BO Stock Cards: "
            + players[playerIndex].playerStocks[7], 750, 580);
        g.drawString("PRR Stock Cards: " 
            + players[playerIndex].playerStocks[8], 750, 600);

        //draws the player's remaining tokens as strings
        g.drawString("Tokens: " 
            + players[playerIndex].tokens, 750, 630);
 
        //draw the board as a BufferedImage
        g.drawImage(image, 0, 20, null);

        //fills destination view cards button
        g.setColor(Color.GRAY);
        g.fillPolygon(buttons[3]);
        g.setColor(Color.YELLOW);
        g.drawString("View Your Destination Cards", 750, 670);

        //paints all claimed routes 
        for (int i = 0; i < routes.length; i++)
        {
            if (routes[i].claimed)
            {
                //paint a filled polygon with the current player's color
                g.setColor(routes[i].claimedBy.color);
                g.fillPolygon(routes[i].routeShape);
            }
        }
    }

    /**
     * Prompts user for number of players, what color they want,
     * and then fills their hands with
     * train cards and destination cards, "flips up" the first
     * five train cards
     */
    public void start()
    {
        //converts the ArrayList to a String array
        String[] legalPlayers = {"2", "3", "4", "5"};
        String[] destinations = new String[5];
        
        //prompts the user
        int choice = 0;
        do
        {
            choice = JOptionPane.showOptionDialog(null, 
            "How many players", "Enter number of players:", 
            0, 0, null, legalPlayers, 0);
        }
        while (choice == -1);
        numPlayers = Integer.parseInt(legalPlayers[choice]);

        //determines what color each player is
        String[] trainOptions = {"Red", "Black", "Blue", "Green", "Yellow"};
        for(int playerIndex = 0; playerIndex < numPlayers; playerIndex++)  
        {
            int piece = 0;
            do
            {
                piece = JOptionPane.showOptionDialog(null, "Player " 
                    + (playerIndex + 1) + " choose a token color",
                    "Choose a piece", 
                    0, 0, null, trainOptions, 0);
            }
            while(piece == -1);

            String temp = trainOptions[piece];
            switch (temp)
            {
                case "Red":
                players[playerIndex].color = Color.RED;
                break;
                case "Black":
                players[playerIndex].color = Color.BLACK;
                break;
                case "Blue":
                players[playerIndex].color = Color.BLUE;
                break;
                case "Green":
                players[playerIndex].color = Color.GREEN;
                break;
                case "Yellow":
                players[playerIndex].color = Color.YELLOW;
                break;
                default:
                playerIndex--;
                continue;

            }
            trainOptions[piece] = "";
        }

        playerIndex = 0;

        int destChoice = 0;

        //big loop that will set the cards for all players
        for(playerIndex = 0; playerIndex < numPlayers; playerIndex++)
        {
            //drawing 5 destination cards
            for(int j = 0; j < 5; j++)
            {
                DestCard currentDestCard = destDeck.remove(
                destDeck.size() - 1);
                destinations[j] = currentDestCard.start + " " 
                + currentDestCard.end + " " 
                + Integer.toString(currentDestCard.pointValue);  
            } 

            //picking which ones you want
            for(int k = 0; k < 5; k++)
            {
                if(k == 0)
                {
                    destChoice = 0;
                    do
                    {
                        destChoice = JOptionPane.showOptionDialog(null,
                            "Player " + (playerIndex + 1)
                            + " choose a destination", "your options:", 
                            0, 0, null, destinations, 0);
                    }
                    while(destChoice == -1);

                    //adding their choice to arrayList of destination card

                    players[playerIndex].destinations.add(
                        destinations[destChoice]);

                    //eliminating the value they chose
                    destinations[destChoice] = "";
                }
                if(k == 1)
                {
                    destChoice = 0;
                    do
                    {
                        destChoice = JOptionPane.showOptionDialog(null,
                            "Player "
                            + (playerIndex + 1)
                            + " choose a destination", "your options:", 
                            0, 0, null, destinations, 0);
                    }
                    while(destChoice == -1);

                    //adding their choice to arrayList of 
                    //destination card if they dont click on the blank option

                    while(destinations[destChoice].equals(""))
                    {
                        destChoice = 0;
                        do
                        {
                            destChoice = JOptionPane.showOptionDialog(null,
                            "ILLEGAL SELECTION! Player "
                            + (playerIndex + 1) + " choose a destination",
                            "your options:", 
                            0, 0, null, destinations, 0);                           
                        }
                        while (destChoice == -1);
                    }
                    players[playerIndex].destinations.add(
                        destinations[destChoice]);

                    //eliminating the value they chose
                    destinations[destChoice] = "";
                }
                if(k == 2)
                {
                    destChoice = 0;
                    do
                    {
                        destChoice = JOptionPane.showOptionDialog(null,
                        "Player " + (playerIndex + 1)
                        + " choose a destination", "your options:", 
                        0, 0, null, destinations, 0);
                    }
                    while(destChoice == -1);

                    //adding their choice to arrayList of 
                    //destination card if they dont click on the blank option
                    while(destinations[destChoice].equals(""))
                    {
                        destChoice = 0;
                        do
                        {
                            destChoice = JOptionPane.showOptionDialog(null,
                                "ILLEGAL SELECTION! Player "
                                + (playerIndex + 1) + " choose a destination",
                                "your options:", 
                                0, 0, null, destinations, 0);
                        }
                        while(destChoice == -1);
                    }
                    players[playerIndex].destinations.add(
                    destinations[destChoice]);
                    //eliminating the value they chose
                    destinations[destChoice] = "";
                }
                if(k == 3)
                {
                    destChoice = 0;
                    do
                    {
                        destChoice = JOptionPane.showOptionDialog(null,
                            "Player " + (playerIndex + 1)
                            + " choose a destination/Choose blank space to stop",
                            "your options:", 
                            0, 0, null, destinations, 0);                            
                    }
                    while(destChoice == -1);

                    //option to stop drawing cards
                    if(destinations[destChoice].equals(""))break;
                    //adding their choice to arrayList of destination card
                    players[playerIndex].destinations.add(
                        destinations[destChoice]);
                    //eliminating the value they chose
                    destinations[destChoice] = "";
                }
                if(k == 4)
                {
                    destChoice = 0;
                    do
                    {
                        destChoice = JOptionPane.showOptionDialog(null,
                        "Player " + (playerIndex + 1)
                        + " choose a destination/Choose blank space to stop",
                        "your options:", 
                        0, 0, null, destinations, 0);
                    }
                    while(destChoice == -1);

                    //option to stop drawing cards
                    if(destinations[destChoice].equals(""))break;
                    //adding their choice to arrayList of destination card
                    players[playerIndex].destinations.add(
                        destinations[destChoice]);
                    //eliminating the value they chose
                    destinations[destChoice] = "";
                }
            }
           
            //"deals" each player four train cards
            for(int i = 0; i < 4; i++)
            {                
                TrainCard currentCard = trainDeck.remove(trainDeck.size()-1);
                if(currentCard.color.equals("green"))
                {
                    players[playerIndex].numGreen++;
                }
                else if(currentCard.color.equals("blue"))
                {
                    players[playerIndex].numBlue++;
                }
                else if(currentCard.color.equals("red"))
                {
                    players[playerIndex].numRed++;
                }
                else if(currentCard.color.equals("white"))
                {
                    players[playerIndex].numWhite++;
                }
                else if(currentCard.color.equals("orange"))
                {
                    players[playerIndex].numOrange++;
                }
                else if(currentCard.color.equals("black"))
                {
                    players[playerIndex].numBlack++;
                }
                else if(currentCard.color.equals("magenta"))
                {
                    players[playerIndex].numPink++;
                }
                else if(currentCard.color.equals("yellow"))
                {
                    players[playerIndex].numYellow++;
                }
                else if(currentCard.color.equals("wild"))
                {
                    players[playerIndex].numWild++;
                }
            }
            repaint();
        }
        playerIndex = 0;
 
        //flipping five cards face up
        for(int p = 0; p < 5; p++)
        {
            TrainCard flipped = trainDeck.remove(trainDeck.size()-1);
            if(flipped.color.equals("wild"))
            {
                wild++;
            }
            flippedCards.add(flipped);
        }
    }

    /**
     * shuffles a given arrayList of trainCards
     * 
     * @return ArrayList of shuffled train cards
     */    
    public ArrayList<TrainCard> resetCurrentTrainDeck(
    ArrayList<TrainCard> shuf){
        Collections.shuffle(shuf);
        return shuf;
    }

    /**
     * called when player clicks select trainCards button during gameplay
     */
    public void selectTrainCards(){
        String[] availableCards = new String[6];
        availableCards[0] = "draw from deck";
        int trainChoice = -1;
        int localWild = 0;
        int wilds = wild;
        for(int i = 0; i < 5; i++)
        {
            availableCards[i+1] = flippedCards.get(i).color;
        }
        //lets player draw two cards per turn
        int counter = 0;
        for(int k = 0; k < 2; k++)
        {
            trainChoice = 0;
            do
            {
                trainChoice = JOptionPane.showOptionDialog(null,
                "Player " + (playerIndex + 1)
                + " choose a train card", "your options:", 
                0, 0, null, availableCards, 0);
            }
            while(trainChoice == -1);

            if(availableCards[trainChoice].equals("green"))
            {
                players[playerIndex].numGreen++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("blue"))
            {
                players[playerIndex].numBlue++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("red"))
            {
                players[playerIndex].numRed++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("white"))
            {
                players[playerIndex].numWhite++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("orange"))
            {
                players[playerIndex].numOrange++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("black"))
            {
                players[playerIndex].numBlack++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("pink"))
            {
                players[playerIndex].numPink++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("yellow"))
            {
                players[playerIndex].numYellow++;
                counter++;
            }
            else if(availableCards[trainChoice].equals("wild")
            && (counter == 0))
            {
                players[playerIndex].numWild++;
                counter++;
                localWild++;
                wild++;
            }
            
            //drawing from deck
            else if(availableCards[trainChoice].equals(
            "draw from deck"))
            {
                TrainCard drawn = trainDeck.remove(trainDeck.size()-1);
                if(drawn.color.equals("green"))
                {
                    players[playerIndex].numGreen++;
                }
                else if(drawn.color.equals("blue"))
                {
                    players[playerIndex].numBlue++;
                }
                else if(drawn.color.equals("red"))
                {
                    players[playerIndex].numRed++;
                }
                else if(drawn.color.equals("white"))
                {
                    players[playerIndex].numWhite++;
                }
                else if(drawn.color.equals("orange"))
                {
                    players[playerIndex].numOrange++;
                }
                else if(drawn.color.equals("black"))
                {
                    players[playerIndex].numBlack++;
                }
                else if(drawn.color.equals("pink"))
                {
                    players[playerIndex].numPink++;
                }
                else if(drawn.color.equals("yellow"))
                {
                    players[playerIndex].numYellow++;
                }
                else if(drawn.color.equals("wild"))
                {
                    players[playerIndex].numWild++;
                }
                JOptionPane.showMessageDialog(null,
                "Player " + (playerIndex + 1) 
                + " drew " + drawn.color + " from the deck");
            }            
            //special case where player tries to select
            //a face-up wild card after
            //already selecting a face-up card
            else
            {
                JOptionPane.showMessageDialog(null,
                "Illegal selection try again");
                boolean illegal = true;
                while(illegal)
                {
                    trainChoice = 0;
                    do
                    {
                        trainChoice = JOptionPane.showOptionDialog(null,
                        "Player " + (playerIndex + 1) 
                        + " choose a train card", "your options:", 
                        0, 0, null, availableCards, 0);
                    }
                    while(trainChoice == -1);

                    if(availableCards[trainChoice].equals("green"))
                    {
                        players[playerIndex].numGreen++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("blue"))
                    {
                        players[playerIndex].numBlue++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("red"))
                    {
                        players[playerIndex].numRed++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("white"))
                    {
                        players[playerIndex].numWhite++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("orange"))
                    {
                        players[playerIndex].numOrange++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("black"))
                    {
                        players[playerIndex].numBlack++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("pink"))
                    {
                        players[playerIndex].numPink++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("yellow"))
                    {
                        players[playerIndex].numYellow++;
                        counter++;
                    }
                    else if(availableCards[trainChoice].equals("wild")
                    && (counter == 0))
                    {
                        players[playerIndex].numWild++;
                        counter++;
                        localWild++;
                        wild++;                       
                    }
                    //drawing from deck
                    else if(availableCards[trainChoice].equals(
                    "draw from deck"))
                    {
                        TrainCard drawn = trainDeck.remove(trainDeck.size()-1);
                        if(drawn.color.equals("green"))
                        {
                            players[playerIndex].numGreen++;
                        }
                        else if(drawn.color.equals("blue"))
                        {
                            players[playerIndex].numBlue++;
                        }
                        else if(drawn.color.equals("red"))
                        {
                            players[playerIndex].numRed++;
                        }
                        else if(drawn.color.equals("white"))
                        {
                            players[playerIndex].numWhite++;
                        }
                        else if(drawn.color.equals("orange"))
                        {
                            players[playerIndex].numOrange++;
                        }
                        else if(drawn.color.equals("black"))
                        {
                            players[playerIndex].numBlack++;
                        }
                        else if(drawn.color.equals("pink"))
                        {
                            players[playerIndex].numPink++;
                        }
                        else if(drawn.color.equals("yellow"))
                        {
                            players[playerIndex].numYellow++;
                        }
                        else if(drawn.color.equals("wild"))
                        {
                            players[playerIndex].numWild++;
                        }
                        JOptionPane.showMessageDialog(null, "Player "
                        + (playerIndex + 1) + " drew " 
                        + drawn.color + " from the deck");
                    }

                    if(!availableCards[trainChoice].equals("wild"))
                    {
                        illegal = false;
                    }
                }
            }
            //resetting menu and cards flipped and 
            //checking that three wild cards aren't face up

            if(!availableCards[trainChoice].equals("draw from deck"))
            {
                if(!trainDeck.isEmpty())
                {
                    if(wilds == 2){
                        while(trainDeck.get(
                        trainDeck.size()-1).color.equals("wild")){
                            TrainCard temp = trainDeck.remove(
                            trainDeck.size()-1);
                            trainDeck.set(0, temp);
                        }
                    }
                    //replacing face-up card selected
                    TrainCard replace = trainDeck.remove(trainDeck.size()-1);
                    if(replace.color.equals("wild")){wild++;}
                    flippedCards.set(trainChoice-1, replace);
                    availableCards[trainChoice] = replace.color;

                    //breaks if user selected a face up wildCard
                    if(localWild == 1)break; 
                }
                else
                {
                    trainDeck = resetCurrentTrainDeck(discards);
                    if(wilds == 2){
                        while(trainDeck.get(
                        trainDeck.size()-1).color.equals("wild")){
                            TrainCard temp = trainDeck.remove(
                            trainDeck.size()-1);
                            trainDeck.set(0, temp);
                        }
                    }
                    //replacing face-up card selected
                    TrainCard replace = trainDeck.remove(trainDeck.size()-1);
                    if(replace.color.equals("wild")){wild++;}
                    flippedCards.set(trainChoice-1, replace);
                    availableCards[trainChoice] = replace.color;
                    //breaks if user selected a face up wildCard
                    if(localWild == 1)break; 
                }
            }
        }

        if(lastTurn){ 
            lastTurnCounter++;
        }
    }

    /**
     * this method is called if you select draw destinations card mid game
     * 
     */
    public void selectDestinations()
    {
        String[] destinations = new String[4];
        int destChoice = 0;
        //drawing 4 destination cards
        for(int j = 0; j < 4; j++)
        {
            DestCard currentDestCard = destDeck.remove(destDeck.size() - 1);
            destinations[j] = currentDestCard.start + " " 
            + currentDestCard.end + " "
            + Integer.toString(currentDestCard.pointValue);  
        } 
        //picking which ones you want
        for(int k = 0; k < 4; k++)
        {
            if(k == 0)
            {
                do
                {
                    destChoice = JOptionPane.showOptionDialog(null,
                    "Player " + (playerIndex + 1)
                    + " choose a destination", "your options:", 
                        0, 0, null, destinations, 0);
                }
                while(destChoice == -1);
                //adding their choice to arrayList of destination card

                players[playerIndex].destinations.add(destinations[destChoice]);
                //eliminating the value they chose
                destinations[destChoice] = "";
            }
            if(k == 1)
            {
                do
                {
                    destChoice = JOptionPane.showOptionDialog(null,
                    "Player " + (playerIndex + 1)
                    + " choose a destination/Choose blank space to stop", 
                    "your options:", 
                        0, 0, null, destinations, 0);
                }
                while(destChoice == -1);

                //adding their choice to arrayList of destination
                //card if they dont click on the blank option

                if(destinations[destChoice].equals(""))break;
                players[playerIndex].destinations.add(destinations[destChoice]);
                //eliminating the value they chose
                destinations[destChoice] = "";
            }
            if(k == 2)
            {
                do
                {
                    destChoice = JOptionPane.showOptionDialog(null, "Player "
                    + (playerIndex + 1) 
                    + " choose a destination/Choose blank space to stop",
                    "your options:", 
                        0, 0, null, destinations, 0);
                }
                while(destChoice == -1);

                //adding their choice to arrayList of destination 
                //card if they dont click on the blank option

                if(destinations[destChoice].equals(""))break;
                players[playerIndex].destinations.add(destinations[destChoice]);
                //eliminating the value they chose
                destinations[destChoice] = "";
            }
            if(k == 3){
                do
                {
                    destChoice = JOptionPane.showOptionDialog(null, "Player "
                    + (playerIndex + 1)
                    + " choose a destination/Choose blank space to stop",
                    "your options:", 
                        0, 0, null, destinations, 0);
                }
                while(destChoice == -1);

                //option to stop drawing cards
                if(destinations[destChoice].equals(""))break;
                //adding their choice to arrayList of destination card
                players[playerIndex].destinations.add(destinations[destChoice]);
                //eliminating the value they chose
                destinations[destChoice] = "";
            }

        }
        if(lastTurn)
        {
            lastTurnCounter++;
        }
    }

    /**
     * Invoked when the mouse button has been pressed on a component.
     * Also acts as the "game loop," managing player turns and
     * determining if it's the last set of turns
     * 
     * @param e a MouseEvent object 
     */
    public void mousePressed(MouseEvent e)
    {   
        //checks to see if the player clicked on a route
        if (canClick && lastTurnCounter != numPlayers)
        {
            if (claimRoute) //claim route phase
            {
                //loop through array of Routes
                for (int i = 0; i < routes.length; i++)
                {
                    if (!routes[i].claimed
                    && routes[i].routeShape.contains(e.getX(), e.getY()))
                    {                        
                        //if the player selected route can be 
                        //currently claimed by them, then mark it as claimed
                        //if there are only two player, mark 
                        //its adjacent route as claimed as well
                        if (canClaim(i))
                        {
                            routes[i].claimed = true;
                            routes[i].claimedBy = players[playerIndex];
                            if (numPlayers < 4 
                            && routes[i].adjacentRoute != null)
                            {
                                routes[i].adjacentRoute.claimed = true;
                                routes[i].adjacentRoute.claimedBy 
                                = players[playerIndex];
                            }
                            if(lastTurn)
                            {
                                lastTurnCounter++;
                            }
                              
                            //CLAIM STOCK
                            drawStock(i);
                            repaint();
                            players[playerIndex].routesClaimed.add(routes[i]);
                            players[playerIndex].score
                            += routePointValues[routes[i].size];
                            JOptionPane.showMessageDialog(null,
                            "Claimed route from "
                            + routes[i].startCity 
                            + " to " + routes[i].endCity + " for "
                            + routePointValues[routes[i].size] + " point(s)!");

                            //determines if it's time for last turns
                            if (canClick && !lastTurn)
                            {
                                for (int j = 0; j < numPlayers; j++)
                                {
                                    if (players[j].tokens < 3)
                                    {
                                        lastTurn = true;
                                        JOptionPane.showMessageDialog(null,
                                        "Last Turn for all Players");
                                    }
                                }
                            }
                            
                            //switch turns
                            if (playerIndex == numPlayers - 1)
                            {
                                playerIndex = 0;
                            }
                            else
                            {
                                playerIndex++;
                            }
                            repaint();
                            claimRoute = false;
                            break;
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null, 
                            "Cannot claim route from " 
                            + routes[i].startCity + " to "
                            + routes[i].endCity);
                        }
                    }
                }
            }
            
            //checks if player clicks on any clickable region
            if (buttons[0].contains(e.getX(), e.getY()))
            {
                claimRoute = false;
                selectDestinations();
                
                //determines if it's time for last turns
                if (canClick && !lastTurn)
                {
                    for (int j = 0; j < numPlayers; j++)
                    {
                        if (players[j].tokens < 3)
                        {
                            lastTurn = true;
                            JOptionPane.showMessageDialog(null,
                            "Last Turn for all Players");
                        }
                    }
                }
                
                //switch turns
                if (playerIndex == numPlayers - 1)
                {
                    playerIndex = 0;
                }
                else
                {
                    playerIndex++;
                }
                repaint();
            }
            if (buttons[1].contains(e.getX(), e.getY()))
            {
                claimRoute = false;
                selectTrainCards();
                if (canClick && !lastTurn)
                {
                    for (int j = 0; j < numPlayers; j++)
                    {
                        if (players[j].tokens < 3)
                        {
                            lastTurn = true;
                            JOptionPane.showMessageDialog(null, 
                            "Last Turn for all Players");
                        }
                    }
                }
                if (playerIndex == numPlayers - 1)
                {
                    playerIndex = 0;
                }
                else
                {
                    playerIndex++;
                }
                repaint();
            }
            if (buttons[2].contains(e.getX(), e.getY()))
            {
                JOptionPane.showMessageDialog(null, 
                "Click a route to claim");
                claimRoute = true;
            }
            if(buttons[3].contains(e.getX(), e.getY())){
                String[] destDisplay 
                = new String[players[playerIndex].destinations.size()];
                String message = "";
                for(int k = 0; k 
                < players[playerIndex].destinations.size(); k++){
                    message = message
                    + players[playerIndex].destinations.get(k) + "\n";
                }
                JOptionPane.showMessageDialog(null, message);
            }
        }

        //end game and trigger scoring
        if (canClick && (lastTurnCounter == numPlayers))
        {
            canClick = false;
            JOptionPane.showMessageDialog(null, "Game Over");
            //SCORING
            calculateFinalScore();
        }
    }

    /**
     * Calculates the final score for each player
     * 
     */
    public void calculateFinalScore(){
        //calculates completed destinations
        for(int i = 0; i < numPlayers; i++){
            players[i].calcDestinations();
        }

        //most destination cards bonus
        int largestIndex = 0;
        
        //array list for settling ties
        ArrayList<Integer> ties = new ArrayList<>();
        for(int i = 1; i < numPlayers; i++){
            if (players[i].destinationsCompleted
            > players[largestIndex].destinationsCompleted)
            {
                largestIndex = i;
            }
            else if (players[i].destinationsCompleted
            == players[largestIndex].destinationsCompleted)
            {
                ties.add(i);
            }
        }
        
        //settles ties
        if (ties.size() > 1)
        {
            for (int i = 0; i < ties.size(); i++)
            {
                players[ties.get(i)].score += 15;
            }
        }
        else
        {
            players[largestIndex].score += 15;
        }        

        //Calculates the stock scores
        calculateStockScores();

        //display scores for each player
        String displayScores = "";
        for(int k = 0; k < numPlayers; k++){
            displayScores += "player "
            + (k + 1) + "'s score: " + players[k].score + "\n"; 
        }
        JOptionPane.showMessageDialog(null, displayScores);
    }

    /**
     * Allows the player to pick stock from a Route
     * 
     * @param routeIndex the index (as an int) of the selected Route in
     *        the routes array
     */
    public void drawStock(int routeIndex)
    {
        //if no stock on route, return
        if (!routes[routeIndex].hasStock)
        {
            return;
        }

        Route temp = routes[routeIndex];
        ArrayList<String> options = new ArrayList<>();
        
        //Adds available stocks to an ArrayList
        if (temp.stock1 && stocks[0].numAvailable > 0)
        {
            options.add("BRP (" + stocks[0].topNum + "/2)");
        }
        if (temp.stock2 && stocks[1].numAvailable > 0)
        {
            options.add("JCL (" + stocks[1].topNum + "/3)");
        }
        if (temp.stock3 && stocks[2].numAvailable > 0)
        {
            options.add("WM (" + stocks[2].topNum + "/4)");
        }
        if (temp.stock4 && stocks[3].numAvailable > 0)
        {
            options.add("NCS (" + stocks[3].topNum + "/5)");
        }
        if (temp.stock5 && stocks[4].numAvailable > 0)
        {
            options.add("LV (" + stocks[4].topNum + "/6)");
        }
        if (temp.stock6 && stocks[5].numAvailable > 0)
        {
            options.add("RL (" + stocks[5].topNum + "/7)");
        }
        if (temp.stock7 && stocks[6].numAvailable > 0)
        {
            options.add("LE (" + stocks[6].topNum + "/8)");
        }
        if (temp.stock8 && stocks[7].numAvailable > 0)
        {
            options.add("BO (" + stocks[7].topNum + "/10)");
        }
        if (temp.stock9 && stocks[8].numAvailable > 0)
        {
            options.add("PRR (" + stocks[8].topNum + "/15)");
        }

        String[] stockOptions = options.toArray(new String[options.size()]);
      
        //Dialog box that displays available stocks
        int choice = 0;
        do
        {
            choice = JOptionPane.showOptionDialog(null,
            "Choose a stock", "Stock options:", 
            0, 0, null, stockOptions, 0);
        }
        while(choice == -1);

        String stockChoice = stockOptions[choice].substring(0, 2);
        //updates player's stock number and lowest numbered
        //stock owned, updates StockCards
        switch(stockChoice)
        {
            case "BR":
            players[playerIndex].playerStocks[0]++;
            //first card taken of a specific type 
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[0] == 0){
                players[playerIndex].lowestStock[0] = stocks[0].topNum;
            }
            stocks[0].takeCard();
            break;

            case "JC":
            players[playerIndex].playerStocks[1]++;
            //first card taken of a specific type
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[1] == 0){
                players[playerIndex].lowestStock[1] = stocks[1].topNum;
            }
            stocks[1].takeCard();
            break;

            case "WM":
            players[playerIndex].playerStocks[2]++;
            //first card taken of a specific type
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[2] == 0){
                players[playerIndex].lowestStock[2] = stocks[2].topNum;
            }
            stocks[2].takeCard();
            break;

            case "NC":
            players[playerIndex].playerStocks[3]++;
            //first card taken of a specific type
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[3] == 0){
                players[playerIndex].lowestStock[3] = stocks[3].topNum;
            }
            stocks[3].takeCard();
            break;

            case "LV":
            players[playerIndex].playerStocks[4]++;
            //first card taken of a specific type 
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[4] == 0){
                players[playerIndex].lowestStock[4] = stocks[4].topNum;
            }
            stocks[4].takeCard();
            break;

            case "RL":
            players[playerIndex].playerStocks[5]++;
            //first card taken of a specific type
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[5] == 0){
                players[playerIndex].lowestStock[5] = stocks[5].topNum;
            }
            stocks[5].takeCard();
            break;

            case "LE":
            players[playerIndex].playerStocks[6]++;
            //first card taken of a specific type 
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[6] == 0){
                players[playerIndex].lowestStock[6] = stocks[6].topNum;
            }
            stocks[6].takeCard();
            break;

            case "BO":
            players[playerIndex].playerStocks[7]++;
            //first card taken of a specific type 
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[7] == 0){
                players[playerIndex].lowestStock[7] = stocks[7].topNum;
            }
            stocks[7].takeCard();
            break;

            case "PR":
            players[playerIndex].playerStocks[8]++;
            //first card taken of a specific type 
            //is going to be the lowest number owned
            if(players[playerIndex].lowestStock[8] == 0){
                players[playerIndex].lowestStock[8] = stocks[8].topNum;
            }
            stocks[8].takeCard();
            break;
        }
    }

    /**
     * Calculates the stock score for each player
     */
    public void calculateStockScores()
    {
        //BRP
        int first = 0;
        int second = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[0] 
            > players[first].playerStocks[0])
            {
                first = i;
            }
        }
        for (int i = 1; i < numPlayers; i++)
        {
            if ((players[i].playerStocks[0]
            > players[second].playerStocks[0])
            && players[i].playerStocks[0]
            < players[first].playerStocks[0])
            {
                second = i;
            }
        }
        
        if (players[first].playerStocks[0] > 0)
        {
            players[first].score += 7; 
        }
        if (players[second].playerStocks[0]
        > 0 && second != first)
        {
            players[second].score += 4; 
        }

        //JCL
        first = 0;
        second = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[1] 
            > players[first].playerStocks[1])
            {
                first = i;
            }
        }
        for (int i = 1; i < numPlayers; i++)
        {
            if ((players[i].playerStocks[1]
            > players[second].playerStocks[1])
            && players[i].playerStocks[1]
            < players[first].playerStocks[1])
            {
                second = i;
            }
        }

        if (players[first].playerStocks[1] > 0)
        {
            players[first].score += 8; 
        }
        if (players[second].playerStocks[1] 
        > 0 && second != first)
        {
            players[second].score += 5; 
        }

        //WM
        first = 0;
        second = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[2] 
            > players[first].playerStocks[2])
            {
                first = i;
            }
        }
        for (int i = 1; i < numPlayers; i++)
        {
            if ((players[i].playerStocks[2] 
            > players[second].playerStocks[2])
            && players[i].playerStocks[2] 
            < players[first].playerStocks[2])
            {
                second = i;
            }
        }

        if (players[first].playerStocks[2] > 0)
        {
            players[first].score += 9; 
        }
        if (players[second].playerStocks[2]
        > 0 && second != first)
        {
            players[second].score += 5; 
        }

        //NCS
        first = 0;
        second = 0;
        int third = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[3] 
            > players[first].playerStocks[3])
            {
                third = second;
                second = first;                
                first = i;
            }
            else if (players[i].playerStocks[3] 
            > players[second].playerStocks[3])
            {
                third = second;
                second = i;
            }
            else if (players[i].playerStocks[3] 
            > players[third].playerStocks[3])
            {
                third = i;
            }
        }

        if (players[first].playerStocks[3] > 0)
        {
            players[first].score += 10; 
        }
        if ((players[second].playerStocks[3] 
        > 0) && second != first)
        {
            players[second].score += 6; 
        }
        if ((players[third].playerStocks[3] > 0)
        && (second != first && second != third))
        {
            players[third].score += 3;
        }        

        //LV
        first = 0;
        second = 0;
        third = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[4]
            > players[first].playerStocks[4])
            {
                third = second;
                second = first;                
                first = i;
            }
            else if (players[i].playerStocks[4] 
            > players[second].playerStocks[4])
            {
                third = second;
                second = i;
            }
            else if (players[i].playerStocks[4]
            > players[third].playerStocks[4])
            {
                third = i;
            }
        }

        if (players[first].playerStocks[4] > 0)
        {
            players[first].score += 12; 
        }
        if (players[second].playerStocks[4] > 0 && second != first)
        {
            players[second].score += 7; 
        }
        if (players[third].playerStocks[4] > 0
        && (second != first && second != third))
        {
            players[third].score += 3;
        }  

        //RL
        first = 0;
        second = 0;
        third = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[5] 
            > players[first].playerStocks[5])
            {
                third = second;
                second = first;                
                first = i;
            }
            else if (players[i].playerStocks[5] 
            > players[second].playerStocks[5])
            {
                third = second;
                second = i;
            }
            else if (players[i].playerStocks[5]
            > players[third].playerStocks[5])
            {
                third = i;
            }
        }

        if (players[first].playerStocks[5] > 0)
        {
            players[first].score += 14; 
        }
        if (players[second].playerStocks[5]
        > 0 && second != first)
        {
            players[second].score += 9; 
        }
        if (players[third].playerStocks[5] > 0
        && (second != first && second != third))
        {
            players[third].score += 5;
        }  

        //LE
        first = 0;
        second = 0;
        third = 0;
        int fourth = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[6] 
            > players[first].playerStocks[6])
            {
                fourth = third;
                third = second;
                second = first;                
                first = i;
            }
            else if (players[i].playerStocks[6] 
            > players[second].playerStocks[6])
            {
                fourth = third;
                third = second;
                second = i;
            }
            else if (players[i].playerStocks[6] 
            > players[third].playerStocks[6])
            {
                fourth = third;
                third = i;
            }
            else if (players[i].playerStocks[6]
            > players[fourth].playerStocks[6])
            {
                fourth = i;
            }
        }

        if (players[first].playerStocks[6] > 0)
        {
            players[first].score += 16; 
        }
        if (players[second].playerStocks[6] > 0 && second != first)
        {
            players[second].score += 10; 
        }
        if (players[third].playerStocks[6] > 0
        && (second != first && second != third))
        {
            players[third].score += 5;
        }  
        if (players[fourth].playerStocks[6] > 0
        && (second != first && second != third && third != fourth))
        {
            players[fourth].score += 1;
        }        

        //BO
        first = 0;
        second = 0;
        third = 0;
        fourth = 0;
        int fifth = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[7] 
            > players[first].playerStocks[7])
            {
                fifth = fourth;
                fourth = third;
                third = second;
                second = first;                
                first = i;
            }
            else if (players[i].playerStocks[7]
            > players[second].playerStocks[7])
            {
                fifth = fourth;
                fourth = third;
                third = second;
                second = i;
            }
            else if (players[i].playerStocks[7]
            > players[third].playerStocks[7])
            {
                fifth = fourth;
                fourth = third;
                third = i;
            }
            else if (players[i].playerStocks[7] 
            > players[fourth].playerStocks[7])
            {
                fifth = fourth;
                fourth = i;
            }
            else if (players[i].playerStocks[7]
            > players[fifth].playerStocks[7])
            {
                fifth = i;
            }
        }

        if (players[first].playerStocks[7] > 0)
        {
            players[first].score += 20; 
        }
        if (players[second].playerStocks[7] > 0 && second != first)
        {
            players[second].score += 14; 
        }
        if (players[third].playerStocks[7] > 0
        && (second != first && second != third))
        {
            players[third].score += 9;
        }  
        if (players[fourth].playerStocks[7] > 0
        && (second != first && second != third && third != fourth))
        {
            players[fourth].score += 5;
        }  
        if (players[fifth].playerStocks[7] > 0
        && ((second != first && second != third) 
        && (third != fourth && fourth != fifth)))
        {
            players[fifth].score += 2;
        }       

        //PRR
        first = 0;
        second = 0;
        third = 0;
        fourth = 0;
        fifth = 0;
        for (int i = 1; i < numPlayers; i++)
        {
            if (players[i].playerStocks[8] 
            > players[first].playerStocks[8])
            {
                fifth = fourth;
                fourth = third;
                third = second;
                second = first;                
                first = i;
            }
            else if (players[i].playerStocks[8]
            > players[second].playerStocks[8])
            {
                fifth = fourth;
                fourth = third;
                third = second;
                second = i;
            }
            else if (players[i].playerStocks[8]
            > players[third].playerStocks[8])
            {
                fifth = fourth;
                fourth = third;
                third = i;
            }
            else if (players[i].playerStocks[8] 
            > players[fourth].playerStocks[8])
            {
                fifth = fourth;
                fourth = i;
            }
            else if (players[i].playerStocks[8] 
            > players[fifth].playerStocks[8])
            {
                fifth = i;
            }
        }

        if (players[first].playerStocks[8] > 0)
        {
            players[first].score += 30; 
        }
        if (players[second].playerStocks[8] > 0 && second != first)
        {
            players[second].score += 21; 
        }
        if (players[third].playerStocks[8] > 0
        && (second != first && second != third))
        {
            players[third].score += 14;
        }  
        if (players[fourth].playerStocks[8] > 0
        && (second != first && second != third && third != fourth))
        {
            players[fourth].score += 9;
        }  
        if (players[fifth].playerStocks[8] > 0
        && ((second != first && second != third) 
        && (third != fourth && fourth != fifth)))
        {
            players[fifth].score += 6;
        }       
    }

    /**
     * Checks to see if a Route can be claimed by a Player,
     * and if so, removes the appropriate amount
     * of game pieces and colored train cards from the user's hand
     * 
     * @param routeIndex the index of the Route object in the routes array
     * @return true if a valid claim, false otherwise
     */
    public boolean canClaim(int routeIndex)
    {
        //temporary Route object
        Route temp = routes[routeIndex]; 

        //prevents the player from claiming a route adjacent to
        //one they already claimed
        if ((temp.adjacentRoute!= null
            && temp.adjacentRoute.claimedBy == players[playerIndex])
        || players[playerIndex].tokens < temp.size)
        {
            return false;
        }

        //determines the color of the selected route 
        //and if the player has enough cards of that color to claim it
        //if so, reduce the amount of cards of that color, and the number
        //of tokens needed to claim the route
        //true only if the selected Route is red and the player
        //has enough cards to claim it
        if (temp.color.equals("Red") 
        && players[playerIndex].numRed
        + players[playerIndex].numWild >= temp.size)
        {
            //build a player's list of options
            ArrayList<String> options = new ArrayList<>();

            //if player has enough reds to claim, add that option
            if (players[playerIndex].numRed >= temp.size)
            {
                options.add("Reds only");
            }

            //if player has reds and wilds with a 
            //sum >= the size of the Route, add that option
            if ((players[playerIndex].numRed > 0
            && players[playerIndex].numWild > 0) 
            && players[playerIndex].numRed
            + players[playerIndex].numWild >= temp.size)
            {
                options.add("Mix reds and wilds");
            }

            //if player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert ArrayList to array of Strings
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //show the user their options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null, 
                "How would you like to claim the route? ("
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //use only red cards
            if (finalOptions[choice].charAt(0) == 'R')
            {
                //reduce the amount of red cards in the 
                //player's hand by the size of the Route
                players[playerIndex].numRed -= temp.size;    

                //discard the amount of reds used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("red"));
                }
            }
            //use only wild cards
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //reduce the amount of wild cards in the
                //player's hand by the size of the Route
                players[playerIndex].numWild -= temp.size;    

                //discards the amound of wilds used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("wild"));
                }
            }
            //use a mix of a single color and wild cards to claim
            else
            {
                //counter to track the amount of cards
                //reamining to claim the Route
                int count = temp.size;

                //loop allows player to choose how many colors and wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //builds the players options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if there are red cards remaining, add the option to the ArrayList
                    if (players[playerIndex].numRed > 0)
                    {
                        newOptions.add("Red (x" 
                        + players[playerIndex].numRed + ")");
                    }
                    //if there are wild cards remaining, add the option to the ArrayList
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the player's option to an array of Strings
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the user's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null,
                        "Choose train card to use (" + count
                        + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //determine the user's choice based
                    //on the first character of their choice
                    //player chose to use a red
                    if (selectOptions[choice].charAt(0) == 'R')
                    {
                        //decrement the amount of red cards in the player's hand
                        players[playerIndex].numRed -= 1;    

                        //discards a red card
                        discards.add(new TrainCard("red"));
                    }
                    //player chose to use a wild
                    else
                    {
                        //decrement the amount of wild cards in the player's hand
                        players[playerIndex].numWild -= 1; 

                        //discard a wild card
                        discards.add(new TrainCard("wild"));
                    }

                    //decrement the amount of cards needed
                    count--;
                }
            }

            //reduce the amount of player's tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //true only if the selected Route is 
        //red and the player has enough cards to claim it
        else if (temp.color.equals("Green")
        && players[playerIndex].numGreen
        + players[playerIndex].numWild >= temp.size)
        {
            //builds a player's list of options
            ArrayList<String> options = new ArrayList<>();

            //if player has enough greens to claim, add that option
            if (players[playerIndex].numGreen >= temp.size)
            {
                options.add("Greens only");
            }

            //if player has greens and wild that have 
            //a sum >= the size of the Route, add that option
            if ((players[playerIndex].numGreen > 0
            && players[playerIndex].numWild > 0) 
            && players[playerIndex].numGreen 
            + players[playerIndex].numWild >= temp.size)
            {
                options.add("Mix greens and wilds");
            }

            //if player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert the ArrayList of option to an array of Strings
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the user's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null,
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //user chose to use all greens        
            if (finalOptions[choice].charAt(0) == 'G')
            {
                //decrease the amount of greens in 
                //the player's hand by the size of the Route
                players[playerIndex].numGreen -= temp.size;   

                //discard the amount of green cards required to claim the Route
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("green"));
                }
            }
            //user chose to use all wilds
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //decrease the amount of greens in the 
                //player's hand by the size of the Route
                players[playerIndex].numWild -= temp.size;  

                //discard the amount of green cards required to claim the Route
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("wild"));
                }
            }
            //user chose to mix greens and wilds
            else
            {
                //counter to keep track of the number of 
                //cards needed to claim the route
                int count = temp.size;

                //loop allows player to choose how many colors and wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold player's available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if the player has green cards, add that option
                    if (players[playerIndex].numGreen > 0)
                    {
                        newOptions.add("Green (x" 
                        + players[playerIndex].numGreen + ")");
                    }

                    //if the player has wild cards, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList of options to an array of Strings
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the user's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null,
                        "Choose train card to use (" + count 
                        + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chooses to use a green
                    if (selectOptions[choice].charAt(0) == 'G')
                    {
                        //decrement the amount of greens in the player's hand
                        players[playerIndex].numGreen -= 1;   

                        //discard a green card
                        discards.add(new TrainCard("green"));
                    }
                    else
                    {
                        //decrement the amount of wilds in the player's hand
                        players[playerIndex].numWild -= 1;

                        //discard a wild card
                        discards.add(new TrainCard("wild"));
                    }

                    //decrement the amount of cards needed to claim route
                    count--;
                }
            }

            //reduce the amount of player tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //if the selected route is blue and the player
        //has enough cards to claim it
        else if (temp.color.equals("Blue") 
        && players[playerIndex].numBlue 
        + players[playerIndex].numWild >= temp.size)
        {
            //ArrayList to hold the player's options
            ArrayList<String> options = new ArrayList<>();

            //if the player has enough blues to claim the Route, add that option
            if (players[playerIndex].numBlue >= temp.size)
            {
                options.add("Blues only");
            }

            //if the player has blues and wilds 
            //with a sum >= the size of the Route, add that option
            if ((players[playerIndex].numBlue > 0 
            && players[playerIndex].numWild > 0) 
            && (players[playerIndex].numBlue
            + players[playerIndex].numWild >= temp.size))
            {
                options.add("Mix blues and wilds");
            }

            //if the player has enough wilds to claim the Route, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //converts ArrayList of options to a String array
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the user's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null, 
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //player chose to use all blues
            if (finalOptions[choice].charAt(0) == 'B')
            {
                //reduce the amount of blues in 
                //the player's hand by the size of the Route
                players[playerIndex].numBlue -= temp.size; 

                //discard the amount of blue cards used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("blue"));
                }
            }
            //player chose to use all wilds
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //reduce the amount of wilds in 
                //the player's hand by the size of the Route
                players[playerIndex].numWild -= temp.size;    

                //discard the amount of wild cards used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("wild"));
                }
            }
            //player chose to mix blues and wilds
            else
            {
                //counter to track the amount of cards needed to claim the Route
                int count = temp.size;

                //loop allows player to choose how many colors and wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if player has blues, add that option
                    if (players[playerIndex].numBlue > 0)
                    {
                        newOptions.add("Blue (x" 
                        + players[playerIndex].numBlue + ")");
                    }

                    //if player has wilds, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList of options to a String array
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null, 
                        "Choose train card to use (" + count
                        + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chose to use a blue
                    if (selectOptions[choice].charAt(0) == 'B')
                    {
                        //decrement the amount of blues in the player's hand
                        players[playerIndex].numBlue -= 1; 

                        //discard a blue card
                        discards.add(new TrainCard("blue"));
                    }
                    else
                    {
                        //decrement the amount of blues in the player's hand
                        players[playerIndex].numWild -= 1;  

                        //discard a wild card
                        discards.add(new TrainCard("wild"));                       
                    }

                    //decrement the counter
                    count--;
                }
            }

            //reduce the player's amount of tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //if the selected route is yellow and the player has enough cards to claim it
        else if (temp.color.equals("Yellow") 
        && players[playerIndex].numYellow
        + players[playerIndex].numWild >= temp.size)
        {
            //ArrayList to hold the available options
            ArrayList<String> options = new ArrayList<>();

            //if the player has enough yellows to claim, add that option
            if (players[playerIndex].numYellow >= temp.size)
            {
                options.add("Yellows only");
            }

            //if the player has yellows and wilds
            //with a sum >= the size of the Route, add that option
            if ((players[playerIndex].numYellow > 0
            && players[playerIndex].numWild > 0) 
            && (players[playerIndex].numYellow 
            + players[playerIndex].numWild >= temp.size))
            {
                options.add("Mix yellow and wilds");
            }

            //if the player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert the ArrayList of option into a String array
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the player's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null, 
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //player chose to use all yellows
            if (finalOptions[choice].charAt(0) == 'Y')
            {
                //decrement the number of yellows in 
                //the player's hand by the size of the Route
                players[playerIndex].numYellow -= temp.size;   

                //discard the amount of yellow cards used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("yellow"));
                }
            }
            //player chose to use all wilds
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //decrement the number of yellows in
                //the player's hand by the size of the Route
                players[playerIndex].numWild -= temp.size;  

                //discard the amount of wild cards used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("wild"));
                }
            }
            //player chose to mix yellows and wilds
            else
            {
                //counter to track the number of cards needed to claim
                int count = temp.size;

                //loop allows player to choose 
                //how many colors and wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold the available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if the player has yellows, add that option
                    if (players[playerIndex].numYellow > 0)
                    {
                        newOptions.add("Yellow (x"
                        + players[playerIndex].numYellow + ")");
                    }

                    //if the player has wilds, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList of options into a String array
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //dislay the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null,
                        "Choose train card to use ("
                        + count + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chose to use a yellow
                    if (selectOptions[choice].charAt(0) == 'Y')
                    {
                        //decrement the player's amount of yellows
                        players[playerIndex].numYellow -= 1;   

                        //discard a yellow card
                        discards.add(new TrainCard("yellow"));                        
                    }
                    else
                    {
                        //decrement the player's amount of wilds
                        players[playerIndex].numWild -= 1;  

                        //discard a wild card
                        discards.add(new TrainCard("wild"));                        
                    }

                    //decrement the counter
                    count--;
                }
            }

            //reduce the amount of player's tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //if the selected route is orange and 
        //the player has enough cards to claim it
        else if (temp.color.equals("Orange") 
        && players[playerIndex].numOrange 
        + players[playerIndex].numWild   >= temp.size)
        {
            //ArrayList to hold available options
            ArrayList<String> options = new ArrayList<>();

            //if the player has enough oranges to claim, add that option
            if (players[playerIndex].numOrange >= temp.size)
            {
                options.add("Oranges only");
            }

            //if the player has oranges and wilds with
            //a sum >= the size of the Route, add that option
            if ((players[playerIndex].numOrange > 0 
            && players[playerIndex].numWild > 0)
            && (players[playerIndex].numOrange 
            + players[playerIndex].numWild >= temp.size))
            {
                options.add("Mix orange and wilds");
            }

            //if the player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert the ArrayList of options into a String array
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the player's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null, 
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //player chose to use all oranges
            if (finalOptions[choice].charAt(0) == 'O')
            {
                //reduce the player's amount of oranges by the size of the Route
                players[playerIndex].numOrange -= temp.size; 

                //discard the amount of orange cards used
                for(int i = 0; i < temp.size; i++){
                    discards.add(new TrainCard("orange"));
                }
            }
            //player chose to use all wilds
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //reduce the player's amount of oranges by the size of the Route
                players[playerIndex].numWild -= temp.size;    

                //discard the amount of wild cards used
                for(int i = 0; i < temp.size; i++){
                    discards.add(new TrainCard("wild"));
                }
            }
            //player chose to mix oranges and wilds
            else
            {
                //counter to track the amount of cards needed to claim
                int count = temp.size;

                //loop allows player to choose 
                //how many colors and wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if player has oranges, add that options
                    if (players[playerIndex].numOrange > 0)
                    {
                        newOptions.add("Orange (x" 
                        + players[playerIndex].numOrange + ")");
                    }

                    //if plater has wilds, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList of options into a String array
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null, 
                        "Choose train card to use (" + count 
                        + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chose to use an orange
                    if (selectOptions[choice].charAt(0) == 'O')
                    {
                        //decrement the player's amount of oranges
                        players[playerIndex].numOrange -= 1; 

                        //discard an orange card
                        discards.add(new TrainCard("orange"));                     
                    }
                    else
                    {
                        //decrement the player's amount of oranges
                        players[playerIndex].numWild -= 1;  

                        //discard a wild card
                        discards.add(new TrainCard("wild"));
                    }

                    //decrement the counter
                    count--;
                }
            }

            //reduce the player's amount of tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //if the selected route is pink and the
        //player has enough cards to claim
        else if (temp.color.equals("Pink")
        && players[playerIndex].numPink 
        + players[playerIndex].numWild   >= temp.size)
        {
            //ArrayList to hold available options
            ArrayList<String> options = new ArrayList<>();

            //if the player has enough pinks to claim, add that option
            if (players[playerIndex].numPink >= temp.size)
            {
                options.add("Pinks only");
            }

            //if the player has pinks and wilds with a sum >=
            //the size of the Route, add that option
            if ((players[playerIndex].numPink > 0 
            && players[playerIndex].numWild > 0)
            && (players[playerIndex].numPink 
            + players[playerIndex].numWild >= temp.size))
            {
                options.add("Mix pink and wilds");
            }

            //if the player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert the ArrayList into a String array
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the player's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null,
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //player chose to use all pinks
            if (finalOptions[choice].charAt(0) == 'P')
            {
                //reduce the player's amount of pinks by the size of the Route
                players[playerIndex].numPink -= temp.size;     

                //discard the amount of pink cards used
                for(int e = 0; e < temp.size; e++){
                    discards.add(new TrainCard("pink"));
                }
            }
            //player chose to use all wilds
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //reduce the player's amount of wilds by the size of the Route
                players[playerIndex].numWild -= temp.size;   

                //discard the amount of wild cards used
                for(int s = 0; s < temp.size; s++){
                    discards.add(new TrainCard("wild"));
                }
            }
            //player chose to mix pinks and wilds
            else
            {
                //counter to track the number of cards needed to claim
                int count = temp.size;

                //loops allows users to pick the number 
                //of pinks and wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if player has pinks, add that option
                    if (players[playerIndex].numPink > 0)
                    {
                        newOptions.add("Pink (x" 
                        + players[playerIndex].numPink + ")");
                    }

                    //if player has wilds, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList into a String array
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null, 
                        "Choose train card to use (" + count
                        + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chose to use a pink
                    if (selectOptions[choice].charAt(0) == 'P')
                    {
                        //decrement the player's amount of pinks
                        players[playerIndex].numPink -= 1;  

                        //discard a pink card                        
                        discards.add(new TrainCard("pink"));                        
                    }
                    //player chose to use a wild
                    else
                    {
                        //decrement the player's amount of wilds
                        players[playerIndex].numWild -= 1; 

                        //discard a wild card
                        discards.add(new TrainCard("wild"));
                    }

                    //decrement the counter
                    count--;
                }
            }

            //reduce the player's amount of tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //if the selected route is black and the player
        //has enough cards to claim
        else if (temp.color.equals("Black") && players[playerIndex].numBlack
        + players[playerIndex].numWild >= temp.size)
        {
            //ArrayList to hold available options
            ArrayList<String> options = new ArrayList<>();

            //if the player has enough black cards 
            //to claim, add that option
            if (players[playerIndex].numBlack >= temp.size)
            {
                options.add("Black cards only");
            }

            //if the player has blacks and wilds with a 
            //sum >= the size of the Route, add that option
            if ((players[playerIndex].numBlack > 0 
            && players[playerIndex].numWild > 0)
            && (players[playerIndex].numBlack 
            + players[playerIndex].numWild >= temp.size))
            {
                options.add("Mix black and wilds");
            }

            //if the player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert the ArrayList to a String array
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the player's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null, 
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //player chose to use a black card
            if (finalOptions[choice].charAt(0) == 'B')
            {
                //reduce the player's amount of black
                //cards by the size of the Route
                players[playerIndex].numBlack -= temp.size; 

                //discard the amount of black cards used
                for(int i = 0; i < temp.size; i++){
                    discards.add(new TrainCard("black"));
                }
            }
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //reduce the player's amount of wild cards
                //by the size of the Route
                players[playerIndex].numWild -= temp.size;   

                //discard the amount of wild cards used
                for(int i = 0; i < temp.size; i++){
                    discards.add(new TrainCard("wild"));
                }
            }
            //player chose to mix black cards and wilds
            else
            {
                //counter to track the amount of cards needed to claim
                int count = temp.size;

                //loop allows the player to 
                //choose to use a black or wild card
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold the available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if the player has black cards, add that option
                    if (players[playerIndex].numBlack > 0)
                    {
                        newOptions.add("Black (x" 
                        + players[playerIndex].numBlack + ")");
                    }

                    //if the player has wild cards, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x" 
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList into a String array
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null, 
                        "Choose train card to use (" + count
                        + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chose to use a black card
                    if (selectOptions[choice].charAt(0) == 'B')
                    {
                        //decrement the player's amount of black cards
                        players[playerIndex].numBlack -= 1;   

                        //discard a black card
                        discards.add(new TrainCard("black"));
                    }
                    //player chose to use a wild card
                    else
                    {
                        //decrement the player's amount of wild cards
                        players[playerIndex].numWild -= 1;  

                        //discards a wild card
                        discards.add(new TrainCard("wild"));
                    }

                    //decrement the counter
                    count--;
                }
            }

            //reduces the player's amount of tokens by the size of the Route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //if the selected Route is white and the player 
        //has enough cards to claim
        else if (temp.color.equals("White") && players[playerIndex].numWhite
        + players[playerIndex].numWild  >= temp.size)
        {
            //ArrayList to hold available options
            ArrayList<String> options = new ArrayList<>();

            //if the player has enough whites to claim, add that option
            if (players[playerIndex].numWhite >= temp.size)
            {
                options.add("White cards only");
            }

            //if the player has whites and wilds with 
            //a sum >= the size of the Route, add that option
            if ((players[playerIndex].numWhite > 0
            && players[playerIndex].numWild > 0)
            && (players[playerIndex].numWhite 
            + players[playerIndex].numWild >= temp.size))
            {
                options.add("Mix white and wilds");
            }

            //if the player has enough wilds to claim, add that option
            if (players[playerIndex].numWild >= temp.size)
            {
                options.add("Wilds only");
            }

            //convert the ArrayList to a String array
            String[] finalOptions = options.toArray(
            new String[options.size()]);

            //display the player's options
            int choice = 0;
            do
            {
                choice = JOptionPane.showOptionDialog(null, 
                "How would you like to claim the route? (" 
                + temp.size + " needed)", "Choose method", 
                    0, 0, null, finalOptions, 0);
            }
            while(choice == -1);

            //player chose to use all whites
            if (finalOptions[choice].charAt(1) == 'h')
            {
                //reduces the player's amount of whites
                //by the size of the Route
                players[playerIndex].numWhite -= temp.size;   

                //discard the amount of white cards used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("white"));
                }
            }
            //player chose to use all wilds
            else if (finalOptions[choice].charAt(0) == 'W')
            {
                //reduce the player's amount of wilds by the size of the Route
                players[playerIndex].numWild -= temp.size;    

                //discard the amount of wilds used
                for(int i = 0; i < temp.size; i++)
                {
                    discards.add(new TrainCard("wild"));
                }
            }
            //player chose to mix white cards and wilds
            else
            {
                //counter to track the amount of cards needed to claim
                int count = temp.size;

                //loop allows player to pick how many
                //whites and how many wilds to use
                for (int i = 0; i < temp.size; i++)
                {
                    //ArrayList to hold available options
                    ArrayList<String> newOptions = new ArrayList<>();

                    //if player has white cards, add that option
                    if (players[playerIndex].numWhite > 0)
                    {
                        newOptions.add("White (x"
                        + players[playerIndex].numWhite + ")");
                    }

                    //if player has wild cards, add that option
                    if (players[playerIndex].numWild > 0)
                    {
                        newOptions.add("Wild (x"
                        + players[playerIndex].numWild + ")");                        
                    }

                    //convert the ArrayList to a String array
                    String[] selectOptions = newOptions.toArray(
                    new String[newOptions.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null,
                        "Choose train card to use (" 
                        + count + " needed)", "Choose card", 
                            0, 0, null, selectOptions, 0);
                    }
                    while(choice == -1);

                    //player chose to use a white card
                    if (selectOptions[choice].charAt(1) == 'h')
                    {
                        //decrement the player's amount of white cards
                        players[playerIndex].numWhite -= 1;  

                        //dicard a white card
                        discards.add(new TrainCard("white"));
                    }
                    //player chose to use a wild card
                    else
                    {
                        //decrement the player's amount of wild cards
                        players[playerIndex].numWild -= 1;  

                        //discard a wild card
                        discards.add(new TrainCard("wild"));                        
                    }

                    //decrement the counter
                    count--;
                }
            }

            //reduce the player's amount of tokens by the size of the route
            players[playerIndex].tokens -= temp.size;

            return true;
        }
        //SPECIAL CASE:
        //lets the user choose what color cards they want to 
        //use to claim a gray route
        else if (temp.color.equals("Gray"))
        {
            //if player has enough of one color to claim a route
            if (players[playerIndex].numRed
            + players[playerIndex].numWild >= temp.size 
            || players[playerIndex].numGreen
            + players[playerIndex].numWild >= temp.size
            || players[playerIndex].numBlue
            + players[playerIndex].numWild >= temp.size
            || players[playerIndex].numYellow
            + players[playerIndex].numWild >= temp.size
            || players[playerIndex].numOrange
            + players[playerIndex].numWild >= temp.size
            || players[playerIndex].numPink
            + players[playerIndex].numWild >= temp.size
            || players[playerIndex].numBlack
            + players[playerIndex].numWild >= temp.size 
            || players[playerIndex].numWhite
            + players[playerIndex].numWild >= temp.size
            || players[playerIndex].numWild
            + players[playerIndex].numWild >= temp.size)
            {   
                //ArrayList to hold available options
                ArrayList<String> options = new ArrayList<>();

                //if player a color amount greater than zero, add that option
                if (players[playerIndex].numRed >= temp.size
                || players[playerIndex].numGreen >= temp.size
                || players[playerIndex].numBlue >= temp.size
                || players[playerIndex].numYellow >= temp.size
                || players[playerIndex].numOrange >= temp.size
                || players[playerIndex].numPink >= temp.size
                || players[playerIndex].numBlack >= temp.size 
                || players[playerIndex].numWhite >= temp.size)
                {
                    options.add("Use one color");
                }

                //if player has wild cards
                if (players[playerIndex].numWild > 0)
                {
                    //if player has an amount of any color
                    //and wild with a sum that's 
                    // >= the size of the Route, add that option
                    if (((players[playerIndex].numRed > 0)
                        && players[playerIndex].numRed 
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numGreen > 0)
                        && players[playerIndex].numGreen
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numBlue > 0) 
                        && players[playerIndex].numBlue 
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numYellow > 0)
                        && players[playerIndex].numYellow 
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numOrange > 0)
                        && players[playerIndex].numOrange
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numPink > 0)
                        && players[playerIndex].numPink 
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numBlack > 0)
                        && players[playerIndex].numBlack 
                        + players[playerIndex].numWild >= temp.size)
                    || ((players[playerIndex].numWhite > 0)
                        && players[playerIndex].numWhite 
                        + players[playerIndex].numWild >= temp.size))
                    {
                        options.add("Mix a color and wilds");
                    }
                }

                //if player has enough wilds to claim, add that option
                if (players[playerIndex].numWild >= temp.size)
                {
                    options.add("Wilds only");
                }

                //convert the ArrayList to a String array
                String[] selectOptions = options.toArray(
                new String[options.size()]);

                //display the player's options
                int choice = 0;
                do
                {
                    choice = JOptionPane.showOptionDialog(null, 
                    "How would you like to claim the route", 
                    "Choose method", 
                    0, 0, null, selectOptions, 0);
                }
                while(choice == -1);

                //player chose to use one color
                if (selectOptions[choice].charAt(0) == 'U')
                {
                    //counter to track the amount of cards needed to claim
                    int count = temp.size;

                    //temporary Player object
                    Player tempPlayer = players[playerIndex];

                    //adds an option to the ArrayList if
                    //the player has cards of that color
                    options = new ArrayList<>();
                    if (tempPlayer.numRed >= temp.size)
                    {
                        options.add("Red (x" + tempPlayer.numRed + ")");
                    }                    
                    if (tempPlayer.numGreen >= temp.size)
                    {
                        options.add("Green (x" + tempPlayer.numGreen + ")");
                    }
                    if (tempPlayer.numBlue >= temp.size)
                    {
                        options.add("Blue (x" + tempPlayer.numBlue + ")");
                    }
                    if (tempPlayer.numYellow >= temp.size)
                    {
                        options.add("Yellow (x" + tempPlayer.numYellow + ")");
                    }
                    if (tempPlayer.numOrange >= temp.size)
                    {
                        options.add("Orange (x" + tempPlayer.numOrange + ")");
                    }
                    if (tempPlayer.numPink >= temp.size)
                    {
                        options.add("Pink (x" + tempPlayer.numPink + ")");
                    }
                    if (tempPlayer.numBlack >= temp.size)
                    {
                        options.add("Black (x" + tempPlayer.numBlack + ")");
                    }
                    if (tempPlayer.numWhite >= temp.size)
                    {
                        options.add("White (x" + tempPlayer.numWhite + ")");
                    }

                    //converts the ArrayList to a String array
                    String[] finalOptions = options.toArray(
                    new String[options.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null, 
                        "Choose train card to use (" + count
                        + " needed)", "Choose card", 
                            0, 0, null, finalOptions, 0);
                    }
                    while(choice == -1);

                    //determines what the user chose using the first char of the String at the index of the user's choice
                    if (finalOptions[choice].charAt(0) == 'R')
                    {
                        //decrease the amount of the
                        //chosen color by the size of the Route
                        players[playerIndex].numRed -= temp.size; 

                        //discard the amount of cards used
                        for(int i = 0; i < temp.size; i++)
                        {
                            discards.add(new TrainCard("red"));
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'G')
                    {
                        //decrease the amount of the 
                        //chosen color by the size of the Route
                        players[playerIndex].numGreen -= temp.size; 

                        //discard the amount of cards used
                        for(int i = 0; i < temp.size; i++)
                        {
                            discards.add(new TrainCard("green"));
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'B')
                    {
                        //player chose blue
                        if (finalOptions[choice].charAt(2) == 'u')
                        {
                            //decrease the amount of the 
                            //chosen color by the size of the Route
                            players[playerIndex].numBlue -= temp.size; 

                            //discard the amount of cards used
                            for(int i = 0; i < temp.size; i++)
                            {
                                discards.add(new TrainCard("blue"));
                            }
                        }
                        //player chose black
                        else
                        {
                            //decrease the amount of the chosen 
                            //color by the size of the Route
                            players[playerIndex].numBlack -= temp.size;  

                            //discard the amount of cards used
                            for(int i = 0; i < temp.size; i++)
                            {
                                discards.add(new TrainCard("black"));
                            }
                        }                         
                    }
                    else if (finalOptions[choice].charAt(0) == 'Y')
                    {
                        //decrease the amount of the chosen 
                        //color by the size of the Route
                        players[playerIndex].numYellow -= temp.size; 

                        //discard the amount of cards used
                        for(int i = 0; i < temp.size; i++)
                        {
                            discards.add(new TrainCard("yellow"));
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'O')
                    {
                        //decrease the amount of the chosen
                        //color by the size of the Route
                        players[playerIndex].numOrange -= temp.size;

                        //discard the amount of cards used
                        for(int i = 0; i < temp.size; i++)
                        {
                            discards.add(new TrainCard("orange"));
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'P')
                    {
                        //decrease the amount of the chosen
                        //color by the size of the Route
                        players[playerIndex].numPink -= temp.size; 

                        //discard the amount of cards used
                        for(int i = 0; i < temp.size; i++)
                        {
                            discards.add(new TrainCard("pink"));
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'W')
                    {
                        //decrease the amount of the chosen 
                        //color by the size of the Route
                        players[playerIndex].numWhite -= temp.size; 

                        //discard the amount of cards used
                        for(int i = 0; i < temp.size; i++)
                        {
                            discards.add(new TrainCard("white"));
                        }
                    }

                    //decrease the amount of player's 
                    //tokens by the size of the Route
                    players[playerIndex].tokens -= temp.size;

                    return true;
                }
                //player chose to use all wilds
                else if (selectOptions[choice].charAt(0) == 'W')
                {
                    ////decrease the amount of the chosen color
                    //by the size of the Route
                    players[playerIndex].numWild -= temp.size; 

                    //discard the amount of cards used
                    for(int i = 0; i < temp.size; i++){
                        discards.add(new TrainCard("wild"));
                    }
                }
                //player chose to mix wilds and colors
                else
                {
                    //temporary Player object
                    Player tempPlayer = players[playerIndex];

                    //adds an option to the ArrayList if 
                    //the player has cards of that color
                    options = new ArrayList<>();
                    if ((tempPlayer.numRed > 0) && tempPlayer.numRed
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Red (x" + tempPlayer.numRed + ")");
                    }                    
                    if ((tempPlayer.numGreen > 0) && tempPlayer.numGreen
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Green (x" + tempPlayer.numGreen + ")");
                    }
                    if ((tempPlayer.numBlue > 0) && tempPlayer.numBlue
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Blue (x" + tempPlayer.numBlue + ")");
                    }
                    if ((tempPlayer.numYellow > 0) && tempPlayer.numYellow
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Yellow (x" + tempPlayer.numYellow + ")");
                    }
                    if ((tempPlayer.numOrange > 0) && tempPlayer.numOrange
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Orange (x" + tempPlayer.numOrange + ")");
                    }
                    if ((tempPlayer.numPink > 0) && tempPlayer.numPink
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Pink (x" + tempPlayer.numPink + ")");
                    }
                    if ((tempPlayer.numBlack > 0) && tempPlayer.numBlack
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("Black (x" + tempPlayer.numBlack + ")");
                    }
                    if ((tempPlayer.numWhite > 0) && tempPlayer.numWhite
                    + tempPlayer.numWild >= temp.size)
                    {
                        options.add("White (x" + tempPlayer.numWhite + ")");
                    }

                    //converts the ArrayList to a String array
                    String[] finalOptions = options.toArray(
                    new String[options.size()]);

                    //display the player's options
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null, 
                        "Choose color to use", "Choose card", 
                            0, 0, null, finalOptions, 0);
                    }
                    while(choice == -1);

                    //counter to track the number of cards needed to claim
                    int count = temp.size;

                    //determines what the user chose using the first
                    //char of the String at the index of the user's choice
                    if (finalOptions[choice].charAt(0) == 'R')
                    {
                        //loop allows the player to pick
                        //to use a color or wild for each part of the Route
                        for (int i = 0; i < temp.size; i++)
                        {
                            //ArrayList to hold available options
                            options = new ArrayList<>();
                            if (tempPlayer.numRed > 0)
                            {
                                options.add("Red (x" 
                                + tempPlayer.numRed + ")");
                            }  
                            if (tempPlayer.numWild > 0)
                            {
                                options.add("Wild (x" 
                                + tempPlayer.numWild + ")");
                            }  

                            //convert the ArrayList to a String array
                            finalOptions = options.toArray(
                            new String[options.size()]);

                            //display the player's options
                            do
                            {
                                choice = JOptionPane.showOptionDialog(null,
                                "Choose train card to use (" + count
                                + " needed)", "Choose card", 
                                    0, 0, null, finalOptions, 0);
                            }
                            while(choice == -1);

                            //player chose red   
                            if (finalOptions[choice].charAt(0) == 'R')
                            {
                                //decrement the player's amount of reds
                                players[playerIndex].numRed -= 1; 

                                //discard a red
                                discards.add(new TrainCard("red"));                                
                            }
                            //player chose wild
                            else
                            {
                                //decrement the player's amount of reds
                                players[playerIndex].numWild -= 1; 

                                //discard a wild
                                discards.add(new TrainCard("wild"));
                            }

                            //decrement counter
                            count--;
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'G')
                    {
                        //ArrayList to hold available options
                        options = new ArrayList<>();

                        //loop allows the player to pick to 
                        //use a color or wild for each part of the Route
                        for (int i = 0; i < temp.size; i++)
                        {
                            //if player has greens, add that option
                            if (tempPlayer.numGreen > 0)
                            {
                                options.add("Green (x" 
                                + tempPlayer.numGreen + ")");
                            }  

                            //if player has wilds, add that option
                            if (tempPlayer.numWild > 0)
                            {
                                options.add("Wild (x" 
                                + tempPlayer.numWild + ")");
                            }  

                            //convertArrayList into a String array
                            finalOptions = options.toArray(
                            new String[options.size()]);

                            //display player's options
                            do
                            {
                                choice = JOptionPane.showOptionDialog(null,
                                "Choose train card to use (" 
                                + count + " needed)", "Choose card", 
                                    0, 0, null, finalOptions, 0);
                            }
                            while(choice == -1);

                            //player chose green
                            if (finalOptions[choice].charAt(0) == 'G')
                            {
                                //decrement the player's amount of greens 
                                players[playerIndex].numGreen -= 1; 

                                //discard a green
                                discards.add(new TrainCard("green"));
                            }
                            //player chose wild
                            else
                            {
                                //decrement the player's amount of greens 
                                players[playerIndex].numWild -= 1;

                                //discard a wild
                                discards.add(new TrainCard("wild"));                                
                            }

                            //decrement the counter
                            count--;
                        }
                    }
                    else if (finalOptions[choice].charAt(0) == 'B')
                    {
                        //player chose blue
                        if (finalOptions[choice].charAt(2) == 'u')
                        {
                            //loop allows the player to choose
                            //the amount of blues and wilds to use
                            for (int i = 0; i < temp.size; i++)
                            {
                                //ArrayList to hold available options
                                options = new ArrayList<>();

                                //if player has blues, add that option
                                if (tempPlayer.numBlue > 0)
                                {
                                    options.add("Blue (x" 
                                    + tempPlayer.numBlue + ")");
                                }  

                                //if player had wilds, add that option
                                if (tempPlayer.numWild > 0)
                                {
                                    options.add("Wild (x" 
                                    + tempPlayer.numWild + ")");
                                }  

                                //convert the ArrayList into a String array
                                finalOptions = options.toArray(
                                new String[options.size()]);

                                //display the player's options
                                do
                                {
                                    choice = JOptionPane.showOptionDialog(null,
                                    "Choose train card to use (" + count 
                                    + " needed)", "Choose card", 
                                        0, 0, null, finalOptions, 0);
                                }
                                while(choice == -1);

                                //player chose blue
                                if (finalOptions[choice].charAt(0) == 'B')
                                {
                                    //decrement the player's amount of blues
                                    players[playerIndex].numBlue -= 1; 

                                    //discard a blue
                                    discards.add(new TrainCard("blue"));                                    
                                }
                                //player chose wild
                                else
                                {
                                    //decrement the player's amount of wilds
                                    players[playerIndex].numWild -= 1; 

                                    //discard a wild
                                    discards.add(new TrainCard("wild"));
                                }

                                //decrement the counter
                                count--;
                            }
                        }
                        //player chose black
                        else
                        {       
                            //loop lets the player choose 
                            //how many blacks and wilds to use
                            for (int i = 0; i < temp.size; i++)
                            {
                                //ArrayList to hold available options
                                options = new ArrayList<>();

                                //if player has blacks, add that option
                                if (tempPlayer.numBlack > 0)
                                {
                                    options.add("Black (x" 
                                    + tempPlayer.numBlack + ")");
                                }  

                                //if player has wilds, add that option
                                if (tempPlayer.numWild > 0)
                                {
                                    options.add("Wild (x" 
                                    + tempPlayer.numWild + ")");
                                }  

                                //convert the ArrayList into a String array
                                finalOptions = options.toArray(
                                new String[options.size()]);

                                //display the player's options
                                do
                                {
                                    choice = JOptionPane.showOptionDialog(null,
                                    "Choose train card to use (" + count 
                                    + " needed)", "Choose card", 
                                        0, 0, null, finalOptions, 0);
                                }
                                while(choice == -1);

                                //player chose black
                                if (finalOptions[choice].charAt(0) == 'B')
                                {
                                    //decrement the player's amount of blacks
                                    players[playerIndex].numBlack -= 1; 

                                    //discard a black card
                                    discards.add(new TrainCard("black"));                                    
                                }
                                //player chose wild
                                else
                                {
                                    //decrement the player's amount of wilds
                                    players[playerIndex].numWild -= 1; 

                                    //discard a wild card
                                    discards.add(new TrainCard("wild"));
                                }

                                //decrement the counter
                                count--;
                            }
                        }                         
                    }
                    //player chose yellow
                    else if (finalOptions[choice].charAt(0) == 'Y')
                    {
                        //loop allows the player to choose 
                        //how many yellows and wilds to use
                        for (int i = 0; i < temp.size; i++)
                        {
                            //ArrayList to hold available options
                            options = new ArrayList<>();

                            //if player has yellows, add that option
                            if (tempPlayer.numYellow > 0)
                            {
                                options.add("Yellow (x"
                                + tempPlayer.numYellow + ")");
                            }  

                            //if player has wilds, add that option
                            if (tempPlayer.numWild > 0)
                            {
                                options.add("Wild (x" 
                                + tempPlayer.numWild + ")");
                            }  

                            //convert the ArrayList into a String array
                            finalOptions = options.toArray(
                            new String[options.size()]);

                            //display the player's options
                            do
                            {
                                choice = JOptionPane.showOptionDialog(null,
                                "Choose train card to use (" 
                                + count + " needed)", "Choose card", 
                                    0, 0, null, finalOptions, 0);
                            }
                            while(choice == -1);

                            //player chose yellow
                            if (finalOptions[choice].charAt(0) == 'Y')
                            {
                                //decrement the player's amount of yellows
                                players[playerIndex].numYellow -= 1; 

                                //discard a yellow                                
                                discards.add(new TrainCard("yellow"));
                            }
                            //player chose wild
                            else
                            {
                                //decrement the player's amount of wilds
                                players[playerIndex].numWild -= 1;

                                //discard a wild
                                discards.add(new TrainCard("wild"));
                            }

                            //decrement the counter
                            count--;
                        }
                    }
                    //player chose orange
                    else if (finalOptions[choice].charAt(0) == 'O')
                    {
                        //loop allows the player to choose 
                        //how many oranges and wilds to use
                        for (int i = 0; i < temp.size; i++)
                        {
                            //ArrayList to hold available options
                            options = new ArrayList<>();

                            //if player has oranges, add that options
                            if (tempPlayer.numOrange > 0)
                            {
                                options.add("Orange (x"
                                + tempPlayer.numOrange + ")");
                            }  

                            //if player has wilds, add that option
                            if (tempPlayer.numWild > 0)
                            {
                                options.add("Wild (x" 
                                + tempPlayer.numWild + ")");
                            }  

                            //convert the ArrayList to a String array
                            finalOptions = options.toArray(
                            new String[options.size()]);

                            //display the player's options
                            do
                            {
                                choice = JOptionPane.showOptionDialog(null,
                                "Choose train card to use (" + count 
                                + " needed)", "Choose card", 
                                    0, 0, null, finalOptions, 0);
                            }
                            while(choice == -1);

                            //player chose orange
                            if (finalOptions[choice].charAt(0) == 'O')
                            {
                                //decrement the player's amount of oranges
                                players[playerIndex].numOrange -= 1; 

                                //discard an orange                                
                                discards.add(new TrainCard("orange"));
                            }
                            //player chose wild
                            else
                            {
                                //decrement the player's amount of oranges
                                players[playerIndex].numWild -= 1; 

                                //discard a wild
                                discards.add(new TrainCard("wild"));
                            }

                            //decrement the counter
                            count--;
                        }
                    }
                    //player chose pink
                    else if (finalOptions[choice].charAt(0) == 'P')
                    {
                        //loop allows the player to choose 
                        //how many pinks and wilds to use
                        for (int i = 0; i < temp.size; i++)
                        {
                            //ArrayList to hold available options
                            options = new ArrayList<>();

                            //if the player has pinks, add that option
                            if (tempPlayer.numPink > 0)
                            {
                                options.add("Pink (x" 
                                + tempPlayer.numPink + ")");
                            }  
 
                            //if the player has wilds, add that opion
                            if (tempPlayer.numWild > 0)
                            {
                                options.add("Wild (x" 
                                + tempPlayer.numWild + ")");
                            }  

                            //convert the ArrayList to a String araray
                            finalOptions = options.toArray(
                            new String[options.size()]);

                            //display the player's options
                            do
                            {
                                choice = JOptionPane.showOptionDialog(null,
                                "Choose train card to use (" + count
                                + " needed)", "Choose card", 
                                    0, 0, null, finalOptions, 0);
                            }
                            while(choice == -1);

                            //player chose pink
                            if (finalOptions[choice].charAt(0) == 'P')
                            {
                                //decrement the player's amount of pinks
                                players[playerIndex].numPink -= 1; 

                                //discard a pink
                                discards.add(new TrainCard("pink"));
                            }
                            else
                            {
                                //decrement the player's amount of wilds
                                players[playerIndex].numWild -= 1; 

                                //discard a wild
                                discards.add(new TrainCard("wild"));
                            }

                            //decrement the counter
                            count--;
                        }
                    }
                    //player chose white
                    else if (finalOptions[choice].charAt(0) == 'W')
                    {
                        //loop allows the player to choose 
                        //how many whites and wilds to use
                        for (int i = 0; i < temp.size; i++)
                        {
                            //ArrayList to hold available options
                            options = new ArrayList<>();

                            //if the player has whites, add that options
                            if (tempPlayer.numWhite > 0)
                            {
                                options.add("White (x" 
                                + tempPlayer.numWhite + ")");
                            }  
                            
                            //if the player has wilds, add that options
                            if (tempPlayer.numWild > 0)
                            {
                                options.add("Wild (x"
                                + tempPlayer.numWild + ")");
                            }  

                            //convert the ArrayList to a String array
                            finalOptions = options.toArray(
                            new String[options.size()]);

                            //display the player's options
                            do
                            {
                                choice = JOptionPane.showOptionDialog(null,
                                "Choose train card to use (" + count
                                + " needed)",
                                "Choose card", 
                                0, 0, null, finalOptions, 0);
                            }
                            while(choice == -1);

                            //player chose white
                            if (finalOptions[choice].charAt(0) == 'W')
                            {
                                //decrement the player's amount of whites
                                players[playerIndex].numWhite -= 1; 

                                //discard a white
                                discards.add(new TrainCard("white"));
                            }
                            //player chose wild
                            else
                            {                                  
                                //decrement the player's amount of wilds
                                players[playerIndex].numWild -= 1; 

                                //discard a wild
                                discards.add(new TrainCard("wild"));
                            }

                            //decrement the counter
                            count--;
                        }
                    }
                }

                //decrease the player's amount of tokens 
                //by the size of the Route
                players[playerIndex].tokens -= temp.size;

                return true;
            }
        }
        //SPECIAL CASE:
        //two ferry routes on the board requre the use of two
        //wild card, with the third and final
        //piece of the route being gray, so we need to prompt
        //the user for their choice of color
        //to use for that third piece (these two routes
        //are located at index 0 and 1 in the routes array) 
        else if (temp.color.equals("Ferry"))
        {           
            //checks if the selected Route is one of the two size 3 ferries
            //and if the player has enough wild cards to claim
            if ((routeIndex == 0 || routeIndex == 1)
            && players[playerIndex].numWild >= 2)
            {
                //checks if the player has a colored card
                //to claim the third piece of the Route
                if ((players[playerIndex].numRed 
                + players[playerIndex].numGreen
                    + players[playerIndex].numBlue
                    + players[playerIndex].numYellow
                    + players[playerIndex].numOrange
                    + players[playerIndex].numPink
                    + players[playerIndex].numBlack
                    + players[playerIndex].numWhite) >= 1)
                {
                    //temporary Player object
                    Player tempPlayer = players[playerIndex];

                    //adds an option to the ArrayList if 
                    //the player has cards of that color
                    ArrayList<String> options = new ArrayList<>();
                    if (tempPlayer.numRed > 0)
                    {
                        options.add("Red (x" + tempPlayer.numRed + ")");
                    }                    
                    if (tempPlayer.numGreen > 0)
                    {
                        options.add("Green (x" + tempPlayer.numGreen + ")");
                    }
                    if (tempPlayer.numBlue > 0)
                    {
                        options.add("Blue (x" + tempPlayer.numBlue + ")");
                    }
                    if (tempPlayer.numYellow > 0)
                    {
                        options.add("Yellow (x" + tempPlayer.numYellow + ")");
                    }
                    if (tempPlayer.numOrange > 0)
                    {
                        options.add("Orange (x" + tempPlayer.numOrange + ")");
                    }
                    if (tempPlayer.numPink > 0)
                    {
                        options.add("Pink (x" + tempPlayer.numPink + ")");
                    }
                    if (tempPlayer.numBlack > 0)
                    {                      
                        options.add("Black (x" + tempPlayer.numBlack + ")");
                    }
                    if (tempPlayer.numWhite > 0)
                    {
                        options.add("White (x" + tempPlayer.numWhite + ")");
                    }

                    //converts the ArrayList to a String array
                    String[] finalOptions = options.toArray(
                    new String[options.size()]);

                    //removes two wild cards from the player's hand
                    players[playerIndex].numWild -= 2;

                    //prompts the user
                    int choice = 0;
                    do
                    {
                        choice = JOptionPane.showOptionDialog(null,
                            "Choose train card to use to "
                            + "claim the single grey space",
                            "Choose card", 
                            0, 0, null, finalOptions, 0);
                    }
                    while(choice == -1);

                    //determines what the user chose using the first
                    //char of the String at the index of the user's choice
                    if (finalOptions[choice].charAt(0) == 'R')
                    {
                        //decrement the player's amount of reds
                        players[playerIndex].numRed -= 1; 

                        //discard a red
                        discards.add(new TrainCard("red"));
                    }
                    else if (finalOptions[choice].charAt(0) == 'G')
                    {
                        //decrement the player's amount of greens
                        players[playerIndex].numGreen -= 1; 

                        //discard a green
                        discards.add(new TrainCard("green"));
                    }
                    else if (finalOptions[choice].charAt(0) == 'B')
                    {
                        if (finalOptions[choice].charAt(2) == 'u')
                        {
                            //decrement the player's amount of blues
                            players[playerIndex].numBlue -= 1; 

                            //discard a blue
                            discards.add(new TrainCard("blue"));
                        }
                        else
                        {
                            //decrement the player's number of blacks
                            players[playerIndex].numBlack -= 1;   

                            //discard a black
                            discards.add(new TrainCard("black"));
                        }                         
                    }
                    else if (finalOptions[choice].charAt(0) == 'Y')
                    {
                        //decrement the player's amount of yellows
                        players[playerIndex].numYellow -= 1; 

                        //discard a yellow
                        discards.add(new TrainCard("yellow"));
                    }
                    else if (finalOptions[choice].charAt(0) == 'O')
                    {
                        //decrement the player's amount of oranges
                        players[playerIndex].numOrange -= 1; 

                        //discard an orange
                        discards.add(new TrainCard("orange"));
                    }
                    else if (finalOptions[choice].charAt(0) == 'P')
                    {
                        //decrement the player's number of pinks
                        players[playerIndex].numPink -= 1; 

                        //discard a pink 
                        discards.add(new TrainCard("pink"));
                    }
                    else if (finalOptions[choice].charAt(0) == 'W')
                    {
                        //decrement the player's amount of whites
                        players[playerIndex].numWhite -= 1; 

                        //discard a white
                        discards.add(new TrainCard("white"));
                    }

                    //reduces the player's amount of tokens 
                    //by the size of the Route
                    players[playerIndex].tokens -= temp.size;

                    return true;
                }

                return false;
            }
            //checks if the route is one of the two size 1 ferries
            else if ((routeIndex == 2 || routeIndex == 3)
            && players[playerIndex].numWild >= 1)
            {
                //decrement the amount of wilds
                players[playerIndex].numWild -= 1;

                //discard a wild
                discards.add(new TrainCard("wild"));

                return true;
            }

            return false;
        }

        return false;
    }

    /**
     * Constructor for objects of type Board
     */
    public Board()
    {        
        //attempt to get our board image
        try
        {
            image = ImageIO.read(getClass().getResourceAsStream(
            "./board_test.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        //create a Polygon object for each route on the board
        //and initialize our routes array
        //set any adjacent routes equal to each other
        //set up any stock on routes if the Route has stock
        int[] x92 = {103, 97, 60, 66};      
        int[] y92 = {121, 125, 65, 62};
        Polygon test92 = new Polygon(x92,y92,x92.length);
        routes[0] = new Route("Ferry", "Ontario", 
        "Erie", true, 3, null, test92);
        routes[0].stock9 = true;
        routes[0].stock7 = true;
        routes[0].stock4 = true;

        int[] x93 = {95, 89, 52, 58};      
        int[] y93 = {126, 130, 70, 66};
        Polygon test93 = new Polygon(x93,y93,x93.length);
        routes[1] = new Route("Ferry", "Ontario",
        "Erie", true, 3, null, test93);
        routes[1].stock9 = true;
        routes[1].stock7 = true;
        routes[1].stock4 = true;

        routes[0].adjacentRoute = routes[1];
        routes[1].adjacentRoute = routes[0];

        int[] x94= {210, 211, 189, 189};      
        int[] y94 = {50, 56, 56, 49};
        Polygon test94 = new Polygon(x94,y94,x94.length);
        routes[2] = new Route("Ferry", "Ontario", 
        "Buffalo", true, 1, null, test94);
        routes[2].stock9 = true;
        routes[2].stock7 = true;
        routes[2].stock4 = true;

        int[] x95 = {210, 210, 189, 189};      
        int[] y95 = {59, 65, 66, 59};
        Polygon test95 = new Polygon(x95,y95,x95.length);
        routes[3] = new Route("Ferry", "Ontario", 
        "Buffalo", true, 1, null, test95);
        routes[3].stock9 = true;
        routes[3].stock7 = true;
        routes[3].stock4 = true;

        routes[2].adjacentRoute = routes[3];
        routes[3].adjacentRoute = routes[2];

        int[] x = {248, 369, 369, 248};
        int[] y = {51, 51, 59, 59};
        Polygon test = new Polygon(x, y, x.length);
        routes[4] = new Route("Yellow", "Buffalo",
        "Rochester", true, 5, null, test);
        routes[4].stock8 = true;
        routes[4].stock7 = true;
        routes[4].stock5 = true;
        routes[4].stock4 = true;
        routes[4].stock1 = true;

        int[] x2 = {248, 369, 369, 248};
        int[] y2 = {59, 60, 67, 68};
        Polygon test2 = new Polygon(x2, y2, x2.length);
        routes[5] = new Route("Black", "Buffalo", 
        "Rochester", true, 5, null, test2);
        routes[5].stock8 = true;
        routes[5].stock7 = true;
        routes[5].stock5 = true;
        routes[5].stock4 = true;
        routes[5].stock1 = true;

        routes[4].adjacentRoute = routes[5];
        routes[5].adjacentRoute = routes[4];

        int[] x3 = {376, 371, 372, 381, 384, 403, 
            408, 389, 388, 379, 379, 384};
        int[] y3 = {66, 88, 94, 116, 120, 133, 127,
            115, 112, 92, 89, 68};
        Polygon test3 = new Polygon(x3,y3,x3.length);
        routes[6] = new Route("Green", "Rochester",
        "Elmira", true, 3, null, test3);
        routes[6].stock9 = true;
        routes[6].stock7 = true;
        routes[6].stock8 = true;
        routes[6].stock5 = true;
        routes[6].stock4 = true;
        routes[6].stock1 = true;

        int[] x4 = {393, 490, 490, 393};
        int[] y4 = {51, 51, 59, 59};
        Polygon test4 = new Polygon(x4,y4,x4.length);
        routes[7] = new Route("Pink", "Rochester", 
        "Syracuse", true, 4, null, test4);
        routes[7].stock5 = true;
        routes[7].stock4 = true;

        int[] x5 = {393, 490, 490, 393};
        int[] y5 = {60, 60, 67, 67};
        Polygon test5 = new Polygon(x5,y5,x5.length);
        routes[8] = new Route("Blue", "Rochester", 
        "Syracuse", true, 4, null, test5);
        routes[8].stock5 = true;
        routes[8].stock4 = true;

        routes[7].adjacentRoute = routes[8];
        routes[8].adjacentRoute = routes[7];

        int[] x6 = {514, 659, 659, 514};
        int[] y6 = {50, 64, 72, 58};
        Polygon test6 = new Polygon(x6,y6,x6.length);
        routes[9] = new Route("White", "Syracuse",
        "Albany", true, 6, null, test6);
        routes[9].stock4 = true;

        int[] x7 = {514, 658, 658, 514};
        int[] y7 = {59, 74, 81, 66};
        Polygon test7 = new Polygon(x7,y7,x7.length);
        routes[10] = new Route("Red", "Syracuse",
        "Albany", true, 6, null, test7);
        routes[10].stock4 = true;

        routes[9].adjacentRoute = routes[10];
        routes[10].adjacentRoute = routes[9];

        int[] x8 = {680, 680, 673, 673};
        int[] y8 = {94, 240, 240, 94};
        Polygon test8 = new Polygon(x8,y8,x8.length);
        routes[11] = new Route("Green", "Albany",
        "New York", true, 6, null, test8);
        routes[11].stock9 = true;
        routes[11].stock8 = true;
        routes[11].stock4 = true;

        int[] x9 = {671, 671, 664, 664};
        int[] y9 = {94, 240, 240, 94};
        Polygon test9 = new Polygon(x9,y9,x9.length);
        routes[12] = new Route("Blue", "Albany", 
        "New York", true, 6, null, test9);
        routes[12].stock9 = true;
        routes[12].stock8 = true;
        routes[12].stock4 = true;

        routes[11].adjacentRoute = routes[12];
        routes[12].adjacentRoute = routes[11];

        int[] x10 = {680, 680, 673, 673};
        int[] y10 = {266, 411, 411, 266};
        Polygon test10 = new Polygon(x10,y10,x10.length);
        routes[13] = new Route("White", "New York", 
        "Atlantic City", true, 6, null, test10);
        routes[13].stock2 = true;

        int[] x11 = {671, 671, 664, 665};
        int[] y11 = {265, 411, 414, 266};
        Polygon test11 = new Polygon(x11,y11,x11.length);
        routes[14] = new Route("Black", "New York", 
        "Atlantic City", true, 6, null, test11);
        routes[14].stock2 = true;

        routes[13].adjacentRoute = routes[14];
        routes[14].adjacentRoute = routes[13];

        int[] x12 = {657, 657, 610, 610};
        int[] y12 = {414, 421, 415, 409};
        Polygon test12 = new Polygon(x12,y12,x12.length);
        routes[15] = new Route("Gray", "Philadelphia", 
        "Atlantic City", true, 2, null, test12);
        routes[15].stock9 = true;
        routes[15].stock2 = true;
        routes[15].stock6 = true;

        int[] x13 = {656, 656, 609, 609};
        int[] y13 = {423, 430, 425, 417};
        Polygon test13 = new Polygon(x13,y13,x13.length);
        routes[16] = new Route("Gray", "Philadelphia", 
        "Atlantic City", true, 2, null, test13);
        routes[16].stock9 = true;
        routes[16].stock2 = true;
        routes[16].stock6 = true;

        routes[15].adjacentRoute = routes[16];
        routes[16].adjacentRoute = routes[15];

        int[] x14 = {661, 612, 604, 654};
        int[] y14 = {265, 402, 399, 262};
        Polygon test14 = new Polygon(x14,y14,x14.length);
        routes[17] = new Route("Gray", "New York", 
        "Philadelphia", true, 6, null, test14);
        routes[17].stock9 = true;
        routes[17].stock8 = true;
        routes[17].stock6 = true;
        routes[17].stock2 = true;

        int[] x15 = {652, 603, 596, 645};
        int[] y15 = {262, 398, 396, 259};
        Polygon test15 = new Polygon(x15,y15,x15.length);
        routes[18] = new Route("Gray", "New York",
        "Philadelphia", true, 6, null, test15);
        routes[18].stock9 = true;
        routes[18].stock8 = true;
        routes[18].stock6 = true;
        routes[18].stock2 = true;

        routes[17].adjacentRoute = routes[18];
        routes[18].adjacentRoute = routes[17];

        int[] x16 = {592, 586, 554, 559};
        int[] y16 = {396, 398, 335, 331};
        Polygon test16 = new Polygon(x16,y16,x16.length);
        routes[19] = new Route("Black", "Allentown",
        "Philadelphia", true, 3, null, test16);
        routes[19].stock9 = true;
        routes[19].stock6 = true;

        int[] x17 = {584, 578, 545, 551};
        int[] y17 = {400, 403, 339, 336};
        Polygon test17 = new Polygon(x17,y17,x17.length);
        routes[20] = new Route("Red", "Allentown", 
        "Philadelphia", true, 3, null, test17);
        routes[20].stock9 = true;
        routes[20].stock6 = true;

        routes[19].adjacentRoute = routes[20];
        routes[20].adjacentRoute = routes[19];

        int[] x18 = {577, 554, 552, 529, 527, 504, 502, 480,
            482, 503, 506, 527, 530, 551, 555,  577};      
        int[] y18 = {413, 414, 414, 413, 412, 408, 407, 400, 
            393, 400, 401, 405, 406, 407, 407, 406};
        Polygon test18 = new Polygon(x18,y18,x18.length);
        routes[21] = new Route("Green", "Lancaster", 
        "Philadelphia", true, 4, null, test18);
        routes[21].stock9 = true;

        int[] x19 = {576, 576, 553, 551, 528, 525, 503, 501,
            479, 481, 502, 505, 526, 529, 551, 553, 576};      
        int[] y19 = {416, 423, 423, 423, 422, 421, 417, 416,
            409, 403, 409, 410, 414, 415, 416, 416, 415};
        Polygon test19 = new Polygon(x19,y19,x19.length);
        routes[22] = new Route("Orange", "Lancaster",
        "Philadelphia", true, 4, null, test19);
        routes[22].stock9 = true;

        routes[21].adjacentRoute = routes[22];
        routes[22].adjacentRoute = routes[21];

        int[] x20 = {578, 582, 561, 558, 537, 534, 512, 
            510, 487, 486, 509, 512, 533, 536, 556, 559, 578};      
        int[] y20 = {424, 430, 440, 441, 448, 449, 453,
            454, 456, 448, 446, 446, 442, 441, 434, 433, 423};
        Polygon test20 = new Polygon(x20,y20,x20.length);
        routes[23] = new Route("Yellow", "Philadelphia",
        "Baltimore", true, 4, null, test20);
        routes[23].stock9 = true;
        routes[23].stock8 = true;

        int[] x21 = {584, 563, 561, 539, 537, 514, 511, 489, 
            489, 512, 514, 535, 538, 558, 562, 581};      
        int[] y21 = {438, 449, 450, 457, 457, 462, 463, 464,
            456, 455, 455, 450, 449, 442, 441, 432};
        Polygon test21 = new Polygon(x21,y21,x21.length);
        routes[24] = new Route("Pink", "Philadelphia", 
        "Baltimore", true, 4, null, test21);
        routes[24].stock9 = true;
        routes[24].stock8 = true;

        routes[23].adjacentRoute = routes[24];
        routes[24].adjacentRoute = routes[23];

        int[] x22 = {664, 643, 641, 619, 616, 594, 592, 569, 567,
            544, 544, 567, 570, 592, 595, 618, 621, 643, 645, 667};      
        int[] y22 = {250, 243, 242, 236, 236, 232, 231, 230, 230,
            230, 222, 222, 222, 224, 225, 229, 230, 236, 236, 244};
        Polygon test22 = new Polygon(x22,y22,x22.length);
        routes[25] = new Route("Red", "Scranton/Wilkes Barre",
        "New York", true, 5, null, test22);
        routes[25].stock7 = true;
        routes[25].stock5 = true;
        routes[25].stock2 = true;

        int[] x23 = {662, 641, 638, 617, 615, 593, 591, 569, 567, 543,
            544, 567, 569, 591, 594, 616, 618, 640, 643, 664};      
        int[] y23 = {259, 252, 251, 245, 245, 241, 241, 239, 239, 239,
            231, 231, 232, 233, 234, 238, 238, 244, 245, 252};
        Polygon test23 = new Polygon(x23,y23,x23.length);
        routes[26] = new Route("Pink", "Scranton/Wilkes Barre",
        "New York", true, 5, null, test23);
        routes[26].stock7 = true;
        routes[26].stock5 = true;
        routes[26].stock2 = true;

        routes[25].adjacentRoute = routes[26];
        routes[26].adjacentRoute = routes[25];

        int[] x24 = {589, 584, 547, 552};      
        int[] y24 = {271, 277, 247, 241};
        Polygon test24 = new Polygon(x24,y24,x24.length);
        routes[27] = new Route("Yellow", "Scranton/Wilkes Barre",
        "Stroudsburg", true, 2, null, test24);
        routes[27].stock6 = true;
        routes[27].stock5 = true;
        routes[27].stock2 = true;

        int[] x25 = {588, 593, 559, 554};      
        int[] y25 = {286, 291, 324, 319};
        Polygon test25 = new Polygon(x25,y25,x25.length);
        routes[28] = new Route("Orange", "Stroudsburg", 
        "Allentown", true, 2, null, test25);
        routes[28].stock6 = true;
        routes[28].stock5 = true;
        routes[28].stock2 = true;

        int[] x26 = {542, 541, 541, 543, 544, 550, 543,
            536, 536, 534, 534, 535};      
        int[] y26 = {243, 266, 269, 290, 293, 314, 316,
            294, 291, 269, 266, 243};
        Polygon test26 = new Polygon(x26,y26,x26.length);
        routes[29] = new Route("Blue", "Scranton/Wilkes Barre",
        "Allentown", true, 3, null, test26);
        routes[29].stock9 = true;
        routes[29].stock6 = true;
        routes[29].stock5 = true;
        routes[29].stock2 = true;

        int[] x27 = {533, 532, 532, 534, 535, 541, 534,
            527, 527, 525, 525, 525};      
        int[] y27 = {244, 267, 269, 292, 294, 316, 318,
            295, 293, 269, 267, 244};
        Polygon test27 = new Polygon(x27,y27,x27.length);
        routes[30] = new Route("White", "Scranton/Wilkes Barre",
        "Allentown", true, 3, null, test27);
        routes[30].stock9 = true;
        routes[30].stock6 = true;
        routes[30].stock5 = true;
        routes[30].stock2 = true;

        routes[29].adjacentRoute = routes[30];
        routes[30].adjacentRoute = routes[29];

        int[] x28 = {539, 494, 491, 535};      
        int[] y28 = {335, 352, 344, 327};
        Polygon test28 = new Polygon(x28,y28,x28.length);
        routes[31] = new Route("Green", "Allentown",
        "Reading", true, 2, null, test28);
        routes[31].stock6 = true;

        int[] x29 = {666, 530, 527, 663};      
        int[] y29 = {89, 139, 132, 82};
        Polygon test29 = new Polygon(x29,y29,x29.length);
        routes[32] = new Route("Pink", "Albany", 
        "Binghampton", false, 6, null, test29);

        int[] x30 = {517, 525, 518, 510};      
        int[] y30 = {75, 121, 123, 76};
        Polygon test30 = new Polygon(x30,y30,x30.length);
        routes[33] = new Route("Orange", "Syracuse", 
        "Binghampton", true, 2, null, test30);
        routes[33].stock7 = true;

        int[] x31 = {508, 516, 509, 501};      
        int[] y31 = {76, 123, 124, 77};
        Polygon test31 = new Polygon(x31,y31,x31.length);
        routes[34] = new Route("Yellow", "Syracuse",
        "Binghampton", true, 2, null, test31);
        routes[34].stock7 = true;

        routes[33].adjacentRoute = routes[34];
        routes[34].adjacentRoute = routes[33];

        int[] x32 = {530, 543, 535, 523};      
        int[] y32 = {145, 216, 217, 146};
        Polygon test32 = new Polygon(x32,y32,x32.length);
        routes[35] = new Route("Green", "Binghampton",
        "Scranton/Wilkes Barre", true, 3, null, test32);
        routes[35].stock7 = true;

        int[] x33 = {520, 533, 527, 514};      
        int[] y33 = {147, 217, 218, 148};
        Polygon test33 = new Polygon(x33,y33,x33.length);
        routes[36] = new Route("Black", "Binghampton", 
        "Scranton/Wilkes Barre", true, 3, null, test33);
        routes[36].stock7 = true;

        routes[35].adjacentRoute = routes[36];
        routes[36].adjacentRoute = routes[35];

        int[] x34 = {522, 460, 465, 526};      
        int[] y34 = {230, 192, 186, 223};
        Polygon test34 = new Polygon(x34,y34,x34.length);
        routes[37] = new Route("Gray", "Towanda",
        "Scranton/Wilkes Barre", true, 3, null, test34);
        routes[37].stock9 = true;
        routes[37].stock7 = true;
        routes[37].stock5 = true;

        int[] x35 = {525, 405, 405, 525};      
        int[] y35 = {239, 252, 244, 232};
        Polygon test35 = new Polygon(x35,y35,x35.length);
        routes[38] = new Route("Orange", "Scranton/Wilkes Barre", 
        "Williamsport", false, 5, null, test35);

        int[] x36 = {522, 424, 418, 517};      
        int[] y36 = {249, 355, 349, 244};
        Polygon test36 = new Polygon(x36,y36,x36.length);
        routes[39] = new Route("Gray", "Scranton/Wilkes Barre",
        "Harrisburg", true, 6, null, test36);
        routes[39].stock9 = true;
        routes[39].stock6 = true;

        int[] x37 = {497, 421, 417, 492};      
        int[] y37 = {75, 134, 129, 70};
        Polygon test37 = new Polygon(x37,y37,x37.length);
        routes[40] = new Route("Black", "Syracuse",
        "Elmira", true, 4, null, test37);
        routes[40].stock7 = true;
        routes[40].stock5 = true;

        int[] x38 = {503, 431, 431, 503};      
        int[] y38 = {141, 141, 133, 133};
        Polygon test38 = new Polygon(x38,y38,x38.length);
        routes[41] = new Route("White", "Elmira", 
        "Binghampton", true, 3, null, test38);
        routes[41].stock7 = true;

        int[] x39 = {507, 468, 463, 502};      
        int[] y39 = {151, 178, 172, 144};
        Polygon test39 = new Polygon(x39,y39,x39.length);
        routes[42] = new Route("Red", "Binghampton", 
        "Towanda", true, 2, null, test39);
        routes[42].stock7 = true;

        int[] x40 = {474, 427, 427, 474};      
        int[] y40 = {358, 362, 354, 351};
        Polygon test40 = new Polygon(x40,y40,x40.length);
        routes[43] = new Route("Pink", "Reading",
        "Harrisburg", true, 2, null, test40);
        routes[43].stock6 = true;

        int[] x41 = {465, 423, 426, 468};      
        int[] y41 = {390, 369, 363, 385};
        Polygon test41 = new Polygon(x41,y41,x41.length);
        routes[44] = new Route("Gray", "Harrisburg", 
        "Lancaster", true, 2, null, test41);
        routes[44].stock9 = true;

        int[] x42 = {461, 419, 422, 463};      
        int[] y42 = {399, 377, 371, 393};
        Polygon test42 = new Polygon(x42,y42,x42.length);
        routes[45] = new Route("Gray", "Harrisburg", 
        "Lancaster", true, 2, null, test42);
        routes[45].stock9 = true;

        routes[44].adjacentRoute = routes[45];
        routes[45].adjacentRoute = routes[44];

        int[] x43 = {486, 480, 472, 478};      
        int[] y43 = {365, 387, 385, 363};
        Polygon test43 = new Polygon(x43,y43,x43.length);
        routes[46] = new Route("Yellow", "Reading",
        "Lancaster", true, 1, null, test43);
        routes[46].stock6 = true;

        int[] x44 = {464, 442, 441, 463};      
        int[] y44 = {409, 412, 405, 402};
        Polygon test44 = new Polygon(x44,y44,x44.length);
        routes[47] = new Route("Pink", "Lancaster", 
        "York", true, 1, null, test44);
        routes[47].stock9 = true;

        int[] x45 = {420, 409, 415, 426};      
        int[] y45 = {403, 383, 380, 400};
        Polygon test45 = new Polygon(x45,y45,x45.length);
        routes[48] = new Route("Black", "Harrisburg",
        "York", true, 1, null, test45);
        routes[48].stock6 = true;
        routes[48].stock9 = true;

        int[] x46 = {420, 398, 396, 417};      
        int[] y46 = {416, 425, 418, 409};
        Polygon test46 = new Polygon(x46,y46,x46.length);
        routes[49] = new Route("Gray", "York", 
        "Gettysburg", true, 1, null, test46);
        routes[49].stock9 = true;
        routes[49].stock3 = true;

        int[] x47 = {459, 392, 394, 462};      
        int[] y47 = {458, 434, 428, 452};
        Polygon test47 = new Polygon(x47,y47,x47.length);
        routes[50] = new Route("Red", "Gettysburg",
        "Baltimore", true, 3, null, test47);
        routes[50].stock9 = true;
        routes[50].stock3 = true;

        int[] x48 = {451, 281, 281, 450};      
        int[] y48 = {464, 464, 457, 457};
        Polygon test48 = new Polygon(x48,y48,x48.length);
        routes[51] = new Route("Blue", "Cumberland",
        "Baltimore", true, 7, null, test48);
        routes[51].stock3 = true;
        routes[51].stock8 = true;

        int[] x49 = {467, 432, 438, 472};      
        int[] y49 = {452, 419, 413, 446};
        Polygon test49 = new Polygon(x49,y49,x49.length);
        routes[52] = new Route("White", "York", 
        "Baltimore", true, 2, null, test49);
        routes[52].stock3 = true;
        routes[52].stock9 = true;

        int[] x50 = {408, 387, 380, 401};      
        int[] y50 = {378, 421, 418, 375};
        Polygon test50 = new Polygon(x50,y50,x50.length);
        routes[53] = new Route("Yellow", "Harrisburg",
        "Gettysburg", true, 2, null, test50);
        routes[53].stock9 = true;
        routes[53].stock6 = true;

        int[] x51 = {368, 347, 349, 370};      
        int[] y51 = {427, 420, 413, 420};
        Polygon test51 = new Polygon(x51,y51,x51.length);
        routes[54] = new Route("Black", "Chambersburg", 
        "Gettysburg", false, 1, null, test51);

        int[] x52 = {389, 347, 343, 386};      
        int[] y52 = {386, 406, 400, 380};
        Polygon test52 = new Polygon(x52,y52,x52.length);
        routes[55] = new Route("Blue", "Harrisburg",
        "Chambersburg", true, 2, null, test52);
        routes[55].stock3 = true;
        routes[55].stock9 = true;
        routes[55].stock6 = true;

        int[] x53 = {397, 354, 348, 390};      
        int[] y53 = {263, 321, 317, 258};
        Polygon test53 = new Polygon(x53,y53,x53.length);
        routes[56] = new Route("Yellow", "Williamsport",
        "Lewiston", true, 3, null, test53);
        routes[56].stock9 = true;
        routes[56].stock6 = true;

        int[] x54 = {402, 361, 364, 406};      
        int[] y54 = {357, 335, 329, 351};
        Polygon test54 = new Polygon(x54,y54,x54.length);
        routes[57] = new Route("Gray", "Lewiston", 
        "Harrisburg", true, 2, null, test54);
        routes[57].stock9 = true;

        int[] x55 = {396, 373, 371, 348, 346, 324, 322, 300, 298,
            277, 280, 300, 303, 324, 326, 347, 350, 371, 374, 396};      
        int[] y55 = {368, 367, 366, 363, 362, 357, 356, 348, 347,
            338, 332, 341, 342, 349, 350, 355, 356, 359, 359, 360};
        Polygon test55 = new Polygon(x55,y55,x55.length);
        routes[58] = new Route("Red", "Altoona",
        "Harrisburg", true, 5, null, test55);
        routes[58].stock9 = true;

        int[] x56 = {395, 372, 369, 347, 344, 322, 319, 297, 295, 274,
            277, 297, 300, 321, 323, 345, 348, 370, 372, 395};      
        int[] y56 = {377, 376, 375, 372, 371, 365, 364, 357, 356, 
            347, 340, 349, 350, 357, 358, 364, 364, 368, 368, 369};
        Polygon test56 = new Polygon(x56,y56,x56.length);
        routes[59] = new Route("Orange", "Altoona", 
        "Harrisburg", true, 5, null, test56);
        routes[59].stock9 = true;

        routes[58].adjacentRoute = routes[59];
        routes[59].adjacentRoute = routes[58];

        int[] x57 = {250, 129, 129, 250};      
        int[] y57 = {464, 464, 457, 457};
        Polygon test57 = new Polygon(x57,y57,x57.length);
        routes[60] = new Route("Red", "Morgantown",
        "Cumberland", true, 5, null, test57);
        routes[60].stock3 = true;
        routes[60].stock8 = true;

        int[] x58 = {322, 284, 280, 318};      
        int[] y58 = {420, 448, 443, 415};
        Polygon test58 = new Polygon(x58,y58,x58.length);
        routes[61] = new Route("Green", "Chambersburg", 
        "Cumberland", true, 2, null, test58);
        routes[61].stock3 = true;
        routes[61].stock9 = true;

        int[] x59 = {259, 226, 232, 266};      
        int[] y59 = {440, 377, 373, 437};
        Polygon test59 = new Polygon(x59,y59,x59.length);
        routes[62] = new Route("Gray", "Johnstown",
        "Cumberland", true, 3, null, test59);
        routes[62].stock3 = true;
        routes[62].stock9 = true;
        routes[62].stock8 = true;

        int[] x60 = {252, 233, 229, 248};      
        int[] y60 = {342, 354, 347, 335};
        Polygon test60 = new Polygon(x60,y60,x60.length);
        routes[63] = new Route("Yellow", "Altoona",
        "Johnstown", true, 1, null, test60);
        routes[63].stock9 = true;

        int[] x61 = {256, 237, 234, 253};      
        int[] y61 = {349, 361, 355, 343};
        Polygon test61 = new Polygon(x61,y61,x61.length);
        routes[64] = new Route("Blue", "Altoona", 
        "Johnstown", true, 1, null, test61);
        routes[64].stock9 = true;

        routes[63].adjacentRoute = routes[64];
        routes[64].adjacentRoute = routes[63];

        int[] x62 = {329, 281, 281, 329};      
        int[] y62 = {328, 328, 321, 321};
        Polygon test62 = new Polygon(x62,y62,x62.length);
        routes[65] = new Route("Green", "Altoona",
        "Lewiston", false, 2, null, test62);

        int[] x63 = {256, 235, 241, 262};      
        int[] y63 = {323, 280, 276, 319};
        Polygon test63 = new Polygon(x63,y63,x63.length);
        routes[66] = new Route("Gray", "Dubois",
        "Altoona", true, 2, null, test63);
        routes[66].stock9 = true;

        int[] x64 = {386, 308, 313, 391};      
        int[] y64 = {243, 187, 181, 238};
        Polygon test64 = new Polygon(x64,y64,x64.length);
        routes[67] = new Route("Green", "Coundersport",
        "Williamsport", true, 4, null, test64);
        routes[67].stock9 = true;

        int[] x65 = {386, 241, 241, 385};      
        int[] y65 = {255, 270, 262, 247};
        Polygon test65 = new Polygon(x65,y65,x65.length);
        routes[68] = new Route("White", "Williamsport",
        "Dubois", false, 6, null, test65);

        int[] x66 = {442, 422, 417, 411, 418, 424, 426, 446};      
        int[] y66 = {186, 175, 169, 147, 146, 167, 169, 179};
        Polygon test66 = new Polygon(x66,y66,x66.length);
        routes[69] = new Route("Yellow", "Elmira", 
        "Towanda", true, 2, null, test66);
        routes[69].stock9 = true;
        routes[69].stock7 = true;
        routes[69].stock5 = true;

        int[] x67 = {404, 314, 312, 402};      
        int[] y67 = {144, 176, 169, 137};
        Polygon test67 = new Polygon(x67,y67,x67.length);
        routes[70] = new Route("Orange", "Elmira",
        "Coundersport", true, 4, null, test67);
        routes[70].stock9 = true;
        routes[70].stock7 = true;
        routes[70].stock8 = true;
        routes[70].stock4 = true;
        routes[70].stock1 = true;

        int[] x68 = {292, 246, 252, 299};      
        int[] y68 = {164, 79, 76, 160};
        Polygon test68 = new Polygon(x68,y68,x68.length);
        routes[71] = new Route("Gray", "Buffalo",
        "Coundersport", true, 4, null, test68);
        routes[71].stock9 = true;
        routes[71].stock7 = true;

        int[] x69 = {294, 272, 269, 247, 243, 221,
            218, 196, 198, 219, 222, 244, 247, 268, 271, 292};      
        int[] y69 = {181, 184, 184, 186, 186, 185,
            184, 180, 173, 177, 177, 178, 178, 178, 177, 173};
        Polygon test69 = new Polygon(x69,y69,x69.length);
        routes[72] = new Route("Gray", "Warren", 
        "Coundersport", true, 4, null, test69);
        routes[72].stock9 = true;
        routes[72].stock7 = true;
        routes[72].stock8 = true;
        routes[72].stock4 = true;
        routes[72].stock1 = true;

        int[] x70 = {223, 192, 198, 229};      
        int[] y70 = {253, 189, 186, 250};
        Polygon test70 = new Polygon(x70,y70,x70.length);
        routes[73] = new Route("Black", "Warren",
        "Dubois", true, 3, null, test70);
        routes[73].stock7 = true;
        routes[73].stock1 = true;        

        int[] x71 = {214, 147, 150, 217};      
        int[] y71 = {266, 240, 233, 259};
        Polygon test71 = new Polygon(x71,y71,x71.length);
        routes[74] = new Route("Pink", "Oil City",
        "Dubois", false, 3, null, test71);

        int[] x72 = {182, 150, 144, 177};      
        int[] y72 = {185, 219, 214, 180};
        Polygon test72 = new Polygon(x72,y72,x72.length);
        routes[75] = new Route("Orange", "Warren", 
        "Oil City", true, 2, null, test72);
        routes[75].stock9 = true;  
        routes[75].stock7 = true;  
        routes[75].stock8 = true;  

        int[] x73 = {241, 199, 192, 235};      
        int[] y73 = {78, 163, 160, 74};
        Polygon test73 = new Polygon(x73,y73,x73.length);
        routes[76] = new Route("Green", "Buffalo",
        "Warren", true, 4, null, test73);
        routes[76].stock9 = true;  
        routes[76].stock7 = true;  
        routes[76].stock8 = true;  
        routes[76].stock1 = true;  

        int[] x74 = {212, 190, 187, 164, 162, 140, 137, 
            115, 117, 138, 141, 162, 165, 187, 190, 212};      
        int[] y74 = {359, 361, 361, 360, 360, 356, 355, 
            349, 342, 348, 349, 352, 353, 353, 353, 352};
        Polygon test74 = new Polygon(x74,y74,x74.length);
        routes[77] = new Route("Pink", "Pittsburgh",
        "Johnstown", true, 4, null, test74);
        routes[77].stock9 = true;  
        routes[77].stock8 = true;  

        int[] x75 = {211, 189, 186, 164, 161, 139, 136,
            115, 116, 138, 140, 162, 164, 186, 189, 211};      
        int[] y75 = {368, 369, 369, 368, 368, 365, 
            364, 358, 352, 358, 359, 362, 362, 363, 363, 361};
        Polygon test75 = new Polygon(x75,y75,x75.length);
        routes[78] = new Route("Black", "Pittsburgh",
        "Johnstown", true, 4, null, test75);
        routes[78].stock9 = true;  
        routes[78].stock8 = true;

        routes[77].adjacentRoute = routes[78];
        routes[78].adjacentRoute = routes[77];

        int[] x76  = {108, 115, 107, 101};      
        int[] y76 = {369, 441, 441, 369};
        Polygon test76 = new Polygon(x76,y76,x76.length);
        routes[79] = new Route("Yellow", "Pittsburgh",
        "Morgantown", false, 3, null, test76);

        int[] x77  = {100, 79, 76, 60, 57, 46, 52, 63, 65, 80, 83, 103};      
        int[] y77 = {459, 450, 447, 432, 429, 408, 
            405, 425, 427, 442, 444, 452};
        Polygon test77 = new Polygon(x77,y77,x77.length);
        routes[80] = new Route("Blue", "Wheeling",
        "Morgantown", true, 3, null, test77);
        routes[80].stock8 = true;

        int[] x78  = {98, 61, 56, 93};      
        int[] y78 = {364, 393, 387, 358};
        Polygon test78 = new Polygon(x78,y78,x78.length);
        routes[81] = new Route("White", "Pittsburgh",
        "Wheeling", true, 2, null, test78);
        routes[81].stock9 = true;

        int[] x79  = {93, 56, 51, 88};      
        int[] y79 = {357, 386, 380, 351};
        Polygon test79 = new Polygon(x79,y79,x79.length);
        routes[82] = new Route("Green", "Pittsburgh", 
        "Wheeling", true, 2, null, test79);
        routes[82].stock9 = true;

        routes[81].adjacentRoute = routes[82];
        routes[82].adjacentRoute = routes[81];

        int[] x80  = {39, 33, 32, 28, 28, 26, 26,
            26, 26, 28, 35, 34, 34, 33, 34, 35, 36, 39, 40, 46};      
        int[] y80 = {387, 365, 363, 340, 337, 314, 311, 288,
            285, 263, 263, 286, 289, 311, 314, 336, 340, 361, 363, 385};
        Polygon test80 = new Polygon(x80,y80,x80.length);
        routes[83] = new Route("Pink", "Youngstown", 
        "Wheeling", true, 5, null, test80);
        routes[83].stock9 = true;
        routes[83].stock8 = true;
        routes[83].stock4 = true;

        int[] x81  = {100, 85, 83, 69, 67, 55, 54, 
            44, 50, 60, 61, 72, 74, 88, 90, 105};      
        int[] y81 = {339, 324, 322, 304, 302, 283, 
            280, 260, 257, 277, 280, 298, 300, 317, 319, 334};
        Polygon test81 = new Polygon(x81,y81,x81.length);
        routes[84] = new Route("Blue", "Youngstown",
        "Pittsburgh", true, 4, null, test81);
        routes[84].stock9 = true;
        routes[84].stock8 = true;
        routes[84].stock4 = true;

        int[] x82  = {93, 77, 75, 61, 59, 48, 46,
            36, 43, 52, 53, 65, 66, 81, 82, 98};      
        int[] y82 = {345, 329, 327, 310, 307, 288, 
            285, 265, 262, 282, 285, 303, 305, 322, 324, 340};
        Polygon test82 = new Polygon(x82,y82,x82.length);
        routes[85] = new Route("Orange", "Youngstown", 
        "Pittsburgh", true, 4, null, test82);
        routes[85].stock9 = true;
        routes[85].stock8 = true;
        routes[85].stock4 = true;

        routes[84].adjacentRoute = routes[85];
        routes[85].adjacentRoute = routes[84];

        int[] x83 = {121, 52, 50, 119};      
        int[] y83 = {235, 254, 246, 228};
        Polygon test83 = new Polygon(x83,y83,x83.length);
        routes[86] = new Route("White", "Oil City",
        "Youngstown", true, 3, null, test83);
        routes[86].stock7 = true;

        int[] x84 = {90, 47, 40, 84};      
        int[] y84 = {153, 238, 235, 149};
        Polygon test84 = new Polygon(x84,y84,x84.length);
        routes[87] = new Route("Yellow", "Erie", 
        "Youngstown", true, 4, null, test84);
        routes[87].stock7 = true;
        routes[87].stock4 = true;

        int[] x85 = {98, 55, 48, 92};      
        int[] y85 = {157, 242, 239, 154};
        Polygon test85 = new Polygon(x85,y85,x85.length);
        routes[88] = new Route("Green", "Erie",
        "Youngstown", true, 4, null, test85);
        routes[88].stock7 = true;
        routes[88].stock4 = true;

        routes[87].adjacentRoute = routes[88];
        routes[88].adjacentRoute = routes[87];

        int[] x86 = {178, 112, 114, 180};      
        int[] y86 = {170, 144, 137, 164};
        Polygon test86 = new Polygon(x86,y86,x86.length);
        routes[89] = new Route("Blue", "Erie",
        "Warren", true, 3, null, test86);
        routes[89].stock9 = true;
        routes[89].stock7 = true;
        routes[89].stock4 = true;

        int[] x87 = {126, 102, 109, 133};      
        int[] y87 = {216, 149, 147, 214};
        Polygon test87 = new Polygon(x87,y87,x87.length);
        routes[90] = new Route("Black", "Erie",
        "Oil City", true, 3, null, test87);
        routes[90].stock9 = true;
        routes[90].stock7 = true;
        routes[90].stock4 = true;

        int[] x88 = {134, 117, 111, 127};      
        int[] y88 = {240, 334, 333, 239};
        Polygon test88 = new Polygon(x88,y88,x88.length);
        routes[91] = new Route("Red", "Oil City",
        "Pittsburgh", true, 4, null, test88);
        routes[91].stock9 = true;
        routes[91].stock8 = true;

        int[] x89 = {230, 212, 209, 191, 188, 168, 165, 145, 
            141, 121, 118, 139, 142, 161, 164, 184, 187, 205, 207, 224};      
        int[] y89 = {74, 88, 90, 103, 104, 115, 117, 126,
            127, 135, 128, 120, 119, 110, 109, 98, 96, 84, 83, 69};
        Polygon test89 = new Polygon(x89,y89,x89.length);
        routes[92] = new Route("White", "Buffalo",
        "Erie", true, 5, null, test89);
        routes[92].stock7 = true;
        routes[92].stock4 = true;

        int[] x90 = {224, 207, 204, 186, 183, 164, 161,
            141, 138, 117, 114, 135, 138, 158, 160, 180, 182, 200, 202, 219};      
        int[] y90 = {67, 81, 82, 95, 96, 107, 109, 118,
            119, 126, 119, 112, 111, 102, 101, 90, 89, 77, 75, 61};
        Polygon test90 = new Polygon(x90,y90,x90.length);
        routes[93] = new Route("Orange", "Buffalo", "Erie",
        true, 5, null, test90);
        routes[93].stock7 = true;
        routes[93].stock4 = true;

        routes[92].adjacentRoute = routes[93];
        routes[93].adjacentRoute = routes[92];

        int[] x91 = {444, 412, 407, 439};      
        int[] y91 = {201, 234, 230, 196};
        Polygon test91 = new Polygon(x91,y91,x91.length);
        routes[94] = new Route("Black", "Towanda",
        "Williamsport", true, 2, null, test91);
        routes[94].stock7 = true;
        routes[94 ].stock6 = true;

        //initialize players array
        players[0] = new Player("A", null);
        players[1] = new Player("B", null);
        players[2] = new Player("C", null);
        players[3] = new Player("D", null);
        players[4] = new Player("E", null);

        //initialize buttons array
        buttons = new Polygon[4];

        int[] button1X = {5, 218, 218, 5};
        int[] button1Y = {500, 500, 650, 650};
        buttons[0] = new Polygon(button1X, button1Y, 4);

        int[] button2X = {228, 453, 453, 228};
        buttons[1] = new Polygon(button2X, button1Y, 4);

        int[] button3X = {457, 710, 710, 457};
        buttons[2] = new Polygon(button3X, button1Y, 4);

        int[] dButtonX = {750, 910, 910, 750};
        int[] dButtonY = {650, 650, 690, 690};
        buttons[3] = new Polygon(dButtonX, dButtonY, 4);

        //stock types
        String[] types = {"BRP", "JCL", "WM", "NCS",
            "LV", "RL", "LE", "BO", "PRR"};

        //initialize stocks array
        for(int i = 0; i < types.length; i++)
        {
            stocks[i] = new StockCard(types[i]);
        }

        //adds mouse listener
        addMouseListener(this);
    }

    /**
     * Invoked when a mouse button has been clicked
     * (pressed and released) on a component
     * (Only present to satisfy the MouseListener interface)
     * 
     * @param e a MouseEvent object 
     */
    public void mouseClicked(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button has been released on a component
     * (Only present to satisfy the MouseListener interface)
     * 
     * @param e a MouseEvent object 
     */
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse enters a component
     * (Only present to satisfy the MouseListener interface)
     * 
     * @param e a MouseEvent object 
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     *Invoked when the mouse exits a component
     *(Only present to satisfy the MouseListener interface)
     *
     *@param e a MouseEvent object 
     */
    public void mouseExited(MouseEvent e)
    {
    }
}