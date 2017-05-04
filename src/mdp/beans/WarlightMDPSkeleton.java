/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.beans;

import java.util.ArrayList;

import bot.*;
import main.*;
import java.util.LinkedList;

/**
 * Created on 30 Apr,2017. 
 * Warlight MDP skeleton is the representation of the
 * warlight problem into MDP.
 * @author Maulik
 */
public class WarlightMDPSkeleton {
    //Set of action constants.   
    public static final int DEPLOY = 0;
    public static final int TRANSFER = 1;
    public static final int ATTACK = 2;
    //Possible Set of action.
    private int actionSet[]; 
    
    //Current State
    private BotState currentWarlightState;
    private double currentReward;
    private String actionPerformed;
    private String id;
    //State Space which can be expanded from the current warlight state.
    private ArrayList<WarlightMDPSkeleton> stateSpace; 
    
    
    
    //Constructor.
    public WarlightMDPSkeleton(BotState currentWarlightState) {
        this.currentWarlightState = new BotState(currentWarlightState);
        this.stateSpace = new ArrayList<WarlightMDPSkeleton> ();
        this.actionSet = new int[]{DEPLOY, TRANSFER, ATTACK}; //Initialize the action set. 
    }//end constructor.
    
    //Constructor.
    public WarlightMDPSkeleton(BotState currentWarlightState,double currentReward,String actionPerformed,String id) {
        this.currentWarlightState = new BotState(currentWarlightState);
        this.currentReward = currentReward;
        this.actionPerformed = ""+actionPerformed;
        this.id = id;
        this.stateSpace = new ArrayList<WarlightMDPSkeleton> ();
        this.actionSet = new int[]{DEPLOY, TRANSFER, ATTACK}; //Initialize the action set. 
    }//end constructor.

    /**
     * This function calculates reward a particular state. This indicates the
     * strength of the player after reaching certain positions.
     * @param currentState is the state for reward calculations.
     * @param armiesPerTurn is armies per turn.
     * @return reward value.
     */
    public static double reward(BotState currentState, int armiesPerTurn) {
        double reward = 0; //Reward value.
        double regionStoR = 0; // Region's sTor ration.
        int armyStrength = 0; //Total Armies.
        int armyInMidRegions = 0; // Total number of armies in the mid regions.
        int numberOfRegions = 0;

        try {
            //Sum all the positive values to find the army strength and
            //Find the number of the region.
            for (Region region: currentState.getVisibleMap().getRegions()) {
                //Get Armies of the current regions.
                //If the region is owned by the player.
                if (currentState.getMyPlayerName().equals(region.getPlayerName())) {
                    regionStoR += (region.getSuperRegion().getSubRegions().size()
                                    / region.getSuperRegion().getArmiesReward());
                    armyStrength += region.getArmies();
                    numberOfRegions++;

                    //Find if the region is bordering region or not.
                    if (!findIfBorderRegion(region.getId(),currentState)) {
                        armyInMidRegions += region.getArmies();
                    }//end if.
                }//end if.
                
            }//end for.
            //The reward for being can be calculated from the reward function as given below.
            reward = (armyStrength - armyInMidRegions) 
                    * Math.pow(regionStoR * numberOfRegions,2) 
                    * armiesPerTurn
                    / 100;
        } catch (Exception e) {
            System.out.println("Error while Calculating Reward:" + e);
        }
        return reward;
    }//end method.

