package extractor;

import java.util.*;
import java.io.*;

import odEstimation.LinkCounts;

import pet_class.Trinca;

/**
 * Converte o grafo G para o grafo G'
 * Executar main();
 * 
 * @author iacer
 *
 */
public class Conversor {
	Graph g = null;
	Graph gline = null;
	
	String odPairs = null;
	String linkEstimations = null;
	String odEstimations = null;
	String oEstimations = null;
	String dEstimations = null;
	
	HashMap<Integer, Pair> map = new HashMap<Integer, Pair>();
	HashMap<String, String> edgesMap = new HashMap<String, String>();
	
	HashMap<String, String> capacitiesMap = new HashMap<String, String>();
	
	public static void main(String argv[]) {
		Conversor c = new Conversor(2,1);
		//Conversor c = new Conversor(1,1);
    }
	
	public Conversor() {}
	
    public Conversor(int idProjeto, int tipoConversao) {
        
        System.out.println("Carregando dados...");
        g = readGraph(idProjeto);
        readEdgeCosts(g, idProjeto);
        LinkedList<FreeTurn> freeTurns = readFreeTurns(idProjeto);
        System.out.println("Dados carregados!");
        System.out.println("Construindo o novo grafo...");
        gline = new Graph();
        gline.setDescription(g.getDescription());
        gline.setNumNodes(g.getNumNodes());
        duplicateNodes(g, gline);
        gline.order();
        
        //System.out.println("Printing the graph String after duplicateNodes(g, gline)...");
        //System.out.println(gline);
        
        handleEdges(g, gline);
        
        //System.out.println("Printing the graph String after handleEdges(g, gline)...");
        //System.out.println(gline);
        
        handleFreeTurns(g, gline, freeTurns);
        
        //System.out.println("Printing the graph String after handleFreeTurns(g, gline, freeTurns)...");
        //System.out.println(gline);
        
        gline.order();
        
        //System.out.println("Printing the graph String after gline.order()...");
        //System.out.println(gline);
        
        geraGrafo(gline, idProjeto, tipoConversao);
        geraCustoNosArcos(gline, idProjeto);
        geraMap(gline, idProjeto);
        
        convertLinkEstimationsInGToGLine(gline, idProjeto);
        
        //System.out.println("edgesMap: "+edgesMap);
    }

