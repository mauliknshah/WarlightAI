/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp;

import java.util.ArrayList;
import main.*;
import bot.*;
import java.util.Arrays;
import java.util.Collections;
import mdp.beans.WarlightMDPSkeleton;
import mdp.util.*;
/**
 * This class will take care of the execution of the MDP in the Warlight problem.
 * Creation Date: 30 Apr 2017.
 * @author Maulik
 */
public class WarlightMDPExecution {
    
    
    /**
     * This method returns an optimal set of actions for the next move by 
     * calculating via MDP over one state. 
     * 
     * For now, there is no value iteration in that. 
     * @param currentState is the current state of the Map from BOT's perspective. 
     * "Fog Regions" are not visible. 
     * @return best possible action set for execution.
     */
    public static ArrayList<String> findOptimalActionSet(BotState currentState,long timeout){
        //Create a tree for the solution.
        WarlightMDPSkeleton mdpTree = new WarlightMDPSkeleton(currentState);
        
//        for(Region reg: currentState.getVisibleMap().getRegions()){
//                System.out.println("Current Reg:" + reg.getId() + " Armies:" + reg.getArmies());
//            }
        
        //Final Optimal set of actions.
        ArrayList<String> optimalActionSet = new ArrayList<String>();
        
        //Find Non Bordering Non-one Regions.
        ArrayList<Region> nonBorderActiveRegions = new ArrayList<Region>();
        
        //Bordering Regions.
        ArrayList<Region> borderingRegions = findActiveRegions(currentState,nonBorderActiveRegions);
        
//        System.out.println("NB:" + nonBorderActiveRegions.size());
//        System.out.println("B:" + borderingRegions.size());
        
        //Deploy armies in certain regions. 
        int armiesToDeploy = currentState.getStartingArmies();
        int id = 1;
        //Find possible combination of deployment of the army.
        ArrayList<ArrayList<Integer>> possibleDeployments = Permutations.printCombination(borderingRegions.size(), armiesToDeploy);
        
//        System.out.println("Total Deployment:" + possibleDeployments.size());
        
        //Add all the possible deployment combination in the state. 
        for(ArrayList<Integer> deployment: possibleDeployments){
            BotState newState = new BotState(currentState);
            
            String action = "";
            //Update all the regions with deployment.
            for(int region=0 ; region < borderingRegions.size();region++){
                
                //Deploy only if the deployment is more than zero.
                if(deployment.get(region)  > 0 ){
                    
                    action += borderingRegions.get(region).getId() + "-" 
                           + borderingRegions.get(region).getId() + "-"
                           + deployment.get(region) + "-"
                           + WarlightMDPSkeleton.DEPLOY + ";"; 
                    
                    newState = WarlightMDPSkeleton.transition(newState, 
                            WarlightMDPSkeleton.DEPLOY,
                            borderingRegions.get(region).getId(), 
                            borderingRegions.get(region).getId(),
                            deployment.get(region) 
                            );
                }//end if.
            }//end for.
            
            double reward = WarlightMDPSkeleton.reward(newState,armiesToDeploy);
            //Add the Data into the tree.
            mdpTree.getStateSpace().add(new WarlightMDPSkeleton(newState,reward,action, ""+id));
        }//end for.
        
        
        
//        System.out.println("Size" + mdpTree.getStateSpace().size());
        
        //Create a merged list and create an array for the same.
        ArrayList<Region> activeRegion = new ArrayList<Region>();
        
        //Add regions to the active region. 
        //Add the non-border active regions.
        for(Region nonBorderActive: nonBorderActiveRegions){
            activeRegion.add(nonBorderActive);
        }//end for.
        //Add the Border regions.
        for(Region border:borderingRegions){
            activeRegion.add(border);
        }//end for.
            
//          System.out.println("Size" + activeRegion.size());
        //Loop through all the regions and explore possible tree.
        ArrayList<Double> maxReward = new ArrayList<Double>();
        ArrayList<String> maxRewardAction = new ArrayList<String>();
        //We are not focusing on sequencing for now.
        for(WarlightMDPSkeleton deployedState: mdpTree.getStateSpace()){
            //Create a solution tree for the given problem.
            //Start with the current tree with only deployed army.
            //Pass Bordering and Non-Bordering Active Regions.
            //
            
//            for(Region reg:deployedState.getCurrentWarlightState().getVisibleMap().getRegions()){
//                System.out.print("Passed Info:" + reg.getId() + " Armies:" + reg.getArmies());
//            }
            
            createTree(deployedState,activeRegion, maxRewardAction, id, maxReward,0
                        ,armiesToDeploy,nonBorderActiveRegions.size(),borderingRegions.size()
                        ,timeout,System.currentTimeMillis(),
                        new ArrayList<Integer> ());
            
        }//end for. Deploy state.
        
        //Set the optimal Action Set. 
        int bestActionIndex = maxReward.indexOf(Collections.max(maxReward));
//        System.out.println("Max Reward:" + Collections.max(maxReward) + " Best Action:" + maxRewardAction.get(bestActionIndex));
        optimalActionSet = new ArrayList<String> (Arrays.asList(maxRewardAction.get(bestActionIndex).split(";")));
        
        return optimalActionSet;
    }//end method.
    