    /**
     * This region finds if the current region is in bordering region or not.
     * @param regionId is the region id of the current region.
     * @param currentState is the current state of the map or environment.
     * @return true if in bordering region , false otherwise.
     */
    public static boolean findIfBorderRegion(int regionId, BotState currentState) {
        boolean isBordering = false;
        //Neighbouring Regions.
        LinkedList<Region> neighbouringRegions = currentState.getVisibleMap().getRegion(regionId).getNeighbors();
        //Find if any of the region is not owned by the player.
        for(Region neighbour : neighbouringRegions ){
           //Find if the armies on the region is positive. 
           if(currentState.getMyPlayerName().equals(neighbour.getPlayerName())){
               isBordering = true;
               break;
           }//end if.
        }//end for.
        
        return isBordering;
    }//end method.
    
    
    /**
     * This method calculates the possibilities of a transition over one state to another. 
     * The method does not validate the actions. 
     * For. eg. if the ATTACK is to owned territory or not.
     * Before calling this method one must validate the actions.
     * @param currentState is the current state. 
     * @param action is the action performed for reaching in some state. 
     * @param sourceRegion  is the origin of the action.
     * @param destinationRegion is the destination of the action.
     * @return next state after performing action.
     */
    public static BotState transition(BotState currentState,int action,int sourceRegion,int destinationRegion,int armyInAction){
        BotState nextState = new BotState(currentState);
        
        try{
            if(action == WarlightMDPSkeleton.DEPLOY){
                int  sourceArmy = nextState.getVisibleMap().getRegion(sourceRegion).getArmies();   
                nextState.getVisibleMap().getRegion(sourceRegion).setArmies(sourceArmy+armyInAction);
            }else if(action == WarlightMDPSkeleton.ATTACK){
                   int sourceArmy = nextState.getVisibleMap().getRegion(sourceRegion).getArmies();
                   String sourcePlayer = nextState.getVisibleMap().getRegion(sourceRegion).getPlayerName();
                   int destinationArmy = nextState.getVisibleMap().getRegion(destinationRegion).getArmies();
                   //Find the value of the destination army after attack.
                   int destArmyAct = destinationArmy - (int)Math.round(0.6*armyInAction);
                   //Find the value of the source army after the attack.
                   int srcArmyAct = armyInAction - (int)Math.round(0.7*destinationArmy);
                   
                   //If the destination region is captured by the source region. 
                   if(destArmyAct <= 0){
                       destinationArmy = (-1 * destArmyAct) + (srcArmyAct);
                       nextState.getVisibleMap().getRegion(destinationRegion).setPlayerName(sourcePlayer);
                       nextState.getVisibleMap().getRegion(destinationRegion).setArmies(destinationArmy);
                       nextState.getVisibleMap().getRegion(sourceRegion).setArmies(sourceArmy-armyInAction);
                   }else{
                       nextState.getVisibleMap().getRegion(destinationRegion).setArmies(destArmyAct);
                       nextState.getVisibleMap().getRegion(sourceRegion).setArmies(sourceArmy-armyInAction);
                   }//end if.
            }else if(action == WarlightMDPSkeleton.TRANSFER){
                int sourceArmy = nextState.getVisibleMap().getRegion(sourceRegion).getArmies();
                int destinationArmy = nextState.getVisibleMap().getRegion(destinationRegion).getArmies();
                nextState.getVisibleMap().getRegion(sourceRegion).setArmies(sourceArmy - armyInAction);
                nextState.getVisibleMap().getRegion(sourceRegion).setArmies(destinationArmy + armyInAction);
            }else{
                new Exception("Action Unidentified.");
            }
        }catch(Exception e){
            System.out.println("Error while Calculating Transition:" + e);
        }//end try catch.
        
        
        return nextState;
    }//end method.
    
    //Getter and Setter for the Space and Actions.
    public ArrayList<WarlightMDPSkeleton> getStateSpace() {
        return stateSpace;
    }

    public void setStateSpace(ArrayList<WarlightMDPSkeleton> stateSpace) {
        this.stateSpace = stateSpace;
    }

    public BotState getCurrentWarlightState() {
        return currentWarlightState;
    }

    public void setCurrentWarlightState(BotState currentWarlightState) {
        this.currentWarlightState = currentWarlightState;
    }

    public int[] getActionSet() {
        return actionSet;
    }

    public void setActionSet(int[] actionSet) {
        this.actionSet = actionSet;
    }

    public double getCurrentReward() {
        return currentReward;
    }

    public void setCurrentReward(double currentReward) {
        this.currentReward = currentReward;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    
}//end class. 