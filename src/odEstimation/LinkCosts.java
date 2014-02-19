/**
 * 
 */
package odEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class containing the structure to store the link costs
 * @author Iacer Calixto
 */
public class LinkCosts
{
	/**
	 * A HashMap containing the link costs
	 */
	private HashMap linkCosts;
	
	/**
	 * Constructor
	 */
	public LinkCosts()
	{
		this.linkCosts = new HashMap();
	}
	
	/**
	 * Add a cost to a link (arc) between two given nodes
	 * (an origin node and a destination node)
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param count The cost of the travel through the arc (link), between the origin and the destination
	 */
	public void setCount (int from, int to, String count)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCosts.put(edge, count);
		
		double dblCount = Double.parseDouble( count );
	}
	
	/**
	 * Remove an existing cost from a link (arc) between two given nodes
	 * (an origin node and a destination node)
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 */
	public void unsetCount (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCosts.remove(edge);
	}
	
	/**
	 * Gets the cost of the link connecting the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public String getCountsBetween (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		String count = null;
		if (this.linkCosts.containsKey(edge))
				count = (String) this.linkCosts.get(edge);
		return count;
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		//HashMap retVal = new HashMap();
		String strOut = "Link costs to costy edges:\n";
		
		Iterator itRNodes = this.linkCosts.entrySet().iterator();
		while (itRNodes.hasNext()) {
			Map.Entry nodeEntry = (Map.Entry) itRNodes.next();
			String key = (String) nodeEntry.getKey();
			String value = (String) nodeEntry.getValue();
			
			String from = key.split(",")[0].replace("(", "").trim();
			String to = key.split(",")[1].replace(")", "").trim();
			
			if (Integer.parseInt(from) > 1000 &&
					10 * Integer.parseInt(from) == Integer.parseInt(to))
			{
				strOut += key + "=" + value + "\n";
				//retVal.put(key, value);
			}
		}
		
		strOut += "\n\nLink costs to dummy edges:\n";
		
		itRNodes = this.linkCosts.entrySet().iterator();
		while (itRNodes.hasNext()) {
			Map.Entry nodeEntry = (Map.Entry) itRNodes.next();
			String key = (String) nodeEntry.getKey();
			String value = (String) nodeEntry.getValue();
			
			String from = key.split(",")[0].replace("(", "").trim();
			String to = key.split(",")[1].replace(")", "").trim();
			
			if (Integer.parseInt(from) < 1000 ||
					Integer.parseInt(from) != 10 * Integer.parseInt(to))
			{
				strOut += key + "=" + value + "\n";
				//retVal.put(key, value);
			}
		}
		
		//return "Link costs: " + retVal.toString();
		return strOut;
	}
}
