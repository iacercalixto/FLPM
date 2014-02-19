/**
 * 
 */
package odEstimation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.management.ReflectionException;

/**
 * Class containing the structure to store the route costs
 * @author Iacer Calixto
 *
 */
public class RouteCosts implements Cloneable
{
	/**
	 * Whether the system is in debug mode or not
	 */
	private static boolean debugMode = false;
	
	/**
	 * A counter with the quantity of routes in the model
	 */
	private int counter;
	
	/**
	 * An ArrayList containing the OD pairs
	 */
	private LinkedHashMap<String, LinkedHashMap<Integer, ?>[]> odPairs;
	
	/**
	 * Constructor
	 */
	public RouteCosts()
	{
		// Each entry in the OD pairs LinkedHashMap will contain
		// a reference to an ArrayList containing its routes
		// (references by the 3 LinkedHashMap objects).
		this.odPairs = new LinkedHashMap<String, LinkedHashMap<Integer, ?>[]>();
	}
	
	/**
	 * Adds a cost to a route. Use the default
	 * 
	 * @param routeIndex The index used to reference the route.
	 * @param routeNodes An array containing the nodes of which the route is composed of.
	 * @param cost The cost of traveling the given route.
	 */
	public void setCount(String routeIndex, String[] routeNodes, String cost)
	{
		String text = "(";
		for (String vertex : routeNodes) {
			text.concat(vertex+",");
		}
		text.substring(0, text.length()-2).concat(")");
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		((LinkedHashMap<Integer, Integer>) hashMaps[0]).
			put(Integer.valueOf(routeIndex), Integer.valueOf(routeIndex));
		((LinkedHashMap<Integer, String>) hashMaps[1]).
			put(Integer.valueOf(routeIndex), text);
		((LinkedHashMap<Integer, String>) hashMaps[2]).
			put(Integer.valueOf(routeIndex), cost);
		
		this.counter++;
	}
	
	/**
	 * Adds a cost to a route.
	 * 
	 * @param routeIndex The index used to reference the route.
	 * @param routeNodes A String representation of the nodes of which the route is composed of.
	 * @param cost The cost of traveling the given route.
	 */
	public void setCount(String routeIndex, String routeNodes, String cost)
	{
		String[] fromTo = routeNodes.split(",");
		fromTo[0] = fromTo[0].replace("(","").trim();
		fromTo[ fromTo.length - 1 ] = fromTo[ fromTo.length - 1 ].replace(")","").trim();
		
		String text = "("+fromTo[0]+","+fromTo[ fromTo.length - 1 ]+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		((LinkedHashMap<Integer, Integer>) hashMaps[0]).
			put(Integer.valueOf(routeIndex), Integer.valueOf(routeIndex));
		((LinkedHashMap<Integer, String>) hashMaps[1]).
			put(Integer.valueOf(routeIndex),  new String("(").concat(routeNodes).concat(")"));
		((LinkedHashMap<Integer, String>) hashMaps[2]).
			put(Integer.valueOf(routeIndex), cost);
		
		this.counter++;
		
		//System.out.println("hashMaps object: " + hashMaps[0] + ", " + hashMaps[1] + ", " + hashMaps[2]);
	}
	
	/**
	 * Returns the HashMap containing the route costs.
	 * @return
	 */
	public LinkedHashMap<Integer, ?> getRouteCosts(String from, String to)
	{
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		return ( hashMaps[2] );
	}
	
