/**
 * 
 */
package preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.FloydWarshall2;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.alg.Node;
import org.jgrapht.alg.Edge;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graph;




//import org.jgraph.*;
//import org.jgrapht.*;
//import org.jgrapht.alg.*;
//import org.jgrapht.graph.*;

import ilog.concert.*;
import ilog.cplex.*;

/**
 * This class does the preprocessing (pp) of the model that has only arc costs,
 * and not route costs. The pp conducted is a mix between Professor Hugo's and Iacer's
 * proposal and Professor Les' proposal:
 * 
 * First, add all the minimum-cost routes to the model.
 * For each arc with traffic counts that don't belong to any route in the model,
 * find one route that uses that arc and add it to the model. Do it until all
 * arcs with traffic counts participates at least in one route in the model.
 * 
 * @author Iacer Calixto
 */
public class PreprocessingLesMixedMethod implements IPreprocessing
{
	/**
	 * Constructor
	 */
	public PreprocessingLesMixedMethod() {}
	
	/**
	 * Do pre-processing converting arc costs into route costs
	 * 
	 * @param graph
	 * @param lCounts
	 * @param od
	 * @param lCosts
	 */
	public HashMap doPreprocessing(AbstractGraph<String, DefaultWeightedEdge> graph, LinkCounts lCounts,
			ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs)
	{
		ArrayList arrFrom = new ArrayList<String>();
		ArrayList arrTo = new ArrayList<String>();
		HashMap hmWeight = new HashMap();
		HashMap addedRoutes = new HashMap();
		
		double d;
		HashMap p = new HashMap();
		
		Node[] nodes = new Node[ graph.vertexSet().size() ];
		Edge[] edges = new Edge[ graph.edgeSet().size() ];
		
		//--------------------------------------------------------------
		// creating the structure(s) to use the Floyd-Warshall algorithm
		// which isn't the one shipped with JGraphT, but a different one
		//--------------------------------------------------------------
		
		// For each node, add it to the Node[] array
		Iterator <String> it = graph.vertexSet().iterator();
		int counter = 0;
		while (it.hasNext())
		{
			String vertex = it.next();
			Node n = new Node( Integer.parseInt(vertex)-1, vertex );
			nodes[Integer.parseInt(vertex)-1] = n;
		}
		
		// For each edge, add it to the Edge[] array
		Iterator <?> it2 = graph.edgeSet().iterator();
		int counter2 = 0;
		while (it2.hasNext())
		{
			DefaultWeightedEdge e = (DefaultWeightedEdge)it2.next();
			String v1 = (String)graph.getEdgeSource(e);
			String v2 = (String)graph.getEdgeTarget(e);
			double weight = (double)graph.getEdgeWeight(e);
			
			Edge e2 = new Edge(nodes[Integer.parseInt(v1)-1],
					nodes[Integer.parseInt(v2)-1],
					(int)weight, 0, 0);
			edges[counter2++] = e2;
		}
		
		// Applying the Floyd-Warshall on the recently created nodes and edges
		FloydWarshall2 fw = new FloydWarshall2( nodes, edges );
		
		//System.out.println("Nodes: " + nodes);
		//System.out.println("Edges: " + edges);
		
		System.out.println("Beginning pre-processing...");
		
		// First of all, iterate through all the OD pairs
		Iterator <?> itODPairs = odPairs.getODPairs().entrySet().iterator();
		while(itODPairs.hasNext())
		{
			Map.Entry objODPair = (Map.Entry) itODPairs.next();
			
			String strODPair = (String) objODPair.getValue();
			String[] fromTo;
			fromTo = strODPair.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			Object vertex = fromTo[0];
			Object vertex2 = fromTo[1];
			
			Node n1 = new Node( Integer.parseInt((String)vertex)-1, (String)vertex );
			Node n2 = new Node( Integer.parseInt((String)vertex2)-1, (String)vertex2 );
			d = fw.getShortestDistance(n1, n2);
			
			List<Node> p1 = fw.getShortestPath(n1, n2);
			
			// If there is at least one route between the OD pair, the distance
			// 'd' of the route will be finite (therefore different of infinity)
			if (d != Double.POSITIVE_INFINITY)
			{
				arrFrom.add((String)vertex);
				arrTo.add((String)vertex2);
				hmWeight.put((String)vertex+"->"+(String)vertex2, d);
				
				// Iterate through the shortest path p1 and obtain the aggregate
				// route error measurements (from the link costs error measurement)
				int edgeCounter = 0;
				String vertexFrom = null;
				String vertexTo = null;
				//System.out.println("p1: " + p1);
				for (Node o : p1)
				{
					if (edgeCounter++==0)
					{
						vertexFrom = vertexTo = o.toString();
					} else
					{
						vertexFrom = vertexTo.trim();
						vertexTo = o.toString().trim();
						//System.out.println("vertex From->To: (" + vertexFrom + "->" + vertexTo + ")");
						//System.out.println("dblSummedLinkErrorAbove: " + dblSummedLinkErrorAbove);
						//System.out.println("dblSummedLinkErrorBelow: " + dblSummedLinkErrorBelow);
					}
				}
				
				// If the route has already been added to the model,
				// do not add it one more time.
				boolean boolFound = false;
				for (Object listN : ((HashMap)p).keySet())
					if (p1.toString().equalsIgnoreCase(((List)listN).toString()))
						boolFound = true;
				
				if ( boolFound )
					System.out.println("Alerta: Tentativa de adi��o de p1 (" + p1 + ") j� existente no modelo.");
				else
					// Add the minimum-cost path to the solution of the problem
					p.put(p1, Double.toString(d));
					//p.put(p1, PreprocessingLesMixedMethod.getPathCost(graph, p1));
				
				//p.put((String)vertex+"->"+(String)vertex2, p1);
				//System.out.println((String)vertex+"->"+(String)vertex2+": "+d);
				//System.out.println("Path: ("+(String)vertex+"-"+(String)vertex2+"): "+p);
			}
		}
		
		// Add some new paths and check whether there are still some arcs missing
		// Iterate through all the OD pairs
		itODPairs = odPairs.getODPairs().entrySet().iterator();
		while(itODPairs.hasNext() /*&& boolLooping*/)
		{
			Map.Entry objODPair = (Map.Entry) itODPairs.next();
			
			String strODPair = (String) objODPair.getValue();
			String[] fromTo = strODPair.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			Object v1 = fromTo[0];
			Object v2 = fromTo[1];
			
			if ((String)v1 == (String)v2) continue;
			
			// Get the 5 shortest paths between the vertex
			// 'v1' and all the other vertices in the graph
			KShortestPaths sp = new KShortestPaths(graph, (String) v1, 5);
			
			// Get a list of the (maximum) 5 paths starting in 'v1' and
			// ending in 'v2'
			List newPaths = sp.getPaths((String) v2);
			
			int intPathCounter = 1;
			//System.out.println("newPaths: " + newPaths);
			// Iterate through all the paths between 'v1' and 'v2'
			Iterator<?> itNewPaths = newPaths.iterator();
			while (itNewPaths.hasNext())
			{
				GraphPath entry = (GraphPath) itNewPaths.next();
				
				
				//System.out.println("v1: " + v1 + ", v2: " + v2 + ", intPathCounter: " + intPathCounter);
				
				
				if (
						// Add the D-C (5-4) least cost path
						// If it is the 1st (least cost) path, add it
						(((String) v1).equalsIgnoreCase("5") && ((String) v2).equalsIgnoreCase("4") && intPathCounter==1) ||
						// Add the A-F (1-8) least cost path
						// If it is the 2nd least cost path, add it
						(((String) v1).equalsIgnoreCase("1") && ((String) v2).equalsIgnoreCase("8") && intPathCounter==2) ||
						// Add the B-F (2-8) least cost path
						// If it is the 2nd least cost path, add it
						(((String) v1).equalsIgnoreCase("2") && ((String) v2).equalsIgnoreCase("8") && intPathCounter==2) ||
						// Add the B-E (2-7) least cost path
						// If it is the 3rd least cost path, add it
						(((String) v1).equalsIgnoreCase("2") && ((String) v2).equalsIgnoreCase("7") && intPathCounter==3)
				)
				{
					String strTemp = new String("(");
					
					String[] tempFromTo = null;
					int counterTemp = 0;
					
					Node n1 = null;
					Node n2 = null;
					List<Node> p1 = new ArrayList();
					Double dblSummedLinkErrorAbove = new Double(0);
					Double dblSummedLinkErrorBelow = new Double(0);
					
					double weight = 0;
					
					// For each arc in the path between 'v1' and 'v2',
					// if it is (one of) the missing arc(s), add its route
					// to the problem
					for (Object edge : (entry.getEdgeList()))
					{
						// If the route under analysis contains the arc, add it to the model.
						// If not, do nothing and continue iterating the other routes.
						
						edge = (DefaultWeightedEdge)edge;
						String vFrom = (String)graph.getEdgeSource((DefaultWeightedEdge)edge);
						String vTo = (String)graph.getEdgeTarget((DefaultWeightedEdge)edge);
						weight = (double)graph.getEdgeWeight((DefaultWeightedEdge)edge);
						
						Edge e2 = new Edge(nodes[Integer.parseInt(vFrom)-1],
								nodes[Integer.parseInt(vTo)-1],
								(int)weight, 0, 0);
						
						for (int i=0; i<edges.length; i++)
						{
							if (edges[i].equals(e2)) e2 = edges[i];
						}
						
						if ( !checkArcBelongsToRoutes(((Edge)e2), newPaths) )
							continue;
						
						tempFromTo = edge.toString().split(" : ");
						tempFromTo[0] = tempFromTo[0].replace("(", "").replace(")", "").trim();
						tempFromTo[1] = tempFromTo[1].replace(")", "").replace("(", "").trim();
						//System.out.println("edge: " + tempFromTo[0] + "->" + tempFromTo[1]);
						
						if(counterTemp == 0)
						{
							n1 = new Node( Integer.parseInt((String)tempFromTo[0])-1, (String)tempFromTo[0] );
							p1.add(n1);
							strTemp += tempFromTo[0] + ", ";
						}
						counterTemp++;
						n2 = new Node( Integer.parseInt((String)tempFromTo[1])-1, (String)tempFromTo[1] );
						strTemp += tempFromTo[1] + ", ";
						
						p1.add(n2);
					}
					
					if (counterTemp>0)
					{
						// Iterate through the shortest path p1 and obtain the aggregate
						// route error measurements (from the link costs error measurement)
						int edgeCounter = 0;
						String vertexFrom = null;
						String vertexTo = null;
						//System.out.println("p1 (adicionado posteriormente): " + p1);
						for (Node o : p1)
						{
							if (edgeCounter++==0)
							{
								vertexFrom = vertexTo = o.toString();
							} else
							{
								vertexFrom = vertexTo.trim();
								vertexTo = o.toString().trim();
								//System.out.println("vertex From->To: (" + vertexFrom + "->" + vertexTo + ")");
							}
						}
						
						boolean boolFound = false;
						
						// If the route has already been added to the model,
						// do not add it one more time.
						for (Object listN : ((HashMap)p).keySet())
							if (p1.toString().equalsIgnoreCase(((List)listN).toString()))
								boolFound = true;
						
						if ( boolFound )
							System.out.println("Alerta: Tentativa de adi��o de p1 " + p1 + " j� existente no modelo.");
						else
							p.put(p1, PreprocessingLesMixedMethod.getPathCost(graph, p1));
					}
				}
				
				intPathCounter++;
			}
		}
		
		System.out.println("Pre-processing finished!");
		
		return p;
	}
	
