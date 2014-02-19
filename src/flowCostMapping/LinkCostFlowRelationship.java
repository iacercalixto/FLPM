/**
 * 
 */
package flowCostMapping;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import odEstimation.LinkCapacities;
import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import v7.FLIPSOD.BPRFunctionName;

/**
 * @author iacer
 *
 */
public class LinkCostFlowRelationship {
	/**
	 * Constructor
	 */
	public LinkCostFlowRelationship() {}
	
	/**
	 * Calculates the link costs according to
	 * (i) the link capacity,
	 * (ii) the link free-flow cost and
	 * (iii) the link flow.
	 * If there is no available link flow (tipically in the first iteration),
	 * use an arbitrary value in the calculation (zero or its capacity).
	 * 
	 * @param linkCapacities the object containing the link capacities and free-flow cost
	 * @param linkCountsHash the object containing the link counts
	 * @return the object with the corresponding link costs
	 * @throws Exception
	 */
	public LinkCosts calculateCostFlowRelationship(DefaultDirectedWeightedGraph g,
			LinkCapacities linkCapacities, LinkCounts linkCounts,
			LinkCounts previousLinkCounts, double smoothingMultiplier,
			BPRFunctionName bprFunctionName)
	throws Exception
	{
		//TODO
		// Check what's wrong with the link costs.
		// The errors happen probably because of missing link capacities
		// in arcs in G' that don't correspond to edges in G
		// (and therefore will have capacities of those edges).
		// The newly generated edges in G' that doesn't represent
		// any edges in G must be fixed.
		
		// Return object
		LinkCosts lCosts = new LinkCosts();
		
		System.out.println("LinkCosts.calculateCostFlowRelationship()");
		
		// First, lets start the link costs to all edges as zero
		// Except for the edges in G' that represent edges in G,
		// these shall receive a symbolic cost of one.
		Set edges = g.edgeSet();
		Iterator itEdges = edges.iterator();
		while (itEdges.hasNext())
		{
			DefaultWeightedEdge edge = (DefaultWeightedEdge) itEdges.next();
			String strFrom, strTo;
			strFrom = edge.toString().split(":")[0].trim().replace("(", "");
			strTo = edge.toString().split(":")[1].trim().replace(")", "");
			
			//System.out.println("edge: ("+strFrom+","+strTo+")");
			
			double finalCost = 0;
			
			if (Integer.parseInt(strFrom) > 1000 &&
					10 * Integer.parseInt(strFrom) == Integer.parseInt(strTo))
			{
				System.out.println(
						"g.getEdgeWeight("+strFrom+"->"+strTo+"): "+
						g.getEdgeWeight(edge)
				);
				finalCost = g.getEdgeWeight(edge);
				//finalCost = 1;
			} else
			{
				finalCost = 0;
			}
			
			// Set the link costs as zero for all edges
			// Add it to the link costs object
			lCosts.setCount(
					Integer.parseInt(strFrom),
					Integer.parseInt(strTo),
					Double.toString(finalCost)
			);
		}
		//System.out.println("edges: "+edges);
		
		// linkCounts in the format {(7,1)=500, (6,7)=5000}
		//System.out.println("linkCountsHash ("+linkCounts.getAllCounts().size()+
		//		"): " + linkCounts);
		
		// Afterwards, lets iterate through the link counts
		// and update the arc costs to the cases where we have counts
		
		// Iterate through the link congestion-free cost together
		// with the link capacity
		Iterator itLCapacities = linkCapacities.getLinkCapacities().entrySet().iterator();
		Iterator itLCostsFreeFlow = linkCapacities.getLinkCostFreeFlow().entrySet().iterator();
		while (itLCapacities.hasNext())
		{
			Map.Entry entryLinkCapacity = (Map.Entry) itLCapacities.next();
			Map.Entry entryLinkCostFreeFlow = (Map.Entry) itLCostsFreeFlow.next();
			
			// Read the data
			String from, to;
			String newCountsSeparator = "";
			String previousCountsSeparator = "";
			String capacitiesSeparator = "";
			double linkCapacity;
			double linkCount, previousLinkCount;
			double linkCostFreeFlow = -1;
			double finalCost;
			
			// Gets the string separator to the link capacity
			if (entryLinkCapacity.getKey().toString().indexOf(",") > -1)
			{
				capacitiesSeparator = ",";
				linkCapacity = (Double)entryLinkCapacity.getValue();
			} else {
				capacitiesSeparator = "->";
				linkCapacity = (Double)entryLinkCapacity.getValue();
			}
			
			// Gets and sanitizes the data
			from = entryLinkCapacity.getKey().toString().split(capacitiesSeparator)[0];
			to = entryLinkCapacity.getKey().toString().split(capacitiesSeparator)[1];
			from = from.replace("(", "").trim();
			to = to.replace(")", "").trim();
			
			// Gets the link capacity and free-flow cost
			try {
				linkCapacity = linkCapacities.getCapacityBetween(from, to);
				linkCostFreeFlow = linkCapacities.getCostFreeFlow(from, to);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			HashMap <?,?>allCounts = linkCounts.getAllCounts();
			
			// Gets the string separator to the link counts
			if (allCounts.get("("+from+","+to+")") != null)
				newCountsSeparator = ",";
			else if (allCounts.get(from+"->"+to) != null)
				newCountsSeparator = "->";
			
			// If there is a count to the link under consideration,
			// use to calculate the link cost. Otherwise, use a
			// flow the same as the link's capacity (or zero) to do so.
			if (allCounts.containsKey("("+from+newCountsSeparator+to+")") ||
					allCounts.containsKey(from+newCountsSeparator+to))
			{
				if (newCountsSeparator == "->")
				{
					linkCount = (Double)allCounts.get(from+newCountsSeparator+to);
				}
				else
				{
					Class<?> c = allCounts.get("("+from+newCountsSeparator+to+")").getClass();
					if (c.toString().indexOf("String") != -1) // if String
						linkCount = Double.parseDouble( (String)allCounts.get(
								"("+from+newCountsSeparator+to+")"));
					else if (c.toString().indexOf("Double") != -1) // if Double
						linkCount = (Double) allCounts.get("("+from+newCountsSeparator+to+")");
					else
						throw new Exception("Un erreur est en place.");
				}
				
			} else {
				linkCount = linkCapacity;
				//linkCount = 0;
			}
			
			HashMap previousLinkCountsHash = previousLinkCounts.getAllCounts();
			
			// Gets the string separator to the *previous* link counts
			if (previousLinkCountsHash.get("("+from+","+to+")") != null)
				previousCountsSeparator = ",";
			
			// If there is a *previous* count to the link under consideration,
			// use to calculate the link cost. Otherwise, throw an exception
			if (previousLinkCountsHash.containsKey("("+from+previousCountsSeparator+to+")") ||
					previousLinkCountsHash.containsKey(from+previousCountsSeparator+to))
			{
				previousLinkCount = Double.parseDouble( (String)previousLinkCountsHash.get(
						"(" + from + previousCountsSeparator + to + ")")
				);
				
			} else {
				previousLinkCount = linkCapacity;
				//linkCount = 0;
			}
			
			//System.out.println("previousLinkCount: "+previousLinkCount);
			//System.out.println("linkCount ("+from+","+to+"): "+linkCount);
			
			// The new smoothed flow
			linkCount = previousLinkCount - (
					(previousLinkCount - linkCount) * smoothingMultiplier
			);
			
			finalCost = 0;
			
			System.out.println("("+from+","+to+"): "+linkCount+", "+
					linkCapacity+", "+linkCostFreeFlow);
			
			// Calculate the cost according to the set of constants
			// informed by the parameter bprFunctionName
			if (bprFunctionName.equals(BPRFunctionName.BPR))
			{
				finalCost = costFlowFunctionBPR(
						linkCount,
						linkCapacity,
						linkCostFreeFlow
				);
			} else if (bprFunctionName.equals(BPRFunctionName.UBPR))
			{
				finalCost = costFlowFunctionUBPR(
						linkCount,
						linkCapacity,
						linkCostFreeFlow
				);
			} else if (bprFunctionName.equals(BPRFunctionName.BBPR))
			{
				finalCost = costFlowFunctionBBPR(
						linkCount,
						linkCapacity,
						linkCostFreeFlow
				);
			}
			
			// Add it to the link costs object
			lCosts.setCount(
					Integer.parseInt(from),
					Integer.parseInt(to),
					Double.toString(finalCost)
			);
		}
		
		System.out.println("LinkCosts.calculateCostFlowRelationship() finished!");
		//System.out.println("lCosts: "+lCosts);
		return lCosts;
	}
	
	/**
	 * Function that maps flows to costs
	 * 
	 * cost = costFreeFlow * (1 + 0.15 * ((flow / capacity) ^ 4))
	 * 
	 * @param flow the flow in the link
	 * @param capacity the link capacity
	 * @param costFreeFlow the cost to travel on the link without traffic
	 * @return the cost
	 */
	private double costFlowFunctionBPR(double flow, double capacity, double costFreeFlow) {
		double cost = costFreeFlow * (1 + (0.15 * Math.pow(flow/capacity, 4)));
		return cost;
	}
	
	/**
	 * Function that maps flows to costs (referenced in Yang, 1992)
	 * 
	 * cost = costFreeFlow * (1 + 2.62 * ((flow / capacity) ^ 5.00))
	 * 
	 * @param flow the flow in the link
	 * @param capacity the link capacity
	 * @param costFreeFlow the cost to travel on the link without traffic
	 * @return the cost
	 */
	private double costFlowFunctionUBPR(double flow, double capacity, double costFreeFlow) {
		double cost = costFreeFlow * (1 + (2.62 * Math.pow(flow/capacity, 5.00)));
		return cost;
	}
	
	/**
	 * Function that maps flows to costs as envisioned by prof. Les heuristically
	 * 
	 * cost = costFreeFlow * (1 + 1.65 * ((flow / capacity) ^ 4.50))
	 * 
	 * @param flow the flow in the link
	 * @param capacity the link capacity
	 * @param costFreeFlow the cost to travel on the link without traffic
	 * @return the cost
	 */
	private double costFlowFunctionBBPR(double flow, double capacity, double costFreeFlow) {
		double cost = costFreeFlow * (1 + (1.65 * Math.pow(flow/capacity, 4.50)));
		return cost;
	}
}