	/**
	 * Returns the HashMap containing the non-least-cost route costs.
	 * @return
	 */
	public LinkedHashMap<Integer, String> getNonLeastCostRouteCosts(String from, String to)
	{
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		LinkedHashMap<Integer, String> retVal = new LinkedHashMap<Integer, String>();
		
		double dblLeastCost = Double.POSITIVE_INFINITY;
		//int intLeastCostIndex = -1;
		
		// Obtain the least cost
		Iterator itIndexes = hashMaps[0].entrySet().iterator();
		Iterator itNodes = hashMaps[1].entrySet().iterator();
		Iterator itCosts = hashMaps[2].entrySet().iterator();
		
		// Iterate through the route costs and the route indexes concomitantly
		while (itCosts.hasNext())
		{
			Map.Entry entryCosts = (Map.Entry) itCosts.next();
			Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			
			// Add the cost element to the return object
			retVal.put((Integer)entryCosts.getKey(), (String)entryCosts.getValue());
			
			// Keep track of the least cost value
			if (Double.parseDouble((String)entryCosts.getValue()) < dblLeastCost)
				dblLeastCost = Double.parseDouble((String)entryCosts.getValue());
		}
		
		// Remove all the least-cost entries
		ArrayList<Integer> nonLeastCostIndexes = new ArrayList<Integer>();
		// Initialize the costs iterator
		itCosts = hashMaps[2].entrySet().iterator();
		// Iterate through the route nodes
		while (itNodes.hasNext())
		{
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			Map.Entry entryCosts = (Map.Entry) itCosts.next();
			
			// If the route under consideration is a least-cost one
			if ((Double)entryCosts.getValue()<=dblLeastCost)
				retVal.remove(entryNodes.getKey());
		}
		
		return ((LinkedHashMap<Integer, String>)retVal);
		
		//String text = "("+from+","+to+")";
		//LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		//return ( hashMaps[2] );
	}
	
	/**
	 * Returns the HashMap containing the route nodes.
	 * 
	 * @return
	 */
	public HashMap<Integer, String> getRouteNodes(String from, String to)
	{
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		return ((HashMap<Integer, String>)hashMaps[1]);
	}
	
	/**
	 * Returns the HashMap containing the non-least-cost route nodes.
	 * 
	 * @return
	 */
	public HashMap<Integer, String> getNonLeastCostRouteNodes(String from, String to)
	{
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		LinkedHashMap<Integer, String> retVal = new LinkedHashMap<Integer, String>();
		
		double dblLeastCost = Double.POSITIVE_INFINITY;
		//int intLeastCostIndex = -1;
		
		// Obtain the least cost
		Iterator itIndexes = hashMaps[0].entrySet().iterator();
		Iterator itNodes = hashMaps[1].entrySet().iterator();
		Iterator itCosts = hashMaps[2].entrySet().iterator();
		
		// Iterate through the route costs and the route indexes concomitantly
		while (itCosts.hasNext())
		{
			Map.Entry entryCosts = (Map.Entry) itCosts.next();
			Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			
			// Add the node element to the return object
			retVal.put((Integer)entryNodes.getKey(), (String)entryNodes.getValue());
			
			// Keep track of the least cost value
			if (Double.parseDouble((String)entryCosts.getValue()) < dblLeastCost)
				dblLeastCost = Double.parseDouble((String)entryCosts.getValue());
		}
		
		// Remove all the least-cost entries
		ArrayList<Integer> nonLeastCostIndexes = new ArrayList<Integer>();
		// Re-initialize the costs and nodes iterator
		itCosts = hashMaps[2].entrySet().iterator();
		itNodes = hashMaps[1].entrySet().iterator();
		// Iterate through the route nodes
		while (itNodes.hasNext())
		{
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			Map.Entry entryCosts = (Map.Entry) itCosts.next();
			
			// If the route under consideration is a least-cost one
			if (Double.parseDouble( (String)entryCosts.getValue() ) <= dblLeastCost)
				retVal.remove(entryNodes.getKey());
		}
		
		return ((HashMap<Integer, String>)retVal);
	}
	
	/**
	 * Returns the HashMap containing the route indexes.
	 * 
	 * @return
	 */
	public HashMap<Integer, Integer> getRouteIndexes(String from, String to)
	{
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		return ((HashMap<Integer, Integer>)hashMaps[0]);
	}
	
