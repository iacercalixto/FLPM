/**
 * 
 */
package parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;

import odEstimation.LinkCapacities;
import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODMatrix;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import extractor.Conversor;
import extractor.Edge;
import extractor.Extractor;
import extractor.FreeTurn;
import extractor.Pair;

/**
 * A graph parser
 * 
 * @author Iacer
 */
public class GraphParser implements IGraphParser {
	private DataInputStream dis = null;
	
	private HashMap<String, String> strVertices = null;
	
	private LinkCounts linkCounts = null;
	private ODCounts odCounts = null;
	private RouteCosts routeCosts = null;
	private LinkCosts linkCosts = null;
	private ODPairs odPairs = null;
	private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = null;
	
	private Conversor conversor = null;
	
	/**
	 * Variables to be used with PETGyn's parser
	 */
	//private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> gPet = null;
	//private LinkCosts linkCostsPet = null;
	//private FreeTurn freeArcsPet = null;
	
	/**
	 * Whether to use PETGyn to cycle or not
	 */
	private boolean boolUsePet = false;
	
	/**
	 * Constructor
	 */
	public GraphParser() {
	}
	
	public void parse(File file) {
		// Create the graph
		DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> problem =
			new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class
			);

		LinkCounts lc = new LinkCounts();
		ODCounts od = new ODCounts();
		RouteCosts rc;
		rc = new RouteCosts();
		LinkCosts lCosts = new LinkCosts();
		ODPairs odPairs = new ODPairs();
		HashMap<String, String> localStrVertices = new HashMap<String, String>();

		String record = null;
		int recCount = 0;
		int lastLines = 0;
		int costRoute = -1;

