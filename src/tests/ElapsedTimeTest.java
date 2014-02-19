/**
 * 
 */
package tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author iacer
 * 
 * A class that allows the user to calculate
 * the computational efficiency of a piece of code.
 */
public class ElapsedTimeTest
{
	/**
	 *  The name of the traffic network being tested
	 */
	static private String trafficNetworkName;
	
	/**
	 *  The name of the configuration being tested
	 */
	static private String configurationName;
	
	/**
	 * The file path to which to write the results
	 * of the test being conducted
	 */
	static private String filePath;
	
	/**
	 * The file name to which to write the results
	 * of the test being conducted
	 */
	static private String fileName;
	
	/**
	 * The start time
	 */
	static private double startTime;
	
	/**
	 * The end time
	 */
	static private double endTime;
	
	/**
	 * Initialize some basic configuration
	 */
	public static void init()
	{
		ElapsedTimeTest.filePath = "/home/iacer/workspace/FLPM2_cplex/";
		ElapsedTimeTest.fileName = "TestResults.txt";
	}
	
	/**
	 * Set the start time to the test
	 */
	public static void start()
	{
		ElapsedTimeTest.startTime = System.currentTimeMillis();
	}
	
	/**
	 * Set the end time to the test
	 */
	public static void end()
	{
		ElapsedTimeTest.endTime = System.currentTimeMillis();
	}
	
	/**
	 * Write the results of the test to a file
	 */
	public static void writeResultsToFile()
	{
		FileWriter fstream = null;
		BufferedWriter outWriter = null;
		
		try {
			// Create the output file (a new file)
			File outputFile = new File(
					filePath + fileName
			);
			
			// Create the resource used to go through
			// the original graph outputted by PETGyn
			try {
				fstream = new FileWriter(outputFile, true);
				outWriter = new BufferedWriter(fstream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Write the data to the file
			outWriter.write(trafficNetworkName + " - configuration " + configurationName + ": ");
			
			double elapsedTime = ((ElapsedTimeTest.endTime - ElapsedTimeTest.startTime) / 1000);
			outWriter.write(elapsedTime + " seconds.");
			outWriter.newLine();
			
			// Free the resource
			outWriter.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return the filePath
	 */
	public static String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public static void setFilePath(String filePath) {
		ElapsedTimeTest.filePath = filePath;
	}

	/**
	 * @return the fileName
	 */
	public static String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public static void setFileName(String fileName) {
		ElapsedTimeTest.fileName = fileName;
	}

	/**
	 * @return the configurationName
	 */
	public static String getConfigurationName() {
		return configurationName;
	}

	/**
	 * @param configurationName the configurationName to set
	 */
	public static void setConfigurationName(String configurationName) {
		ElapsedTimeTest.configurationName = configurationName;
	}

	/**
	 * @return the trafficNetworkName
	 */
	public static String getTrafficNetworkName() {
		return trafficNetworkName;
	}

	/**
	 * @param trafficNetworkName the trafficNetworkName to set
	 */
	public static void setTrafficNetworkName(String trafficNetworkName) {
		ElapsedTimeTest.trafficNetworkName = trafficNetworkName;
	}
}
