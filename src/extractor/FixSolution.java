package extractor;

import java.util.*;
import java.io.*;

/**
 * Transforma a solucao de G' na solucao de G
 * Executar main();
 * 
 * @author iacer
 *
 */
public class FixSolution {
	/**
	 * Map between G' and G
	 */
	public static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
	
	/**
	 * 
	 * @param argv
	 */
    public static void main(String argv[]) {
        //FixSolution f = new FixSolution(2, false);
    	
    	FixSolution f = new FixSolution();
    	int idProjeto = 2;
    	//String filePath = "/home/iacer/workspace/FLPM2_cplex/"+
    	//		"10 - solutions_arbitrary_errors_BPR_M8_ODError_180/";
    	String filePath = "/home/iacer/workspace/FLPM2_cplex/"+
    			"2 - solutions_errors_100%_BPR_M8_ODError_150/";
    	
    	for (int i=0; i<1; i++)
    	{
    		//String inputFileName = "ODMatrix_" + idProjeto + "_solution_" + (i+1) + ".dat";
    		//String outputFileName = "New_ODMatrix_" + idProjeto + "_solution_" + (i+1) + ".dat";
    		String inputFileName = (i+1) + " - Rotas_" + idProjeto + "_solution_" + (i+1) + ".dat";
    		String outputFileName = (i+1) + " - New_Rotas_" + idProjeto + "_solution_" + (i+1) + ".dat";
    		
    		f.carregaMap(idProjeto);
    		//System.out.println(map);
    		//f.fixRoutes(2, filePath);
    		f.fixRoutes(filePath, inputFileName, outputFileName);
    		//f.fixMatrixOD(filePath, inputFileName, outputFileName);
    		//f.fixMatrixOD(idProjeto, (i+1), "ODMatrix_" + idProjeto, filePath);
    	}
    	
    	/*
    	String filePath = "/home/iacer/workspace/FLPM2_cplex/";
    	FixSolution f = new FixSolution();
    	f.carregaMap(1);
    	f.fixRoutes(1, filePath);
    	*/
    }
    
    /**
     * Constructor
     */
    public FixSolution() {}
    
    /**
     * Constructor
     * 
     * @param idProjeto
     * @param withRoutes
     */
    public FixSolution(int idProjeto, boolean withRoutes, String filePath) {
        carregaMap(idProjeto);
        fixMatrixOD(idProjeto, filePath);
        if (withRoutes)
        	fixRoutes(idProjeto, filePath);
    }
    
