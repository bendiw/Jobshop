package utils;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class Utils {
	public static int[] getJobArray(double[] position, int jobs){
		int[] jobArray = new int[position.length];
		double[] posCopy = position.clone();
//		int[] indexArray = new int[position.length];
		Arrays.sort(posCopy);
		for (int i = 0; i < jobArray.length; i++) {
			int index = ArrayUtils.indexOf(posCopy,position[i]);
//			indexArray[i] = index;
			jobArray[i] = ((index+1)%(jobs));
		}
//		System.out.println(Arrays.toString(indexArray));
//		System.out.println(Arrays.toString(jobArray));
		return jobArray;
	}
	
	public static int[] normalizeArray(int[] val, int machines, int jobs){
		int[] normalized = new int[machines*jobs];
		for (int i = 0; i < val.length; i++) {
			normalized[i] = Math.floorDiv(val[i], machines);
		}
		return normalized;
	}
	
	public static void shuffleArray(int[] array)
	{
	    int index, temp;
	    Random random = new Random();
	    for (int i = array.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	        temp = array[index];
	        array[index] = array[i];
	        array[i] = temp;
	    }
	}
}
