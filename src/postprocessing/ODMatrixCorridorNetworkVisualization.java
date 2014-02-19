package postprocessing;

import graphviz.GraphViz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.FileFormat;
import org.ujmp.core.exceptions.MatrixException;
import org.ujmp.core.intmatrix.IntMatrix;
import org.ujmp.core.intmatrix.IntMatrix2D;
import org.ujmp.core.intmatrix.factory.IntMatrix2DFactory;
import org.ujmp.gui.io.ExportPDF;

public class ODMatrixCorridorNetworkVisualization
{
	/**
	 * The path to the files with the OD matrices
	 */
	public static String inputFilesPath = "";
	
	/**
	 * The path to the visualization to be drawn
	 */
	public static String outputFilesPath = "";
	
	/**
	 * A hashmap that will contain the OD estimates parsed
	 * from the files with the OD matrices
	 */
	public static HashMap<String, Double> hashODEstimates =
			new HashMap<String, Double>();
	
	/**
	 * The minimum flow amongst all OD estimates
	 * Used to normalize the OD estimates
	 */
	public static Double min = Double.MAX_VALUE;
	
	/**
	 * The maximum flow amongst all OD estimates
	 * Used to normalize the OD estimates
	 */
	public static Double max = Double.MIN_VALUE;
	
	/**
	 * Constructor
	 */
	public ODMatrixCorridorNetworkVisualization()
	{
		init();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		init();
	}
	
	/**
	 * Generate all the visualizations to the Corridor Network
	 */
	public static void init()
	{
		String outputFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		// 50% of link counts available
		
		outputFilesPath = baseFilePath+"CorridorNetwork_50%LinkCounts_NoODTripTable/";
		System.out.println("Before "+outputFilesPath);
		generateGraphFromODMatrixFile();
		/*
		outputFilesPath = baseFilePath+"CorridorNetwork_50%LinkCounts_AlternativeEquilibriumTripTable/";
		System.out.println("Before "+outputFilesPath);
		generateGraphFromODMatrixFile();
		
		outputFilesPath = baseFilePath+"CorridorNetwork_50%LinkCounts_CorrectTripTable/";
		System.out.println("Before "+outputFilesPath);
		generateGraphFromODMatrixFile();
		
		outputFilesPath = baseFilePath+"CorridorNetwork_50%LinkCounts_SmallErrorTripTable/";
		System.out.println("Before "+outputFilesPath);
		generateGraphFromODMatrixFile();
		*/
	}
	
	/**
	 * This method performs the following:
	 * - deletes any already generated OD matrix visualizations to the corridor network;
	 * - obtains the minimum and maximum values for OD estimates to a problem instance;
	 * - generates an image with the visualization to the OD matrices.
	 */
	public static void generateGraphFromODMatrixFile()
	{
		// Delete any already outputted OD matrix visualizations
		File file = new File(outputFilesPath);
		if (file.isDirectory())
		{
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
				// Delete all the outputted OD matrix visualizations
				if (fileList[i].getName().indexOf("outODMatrix")>=0)
					fileList[i].delete();
		}
		
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		
		// Obtain the extreme solutions to the whole problem
		// This allows us to generate OD estimates relative to the problem as a whole
		file = new File(outputFilesPath);
		if (file.isDirectory())
		{
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
			{
				// If it is a file representing a transformed route
				if (fileList[i].getName().indexOf("ODMatrix")>=0)
				{
					// First of all, parse the OD estimates from the
					// OD matrix file and populate the hashODEstimates hashmap
					parseODMatrixFile(fileList[i]);
					
					// Get the minimum and maximum OD estimates values
					getExtremeODEstimateValuesPerProblemInstance();
					
					System.out.println("hashODEstimates: "+hashODEstimates);
					
					//System.out.format("max: %f%n", (double)max);
					//System.out.format("min: %f%n", (double)min);
				}
			}
		}
		
		System.out.format("max: %f%n", (double)max);
		System.out.format("min: %f%n", (double)min);
		
		// Generate the output OD matrix visualizations
		file = new File(outputFilesPath);
		if (file.isDirectory())
		{
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
			{
				// Initialize the defaults
				hashODEstimates = new HashMap<String, Double>();
				
				// If it is a file representing link counts
				if (fileList[i].getName().indexOf("ODMatrix")>=0)
				{
					// Parse the solution number from the link file
					int solutionNumber = Integer.parseInt(
							fileList[i].getName().split(" ")[0]
					);
					String originalFileName = "outODMatrix_"+solutionNumber;
					
					System.out.println("\n\n");
					System.out.println(originalFileName);
					
					// First of all, parse the link counts from the link file
					// and populate the hashLinkCounts hashmap
					parseODMatrixFile(fileList[i]);
					
					// Generate the graph image using graphviz
					generateODMatrixVisualization(originalFileName);
					
					break;
				}
			}
		}
	}
	
