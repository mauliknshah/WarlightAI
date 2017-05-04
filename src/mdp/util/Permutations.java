/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.util;


import java.util.ArrayList;
/**
 * The code is referenced from 
 * http://www.geeksforgeeks.org/print-all-possible-combinations-of-r-elements-in-a-given-array-of-size-n/
 * updated by Maulik Shah.
 */
public class Permutations {

    //Code added by Maulik
    
    
    
    /* arr[]  ---> Input Array
    data[] ---> Temporary array to store current combination
    start & end ---> Staring and Ending indexes in arr[]
    index  ---> Current index in data[]
    r ---> Size of a combination to be printed */
    static void combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r,ArrayList<ArrayList<Integer>> possibleCombinations)
    {
        // Current combination is ready to be printed, print it
        if (index == r)
        {
            ArrayList<Integer> newCombination = new ArrayList<Integer>();
            int sum = 0;
            for (int j=0; j<r; j++){
//                System.out.print(data[j]+" ");
                newCombination.add(data[j]);
                sum += data[j];
            }    
            if(sum == end){
                possibleCombinations.add(newCombination);
            }
//            System.out.println("");
            return;
        }
 
        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i=start; i<=end; i++)
        {
            data[index] = arr[i];
            //Modification by Maulik Shah.
            combinationUtil(arr, data, start, end, index+1, r,possibleCombinations);
        }
    }
 
    // The main function that prints all combinations of size r
    // in arr[] of size n. This function mainly uses combinationUtil()
    public static ArrayList<ArrayList<Integer>> printCombination(int regions, int armiesToDeploy)
    {
        //ArrayList.
        ArrayList<ArrayList<Integer>> possibleCombinations = new ArrayList<ArrayList<Integer>>();
        
        //create a simple array from 0 to max.
        int arr[] = new int[armiesToDeploy + 1];
        for(int i=0;i<=armiesToDeploy;i++)
             arr[i] =i;
        // A temporary array to store all combination one by one
        int data[]=new int[regions];
 
        // Print all combination using temprary array 'data[]'
        combinationUtil(arr, data, 0, armiesToDeploy, 0, regions,possibleCombinations);
        return possibleCombinations;
    }
 
    /*Driver function to check for above function*/
//    public static void main (String[] args) {
////        int arr[] = {0,1,2,3,4,5};
////        int r = 3;
////        int n = arr.length;
////        printCombination(3,5);
//        
////        for(ArrayList<Integer> combination: printCombination(3,5)){
////            System.out.println("Combination:" + combination.toString());
////        }
//
////          System.out.println(Math.ceil(10.6));
//    }
}