    private void geraMap(Graph gline, int idProjeto) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("Map_" + Integer.toString(idProjeto) + "_output.dat")
            );
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            Set<Integer> m = map.keySet();
            List<Integer> n = new LinkedList<Integer>();
            n.addAll(m);
            Collections.sort(n);
            Iterator it = n.iterator();

            for (int i = 0; i < n.size(); i++) {
                line = "";
                int o = (Integer) it.next();
                line = line.concat("(" + o + "," + map.get(o).getFirst() + "," +
                        map.get(o).getLast() + ")");
                outWriter.println(line);
            }
            outWriter.println("AUX");
            for (int i=gline.getNumNodes()*2; i<gline.getNodesSize(); i++) {
                outWriter.println(gline.getNode(i).getNumber() + " " + gline.getNode(i).getFirst());
            }
            outWriter.close();
            System.out.println("Arquivo Map_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void geraCustoNosArcos(Graph gline, int idProjeto) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("New_CustoNosArcos_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            for (int i = 0; i < gline.getEdgesSize(); i++) {
                line = "";
                line = line.concat(Integer.toString(i) + " (" + gline.getEdge(i).getFrom().getNumber()
                        + "," + gline.getEdge(i).getTo().getNumber() + ") " + gline.getEdge(i).getCost());
                outWriter.println(line);
            }
            outWriter.close();
            System.out.println("Arquivo New_CustoNosArcos_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void geraGrafo(Graph gline, int idProjeto, int tipoConversao) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("New_Graph_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            outWriter.println(gline.getDescription());
            outWriter.println(gline.getNodesSize() + " " + gline.getEdgesSize());
            line = "";
            
            // Create the entry containing the nodes
            for (int i = 0; i < gline.getNodesSize(); i++) {
                line = line.concat(
                		Integer.toString(gline.getNode(i).getNumber()) + " "
                );
            }
            outWriter.println(line);
            
            // Create the entry containing the edges
            for (int i = 0; i < gline.getNodesSize(); i++) {
                line = "";
                for (int j = 0; j < gline.getEdgesSize(); j++) {
                	
                	//System.out.println("node number: "+
                	//		gline.getNode(i).getNumber());
                	//System.out.println("edge from number: "+
                	//		gline.getEdge(j).getFrom().getNumber());
                	
                    if (gline.getEdge(j).getFrom().getNumber() == gline.getNode(i).getNumber()) {
                    	
                    	//System.out.println(
                    	//		"(" +
                        //		gline.getEdge(j).getFrom().getNumber() + "," +
                        //		gline.getEdge(j).getTo().getNumber() +
                        //		") "
                    	//);
                    	
                        line = line.concat(
                        		"(" +
                        		gline.getEdge(j).getFrom().getNumber() + "," +
                        		gline.getEdge(j).getTo().getNumber() +
                        		") "
                        );
                    }
                }
                if (!line.equals("")) {
                    outWriter.println(line);
                }
            }
            
            outWriter.println("$");
            
            // Processar os pares OD e converte-los tambem
            int begin, end;
            StringTokenizer st = new StringTokenizer(odPairs);
            while (st.hasMoreTokens()) {
            	line = st.nextToken();
                String b = "";
                String e = "";
                int x = 1;
                int y;
                while (line.charAt(x) != ',') {
                    x++;
                }
                b = line.substring(1, x);
                x++;
                y = x;
                while (line.charAt(x) != ')') {
                    x++;
                }
                e = line.substring(y, x);
                begin = Integer.parseInt(b);
                end = Integer.parseInt(e);
                outWriter.print("(" +
                		map.get(begin).getFirst() + "," +
                		map.get(end).getLast() + ") "
                );
            }
            
            // Quebrar a linha
            outWriter.println();
            
            // ----------------------------------------------
            // Processar as estimacoes e tambem converte-las
            // ----------------------------------------------
            outWriter.println("#");
            
            // Primeiramente, as contagens volumetricas
            String[] entries = linkEstimations.split(" ");
            for (int i=0; i<entries.length; i++)
            {
            	String entry = entries[i].trim();
            	String from, to, estimation, errorAbove, errorBelow;
            	
            	String[] temp = entry.split(",");
            	from = temp[0].replace("(", "").trim();
            	to = temp[1].trim();
            	estimation = temp[2].trim();
            	errorAbove = temp[3].trim();
            	errorBelow = temp[4].replace(")", "").trim();
            	
            	//System.out.println("Original entry: ("+from+","+to+","+
            	//		estimation+","+errorAbove+","+errorBelow+")");
            	//System.out.println("New entry     : ("+
            	//		map.get( Integer.parseInt(from) ).getFirst()+","+
            	//		map.get( Integer.parseInt(to) ).getLast()+","+
            	//		estimation+","+errorAbove+","+errorBelow+")");
            	
            	// Converter os nos de origem e destino atraves
            	// do objeto map
            	outWriter.print("(" +
                		map.get( Integer.parseInt(from) ).getFirst() + "," +
                		map.get( Integer.parseInt(to) ).getLast() + "," +
                		estimation + "," +
                		errorAbove + "," +
                		errorBelow + ") "
                );
            }
            
            // Quebrar a linha
            outWriter.println();
            
            // Em seguida, as estimacoes OD
            entries = odEstimations.split(" ");
            for (int i=0; i<entries.length; i++)
            {
            	String entry = entries[i].trim();
            	String from, to, estimation, errorAbove, errorBelow;
            	
            	String[] temp = entry.split(",");
            	from = temp[0].replace("(", "").trim();
            	to = temp[1].trim();
            	estimation = temp[2].trim();
            	errorAbove = temp[3].trim();
            	errorBelow = temp[4].replace(")", "").trim();
            	
            	//System.out.println("to: "+Integer.parseInt(to));
            	
            	// Converter os nos de origem e destino atraves
            	// do objeto map
            	outWriter.print("(" +
                		map.get( Integer.parseInt(from) ).getFirst() + "," +
                		map.get( Integer.parseInt(to) ).getLast() + "," +
                		estimation + "," +
                		errorAbove + "," +
                		errorBelow + ") "
                );
            }
            
            // Quebrar a linha
            outWriter.println();
            
            outWriter.println("&");
            
            // Em seguida, as estimacoes de origem
            entries = oEstimations.split(" ");
            for (int i=0; i<entries.length; i++)
            {
            	String entry = entries[i].trim();
            	String from, estimation, errorAbove, errorBelow;
            	
            	String[] temp = entry.split(",");
            	from = temp[0].replace("(", "").trim();
            	estimation = temp[1].trim();
            	errorAbove = temp[2].trim();
            	errorBelow = temp[3].replace(")", "").trim();
            	
            	// Converter o no de origem atraves do objeto map
            	outWriter.print("(" +
                		map.get( Integer.parseInt(from) ).getFirst() + "," +
                		estimation + "," +
                		errorAbove + "," +
                		errorBelow + ") "
                );
            }
            
            // Quebrar a linha
            outWriter.println();
            
            // Finalmente, as estimacoes de destino
            entries = dEstimations.split(" ");
            for (int i=0; i<entries.length; i++)
            {
            	String entry = entries[i].trim();
            	String to, estimation, errorAbove, errorBelow;
            	
            	String[] temp = entry.split(",");
            	to = temp[0].replace("(", "").trim();
            	estimation = temp[1].trim();
            	errorAbove = temp[2].trim();
            	errorBelow = temp[3].replace(")", "").trim();
            	
            	// Converter o no de destino atraves do objeto map
            	outWriter.print("(" +
                		map.get( Integer.parseInt(to) ).getLast() + "," +
                		estimation + "," +
                		errorAbove + "," +
                		errorBelow + ") "
                );
            }
            
            outWriter.close();
            System.out.println("Arquivo New_Graph_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
        	e.printStackTrace();
            //System.err.println(e.getMessage());
        }
    }
    
    /**
     * For each free node in G, this method creates:
     * - one edge connecting the edge departure node (created by
     *   the method handleEdges) and the the exploded node in G'
     *   (created with the method duplicateNodes) that is the arrival.
     * 
     * @param g
     * @param gline
     * @param freeTurns
     */
    public void handleFreeTurns(Graph g, Graph gline, LinkedList<FreeTurn> freeTurns) {
        for (int i = 0; i < freeTurns.size(); i++) {
            int j = g.getNodesSize() * 2 - 1;
            int k = g.getNodesSize() * 2 - 1;
            Double cost = -1.0;
            while (
            		gline.getNode(j).getFirst() != freeTurns.get(i).getBegin() ||
            		gline.getNode(j).getSecond() != freeTurns.get(i).getMiddle()
            ) {
                j++;
            }
            while (
            		gline.getNode(k).getFirst() != freeTurns.get(i).getMiddle() ||
            		gline.getNode(k).getSecond() != freeTurns.get(i).getEnd()
            ) {
                k++;
            }
            for (int x = 0; x < g.getEdgesSize(); x++) {
                if (
                		g.getEdge(x).getFrom().getNumber() == freeTurns.get(i).getBegin() &&
                		g.getEdge(x).getTo().getNumber() == freeTurns.get(i).getMiddle()
                ) {
                    cost = g.getEdge(x).getCost();
                    break;
                }
            }
            
            //System.out.println("Free turn ("+j+","+k+")");
            
            // Add an edge in G' that corresponds to the free turn
            gline.addEdge(
            		new Edge(
            				gline.getNode(j+1),
            				gline.getNode(k),
            				cost
            		)
            );
        }
        System.out.println("Os arcos livres foram adicionados com sucesso!");
    }
    
    /**
     * For each edge in G, this method creates:
     * - two nodes in G' (one edge departure node and one edge arrival node);
     * - one edge connecting these two nodes (departure and arrival);
     * - one edge connecting the exploded node in G' (created with the method
     *   duplicateNodes) that is the departure and the edge departure node
     *   that has just been created.
     *   
     * @param g
     * @param gline
     */
    public void handleEdges(Graph g, Graph gline) {
        //System.out.println("Adicionando as arestas do grafo original...");
        int number = gline.getNodesSize();
        for (int i = 0; i < g.getNumEdges(); i++) {
        	// Create the departure node of the edge in G
        	Node nFrom = new Node(
    				-1,
    				//number,
    				number*100,
    				g.getEdge(i).getFrom().getNumber(),
    				g.getEdge(i).getTo().getNumber()
    		);
        	
        	// Create the arrival node of the edge in G
        	Node nTo = new Node(
    				-1,
    				//number*2,
    				number*1000,
    				g.getEdge(i).getFrom().getNumber(),
    				g.getEdge(i).getTo().getNumber()
    		);
        	
        	// Add the node in G' that correspond to the
        	// departure node of the edge in G
            gline.addNode( nFrom );
            
            // Add an edge (in G') that connects the departure node (in G')
        	// to the node (in G') that corresponds to an edge in G
            gline.addEdge(
            		new Edge(
            				gline.getNode(
            						map.get(g.getEdge(i).getFrom().getNumber()).getFirst()
            				),
            				gline.getNode(gline.getNodesSize() - 1),
            				0.0
            		)
            );
            
            // Add the node in G' that correspond to the
        	// arrival node of the edge in G
            gline.addNode( nTo );
            
            // Create the edge in G' that corresponds to
            // the edge in G
            Edge e = new Edge(
    				gline.getNode(gline.getNodesSize() - 2),
    				gline.getNode(gline.getNodesSize() - 1),
    				//0.0
    				g.getEdge(i).getCost()
    		);
            
            //System.out.println(gline.getNode(gline.getNodesSize() - 2).getNumber()+"->"+
            //		gline.getNode(gline.getNodesSize() - 1).getNumber()+" = "+
            //		g.getEdge(i).getCost());
            
            // Add an edge between the two recently added nodes
            gline.addEdge( e );
            
            // Add the new edge to the edges map (between G and G')
            edgesMap.put(
            		e.getFrom().getNumber()+","+e.getTo().getNumber(),
            		g.getEdge(i).getFrom().getNumber()+","+
    				g.getEdge(i).getTo().getNumber()
    		);
            
            //System.out.print("("+ (gline.getNodesSize() - 1) );
            //System.out.print(","+
            //		map.get(g.getEdge(i).getTo().getNumber()).getLast() + "): ");
            //System.out.println(g.getEdge(i).getCost());
            
            //System.out.println("Debug: ("+
            //		map.get(g.getEdge(i).getTo().getNumber()).getFirst()+","+
            //		map.get(g.getEdge(i).getTo().getNumber()).getLast()+")");
            
            // Add an edge in G' that connects the node that corresponds
            // to an edge in G to an arrival node in G'
            gline.addEdge(
            		new Edge(
            				gline.getNode(gline.getNodesSize()-1),
            				//gline.getNode(
            				//		map.get(g.getEdge(i).getFrom().getNumber()).getFirst()
            				//),
            				gline.getNode(
            						map.get(g.getEdge(i).getTo().getNumber()).getLast()
            				),
            				g.getEdge(i).getCost()
            				//0d
            		)
            );
            
            number++;
            
            //System.out.println("Internal: "+gline);
            
        }
        
        if (gline.getEdgesSize() == g.getEdgesSize() * 3) {
            System.out.println("As arestas foram adicionadas com sucesso!");
        } else {
            System.out.println("Ocorreu um erro no processo de adicao!");
        }

    }
    
    /**
     * Duplicates the nodes in G to G'.
     * We create the destination node in G' only if the
     * original node in G is part of at least one OD pair.
     * 
     * @param g
     * @param gline
     * @param map
     */
    public void duplicateNodes(Graph g, Graph gline) {
        //System.out.println("Duplicando nos do grafo original...");
        for (int i = 0; i < g.getNumNodes(); i++) {
        	// Add the correspondent departure node
            gline.addNode(
            		new Node( g.getNode(i).getNumber(), i )
            );
            
            // Add the correspondent arrival node
            gline.addNode(
            		new Node(
            				g.getNode(i).getNumber(),
            				i + g.getNumNodes()
            		)
            );
            
            // Create the entry in the map
            map.put(
            		g.getNode(i).getNumber(),
            		new Pair(i, i + g.getNumNodes())
            );
            
            // The map key is the node number in G
            // The map value is a Pair object with the entry and exit nodes
        }
        if (gline.getNodesSize() == g.getNodesSize() * 2) {
            System.out.println("Os nos foram duplicados com sucesso!");
        } else {
            System.out.println("Ocorreu um erro no processo de duplicacao!");
        }
    }

    public LinkedList<FreeTurn> readFreeTurns(int idProjeto) {
        LinkedList<FreeTurn> ft = new LinkedList<FreeTurn>();
        BufferedReader inReader = null;
        String line;

        try {
            inReader = new BufferedReader(
                    new FileReader(
                    		"ArcosLivres_" + Integer.toString(idProjeto) + "_output.dat"
                    )
            );
        } catch (FileNotFoundException e) {
            System.err.println(
            		"O arquivo ArcosLivres_" + idProjeto + "_output.dat nao pode ser lido."
            );
            System.exit(1);
        }
        try {
            //System.out.println("Carregando arcos livres...");
            while ((line = inReader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String b = "";
                    String m = "";
                    String e = "";
                    int i = 1;
                    int j;
                    line = st.nextToken();
                    while (line.charAt(i) != ',') {
                        i++;
                    }
                    b = line.substring(1, i);
                    i++;
                    j = i;
                    while (line.charAt(i) != ',') {
                        i++;
                    }
                    m = line.substring(j, i);
                    i++;
                    e = line.substring(i, line.length() - 1);
                    ft.add(
                    		new FreeTurn(
                    				Integer.parseInt(b),
                    				Integer.parseInt(m),
                    				Integer.parseInt(e)
                    		)
                    );
                }
            }
            //System.out.println("Arcos livres carregados com sucesso!");
            inReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return ft;
    }

    public void readEdgeCosts(Graph g, int idProjeto) {
        BufferedReader inReader = null;
        String line;
        Scanner sc;
        boolean success = true;

        try {
            inReader = new BufferedReader(
                    new FileReader(
                    		"CustoNosArcos_" + Integer.toString(idProjeto) + "_output.dat"
                    )
            );
        } catch (FileNotFoundException e) {
            System.err.println(
            		"O arquivo CustoNosArcos_" + idProjeto +
            		"_output.dat nao pode ser lido."
            );
            System.exit(1);
        }
        try {
            //System.out.println("Carregando custo dos arcos...");
            int i, begin, end;
            while ((line = inReader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                i = Integer.parseInt(st.nextToken());
                line = st.nextToken();
                String b = "";
                String e = "";
                int x = 1;
                int y;
                while (line.charAt(x) != ',') {
                    x++;
                }
                b = line.substring(1, x);
                x++;
                y = x;
                while (line.charAt(x) != ')') {
                    x++;
                }
                e = line.substring(y, x);
                begin = Integer.parseInt(b);
                end = Integer.parseInt(e);
                line = st.nextToken();
                if (g.getEdge(i).getFrom().getNumber() ==
                	begin && g.getEdge(i).getTo().getNumber() == end) {
                    g.getEdge(i).setCost(Double.parseDouble(line));
                } else {
                    for (x = 0; x < g.getNumEdges(); x++) {
                        if (g.getEdge(x).getFrom().getNumber() ==
                        	begin && g.getEdge(x).getTo().getNumber() == end) {
                            break;
                        }
                    }
                    if (x == g.getNumEdges()) {
                        System.out.println("Os custos nao foram carregados corretamente!");
                        success = false;
                        break;
                    } else {
                        g.getEdge(x).setCost(Double.parseDouble(line));
                    }
                }
            }
            if (success) {
                //System.out.println("Custos carregados com sucesso!");
            }
            inReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public Graph readGraph(int idProjeto) {
        Graph g = new Graph();
        HashMap<Integer, Integer> nodeTable;
        BufferedReader inReader = null;
        String line;
        Scanner sc;

        try {
            inReader = new BufferedReader(
                    new FileReader("Graph_" + Integer.toString(idProjeto) + "_output.dat")
            );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo Graph_" + idProjeto +
            		"_output.dat nao pode ser lido.");
            System.exit(1);
        }
        try {
            //System.out.println("Carregando grafo...");
            line = inReader.readLine();
            g.setDescription(line);
            line = inReader.readLine();
            sc = new Scanner(line);
            g.setNumNodes(sc.nextInt());
            nodeTable = new HashMap<Integer, Integer>(g.getNumNodes());
            g.setNumEdges(sc.nextInt());
            line = inReader.readLine();
            sc = new Scanner(line);
            while (sc.hasNextInt()) {
                Node n = new Node(-1, sc.nextInt());
                g.addNode(n);
                nodeTable.put(n.getNumber(), g.getNodesSize() - 1);
            }
            g.setTable(nodeTable);
            line = inReader.readLine();
            while (!line.equals("$")) {
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String f = "";
                    String t = "";
                    int i = 1;
                    int j;
                    line = st.nextToken();
                    while (line.charAt(i) != ',') {
                        i++;
                    }
                    f = line.substring(1, i);
                    i++;
                    j = i;
                    while (line.charAt(i) != ')') {
                        i++;
                    }
                    t = line.substring(j, i);
                    g.addEdge(
                    		new Edge(
                    				g.getNode(
                    						nodeTable.get( Integer.parseInt(f) )
                    				),
                    				g.getNode(
                    						nodeTable.get( Integer.parseInt(t) )
                    				)
                    		)
                    );
                }
                line = inReader.readLine();
            }
            
            // Ler os pares OD (apos o cifrao)
            odPairs = inReader.readLine();
            
            String tmp;
            // Ler as estimacoes
            tmp = inReader.readLine();
            //System.out.println("#: "+tmp);
            if ( tmp.equalsIgnoreCase("#") )
            {
            	// Ler as contagens volumetricas
            	tmp = inReader.readLine();
            	if (tmp != "&")
            		linkEstimations = tmp;
            	
            	// Ler as estimacoes OD
            	tmp = inReader.readLine();
            	if (tmp != "&")
            		odEstimations = tmp;
            }
            
            tmp = inReader.readLine();
            //System.out.println("&: "+tmp);
            if ( tmp.equalsIgnoreCase("&") )
            {
            	// Ler as estimacoes O
            	oEstimations = inReader.readLine();
            	
            	// Ler as estimacoes D
            	dEstimations = inReader.readLine();
            }
            
            System.out.println();
            System.out.println("Older link counts: ");
			System.out.println(linkEstimations);
			
            //System.out.println("linkEstimations: "+linkEstimations);
            //System.out.println("odEstimations: "+odEstimations);
            //System.out.println("oEstimations: "+oEstimations);
            //System.out.println("dEstimations: "+dEstimations);
            
            if (g.isValid()) {
                System.out.println("Grafo carregado com sucesso!");
            } else {
                System.out.println("O grafo nao foi carregado corretamente!");
            }
            inReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return g;
    }
    
    public Integer getCorrespondentEntryNodeInGLine(Integer nodeInG)
    {
    	return map.get(nodeInG).getLast();
    }
    
    public Integer getCorrespondentExitNodeInGLine(Integer nodeInG)
    {
    	return map.get(nodeInG).getFirst();
    }
    
    public void convertLinkEstimationsInGToGLine(Graph gline, int idProjeto)
	{
		PrintWriter outWriter = null;
        File fileGraph = null;
        
        try {
            outWriter = new PrintWriter(
                    new FileWriter("New_Graph_"+idProjeto+"_output_temp.dat")
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            // The Project_2_output_Estimations.dat file
            fileGraph = new File("/home/iacer/workspace/FLPM2_cplex/New_Graph_"+
            		idProjeto+"_output.dat");
        } catch (Exception e) {
            System.err.println("O arquivo New_Graph_"+idProjeto+
            		"_output.dat nao pode ser lido.");
            System.exit(1);
        }
        
        String finalStr = "";
		LinkCounts lc = new LinkCounts();
		String record = null;
		DataInputStream dis = null;
		
		if (!fileGraph.canRead()) {
			System.err.println("Problemas ao abrir o arquivo " +
					fileGraph.getName());
			return;
		}
		try {
			FileInputStream fis = new FileInputStream(fileGraph);
			BufferedInputStream bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			
			// Iterate through the file and get the link counts
			while ((record = dis.readLine()) != null) {
				// Copy the file content to the new file
				outWriter.println(record);
				// Next line contains the link counts
				if (record.equalsIgnoreCase("#")) break;
			}
			
			// Get the link counts
			record = dis.readLine();
			
			System.out.println("Old link counts: ");
			System.out.println(record);
			System.out.println();
			
			System.out.println("ODPairs: "+odPairs);
			System.out.println("Map: "+map);
			
			// Get each link count entry
			String[] entries = record.split(" ");
			for (int i=0; i<entries.length; i++)
			{
				// Get its elements
				String[] temp = entries[i].split(",");
				
				String from, to, estimation, errorAbove, errorBelow;
				from = temp[0].replace("(", "").trim();
				to = temp[1].trim();
				estimation = temp[2].trim();
				errorAbove = temp[3].trim();
				errorBelow = temp[4].replace(")", "").trim();
				
				System.out.println("Link count: ("+from+","+to+")");
				
				for (int j=0; j<gline.getEdgesSize(); j++) {
					Edge e1 = gline.getEdge(j);
					
					int fnumber = e1.getFrom().getNumber();
					int tnumber = e1.getTo().getNumber();
					int ffirst = e1.getFrom().getFirst();
					int fsecond = e1.getFrom().getSecond();
					int tfirst = e1.getTo().getFirst();
					int tsecond = e1.getTo().getSecond();
					
					if (fnumber != -1 && tnumber != -1 && ffirst != -1 &&
							fsecond != -1 && tfirst != -1 && tsecond != -1)
					{
						if (ffirst==tfirst && fsecond==tsecond)
						{
							int newFrom = -1;
							int newTo = -1;
							Iterator itMap = map.entrySet().iterator();
							while (itMap.hasNext())
							{
								Map.Entry entry = (Map.Entry) itMap.next();
								int key = (Integer) entry.getKey();
								Pair value = (Pair) entry.getValue();
								
								if (value.getFirst() == Integer.parseInt(from))
									newFrom = key;
								
								if (value.getLast() == Integer.parseInt(to))
									newTo = key;
							}
							
							//System.out.println("map.get: ("+newFrom+","+newTo+")");
							if (newFrom == ffirst && newTo == fsecond)
							{
								System.out.println("Edge: "+fnumber+","+tnumber);
								//System.out.println("("+e1.getFrom().getFirst()+","+e1.getFrom().getSecond()+")");
								//System.out.println("("+e1.getTo().getFirst()+","+e1.getTo().getSecond()+")");
								
								// Populate the capacities map
								String key = "("+from+","+to+")";
								String val = "("+fnumber+","+tnumber+")";
								
								capacitiesMap.put(key, val);
								
								// Add the estimate to the edge entry
								finalStr += (
										"("+
										fnumber+","+
										tnumber+","+
										estimation+","+
										errorAbove+","+
										errorBelow+") "
								);
							}
						}
					}
		        }
			}
			
			// Copy the modified link counts to the output file
			outWriter.println(finalStr);
			
			// Iterate through the rest of the file and finish the copy
			while ((record = dis.readLine()) != null) {
				// Copy the file content to the new file
				outWriter.println(record);
			}
			
			outWriter.close();
		}  catch (IOException e) {
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
		
		// -----------------------------------------
		// Overwrite the old graph file with the
		// temporary file created with the outWriter
		// -----------------------------------------
		
        // Erase the old file and create a new one
        fileGraph.delete();
        try {
			fileGraph.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        BufferedReader inReader = null;
        
        try {
        	// A reader to the temporary graph
            inReader = new BufferedReader(
            		new FileReader("New_Graph_"+idProjeto+"_output_temp.dat")
            );
            
            // A writer to the final graph
            outWriter = new PrintWriter(
            		new FileWriter("New_Graph_"+idProjeto+"_output.dat")
            );
        } catch (Exception e) {
        	e.printStackTrace();
            System.exit(1);
        }
        
        try {
        	String line;
        	while ( (line = inReader.readLine()) != null )
        	{
        		outWriter.println(line);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        try {
			inReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		outWriter.close();
	}
    
    public String getCapacityThroughMap(String from, String to)
    {
    	String val = null;
    	if (capacitiesMap.containsKey("("+from+","+to+")"))
    		val = capacitiesMap.get("("+from+","+to+")");
    	return val;
    }
}