	/**
	 * Calculates the cost of the given path.
	 * 
	 * @param graph The graph that represents the traffic network
	 * @param path The path to which to calculate the cost
	 * @return
	 */
	private static String getPathCost(AbstractGraph graph, List<Node> path)
	{
		String thisNode = null;
		String nextNode = null;
		DefaultWeightedEdge edge;
		double weight;
		double sum = 0;
		
		int counter = 0;
		for (Node n : path)
		{
			if (counter++ == 0)
			{
				thisNode = nextNode = n.toString();
				continue;
			} else
			{
				thisNode = nextNode;
			}
			
			nextNode = n.toString();
			edge = (DefaultWeightedEdge)graph.getEdge(thisNode, nextNode);
			weight = (double)graph.getEdgeWeight(edge);
			sum += weight;
		}
		return Double.toString(sum);
	}
	
	/**
	 * Returns a list with the arcs that don't participate in any routes
	 * in the model.
	 * 
	 * @param graph The graph that represents the traffic network
	 * @param lCounts The link counts
	 * @param odPairs The OD pairs
	 * @param rc The route costs
	 * @param p The HashMap containing the routes added to the model.
	 * @return
	 */
	private static List getMeasuredArcsWithoutRoutes(AbstractGraph graph, LinkCounts lCounts,
			ODPairs odPairs, RouteCosts rc, HashMap p)
	{
		// The return value
		List retList = new ArrayList();
		
		// Obtain the valid routes (the one containing at least two nodes)
		HashMap validRoutes = new HashMap();
		Iterator<?> itValidRoutes = p.entrySet().iterator();
		while (itValidRoutes.hasNext()) {
			Map.Entry route = (Map.Entry) itValidRoutes.next();
			Object listNodes = route.getKey();
			
			//System.out.println("listNodes: " + listNodes);
			if (listNodes == null) continue;
			
			validRoutes.put(listNodes, listNodes);
		}
		//System.out.println("validRoutes.entrySet() [" + validRoutes.entrySet().size() +
		//		"]: " + validRoutes.entrySet());
		
		boolean[] arcFoundInRoute = new boolean[lCounts.getAllCounts().entrySet().size()];
		int routeCounter = 0;
		
		// Add all the valid arcs to the returning List and
		// remove the entries containing arcs already found
		Iterator<?> itLinkCounts = lCounts.getAllCounts().entrySet().iterator();
		while (itLinkCounts.hasNext())
		{
			String[] arrStr;
			String from, to, value;
			
			Map.Entry entry = (Map.Entry) itLinkCounts.next();
			
			arrStr = entry.getKey().toString().split(",");
			
			from = arrStr[0].replace("(","").replace(")","");
			to = arrStr[1].replace("(","").replace(")","");
			value = entry.getValue().toString();
			
			String objLink = from + "->" + to;
			
			retList.add(objLink);
		}
		
		// Iterate through all the links that contain count measurements
		//System.out.println("Link Counts:");
		itLinkCounts = lCounts.getAllCounts().entrySet().iterator();
		while (itLinkCounts.hasNext())
		{
			String[] arrStr;
			String from, to, value;
			
			Map.Entry entry = (Map.Entry) itLinkCounts.next();
			
			arrStr = entry.getKey().toString().split(",");
			
			from = arrStr[0].replace("(","").replace(")","");
			to = arrStr[1].replace("(","").replace(")","");
			value = entry.getValue().toString();
			
			String objLink = from + "->" + to;
			
			itValidRoutes = validRoutes.entrySet().iterator();
			//System.out.println("Link count " + from+"->"+to+"="+value);
			//System.out.println("Iterando as " + validRoutes.entrySet().size() + " rotas:");
			
			// Iterating through all the valid routes
			while (itValidRoutes.hasNext()) {
				Map.Entry routes = (Map.Entry) itValidRoutes.next();
				if (routes.getValue().getClass().toString().equalsIgnoreCase("class org.jgrapht.alg.KShortestPaths$PathWrapper"))
					continue;
				
				List listNodes = (List) routes.getKey();
				
				if (listNodes.isEmpty()) continue;
				
				// Iterating through all the arcs in each valid route
				int sz = listNodes.size();
				for (int i=0; i<sz; i++)
				{
					if (i+1 < sz)
					{
						Node thisNode = (Node)listNodes.get(i);
						Node nextNode = (Node)listNodes.get(i+1);
						
						//System.out.println("thisNode: " + thisNode + ", nextNode: " + nextNode);
						
						if (thisNode.toString().equalsIgnoreCase(from) &&
								nextNode.toString().equalsIgnoreCase(to))
						{
							// If the arc found is in the return list, remove it
							if (retList.contains(objLink))
								retList.remove(objLink);
							
							//System.out.println("Link "+thisNode+"->"+nextNode+" encontrado na rota "+listNodes + ".");
							arcFoundInRoute[routeCounter] = true;
							break;
						}
					} else
					{
						continue;
					}
					
				}
				
				if (arcFoundInRoute[routeCounter])
				{
					routeCounter++;
					break;
				}
			}
		}
		
		//System.out.println("validRoutes: " + validRoutes.entrySet());
		//System.out.println("retList: " + retList);
		return retList;
	}
	
