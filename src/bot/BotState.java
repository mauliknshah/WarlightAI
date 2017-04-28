/**
 * Warlight AI Game Bot
 *
 * Last update: April 02, 2014
 *
 * @author Jim van Eeden
 * @version 1.0
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

import java.util.ArrayList;
import java.util.Collections;

import main.Map;
import main.Region;
import main.SuperRegion;

import move.AttackTransferMove;
import move.PlaceArmiesMove;
import move.Move;

public class BotState {
	
	private String myName = "";
	private String opponentName = "";
	
	private final Map fullMap = new Map(); //This map is known from the start, contains all the regions and how they are connected, doesn't change after initialization
	private Map visibleMap; //This map represents everything the player can see, updated at the end of each round.
	
	private ArrayList<Region> pickableStartingRegions; //2 randomly chosen regions from each superregion are given, which the bot can chose to start with
	
	private ArrayList<Move> opponentMoves; //list of all the opponent's moves, reset at the end of each round

	private int startingArmies; //number of armies the player can place on map
	
	private int roundNumber;
	
	public BotState()
	{
		pickableStartingRegions = new ArrayList<Region>();
		opponentMoves = new ArrayList<Move>();
		roundNumber = 0;
	}
	
	public void updateSettings(String key, String value)
	{
		if(key.equals("your_bot")) //bot's own name
			myName = value;
		else if(key.equals("opponent_bot")) //opponent's name
			opponentName = value;
		else if(key.equals("starting_armies")) 
		{
			startingArmies = Integer.parseInt(value);
			roundNumber++; //next round
		}
	}
	
	//initial map is given to the bot with all the information except for player and armies info
        //Changed by Maulik Shah
        //28Apr 2017.
	public void setupMap(String[] mapInput)
	{
		int i, regionId, superRegionId, reward,army;
		
		if(mapInput[1].equals("super_regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new SuperRegion(superRegionId, reward));
				}
				catch(Exception e) {
					System.err.println("Unable to parse SuperRegions");
				}
			}
		}
		else if(mapInput[1].equals("regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
                                        army = Integer.parseInt(mapInput[i]);
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = fullMap.getSuperRegion(superRegionId);
                                        //Changed by Maulik Shah.
					fullMap.add(new Region(regionId, superRegion,army));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("neighbors"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					Region region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for(int j=0; j<neighborIds.length; j++)
					{
						Region neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch(Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
	}
	
	//regions from wich a player is able to pick his preferred starting regions
        
        //Changed by Maulik Shah
        //28 Apr 2017.
	public void setPickableStartingRegions(String[] mapInput)
	{
                //List: Ratio of (Sum of Armies * Num of regions) to Reward Army in a super region.        
                ArrayList<Double> armyToRewardRatio =new ArrayList<Double>(6);
                //List: Least AtoR ratio regions.
                ArrayList<Integer> selectedRegionId =new ArrayList<Integer>(6);
		for(int i=2; i<mapInput.length; i++)
		{
			int regionId;
                        int totalArmy = 0;
                        double aTorRatio = 0;
			try {
                                
                                //Get the Region.
				regionId = Integer.parseInt(mapInput[i]);
				SuperRegion currentRegSR = fullMap.getRegion(regionId).getSuperRegion();
                                 
                                //Sum all the armies in the region.
                                for(Region reg: currentRegSR.getSubRegions()){
                                    totalArmy += reg.getArmies();
//                                    System.out.println("Armies:" + reg.getArmies());
                                }
                                
                                
                               
                                //Find the aTorRatio
                                aTorRatio = (totalArmy * currentRegSR.getSubRegions().size())/(currentRegSR.getArmiesReward());
                                
//                                System.out.println("Region: " + regionId + "Total Army:" + totalArmy 
//                                        + " SuperRegion Ratio: " + aTorRatio 
//                                        + " No. of Reg." + currentRegSR.getSubRegions().size() 
//                                        + " Army Reward:" + currentRegSR.getArmiesReward());
                                
                                //If the size of the array list is less than 6
                                //Then add the region into the list.
                                if(armyToRewardRatio.size() < 6){
                                    armyToRewardRatio.add(aTorRatio);
                                    selectedRegionId.add(regionId);
                                }else if(Collections.max(armyToRewardRatio) > aTorRatio ){ //If the current ratio is less than some element of array then.
                                    int index = armyToRewardRatio.indexOf(Collections.max(armyToRewardRatio));
                                    armyToRewardRatio.set(index, aTorRatio);
                                    selectedRegionId.set(index, regionId);
                                }else{
                                    //Do nothing.
                                }//end if-else if-else.
			}catch(Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
                //Add the selected regions in the list. 
                //Select the region with the least atoR ratio first.
                for(int i= 0 ; i < 6 ; i++){
                    int index = armyToRewardRatio.indexOf(Collections.min(armyToRewardRatio));
                    //Add the region with the least aTor ratio in the list.
                    pickableStartingRegions.add(fullMap.getRegion(selectedRegionId.get(index)));
                    //Remove both the items.
                    armyToRewardRatio.remove(index);
                    selectedRegionId.remove(index);
                }//end for.
	}//end method.
	
	//visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput)
	{
		visibleMap = fullMap.getMapCopy();
		for(int i=1; i<mapInput.length; i++)
		{
			try {
				Region region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i+1];
				int armies = Integer.parseInt(mapInput[i+2]);
				
				region.setPlayerName(playerName);
				region.setArmies(armies);
				i += 2;
			}
			catch(Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
		ArrayList<Region> unknownRegions = new ArrayList<Region>();
		
		//remove regions which are unknown.
		for(Region region : visibleMap.regions)
			if(region.getPlayerName().equals("unknown"))
				unknownRegions.add(region);
		for(Region unknownRegion : unknownRegions)
			visibleMap.getRegions().remove(unknownRegion);				
	}

	//Parses a list of the opponent's moves every round. 
	//Clears it at the start, so only the moves of this round are stored.
	public void readOpponentMoves(String[] moveInput)
	{
		opponentMoves.clear();
		for(int i=1; i<moveInput.length; i++)
		{
			try {
				Move move;
				if(moveInput[i+1].equals("place_armies")) {
					Region region = visibleMap.getRegion(Integer.parseInt(moveInput[i+2]));
					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i+3]);
					move = new PlaceArmiesMove(playerName, region, armies);
					i += 3;
				}
				else if(moveInput[i+1].equals("attack/transfer")) {
					Region fromRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i+2]));
					if(fromRegion == null) //might happen if the region isn't visible
						fromRegion = fullMap.getRegion(Integer.parseInt(moveInput[i+2]));

					Region toRegion = visibleMap.getRegion(Integer.parseInt(moveInput[i+3]));
					if(toRegion == null) //might happen if the region isn't visible
						toRegion = fullMap.getRegion(Integer.parseInt(moveInput[i+3]));

					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i+4]);
					move = new AttackTransferMove(playerName, fromRegion, toRegion, armies);
					i += 4;
				}
				else { //never happens
					continue;
				}
				opponentMoves.add(move);
			}
			catch(Exception e) {
				System.err.println("Unable to parse Opponent moves " + e.getMessage());
			}
		}
	}
	
	public String getMyPlayerName(){
		return myName;
	}
	
	public String getOpponentPlayerName(){
		return opponentName;
	}
	
	public int getStartingArmies(){
		return startingArmies;
	}
	
	public int getRoundNumber(){
		return roundNumber;
	}
	
	public Map getVisibleMap(){
		return visibleMap;
	}
	
	public Map getFullMap(){
		return fullMap;
	}

	public ArrayList<Move> getOpponentMoves(){
		return opponentMoves;
	}
	
	public ArrayList<Region> getPickableStartingRegions(){
		return pickableStartingRegions;
	}

}