    /**
     * This method finds the bordering regions owned by the BOT.
     * @param currentState is the current state in the map.
     * @param nonBorderingActiveRegions is an array for updating the non-Bordering Active Regions.
     * @return bordering regions.
     */
    public static ArrayList<Region> findActiveRegions(BotState currentState,ArrayList<Region> nonBorderingActiveRegions){
        ArrayList<Region> borderingRegions = new ArrayList<Region>();
        //Iterate through all the regions.
        for(Region region: currentState.getVisibleMap().getRegions()){
            //If the Current state is owned by the BOT.
            if(currentState.getMyPlayerName().equals(region.getPlayerName())){
                boolean isBordering = false;
                //Search through all the neighbours. 
                for(Region neighbour: region.getNeighbors()){
                    //If the region is not owned by the BOT then.
                    if(!(currentState.getMyPlayerName().equals(neighbour.getPlayerName()))){
                        borderingRegions.add(region);
                        isBordering = true;
                        break;
                    }//end if.
                }//end for.
                //If the region is not bordering.
                if(!isBordering){
                  //If the armies on the region is greater than 1.
                  if(region.getArmies() > 1){
                      nonBorderingActiveRegions.add(region);
                  }//end if.
                }//end if.
            }//end if.
        }//end for.
        
        return borderingRegions;
    }//end method.
    