		if (!file.canRead()) {
			System.err
					.println("Problemas ao abrir o arquivo " + file.getName());
			return;
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			int verticesNumber = 0;
			int edgesNumber = 0;
			String[] pieces;

			while ((record = dis.readLine()) != null) {
				recCount++;
				String ln = record.trim();

				// Is it the line marking the OD pairs to come?
				if (ln.length() == 1 && ln.startsWith("$")) {
					lastLines = -5;
					continue;
				}

				// Is it the line marking the origin-only demands
				// or the destination-only demands?
				if (ln.length() == 1 && ln.startsWith("&")) {
					lastLines = -15;
					continue;
				}

				// Read the line containing the OD pairs in the problem
				if (lastLines == -5) {
					// array containing the OD pairs
					pieces = ln.split(" ");

					for (int i = 0; i < pieces.length; i++) {
						String strOdPair = pieces[i];

						String[] fromTo = strOdPair.split(",");

						// System.out.println("fromTo.length: " +
						// fromTo.length);
						if (fromTo.length > 1) {
							fromTo[0].replace("(", "").trim();
							fromTo[1].replace(")", "").trim();

							odPairs.add(fromTo[0], fromTo[1]);
						}
					}
					lastLines = 0;
					continue;
				}

				// Read the line containing the origin-only and destination-only
				// demands in the problem
				if (lastLines == -15 || lastLines == -25) {
					// array containing the origin-only or destination-only
					// demands
					pieces = ln.split(" ");
					// System.out.println("pieces.length: " + pieces.length);

					for (int i = 0; i < pieces.length; i++) {
						String strOriginOrDestinationCount = pieces[i];
						String[] fromOrTo = strOriginOrDestinationCount
								.split(",");

						if (fromOrTo.length > 1) {
							fromOrTo[0] = fromOrTo[0].replace("(", "").trim();
							fromOrTo[1] = fromOrTo[1].trim();
							fromOrTo[2] = fromOrTo[2].trim();
							fromOrTo[3] = fromOrTo[3].replace(")", "").trim();

							// System.out.println(fromOrTo[0] + ", " +
							// fromOrTo[1] + ", " + fromOrTo[2] + ", " +
							// fromOrTo[3]);

							if (lastLines == -15) {
								// origin counts
								od.setOriginCount(fromOrTo[0], fromOrTo[1],
										Double.parseDouble(fromOrTo[2]), Double
												.parseDouble(fromOrTo[3]));
							} else // if (lastLines == -25)
							{
								// destination counts
								od.setDestinationCount(fromOrTo[0],
										fromOrTo[1], Double
												.parseDouble(fromOrTo[2]),
										Double.parseDouble(fromOrTo[3]));
							}
						}
					}

					if (lastLines == -15)
						lastLines = -25;
					else if (lastLines == -25)
						lastLines = -6;
					continue;
				}

				// Is it the line marking the next two (OD counts and link
				// counts) to come?
				if (ln.length() == 1 && ln.startsWith("#")) {
					lastLines = 1;
					continue;
				}

				// Is it the line marking the link/route costs to come?
				if (ln.startsWith("%")) {
					lastLines = 10;
					if (ln.contains("ROUTE"))
						costRoute = 1;
					else if (ln.contains("LINK"))
						costRoute = 0;
					else
						System.out.println("Linha " + recCount + ", %"
								+ ln.substring(1) + " inválido.");
					continue;
				}

				// Line 1: Contains a textual graph description
				if (recCount == 1) {
					// If there is the @PET string, use PETGyn to cycle
					// Use PETGyn to cycle or not?
					if (ln.startsWith("@PET")) {
						boolUsePet = true;
						continue;
					}
				}

				// Line 2: Contains the total quantity of vertices and edges in
				// the graph
				if (recCount == 2) {
					pieces = ln.split(" ");
					verticesNumber = Integer.parseInt(pieces[0]);
					edgesNumber = Integer.parseInt(pieces[1]);
					continue;
				}

				// Line 3: Contains the labels for all the existing vertices
				if (recCount == 3) {
					// skip it
					pieces = ln.split(" ");

					// create vertices
					for (int j = 0; j < pieces.length; j++) {
						problem.addVertex((String) pieces[j]);

						localStrVertices.put(Integer.toString(j),
								(String) pieces[j]);
					}

					System.out.println("O grafo possui "
							+ problem.vertexSet().size() + " vértices.");
					continue;
				}

				// Line 4 until n-2 
				// Contains the graph's edges
				if (recCount >= 4 && lastLines == 0) {
					// If the line contains nothing, skip
					if (ln.trim().length() == 0)
						continue;

					// Edges that are dissident from vertices
					pieces = ln.split(" "); // get all the edges
					for (int j = 1; j <= pieces.length; j++) {
						String[] ve = pieces[j - 1].split(","); // get the
																// dissident and
																// incident
																// vertices
						ve[0] = ve[0].trim().replace("(", "").replace(")", ""); // dissident
																				// from
						ve[1] = ve[1].trim().replace("(", "").replace(")", ""); // incident
																				// to

						String indexVertexFrom = ve[0];
						String indexVertexTo = ve[1];

						// System.out.println(
						// "Aresta " indexVertexFrom + "," + indexVertexTo +
						// " adicionada.");

						// add the edge to the graph
						problem.addEdge(indexVertexFrom, indexVertexTo);
					}

					// Executed when we read the line containing the last edges
					if (recCount == verticesNumber + 3) {
						System.out.println("O grafo possui "
								+ problem.edgeSet().size() + " arestas.");
					}
					continue;
				}

				// Read the two lines in the text file which contains
				// the measured arc flows and the measured OD flows,
				// respectively
				if (lastLines > 0) {
					// edges that are dissident from vertices
					pieces = ln.split(" "); // get all the edges

					if (lastLines == 1) {
						// row containing the measured arcs' counts
						if (!pieces[0].isEmpty()) {
							for (int j = 1; j <= pieces.length; j++) {
								// get the incident and dissident vertices
								String[] ve = pieces[j - 1].split(",");
								ve[0] = ve[0].trim().replace("(", "").replace(
										")", "");
								ve[1] = ve[1].trim().replace("(", "").replace(
										")", "");
								ve[2] = ve[2].trim().replace("(", "").replace(
										")", "");
								ve[3] = ve[3].trim().replace("(", "").replace(
										")", "");
								ve[4] = ve[4].trim().replace("(", "").replace(
										")", "");

								String vertexFrom = ve[0];
								String vertexTo = ve[1];

								// System.out.println("Link count " + ve[2] +
								// " in " +
								// vertexFrom + "->" + vertexTo);

								// Set the counts in the LinkCount object
								lc.setCount(vertexFrom, vertexTo, ve[2], Double
										.parseDouble(ve[3]), Double
										.parseDouble(ve[4]));
							}
						}

						lastLines++; // indicate the last line
					} else if (lastLines == 2) {
						// row containing the measured OD counts

						for (int j = 1; j <= pieces.length; j++) {
							// get the incident and dissident vertices
							String[] ve = pieces[j - 1].split(",");

							// System.out.print(ve[0] + " to " + ve[1] + " ");

							// if there is at least one OD count to add
							if (ve.length > 1) {
								ve[0] = ve[0].trim().replace("(", "").replace(
										")", "");
								ve[1] = ve[1].trim().replace("(", "").replace(
										")", "");
								ve[2] = ve[2].trim().replace("(", "").replace(
										")", "");
								ve[3] = ve[3].trim().replace("(", "").replace(
										")", "");
								ve[4] = ve[4].trim().replace("(", "").replace(
										")", "");

								String vertexFrom = ve[0];
								String vertexTo = ve[1];

								// System.out.println("OD demand " + ve[2] +
								// " from " +
								// vertexFrom + " to " + vertexTo);

								// Set the counts in the ODCount object
								od.setCount(vertexFrom, vertexTo, ve[2], Double
										.parseDouble(ve[3]), Double
										.parseDouble(ve[4]));
							}
						}
					}
				}

				// Process the link costs
				if (costRoute == 0) {
					// links and their costs
					pieces = ln.split(" "); // get the link and its cost

					pieces[1] = pieces[1].trim().replace("(", "").replace(")",
							"");
					String[] fromTo = pieces[1].split(",");

					System.out.println("Link " + fromTo[0] + "->" + fromTo[1]);
					// +
					// " contains cost " + pieces[2]);

					lCosts.setCount(Integer.parseInt(fromTo[0]), Integer
							.parseInt(fromTo[1]), pieces[2]);

					problem.setEdgeWeight(
							problem.getEdge(fromTo[0], fromTo[1]), Double
									.parseDouble(pieces[2]));
				}

				// Process the route costs
				if (costRoute == 1) {
					// routes and their costs
					pieces = ln.split(" "); // get all the route and its cost
					pieces[1] = pieces[1].trim().replace("(", "").replace(")",
							"");

					rc.setCount(pieces[0], pieces[1], pieces[2]);
				}
			}

			// System.out.println(recCount + ": " + record);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// if the file opened okay, make sure we close it
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException ioe) {
				}
			}
		}

		// Update the class variables
		this.strVertices = localStrVertices;
		this.linkCounts = lc;
		this.odCounts = od;
		this.routeCosts = rc;
		this.linkCosts = lCosts;
		this.odPairs = odPairs;
		this.g = problem;
	}

	/**
	 * Parse the links' capacity. Also convert the link nodes
	 * according to the Conversor map.
	 * 
	 * @param file
	 * @return
	 */
	public LinkCapacities parseLinkCapacities(File file)
	{
		String record = null;
		LinkCapacities linkCapacity = new LinkCapacities();
		
		if (conversor==null)
		{
			if (!file.canRead()) {
				System.out
						.println("Problemas ao abrir o arquivo "
								+ file.getName()
								+ ". O nome do arquivo de capacidades dos arcos deve ser o mesmo "
								+ "nome do arquivo com a entrada para o problema seguido de "
								+ "\"_LinkCapacities\"");
				System.exit(-1);
			}
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);

				int verticesNumber = 0;
				int edgesNumber = 0;
				String[] pieces;

				while ((record = dis.readLine()) != null) {
					String ln = record.trim();
					
					pieces = ln.split(" "); // get the link index, code and capacity
					
					pieces[1] = pieces[1].trim().replace("(", "").replace(")", "");
					String[] fromTo = pieces[1].split(",");
					
					//System.out.println("Link " + fromTo[0] + "->" + fromTo[1]
					//		+ " has capacity " + pieces[2]);
					
					// Populate the linkCapacity object
					linkCapacity.setCapacity(
							Integer.parseInt(fromTo[0]),
							Integer.parseInt(fromTo[1]),
							Double.parseDouble(pieces[2]),
							Double.parseDouble(pieces[3])
					);
				}
			}// Catches any error conditions
			catch (IOException e) {
				System.err.println("Unable to read from file");
				System.exit(-1);
			}
		} else {
			if (!file.canRead()) {
				System.out
						.println("Problemas ao abrir o arquivo "
								+ file.getName()
								+ ". O nome do arquivo de capacidades dos arcos deve ser o mesmo "
								+ "nome do arquivo com a entrada para o problema seguido de "
								+ "\"_LinkCapacities\"");
				System.exit(-1);
			}
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);

				int verticesNumber = 0;
				int edgesNumber = 0;
				String[] pieces;

				while ((record = dis.readLine()) != null) {
					String ln = record.trim();
					
					pieces = ln.split(" "); // get the link index, code and capacity
					
					pieces[1] = pieces[1].trim().replace("(", "").replace(")", "");
					String[] fromTo = pieces[1].split(",");
					String from = fromTo[0];
					String to = fromTo[1];
					
					String ffrom = Integer.toString(
							conversor.getCorrespondentExitNodeInGLine( Integer.parseInt(from) )
					);
					String fto = Integer.toString(
							conversor.getCorrespondentEntryNodeInGLine( Integer.parseInt(to) )
					);
					//System.out.println("ffrom: "+ffrom+", fto: "+fto);
					String convertedCapacity = conversor.getCapacityThroughMap(ffrom, fto);
					//System.out.println("convertedCapacity: "+convertedCapacity);
					
					//System.out.println("Link " +
					//		conversor.getCorrespondentExitNodeInGLine( Integer.parseInt(from) ) +
					//		"->" +
					//		conversor.getCorrespondentEntryNodeInGLine( Integer.parseInt(to) )
					//		+ " ("+fromTo[0]+","+fromTo[1]+") has capacity " + pieces[2]);
					
					if (convertedCapacity != null)
					{
						String nfrom = convertedCapacity.split(",")[0].trim().replace("(", "");
						String nto = convertedCapacity.split(",")[1].trim().replace(")", "");
						
						System.out.println("Link ("+ffrom+","+fto+") transformed to "+
								"("+nfrom+","+nto+").");
						
						// Populate the linkCapacity object
						linkCapacity.setCapacity(
								Integer.parseInt( nfrom ),
								Integer.parseInt( nto ),
								Double.parseDouble(pieces[2]),
								Double.parseDouble(pieces[3])
						);
					} else
					{
						//System.out.println("Link ("+ffrom+","+fto+") couldn't be transformed.");
						
						// Populate the linkCapacity object
						linkCapacity.setCapacity(
								conversor.getCorrespondentExitNodeInGLine( Integer.parseInt(from) ),
								conversor.getCorrespondentEntryNodeInGLine( Integer.parseInt(to) ),
								Double.parseDouble(pieces[2]),
								Double.parseDouble(pieces[3])
						);
					}
					
				}
			}// Catches any error conditions
			catch (IOException e) {
				System.err.println("Unable to read from file");
				System.exit(-1);
			}
		}
		
		return linkCapacity;
	}

	public ODMatrix parseODMatrix(File file)
	{
		String record = null;
		ODMatrix odMatrix = new ODMatrix();
		
		// If we're not using PETGyn
		if (conversor==null)
		{
			if (!file.canRead()) {
				System.out
						.println("Problemas ao abrir o arquivo "
								+ file.getName()
								+ ". O nome do arquivo de matriz OD deve ser o mesmo "
								+ "nome do arquivo com a entrada para o problema seguido de "
								+ "\"_ODMatrix\"");
				System.exit(-1);
			}
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);

				String[] pieces;

				// Iterate the file, line by line
				while ((record = dis.readLine()) != null) {
					String ln = record.trim();

					// Get the origin, destination and demand
					pieces = ln.split(",");

					pieces[0] = pieces[0].trim().replace("(", "");
					pieces[2] = pieces[2].trim().replace(")", "");

					System.out.println("OD " + pieces[0] + "->" + pieces[1]
							+ " has demand " + pieces[2]);

					// Populate the odMatrix object
					odMatrix.setCount(
							Integer.toString(
									Integer.parseInt(pieces[0])
							),
							Integer.toString(
									Integer.parseInt(pieces[1])
							),
							Double.parseDouble(pieces[2])
					);
				}
			}// Catches any error conditions
			catch (IOException e) {
				System.err.println("Unable to read from file.");
				System.exit(-1);
			}
		} else {
			if (!file.canRead()) {
				System.out
						.println("Problemas ao abrir o arquivo "
								+ file.getName()
								+ ". O nome do arquivo de matriz OD deve ser o mesmo "
								+ "nome do arquivo com a entrada para o problema seguido de "
								+ "\"_ODMatrix\"");
				System.exit(-1);
			}
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);

				String[] pieces;

				// Iterate the file, line by line
				while ((record = dis.readLine()) != null) {
					String ln = record.trim();

					// Get the origin, destination and demand
					pieces = ln.split(",");

					pieces[0] = pieces[0].trim().replace("(", "");
					pieces[2] = pieces[2].trim().replace(")", "");

					System.out.println("OD " + pieces[0] + "->" + pieces[1]
							+ " has demand " + pieces[2]);

					// Populate the odMatrix object
					odMatrix.setCount(
							Integer.toString(
									conversor.getCorrespondentExitNodeInGLine( Integer.parseInt(pieces[0]) )
							),
							Integer.toString(
									conversor.getCorrespondentEntryNodeInGLine( Integer.parseInt(pieces[1]) )
							),
							Double.parseDouble(pieces[2])
					);
				}
			}// Catches any error conditions
			catch (IOException e) {
				System.err.println("Unable to read from file.");
				System.exit(-1);
			}
		}

		return odMatrix;
	}

	/**
	 * @return the strVertices
	 */
	public HashMap<String, String> getStrVertices() {
		return strVertices;
	}

	/**
	 * @param strVertices
	 *            the strVertices to set
	 */
	public void setStrVertices(HashMap<String, String> strVertices) {
		this.strVertices = strVertices;
	}

	/**
	 * @return the linkCounts
	 */
	public LinkCounts getLinkCounts() {
		return linkCounts;
	}

	/**
	 * @param linkCounts
	 *            the linkCounts to set
	 */
	public void setLinkCounts(LinkCounts linkCounts) {
		this.linkCounts = linkCounts;
	}

	/**
	 * @return the odCounts
	 */
	public ODCounts getOdCounts() {
		return odCounts;
	}

	/**
	 * @param odCounts
	 *            the odCounts to set
	 */
	public void setOdCounts(ODCounts odCounts) {
		this.odCounts = odCounts;
	}

	/**
	 * @return the routeCosts
	 */
	public RouteCosts getRouteCosts() {
		return routeCosts;
	}

	/**
	 * @param routeCosts
	 *            the routeCosts to set
	 */
	public void setRouteCosts(RouteCosts routeCosts) {
		this.routeCosts = routeCosts;
	}

	/**
	 * @return the linkCosts
	 */
	public LinkCosts getLinkCosts() {
		return linkCosts;
	}

	/**
	 * @param linkCosts
	 *            the linkCosts to set
	 */
	public void setLinkCosts(LinkCosts linkCosts) {
		this.linkCosts = linkCosts;
	}

	/**
	 * @return the odPairs
	 */
	public ODPairs getOdPairs() {
		return odPairs;
	}

	/**
	 * @param odPairs
	 *            the odPairs to set
	 */
	public void setOdPairs(ODPairs odPairs) {
		this.odPairs = odPairs;
	}

	/**
	 * @return the g
	 */
	public DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> getG() {
		return g;
	}

	/**
	 * @param g
	 *            the g to set
	 */
	public void setG(DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g) {
		this.g = g;
	}
	
	/**
	 * Creates the file Project_2_output.dat, that will contain the input
	 * file to FLIPSOD. It is expected that there is a file called
	 * Project_2_output_Estimations.dat containing the link counts and O, D and
	 * OD estimates.
	 * 
	 * @param file
	 */
	public File extractDataFromPETGyn() {
		// The extractor reads the PETGyn database and creates:
		// - Graph_2_output.dat with the graph G
		// - ArcosLivres_2_output.dat with the free arcs
		// - CustoNosArcos_2_output.dat with the arc costs.
		Extractor extractor = new Extractor(2, false);
		
		// Converts the graph outputted by PETGyn - just extracted by
		// the method above - to a transformed version
		// It creates the following files:
		// - New_Graph_2_output.dat with the transformed graph G'
		// - New_CustosNosArcos_2_output.dat with the new arc costs
		// - Map_2_output.dat with the mapping between G and G' 
		conversor = new Conversor(2,1);
		
		//TODO
		// Converter Project_2_Estimations.dat para entrada compativel
		// com grafo G' usando objeto FixSolution e Map_2_output.dat
		//convertGEstimationsToGLineEstimations(2);
		
		// Compose the final input file using the transformed version
		// of the graph and the files containing the estimates.
		File file = composeInputFile();
		
		return file;
	}
	
	/**
	 * Transforms the Project_2_output_Estimations.dat file in the
	 * New_Project_2_output_Estimations.dat file using the
	 * Map_2_output.dat file in the process.
	 * 
	 * It uses the FixSolution object in order to do it.
	 * 
	 * @param idProjeto The PETGyn project id
	 */
	/*
	private void convertGEstimationsToGLineEstimations(int idProjeto)
	{
		//TODO
		
		// Create the resources to files New_Project_2_output_Estimations.dat
		// (the one to write to) and the Map_2_output.dat
		// (the one to read from)
		PrintWriter outWriter = null;
        BufferedReader inReader = null;
        BufferedReader inReaderEstimations = null;
        File fileEstimations = null;
        
        try {
            outWriter = new PrintWriter(
                    new FileWriter(
                    		"New_Project_"+idProjeto+"_output_Estimations.dat"
                    )
            );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo New_Project_"+idProjeto+
            		"_output_Estimations.dat nao pode ser lido.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
        	// The reader to the Map_2_output.dat file
            inReader = new BufferedReader(
                    new FileReader("Map_"+idProjeto+"_output.dat")
            );
            
            // The reader to the Project_2_output_Estimations.dat file
            inReaderEstimations = new BufferedReader(
                    new FileReader("Project_"+idProjeto+"_output_Estimations.dat")
            );
            
            // The Project_2_output_Estimations.dat file
            fileEstimations = new File("/home/iacer/workspace/FLPM2_cplex/Project_"+
            		idProjeto+"_output_Estimations.dat");
            
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo Project_"+idProjeto+
            		"_output_Estimations.dat nao pode ser lido.");
            System.exit(1);
        }
        
        // --------------------------------------
        // Load the G->G' map in the map variable
        // --------------------------------------
        
        String line;
        Scanner sc;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        try {
        	// Parser copiado da classe FixSolution, metodo CarregaMap
            System.out.println("Carregando mapa...");
            // Loads the G' nodes that represent the entry
            // and exit nodes in G (named as duplicated nodes)
            while ((line = inReader.readLine()) != null && !line.equals("AUX")) {
                String n = "";
                String s = "";
                String e = "";
                int i = 1;
                int j;
                while (line.charAt(i) != ',') {
                    i++;
                }
                n = line.substring(1, i);
                i++;
                j = i;
                while (line.charAt(i) != ',') {
                    i++;
                }
                s = line.substring(j, i);
                i++;
                e = line.substring(i, line.length() - 1);
                map.put(Integer.parseInt(s), Integer.parseInt(n));
                map.put(Integer.parseInt(e), Integer.parseInt(n));
            }
            
            // This line is "AUX" and divides the duplicated nodes
            // and the nodes in G' that represent edges in G
            while ((line = inReader.readLine()) != null) {
                sc = new Scanner(line);
                map.put(sc.nextInt(), sc.nextInt());
            }
            
            System.out.println("Mapa carregado com sucesso!");
            inReader.close();
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        // --------------------------------------------------
        // Iterate through the estimations file and generate
        // the new estimations to G'
        // --------------------------------------------------
        
		LinkCounts lc = new LinkCounts();
		ODCounts od = new ODCounts();
		ODPairs odPairs = new ODPairs();
		
		String record = null;
		int recCount = 0;
		int lastLines = 0;
		
		if (!fileEstimations.canRead()) {
			System.err.println("Problemas ao abrir o arquivo " +
					fileEstimations.getName());
			return;
		}
		try {
			FileInputStream fis = new FileInputStream(fileEstimations);
			BufferedInputStream bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			int verticesNumber = 0;
			int edgesNumber = 0;
			String[] pieces;
			
			// Write the od pairs first
			outWriter.println("$");
			
			// Iterate through the file
			while ((record = dis.readLine()) != null) {
				recCount++;
				String ln = record.trim();

				// Is it the line marking the OD pairs to come?
				if (ln.length() == 1 && ln.startsWith("$")) {
					lastLines = -5;
					continue;
				}

				// Is it the line marking the origin-only demands
				// or the destination-only demands?
				if (ln.length() == 1 && ln.startsWith("&")) {
					lastLines = -15;
					continue;
				}
				
				// Read the line containing the OD pairs in the problem
				if (lastLines == -5) {
					// array containing the OD pairs
					pieces = ln.split(" ");

					for (int i = 0; i < pieces.length; i++) {
						String strOdPair = pieces[i];
						String[] fromTo = strOdPair.split(",");
						
						if (fromTo.length > 1) {
							fromTo[0].replace("(", "").trim();
							fromTo[1].replace(")", "").trim();
							
							// Convert the OD pair
							//TODO
							map.get(fromTo[0]);
							map.get(fromTo[1]);
							
							// Write the OD pair
							outWriter.print("("+fromTo[0]+","+fromTo[1]+") ");
						}
					}
					
					// Break the line
					outWriter.println();
					
					// Now write the link counts and OD estimates
					outWriter.println("#");
					
					lastLines = 0;
					continue;
				}
				
				// Read the line containing the origin-only and destination-only
				// demands in the problem
				if (lastLines == -15 || lastLines == -25) {
					// array containing the origin-only or destination-only
					// demands
					pieces = ln.split(" ");
					// System.out.println("pieces.length: " + pieces.length);

					for (int i = 0; i < pieces.length; i++) {
						String strOriginOrDestinationCount = pieces[i];
						String[] fromOrTo = strOriginOrDestinationCount
								.split(",");

						if (fromOrTo.length > 1) {
							fromOrTo[0] = fromOrTo[0].replace("(", "").trim();
							fromOrTo[1] = fromOrTo[1].trim();
							fromOrTo[2] = fromOrTo[2].trim();
							fromOrTo[3] = fromOrTo[3].replace(")", "").trim();

							// System.out.println(fromOrTo[0] + ", " +
							// fromOrTo[1] + ", " + fromOrTo[2] + ", " +
							// fromOrTo[3]);

							if (lastLines == -15) {
								// origin counts
								//TODO
								od.setOriginCount(fromOrTo[0], fromOrTo[1],
										Double.parseDouble(fromOrTo[2]), Double
												.parseDouble(fromOrTo[3]));
							} else // if (lastLines == -25)
							{
								// destination counts
								//TODO
								od.setDestinationCount(fromOrTo[0],
										fromOrTo[1], Double
												.parseDouble(fromOrTo[2]),
										Double.parseDouble(fromOrTo[3]));
							}
						}
					}

					if (lastLines == -15)
						lastLines = -25;
					else if (lastLines == -25)
						lastLines = -6;
					continue;
				}
				
				// Is it the line marking the next two (OD counts and link
				// counts) to come?
				if (ln.length() == 1 && ln.startsWith("#")) {
					lastLines = 1;
					continue;
				}
				
				// Read the two lines in the text file which contains
				// the measured arc flows and the measured OD flows,
				// respectively
				if (lastLines > 0) {
					// edges that are dissident from vertices
					pieces = ln.split(" "); // get all the edges

					if (lastLines == 1) {
						// row containing the measured arcs' counts
						if (!pieces[0].isEmpty()) {
							for (int j = 1; j <= pieces.length; j++) {
								// get the incident and dissident vertices
								String[] ve = pieces[j - 1].split(",");
								ve[0] = ve[0].trim().replace("(", "").replace(
										")", "");
								ve[1] = ve[1].trim().replace("(", "").replace(
										")", "");
								ve[2] = ve[2].trim().replace("(", "").replace(
										")", "");
								ve[3] = ve[3].trim().replace("(", "").replace(
										")", "");
								ve[4] = ve[4].trim().replace("(", "").replace(
										")", "");

								String vertexFrom = ve[0];
								String vertexTo = ve[1];

								// System.out.println("Link count " + ve[2] +
								// " in " +
								// vertexFrom + "->" + vertexTo);

								// Set the counts in the LinkCount object
								//TODO
								lc.setCount(vertexFrom, vertexTo, ve[2], Double
										.parseDouble(ve[3]), Double
										.parseDouble(ve[4]));
							}
						}

						lastLines++; // indicate the last line
					} else if (lastLines == 2) {
						// row containing the measured OD counts

						for (int j = 1; j <= pieces.length; j++) {
							// get the incident and dissident vertices
							String[] ve = pieces[j - 1].split(",");

							// System.out.print(ve[0] + " to " + ve[1] + " ");

							// if there is at least one OD count to add
							if (ve.length > 1) {
								ve[0] = ve[0].trim().replace("(", "").replace(
										")", "");
								ve[1] = ve[1].trim().replace("(", "").replace(
										")", "");
								ve[2] = ve[2].trim().replace("(", "").replace(
										")", "");
								ve[3] = ve[3].trim().replace("(", "").replace(
										")", "");
								ve[4] = ve[4].trim().replace("(", "").replace(
										")", "");

								String vertexFrom = ve[0];
								String vertexTo = ve[1];

								// System.out.println("OD demand " + ve[2] +
								// " from " +
								// vertexFrom + " to " + vertexTo);

								// Set the counts in the ODCount object
								//TODO
								od.setCount(vertexFrom, vertexTo, ve[2], Double
										.parseDouble(ve[3]), Double
										.parseDouble(ve[4]));
							}
						}
					}
				}
			}

			// System.out.println(recCount + ": " + record);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// if the file opened okay, make sure we close it
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException ioe) {
				}
			}
		}

		// Update the class variables
		//this.strVertices = localStrVertices;
		this.linkCounts = lc;
		this.odCounts = od;
		//this.routeCosts = rc;
		//this.linkCosts = lCosts;
		this.odPairs = odPairs;
		//this.g = problem;
		
	}
	*/
	/**
	 * Generates the final input file CompleteInputFile_2_output.dat
	 * from the partial files:
	 * 
	 * - Graph_2_output.dat,
	 * - CustoNosArcos_2_output.dat and
	 * - Project_2_output_Estimations.dat
	 */
	private File composeInputFile()
	{
		//TODO
		// Change the file to create the complete input file:
		// - CompleteInputFile_2_output.dat
		// using the following files as inputs:
		// - New_Graph_2_output.dat
		// - New_CustoNosArcos_2_output.dat
		
		// Original (partial) graph file
		String fileGraphName =
			"/home/iacer/workspace/FLPM2_cplex/New_Graph_2_output.dat";
		File fileGraph = new File(fileGraphName);
		
		// Arc costs file
		String fileArcCostsName =
			"/home/iacer/workspace/FLPM2_cplex/New_CustoNosArcos_2_output.dat";
		File fileArcCosts = new File(fileArcCostsName);
		
		// Link counts and OD (and O and D) estimations file
		String fileCompleteInputFileName =
			"/home/iacer/workspace/FLPM2_cplex/CompleteInputFile_2_output.dat";
		File fileCompleteInputFile = new File(fileCompleteInputFileName);
		
		// Create the final (full) input file
		try {
			// If there is already a file corresponding to the complete input file,
			// delete it and create a new one
			fileCompleteInputFile.delete();
			fileCompleteInputFile.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!fileGraph.canRead()) {
			System.out.println("Problemas ao abrir o arquivo " +
					fileGraph.getName() + ".");
			System.exit(-1);
		} else if (!fileArcCosts.canRead()) {
			System.out.println("Problemas ao abrir o arquivo " +
					fileArcCosts.getName() + ".");
			System.exit(-1);
		} else if (!fileCompleteInputFile.canRead()) {
			System.out.println("Problemas ao abrir o arquivo " +
					fileCompleteInputFile.getName() + ".");
			System.exit(-1);
		}
		
		// ----------------------------------------------------------------
		// First of all, create a new file (the complete input file)
		// and append the graph generated by PETGyn to it.
		// ----------------------------------------------------------------
		
		String record = null;
		FileWriter fstream = null;
		BufferedWriter out = null;
		
		// Create the resource used to go through
		// the original graph outputted by PETGyn
		try {
			fstream = new FileWriter(fileCompleteInputFile, true);
			out = new BufferedWriter(fstream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// The graph outputted by PETGyn
			DataInputStream disG = new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(fileGraph)
					)
			);
			
			// First of all, append the @PET directive so that
			// FLIPSOD will know how to cycle properly
			out.write("@PET ");
			
			// Append each line to the final input file
			while ((record = disG.readLine()) != null) {
				out.write(record.trim());
				out.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ----------------------------------------------------------------
		// After appending the graph generated by PETGyn's structure to
		// the complete input file, lets append the arc costs to it as well.
		// ----------------------------------------------------------------
		// We're not appending the estimations, only the arc costs.
		// The estimations are already appended by the Extractor.
		// ----------------------------------------------------------------
		
		try {
			// The estimations file
			//DataInputStream disE = new DataInputStream(
			//		new BufferedInputStream(
			//				new FileInputStream(fileEstimations)
			//		)
			//);
			
			// The arc costs file
			DataInputStream disAC = new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(fileArcCosts)
					)
			);
			
			// Append each line in the estimations file to the final input file
			//while ((record = disE.readLine()) != null)
			//{
			//	out.write(record.trim());
			//	out.newLine();
			//}
			
			// Create the %LINK line in the final input file
			// so the parser will interpret the next entries as link costs
			out.write("%LINK");
			out.newLine();
			
			// Append each line in the arc costs file to the final input file
			while ((record = disAC.readLine()) != null) {
				out.write(record.trim());
				out.newLine();
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		//Close the output stream
		try {
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("CompleteInputFile_2_output.dat has been successively created.");
		
		return fileCompleteInputFile;
	}

	/**
	 * @return the boolUsePet
	 */
	public boolean isBoolUsePet() {
		return boolUsePet;
	}

	/**
	 * @param boolUsePet the boolUsePet to set
	 */
	public void setBoolUsePet(boolean boolUsePet) {
		this.boolUsePet = boolUsePet;
	}
}