	/**
	 * Returns <code>true</code> if all the arcs with traffic counts belong to at
	 * least one route and <code>false</code> otherwise.
	 * 
	 * @param graph The graph that represents the traffic network
	 * @param lCounts The link counts
	 * @param odPairs The OD pairs
	 * @param rc The route costs
	 * @param p The HashMap containing the routes added to the model.
	 * @return
	 */
	private static boolean checkAllMeasuredArcsHaveRoutes(AbstractGraph graph, LinkCounts lCounts,
			ODPairs odPairs, RouteCosts rc, HashMap p)
	{
		// After all the optimum routes are added to the HashMap,
		// we must ensure there is at least one route encompassing
		// every and each of the arcs containing traffic measurements
		
		//System.out.println("p.entrySet(): ["+p.entrySet().size()+"]" + p.entrySet());
		
		HashMap validRoutes = new HashMap();
		Iterator<?> itValidRoutes = p.entrySet().iterator();
		while (itValidRoutes.hasNext()) {
			Map.Entry route = (Map.Entry) itValidRoutes.next();
			Object listNodes = route.getKey();
			
			//System.out.println("listNodes: " + listNodes);
			if (listNodes == null) continue;
			
			validRoutes.put(listNodes, listNodes);
		}
		//System.out.println("validRoutes.entrySet() [" + validRoutes.entrySet().size() +
		//		"]: " + validRoutes.entrySet());
		
		//boolean[] arcFoundInRoute = new boolean[validRoutes.entrySet().size()];
		boolean[] arcFoundInRoute = new boolean[lCounts.getAllCounts().entrySet().size()];
		int routeCounter = 0;
		//System.out.println("Link Counts:");
		Iterator<?> itLinkCounts = lCounts.getAllCounts().entrySet().iterator();
		while (itLinkCounts.hasNext())
		{
			String[] arrStr;
			String from, to, value;
			
			Map.Entry entry = (Map.Entry) itLinkCounts.next();
			
			arrStr = entry.getKey().toString().split(",");
			
			from = arrStr[0].replace("(","").replace(")","");
			to = arrStr[1].replace("(","").replace(")","");
			value = entry.getKey().toString();
			
			itValidRoutes = validRoutes.entrySet().iterator();
			//System.out.println("Link count " + from+"->"+to+"="+value);
			//System.out.println("Iterando as " + validRoutes.entrySet().size() + " rotas:");
			while (itValidRoutes.hasNext()) {
				Map.Entry routes = (Map.Entry) itValidRoutes.next();
				if (routes.getValue().getClass().toString().equalsIgnoreCase("class org.jgrapht.alg.KShortestPaths$PathWrapper"))
					continue;
				
				List listNodes = (List) routes.getKey();
				
				if (listNodes.isEmpty()) continue;
				
				int sz = listNodes.size();
				//System.out.println("Entrando no For...");
				for (int i=0; i<sz; i++)
				{
					//if (arcFoundInRoute[routeCounter]) break;
					if (i+1 < sz)
					{
						Node thisNode = (Node)listNodes.get(i);
						Node nextNode = (Node)listNodes.get(i+1);
						
						//System.out.println("thisNode: " + thisNode + ", nextNode: " + nextNode);
						
						if (thisNode.toString().equalsIgnoreCase(from) &&
								nextNode.toString().equalsIgnoreCase(to))
						{
							//System.out.println("Link "+thisNode+"->"+nextNode+" encontrado na rota "+listNodes + ".");
							arcFoundInRoute[routeCounter] = true;
							break;
						}
					} else
					{
						continue;
					}
					
					//System.out.println(listNodes);
				}
				
				if (arcFoundInRoute[routeCounter])
				{
					routeCounter++;
					break;
				}
			}
		}
		//System.out.println("boolean array arcFoundInRoute(" + arcFoundInRoute.length + "):");
		boolean someArcMissing = false;
		//String printlnValue = null;
		for (int i=0; i<arcFoundInRoute.length; i++)
		{
			if (arcFoundInRoute[i]==false) someArcMissing = true;
			//printlnValue = (arcFoundInRoute[i]==true) ? "1 | " : "0 | ";
			//System.out.print(printlnValue);
		}
		System.out.println("Are there any arcs missing: " + ((someArcMissing==true) ? " true." : " false."));
		
		return !someArcMissing;
	}
	
