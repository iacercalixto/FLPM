/**
 * 
 */
package preprocessing;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.alg.Node;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;




/**
 * @author iacer
 *
 */
public class Preprocessing {
	/**
	 * The class to use to do the preprocesssing job
	 */
	private static PreprocessingMethod preprocessingMethodClass = null;
	
	/**
	 * Arguments needed to the preprocessing class(es)
	 */
	private static String[] argc = null;
	private static Object[] argv = null;
	private static Class<?>[] argt = null;
	
	/**
	 * Constructor
	 */
	public Preprocessing()
	{
		// The name of class used to do the preprocessing,
		// if necessary. The standard is the class below.
		Preprocessing.preprocessingMethodClass = PreprocessingMethod.PreprocessingMinimumCostRoutes;
		
		// Use continuous optimization as default
		this.init();
	}
	
	/**
	 * Constructor
	 * 
	 * @param preprocessingMethodClass The class to use to do the pre-processing
	 */
	public Preprocessing(PreprocessingMethod preprocessingMethodClass)
	{
		// The name of class used to do the preprocessing, if necessary.
		Preprocessing.preprocessingMethodClass = preprocessingMethodClass;
		
		// Use continuous optimization as default
		this.init();
	}
	
	/**
	 * Initialize required variables.
	 * 
	 * @param integerProgramming Whether to solve a MIP or continuous optimization problem (M6)
	 */
	public void init()
	{
		// Initialize the arguments of the preprocessing class
		Preprocessing.argc = null;
		Preprocessing.argv = null;
		Preprocessing.argt = null;
	}
	
	/**
	 * Enumeration that provides the available methods to do
	 * the preprocessing (converting arc costs to route costs)
	 * 
	 * @author Iacer Calixto
	 */
	public enum PreprocessingMethod
	{
		PreprocessingMinimumCostRoutes,
		PreprocessingLesMixedMethod,
		PreprocessingKPathsPerODPair
	}
	
	/**
	 * Gives the method and its argument to call in the preprocessing class, if any.
	 * 
	 * @param methodName The method name
	 * @param value The value of the parameter
	 * @param type The type of the parameter
	 */
	public void setPreprocessingArguments(String methodName, Object value, Class<?> type)
	{
		Preprocessing.argc = new String[1];
		Preprocessing.argv = new Object[1];
		Preprocessing.argt = new Class[1];
		
		Preprocessing.argc[0] = methodName;
		Preprocessing.argv[0] = value;
		Preprocessing.argt[0] = type;
	}
	
	/**
	 * Gives the methods and its arguments to call in the preprocessing class, if any.
	 * 
	 * @param methodNames An array containing the method names
	 * @param values An array containing the parameters values
	 * @param types An array containing the parameters types
	 * @throws Exception
	 */
	public void setPreprocessingArguments(String[] methodNames, Object[] values, Class<?>[] types)
	throws Exception
	{
		if (methodNames.length != values.length ||
				methodNames.length != types.length ||
				values.length != types.length)
			throw new Exception("There must be exactly one value for each method name provided.");
		
		Preprocessing.argc = new String[methodNames.length];
		Preprocessing.argv = new Object[values.length];
		Preprocessing.argt = new Class[types.length];
		
		Preprocessing.argc = methodNames;
		Preprocessing.argv = values;
		Preprocessing.argt = types;
	}
	
	/**
	 * 
	 */
	public RouteCosts doPreprocessing(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, LinkCosts lCosts, ODPairs odPairs)
	{
		RouteCosts rc = new RouteCosts();
		
		try {
			System.out.println("Pre-processing started.");
			
			int routeCounter = 0;
			
			// Use the class identified in the variable
			// 'FuzzyLPModel.preprocessingMethodClass' to create
			// the object responsible to do the preprocessing
			IPreprocessing pp = null;
			
			try {
				Class<?> clPrep = Class.forName(Preprocessing.class.getPackage().getName() +
						"." + Preprocessing.preprocessingMethodClass.toString());
				Constructor<?> ct = clPrep.getConstructor();
				pp = (IPreprocessing) ct.newInstance();
				
				Object[] arglist = null;
				if (Preprocessing.argt != null)
				{
					arglist = new Object[Preprocessing.argt.length];
					
					// Allows methods with only one parameter each
					for (int i=0; i<Preprocessing.argc.length; i++)
					{
						Method meth = clPrep.getMethod( argc[i], argt[i] );
						Class<?> clArg = Class.forName( Preprocessing.argt[i].getName() );
						arglist[i] = Preprocessing.argv[i];
						Object retObj;
						
						// Invoke the method
						if (Preprocessing.argc.length>1)
							retObj = meth.invoke(pp, arglist[i]);
						else
							retObj = meth.invoke(pp, arglist);
					}
					
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			HashMap<?,?> minimumCostRoutes = pp.doPreprocessing(graph, lCounts, od, rc, lCosts, odPairs);
			
			//System.out.println("routesCost: "+minimumCostRoutes.entrySet());
			
			// Iterate through all the routes added to the model after the preprocessing
			// and add them to the rc (RouteCosts) object
			Iterator<?> iteratorRoutes = minimumCostRoutes.entrySet().iterator();
			while(iteratorRoutes.hasNext())
			{
				Map.Entry<?,?> entry = (Map.Entry<?,?>) iteratorRoutes.next();
				
				String fromTo;
				fromTo = ((Node)((ArrayList)entry.getKey()).get(0)).toString() + "->";
				fromTo += ((Node)((ArrayList)entry.getKey()).get(
						((ArrayList)entry.getKey()).size() - 1
						)).toString();
				
				ArrayList<Node> path = (ArrayList<Node>) entry.getKey();
				String cost = (String) entry.getValue();
				
				// Add all the existing paths between every two vertices
				if (path.size()>0)
				{
					// Generate a string version of the path and use it to create
					// the RouteCosts object
					Iterator pathIterator = path.iterator();
					
					String strPath = "";
					while (pathIterator.hasNext())
					{
						Node vertex = (Node)pathIterator.next();
						strPath += vertex.toString() + ", ";
					}
					
					strPath = strPath.substring(0, strPath.length()-2);
					
					//String[] arrFromTo = fromTo.split("->");
					rc.setCount(
							Integer.toString(++routeCounter),
							strPath,
							cost
					);
				}
			}
			
			System.out.println("Pre-processing finished.");
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rc;
	}
}
