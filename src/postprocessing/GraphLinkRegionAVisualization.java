package postprocessing;

import graphviz.GraphViz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class GraphLinkRegionAVisualization
{
	/**
	 * The GraphViz-Java binding object
	 */
	public static GraphViz gv;
	
	/**
	 * The path to the files with the link counts
	 */
	public static String linkFilePath = "";
	
	/**
	 * The path to the graph to be drawn
	 */
	public static String filePath = "/home/iacercalixto/workspace/FLPM2_cplex/";
	
	/**
	 * Whether to generate the visualizations using absolute
	 * values as the boundaries to the flows on arcs.
	 * 
	 * If set to true, the values of the variables
	 * absMinBoundary and absMaxBoundary
	 * must be set to a valid range.
	 */
	private static boolean generateVisualizationsWithAbsoluteBoundaries = false;
	
	/**
	 * The path to the directory where to output the
	 * visualizations in case we're generating visualizations
	 * with absolute boundaries
	 */
	private static String absDir = null;
	
	/**
	 * The path to the directory where to output the
	 * visualizations in case we're generating visualizations
	 * with relative boundaries
	 */
	
	private static String relDir = null;
	
	/**
	 * The minimum, absolute boundary to use to flows on arcs
	 */
	private static Double absMinBoundary = null;
	
	/**
	 * The maximum, absolute boundary to use to flows on arcs
	 */
	private static Double absMaxBoundary = null;
	
	/**
	 * A hashmap that will contain the link counts parsed
	 * from the files with the link counts
	 */
	public static HashMap<String, Double> hashLinkCounts =
			new HashMap<String, Double>();
	
	/**
	 * A hashmap that will contain the nodes X,Y parsed
	 * from the file that contains their positions
	 */
	public static HashMap<Integer, String> hashNodesXY =
			new HashMap<Integer, String>();
	
	/**
	 * The minimum flow amongst all link flows
	 * Used to normalize the flows
	 */
	public static Double min = Double.MAX_VALUE;
	
	/**
	 * The maximum flow amongst all link flows
	 * Used to normalize the flows
	 */
	public static Double max = Double.MIN_VALUE;
	
	/**
	 * Constructor
	 */
	public GraphLinkRegionAVisualization() {}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Generate the visualization to the Region A only, without flows
		GraphLinkRegionAVisualization.generateVisualizationNoFlow();
		
		// Generate the visualizations with absolute boundaries
		//GraphLinkRegionAVisualization.generateVisualizationsWithAbsoluteBoundaries = true;
		//GraphLinkRegionAVisualization.absMaxBoundary = 2000d;
		//GraphLinkRegionAVisualization.absMinBoundary = 0d;
		//GraphLinkRegionAVisualization.init();
		
		// Generate visualizations with relative boundaries
		//GraphLinkRegionAVisualization.generateVisualizationsWithAbsoluteBoundaries = false;
		//GraphLinkRegionAVisualization.init();
		
	}
	
	/**
	 * Generate all the visualizations to region A's testbed
	 */
	public static void init()
	{
		String firstTestFilePath, secondTestFilePath, thirdTestFilePath;
		String fourthTestFilePath, fifthTestFilePath, sixthTestFilePath;
		String seventhTestFilePath, eighthTestFilePath;
		String ninethTestFilePath, tenthTestFilePath;
		String eleventhTestFilePath;
		String baseFilePath = "/home/iacercalixto/workspace/FLPM2_cplex/";
		
		parseNodesXY();
		
		// BPR
		/*
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BPR_M3_ODError_1/";
		System.out.println("Before "+firstTestFilePath);
		linkFilePath = firstTestFilePath;
		generateGraphFromLinkFile(false);
		*/
		/*
		//secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150_Maximize/";
		System.out.println("Before "+secondTestFilePath);
		linkFilePath = secondTestFilePath;
		generateGraphFromLinkFile(false);
		*/
		/*
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BPR_M3_ODError_1/";
		System.out.println("Before "+thirdTestFilePath);
		linkFilePath = thirdTestFilePath;
		generateGraphFromLinkFile(false);
		*/
		
		//fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150_Maximize/";
		System.out.println("Before "+fourthTestFilePath);
		linkFilePath = fourthTestFilePath;
		generateGraphFromLinkFile(false);
		
		
		// PET
		/*
		fifthTestFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		System.out.println("Before "+fifthTestFilePath);
		linkFilePath = fifthTestFilePath;
		generateGraphFromLinkFile(false);
		
		sixthTestFilePath = baseFilePath+"6 - solutions_errors_100%_PET_M3_ODError_1/";
		System.out.println("Before "+sixthTestFilePath);
		linkFilePath = sixthTestFilePath;
		generateGraphFromLinkFile(false);
		
		seventhTestFilePath = baseFilePath+"7 - solutions_optimal_errors_PET_M8_ODError_150/";
		System.out.println("Before "+seventhTestFilePath);
		linkFilePath = seventhTestFilePath;
		generateGraphFromLinkFile(false);
		
		eighthTestFilePath = baseFilePath+"8 - solutions_optimal_errors_PET_M3_ODError_1/";
		System.out.println("Before "+eighthTestFilePath);
		linkFilePath = eighthTestFilePath;
		generateGraphFromLinkFile(false);
		
		// UBPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_UBPR_M3_ODError_1/";
		System.out.println("Before "+firstTestFilePath);
		linkFilePath = firstTestFilePath;
		generateGraphFromLinkFile(false);
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_UBPR_M8_ODError_150/";
		System.out.println("Before "+secondTestFilePath);
		linkFilePath = secondTestFilePath;
		generateGraphFromLinkFile(false);
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_UBPR_M3_ODError_1/";
		System.out.println("Before "+thirdTestFilePath);
		linkFilePath = thirdTestFilePath;
		generateGraphFromLinkFile(false);
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_UBPR_M8_ODError_150/";
		System.out.println("Before "+fourthTestFilePath);
		linkFilePath = fourthTestFilePath;
		generateGraphFromLinkFile(false);
		
		// BBPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BBPR_M3_ODError_1/";
		System.out.println("Before "+firstTestFilePath);
		linkFilePath = firstTestFilePath;
		generateGraphFromLinkFile(false);
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BBPR_M8_ODError_150/";
		System.out.println("Before "+secondTestFilePath);
		linkFilePath = secondTestFilePath;
		generateGraphFromLinkFile(false);
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BBPR_M3_ODError_1/";
		System.out.println("Before "+thirdTestFilePath);
		linkFilePath = thirdTestFilePath;
		generateGraphFromLinkFile(false);
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BBPR_M8_ODError_150/";
		System.out.println("Before "+fourthTestFilePath);
		linkFilePath = fourthTestFilePath;
		generateGraphFromLinkFile(false);
		*/
		/*
		// more PET tests
		ninethTestFilePath = baseFilePath+"9 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		linkFilePath = ninethTestFilePath;
		generateGraphFromLinkFile(false);
		*/
		/*
		tenthTestFilePath = baseFilePath+"10 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		linkFilePath = tenthTestFilePath;
		generateGraphFromLinkFile(false);
		*/
		/*
		// BPR again
		eleventhTestFilePath = baseFilePath+"11 - errors-mapped-with-gradient-function_BPR_M8/";
		linkFilePath = eleventhTestFilePath;
		generateGraphFromLinkFile(false);
		*/
	}
	
	/**
	 * Generate a visualization with only the graph drawing,
	 * not any details on edges sizes and/or weights
	 */
	public static void generateVisualizationNoFlow()
	{
		String outputFilePath;
		String baseFilePath = "/home/iacercalixto/workspace/FLPM2_cplex/";
		
		parseNodesXY();
		
		// 50% of link counts available
		outputFilePath = baseFilePath+"10 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		//outputFilePath = baseFilePath+"Region_A_noFlow/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(true);
	}
	
	/**
	 * This method performs the following:
	 * - deletes any already generated link visualizations to region A;
	 * - obtains the minimum and maximum flows to a problem instance;
	 * - generates a graph drawing with the link counts.
	 */
	public static void generateGraphFromLinkFile(boolean visualizationWithoutFlow)
	{
		// Delete any already outputted graphs
		File file = new File(linkFilePath);
		
		if ( !visualizationWithoutFlow )
		{
			if (file.isDirectory())
			{
				// Iterate through the files in the directory
				File[] fileList = file.listFiles();
				for (int i=0; i<fileList.length; i++)
					// Delete all the outputted graphs first
					if (fileList[i].getName().indexOf("outOriginalGraph")>=0)
						fileList[i].delete();
			}
			
			// If the directories absVisualization and relVisualization
			// doesn't exist, create them
			absDir = new String(linkFilePath + "absVisualization/");
			relDir = new String(linkFilePath + "relVisualization/");
			File absDirFile = new File(absDir);
			absDirFile.mkdir();
			File relDirFile = new File(relDir);
			relDirFile.mkdir();
		}
		
		// The minimum and maximum values to the boundaries
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		
		// If we're using relative boundaries to generate the visualizations,
		// obtain the minimum and maximum values.
		if (generateVisualizationsWithAbsoluteBoundaries)
		{
			if (absMaxBoundary == null || absMinBoundary == null)
			{
				try {
					throw new Exception("The absolute minimum and maximum boundaries"+
							" must be set positive values in order to generate the visualizations.");
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			max = absMaxBoundary;
			min = absMinBoundary;
		} else
		{
			// Obtain the extreme solutions to the whole problem
			// This allows us to generate link counts relative to the problem as a whole
			// i.e.: if the generated OD matrix contains only very small numbers, but
			// there is one solution with heavy link counts, these results will be shown
			// relative to these heavy counts
			file = new File(linkFilePath);
			if (file.isDirectory())
			{
				// Iterate through the files in the directory
				File[] fileList = file.listFiles();
				for (int i=0; i<fileList.length; i++)
				{
					// If it is a file representing a transformed route
					if (fileList[i].getName().indexOf("Right_Link_Counts")>=0)
					{
						// First of all, parse the link counts from the link file
						// and populate the hashLinkCounts hashmap
						parseLinkFile(fileList[i]);
						
						// Get the minimum and maximum link count values
						getExtremeLinkCountValuesPerProblemInstance();
						
						//System.out.format("max: %f%n", (double)max);
						//System.out.format("min: %f%n", (double)min);
					}
				}
			}
		}
		
		System.out.format("max: %f%n", (double)max);
		System.out.format("min: %f%n", (double)min);
		
		// Generate the output graphs with the link counts
		file = new File(linkFilePath);
		if (file.isDirectory())
		{
			System.out.println("checking directory "+file.toString());
			
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
			{
				// Initialize the defaults
				hashLinkCounts = new HashMap<String, Double>();
				
				// If it is a file representing a transformed route
				if (fileList[i].getName().indexOf("Right_Link_Counts")>=0)
				{
					// Parse the solution number from the link file
					int solutionNumber = Integer.parseInt(
							fileList[i].getName().split(" ")[0]
					);
					String originalFileName = "outOriginalGraph_"+solutionNumber;
					
					System.out.println("\n\n");
					System.out.println(originalFileName);
					
					// First of all, parse the link counts from the link file
					// and populate the hashLinkCounts hashmap
					parseLinkFile(fileList[i]);
					
					// Get the minimum and maximum link count values
					//getExtremeLinkCountValuesPerLinkFile();
					
					// Generate the graph image using graphviz
					generateOriginalGraph(originalFileName, visualizationWithoutFlow);
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
	public static void generateOriginalGraph(String fileName, boolean visualizationWithoutFlow)
	{
		System.out.println("Been here");
		
		// Parse and output the original graph
		gv = new GraphViz();
		gv.addln(gv.start_graph());
		parseOriginalGraph(visualizationWithoutFlow);
		gv.addln(gv.end_graph());
		//System.out.println(gv.getDotSource());
		String type = "png";
		
		if (!visualizationWithoutFlow)
		{
			if (generateVisualizationsWithAbsoluteBoundaries)
				linkFilePath = absDir;
			else
				linkFilePath = relDir;
		} else {
			linkFilePath =  "/home/iacercalixto/workspace/FLPM2_cplex/";
		}
		
		if (visualizationWithoutFlow)
		{
			fileName = "Region_A";
			//type = "png";
			type = "eps";
		}
		
		System.out.println(linkFilePath + fileName + "." + type);
		
		File out = new File(linkFilePath + fileName + "." + type);
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}
	
	/**
	 * Parses the link file and populates the
	 * hashLinkCounts hashmap
	 * 
	 * @param linkFile The file containing the link counts to read from
	 */
	public static void parseLinkFile(File linkFile)
	{
		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader( new FileReader(linkFile) );
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+linkFile+
					" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			String line = null;
			while ((line = inReader.readLine()) != null)
			{
				String strFrom, strTo;
				Double dblFlow;
				
				strFrom = line.split("->")[0].replace("(", "").trim();
				strTo = line.split(" ")[0].split("->")[1].replace(":", "").trim();
				dblFlow = Double.parseDouble( line.split(" ")[1] );
				
				hashLinkCounts.put(strFrom+","+strTo, dblFlow);
			}
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtains the minimum and maximum link counts
	 * for each link count file
	 */
	public static void getExtremeLinkCountValuesPerLinkFile()
	{
		Double tmin = Double.POSITIVE_INFINITY;
		Double tmax = Double.NEGATIVE_INFINITY;
		
		Iterator<Entry<String, Double>> it = hashLinkCounts.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) it.next();
			
			Double value = entry.getValue();
			
			if (tmin > value) tmin = value;
			if (tmax < value) tmax = value;
		}
		
		GraphLinkRegionAVisualization.min = tmin;
		GraphLinkRegionAVisualization.max = tmax;
	}
	
	/**
	 * Obtains the minimum and maximum link counts
	 * for a whole problem instance (usually several link count files)
	 */
	public static void getExtremeLinkCountValuesPerProblemInstance()
	{
		Double tmin = Double.POSITIVE_INFINITY;
		Double tmax = Double.NEGATIVE_INFINITY;
		
		//System.out.println("hashLinkCounts.size(): "+hashLinkCounts.size());
		
		Iterator<Entry<String, Double>> it = hashLinkCounts.entrySet().iterator();
		if (hashLinkCounts.size()==0)
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
		
		if (GraphLinkRegionAVisualization.min > tmin)
			GraphLinkRegionAVisualization.min = tmin;
		if (GraphLinkRegionAVisualization.max < tmax)
			GraphLinkRegionAVisualization.max = tmax;
	}
	
	public static void parseNodesXY()
	{
		System.out.println("filePath: "+filePath);
		String originalFileName = "NodesXY_2_output.dat";
		File originalFile = new File( filePath + originalFileName );
		
		// Values to use as normalization to xMin, xMax, yMin and yMax
		int intXMin = 0;
		int intXMax = 400;
		int intYMin = 0;
		int intYMax = 650;
		
		long xMin = Long.MAX_VALUE;
		long xMax = Long.MIN_VALUE;
		long yMin = Long.MAX_VALUE;
		long yMax = Long.MIN_VALUE;
		
		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader(
					new FileReader( filePath + originalFileName )
			);
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+filePath+originalFileName+
					" nao pode ser lido.");
			System.exit(1);
		}
		
		// Find the minX, minY, maxX and maxY
		try {
			String line = null;
			while ((line = inReader.readLine()) != null)
			{
				String[] strEntry = line.split(" ");
				int intNode = Integer.parseInt( strEntry[0] );
				Double dblX = Double.parseDouble( strEntry[1] );
				Double dblY = Double.parseDouble( strEntry[2] );
				
				// Set the values positive numbers
				dblX = dblX + 50;
				dblY = dblY + 17;
				
				// Set the values to a better range
				long longX = Double.doubleToLongBits( dblX * 100000000000000l );
				long longY = Double.doubleToLongBits( dblY * 100000000000000l );
				
				// Update the minimum (x,y) and maximum (x,y) values
				if (longX < xMin)
					xMin = longX;
				if (longX > xMax)
					xMax = longX;
				if (longY > yMax)
					yMax = longY;
				if (longY < yMin)
					yMin = longY;
				
				//System.out.println("longY: "+longY);
				//System.out.println("yMin: "+yMin);
			}
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("xMin: "+xMin);
		System.out.println("xMax: "+xMax);
		System.out.println("yMin: "+yMin);
		System.out.println("yMax: "+yMax);
		
		// Reload the input buffer
		try {
			inReader = new BufferedReader(
					new FileReader( filePath + originalFileName )
			);
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+filePath+originalFileName+
					" nao pode ser lido.");
			System.exit(1);
		}
		
		// Generate the normalized entries
		try {
			String line = null;
			while ((line = inReader.readLine()) != null)
			{
				String[] strEntry = line.split(" ");
				int intNode = Integer.parseInt( strEntry[0] );
				Double dblX = Double.parseDouble( strEntry[1] );
				Double dblY = Double.parseDouble( strEntry[2] );
				
				//System.out.println("dblX: "+dblX);
				//System.out.println("dblY: "+dblY);
				
				// Set the values positive numbers
				dblX = dblX + 50;
				dblY = dblY + 17;
				
				//System.out.println("dblX (positive): "+dblX);
				//System.out.println("dblY (positive): "+dblY);
				
				// Set the values to a better range
				long longX = Double.doubleToLongBits( dblX * 100000000000000l );
				long longY = Double.doubleToLongBits( dblY * 100000000000000l );
				
				//System.out.println("longX: "+longX);
				//System.out.println("longY: "+longY);
				
				// Generate the normalized x,y (between 0 and 1)
				double normalizedX = (double)(xMax-longX)/(xMax-xMin);
				double normalizedY = (double)(yMax-longY)/(yMax-yMin);
				
				//System.out.println("normalizedX: "+normalizedX);
				//System.out.println("normalizedY: "+normalizedY);
				
				// Put the values between intXMin, intXMax, intYMin and intYMax
				double finalX = intXMin + (normalizedX * (intXMax-intXMin));
				double finalY = intYMin + (normalizedY * (intYMax-intYMin));
				
				//System.out.println("finalX: "+finalX);
				//System.out.println("finalY: "+finalY);
				
				hashNodesXY.put(intNode, finalX+","+finalY);
				
				//System.out.println(intNode+": "+finalX+","+finalY);
			}
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the original graph and populates
	 * the GraphViz-Java binding object
	 */
	public static void parseOriginalGraph(boolean visualizationWithoutFlow)
	{
		String originalFileName = "Graph_2_output.dat";
		File originalFile = new File( filePath + originalFileName );
		
		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader(
					new FileReader( filePath + originalFileName )
			);
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+filePath+originalFileName+
					" nao pode ser lido.");
			System.exit(1);
		}
		
		System.out.println("hashNodesXY: "+hashNodesXY);
		
		try {
			String line = null;
			int counter = 0;
			
			while ((line = inReader.readLine()) != null)
			{
				counter++;
				if (counter <= 2) continue;
				
				// Parse the nodes
				if (counter==3)
				{
					String[] nodes = line.split(" ");
					String strPos = "";
					for (int i=0; i<nodes.length; i++)
					{
						String strXY = hashNodesXY.get(
								//nodes[i]
								Integer.parseInt(nodes[i])
						);
						
						// Draw the graph with the nodes pinned
						//strPos = "pos="+'"'+strXY+'"';
						strPos = "pos="+'"'+"null"+'"';
						/*
						if (visualizationWithoutFlow)
						{
							if (nodes[i].equalsIgnoreCase("586"))
							{
								
							}
							
							// If it is the node 586, do a workaround
							// to draw a better graph
							//if (nodes[i].equalsIgnoreCase("586"))
							if ( !nodes[i].equalsIgnoreCase("608") &&
									!nodes[i].equalsIgnoreCase("482") &&
									!nodes[i].equalsIgnoreCase("484"))
							{
								strXY = hashNodesXY.get(Integer.parseInt(nodes[i]));
								String tempX = strXY.split(",")[0];
								String tempY = strXY.split(",")[1];
								
								//System.out.println("Old tempX: "+tempX);
								//System.out.println("Old tempY: "+tempY);
								
								tempX = Double.toString(
										Double.parseDouble(tempX) * 4
								);
								tempY = Double.toString(
										Double.parseDouble(tempY) * 4
								);
								
								//System.out.println("New tempX: "+tempX);
								//System.out.println("New tempY: "+tempY);
								
								strPos = "pos="+'"'+tempX+","+tempY+'"';
							}
							
							if (nodes[i].equalsIgnoreCase("608"))
							{
								strPos = "pos="+'"'+'"';
							}
							
							if (nodes[i].equalsIgnoreCase("482"))
							{
								strXY = hashNodesXY.get(Integer.parseInt(nodes[i]));
								String tempX = strXY.split(",")[0];
								String tempY = strXY.split(",")[1];
								
								//System.out.println("Old tempX: "+tempX);
								//System.out.println("Old tempY: "+tempY);
								
								tempX = Double.toString(
										Double.parseDouble(tempX) - 100
								);
								tempY = Double.toString(
										Double.parseDouble(tempY) - 100
								);
								
								strPos = "pos="+'"'+400+","+550+'"';
								
							}
							
							if (nodes[i].equalsIgnoreCase("484"))
							{
								strXY = hashNodesXY.get(Integer.parseInt(nodes[i]));
								String tempX = strXY.split(",")[0];
								String tempY = strXY.split(",")[1];
								
								System.out.println("Old tempX: "+tempX);
								System.out.println("Old tempY: "+tempY);
								
								tempX = Double.toString(
										Double.parseDouble(tempX)
								);
								tempY = Double.toString(
										Double.parseDouble(tempY) - 5
								);
								
								System.out.println("New tempX: "+tempX);
								System.out.println("New tempY: "+tempY);
								
								//strPos = "pos="+'"'+tempX+","+tempY+'"';
								
							}
							
						}
						*/
						gv.addln(nodes[i]+"[pin=true,"+
								strPos+","+
								//"height=0.2"+","+
								//"width=0.4"+
								"];");
					}
					continue;
				}
				
				if (line.equalsIgnoreCase("$"))
					break;
				
				String[] edges = line.split(" ");
				for (int i=0; i<edges.length; i++)
				{
					String from = edges[i].split(",")[0].replace("(", "").trim();
					String to = edges[i].split(",")[1].replace(")", "").trim();
					
					Double dblLinkCount = 0d;
					Double dblOriginalLinkCount;
					// Get the edge count from the link file
					if (hashLinkCounts.containsKey(from+","+to))
							dblLinkCount = hashLinkCounts.get(from+","+to);
					
					dblOriginalLinkCount = dblLinkCount;
					
					// The length and color of the edge must be proportional to the
					// normalized link count value.
					// The bigger, the darker and thicker.
					// The smaller, the clearer and thinner.
					
					// Get a value between one and ten (1-10)
					Integer intNormalizedOneTenLinkCount = (int)((dblLinkCount-min) / (max-min)*9)+1;
					
					// Get a value between zero and one (0.1-1.1) to the color saturation
					Double dblNormalizedZeroOneLinkCount = ((dblLinkCount-min) / (max-min))+0.1;
					
					if (dblNormalizedZeroOneLinkCount.isNaN())
						dblNormalizedZeroOneLinkCount = 0.1;
					
					if (dblNormalizedZeroOneLinkCount<0)
						dblNormalizedZeroOneLinkCount = 0.1d;
					
					dblNormalizedZeroOneLinkCount = 1-dblNormalizedZeroOneLinkCount;
					
					System.out.println("One to ten: "+intNormalizedOneTenLinkCount);
					System.out.println("Zero to one: "+dblNormalizedZeroOneLinkCount);
					
					String len;
					if (from.equalsIgnoreCase("586") && to.equalsIgnoreCase("588"))
						len = "len=1";
					else if (from.equalsIgnoreCase("490") && to.equalsIgnoreCase("586"))
						len = "len=1";
					else if (from.equalsIgnoreCase("586") && to.equalsIgnoreCase("594"))
						len = "len=3";
					else
						len = "len=2";
					
					// Add the edge
					if (visualizationWithoutFlow == true)
					{
						gv.addln(from+" -> "+to+" ["+
								"color="+'"'+"0.000 0.000 0.000"+'"'+","+
								len+
								"]");
					} else
					{
						gv.addln(from+" -> "+to+" [penwidth="+intNormalizedOneTenLinkCount+","+
								//"len=2,"+
								len+
								"color="+'"'+"0.000 0.000 "+dblNormalizedZeroOneLinkCount+""+'"'+","+
								"fontcolor="+'"'+"red"+'"'+","+
								"label="+dblOriginalLinkCount.intValue()+","+
								"labeldistance=2.0"+
								"]");
					}
				}
			}
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return the generateVisualizationsWithAbsoluteBoundaries
	 */
	public static boolean isGenerateVisualizationsWithAbsoluteBoundaries() {
		return generateVisualizationsWithAbsoluteBoundaries;
	}

	/**
	 * @param generateVisualizationsWithAbsoluteBoundaries the generateVisualizationsWithAbsoluteBoundaries to set
	 */
	public static void setGenerateVisualizationsWithAbsoluteBoundaries(
			boolean generateVisualizationsWithAbsoluteBoundaries) {
		GraphLinkRegionAVisualization.generateVisualizationsWithAbsoluteBoundaries =
			generateVisualizationsWithAbsoluteBoundaries;
	}

	/**
	 * @return the absMinBoundary
	 */
	public static Double getAbsMinBoundary() {
		return absMinBoundary;
	}

	/**
	 * @param absMinBoundary the absMinBoundary to set
	 */
	public static void setAbsMinBoundary(Double absMinBoundary) {
		GraphLinkRegionAVisualization.absMinBoundary = absMinBoundary;
	}

	/**
	 * @return the absMaxBoundary
	 */
	public static Double getAbsMaxBoundary() {
		return absMaxBoundary;
	}

	/**
	 * @param absMaxBoundary the absMaxBoundary to set
	 */
	public static void setAbsMaxBoundary(Double absMaxBoundary) {
		GraphLinkRegionAVisualization.absMaxBoundary = absMaxBoundary;
	}
}
