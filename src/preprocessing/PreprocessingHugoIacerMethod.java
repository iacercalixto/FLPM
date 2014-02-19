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
 * and not route costs. The pp is conducted as described:
 * 
 * For each OD pair in the model, find the least cost routes for each.
 * The other routes to be added are going to be enumerated according
 * to a combinatorial optimization method as proposed by Hugo and Iacer.
 * 
 * @author Iacer Calixto
 */
public class PreprocessingHugoIacerMethod implements IPreprocessing
{
	/**
	 * The (maximum) number of routes per OD pair to be used.
	 */
	private int k;
	
	/**
	 * The constant used to calculate the non-least-cost routes
	 * between OD pairs.
	 */
	private static int c = 10;
	
	/**
	 * Whether to use or not the constant c above
	 * or to calculate all the path costs (not only for k=1).
	 */
	private static boolean useConstant = true;
	
	/**
	 * Constant used in calculations
	 */
	private static int L = 1000;
	
	/**
	 * Constructor
	 */
	public PreprocessingHugoIacerMethod()
	{
		this.k = 5;
	}
	
	/**
	 * Do preprocessing converting arc costs into route costs
	 * 
	 * @param graph
	 * @param lCounts
	 * @param od
	 * @param lCosts
	 * @throws Exception 
	 */
	public HashMap doPreprocessing(AbstractGraph<String, DefaultWeightedEdge> graph, LinkCounts lCounts,
			ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs) throws Exception
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
		
		// Add some new paths and check whether there are still some arcs missing
		// Iterate through all the OD pairs
		Iterator <?> itODPairs = odPairs.getODPairs().entrySet().iterator();
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
			
			// Get the 'k' shortest paths between the vertex
			// 'v1' and all the other vertices in the graph
			if (this.k <= 0)
			{
				throw new Exception("The parameter 'k' must be set before calling the preprocessing job.");
			}
			KShortestPaths sp = new KShortestPaths(graph, (String) v1, this.k);
			
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
				
				String strTemp = new String("(");
				
				String[] tempFromTo = null;
				int counterTemp = 0;
				
				Node n1 = null;
				Node n2 = null;
				List<Node> p1 = new ArrayList();
				//Double dblSummedLinkErrorAbove = new Double(0);
				//Double dblSummedLinkErrorBelow = new Double(0);
				
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
					for (Node o : p1)
					{
						if (edgeCounter++==0)
						{
							vertexFrom = vertexTo = o.toString();
						} else
						{
							vertexFrom = vertexTo.trim();
							vertexTo = o.toString().trim();
							//dblSummedLinkErrorAbove += lCosts.getErrorAboveBetween((String)vertexFrom, (String)vertexTo);
							//dblSummedLinkErrorBelow += lCosts.getErrorBelowBetween((String)vertexFrom, (String)vertexTo);
							//System.out.println("vertex From->To: (" + vertexFrom + "->" + vertexTo + ")");
							//System.out.println("dblSummedLinkErrorAbove: " + dblSummedLinkErrorAbove);
							//System.out.println("dblSummedLinkErrorBelow: " + dblSummedLinkErrorBelow);
						}
					}
					
					// Add the error measurements
					//errorMeasurementAbove.put(p1, dblSummedLinkErrorAbove);
					//errorMeasurementBelow.put(p1, dblSummedLinkErrorBelow);
					
					// If the path is a minimum-cost path (k==1), add its weight as is
					if (intPathCounter==1)
					{
						p.put(p1, Double.toString(((GraphPath)newPaths.get(0)).getWeight()) );
						System.out.println("p1: " + p1 + ": " + Double.toString(((GraphPath)newPaths.get(0)).getWeight()) );
					} else
					{
						// If the path is NOT a minimum-cost path (k>1) 
						if (this.useConstant)
						{
							// If constant C is being used, the path cost equals the
							// k==1 path times C
							p.put(p1, Double.toString(((GraphPath)newPaths.get(0)).getWeight() * this.c) );
							System.out.println(
									"p1: " + p1 + ": " + Double.toString(((GraphPath)newPaths.get(0)).getWeight() * this.c)
							);
						} else
						{
							// If constant C is NOT being used, the path cost equals
							// its total cost
							p.put(p1, this.getPathCost(graph, p1) );
							System.out.println( "p1: " + p1 + ": " + this.getPathCost(graph, p1) );
						}
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
	
	public void setK(Integer k) throws Exception
	{
		if (k<=0)
			throw new Exception("The maximum number of routes per OD pair must be at least one.");
		this.k = (int)k;
	}
	
	public void setUseConstant(Boolean b) throws Exception
	{
		this.useConstant = (boolean)b;
	}
}