	/**
	 * Obtains all the existing routes between two given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap<String, String> getRoutesBetween(String from, String to) throws Exception
	{
		if (this.debugMode)
			System.out.println("Beginning of RouteCosts.getRoutesBetween()");
		
		LinkedHashMap<String, String> retVal = new LinkedHashMap<String, String>();
		
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		// Iterate through the nodes
		Iterator itNodes = hashMaps[1].entrySet().iterator();
		Iterator itCosts = hashMaps[2].entrySet().iterator();
		while (itNodes.hasNext())
		{
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			Map.Entry entryCosts = null;
			
			try {
				entryCosts = (Map.Entry) itCosts.next();
				if (!entryNodes.getKey().toString().equalsIgnoreCase(entryCosts.getKey().toString()))
					throw new Exception("The cost found doesn't match route " + entryNodes);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Add the entry to the return object
			retVal.put((String)entryNodes.getValue(), (String)entryCosts.getValue());
		}
		
		if (this.debugMode)
			System.out.println("Leaving RouteCosts.getRoutesBetween()");
		
		return retVal;
	}
	
	/**
	 * Obtains the greatest-cost route between two given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap<String, String> getGreatestCostRouteBetween(String from, String to) throws Exception
	{
		LinkedHashMap<String, String> allRoutes = new LinkedHashMap<String, String>();
		
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		// Iterate through the nodes
		Iterator itNodes = hashMaps[1].entrySet().iterator();
		while (itNodes.hasNext())
		{
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			Map.Entry entryCosts = null;
			
			// Iterate through the costs concomitantly
			Iterator itCosts = hashMaps[2].entrySet().iterator();
			while (itCosts.hasNext())
				entryCosts = (Map.Entry) itCosts.next();
			
			if (entryCosts == null) throw new Exception("No cost found to route " +
					entryNodes);
			
			// Add the entry to the return object
			allRoutes.put((String)entryNodes.getValue(), (String)entryCosts.getValue());
		}
		
		LinkedHashMap<String, String> retVal = new LinkedHashMap<String, String>();
		
		// Add only the greatest-cost route to the retVal object
		double greatestRouteKey = -1;
		String greatestRouteValue = "";
		
		Iterator<?> itAllRoutes = allRoutes.entrySet().iterator();
		while ( itAllRoutes.hasNext() )
		{
			Map.Entry nodeEntry = (Map.Entry) itAllRoutes.next();
			String routeKey = (String) nodeEntry.getValue();
			String routeValue = (String) nodeEntry.getKey();
			
			if (greatestRouteKey == -1)
			{
				greatestRouteValue = routeValue;
				greatestRouteKey = Double.parseDouble(routeKey);
				continue;
			}
			
			if (Double.parseDouble(routeKey) > greatestRouteKey)
			{
				greatestRouteValue = routeValue;
				greatestRouteKey = Double.parseDouble(routeKey);
			}
		}
		
		retVal.put(greatestRouteValue, Double.toString(greatestRouteKey));
		
		//System.out.println("retVal: " + retVal);
		
		return retVal;
	}
	
	/**
	 * Obtains all the existing route indexes between two given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap getRoutesIndexesBetween(String from, String to) throws Exception
	{
		if (this.debugMode)
			System.out.println("Beginning of RouteCosts.getRoutesIndexesBetween()");
		
		LinkedHashMap<Integer, Integer> retVal = new LinkedHashMap<Integer, Integer>();
		
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		// Iterate through the nodes
		Iterator itNodes = hashMaps[1].entrySet().iterator();
		while (itNodes.hasNext())
		{
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			
			// Add the entry to the return object
			retVal.put(
					(Integer)entryNodes.getKey(),
					(Integer)entryNodes.getKey()
			);
		}
		
		if (this.debugMode)
			System.out.println("End of RouteCosts.getRoutesIndexesBetween()");
		
		return retVal;
	}
	
	/**
	 * Obtains the greatest-cost existing route index between two given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap getGreatestCostRouteIndexBetween(String from, String to) throws Exception
	{
		LinkedHashMap<String, String> allRoutes = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> retVal = new LinkedHashMap<String, String>();
		
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		// Iterate through the nodes
		Iterator itNodes = hashMaps[1].entrySet().iterator();
		Iterator itIndexes = hashMaps[2].entrySet().iterator();
		while (itNodes.hasNext())
		{
			Map.Entry entryNodes = (Map.Entry) itNodes.next();
			String strIndex = "";
			
			// Iterate through the costs concomitantly
			Map.Entry indexEntry = (Map.Entry) itIndexes.next();
			strIndex = (String)indexEntry.getValue();
			
			// Add the entry to the return object
			allRoutes.put((String)entryNodes.getValue(), strIndex);
		}
		
		// allRoutes object has (String,String):
		// (1,3,2,6)=12.0 or nodeValue=index
		
		// Add only the greatest-cost routes to the retVal object
		ArrayList <Integer> greatestRouteKey = new ArrayList <Integer>();
		ArrayList <String> greatestRouteValue = new ArrayList <String>();
		
		int counter = 0;
		
		Iterator<?> itAllRoutes = allRoutes.entrySet().iterator();
		while ( itAllRoutes.hasNext() )
		{
			Map.Entry nodeEntry = (Map.Entry) itAllRoutes.next();
			String routeKey = (String) nodeEntry.getValue();
			String routeValue = (String) nodeEntry.getKey();
			
			// Create the element entry
			greatestRouteKey.add(counter, -1);
			greatestRouteValue.add(counter, "");
			
			if (counter == 0)
			{
				greatestRouteValue.set(0, routeValue );
				greatestRouteKey.set(0, (int)Double.parseDouble(routeKey) );
				counter++;
			} else
			{
				if ( (int)Double.parseDouble(routeKey) >= greatestRouteKey.get(counter))
				{
					greatestRouteValue.set(counter, routeValue );
					greatestRouteKey.set(counter, (int)Double.parseDouble(routeKey) );
					counter++;
				}
			}
		}
		
		// Add the entries to the retVal object
		for (int i=0; i<greatestRouteValue.size(); i++)
			retVal.put(greatestRouteValue.get(i), Double.toString(greatestRouteKey.get(i)));
		
		return retVal;
	}
	
	/**
	 * Obtains the non-least-cost existing routes indexes between two given nodes
	 * 
	 * @param from The origin node
	 * @param to The destination node
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap getNonLeastCostRoutesIndexesBetween(String from, String to) throws Exception
	{
		LinkedHashMap<String, Object> retVal = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> costVal = new LinkedHashMap<String, Object>();
		
		String text = "("+from+","+to+")";
		
		// Check whether the OD pair has already been created in the model
		// If not, create it dynamically
		checkCreateODPair(text);
		
		LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(text);
		
		Iterator<?> itRNodes = hashMaps[1].entrySet().iterator();
		Iterator<?> itRIndexes = hashMaps[0].entrySet().iterator();
		Iterator<?> itRCosts = hashMaps[2].entrySet().iterator();
		Object index, cost;
		String rFrom, rTo;
		String[] temp;
		String leastCostRoute = "";
		
		double leastCost = -1;
		double actualCost = -1;
		
		// Iterate through the routes nodes
		while ( itRNodes.hasNext() )
		{
			Map.Entry nodeEntry = (Map.Entry) itRNodes.next();
			String pairs = (String) nodeEntry.getValue();
			
			// Iterate through the routes indexes and costs concomitantly
			Map.Entry indexEntry = (Map.Entry) itRIndexes.next();
			Map.Entry costEntry = (Map.Entry) itRCosts.next();
			
			index = indexEntry.getValue();
			cost = costEntry.getValue();
			
			temp = pairs.split(",");
			rFrom = temp[ 0 ].substring(1, temp[0].length()).trim();
			rTo = temp[ temp.length - 1 ].substring(0, temp[ temp.length - 1 ].length() -1).trim();
			
			// If the route found matches the origin and destination given, add it to the results
			// Keep record of the greatest-cost route and remove it by the end
			retVal.put(pairs, index);
			costVal.put(pairs, cost);
			
			if (leastCost == -1)
			{
				leastCost = Double.parseDouble(((String) cost));
				leastCostRoute = pairs;
				continue;
			}
			
			actualCost = Double.parseDouble(((String) cost));
			
			if (actualCost < leastCost)
			{
				leastCostRoute = pairs;
				leastCost = actualCost;
			}
		}
		
		
		// Remove the least-cost route(s) from the retVal object
		Iterator itRet = retVal.entrySet().iterator();
		Iterator itCost = costVal.entrySet().iterator();
		while (itRet.hasNext())
		{
			Map.Entry entry = (Map.Entry) itRet.next();
			Map.Entry entryCost = (Map.Entry) itCost.next();
			
			if (Double.parseDouble(entryCost.getValue().toString()) == leastCost)
			{
				itRet.remove();
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns the OD pairs for every and each existing route
	 * in alphabetical (lexicographical) order
	 * 
	 * @return
	 * @throws Exception
	 */
	public TreeSet<String> getODPairs() throws Exception
	{
		TreeSet<String> retVal = new TreeSet<String>();
		
		Iterator it = this.odPairs.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			
			String text = (String)entry.getKey();
			String[] fromTo = text.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			retVal.add(fromTo[0]+","+fromTo[1]);
		}
		