	/**
	 * Creates a GraphViz object, parses the graph and
	 * outputs it to a file
	 * 
	 * @param fileName The name of the outputted graph file visualization
	 */
	public static void generateODMatrixVisualization(String fileName)
	{
		System.out.println("hashODEstimates: "+hashODEstimates);
		
		Iterator it, itD;
		int counter;
		
		ArrayList<Integer> origins = new ArrayList<Integer>();
		ArrayList<Integer> destinations = new ArrayList<Integer>();
		
		// Obtain the origin and destination nodes
		it = hashODEstimates.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			int intFrom = Integer.parseInt(
					((String)entry.getKey()).split(",")[0].replace("(","").trim()
			);
			int intTo = Integer.parseInt(
					((String)entry.getKey()).split(",")[1].replace(")","").trim()
			);
			
			if (!origins.contains(intFrom))
				origins.add(intFrom);
			if (!destinations.contains(intTo))
				destinations.add(intTo);
		}
		
		// Generate the OD matrix visualization
		Matrix m1 = MatrixFactory.sparse(origins.size(), destinations.size());
		
		// Iterate through the origins and create the labels in the matrix
		it = origins.iterator();
		counter = 0;
		while (it.hasNext())
		{
			int intOrigin = (Integer) it.next();
			System.out.println("origin "+intOrigin);
			
			m1.setRowLabel(counter, Integer.toString(intOrigin) );
			m1.getRowLabel(counter);
			
			counter++;
		}
		
		// Iterate through the destinations and create the labels in the matrix
		it = destinations.iterator();
		counter = 0;
		while (it.hasNext())
		{
			int intDestination = (Integer) it.next();
			System.out.println("destination "+intDestination);
			
			m1.setColumnLabel(counter, Integer.toString(intDestination) );
			m1.getColumnLabel(counter);
			
			counter++;
		}
		
		// Iterate through the estimates and add the values to the matrix
		for (int i=0; i<origins.size(); i++)
		{
			for (int j=0; j<destinations.size(); j++)
			{
				Double estimate;
				if (hashODEstimates.containsKey(origins.get(i)+","+destinations.get(j)))
				{
					estimate = hashODEstimates.get(origins.get(i)+","+destinations.get(j));
					m1.setAsInt(estimate.intValue(), i, j);
				} else {
					m1.setAsInt(0, i, j);
				}
			}
		}
		
		m1.showGUI();
		/*
		try {
			m1.exportToClipboard(FileFormat.CSV);
			m1.exportToFile( "myFileName.csv" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * Parses the OD matrix file and populates the
	 * hashODEstimates hashmap
	 * 
	 * @param odEstimateFile The file containing the OD matrix to read from
	 */
	public static void parseODMatrixFile(File odEstimateFile)
	{
		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader( new FileReader(odEstimateFile) );
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+odEstimateFile+" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			String line = null;
			int counter = 1;
			while ((line = inReader.readLine()) != null)
			{
				String strFrom, strTo;
				Double dblFlow;
				
				if (counter++ <= 3) continue;
				if (line.isEmpty()) break;
				
				strFrom = line.split(",")[0].replace("(", "").trim();
				strTo = line.split(",")[1].trim();
				dblFlow = Double.parseDouble(
						line.split(",")[2].replace(")", "").trim()
				);
				
				hashODEstimates.put(strFrom+","+strTo, dblFlow);
			}
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtains the minimum and maximum OD estimates
	 * for each OD matrix file
	 */
	public static void getExtremeODEstimateValuesPerODMatrixFile()
	{
		Double tmin = Double.POSITIVE_INFINITY;
		Double tmax = Double.NEGATIVE_INFINITY;
		
		Iterator<Entry<String, Double>> it = hashODEstimates.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) it.next();
			
			Double value = entry.getValue();
			
			if (tmin > value) tmin = value;
			if (tmax < value) tmax = value;
		}
		
		ODMatrixCorridorNetworkVisualization.min = tmin;
		ODMatrixCorridorNetworkVisualization.max = tmax;
	}
	
	/**
	 * Obtains the minimum and maximum OD estimates
	 * for a whole problem instance (usually several OD matrix files)
	 */
	public static void getExtremeODEstimateValuesPerProblemInstance()
	{
		Double tmin = Double.POSITIVE_INFINITY;
		Double tmax = Double.NEGATIVE_INFINITY;
		
		//System.out.println("hashLinkCounts.size(): "+hashLinkCounts.size());
		
		Iterator<Entry<String, Double>> it = hashODEstimates.entrySet().iterator();
		if (hashODEstimates.size()==0)
		{
			tmin = 0d;
			tmax = 0d;
		}
		while (it.hasNext())
		{
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) it.next();
			
			Double value = entry.getValue();
			
			if (tmin > value) tmin = value;
			if (tmax < value) tmax = value;
		}
		
		if (ODMatrixCorridorNetworkVisualization.min > tmin)
			ODMatrixCorridorNetworkVisualization.min = tmin;
		if (ODMatrixCorridorNetworkVisualization.max < tmax)
			ODMatrixCorridorNetworkVisualization.max = tmax;
	}
}