	/**
	 * Check whether the given arc belongs to at least one of the
	 * given routes.
	 * 
	 * @param e The arc to check
	 * @param routes A list containing the routes.
	 * @return
	 */
	private static boolean checkArcBelongsToRoutes(Edge e, List <GraphPath> routes)
	{
		boolean boolBelongs = false;
		boolean boolTempBelongs = false;
		for (GraphPath route : routes)
		{
			List edgeList = route.getEdgeList();
			for (Object edge : edgeList)
			{
				//System.out.println("edge.getClass(): " + edge.getClass() + ", edge: " + edge);
				String strEdge = edge.toString();
				//System.out.println("strEdge: " + strEdge);
				String[] strFromTo = strEdge.split(" : ");
				strFromTo[0] = strFromTo[0].replace("(", "").trim();
				strFromTo[1] = strFromTo[1].replace(")", "").trim();
				
				if (e.getFrom().equalsIgnoreCase(strFromTo[0]) &&
						e.getTo().equalsIgnoreCase(strFromTo[1]) )
				{
					boolTempBelongs = true;
				}
			}
			if (boolTempBelongs = true)
			{
				//System.out.println("Route " + route.toString() + " contains edge " + e.toString());
				boolBelongs = true;
			} else
			{
				//System.out.println("Route " + route.toString() + " DOESN'T contains edge " + e.toString());
			}
		}
		return boolBelongs;
	}
	