		return retVal;
	}
	
	/**
	 * Returns a node, part of a route, given its index
	 * 
	 * @param index The index of the node.
	 * @return
	 */
	public String getRouteNodeByIndex(String index)
	{
		String retVal = null;
		
		Iterator it = this.odPairs.entrySet().iterator();
		while (it.hasNext() && retVal == null)
		{
			Map.Entry entry = (Map.Entry) it.next();
			
			String text = (String)entry.getValue();
			String[] fromTo = text.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(fromTo[0]+","+fromTo[1]);
			
			if (hashMaps[1].containsKey(index))
			{
				retVal = (String) hashMaps[1].get(index);
			}
			else
			{
				retVal = null;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns a route value given its index
	 * 
	 * @param index The index of the node.
	 * @return
	 */
	public String getRouteValueByIndex(String index)
	{
		String retVal = null;
		
		Iterator it = this.odPairs.entrySet().iterator();
		while (it.hasNext() && retVal == null)
		{
			Map.Entry entry = (Map.Entry) it.next();
			
			String text = (String)entry.getValue();
			String[] fromTo = text.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(fromTo[0]+","+fromTo[1]);
			
			if (hashMaps[1].containsKey(index))
			{
				retVal = (String) hashMaps[1].get(index);
			}
			else
			{
				retVal = null;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns a route value given one of its nodes
	 * 
	 * @param routeNode One of the nodes in the route.
	 * @return
	 */
	public String getRouteValueByRouteNode(String routeNode)
	{
		String retVal = null;
		
		Iterator it = this.odPairs.entrySet().iterator();
		while (it.hasNext() && retVal == null)
		{
			Map.Entry entry = (Map.Entry) it.next();
			
			String text = (String)entry.getValue();
			String[] fromTo = text.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get(fromTo[0]+","+fromTo[1]);
			
			if (hashMaps[1].containsValue(routeNode))
			{
				Collection m = hashMaps[1].values();
				retVal = (String) retVal;
			}
		}
		
		return retVal;	
	}
	
	/**
	 * Returns a String representation of the object
	 * 
	 * @return
	 */
	public String toString()
	{
		LinkedHashMap retVal = new LinkedHashMap();
		
		Iterator it = this.odPairs.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			
			String text = (String)entry.getKey();
			String[] fromTo = text.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			LinkedHashMap<Integer, ?>[] hashMaps = (LinkedHashMap<Integer, ?>[]) entry.getValue();
			
			Iterator itRNodes = hashMaps[1].entrySet().iterator();
			Iterator itRIndexes = hashMaps[0].entrySet().iterator();
			
			while (itRNodes.hasNext()) {
				Map.Entry nodeEntry = (Map.Entry) itRNodes.next();
				String pairs = (String) nodeEntry.getValue();
				Integer indexValue = (Integer) nodeEntry.getKey();
				
				pairs = pairs + "=" + indexValue;
				retVal.put(pairs, hashMaps[2].get( nodeEntry.getKey() ));
			}
		}
		
		return "Route costs (RouteCosts.toString()): " + retVal.toString();
	}
	
	public int getNumberOfRoutes()
	{
		return this.counter;
	}
	
	/**
	 * Order the routes in ascending order,
	 * according to the OD pair and cost
	 * @throws  
	 */
	public void order()
	{
		if (this.debugMode)
			System.out.println("Beginning of RouteCosts.order()");
		
		int nodeQuantity = -1;
		int intO = 0;
		int intD = 0;
		int counter = 1;
		
		TreeSet<String> strODPairs = null;
		try {
			strODPairs = this.getODPairs();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (strODPairs != null)
		{
			Iterator itStrODPairs = strODPairs.iterator();
			LinkedHashMap oldFrom = new LinkedHashMap();
			LinkedHashMap oldTo = new LinkedHashMap();
			
			// Iterate through each and every OD pair
			while (itStrODPairs.hasNext())
			{
				LinkedHashMap<String, String> tempRoutesBetween = null;
				LinkedHashMap<Integer, Integer> tempRoutesIndexesBetween = null;
				
				String entry = (String) itStrODPairs.next();
				String[] fromTo = entry.split(",");
				fromTo[0] = fromTo[0].trim();
				fromTo[1] = fromTo[1].trim();
				
				//System.out.println("Iterating through OD pair " + fromTo[0] + "," + fromTo[1]);
				
				// Get the routes between the OD pair
				try {
					tempRoutesBetween = this.getRoutesBetween( fromTo[0], fromTo[1] );
					tempRoutesIndexesBetween = this.getRoutesIndexesBetween( fromTo[0], fromTo[1] );
					
					//System.out.println("tempRoutesBetween: " + tempRoutesBetween);
					//System.out.println("tempRoutesIndexesBetween: " + tempRoutesIndexesBetween);
					
					Iterator<Entry<String, String>> itEntryRoute = tempRoutesBetween.entrySet().iterator();
					Iterator<Entry<Integer, Integer>> itEntryRouteIndexes = tempRoutesIndexesBetween.entrySet().iterator();
					
					LinkedHashMap<Integer, ?>[] hashMaps = this.odPairs.get("("+fromTo[0]+","+fromTo[1]+")");
					
					List arrayCosts = new ArrayList( hashMaps[2].entrySet().size() );
					List arrayNodes = new ArrayList( hashMaps[1].entrySet().size() );
					List arrayIndexes = new ArrayList<Integer>( hashMaps[0].entrySet().size() );
					
					LinkedHashMap<Integer, Integer> tempRouteIndexes = new LinkedHashMap<Integer, Integer>();
					LinkedHashMap<Integer, String> tempRouteNodes = new LinkedHashMap<Integer, String>();
					LinkedHashMap<Integer, String> tempRouteValues = new LinkedHashMap<Integer, String>();
					
					while(itEntryRoute.hasNext())
					{
						Entry<String, String> entryRoute = (Entry<String, String>) itEntryRoute.next();
						Entry<Integer, Integer> entryIndexes = (Entry<Integer, Integer>) itEntryRouteIndexes.next();
						String entryKey = entryRoute.getKey();
						String entryValue = entryRoute.getValue();
						Object entryIndex = entryIndexes.getValue();
						
						int from = Integer.parseInt(fromTo[0].trim());
						int to = Integer.parseInt(fromTo[ fromTo.length - 1 ].trim());
						
						arrayIndexes.add(entryIndex);
						arrayCosts.add(entryValue);
						arrayNodes.add(entryKey);
						
						//System.out.println(
						//		"Route index " + entryIndex +
						//		": Nodes " + entryKey +
						//		", Cost " + entryValue + " added."
						//);
					} // End while(itEntryRoute.hasNext())
					
					Object[] costs = arrayCosts.toArray();
					Object[] nodes = arrayNodes.toArray();
					Object[] indexes = arrayIndexes.toArray();
						
					// Sort the routes alphabetically according to its nodes
					Arrays.sort(nodes);
					
					// Re-structure the routes indexes, nodes and values
					for(int k=0; k<nodes.length; k++)
					{
						if (this.debugMode)
							System.out.println(indexes[k] + ", " + nodes[k] + ", " + costs[k]);
						
						String oldIndex = "";
						double newCost = -1;
						
						// Get the (old) index of the route
						Iterator itRouteNodes = hashMaps[1].entrySet().iterator();
						while (itRouteNodes.hasNext())
						{
							Entry<Integer, String> entryRouteNodes = (Entry<Integer, String>) itRouteNodes.next();
							if ( entryRouteNodes.getValue().equalsIgnoreCase( (String)nodes[k] ) )
							{
								oldIndex = entryRouteNodes.getKey().toString();
								break;
							}
						}
						
						// Get the (old) cost of the route
						Iterator itRouteValues = hashMaps[2].entrySet().iterator();
						while (itRouteValues.hasNext())
						{
							Entry<Integer, String> entryRouteValues = (Entry<Integer, String>) itRouteValues.next();
							if (entryRouteValues.getKey()== Integer.parseInt(oldIndex) )
							{
								newCost = Double.parseDouble(entryRouteValues.getValue());
								break;
							}
						}
						
						indexes[k] = counter++;
						costs[k] = newCost;
					} // End for(int k=0; k<nodes.length; k++)
					
					if (this.debugMode)
					{
						System.out.println("indexes, nodes and costs (afterwards): ");
						for(int k=0; k<nodes.length; k++)
						{
							System.out.println(indexes[k] + ", " + nodes[k] + ", " + costs[k]);
						}
					}
					
					for(int k=0; k<nodes.length; k++)
					{
						tempRouteIndexes.put((Integer)indexes[k], (Integer)indexes[k]);
						tempRouteNodes.put((Integer)indexes[k], (String) nodes[k]);
						tempRouteValues.put((Integer)indexes[k], Double.toString((Double)costs[k]) );
					}
					
					// Update the entries in the odPairs object
					hashMaps[0] = tempRouteIndexes;
					hashMaps[1] = tempRouteNodes;
					hashMaps[2] = tempRouteValues;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // End try --- Get the routes between the OD pair
			} // End while (itStrODPairs.hasNext()) --- Iterate through each and every OD pair
		} // End if (strODPairs != null)
		
		if (this.debugMode)
			System.out.println("End of RouteCosts.order()");
	}
	
	public void checkCreateODPair(String odPair)
	{
		// If the OD pair hasn't been created yet,
		// add it dynamically
		if (!this.odPairs.containsKey(odPair))
		{
			LinkedHashMap<Integer, Integer> hashRouteIndexes = new LinkedHashMap<Integer, Integer>();
			LinkedHashMap<Integer, String> hashRouteNodes = new LinkedHashMap<Integer, String>();
			LinkedHashMap<Integer, String> hashRouteValues = new LinkedHashMap<Integer, String>();
			
			LinkedHashMap<Integer, ?>[] arr = new LinkedHashMap[3];
			arr[0] = hashRouteIndexes;
			arr[1] = hashRouteNodes;
			arr[2] = hashRouteValues;
			
			// The OD pair will be saved in the format (x,y)
			// being x the origin and y the destination
			this.odPairs.put(odPair, arr);
		}
	}
	
	public RouteCosts clone() throws CloneNotSupportedException
	{
		return (RouteCosts) super.clone();
	}
	
	/**
	 * Removes all the existing OD pairs and its routes
	 */
	public void removeAllRoutes() {
		this.odPairs = new LinkedHashMap<String, LinkedHashMap<Integer, ?>[]>();
		this.counter = 0;
	}
}
