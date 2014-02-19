/**
 * 
 */
package odEstimation;

import java.util.HashMap;

/**
 * Class containing the structure to store the OD pairs
 * @author Iacer Calixto
 *
 */
public class ODPairs
{
	/**
	 * A HashMap containing the OD pairs
	 */
	private HashMap <String, String> odPairs;
	
	/**
	 * Constructor
	 */
	public ODPairs()
	{
		this.odPairs = new HashMap<String, String>();
	}
	
	/**
	 * Add an OD pair
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 */
	public void add (String from, String to)
	{
		String od = new String("("+from+","+to+")");
		this.odPairs.put(od, od);
	}
	
	/**
	 * Removes an OD pair
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 */
	public void remove (String from, String to)
	{
		String od = new String("("+from+","+to+")");
		this.odPairs.remove(od);
	}
	
	/**
	 * Returns the HashMap containing the OD pairs
	 * 
	 * @return
	 */
	public HashMap <String, String> getODPairs()
	{
		return this.odPairs;
	}
	
	/**
	 * Returns the amount of added OD pairs
	 * 
	 * @return
	 */
	public int getCount()
	{
		return this.odPairs.size();
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		return "OD pairs: " + this.odPairs.toString() + "\n";
	}
}
