/**
 * 
 */
package odEstimation;

import java.util.HashMap;

/**
 * Class containing the structure to store the OD counts
 * 
 * @author Iacer Calixto
 */
public class ODCounts
{
	/**
	 * A HashMap containing the OD counts
	 */
	private HashMap odCounts;
	
	/**
	 * A HashMap containing the origin counts (counts that originate
	 * in a node but for which the destination is unknown)
	 */
	private HashMap originCounts;
	
	/**
	 * A HashMap containing the destination counts (counts that are
	 * attracted to a node but for which the origin is unknown)
	 */
	private HashMap destinationCounts;
	
	/**
	 * A HashMap containing the error measurements in the OD counts
	 * The error value is interpreted as always negative
	 */
	private HashMap errorMeasurementBelow;
	
	/**
	 * A HashMap containing the error measurements in the OD counts
	 * The error value is interpreted as always positive
	 */
	private HashMap errorMeasurementAbove;
	
	/**
	 * A HashMap containing the error measurements in the origin counts
	 * The error value is interpreted as always negative
	 */
	private HashMap originErrorMeasurementBelow;
	
	/**
	 * A HashMap containing the error measurements in the origin counts
	 * The error value is interpreted as always positive
	 */
	private HashMap originErrorMeasurementAbove;
	
	/**
	 * A HashMap containing the error measurements in the destination counts
	 * The error value is interpreted as always negative
	 */
	private HashMap destinationErrorMeasurementBelow;
	
	/**
	 * A HashMap containing the error measurements in the destination counts
	 * The error value is interpreted as always positive
	 */
	private HashMap destinationErrorMeasurementAbove;
	
	/**
	 * The default value used to error measurements
	 */
	private static double absoluteLinkErrorMeasurement = 20.0;
	
	/**
	 * Constructor
	 */
	public ODCounts()
	{
		this.odCounts = new HashMap();
		this.originCounts = new HashMap();
		this.destinationCounts = new HashMap();
		errorMeasurementBelow = new HashMap();
		errorMeasurementAbove = new HashMap();
		originErrorMeasurementBelow = new HashMap();
		originErrorMeasurementAbove = new HashMap();
		destinationErrorMeasurementBelow = new HashMap();
		destinationErrorMeasurementAbove = new HashMap();
	}
	
	/**
	 * Set an OD count between two nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param count The value of the OD count
	 */
	public void setCount (String from, String to, String count)
	{
		String edge = new String("("+from+","+to+")");
		this.odCounts.put(edge, count);
		
		// Set the error values as the default
		this.errorMeasurementAbove.put(edge, this.absoluteLinkErrorMeasurement);
		this.errorMeasurementBelow.put(edge, this.absoluteLinkErrorMeasurement);
	}
	
	/**
	 * Set an OD count between two nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @param count The value of the OD count
	 * @param errorBelow The value of the error measurement (below the given value)
	 * @param errorAbove The value of the error measurement (above the given value)
	 */
	public void setCount (String from, String to, String count, double errorBelow, double errorAbove)
	{
		String edge = new String("("+from+","+to+")");
		this.odCounts.put(edge, count);
		
		// Set the error values
		this.errorMeasurementAbove.put(edge, errorAbove);
		this.errorMeasurementBelow.put(edge, errorBelow);
	}
	
	/**
	 * Set a count originating in a node without destination
	 * 
	 * @param from The origin node
	 * @param count The value of the origin count
	 */
	public void setOriginCount (String from, String count)
	{
		//String edge = new String("("+from+","+to+")");
		this.originCounts.put(from, count);
		
		// Set the error values as the default
		this.originErrorMeasurementAbove.put(from, this.absoluteLinkErrorMeasurement);
		this.originErrorMeasurementBelow.put(from, this.absoluteLinkErrorMeasurement);
	}
	
	/**
	 * Set a count originating in a node without destination
	 * 
	 * @param from The origin node
	 * @param count The value of the OD count
	 * @param errorBelow The value of the error measurement (below the given value)
	 * @param errorAbove The value of the error measurement (above the given value)
	 */
	public void setOriginCount (String from, String count, double errorBelow, double errorAbove)
	{
		//String edge = new String("("+from+","+to+")");
		this.originCounts.put(from, count);
		
		// Set the error values
		this.originErrorMeasurementAbove.put(from, errorAbove);
		this.originErrorMeasurementBelow.put(from, errorBelow);
	}
	
	/**
	 * Set a count attracted to a node with an unknown origin
	 * 
	 * @param to The destination node
	 * @param count The value of the destination count
	 */
	public void setDestinationCount (String to, String count)
	{
		//String edge = new String("("+from+","+to+")");
		this.destinationCounts.put(to, count);
		
		// Set the error values as the default
		this.destinationErrorMeasurementAbove.put(to, this.absoluteLinkErrorMeasurement);
		this.destinationErrorMeasurementBelow.put(to, this.absoluteLinkErrorMeasurement);
	}
	