    /**
     * 
     * @param idProjeto
     */
    public void fixRoutes(int idProjeto, String filePath) {
        PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String line, cost;
        String newFileName = "New_Rotas_"+idProjeto+"_output.dat";
        String oldFileName = "Rotas_"+idProjeto+"_output.dat";
        
        // Create the writer and reader to the input and output files
        try {
            outWriter = new PrintWriter( new FileWriter(filePath + newFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+newFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            inReader = new BufferedReader( new FileReader(filePath + oldFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+oldFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        try {
            System.out.println("Corrigindo rotas...");
            String finalEntry = "";
            
            while ((line = inReader.readLine()) != null) {
                
                StringTokenizer st = new StringTokenizer(line);
                String entry = "";
                
                entry += st.nextToken()+" ";
                
                line = st.nextToken();
                String b = "";
                int inicio;
                int fim = 1;
                boolean corrige = true;
                
                ArrayList <Integer> addedNodes = new ArrayList <Integer>();
                
                while (fim < line.length()) {
                    inicio = fim;
                    while (line.charAt(fim) != ')' && line.charAt(fim) != '_') {
                        fim++;
                    }
                    b = line.substring(inicio, fim);
                    
                    // Add the new node, if still not added
                    if (!addedNodes.contains( map.get(Integer.parseInt(b))) )
                    {
                    	addedNodes.add(map.get(Integer.parseInt(b)));
                    	if (corrige) {
                            //outWriter.printf("(%d", map.get(Integer.parseInt(b)));
                        	entry += "("+map.get(Integer.parseInt(b));
                            corrige = false;
                        }
                        else
                        {
                            //outWriter.printf("_%d", map.get(Integer.parseInt(b)));
                        	entry += "_"+map.get(Integer.parseInt(b));
                        }
                    }
                    fim++;
                }
                
                String strCost = st.nextToken();
                entry += ") "+strCost+"\n";
                
                // If the cost is strictly positive, add the route to the file
                if ( Double.parseDouble(strCost) > 0 )
                {
                	finalEntry += entry;
                }
                
            }
            
            // Finished
            outWriter.printf(finalEntry);
            
            inReader.close();
            outWriter.close();
            System.out.println("Rotas corrigidas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Reads a file named MatrixOD_2_output.dat and
     * generates a file named New_MatrixOD_2_output.dat.
     * 
     * The file read contains an OD matrix in G', and the
     * file generated contains the correspondent OD matrix in G.
     * 
     * @param idProjeto
     */
    public void fixMatrixOD(int idProjeto, String filePath) {
        PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String line, cost;
        String newFileName = "New_MatrixOD_"+idProjeto+"_output.dat";
        String oldFileName = "MatrixOD_"+idProjeto+"_output.dat";
        
        // Create the reader and writer to the input and output files
        try {
            outWriter = new PrintWriter( new FileWriter(filePath + newFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+newFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
        try {
            inReader = new BufferedReader( new FileReader(filePath + oldFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+oldFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            System.out.println("Corrigindo matriz OD...");
            
            // The OD pairs that have already been added
    		HashMap <String, String> usedOD = new HashMap <String, String> ();
    		
            while ((line = inReader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                line = st.nextToken();
                String b = "";
                String e = "";
                String c = "";
                int x = 1;
                int y;
                while (line.charAt(x) != ',') {
                    x++;
                }
                b = line.substring(1, x);
                x += 1;
                y = x;
                while (line.charAt(y) != ',') {
                    y++;
                }
                e = line.substring(x, y);
                y += 1;
                c = line.substring(y, line.length() - 1);
                
                // The b is the departure node
                // The e is the arrival node
                // The c is the estimated demand between the nodes
                String entry = "(" + map.get(Integer.parseInt(b)) +
        		"," + map.get(Integer.parseInt(e)) + "," + c + ")";
                
                // Only add the positive demands
            	if (Double.parseDouble (c) > 0)
            	{
            		// If the entry has already been added, update its cost
                    // Otherwise, add it
                    if (!usedOD.containsKey(entry))
                    {
                    	// Add the entry in the HashMap in the format (1,2,100)=100
                    	// key: (FROM_NUMBER,TO_NUMBER,FLOW_VALUE)
                    	// value: FLOW_VALUE
                    	usedOD.put(entry, c);
                    } else
                    {
                    	// Update the flow in the entry
                    	//String oldEntry = usedOD.get(entry);
                    	String oldEntry = entry;
                    	
                    	// The index 2 has the OD demand value
                    	String[] splittedString = oldEntry.split(",");
                    	int index = oldEntry.lastIndexOf(splittedString[2]);
                    	
                    	String firstSubstring, lastSubstring;
                    	// The string until the cost
                    	firstSubstring = oldEntry.substring(0, index-1);
                    	// The string from the cost on, without the last parenthesis
                    	lastSubstring = oldEntry.substring(index, oldEntry.length()-2);
                    	
                    	double newCost = Double.parseDouble (lastSubstring) + Double.parseDouble (c);
                    	
                    	String newEntry = firstSubstring + newCost + ")";
                    	
                    	// Replace the entry
                    	usedOD.put(entry, newEntry);
                    }
            	}
                
                /*
                outWriter.println(
                		"(" +
                		map.get(Integer.parseInt(b)) +
                		"," +
                		map.get(Integer.parseInt(e)) +
                		"," + c + ")"
                );
                */
            }
            
            // For each entry in the usedOD HashMap,
            // add it as a line to the file with the outWriter
            Iterator itUsedOD = usedOD.entrySet().iterator();
            while (itUsedOD.hasNext())
            {
            	Map.Entry entry = (Map.Entry) itUsedOD.next();
            	outWriter.println(entry.getKey()); // Add it
            }
            
            inReader.close();
            outWriter.close();
            System.out.println("Matriz OD corrigida!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 
     * @param idProjeto
     */
    public void carregaMap(int idProjeto)
    {
        BufferedReader inReader = null;
        String line;
        Scanner sc;
        String fileName = "Map_"+idProjeto+"_output.dat";
        
        
        // Create the reader to the input file
        try {
            inReader = new BufferedReader( new FileReader(fileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+fileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            System.out.println("Carregando mapa...");
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
                //System.out.println("Chaves (" + s + "," + n + ") e (" + e + "," + n + ") inseridas no map!");
            }
            while ((line = inReader.readLine()) != null) {
                sc = new Scanner(line);
                map.put(sc.nextInt(), sc.nextInt());
                //System.out.println("Chave (" + line + ") inserida no map!");
            }
            System.out.println("Mapa carregado com sucesso!");
            inReader.close();
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 
     * @param nodeInGLine
     * @return
     */
    public Integer getCorrespondentNodeInG(Integer nodeInGLine) {
    	return map.get(nodeInGLine);
    }
    
    /**
     * 
     * @param filePath
     * @param inputFileName
     * @param outputFileName
     */
    public void fixRoutes(String filePath, String inputFileName, String outputFileName) {
        PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String line, cost;
        
        System.out.println(inputFileName);
        System.out.println(outputFileName);
        
        // Create the input reader and the output writer
        try {
            outWriter = new PrintWriter( new FileWriter(filePath + outputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + outputFileName + " nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            inReader = new BufferedReader( new FileReader(filePath + inputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + inputFileName + " nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        try {
            System.out.println("Corrigindo rotas...");
            String finalEntry = "";
            
            HashMap <String, Double> addedRoutes = new HashMap <String, Double>();
            
            while ((line = inReader.readLine()) != null) {
            	String routeId = "";
                String routeNodes = "";
                String routeValue = "";
                
                routeId = line.split(" ")[0];
                routeNodes = line.split(" ")[1];
                routeValue = line.split(" ")[2];
                
                String finalOutputtedNodes = "(";//routeId + " (";
                
                String[] nodesArray = routeNodes.split("_");
                
                //System.out.println(nodesArray[ nodesArray.length-1 ]);
                
                //System.out.println("nodesArray[ nodesArray.length-1 ]: "+
                //		nodesArray[ nodesArray.length-1 ]);
                
                nodesArray[ nodesArray.length-1 ] = nodesArray[ nodesArray.length-1 ].substring(
                		0,
                		nodesArray[ nodesArray.length-1 ].length()-1
                );
                
                //System.out.println(nodesArray[ nodesArray.length-1 ]);
                
                int numberOfNodes = nodesArray.length;
                
                // Iterate the nodes in the route,
                // beginning from the second
                for (int i=1; i<numberOfNodes; i++)
                {
                	// If the node is even, skip it
                	if ( ((i/2)%2 != 1) && i!=numberOfNodes-1) continue;
                	
                	String nextNodeNumber = nodesArray[i];
                	finalOutputtedNodes += map.get(
                			Integer.parseInt(nextNodeNumber)
                	)+"_";
                }
                
                finalOutputtedNodes = finalOutputtedNodes.substring(
                		0,
                		finalOutputtedNodes.length()-1
                );
                
                finalOutputtedNodes += ")";
                
                
                
                //System.out.println(finalOutputtedNodes);
                
                
                
                // If the route hasn't yet been added, do it
                if (!addedRoutes.containsKey((String)finalOutputtedNodes))
                {
                	addedRoutes.put(finalOutputtedNodes, Double.parseDouble(routeValue) );
                } else
                {
                	// If the route has already been added,
                	// sum its flow to the previous value
                	double temp  = addedRoutes.get((String)finalOutputtedNodes);
                	addedRoutes.put(
                			finalOutputtedNodes,
                			temp + Double.parseDouble(routeValue)
                	);
                }
                
                //finalOutputtedNodes += " "+routeValue+"\n";
                //
                // If the cost is strictly positive, add the route to the file
                //if ( Double.parseDouble(routeValue) > 0 )
                //{
                //	finalEntry += finalOutputtedNodes;
                //}
            }
            
            // Instead of using the route id,
            // use the counter to number the routes 
            int counter = 1;
            
            // For each route in the addedRoutes map
            Iterator itAddedRoutes = addedRoutes.entrySet().iterator();
            while (itAddedRoutes.hasNext())
            {
            	Map.Entry entry = (Map.Entry) itAddedRoutes.next();
            	
            	String nodes = (String)entry.getKey();
            	double value = (Double)entry.getValue();
            	
            	// If the route flow is strictly positive, add it to the route file
            	if (value>0)
            		outWriter.printf(counter++ + " " + nodes+" "+value+"\n");
            }
            
            // Finished
            //outWriter.printf(finalEntry);
            
            inReader.close();
            outWriter.close();
            System.out.println("Rotas corrigidas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void fixMatrixOD(String filePath, String inputFileName, String outputFileName)
    {
    	PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String line, cost;
        
        // Create the reader and writer to the input and output files
        try {
            outWriter = new PrintWriter( new FileWriter(filePath + outputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+outputFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
        try {
            inReader = new BufferedReader( new FileReader(filePath + inputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+inputFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            System.out.println("Corrigindo matriz OD...");
            
            // The OD pairs that have already been added
    		HashMap <String, String> usedOD = new HashMap <String, String> ();
    		
            while ((line = inReader.readLine()) != null && !line.isEmpty()) {
            	//System.out.println(line);
            	
            	String b = "";
                String e = "";
                String c = "";
                b = line.split(",")[0].trim().replace("(", "");
                e = line.split(",")[1].trim();
                c = line.split(",")[2].trim().replace(")", "");
                
                // The b is the departure node
                // The e is the arrival node
                // The c is the estimated demand between the nodes
                String entry = "(" + map.get(Integer.parseInt(b)) +
        		"," + map.get(Integer.parseInt(e)) + "," + c + ")";
                
                // Only add the positive demands
            	if (Double.parseDouble (c) > 0)
            	{
            		// If the entry has already been added, update its cost
                    // Otherwise, add it
                    if (!usedOD.containsKey(entry))
                    {
                    	// Add the entry in the HashMap in the format (1,2,100)=100
                    	// key: (FROM_NUMBER,TO_NUMBER,FLOW_VALUE)
                    	// value: FLOW_VALUE
                    	usedOD.put(entry, c);
                    } else
                    {
                    	// Update the flow in the entry
                    	//String oldEntry = usedOD.get(entry);
                    	String oldEntry = entry;
                    	
                    	// The index 2 has the OD demand value
                    	String[] splittedString = oldEntry.split(",");
                    	int index = oldEntry.lastIndexOf(splittedString[2]);
                    	
                    	String firstSubstring, lastSubstring;
                    	// The string until the cost
                    	firstSubstring = oldEntry.substring(0, index-1);
                    	// The string from the cost on, without the last parenthesis
                    	lastSubstring = oldEntry.substring(index, oldEntry.length()-2);
                    	
                    	double newCost = Double.parseDouble (lastSubstring) + Double.parseDouble (c);
                    	
                    	String newEntry = firstSubstring + newCost + ")";
                    	
                    	// Replace the entry
                    	usedOD.put(entry, newEntry);
                    }
            	}
            }
            
            // For each entry in the usedOD HashMap,
            // add it as a line to the file with the outWriter
            Iterator itUsedOD = usedOD.entrySet().iterator();
            while (itUsedOD.hasNext())
            {
            	Map.Entry entry = (Map.Entry) itUsedOD.next();
            	outWriter.println(entry.getKey()); // Add it
            }
            
            inReader.close();
            outWriter.close();
            System.out.println("Matriz OD corrigida!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Reads a file with the name given by the parameter completeFileName
     * and generates a file named New_MatrixOD_2_output.dat.
     * 
     * The file read contains an OD matrix in G', and the
     * file generated contains the correspondent OD matrix in G.
     * 
     * @param idProjeto
     * @param solutionNumber
     * @param fileName
     */
    /*
    public void fixMatrixOD(int idProjeto, int solutionNumber, String fileName, String filePath) {
    	PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String line, cost;
        //String outputFileName = "New_ODMatrix_"+idProjeto+"_solution_"+solutionNumber+".dat";
        String outputFileName = "New_MatrixOD_"+idProjeto+"_solution_"+solutionNumber+".dat";
        
        try {
            outWriter = new PrintWriter( new FileWriter(filePath + outputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println( "O arquivo "+filePath+outputFileName+" nao pode ser lido." );
            System.exit(1);
        } catch (IOException e) {
            System.err.println( e.getMessage() );
            System.exit(1);
        }
        try {
            inReader = new BufferedReader(
                    new FileReader( filePath + fileName + "_solution_" + solutionNumber + ".dat" )
            );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + filePath + fileName + "_solution_" +
            		solutionNumber + ".dat nao pode ser lido.");
            System.exit(1);
        }
        try {
            System.out.println("Corrigindo matriz OD...");
            
            // The OD pairs that have already been added
    		HashMap <String, String> usedOD = new HashMap <String, String> ();
    		
            while ((line = inReader.readLine()) != null && !line.isEmpty()) {
                //StringTokenizer st = new StringTokenizer(line);
                //line = st.nextToken();
            	
            	System.out.println(line);
            	
            	String b = "";
                String e = "";
                String c = "";
                b = line.split(",")[0].trim().replace("(", "");
                e = line.split(",")[1].trim();
                c = line.split(",")[2].trim().replace(")", "");
                
                // The b is the departure node
                // The e is the arrival node
                // The c is the estimated demand between the nodes
                String entry = "(" + map.get(Integer.parseInt(b)) +
        		"," + map.get(Integer.parseInt(e)) + "," + c + ")";
                
                // Only add the positive demands
            	if (Double.parseDouble (c) > 0)
            	{
            		// If the entry has already been added, update its cost
                    // Otherwise, add it
                    if (!usedOD.containsKey(entry))
                    {
                    	// Add the entry in the HashMap in the format (1,2,100)=100
                    	// key: (FROM_NUMBER,TO_NUMBER,FLOW_VALUE)
                    	// value: FLOW_VALUE
                    	usedOD.put(entry, c);
                    } else
                    {
                    	// Update the flow in the entry
                    	//String oldEntry = usedOD.get(entry);
                    	String oldEntry = entry;
                    	
                    	// The index 2 has the OD demand value
                    	String[] splittedString = oldEntry.split(",");
                    	int index = oldEntry.lastIndexOf(splittedString[2]);
                    	
                    	String firstSubstring, lastSubstring;
                    	// The string until the cost
                    	firstSubstring = oldEntry.substring(0, index-1);
                    	// The string from the cost on, without the last parenthesis
                    	lastSubstring = oldEntry.substring(index, oldEntry.length()-2);
                    	
                    	double newCost = Double.parseDouble (lastSubstring) + Double.parseDouble (c);
                    	
                    	String newEntry = firstSubstring + newCost + ")";
                    	
                    	// Replace the entry
                    	usedOD.put(entry, newEntry);
                    }
            	}
            }
            
            // For each entry in the usedOD HashMap,
            // add it as a line to the file with the outWriter
            Iterator itUsedOD = usedOD.entrySet().iterator();
            while (itUsedOD.hasNext())
            {
            	Map.Entry entry = (Map.Entry) itUsedOD.next();
            	outWriter.println(entry.getKey()); // Add it
            }
            
            inReader.close();
            outWriter.close();
            System.out.println("Matriz OD corrigida!");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    */
    /**
     * Opens an OD matrix file in G' and converts it to
     * an OD matrix in G
     * 
     * @param filePath the path in which the file is available
     * @param inputFileName the input file name (with the OD matrix in G')
     * @param outputFileName the output file name  (with the converted OD matrix in G)
     */
    public void orderODMatrixEstimations(String filePath, String inputFileName, String outputFileName)
	{
		File inputFile = new File(filePath + inputFileName);
		File outputFile = new File(filePath + outputFileName);
		
		// An array containing the final ordered OD estimations
		ArrayList<String> arrayOrderedEntries = new ArrayList<String>();
		
		// A sorted set containing the 'from' entries in the file
		TreeSet<Integer> setFrom = new TreeSet<Integer>();
		
		// A sorted set containing the 'to' entries in the file
		TreeSet<Integer> setTo = new TreeSet<Integer>();
		
		// The map that will hold the final, ordered OD estimations
		LinkedHashMap<Integer, LinkedHashMap<Integer, String>> mapFrom =
			new LinkedHashMap<Integer, LinkedHashMap<Integer, String>>();
		
		// -----------------------------------------------
		// Put all the 'from' and 'to' nodes in arraylists
		// -----------------------------------------------
		
		BufferedReader inReader = null;
        String line;
        try {
            inReader = new BufferedReader( new FileReader(inputFile) );
        } catch (FileNotFoundException e) {
            System.err.println(
            		"O arquivo " + inputFile.getName() + " nao pode ser lido."
            );
            e.printStackTrace();
            System.exit(1);
        }
        try {
        	//int counter = 1;
            while ((line = inReader.readLine()) != null && !line.isEmpty()) {
            	// Parse all the lines as OD estimations
            	String from, to;
            	from = line.split(",")[0].trim().replace("(", "");
            	to = line.split(",")[1].trim();
            	
            	// Add the 'from' node to the sorted set
            	setFrom.add( Integer.parseInt(from) );
            	
            	// Add the 'to' node to the sorted set
            	setTo.add( Integer.parseInt(to) );
            	
            	LinkedHashMap<Integer, String> mapTo;
            	
            	if (mapFrom.containsKey(Integer.parseInt(from)))
            	{
            		mapTo = mapFrom.get(Integer.parseInt(from));
            	} else
            	{
            		// Add the entry to the linked hashmap
                	mapTo = new LinkedHashMap<Integer, String>();
            	}
            	
            	// Add the whole line entry to the mapTo linked hashmap
                mapTo.put(Integer.parseInt(to), line);
                
                // Add the mapTo linked hashmap to the mapFrom hashmap
                mapFrom.put(Integer.parseInt(from), mapTo);
            }
            inReader.close();
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
        
        // Iterate through the sorted set with the 'from' nodes
        Iterator itSetFrom = setFrom.iterator();
        while (itSetFrom.hasNext())
        {
        	Integer intFrom = (Integer) itSetFrom.next();
        	
        	//System.out.println(intFrom + " retrieved from the ordered 'from' set.");
        	
        	// Iterate through the sorted set with the 'to' nodes
        	// and retrieve the respective entries in the linked hashmap
        	Iterator itSetTo = setTo.iterator();
            while (itSetTo.hasNext())
            {
            	Integer intTo = (Integer) itSetTo.next();
            	
            	//System.out.println(intTo + " retrieved from the ordered 'to' set.");
            	
            	// Get the destinations added to the 'from' node
            	// to the 'to' node, if it exists
            	LinkedHashMap<Integer, String> mapTo = mapFrom.get(intFrom);
            	Iterator itTo = mapTo.entrySet().iterator();
            	while (itTo.hasNext())
            	{
            		Map.Entry entry = (Map.Entry) itTo.next();
            		Integer intNewTo = (Integer) entry.getKey();
            		String fullLine = null;
            		
            		//System.out.println(intNewTo + " retrieved from the LinkedHashMap.");
            		
            		// If the 'to' node read in the linked hashmap
            		// is the same as the node retrieved from the
            		// sorted set, add the entry to the final ordered array
            		if ((int)intTo == (int)intNewTo)
            		{
            			fullLine = (String) entry.getValue();
            			arrayOrderedEntries.add(fullLine);
            			//System.out.println(fullLine + " added to the final ordered array.");
            		}
            	}
            }
        }
        
        // ----------------------------------------------
        // At this point, the ordered lines are available
        // in the array arrayOrderedEntries
        // ----------------------------------------------
        
        // Delete the output file and recreate an empty one
        outputFile.delete();
        try {
        	outputFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
            System.exit(1);
		}
        
		// Create a writer to the file
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter(new FileWriter(outputFile));
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+outputFileName+" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Add the link estimation entries to the file
		for (int i=0; i<arrayOrderedEntries.size(); i++)
		{
			outWriter.println(arrayOrderedEntries.get(i));
		}
		outWriter.close();
	}
    
    /**
     * Will order the routes lexicographically according to the start node.
     * Order until the third node.
     * 
     * @param filePath
     * @param inputFileName
     * @param outputFileName
     */
    public void orderRoutes(String filePath, String inputFileName, String outputFileName)
	{
		File inputFile = new File(filePath + inputFileName);
		File outputFile = new File(filePath + outputFileName);
		
		// An array containing the final ordered routes
		ArrayList<String> arrayOrderedEntries = new ArrayList<String>();
		
		// A sorted set containing the 'from' entries in the file
		TreeSet<Integer> setFrom = new TreeSet<Integer>();
		
		// A sorted set containing the second 'from' entries in the file
		TreeSet<Integer> setSecond = new TreeSet<Integer>();
		
		// A sorted set containing the 'to' entries in the file
		TreeSet<Integer> setTo = new TreeSet<Integer>();
		
		// The map that will hold the final, ordered OD estimations
		LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>>> mapFrom =
			new LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>>>();
		
		// The map that will hold the final, ordered OD estimations
		// Hack to hold entries with routes with only 2 nodes
		LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>> mapFromTwo =
			new LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>>();
		
		// -----------------------------------------------
		// Put all the 'from' and 'to' nodes in arraylists
		// -----------------------------------------------
		
		BufferedReader inReader = null;
        String line;
        try {
            inReader = new BufferedReader( new FileReader(inputFile) );
        } catch (FileNotFoundException e) {
            System.err.println(
            		"O arquivo " + inputFile.getName() + " nao pode ser lido."
            );
            e.printStackTrace();
			System.exit(1);
        }
        
        try {
            while ((line = inReader.readLine()) != null && !line.isEmpty()) {
            	// Parse all the lines as routes
            	String route, from, to, second = "";
            	route = line.split(" ")[1];
            	int nodesInRoute = route.split("_").length;
            	
            	System.out.println("route: "+route);
            	System.out.println("routeId: "+line.split(" ")[0]);
            	System.out.println("nodesInRoute: "+nodesInRoute);
            	
            	// If route has only two nodes,
            	// the 'to' element will be the 'second' element
            	if (nodesInRoute==2)
            	{
            		from = route.split("_")[0].trim().replace("(", "");
                	to = route.split("_")[ nodesInRoute-1 ].trim().replace(")", "");
            	} else {
            		from = route.split("_")[0].trim().replace("(", "");
                	second = route.split("_")[1].trim();
                	to = route.split("_")[ nodesInRoute-1 ].trim().replace(")", "");
            	}
            	
            	// Add the 'from' node to the sorted set
            	setFrom.add( Integer.parseInt(from) );
            	
            	// Add the 'to' node to the sorted set
            	setTo.add( Integer.parseInt(to) );
            	
            	// If route has only two nodes,
            	// the 'to' element will be the 'second' element
            	if (nodesInRoute>2)
            	{
            		// Add the second node to the sorted set
                	setSecond.add ( Integer.parseInt(second) );
                	
                	LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>> mapSecond;
                	if (mapFrom.containsKey(Integer.parseInt(from)))
                	{
                		mapSecond = mapFrom.get(Integer.parseInt(from));
                	} else
                	{
                		// Add the entry to the linked hashmap
                    	mapSecond = new LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>>();
                	}
                	
                	LinkedHashMap<Integer, ArrayList<String>> mapTo;
                	if (mapSecond.containsKey(Integer.parseInt(second)))
                	{
                		mapTo = mapSecond.get(Integer.parseInt(second));
                	} else
                	{
                		// Add the entry to the linked hashmap
                    	mapTo = new LinkedHashMap<Integer, ArrayList<String>>();
                	}
                	
                	// Add the whole line entry to the mapTo linked hashmap
            		ArrayList<String> arrayEntries = mapTo.get(Integer.parseInt(to));
            		if (arrayEntries == null)
            			arrayEntries = new ArrayList<String>();
            		
            		arrayEntries.add( line );
                    mapTo.put(Integer.parseInt(to), arrayEntries);
                    
                    // Nest the hashmaps one into the other
                    mapSecond.put(Integer.parseInt(second), mapTo);
                    mapFrom.put(Integer.parseInt(from), mapSecond);
            	} else {
            		
            		System.out.println("nodesInRoute: "+nodesInRoute);
            		System.out.println("route: "+route);
            		
                	LinkedHashMap<Integer, ArrayList<String>> mapTo;
                	if (mapFromTwo.containsKey(Integer.parseInt(to)))
                	{
                		mapTo = mapFromTwo.get(Integer.parseInt(to));
                	} else
                	{
                		// Add the entry to the linked hashmap
                    	mapTo = new LinkedHashMap<Integer, ArrayList<String>>();
                	}
                	
                	// Add the whole line entry to the mapTo linked hashmap
            		ArrayList<String> arrayEntries = mapTo.get(Integer.parseInt(to));
            		if (arrayEntries == null)
            			arrayEntries = new ArrayList<String>();
            		
            		arrayEntries.add( line );
                    mapTo.put(Integer.parseInt(to), arrayEntries);
                    
                    // Nest the hashmaps one into the other
                    mapFromTwo.put(Integer.parseInt(from), mapTo);
                    
                    System.out.println("from: "+from);
                    System.out.println("to: "+to);
            	}
            }
            inReader.close();
        } catch (IOException e) {
        	e.printStackTrace();
			System.exit(1);
        }
        
        // Iterate through the sorted set with the 'from' nodes
        Iterator itSetFrom = setFrom.iterator();
        while (itSetFrom.hasNext())
        {
        	Integer intFrom = (Integer) itSetFrom.next();
        	
        	// ----------------------------------------
        	// First add the routes with two nodes only
        	// ----------------------------------------
        	
        	// Iterate through the sorted set with the 'to' nodes
        	// and retrieve the respective entries in the linked hashmap
        	Iterator itSetTo = setTo.iterator();
            while (itSetTo.hasNext())
            {
            	Integer intTo = (Integer) itSetTo.next();
            	
            	LinkedHashMap<Integer, ArrayList<String>> mapTo = mapFromTwo.get((int)intFrom);
            	
            	if (mapTo == null || !mapTo.containsKey((int)intTo))
            			continue;
            	
            	// Iterate through the mapTo entries
            	Iterator itTo = mapTo.entrySet().iterator();
            	while (itTo.hasNext())
            	{
            		Map.Entry entry = (Map.Entry) itTo.next();
            		ArrayList<String> arrayNewTo = (ArrayList<String>) entry.getValue();
            		
            		for (String strNewEntry : arrayNewTo)
            		{
            			//Parse the entry from the String
            			String newRoute;
                    	newRoute = strNewEntry.split(" ")[1];
                    	int nodesInRoute = newRoute.split("_").length;
                    	int intNewTo = Integer.parseInt(
                    			newRoute.split("_")[ nodesInRoute-1 ].trim().replace(")", "")
                    	);
                    	
                		String fullLine = null;
                		
                		if ((int)intTo == (int)intNewTo)
                		{
                			fullLine = strNewEntry;
                			arrayOrderedEntries.add(fullLine);
                			System.out.println(fullLine + " added to the final ordered array.");
                		}
            		}
            	}
            }
            
            // ----------------------------------------
        	// Now add the other routes
        	// ----------------------------------------
        	
        	// Iterate through the sorted set with the second 'from' nodes
        	// and retrieve the respective entries in the linked hashmap
        	Iterator itSetSecond = setSecond.iterator();
            while (itSetSecond.hasNext())
            {
            	Integer intSecond = (Integer) itSetSecond.next();
            	
            	// Obtain the hashmap with the second nodes
            	LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<String>>>
    					mapSecond = mapFrom.get(intFrom);
            	
            	// If there is no entry with the second node under check, continue
            	if (mapSecond == null || mapSecond.isEmpty())
            		continue;
            	
            	// Iterate through the sorted set with the 'to' nodes
            	// and retrieve the respective entries in the linked hashmap
            	//Iterator itSetTo = setTo.iterator();
            	itSetTo = setTo.iterator();
                while (itSetTo.hasNext())
                {
                	Integer intTo = (Integer) itSetTo.next();
                	
                	LinkedHashMap<Integer, ArrayList<String>> mapTo = mapSecond.get(intSecond);
                	
                	if (mapTo == null || !mapTo.containsKey((int)intTo))
                			continue;
                	
                	// Iterate through the mapTo entries
                	Iterator itTo = mapTo.entrySet().iterator();
                	while (itTo.hasNext())
                	{
                		Map.Entry entry = (Map.Entry) itTo.next();
                		ArrayList<String> arrayNewTo = (ArrayList<String>) entry.getValue();
                		
                		for (String strNewEntry : arrayNewTo)
                		{
                			//Parse the entry from the String
                			String newRoute;
                        	newRoute = strNewEntry.split(" ")[1];
                        	int nodesInRoute = newRoute.split("_").length;
                        	int intNewTo = Integer.parseInt(
                        			newRoute.split("_")[ nodesInRoute-1 ].trim().replace(")", "")
                        	);
                        	
                    		String fullLine = null;
                    		
                    		if ((int)intTo == (int)intNewTo)
                    		{
                    			//fullLine = (String) entry.getValue();
                    			fullLine = strNewEntry;
                    			arrayOrderedEntries.add(fullLine);
                    			System.out.println(fullLine + " added to the final ordered array.");
                    		}
                		}
                		
                	}
                }
            }
        }
        
        // ----------------------------------------------
        // At this point, the ordered lines are available
        // in the array arrayOrderedEntries
        // ----------------------------------------------
        
        // Delete the output file and recreate an empty one
        outputFile.delete();
        try {
        	outputFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
        
		// Create a writer to the file
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter(new FileWriter(outputFile));
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+outputFileName+" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Add the route entries to the file
		for (int i=0; i<arrayOrderedEntries.size(); i++)
		{
			outWriter.println(arrayOrderedEntries.get(i));
		}
		outWriter.close();
	}
    
    /**
     * Opens a link counts file in G' and converts it to
     * link counts in G
     * 
     * @param filePath the path in which the file is available
     * @param inputFileName the input file name (with the link counts in G')
     * @param outputFileName the output file name (with the link counts in G)
     */
    public void orderLinkEstimations(String filePath, String inputFileName, String outputFileName)
	{
		File inputFile = new File(filePath + inputFileName);
		File outputFile;
		
		// An array containing the final ordered link estimations
		ArrayList<String> arrayOrderedEntries = new ArrayList<String>();
		
		// A sorted set containing the 'from' entries in the file
		TreeSet<Integer> setFrom = new TreeSet<Integer>();
		
		// A sorted set containing the 'to' entries in the file
		TreeSet<Integer> setTo = new TreeSet<Integer>();
		
		// The map that will hold the final, ordered link estimations
		LinkedHashMap<Integer, LinkedHashMap<Integer, String>> mapFrom =
			new LinkedHashMap<Integer, LinkedHashMap<Integer, String>>();
		
		// -----------------------------------------------
		// Put all the 'from' and 'to' nodes in arraylists
		// -----------------------------------------------
		
		String prefix = "";
		BufferedReader inReader = null;
        String line;
        try {
            inReader = new BufferedReader( new FileReader(inputFile) );
        } catch (FileNotFoundException e) {
            System.err.println(
            		"O arquivo " + inputFile.getName() + " nao pode ser lido."
            );
			e.printStackTrace();
            System.exit(1);
        }
        try {
        	int counter = 1;
            while ((line = inReader.readLine()) != null && !line.isEmpty()) {
            	
            	//System.out.println("line: "+line);
            	
            	// If any of the first three lines are the ones read,
            	// add them to the prefix variable and move on
            	if (counter <= 3)
            	{
            		prefix += line + "\n";
            		counter++;
            		continue;
            	}
            	
            	// Parse all the other lines as link estimations
            	String from, to;//, estimation;
            	from = line.split("->")[0].trim().replace("(", "");
            	to = line.split("->")[1].split(":")[0].trim();
            	//estimation = line.split(" ")[1].trim().replace(")", "");
            	
            	// Add the 'from' node to the sorted set
            	setFrom.add( Integer.parseInt(from) );
            	
            	// Add the 'to' node to the sorted set
            	setTo.add( Integer.parseInt(to) );
            	
            	LinkedHashMap<Integer, String> mapTo;
            	
            	if (mapFrom.containsKey(Integer.parseInt(from)))
            	{
            		mapTo = mapFrom.get(Integer.parseInt(from));
            	} else
            	{
            		// Add the entry to the linked hashmap
                	mapTo = new LinkedHashMap<Integer, String>();
            	}
            	
            	// Add the whole line entry to the mapTo linked hashmap
                mapTo.put(Integer.parseInt(to), line);
                
                // Add the mapTo linked hashmap to the mapFrom hashmap
                mapFrom.put(Integer.parseInt(from), mapTo);
            }
            inReader.close();
        } catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
        }
        
        // Iterate through the sorted set with the 'from' nodes
        Iterator itSetFrom = setFrom.iterator();
        while (itSetFrom.hasNext())
        {
        	Integer intFrom = (Integer) itSetFrom.next();
        	
        	//System.out.println(intFrom + " retrieved from the ordered 'from' set.");
        	
        	// Iterate through the sorted set with the 'to' nodes
        	// and retrieve the respective entries in the linked hashmap
        	Iterator itSetTo = setTo.iterator();
            while (itSetTo.hasNext())
            {
            	Integer intTo = (Integer) itSetTo.next();
            	
            	//System.out.println(intTo + " retrieved from the ordered 'to' set.");
            	
            	// Get the destinations added to the 'from' node
            	// to the 'to' node, if it exists
            	LinkedHashMap<Integer, String> mapTo = mapFrom.get(intFrom);
            	Iterator itTo = mapTo.entrySet().iterator();
            	while (itTo.hasNext())
            	{
            		Map.Entry entry = (Map.Entry) itTo.next();
            		Integer intNewTo = (Integer) entry.getKey();
            		String fullLine = null;
            		
            		//System.out.println(intNewTo + " retrieved from the LinkedHashMap.");
            		
            		// If the 'to' node read in the linked hashmap
            		// is the same as the node retrieved from the
            		// sorted set, add the entry to the final ordered array
            		if ((int)intTo == (int)intNewTo)
            		{
            			fullLine = (String) entry.getValue();
            			arrayOrderedEntries.add(fullLine);
            			//System.out.println(fullLine + " added to the final ordered array.");
            		}
            	}
            }
        }
        
        // ----------------------------------------------
        // At this point, the ordered lines are available
        // in the array arrayOrderedEntries
        // ----------------------------------------------
        
		outputFile = new File(filePath + outputFileName);
		
        // Delete the output file and recreate an empty one
		outputFile.delete();
        try {
        	outputFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
        
		// Create a writer to the output file
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter( new FileWriter(outputFile) );
		} catch (FileNotFoundException e) {
			System.err.println("O arquivo "+outputFileName+" nao pode ser lido.");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Add the three first lines back to the file
		outWriter.print(prefix);
		
		// Add the link estimation entries to the file
		for (int i=0; i<arrayOrderedEntries.size(); i++)
		{
			outWriter.println(arrayOrderedEntries.get(i));
		}
		outWriter.close();
	}
    
    /**
     * Converts link counts estimated to G' to link counts
     * estimated to G.
     * 
     * @param filePath the path in which the input and output files are available
     * @param inputFileName the input file name (with the link counts estimated to G')
     * @param outputFileName the output file name (with the converted link counts to G)
     */
    public void convertLinkEstimationsInGToGLine(String filePath, String inputFileName, String outputFileName)
	{
    	if (map==null)
    		this.carregaMap(2);
		
		File inputFile = new File(filePath + inputFileName);
		File outputFile = new File(filePath + outputFileName);
		
		// If the output file already exists, delete it and recreate it from scratch
		outputFile.delete();
		try {
			outputFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		HashMap<String, Double> odPairs = new HashMap<String, Double>();
		
		// Create a writer to the output file and
		// a reader to the input file
		PrintWriter outWriter = null;
		BufferedReader inReader = null;
		String prefix = "";
        String line;
        try {
            inReader = new BufferedReader( new FileReader(inputFile) );
            outWriter = new PrintWriter( new FileWriter(outputFile) );
        } catch (FileNotFoundException e) {
            System.err.println(
            		"O arquivo " + inputFile.getName() + " nao pode ser lido."
            );
			e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
        try {
        	int counter = 1;
            while ((line = inReader.readLine()) != null && !line.isEmpty()) {
            	
            	//System.out.println("line: "+line);
            	
            	// If any of the first three lines are the ones read,
            	// add them to the prefix variable and move on
            	if (counter <= 3)
            	{
            		prefix += line + "\n";
            		counter++;
            		continue;
            	}
            	
            	// Parse all the other lines as link estimations
            	String from, to, estimation;
            	from = line.split("->")[0].trim().replace("(", "");
            	to = line.split("->")[1].split(":")[0].trim();
            	estimation = line.split("->")[1].split(":")[1].trim().replace(")", "");
            	
            	Integer newFrom, newTo;
            	
            	// Convert the data between G and G'
            	newFrom = this.getCorrespondentNodeInG(
            			Integer.parseInt(from)
            	);
            	
            	newTo = this.getCorrespondentNodeInG(
            			Integer.parseInt(to)
            	);
            	
            	//System.out.println("newFrom: "+newFrom);
            	//System.out.println("newTo: "+newTo);
            	
            	Double oldEstimation = 0.0;
            	
            	// Skip those lines
            	if ((int)newFrom == (int)newTo) continue;
            	
            	// Get the old estimation to the link, if so
            	//if ( !odPairs.containsKey(newFrom+","+newTo) )
            	//{
            	//	odPairs.put(
            	//			newFrom+","+newTo,
            	//			Double.parseDouble(estimation)
            	//	);
            	//} else
            	//{
            	//	oldEstimation = odPairs.get(newFrom+","+newTo);
            	//}
            	
            	oldEstimation += Double.parseDouble(estimation);
            	
            	// Update the "oldEstimation" value in the ODPairs object
            	odPairs.put(newFrom+","+newTo, oldEstimation);
            }
            inReader.close();
            
            String output = "";
            // Iterate through the ODPairs object and generate the output
            Iterator itODPairs = odPairs.entrySet().iterator();
            while (itODPairs.hasNext())
            {
            	Map.Entry entry = (Map.Entry) itODPairs.next();
            	
            	String from, to;
            	from = ((String)entry.getKey()).split(",")[0].trim();
            	to = ((String)entry.getKey()).split(",")[1].trim();
            	
            	// Add it as a line to the output
            	output += (from + "->" + to + ": "+ entry.getValue() +" \n");
            }
            
            outWriter.print(prefix);
            outWriter.println(output);
            outWriter.close();
        } catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
        }
	}
}
