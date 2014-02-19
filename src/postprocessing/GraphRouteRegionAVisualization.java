/**
 * 
 */
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

/**
 * @author iacer
 * 
 * Visualization to generate images with the flows on routes,
 * for every test in the region A testbed
 */
public class GraphRouteRegionAVisualization
{
	/**
	 * A graphviz object responsible for generating the dot file
	 */
	public static GraphViz gv;
	
	/**
	 * The route file path to be used
	 */
	public static String routeFilePath = "";
	
	/**
	 * The path to the file representing the original (G) graph
	 */
	public static String filePath = "/home/iacer/workspace/FLPM2_cplex/";
	
	/**
	 * A hashmap that will contain the nodes X,Y parsed
	 * from the file that contains their positions
	 */
	public static HashMap<Integer, String> hashNodesXY =
			new HashMap<Integer, String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		init();
	}
	
	public GraphRouteRegionAVisualization()
	{
		init();
	}
	
	public static void init()
	{
		String firstTestFilePath, secondTestFilePath, thirdTestFilePath;
		String fourthTestFilePath, fifthTestFilePath, sixthTestFilePath;
		String seventhTestFilePath, eighthTestFilePath, ninethTestFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		parseNodesXY();
		
		// BPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BPR_M3_ODError_1/";
		routeFilePath = firstTestFilePath;
		generateRoutesFromRouteFile();
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		routeFilePath = secondTestFilePath;
		generateRoutesFromRouteFile();
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BPR_M3_ODError_1/";
		routeFilePath = thirdTestFilePath;
		generateRoutesFromRouteFile();
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		routeFilePath = fourthTestFilePath;
		generateRoutesFromRouteFile();
		
		// PET
		
		fifthTestFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		routeFilePath = fifthTestFilePath;
		generateRoutesFromRouteFile();
		
		sixthTestFilePath = baseFilePath+"6 - solutions_errors_100%_PET_M3_ODError_1/";
		routeFilePath = sixthTestFilePath;
		generateRoutesFromRouteFile();
		
		seventhTestFilePath = baseFilePath+"7 - solutions_optimal_errors_PET_M8_ODError_150/";
		routeFilePath = seventhTestFilePath;
		generateRoutesFromRouteFile();
		
		eighthTestFilePath = baseFilePath+"8 - solutions_optimal_errors_PET_M3_ODError_1/";
		routeFilePath = eighthTestFilePath;
		generateRoutesFromRouteFile();
		
		// UBPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_UBPR_M3_ODError_1/";
		routeFilePath = firstTestFilePath;
		generateRoutesFromRouteFile();
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_UBPR_M8_ODError_150/";
		routeFilePath = secondTestFilePath;
		generateRoutesFromRouteFile();
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_UBPR_M3_ODError_1/";
		routeFilePath = thirdTestFilePath;
		generateRoutesFromRouteFile();
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_UBPR_M8_ODError_150/";
		routeFilePath = fourthTestFilePath;
		generateRoutesFromRouteFile();
		
		// BBPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BBPR_M3_ODError_1/";
		routeFilePath = firstTestFilePath;
		generateRoutesFromRouteFile();
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BBPR_M8_ODError_150/";
		routeFilePath = secondTestFilePath;
		generateRoutesFromRouteFile();
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BBPR_M3_ODError_1/";
		routeFilePath = thirdTestFilePath;
		generateRoutesFromRouteFile();
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BBPR_M8_ODError_150/";
		routeFilePath = fourthTestFilePath;
		generateRoutesFromRouteFile();
		
		ninethTestFilePath = baseFilePath+"9 - solutions_arbitrary_errors_BPR_M8_ODError_180/";
		routeFilePath = ninethTestFilePath;
		generateRoutesFromRouteFile();
	}
	
	/**
	 * Generates the routes images for each route
	 * in a route file
	 */
	public static void generateRoutesFromRouteFile()
	{
		File file = new File(routeFilePath);
		if (file.isDirectory())
		{
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
			{
				// Delete all the outputted graphs first
				if (fileList[i].getName().indexOf("outOriginalRouteGraph_")>=0)
					fileList[i].delete();
				if (fileList[i].getName().indexOf("outTransformedRouteGraph_")>=0)
					fileList[i].delete();
			}
		}
		
		file = new File(routeFilePath);
		if (file.isDirectory())
		{
			// Iterate through the files in the directory
			File[] fileList = file.listFiles();
			for (int i=0; i<fileList.length; i++)
			{
				// If it is a file representing a transformed route
				if (fileList[i].getName().indexOf("New_Rotas")>=0)
				{
					if (fileList[i].isDirectory())
						continue;
					
					// Parse the solution number from the route file
					int solutionNumber = Integer.parseInt(
							fileList[i].getName().split(" ")[0]
					);
					
					// For each route in the route file, parse it
					BufferedReader inReader = null;
					try {
						inReader = new BufferedReader( new FileReader(fileList[i]) );
					} catch (FileNotFoundException e) {
						System.err.println("O arquivo "+fileList[i].getName()+
								" nao pode ser lido.");
						e.printStackTrace();
						System.exit(1);
					}
					
					ArrayList<Integer> routesIds = new ArrayList<Integer>();
					
					// Find the route with the specified id
					try {
						String line = null;
						while ((line = inReader.readLine()) != null) {
							String fileRouteId = line.split(" ")[0].trim();
							
							// Add the route id to the route id's array
							routesIds.add( Integer.parseInt(fileRouteId) );
						}
						inReader.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					
					for (int j=0; j<routesIds.size(); j++)
					{
						String routeId = Integer.toString( routesIds.get(j) );
						String originalFileName = "outOriginalRouteGraph_"+routeId;
						generateOriginalGraph(true, routeId, originalFileName, solutionNumber);
						//String transformedFileName = "outTransformedRouteGraph_"+routeId;
						//generateTransformedGraph(true, routeId, transformedFileName, solutionNumber);
					}
				}
			}
		}
	}
	
	/**
	 * Parse the original graph G and generate the dot file.
	 * Use graphviz to output an image from the file using neato.
	 * 
	 * @param withRoute Whether to draw the route with emphasys or not
	 * @param routeId The id of the route being generated
	 * @param fileName The name of the output route image file
	 * @param solutionNumber The solution number
	 */
	public static void generateOriginalGraph(boolean withRoute,
			String routeId, String fileName, int solutionNumber)
	{
		// If the folder to output the routes hasn't still
		// been created, create it
		String tempRouteFilePath = routeFilePath + solutionNumber + "/";
		boolean boolDirCreated = ( new File(tempRouteFilePath).mkdir() );
		
		// Parse and output the original graph
		gv = new GraphViz();
		gv.addln(gv.start_graph());
		parseOriginalGraph(withRoute, routeId, solutionNumber, tempRouteFilePath);
		gv.addln(gv.end_graph());
		System.out.println(gv.getDotSource());
		String type = "gif";
		
		File out = new File(tempRouteFilePath + fileName + "." + type);
		
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}
	
	/**
	 * Parse the transformed graph G' and generate the dot file.
	 * Use graphviz to output an image from the file using neato.
	 * 
	 * @param withRoute Whether to draw the route with emphasys or not
	 * @param routeId The id of the route being generated
	 * @param fileName The name of the output route image file
	 * @param solutionNumber The solution number
	 */
	public static void generateTransformedGraph(boolean withRoute,
			String routeId, String fileName, int solutionNumber)
	{
		// If the folder to output the routes hasn't still
		// been created, create it
		String tempRouteFilePath = routeFilePath + solutionNumber + "/";
		boolean boolDirCreated = ( new File(tempRouteFilePath).mkdir() );
		
		// Parse and output the transformed graph
		gv = new GraphViz();
		gv.addln(gv.start_graph());
		parseTransformedGraph(withRoute, routeId, solutionNumber, tempRouteFilePath);
		gv.addln(gv.end_graph());
		System.out.println(gv.getDotSource());
		String type = "gif";
		
		File out = new File(tempRouteFilePath + fileName + "." + type);
		
		gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
	}
	
	/**
	 * Parse the nodes X,Y coordinates from a file
	 */
	public static void parseNodesXY()
	{
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
			System.err.println("O arquivo "+originalFileName+
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
			System.err.println("O arquivo "+originalFileName+
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
				
				System.out.println("finalX: "+finalX);
				System.out.println("finalY: "+finalY);
				
				hashNodesXY.put(intNode, finalX+","+finalY);
			}
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Parse the original graph G and generate a dot file
	 * based on the graph and the route to use.
	 * 
	 * @param withRoute Whether to draw the route with emphasys or not
	 * @param routeId The id of the route being generated
	 * @param fileName The name of the output route image file
	 * @param solutionNumber The solution number
	 */
	public static void parseOriginalGraph(boolean withRoute, String routeId,
			int solutionNumber, String tempRouteFilePath)
	{
		String originalFileName = "Graph_2_output.dat";
		String originalRouteFileName = solutionNumber+" - New_Rotas_2_solution_"+
				solutionNumber+".dat";
		
		File originalFile = new File( filePath + originalFileName );
		File originalRouteFile = new File( filePath + originalRouteFileName );
		
		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader(
					new FileReader( filePath + originalFileName )
			);
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+originalFileName+
					" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		}
		
		String route = null;
		
		if (withRoute)
		{
			// Read the route file
			BufferedReader inRouteReader = null;
			try {
				inRouteReader = new BufferedReader(
						new FileReader( routeFilePath + originalRouteFileName )
				);
			} catch (FileNotFoundException e) {
				System.err.println("O arquivo "+originalRouteFileName+
						" nao pode ser lido.");
				e.printStackTrace();
				System.exit(1);
			}
			
			// Find the route with the specified id
			try {
				String line = null;
				while ((line = inRouteReader.readLine()) != null) {
					String fileRouteId = line.split(" ")[0].trim();
					
					// If the route under consideration is the route
					// being searched for
					if (fileRouteId.equalsIgnoreCase(routeId))
						route = line.split(" ")[1];
					
				}
				inRouteReader.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try {
			String line = null;
			int counter = 0;
			
			while ((line = inReader.readLine()) != null) {
				counter++;
				if (counter <= 2) continue;
				
				// Parse the nodes
				if (counter==3)
				{
					String[] nodes = line.split(" ");
					String strPos = "";
					for (int i=0; i<nodes.length; i++)
					{
						String strXY = hashNodesXY.get(nodes[i]);
						
						// Draw the graph with the nodes pinned
						strPos = "pos="+'"'+strXY+'"';
						
						gv.addln(nodes[i]+"[pin=true,"+strPos+"];");
					}
					continue;
				}
				
				//counter++;
				//if (counter<=3) continue;
				
				if (line.equalsIgnoreCase("$"))
					break;
				
				String[] edges = line.split(" ");
				System.out.println("line: "+line);
				for (int i=0; i<edges.length; i++)
				{
					String from = edges[i].split(",")[0].replace("(", "").trim();
					String to = edges[i].split(",")[1].replace(")", "").trim();
					
					// Add the edge
					if (route == null)
						gv.addln(from+" -> "+to+" [len=2];");
					else
					{
						// Using a route of reference.
						// Draw it with emphasys.
						String[] strNodes = route.split("_");
						
						// Sanitize string
						strNodes[0] = strNodes[0].replace("(", "");
						strNodes[ strNodes.length-1 ] =
							strNodes[ strNodes.length-1 ].replace(")", "");
						
						String fileEntry = "";
						// Iterate starting from the second node on
						for (int j=1; j<strNodes.length; j++)
						{
							// If the edge under consideration is part of the route,
							// draw it with emphasys
							if (strNodes[j].equalsIgnoreCase( to ) &&
									strNodes[j-1].equalsIgnoreCase( from ))
							{
								fileEntry = from+" -> "+to+" [len=2, style=bold, color=red];";
								break;
							}
						}
						
						if (fileEntry == "")
							fileEntry = from+" -> "+to+" [len=2];";
						
						gv.addln(fileEntry);
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
	 * Parse the transformed graph G' and generate a dot file
	 * based on the graph and the route to use.
	 * 
	 * @param withRoute Whether to draw the route with emphasys or not
	 * @param routeId The id of the route being generated
	 * @param fileName The name of the output route image file
	 * @param solutionNumber The solution number
	 */
	public static void parseTransformedGraph(boolean withRoute,
			String routeId, int solutionNumber, String tempRouteFilePath)
	{
		String transformedFileName = "CompleteInputFile_2_output.dat";
		String originalRouteFileName = solutionNumber+" - Rotas_2_solution_"+
				solutionNumber+".dat";
		
		File transformedFile = new File( filePath + transformedFileName );
		File originalRouteFile = new File( filePath + originalRouteFileName );

		BufferedReader inReader = null;
		try {
			inReader = new BufferedReader(
					new FileReader( filePath + transformedFileName )
			);
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+transformedFileName+
					" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		}
		
		String route = null;
		
		if (withRoute)
		{
			// Read the route file
			BufferedReader inRouteReader = null;
			try {
				inRouteReader = new BufferedReader(
						new FileReader( routeFilePath + originalRouteFileName )
				);
			} catch (FileNotFoundException e) {
				System.err.println("O arquivo "+originalRouteFileName+
						" nao pode ser lido.");
				e.printStackTrace();
				System.exit(1);
			}
			
			// Find the route with the specified id
			try {
				String line = null;
				while ((line = inRouteReader.readLine()) != null) {
					String fileRouteId = line.split(" ")[0].trim();
					
					// If the route under consideration is the route
					// being searched for
					if (fileRouteId.equalsIgnoreCase(routeId))
						route = line.split(" ")[1];
					
				}
				inRouteReader.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try {
			String line = null;
			int counter = 0;
			while ((line = inReader.readLine()) != null) {
				counter++;
				if (counter<=3) continue;
				
				if (line.equalsIgnoreCase("$"))
					break;
				
				String[] edges = line.split(" ");
				for (int i=0; i<edges.length; i++)
				{
					String from = edges[i].split(",")[0].replace("(", "").trim();
					String to = edges[i].split(",")[1].replace(")", "").trim();
					
					// Add the edge
					//gv.addln(from+" -> "+to+" [len=2];");
					
					// Add the edge
					if (route == null)
						gv.addln(from+" -> "+to+" [len=2];");
					else
					{
						// Using a route of reference.
						// Draw it with emphasys.
						String[] strNodes = route.split("_");
						
						// Sanitize string
						strNodes[0] = strNodes[0].replace("(", "");
						strNodes[ strNodes.length-1 ] =
							strNodes[ strNodes.length-1 ].replace(")", "");
						
						String fileEntry = "";
						// Iterate starting from the second node on
						for (int j=1; j<strNodes.length; j++)
						{
							// If the edge under consideration is part of the route,
							// draw it with emphasys
							if (strNodes[j].equalsIgnoreCase( to ) &&
									strNodes[j-1].equalsIgnoreCase( from ))
							{
								fileEntry = from+" -> "+to+" [len=2, style=bold, color=red];";
								break;
							}
						}
						
						if (fileEntry == "")
							fileEntry = from+" -> "+to+" [len=2];";
						
						gv.addln(fileEntry);
					}
				}
			}
			
			inReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
