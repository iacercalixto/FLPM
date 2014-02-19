/**
 * 
 */
package odEstimation;

import java.util.HashMap;

/**
 * Class containing the structure to store the link capacities
 * 
 * @author Iacer Calixto
 */
public class LinkCapacities
{
	/**
	 * A HashMap containing the link capacities
	 */
	private HashMap<String, Double> linkCapacities;
	
	/**
	 * A HashMap containing the cost to travel on the link
	 * without traffic
	 */
	private HashMap<String, Double> linkCostFreeFlow;
	
	/**
	 * Constructor
	 */
	public LinkCapacities()
	{
		this.linkCapacities = new HashMap<String, Double>();
		this.linkCostFreeFlow = new HashMap<String, Double>();
	}
	
	/**
	 * Adds a capacity to a link (arc) between two given nodes
	 * (an origin node and a destination node)
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param capacity The capacity of the arc (link)
	 * @param costFreeFlow The cost to travel on the link without traffic
	 */
	public void setCapacity (int from, int to, double capacity, double costFreeFlow)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCapacities.put(edge, capacity);
		this.linkCostFreeFlow.put(edge, costFreeFlow);
	}
	
	/**
	 * Remove an existing capacity from a link (arc) between
	 * an origin node and a destination node
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 */
	public void unsetCapacity (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCapacities.remove(edge);
		this.linkCostFreeFlow.remove(edge);
	}
	
	/**
	 * Gets the capacity of the link connecting the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public double getCapacityBetween (String from, String to)
	throws Exception
	{
		String edge = new String("("+from+","+to+")");
		double capacity = -1;
		if (this.linkCapacities.containsKey(edge))
				capacity = this.linkCapacities.get(edge);
		
		if (capacity==-1)
			throw new Exception("Link ("+from+","+to+") not found.");
		
		return capacity;
	}
	
	/**
	 * Gets the travel cost of the link (without traffic)
	 * connecting the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public double getCostFreeFlow (String from, String to)
	throws Exception
	{
		String edge = new String("("+from+","+to+")");
		double costFreeFlow = -1;
		if (this.linkCostFreeFlow.containsKey(edge))
			costFreeFlow = this.linkCostFreeFlow.get(edge);
		
		if (costFreeFlow==-1)
			throw new Exception("Link ("+from+","+to+") not found.");
		
		return costFreeFlow;
	}
	
	/**
	 * @return the link capacities
	 */
	public HashMap<String, Double> getLinkCapacities()
	{
		return this.linkCapacities;
	}
	
	/**
	 * @return the congestion-free cost of the link
	 */
	public HashMap<String, Double> getLinkCostFreeFlow()
	{
		return this.linkCostFreeFlow;
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		return "Link capacities: "+this.linkCapacities.toString() + "\n" +
			"Link costs (free flow): "+this.linkCostFreeFlow.toString();
	}
}
