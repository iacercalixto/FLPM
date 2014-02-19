/**
 * 
 */
package odEstimation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class containing the structure to store the link counts
 * @author Iacer Calixto
 *
 */
public class LinkCounts
{
	/**
	 * A HashMap containing the link (arc) counts
	 */
	private HashMap linkCounts;
	
	/**
	 * A HashMap containing the error measurements in the link (arc) counts
	 * The error value is interpreted as always negative
	 */
	private HashMap errorMeasurementBelow;
	
	/**
	 * A HashMap containing the error measurements in the link (arc) counts
	 * The error value is interpreted as always positive
	 */
	private HashMap errorMeasurementAbove;
	
	/**
	 * The default value used to error measurements
	 */
	private static double absoluteLinkErrorMeasurement = 20.0;
	
	/**
	 * Constructor
	 */
	public LinkCounts()
	{
		linkCounts = new HashMap();
		errorMeasurementBelow = new HashMap();
		errorMeasurementAbove = new HashMap();
	}
	
	/**
	 * Set a link count between two nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param count The value of the link count
	 */
	public void setCount (String from, String to, String count)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCounts.put(edge, count);
		
		// Set the error values as the default
		this.errorMeasurementAbove.put(edge, this.absoluteLinkErrorMeasurement);
		this.errorMeasurementBelow.put(edge, this.absoluteLinkErrorMeasurement);
	}
	
	/**
	 * Sets a link count between two nodes and its error measurements
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param count The value of the link count
	 * @param errorBelow The value of the error measurement (below the given value)
	 * @param errorAbove The value of the error measurement (above the given value)
	 */
	public void setCount (String from, String to, String count,
			double errorBelow, double errorAbove)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCounts.put(edge, count);
		
		// Set the error values
		this.errorMeasurementAbove.put(edge, errorAbove);
		this.errorMeasurementBelow.put(edge, errorBelow);
	}
	
	/**
	 * Unsets a link count (and its error measurements) between two nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 */
	public void unsetCount (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		this.linkCounts.remove(edge);
		this.errorMeasurementAbove.remove(edge);
		this.errorMeasurementBelow.remove(edge);
	}
	
	/**
	 * @param from The origin node
	 * @param to The destination node
	 * @return Whether the link has counts or not
	 */
	public boolean hasCounts(String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		return (this.linkCounts.containsKey(edge));
	}
	
	/**
	 * Gets the link counts between the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public String getCountsBetween (int from, int to)
	{
		String edge = new String("("+from+","+to+")");
		String count = null;
		if (this.linkCounts.containsKey(edge))
				count = (String) this.linkCounts.get(edge);
		
		return count;
	}
	
	/**
	 * Gets the error measurements in link counts between the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public String getErrorsBetween (int from, int to)
	{
		String edge = new String("("+from+","+to+")");
		String countBelow = null;
		String countAbove = null;
		if (this.errorMeasurementBelow.containsKey(edge))
				countBelow = Double.toString((Double) this.errorMeasurementBelow.get(edge));
		if (this.errorMeasurementAbove.containsKey(edge))
				countAbove = Double.toString((Double) this.errorMeasurementAbove.get(edge));
		
		return countBelow + " - " + countAbove;
	}
	
	/**
	 * Returns the link counts HashMap
	 * 
	 * @return
	 */
	public HashMap getAllCounts()
	{
		return this.linkCounts;
	}
	
	/**
	 * Returns the error measurements (above) HashMap
	 * 
	 * @return
	 */
	public HashMap getAllErrorsAbove()
	{
		return this.errorMeasurementAbove;
	}
	
	/**
	 * Returns the error measurements (below) HashMap
	 * 
	 * @return
	 */
	public HashMap getAllErrorsBelow()
	{
		return this.errorMeasurementBelow;
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		return "Link counts: " + this.linkCounts.toString() + "\n" + 
				"Errors (-): " + this.errorMeasurementBelow.toString() + "\n" +
				"Errors (+): " + this.errorMeasurementAbove.toString();
	}
	
	public LinkCounts copyLinkCounts()
	{
		LinkCounts retVal = new LinkCounts();
		
		Iterator it = linkCounts.entrySet().iterator();
		Iterator itEB = errorMeasurementBelow.entrySet().iterator();
		Iterator itEA = errorMeasurementAbove.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			String count = (String) entry.getValue();
			
			String[] fromTo = key.split(",");
			String from = fromTo[0].trim().replace("(", "");
			String to = fromTo[1].trim().replace(")", "");
			
			// Iterate through the errors as well
			Map.Entry entryEA = (Map.Entry) itEA.next();
			Map.Entry entryEB = (Map.Entry) itEB.next();
			
			Double errorBelow = (Double) entryEB.getValue();
			Double errorAbove = (Double) entryEA.getValue();
			
			retVal.setCount(from, to, count, errorBelow, errorAbove);
		}
		
		return retVal;
	}
}