	/**
	 * Set a count attracted to a node with an unknown origin
	 * 
	 * @param to The destination node
	 * @param count The value of the OD count
	 * @param errorBelow The value of the error measurement (below the given value)
	 * @param errorAbove The value of the error measurement (above the given value)
	 */
	public void setDestinationCount (String to, String count, double errorBelow, double errorAbove)
	{
		//String edge = new String("("+from+","+to+")");
		this.destinationCounts.put(to, count);
		
		// Set the error values
		this.destinationErrorMeasurementAbove.put(to, errorAbove);
		this.destinationErrorMeasurementBelow.put(to, errorBelow);
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
		this.odCounts.remove(edge);
		this.errorMeasurementAbove.remove(edge);
		this.errorMeasurementBelow.remove(edge);
	}
	
	/**
	 * Unsets an origin count (and its error measurements) between two nodes
	 * 
	 * @param from The origin node
	 */
	public void unsetOriginCount (String from)
	{
		//String edge = new String("("+from+","+to+")");
		this.originCounts.remove(from);
		this.originErrorMeasurementAbove.remove(from);
		this.originErrorMeasurementBelow.remove(from);
	}
	
	/**
	 * Unsets a destination count (and its error measurements) between two nodes
	 * 
	 * @param to The destination node
	 */
	public void unsetDestinationCount (String to)
	{
		//String edge = new String("("+from+","+to+")");
		this.destinationCounts.remove(to);
		this.destinationErrorMeasurementAbove.remove(to);
		this.destinationErrorMeasurementBelow.remove(to);
	}
	
	/**
	 * Gets the OD counts between the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public String getCountsBetween (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		String count = null;
		if (this.odCounts.containsKey(edge))
				count = (String) this.odCounts.get(edge);
		return count;
	}
	
	/**
	 * Gets the error measurements in OD counts between the given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 */
	public String getErrorsBetween (String from, String to)
	{
		String edge = new String("("+from+","+to+")");
		String countBelow = null;
		String countAbove = null;
		if (this.errorMeasurementBelow.containsKey(edge))
				countBelow = (String) this.errorMeasurementBelow.get(edge);
		if (this.errorMeasurementAbove.containsKey(edge))
				countAbove = (String) this.errorMeasurementAbove.get(edge);
		
		return countBelow + " - " + countAbove;
	}
	
	/**
	 * Returns the HashMap containing the OD counts
	 * 
	 * @return
	 */
	public HashMap getODCounts()
	{
		return this.odCounts;
	}
	
	/**
	 * Returns the HashMap containing the origin counts
	 * 
	 * @return
	 */
	public HashMap getOriginCounts()
	{
		return this.originCounts;
	}
	
	/**
	 * Returns the HashMap containing the destination counts
	 * 
	 * @return
	 */
	public HashMap getDestinationCounts()
	{
		return this.destinationCounts;
	}
	
	/**
	 * Returns the HashMap containing the error measurements (above)
	 * 
	 * @return
	 */
	public HashMap getAllErrorsAbove()
	{
		return this.errorMeasurementAbove;
	}
	
	/**
	 * Returns the HashMap containing the error measurements (below)
	 * 
	 * @return
	 */
	public HashMap getAllErrorsBelow()
	{
		return this.errorMeasurementBelow;
	}
	
	/**
	 * Returns the HashMap containing the origin-only
	 * error measurements (above)
	 * 
	 * @return
	 */
	public HashMap getOriginAllErrorsAbove()
	{
		return this.originErrorMeasurementAbove;
	}
	
	/**
	 * Returns the HashMap containing the origin-only
	 * error measurements (below)
	 * 
	 * @return
	 */
	public HashMap getOriginAllErrorsBelow()
	{
		return this.originErrorMeasurementBelow;
	}
	
	/**
	 * Returns the HashMap containing the destination-only
	 * error measurements (above)
	 * 
	 * @return
	 */
	public HashMap getDestinationAllErrorsAbove()
	{
		return this.destinationErrorMeasurementAbove;
	}
	
	/**
	 * Returns the HashMap containing the destination-only
	 * error measurements (below)
	 * 
	 * @return
	 */
	public HashMap getDestinationAllErrorsBelow()
	{
		return this.destinationErrorMeasurementBelow;
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		return "OD counts: " + this.odCounts.toString() + "\n" + 
				"Errors (-): " + this.errorMeasurementBelow.toString() + "\n" +
				"Errors (+): " + this.errorMeasurementAbove.toString() + "\n" +
				"Origin counts: " + this.originCounts.toString() + "\n" + 
				"Errors (-): " + this.originErrorMeasurementBelow.toString() + "\n" +
				"Errors (+): " + this.originErrorMeasurementAbove.toString() + "\n" +
				"Destination counts: " + this.destinationCounts.toString() + "\n" + 
				"Errors (-): " + this.destinationErrorMeasurementBelow.toString() + "\n" +
				"Errors (+): " + this.destinationErrorMeasurementAbove.toString();
	}
}
