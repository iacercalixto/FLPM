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

public class GraphLinkCorridorNetworkVisualization
{
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
	public static String graphFilePath = "/home/iacer/workspace/FLPM2_cplex/src/"+
			"instances/v7/Corridor Network/Link counts in 67% of arcs/";
	
	/**
	 * A hashmap that will contain the link counts parsed
	 * from the files with the link counts
	 */
	public static HashMap<String, Double> hashLinkCounts =
			new HashMap<String, Double>();
	
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
	public GraphLinkCorridorNetworkVisualization() { }
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Generate the visualization to the corridor network only, without flows
		GraphLinkCorridorNetworkVisualization.generateVisualizationNoFlow();
		/*
		// Generate the visualizations with absolute boundaries
		GraphLinkCorridorNetworkVisualization.generateVisualizationsWithAbsoluteBoundaries = true;
		GraphLinkCorridorNetworkVisualization.absMaxBoundary = 10000d;
		GraphLinkCorridorNetworkVisualization.absMinBoundary = 0d;
		GraphLinkCorridorNetworkVisualization.init();
		
		// Generate visualizations with relative boundaries
		GraphLinkCorridorNetworkVisualization.generateVisualizationsWithAbsoluteBoundaries = false;
		GraphLinkCorridorNetworkVisualization.init();
		*/
	}
	
	/**
	 * Generate all the flow visualizations to the Corridor Network
	 */
	public static void init()
	{
		String outputFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		// 50% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_NoODTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_AlternativeEquilibriumTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_CorrectTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_SmallErrorTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		// 67% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_NoODTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_AlternativeEquilibriumTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_CorrectTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_SmallErrorTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		// 100% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_NoODTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_AlternativeEquilibriumTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_CorrectTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_SmallErrorTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(false);
		
	}
	
	/**
	 * Generate a visualization with only the graph drawing,
	 * not any details on edges sizes and/or weights
	 */
	public static void generateVisualizationNoFlow()
	{
		String outputFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		// 50% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_NoODTripTable/";
		System.out.println("Before "+outputFilePath);
		linkFilePath = outputFilePath;
		generateGraphFromLinkFile(true);
	}
	
	/**
	 * This method performs the following:
	 * - deletes any already generated link visualizations to the corridor network;
	 * - obtains the minimum and maximum flows to a problem instance;
	 * - generates a graph drawing with the link counts.
	 * 
	 * @param visualizationWithoutFlow Whether to generate a visualization with or without the link counts
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
		}
		
		// If the directories absVisualization and relVisualization
		// doesn't exist, create them
		absDir = new String(linkFilePath + "absVisualization/");
		relDir = new String(linkFilePath + "relVisualization/");
		
		File absDirFile = new File(absDir);
		absDirFile.mkdir();
		File relDirFile = new File(relDir);
		relDirFile.mkdir();
		
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
					if (fileList[i].getName().indexOf("LinkCounts")>=0)
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
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
			{
				// Initialize the defaults
				hashLinkCounts = new HashMap<String, Double>();
				
				// If it is a file representing link counts
				if (fileList[i].getName().indexOf("LinkCounts")>=0)
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
		// Parse and output the original graph
		gv = new GraphViz();
		gv.addln(gv.start_graph());
		parseOriginalGraph(visualizationWithoutFlow);
		gv.addln(gv.end_graph());
		//System.out.println(gv.getDotSource());
		String type = "gif";
		
		if (generateVisualizationsWithAbsoluteBoundaries)
			linkFilePath = absDir;
		else
			linkFilePath = relDir;
		
		if (visualizationWithoutFlow)
		{
			fileName = "The_Corridor_Network";
			type = "png";
		}
		
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
			int counter = 1;
			while ((line = inReader.readLine()) != null)
			{
				String strFrom, strTo;
				Double dblFlow;
				
				if (counter++ <= 3) continue;
				if (line.isEmpty()) break;
				
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
		
		GraphLinkCorridorNetworkVisualization.min = tmin;
		GraphLinkCorridorNetworkVisualization.max = tmax;
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
		
		if (GraphLinkCorridorNetworkVisualization.min > tmin)
			GraphLinkCorridorNetworkVisualization.min = tmin;
		if (GraphLinkCorridorNetworkVisualization.max < tmax)
			GraphLinkCorridorNetworkVisualization.max = tmax;
	}
	
	/**
	 * Parses the original graph and populates
	 * the GraphViz-Java binding object
	 */
	public static void parseOriginalGraph(boolean visualizationWithoutFlow)
	{
		String originalFileName = "Corridor Network_LinkCount10%_noTripTable (no OD entries).dat";
		File originalFile = new File( graphFilePath + originalFileName );
		
		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader(
					new FileReader( graphFilePath + originalFileName )
			);
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+originalFileName+
					" nao pode ser lido.");
			System.exit(1);
		}
		
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
					String strODNodesShape = "";
					for (int i=0; i<nodes.length; i++)
					{
						// Draw the graph with the nodes pinned
						if (nodes[i].equalsIgnoreCase("6"))
							strPos = "pos="+'"'+"50,200"+'"';
						if (nodes[i].equalsIgnoreCase("7"))
							strPos = "pos="+'"'+"150,250"+'"';
						if (nodes[i].equalsIgnoreCase("8"))
							strPos = "pos="+'"'+"150,150"+'"';
						if (nodes[i].equalsIgnoreCase("1"))
							strPos = "pos="+'"'+"150,350"+'"';
						if (nodes[i].equalsIgnoreCase("10"))
							strPos = "pos="+'"'+"250,150"+'"';
						if (nodes[i].equalsIgnoreCase("9"))
							strPos = "pos="+'"'+"250,250"+'"';
						if (nodes[i].equalsIgnoreCase("4"))
							strPos = "pos="+'"'+"250,350"+'"';
						if (nodes[i].equalsIgnoreCase("5"))
							strPos = "pos="+'"'+"200,50"+'"';
						if (nodes[i].equalsIgnoreCase("11"))
							strPos = "pos="+'"'+"350,250"+'"';
						if (nodes[i].equalsIgnoreCase("12"))
							strPos = "pos="+'"'+"350,150"+'"';
						if (nodes[i].equalsIgnoreCase("3"))
							strPos = "pos="+'"'+"450,150"+'"';
						if (nodes[i].equalsIgnoreCase("2"))
							strPos = "pos="+'"'+"450,250"+'"';
						
						if (nodes[i].equalsIgnoreCase("6") ||
								nodes[i].equalsIgnoreCase("1") ||
								nodes[i].equalsIgnoreCase("4") ||
								nodes[i].equalsIgnoreCase("2") ||
								nodes[i].equalsIgnoreCase("3") ||
								nodes[i].equalsIgnoreCase("5"))
							strODNodesShape = "shape=doublecircle";
						else
							strODNodesShape = "shape=circle";
						
						gv.addln(nodes[i]+"["+
								"pin=true,"+
								strPos+","+
								strODNodesShape+","+
								"height=0.5"+","+
								"width=0.5"+","+
								"];");
					}
					
					// If drawing the no flow visualization, add the
					// legend
					gv.addln('"'+'"'+"["+
							"pin=true,"+
							"pos="+'"'+"350,50"+'"'+","+
							"shape=doublecircle"+","+
							"height=0.5"+","+
							"width=0.5"+","+
							"label="+'"'+"origens e/ou destinos"+'"'+","+
							"labeldistance=2.0"+
							"fixedsize=true"+
							"];");
					
					continue;
				}
				
				if (line.equalsIgnoreCase("$")) break;
				
				String[] edges = line.split(" ");
				for (int i=0; i<edges.length; i++)
				{
					if (edges[i].isEmpty()) continue;
					
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
					
					// Add the edge
					if (visualizationWithoutFlow == true)
					{
						gv.addln(from+" -> "+to+" ["+
								"color="+'"'+"0.000 0.000 0.000"+'"'+","+
								"fontcolor="+'"'+"red"+'"'+
								"]");
					} else {
						gv.addln(from+" -> "+to+" [penwidth="+intNormalizedOneTenLinkCount+","+
								//"len=1000,"+
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
		GraphLinkCorridorNetworkVisualization.generateVisualizationsWithAbsoluteBoundaries =
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
		GraphLinkCorridorNetworkVisualization.absMinBoundary = absMinBoundary;
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
		GraphLinkCorridorNetworkVisualization.absMaxBoundary = absMaxBoundary;
	}
}