    /**
     * Find the best solution tree. 
     * @param solutionTree is the current solution tree.
     * @param activeRegions is the combination of the non-Border Active and Border
     * regions.
     * @param bestAction is the best performed action.
     * @param id is the id of a particular state.
     * @param maxReward is the maximum Reward till now.
     * @param treeLevel is the level in the tree. 
     * @param armiesToDeploy is the number of armies to be deployed in this turn.
     * @param transferSize is the number of transfer regions.
     * @param attackSize is the number of border regions.
     */
    public static void createTree(WarlightMDPSkeleton solutionTree
            ,ArrayList<Region> activeRegions,ArrayList<String> bestAction,Integer id,
            ArrayList<Double> maxReward,int treeLevel,int armiesToDeploy,
            int transferSize,int attackSize,long timeout,long beginTime,
            ArrayList<Integer> exploredNeighbourhood){
             //If we have not crossed the time limit.         
            if((System.currentTimeMillis() - beginTime) <= (timeout - 100)){
                //Get the current level of Bordering Region.
                boolean skipRegion = true; //IF the region is skipped for exploring or not.
                Region activeReg = activeRegions.get(treeLevel);
                
                // Iterate throrugh all the neighbouring region of the 
                for(Region neighbourReg: activeReg.getNeighbors()){
                    int activeRegArmy = solutionTree.getCurrentWarlightState().getVisibleMap().getRegion(activeReg.getId()).getArmies();
                    int neightbourArmy = solutionTree.getCurrentWarlightState().getVisibleMap().getRegion(neighbourReg.getId()).getArmies();
                    BotState newBOTState = null;
                    WarlightMDPSkeleton newState  = null;
                    //If transfer or attack.
                    if(treeLevel < transferSize){
                        //If the neighbouring region is owned by the BOT.
                        //It is in the active region
                        // And a border territory then do the transfer.
                        if(neighbourReg.getPlayerName().equals(solutionTree.getCurrentWarlightState().getMyPlayerName())
                                && activeRegions.contains(neighbourReg)
                                && (activeRegions.indexOf(neighbourReg) > transferSize)
                                && activeRegArmy > 1){
                            skipRegion = false;
                            String transferString = activeReg.getId() + "-" 
                                        + neighbourReg.getId() +  "-" 
                                        + (activeRegArmy - 1) + "-"
                                        + WarlightMDPSkeleton.TRANSFER + ";";
                            
                            //Creaate new state from the currently executing state.
                                 newBOTState = 
                                        WarlightMDPSkeleton.transition(solutionTree.getCurrentWarlightState()
                                                                        ,WarlightMDPSkeleton.TRANSFER
                                                                        ,activeReg.getId()
                                                                        , neighbourReg.getId()
                                                                        , (activeRegArmy - 1));
                                //double reward = WarlightMDPSkeleton.reward(newBOTState,armiesToDeploy);
                                //Set the new state afterthe transition.
                                 newState = new WarlightMDPSkeleton(
                                                newBOTState,
                                                0,
                                                solutionTree.getActionPerformed() + transferString,
                                                ""+id); 
                                solutionTree.getStateSpace()
                                        .add(newState);
                                //Go ahead with other operation.
                                createTree(newState,activeRegions, bestAction, id, maxReward
                                            ,treeLevel + 1,armiesToDeploy,transferSize,attackSize,timeout
                                                ,System.currentTimeMillis(),exploredNeighbourhood);
                        }//end if.
                    }else{
                        //Find if the region is the enemy region
                        // it is worthy to fight this region or not.
                        // and if for the same round,the region is already explored before or not.
                        if(!neighbourReg.getPlayerName().equals(solutionTree.getCurrentWarlightState().getMyPlayerName()) 
                                        && neightbourArmy <= Math.round((activeRegArmy-1) * 0.6)
                                        && !exploredNeighbourhood.contains(neighbourReg.getId())){
                            skipRegion = false;
//                            System.out.println("ID:" + activeReg.getId() + " Active Army: " + activeRegArmy + "N ID: " + neighbourReg.getId() + " Neighbour Army:" + neightbourArmy);
                            //Iterate through all possible combination of the 
                            //army deployment. 
                            
                            for(int i= (int)Math.round(((neightbourArmy) * 1.6));
                                                    i < (activeRegArmy);i++){

                                int action = WarlightMDPSkeleton.ATTACK;


                                //Attack String.
                                String attackString = activeReg.getId() + "-" 
                                        + neighbourReg.getId() +  "-" 
                                        + i + "-"
                                        + action + ";";

                                //Creaate new state from the currently executing state.
                                 newBOTState = 
                                        WarlightMDPSkeleton.transition(solutionTree.getCurrentWarlightState()
                                                                        ,action
                                                                        ,activeReg.getId()
                                                                        , neighbourReg.getId()
                                                                        , i);
                                //double reward = WarlightMDPSkeleton.reward(newBOTState,armiesToDeploy);
                                //Set the new state afterthe transition.
                                 newState = new WarlightMDPSkeleton(
                                                newBOTState,
                                                0,
                                                solutionTree.getActionPerformed() + attackString,
                                                ""+id); 
                                solutionTree.getStateSpace()
                                        .add(newState);
//                                System.out.println(newState.getActionPerformed());
                                id++;

                                //Recurrsion for each tree level to find the solution.
                                //Find each level of the tree and add it to the tree.
                                if((activeRegArmy - i -1) > 0){
                                    exploredNeighbourhood.add(neighbourReg.getId());
                                    //Create Recursive Tree for the next level.
                                    createTree(newState,activeRegions, bestAction, id, maxReward
                                            ,treeLevel,armiesToDeploy,transferSize,attackSize,timeout,
                                            System.currentTimeMillis(),exploredNeighbourhood);
                                }else{
                                    if(treeLevel < (activeRegions.size() - 1)){
                                        createTree(newState,activeRegions, bestAction, id, maxReward
                                            ,treeLevel + 1,armiesToDeploy,transferSize,attackSize,timeout
                                                ,System.currentTimeMillis(),exploredNeighbourhood);
                                    }else{
                                        double newReward = WarlightMDPSkeleton.reward(newBOTState,armiesToDeploy);
                                        newState.setCurrentReward(newReward);
                                        //If the new state has more reward than the previous state then...
                                        //Set the maximum Reward and the best action.
                                            maxReward.add(newReward);
                                            bestAction.add(newState.getActionPerformed());
//                                            System.out.println("MaxR:" + maxReward + " Best Action:" + bestAction);

                                    }//end if-else. 
                                }//end if-else.
                            }//end for.
                        }//end if.
                    }//end if Transfer or attack.
                }//end for Neighbour.
                
                if(treeLevel < (activeRegions.size() - 1) && skipRegion){
                                    //Create Recursive Tree for the next level.
                    createTree(solutionTree,activeRegions, bestAction
                                            ,id, maxReward
                                            ,treeLevel + 1,armiesToDeploy,transferSize,attackSize
                                            ,timeout,System.currentTimeMillis()
                                            ,exploredNeighbourhood);
                }else{
                    double newReward = WarlightMDPSkeleton.reward(solutionTree.getCurrentWarlightState(),armiesToDeploy);
                    solutionTree.setCurrentReward(newReward);
                    //If the new state has more reward than the previous state then...
                    //Set the maximum Reward and the best action.
                       maxReward.add(newReward);
                       bestAction.add(solutionTree.getActionPerformed());
//                       System.out.println("MaxR:" + maxReward + " Best Action:" + bestAction);
                }//end if-else. 
                
            }//end if
    }//end method.
}//end class. 
