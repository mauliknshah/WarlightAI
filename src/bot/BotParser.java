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
import java.util.Scanner;

import main.Region;
import mdp.WarlightMDPExecution;
import mdp.beans.*;
import move.PlaceArmiesMove;
import move.AttackTransferMove;

public class BotParser {
	
	final Scanner scan;
	
	final Bot bot;
	
	BotState currentState;
        
        ArrayList<String> actionString; 
	
	public BotParser(Bot bot)
	{
		this.scan = new Scanner(System.in);
		this.bot = bot;
		this.currentState = new BotState();
	}
	
	public void run()
	{
		while(scan.hasNextLine())
		{
			String line = scan.nextLine().trim();
			if(line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if(parts[0].equals("pick_starting_regions")) {
                            
                                
				//pick which regions you want to start with
				currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = bot.getPreferredStartingRegions(currentState, Long.valueOf(parts[1]));
				String output = "";
				for(Region region : preferredStartingRegions)
					output = output.concat(region.getId() + " ");
				
				System.out.println(output);
			} else if(parts.length == 3 && parts[0].equals("go")) {
				//we need to do a move
				String output = "";
				if(parts[1].equals("place_armies")) 
				{
					//place armies
//					ArrayList<PlaceArmiesMove> placeArmiesMoves = bot.getPlaceArmiesMoves(currentState, Long.valueOf(parts[2]));
//					for(PlaceArmiesMove move : placeArmiesMoves)
                                    
                                         this.actionString = WarlightMDPExecution.findOptimalActionSet(currentState,Long.valueOf(parts[2]));
                                         
//                                         for(String action:actionString){
//                                             System.out.println("Action:" + action);
//                                         }
                                         
                                         for(String action:actionString){
                                             String actionParts[] = action.split("-");
                                             //If the action is Deploy then.
                                             try{
                                                if(WarlightMDPSkeleton.DEPLOY == Integer.parseInt(actionParts[3])){
                                                    output = output.concat(currentState.getMyPlayerName() + " place_armies " 
                                                                                   + actionParts[0] + " " + actionParts[2]
                                                                                   + ",");
                                                }//end if.
                                             }catch(Exception e){
                                                 
                                             }
                                         }//end for.
						
				} 
				else if(parts[1].equals("attack/transfer")) 
				{
					//attack/transfer
//					ArrayList<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(currentState, Long.valueOf(parts[2]));
//					for(AttackTransferMove move : attackTransferMoves)
//						output = output.concat(move.getString() + ",");
                                    for(String action:actionString){
                                             String actionParts[] = action.split("-");
                                        //If the action is Deploy then.
                                        if(!actionParts[3].equals(WarlightMDPSkeleton.DEPLOY)){
                                            output = output.concat(currentState.getMyPlayerName() + " attack/transfer " 
                                                                                + actionParts[0] + " " + actionParts[1] 
                                                                                + " " + actionParts[2]
                                                                                + ",");
                                        }//end if.
                                    }//end for.
				}
				if(output.length() > 0)
					System.out.println(output);
				else
					System.out.println("No moves");
			} else if(parts.length == 3 && parts[0].equals("settings")) {
				//update settings
				currentState.updateSettings(parts[1], parts[2]);
			} else if(parts[0].equals("setup_map")) {
				//initial full map is given
				currentState.setupMap(parts);
			} else if(parts[0].equals("update_map")) {
				//all visible regions are given
				currentState.updateMap(parts);
			} else if(parts[0].equals("opponent_moves")) {
				//all visible opponent moves are given
				currentState.readOpponentMoves(parts);
			} else {
				System.err.printf("Unable to parse line \"%s\"\n", line);
			}
		}
	}

}