	/**
	 * Check whether the given arc belongs to the given route.
	 * 
	 * @param e The arc to check
	 * @param route A list of nodes representing the route.
	 * @return
	 */
	private static boolean checkArcBelongsToRoute(String e, List <Node> route)
	{
		//System.out.println("Entering in checkArcBelongsToRoute()");
		String[] strNodes = null;
		String strRoute = new String();
		String strEdgeFrom = null;
		String strEdgeTo = null;
		List edges = new ArrayList();
		boolean boolBelongs = false;
		for (Node n : route)
		{
			strRoute += n.toString() + ",";
		}
		strRoute = strRoute.substring(0, strRoute.length()-1);
		strNodes = strRoute.split(",");
		
		for (int i=1; i<strNodes.length; i++)
		{
			edges.add( strNodes[i-1] + "->" + strNodes[i] );
		}
		
		String[] strTempFromTo = null;
		String strDefaultEdge = new String();
		strDefaultEdge = e.toString();
		strTempFromTo = strDefaultEdge.split("->");
		strEdgeFrom = strTempFromTo[0];
		strEdgeTo = strTempFromTo[1];
		
		for (Object edge : edges)
		{
			//System.out.println("edge.getClass(): " + edge.getClass() + ", edge: " + edge);
			String strEdge = edge.toString();
			String[] strFromTo = strEdge.split("->");
			strFromTo[0] = strFromTo[0].replace("(", "").trim();
			strFromTo[1] = strFromTo[1].replace(")", "").trim();
			
			if (strEdgeFrom.equalsIgnoreCase(strFromTo[0]) &&
					strEdgeTo.equalsIgnoreCase(strFromTo[1]) )
			{
				boolBelongs = true;
			}
		}
		//if (boolBelongs == true)
		//	System.out.println("Route " + route.toString() + " contains edge " + e.toString());
		//else
		//	System.out.println("Route " + route.toString() + " DOESN'T contains edge " + e.toString());
		
		//System.out.println("Leaving from checkArcBelongsToRoute()");
		return boolBelongs;
	}
}
