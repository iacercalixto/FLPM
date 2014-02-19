/**
 * 
 */
package odEstimation;

import java.util.HashMap;

/**
 * Class containing the structure to store the OD matrix
 * 
 * @author Iacer Calixto
 */
public class ODMatrix
{
	/**
	 * A HashMap containing the OD counts
	 */
	private HashMap<String, Double> odMatrix;
	
	/**
	 * Constructor
	 */
	public ODMatrix()
	{
		this.odMatrix = new HashMap<String, Double>();
	}
	
	/**
	 * Set an OD count between two nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param count The value of the OD count
	 */
	public void setCount (String from, String to, Double count)
	{
		String edge = new String("("+from+","+to+")");
		this.odMatrix.put(edge, count);
	}
	
	/**
	 * Unsets an OD count (and its error measurements) between two nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 */
	public void unsetCount (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		this.odMatrix.remove(edge);
	}
	
	/**
	 * Gets the OD entry between the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public Double getCountBetween (String from, String to)
	throws Exception
	{
		String edge = new String("("+from+","+to+")");
		double count = -1;
		
		// If the OD matrix doesn't contain the link, assume it's count is zero
		if (this.odMatrix.containsKey(edge))
			count = this.odMatrix.get(edge);
		else
			count = 0;
			//throw new Exception("There is no OD entry between nodes "+from+" and "+to+".");
		return count;
	}
	
	/**
	 * Returns the HashMap containing the OD matrix
	 * 
	 * @return
	 */
	public HashMap getODMatrix()
	{
		return this.odMatrix;
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		return "OD matrix entries: " + this.odMatrix.toString();
	}
}
