/**
 * 
 */
package cplexModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import odEstimation.AutoMapValue;
import odEstimation.LinkCapacities;
import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import flowCostMapping.ErrorValueMapping;
import flowCostMapping.ErrorValueMapping.CountType;




import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;

/**
 * @author Iacer Calixto
 */
public class FuzzyLPModel_AllRoutesInObjectiveFunction implements IFuzzyLPModel
{
	/**
	 * Whether to use continuous optimization or mixed integer programming.
	 * This applies to M8, but not M2 and M3 (which are always continuous)
	 */
	private static boolean integerProgramming = false;
	
	/**
	 * Problem M2
	 */
	private static IloCplex cplexM2 = null;
	
	/**
	 * Problem M2's variables
	 */
	private static IloNumVar[] xM2 = null;
	
	/**
	 * Problem M3
	 */
	private static IloCplex cplexM3 = null;
	
	/**
	 * Problem M3's variables
	 */
	private static IloNumVar[] xM3 = null;
	
	/**
	 * Problem M8
	 */
	private static IloCplex cplexM8 = null;
	
	/**
	 * Problem M8's lambda (variable)
	 */
	private static IloNumVar[] xM8lambda = null;
	
	/**
	 * Problem M8's other variables
	 */
	private static IloNumVar[] xM8 = null;
	
	/**
	 * The variable to hold the estimated OD matrix
	 */
	private static AutoMapValue <String, String> odMatrix = null;
	
	/**
	 * The variable to hold the final solution of M8
	 */
	private static double solutionM8 = -1;
	
	/**
	 * An array containing the route costs (objective function multipliers)
	 */
	private static double[] arrayObjectiveValues;
	
	/**
	 * The variable containing the existing nodes
	 */
	private static HashMap <String, String> odMatrixNodes = null;
	
	/**
	 * A map from the LP (the route indexes) to the OD pair it is part of
	 */
	private static HashMap <Integer, String> LPToODMapping = null;
	
	/**
	 * A map from the routes to its indexes in the LP
	 */
	private static HashMap <String, Integer> RouteToLPMapping = null;
	
	/**
	 * The variable to hold the estimated link counts
	 */
	private static HashMap <String, Double> linkCounts = null;
	
	/**
	 * The variable to hold the estimated counts on used routes
	 */
	private static HashMap <String, Double> routeCounts = null;
	
	/**
	 * The String representation of the linear programming model M8
	 */
	private static String strLPModel = null;
	
	/**
	 * The String representation of the linear programming model M2
	 */
	private static String strLPModelM2 = null;
	
	/**
	 * Whether it is in debug mode
	 */
	private static boolean debug = false;
	
	/**
	 * Whether to use errors coded in the input file or to ignore
	 * those errors and use predefined error values
	 */
	private static boolean useCodedErrors = false;
	
	/**
	 * Whether to use prefined errors with values of a gradient,
	 * linear distribution or a fixed distribution.
	 */
	private static boolean useGradientErrors = false;
	
	/**
	 * The link counts error measurements
	 */
	private static float a = -1;
	
	/**
	 * The OD measurements error
	 */
	
	private static float b = -1;
	
	/**
	 * The origin-only error measurements
	 */
	
	private static float d = -1;
	
	/**
	 * The destination-only error measurements
	 */
	
	private static float e = -1;

	/**
	 * Constructor
	 */
	public FuzzyLPModel_AllRoutesInObjectiveFunction()
	{
		// Use continuous optimization as default
		this.init(false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param integerProgramming
	 */
	public FuzzyLPModel_AllRoutesInObjectiveFunction(boolean integerProgramming)
	{
		this.init(integerProgramming);
	}
	
	/**
	 * Initialize required variables.
	 * 
	 * @param integerProgramming Whether to solve a MIP or continuous optimization problem (M8)
	 */
	public void init(boolean integerProgramming)
	{
		// Initialize the object used to map the LP structures
		// to the final generated OD matrix
		FuzzyLPModel_AllRoutesInObjectiveFunction.LPToODMapping = new HashMap<Integer, String>();
		FuzzyLPModel_AllRoutesInObjectiveFunction.RouteToLPMapping = new HashMap<String, Integer>();
		
		strLPModel = new String();
		
		// Whether to build a MIP program or a continuous optimization program
		FuzzyLPModel_AllRoutesInObjectiveFunction.integerProgramming = integerProgramming;
		
		// If the relative link count error hasn't been set, use 10%
		if (FuzzyLPModel_AllRoutesInObjectiveFunction.a==-1) a = 0.1f;
		
		// If the relative OD measurement error hasn't been set, use 20%
		if (FuzzyLPModel_AllRoutesInObjectiveFunction.b==-1) b = 0.2f;
		
		// If the relative origin-only measurement error hasn't been set, use 20%
		if (FuzzyLPModel_AllRoutesInObjectiveFunction.d==-1) d = 0.2f;
		
		// If the relative destination-only measurement error hasn't been set, use 20%
		if (FuzzyLPModel_AllRoutesInObjectiveFunction.e==-1) e = 0.2f;
		
		try {
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2 = new IloCplex();
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3 = new IloCplex();
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8 = new IloCplex();
		} catch (IloException e)
		{
			System.err.println("Concert exception caught: " + e);
		}
	}
	
	/**
	 * Method used to effectively create the lower bound problem
	 * (namely problem M3 referenced in Professor Foulds' model),
	 * part of the fuzzy linear programming problem.
	 * 
	 * @param graph The graph representing an urban network.
	 * @param lCounts An object containing the observed link counts on arcs.
	 * @param od An object containing the observed OD counts (or the demands between two points in the graph).
	 * @param rc An object containing the costs of each of the possible routes in our graph.
	 * @param lCosts An object containing the costs of each of the arcs in our graph (used to sum up the costs in all possible routes).
	 * @param odPairs The possible OD pairs.
	 */
	@SuppressWarnings("unchecked")
	public void createProblemM3(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs)
	{
		ErrorValueMapping errorValueMapping = new ErrorValueMapping();
		
		System.out.println("Beginning to process M3.");
		
		try {
			FuzzyLPModel_AllRoutesInObjectiveFunction.RouteToLPMapping = new HashMap<String, Integer>();
			
			// Create model (lower bounds, upper bounds, objective function variables)
			HashMap<Integer, Double> lb = new HashMap<Integer, Double>();
			HashMap<Integer, Double> ub = new HashMap<Integer, Double>();
			HashMap<Integer, Double> objvals = new HashMap<Integer, Double>();
			HashMap<Integer, String> objnames = new HashMap<Integer, String>();
			
			// ------------------------------------------------------------------
			// ----------------------- create the model -------------------------
			// ------------------------------------------------------------------
			
			// Get an iterator to all the modeled OD pairs
			Iterator itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			int i=0;
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				// obtain the route indexes
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				// obtain the route nodes
				Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
				String strI = "";
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					// Iterate through the nodes together with the costs
					Map.Entry nodes = (Map.Entry) itNodes.next();
					strI = (String) nodes.getValue();
					strI = strI.replace(" ", "").replace(",", "_");
					
					// Initialize the lower bounds with ZERO value
					lb.put(i, 0.0);
					
					// Initialize the upper bounds with the biggest number possible
					ub.put(i, Double.MAX_VALUE);
					
					// Initialize the objective function's coefficient
					objvals.put(i, Double.valueOf((String) pairs.getValue()).doubleValue() );
					
					// Initialize the name of the variable (as shown in the .lp file)
					objnames.put(i, "Route_"+Integer.toString(i)+"_"+strI);
					
					// Create the object that maps the routes in the LP
					// to the structure that contains the estimated OD matrix
					try {
						RouteToLPMapping.put(strI, i);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			// Uncomment to create integer variables
			//int[] arrayLb = new int[lb.size()];
			//int[] arrayUb = new int[ub.size()];
			double[] arrayLb = new double[lb.size()];
			double[] arrayUb = new double[ub.size()];
			
			double[] arrayObjvals = new double[objvals.size()];
			String[] arrayObjnames = new String[objnames.size()];
			
			for (i=0; i < arrayLb.length; i++)
					arrayLb[i]= (lb.get(i+1)).intValue();
			for (i=0; i < arrayUb.length; i++)
					arrayUb[i]= (ub.get(i+1)).intValue();
			for (i=0; i < arrayObjvals.length; i++)
					arrayObjvals[i]= ((Double) objvals.get(i+1)).doubleValue();
			for (i=0; i < arrayObjnames.length; i++)
					arrayObjnames[i]= objnames.get(i+1);
			
			// Create the expression and the objective function
			
			// Uncomment to create integer variables
			//FuzzyLPModel.xM3 = FuzzyLPModel.cplexM3.intVarArray(i, arrayLb, arrayUb, arrayObjnames);
			FuzzyLPModel_AllRoutesInObjectiveFunction.xM3 =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.numVarArray(
						i,
						arrayLb,
						arrayUb,
						arrayObjnames
				);
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addMinimize(
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.scalProd(
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM3,
							arrayObjvals
					)
			);
			
			// --------------------------------------------------------------
			// ----------------- create the constraints ---------------------
			// --------------------------------------------------------------
			
			// --------------------------------------------------------------
			// --------- first add the positiveness constraints -------------
			// --------------------------------------------------------------
			
			itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				// obtain the route indexes
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				// obtain the route nodes
				//Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				i=0;
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					// add the xM3[i] >= 0 constraint
					IloNumExpr expr = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.linearNumExpr();
					((IloLinearNumExpr) expr).addTerm(
							1.0,
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM3[ i - 1 ]
					);
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addGe(
							expr,
							0,
							"Positiveness_"+
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM3[ i - 1 ].getName()
					);
				}
			}
			
			// --------------------------------------------------------------
			// ---- for each existing link count, create its constraints ----
			// --------------------------------------------------------------
			
			System.out.println("Beginning to create link count constraints.");
			
			HashMap hmLinkCounts = lCounts.getAllCounts();
			HashMap hmErrorsAboveLinkCounts = lCounts.getAllErrorsAbove();
			HashMap hmErrorsBelowLinkCounts = lCounts.getAllErrorsBelow();
			
			Iterator itLC = hmLinkCounts.entrySet().iterator();
			Iterator itEALC = hmErrorsAboveLinkCounts.entrySet().iterator();
			Iterator itEBLC = hmErrorsBelowLinkCounts.entrySet().iterator();
			
			String[] arrStr;
			String[] arrEAStr;
			String[] arrEBStr;
			String from, to, value;
			String fromEA, toEA, valueEA;
			String fromEB, toEB, valueEB;
			
			// Iterate through the link counts
			while (itLC.hasNext()) {
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry arc = (Map.Entry) itLC.next();
				Map.Entry arcEA = (Map.Entry) itEALC.next();
				Map.Entry arcEB = (Map.Entry) itEBLC.next();
				
				arrStr = arc.getKey().toString().split(",");
				arrEAStr = arc.getKey().toString().split(",");
				arrEBStr = arc.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","");
				to = arrStr[1].replace("(","").replace(")","");
				value = arc.getValue().toString();
				
				//System.out.println("Link Count: "+from+"->"+to+"="+value);
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = arcEA.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = arcEB.getValue().toString();
				
				// create the expression to contain the 
				// constraint on the existing measurements on ARCS
				IloNumExpr expr = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.linearNumExpr();
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// The number of routes that uses the link
				//int counterRoutesContainingLink;
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// obtain the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// obtain the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// obtain the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String[] routeNodes;
						String routeValue;
						String routeKey;
						
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						// Iterate through the route indexes and nodes concomitantly
						Map.Entry entryNodes = (Map.Entry) itNodes.next();
						Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
						
						// Obtain the route nodes
						routeNodes = ((String)entryNodes.getValue()).split(",");
						
						// If the route doesn't contain the two nodes, continue.
						// If it does, iterate through its nodes and check if they
						// appear in sequence.
						if (((String)entryNodes.getValue()).indexOf(from+","+to)<0 &&
								((String)entryNodes.getValue()).indexOf(from+", "+to)<0)
							continue;
						
						// Create the constraints regarding the existing measurements on arcs
						int ODPairsCounter=0;
						
						// Iterate through each node in the route
						for (int j=0; j<routeNodes.length; j++)
						{
							if (j==0) continue;
							
							String fromRoute = routeNodes[j-1].replace("(", "").trim();
							String toRoute = routeNodes[j].replace(")", "").trim();
							addedODPairs.add(ODPairsCounter++, fromTo[0]+","+fromTo[1]);
							
							if (fromRoute.equalsIgnoreCase(from) && toRoute.equalsIgnoreCase(to))
							{
								try {
									String nodesValue = (String)entryNodes.getValue();
									nodesValue = nodesValue.replace(" ", "").replace(",", "_");
									int varPosition = RouteToLPMapping.get(nodesValue)-1;
									
									// Add the term to the expression (constraint)
									((IloLinearNumExpr) expr).addTerm(
											1.0,
											FuzzyLPModel_AllRoutesInObjectiveFunction.xM3[ varPosition ]
									);
								} catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				
				if (addedODPairs.isEmpty())
				{
					System.out.print("There is no path using arc "+from+"->"+to+". ");
					System.out.print(" arrayRouteCost.size()==0 to arc "+from+"->"+to+".");
					System.out.println(" Problem unfeasible.");
					System.exit(1);
				}
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				Double dblValueEB = Double.parseDouble(valueEB);
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblValueEB = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.LinkCount,
								//Double.parseDouble(valueEB)
								Double.valueOf(value)
						);
					} else {
						dblValueEB = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.a *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addGe(
						expr,
						Double.valueOf(value) - dblValueEB,
						"Link_count_"+from+"_"+to
				);
				
				// Create the lower-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				Double dblValueEA = Double.parseDouble(valueEA);
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblValueEA = errorValueMapping.getRelativeError(
								CountType.LinkCount,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
					} else {
						dblValueEA = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.a *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addLe(
						expr,
						Double.valueOf(value) + dblValueEA,
						"Link_count_"+from+"_"+to
				);
			}
			
			System.out.println("Link count constraints finished.");
			
			// --------------------------------------------------------------
			// -------------- link count constraints finished ---------------
			// --------------------------------------------------------------
			
			// ---------------------------------------------------------------
			//  create the constraints regarding the existing OD measurements
			// ---------------------------------------------------------------
			
			System.out.println("Beginning to create OD measurement constraints.");
			
			// Iterate through the OD measurements
			Iterator odIt = od.getODCounts().entrySet().iterator();
			Iterator odEAIt = od.getAllErrorsAbove().entrySet().iterator();
			Iterator odEBIt = od.getAllErrorsBelow().entrySet().iterator();
			while (odIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry odCount = (Map.Entry) odIt.next();
				Map.Entry odEACount = (Map.Entry) odEAIt.next();
				Map.Entry odEBCount = (Map.Entry) odEBIt.next();
				
				arrStr = odCount.getKey().toString().split(",");
				arrEAStr = odEACount.getKey().toString().split(",");
				arrEBStr = odEBCount.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				to = arrStr[1].replace("(","").replace(")","").trim();
				value = odCount.getValue().toString();
				
				//System.out.println("OD Count: "+from+","+to+"="+value);
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = odEACount.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = odEBCount.getValue().toString();
				
				// Create the constraint regarding the existing OD measurements
				IloNumExpr exprOD = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.linearNumExpr();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(from, to).entrySet().iterator();
				
				// obtain the route costs
				Iterator itNodes = rc.getRouteNodes(from, to).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(from, to).entrySet().iterator();
				
				// For each route, if it starts on the given start point and
				// ends on the given end point, add it to the OD count constraint
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry route = (Map.Entry) it.next();
					
					// Extract the nodes from the route
					String[] routeNodes;
					String routeValue;
					String routeKey;
					
					// Iterate through the route indexes and nodes concomitantly
					Map.Entry entryNodes = (Map.Entry) itNodes.next();
					Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
					
					//System.out.println("route: " + entryNodes.getValue());
					
					routeKey = Integer.toString( (Integer)route.getKey() );
					routeValue = (String) route.getValue();
					
					// Populate the array of Strings with the route nodes
					routeNodes = routeKey.split(",");
					
					addedODPairs.add(from+","+to);
					String nodesValue = (String)entryNodes.getValue();
					nodesValue = nodesValue.replace(" ", "").replace(",", "_");
					int varPosition = RouteToLPMapping.get(nodesValue)-1;
					
					try {
						((IloLinearNumExpr) exprOD).addTerm(
								1.0,
								FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[varPosition]
						);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				} // End while (it.hasNext()) --- Iterate through the route costs
				
				//System.out.println("exprOD: " + exprOD);
				//System.out.println("value: " + value);
				
				// If the exprOD is empty, there exists no route between the OD pair
				if (exprOD.toString().trim().isEmpty())
					continue;
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				Double dblValueEB = Double.parseDouble(valueEB);
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblValueEB = errorValueMapping.getRelativeError(
								CountType.ODMeasurement,
								//Double.parseDouble(valueEB)
								Double.valueOf(value)
						);
					} else {
						dblValueEB = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.b *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addGe(
						exprOD,
						Double.valueOf(value) - dblValueEB,
						"OD_measurement_"+from+"_"+to
				);
				
				// Create the lower-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				Double dblValueEA = Double.parseDouble(valueEA);
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblValueEA = errorValueMapping.getRelativeError(
								CountType.ODMeasurement,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
					} else {
						dblValueEA = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.b *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addLe(
						exprOD,
						Double.valueOf(value) + dblValueEA,
						"OD_measurement_"+from+"_"+to
				);
			} // End while (odIt.hasNext()) --- Iterate through the OD measurements
			
			System.out.println("OD measurement constraints finished.");
			
			// ---------------------------------------------------------------
			// ------------- OD measurements constraints finished ------------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// -------- create the origin-only (O) demands constraints -------
			// ---------------------------------------------------------------
			
			System.out.println("Beginning to create Origin-only measurement constraints.");
			
			// Iterate through the origin-only (O) measurements together with its
			// error measurements
			Iterator oIt = od.getOriginCounts().entrySet().iterator();
			Iterator oEAIt = od.getOriginAllErrorsAbove().entrySet().iterator();
			while (oIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry oCount = (Map.Entry) oIt.next();
				Map.Entry oEACount = (Map.Entry) oEAIt.next();
				
				arrStr = oCount.getKey().toString().split(",");
				arrEAStr = oEACount.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				value = oCount.getValue().toString();
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				valueEA = oEACount.getValue().toString();
				
				// Create the constraints regarding the existing
				// origin-only (O) measurements
				IloNumExpr exprO1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				
				// For each route, if it starts on the given start point,
				// add it to the origin (O) count constraint
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					if (!fromTo[0].equalsIgnoreCase(from)) continue;
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String routeValue;
						String routeKey;
						
						// Iterate through each route in the routes object
						//Iterator itRoutes = routes.entrySet().iterator();
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						try {
							// Iterate through the route indexes and nodes concomitantly
							Map.Entry entryNodes = (Map.Entry) itNodes.next();
							Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
							
							routeKey = Integer.toString( (Integer)route.getKey() );
							routeValue = (String) route.getValue();
							
							String nodesValue = (String)entryNodes.getValue();
							nodesValue = nodesValue.replace(" ", "").replace(",", "_");
							int varPosition = RouteToLPMapping.get(nodesValue)-1;
							
							((IloLinearNumExpr) exprO1).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM3[varPosition]
							);
							
							// Get the route error measurements
							dblErrorAbove = Double.parseDouble(valueEA);
							
							//System.out.println("Origin-only constraint " +
							//		from + ": " + nodesValue + " added." );
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								CountType.OriginOnlyMeasurement,
								Double.parseDouble(value)
						);
					} else {
						dblErrorAbove = FuzzyLPModel_AllRoutesInObjectiveFunction.d *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addGe(
						exprO1,
						Double.valueOf(value) + dblErrorAbove,
						"Origin_measurement_"+from
				);
			}
			
			System.out.println("Origin-only measurement constraints finished.");
			
			// ---------------------------------------------------------------
			// --------- origin-only (O) demands constraints finished --------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// ----- create the destination-only (D) demands constraints -----
			// ---------------------------------------------------------------
			
			System.out.println("Beginning to create Destination-only measurement constraints.");
			
			// Iterate through the destination-only (D) measurements
			// together with its error measurements
			Iterator dIt = od.getDestinationCounts().entrySet().iterator();
			Iterator dEAIt = od.getDestinationAllErrorsAbove().entrySet().iterator();
			while (dIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry dCount = (Map.Entry) dIt.next();
				Map.Entry dEACount = (Map.Entry) dEAIt.next();
				
				arrStr = dCount.getKey().toString().split(",");
				arrEAStr = dEACount.getKey().toString().split(",");
				
				to = arrStr[0].replace("(","").replace(")","").trim();
				value = dCount.getValue().toString();
				
				toEA = arrEAStr[0].replace("(","").replace(")","");
				valueEA = dEACount.getValue().toString();
				
				// Create the constraints regarding the existing
				// demand-only (D) measurements
				IloNumExpr exprD1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				
				// For each route, if it ends on the given end point,
				// add it to the destination-only (D) count constraint
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					if (!fromTo[1].equalsIgnoreCase(to)) continue;
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String routeValue;
						String routeKey;
						
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						try {
							// Iterate through the route indexes and nodes concomitantly
							Map.Entry entryNodes = (Map.Entry) itNodes.next();
							Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
							
							routeKey = Integer.toString( (Integer)route.getKey() );
							routeValue = (String) route.getValue();
							
							String nodesValue = (String)entryNodes.getValue();
							nodesValue = nodesValue.replace(" ", "").replace(",", "_");
							int varPosition = RouteToLPMapping.get(nodesValue)-1;
							
							((IloLinearNumExpr) exprD1).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM3[varPosition]
							);
							
							// Get the route error measurements
							dblErrorAbove = Double.parseDouble(valueEA);
							
							//System.out.println("Destination-only constraint " +
							//		to + ": " + nodesValue + " added." );
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.

				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								CountType.DestinationOnlyMeasurement,
								Double.parseDouble(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.e *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.addGe(
						exprD1,
						Double.valueOf(value) + dblErrorAbove,
						"Destination_measurement_"+to
				);
			}
			
			System.out.println("Destination-only measurement constraints finished.");
			
		} catch (IloException e)
		{
			System.err.println("Concert exception caught while creating problem M3: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ---------------------------------------------------------------
		// ------ destination-only (D) demands constraints finished ------
		// ---------------------------------------------------------------
		
		System.out.println("Finished processing M3.");
		System.out.println();
	}
	
	/**
	 * Method used to solve problem M3 (the lower bound model)
	 * 
	 * @return the value of the solution of M3.
	 */
	public double solveProblemM3(String filePath)
	{
		double solution = -1;
		
		try {
			// Write model to file
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.exportModel(filePath+"Problem M3.lp");
			
			if ( FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.solve() )
			{
				System.out.println("Solution status = " + FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.getStatus());
				solution = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.getObjValue();
				double[] val = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.getValues(FuzzyLPModel_AllRoutesInObjectiveFunction.xM3);
				int ncols = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.getNcols();
				/*
				for (int j = 0; j < ncols; ++j)
				{
					if (val[j]>0)
					{
						System.out.println("Column: " +
							FuzzyLPModel.xM3[j] +
							" Value = " + val[j]);
					}
				}
				*/
			} else {
				System.out.println("Solution error status = " + FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.getStatus());
			}
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM3.end();
		} catch (IloException e)
		{
			System.err.println("Concert exception caught while trying to solve problem M3: " + e);
		}
		return solution;
	}
	
	/**
	 * Method used to effectively create the upper bound problem
	 * (namely problem M2 referenced in Professor Foulds' model),
	 * part of the fuzzy linear programming problem.
	 * 
	 * @param graph The graph representing an urban network.
	 * @param lCounts An object containing the observed link counts on arcs.
	 * @param od An object containing the observed OD counts (or the demands between two points in the graph).
	 * @param rc An object containing the costs of each of the possible routes in our graph.
	 * @param lCosts An object containing the costs of each of the arcs in our graph (used to sum up the costs in all possible routes).
	 * @param odPairs The possible OD pairs in the model.
	 */
	public void createProblemM2(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs)
	{
		System.out.println("Beginning to create M2.");
		
		ErrorValueMapping errorValueMapping = new ErrorValueMapping();
		
		try {
			FuzzyLPModel_AllRoutesInObjectiveFunction.RouteToLPMapping = new HashMap<String, Integer>();
			
			// Create structure to contain the lower bounds, upper bounds,
			// objective function variables and its names
			HashMap<Integer, Double> lb = new HashMap<Integer, Double>();
			HashMap<Integer, Double> ub = new HashMap<Integer, Double>();
			HashMap<Integer, Double> objvals = new HashMap<Integer, Double>();
			HashMap<Integer, String> objnames = new HashMap<Integer, String>();
			
			// ------------------------------------------------------------------
			// ----------------------- create the model -------------------------
			// ------------------------------------------------------------------
			
			Iterator itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			int i=0;
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				String strI = "";
				// Iterate through all route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					// Iterate through the nodes together with the costs
					Map.Entry nodes = (Map.Entry) itNodes.next();
					strI = (String) nodes.getValue();
					strI = strI.replace(" ", "").replace(",", "_");
					
					// Initialize the lower bounds with ZERO value
					lb.put(i, 0.0);
					
					// Initialize the upper bounds with the biggest number possible
					ub.put(i, Double.MAX_VALUE);
					
					// Initialize the objective function's coefficient
					objvals.put(i, Double.valueOf((String) pairs.getValue()).doubleValue() );
					
					// Initialize the name of the variable (as shown in the .lp file)
					objnames.put(i, "Route_"+Integer.toString(i)+"_"+strI);
					
					// Create the object that maps the routes in the LP
					// to the structure that contains the estimated OD matrix
					try {
						RouteToLPMapping.put(strI, i);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			// Uncomment to create integer variables
			//int[] arrayLb = new int[lb.size()];
			//int[] arrayUb = new int[ub.size()];
			double[] arrayLb = new double[lb.size()];
			double[] arrayUb = new double[ub.size()];
			double[] arrayObjvals = new double[objvals.size()];
			String[] arrayObjnames = new String[objnames.size()];
			
			for (i=0; i < arrayLb.length; i++)
					arrayLb[i]= (lb.get(i+1)).intValue();
			for (i=0; i < arrayUb.length; i++)
					arrayUb[i]= (ub.get(i+1)).intValue();
			for (i=0; i < arrayObjvals.length; i++)
					arrayObjvals[i]= ((Double) objvals.get(i+1)).doubleValue();
			for (i=0; i < arrayObjnames.length; i++)
					arrayObjnames[i]= objnames.get(i+1);
			
			// Create the expression and the objective function
			
			// Uncomment to create integer variables
			//FuzzyLPModel.xM2 = FuzzyLPModel.cplexM2.intVarArray(i, arrayLb, arrayUb, arrayObjnames);
			FuzzyLPModel_AllRoutesInObjectiveFunction.xM2 =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.numVarArray(
						i,
						arrayLb,
						arrayUb,
						arrayObjnames
				);
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.addMinimize(
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.scalProd(
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM2,
							arrayObjvals
					)
			);
			
			// --------------------------------------------------------------
			// ----------------- create the constraints ---------------------
			// --------------------------------------------------------------
			
			// --------------------------------------------------------------
			// --------- first add the positiveness constraints -------------
			// --------------------------------------------------------------
			
			itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			i=0;
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// Get an iterator to the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Get an iterator to the route indexes
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					// add the xM2[i] >= 0 constraint
					IloNumExpr expr =
						FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.linearNumExpr();
					((IloLinearNumExpr) expr).addTerm(
							1.0,
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[ i - 1 ]
					);
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.addGe(
							expr,
							0,
							"Positiveness_"+
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[ i - 1 ].getName()
					);
				}
			}
			
			// ----------------------------------------------------------------
			//  create the constraints regarding the existing OD measurements
			// ----------------------------------------------------------------
			//  For all Qij, where Qij' > 0,
			//  set xijH = Qij + bij, for one H in Kij' (not least-cost routes)
			// ----------------------------------------------------------------
			
			System.out.println("Beginning to create OD measurement constraints.");
			
			HashMap hmLinkCounts = lCounts.getAllCounts();
			HashMap hmErrorsAboveLinkCounts = lCounts.getAllErrorsAbove();
			HashMap hmErrorsBelowLinkCounts = lCounts.getAllErrorsBelow();
			
			Iterator itLC = hmLinkCounts.entrySet().iterator();
			Iterator itEALC = hmErrorsAboveLinkCounts.entrySet().iterator();
			Iterator itEBLC = hmErrorsBelowLinkCounts.entrySet().iterator();
			
			String[] arrStr;
			String[] arrEAStr;
			String[] arrEBStr;
			String from, to, value;
			String fromEA, toEA, valueEA;
			String fromEB, toEB, valueEB;
			
			int actualTempRouteNodes = -1;
			HashMap <Integer, Double> addedRoutes = new HashMap <Integer, Double>();
			
			Iterator odIt = od.getODCounts().entrySet().iterator();
			Iterator odEAIt = od.getAllErrorsAbove().entrySet().iterator();
			Iterator odEBIt = od.getAllErrorsBelow().entrySet().iterator();
			// Iterate through the OD measurements
			while (odIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry odCount = (Map.Entry) odIt.next();
				Map.Entry odEACount = (Map.Entry) odEAIt.next();
				Map.Entry odEBCount = (Map.Entry) odEBIt.next();
				
				arrStr = odCount.getKey().toString().split(",");
				arrEAStr = odEACount.getKey().toString().split(",");
				arrEBStr = odEBCount.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				to = arrStr[1].replace("(","").replace(")","").trim();
				value = odCount.getValue().toString();
				
				//System.out.println("OD Count: "+from+","+to+"="+value);
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = odEACount.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = odEBCount.getValue().toString();
				
				// Create the constraint regarding the existing OD measurements
				IloNumExpr exprOD = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.linearNumExpr();
				
				// Get an iterator to the route costs
				Iterator it = rc.getNonLeastCostRouteCosts(from, to).entrySet().iterator();
				
				// Get an iterator to the route nodes
				Iterator itNodes = rc.getNonLeastCostRouteNodes(from, to).entrySet().iterator();
				
				// Get an iterator to the route indexes
				Iterator itIndexes = rc.getNonLeastCostRoutesIndexesBetween(from, to).entrySet().iterator();
				
				// If all routes are least-cost routes,
				// use a least-cost route
				if (!itIndexes.hasNext())
				{
					it = rc.getRouteCosts(from, to).entrySet().iterator();
					itNodes = rc.getRouteNodes(from, to).entrySet().iterator();
					itIndexes = rc.getRouteIndexes(from, to).entrySet().iterator();
				}
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry route = (Map.Entry) it.next();
					Map.Entry entryNodes = (Map.Entry) itNodes.next();
					Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
					
					// Extract the nodes from the route
					String[] routeNodes;
					String routeValue;
					String routeKey;
					
					routeKey = Integer.toString( (Integer)route.getKey() );
					routeValue = (String) route.getValue();
					
					// Populate the array of Strings with the route nodes
					routeNodes = routeKey.split(",");
					
					addedODPairs.add(from+","+to);
					String nodesValue = (String)entryNodes.getValue();
					nodesValue = nodesValue.replace(" ", "").replace(",", "_");
					int varPosition = RouteToLPMapping.get(nodesValue)-1;
					
					try {
						//System.out.println("nodesValue: " + nodesValue);
						//System.out.println("varPosition: " + varPosition);
						
						// Add the route to the expression
						((IloLinearNumExpr) exprOD).addTerm(
								1.0,
								FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[varPosition]
						);
						
						actualTempRouteNodes = varPosition;
						break;
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				
				// If exprOD is empty, there is no route between the OD pair
				if (exprOD.toString().trim().isEmpty())
					continue;
				
				// --------------------------------------------------------
				//  Create the constraint (for H in Kij'):
				//  xijH = Qij + bij
				// --------------------------------------------------------
				Double dblValueEA = Double.parseDouble(valueEA);
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblValueEA = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.ODMeasurement,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
					} else {
						dblValueEA = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.b *
								Double.valueOf(value);
					}
				}
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.addGe(
						exprOD,
						Double.valueOf(value) + dblValueEA,
						"OD "+from+"->"+to
				);
				
				addedRoutes.put(actualTempRouteNodes, Double.valueOf(value) + dblValueEA);
			}
			
			System.out.println("OD measurement constraints created.");
			
			// ----------------------------------------------------------------
			// -------------- OD measurement constraints finished -------------
			// ----------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// -------- create the origin-only (O) demands constraints -------
			// ---------------------------------------------------------------
			
			System.out.println("Beginning to create Origin-only measurement constraints.");
			
			// Iterate through the origin-only (O) measurements together with its
			// error measurements
			Iterator oIt = od.getOriginCounts().entrySet().iterator();
			Iterator oEAIt = od.getOriginAllErrorsAbove().entrySet().iterator();
			while (oIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry oCount = (Map.Entry) oIt.next();
				Map.Entry oEACount = (Map.Entry) oEAIt.next();
				
				arrStr = oCount.getKey().toString().split(",");
				arrEAStr = oEACount.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				value = oCount.getValue().toString();
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				valueEA = oEACount.getValue().toString();
				
				// Create the constraints regarding the existing
				// origin-only (O) measurements
				IloNumExpr exprO1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				
				// For each route, if it starts on the given start point,
				// add it to the origin (O) count constraint
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// We're only interested in the OD pairs departing from our origin
					if (!fromTo[0].equalsIgnoreCase(from)) continue;
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String routeValue;
						String routeKey;
						
						// Iterate through each route in the routes object
						//Iterator itRoutes = routes.entrySet().iterator();
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						try {
							// Iterate through the route indexes and nodes concomitantly
							Map.Entry entryNodes = (Map.Entry) itNodes.next();
							Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
							
							routeKey = Integer.toString( (Integer)route.getKey() );
							routeValue = (String) route.getValue();
							
							String nodesValue = (String)entryNodes.getValue();
							nodesValue = nodesValue.replace(" ", "").replace(",", "_");
							int varPosition = RouteToLPMapping.get(nodesValue)-1;
							
							((IloLinearNumExpr) exprO1).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[varPosition]
							);
							
							// Get the route error measurements
							dblErrorAbove = Double.parseDouble(valueEA);
							
							//System.out.println("Origin-only constraint " +
							//		from + ": " + nodesValue + " added." );
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				//System.out.println("From "+from+".");
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.OriginOnlyMeasurement,
								Double.parseDouble(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.d *
								Double.valueOf(value);
					}
				}
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.addGe(
						exprO1,
						Double.valueOf(value) + dblErrorAbove,
						"Origin "+from
				);
			}
			
			System.out.println("Origin-only measurement constraints created.");
			
			// ---------------------------------------------------------------
			// --------- origin-only (O) demands constraints finished --------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// ----- create the destination-only (D) demands constraints -----
			// ---------------------------------------------------------------
			
			System.out.println("Beginning to create Destination-only measurement constraints.");
			
			// Iterate through the destination-only (D) measurements
			// together with its error measurements
			Iterator dIt = od.getDestinationCounts().entrySet().iterator();
			Iterator dEAIt = od.getDestinationAllErrorsAbove().entrySet().iterator();
			while (dIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry dCount = (Map.Entry) dIt.next();
				Map.Entry dEACount = (Map.Entry) dEAIt.next();
				
				arrStr = dCount.getKey().toString().split(",");
				arrEAStr = dEACount.getKey().toString().split(",");
				
				to = arrStr[0].replace("(","").replace(")","").trim();
				value = dCount.getValue().toString();
				
				toEA = arrEAStr[0].replace("(","").replace(")","");
				valueEA = dEACount.getValue().toString();
				
				// Create the constraints regarding the existing
				// demand-only (D) measurements
				IloNumExpr exprD1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				
				// For each route, if it ends on the given end point,
				// add it to the destination-only (D) count constraint
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// We're only interested in the routes ending in our destination node
					if (!fromTo[1].equalsIgnoreCase(to)) continue;
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String routeValue;
						String routeKey;
						
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						try {
							// Iterate through the route indexes and nodes concomitantly
							Map.Entry entryNodes = (Map.Entry) itNodes.next();
							Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
							
							routeKey = Integer.toString( (Integer)route.getKey() );
							routeValue = (String) route.getValue();
							
							String nodesValue = (String)entryNodes.getValue();
							nodesValue = nodesValue.replace(" ", "").replace(",", "_");
							int varPosition = RouteToLPMapping.get(nodesValue)-1;
							
							((IloLinearNumExpr) exprD1).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[varPosition]
							);
							
							// Get the route error measurements
							dblErrorAbove = Double.parseDouble(valueEA);
							
							//System.out.println("Destination-only constraint " +
							//		to + ": " + nodesValue + " added." );
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.DestinationOnlyMeasurement,
								Double.parseDouble(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.e *
								Double.valueOf(value);
					}
				}
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.addGe(
						exprD1,
						Double.valueOf(value) + dblErrorAbove,
						"Destination "+to
				);
			}
			
			System.out.println("Destination-only measurement constraints created.");
			
			// ---------------------------------------------------------------
			// ------ destination-only (D) demands constraints finished ------
			// ---------------------------------------------------------------
			
			// --------------------------------------------------------------
			// ---- for each existing link count, create its constraints ----
			//  For all alpha in Av, where falpha < falpha' + aalphaU,
			//  set xijH = Qij + bij, for one H in Kij' (not least-cost routes)
			// --------------------------------------------------------------
			
			System.out.println("Beginning to create link counts constraints.");
			
			hmLinkCounts = lCounts.getAllCounts();
			hmErrorsAboveLinkCounts = lCounts.getAllErrorsAbove();
			hmErrorsBelowLinkCounts = lCounts.getAllErrorsBelow();
			
			itLC = hmLinkCounts.entrySet().iterator();
			itEALC = hmErrorsAboveLinkCounts.entrySet().iterator();
			itEBLC = hmErrorsBelowLinkCounts.entrySet().iterator();
			
			double previousRouteValue = -1;
			double actualRouteValue = -1;
			int actualVarPosition = -1;
			
			// Iterate through the link counts
			while (itLC.hasNext()) {
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry arc = (Map.Entry) itLC.next();
				Map.Entry arcEA = (Map.Entry) itEALC.next();
				Map.Entry arcEB = (Map.Entry) itEBLC.next();
				
				arrStr = arc.getKey().toString().split(",");
				arrEAStr = arc.getKey().toString().split(",");
				arrEBStr = arc.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","");
				to = arrStr[1].replace("(","").replace(")","");
				value = arc.getValue().toString();
				
				//System.out.println("Link Count: "+from+","+to+"="+value);
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = arcEA.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = arcEB.getValue().toString();
				
				// create the expression to contain the 
				// constraint on the existing measurements on ARCS
				IloNumExpr expr =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.linearNumExpr();
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				ArrayList<Integer> arrayRouteIndex = new ArrayList<Integer>();
				ArrayList<Double> arrayRouteCost = new ArrayList<Double>();
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String[] routeNodes;
						String routeValue;
						String routeKey;
						
						routeKey = Integer.toString( (Integer) route.getKey() );
						routeValue = (String) route.getValue();
						
						// Iterate through the route indexes and nodes concomitantly
						Map.Entry entryNodes = (Map.Entry) itNodes.next();
						Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
						
						// Obtain the route nodes
						routeNodes = ((String)entryNodes.getValue()).split(",");
						
						// Create the constraints regarding the existing measurements on arcs
						int ODPairsCounter=0;
						
						//if (from.equalsIgnoreCase("488") && to.equalsIgnoreCase("492"))
						//	System.out.println("OD "+fromTo[0]+","+fromTo[1]+". Arc 488->492 found in route "+
						//			entryNodes.getValue());
						
						// If the route doesn't contain the two nodes, continue.
						// If it does, check whether the nodes appear in sequence.
						if (((String)entryNodes.getValue()).indexOf(from+","+to)<0 &&
								((String)entryNodes.getValue()).indexOf(from+", "+to)<0)
							continue;
						
						// Iterate through each node in the route
						for (int j=0; j<routeNodes.length; j++)
						{
							if (j==0) continue;
							
							String fromRoute = routeNodes[j-1].replace("(", "").trim();
							String toRoute = routeNodes[j].replace(")", "").trim();
							
							//if (from.equalsIgnoreCase("488") && to.equalsIgnoreCase("492"))
							//	System.out.println("Arc 488->492 found in route "+
							//			entryNodes.getValue());
							
							// If the route being accessed contains the arc with link counts
							// and is also the greatest-cost route to the OD pair,
							// add it to the model
							if ( fromRoute.equalsIgnoreCase(from) && toRoute.equalsIgnoreCase(to) )
							{
								addedODPairs.add(ODPairsCounter++, fromTo[0]+","+fromTo[1]);
								
								try {
									routeKey = Integer.toString( (Integer)route.getKey() );
									routeValue = (String) route.getValue();
									
									String nodesValue = (String)entryNodes.getValue();
									nodesValue = nodesValue.replace(" ", "").replace(",", "_");
									
									int varPosition = RouteToLPMapping.get(nodesValue)-1;
									
									if (previousRouteValue == -1)
									{
										actualVarPosition = varPosition;
										previousRouteValue = Double.parseDouble(routeValue);
									}
									
									if (previousRouteValue == -1 || actualRouteValue == -1 ||
											previousRouteValue < actualRouteValue)
									{
										actualRouteValue = Double.parseDouble(routeValue);
										actualTempRouteNodes = varPosition;
										
										if (actualRouteValue > previousRouteValue)
											previousRouteValue = actualRouteValue;
										
										// Used afterwards to check the maximum-cost route
										arrayRouteIndex.add(actualTempRouteNodes);
										arrayRouteCost.add(actualRouteValue);
									}
								} catch (Exception e)
								{
									e.printStackTrace();
								}
							} // End if ( fromRoute.equalsIgnoreCase(from) && toRoute.equalsIgnoreCase(to) )
							
							previousRouteValue = actualRouteValue = -1;
						} // End for (int j=0; j<routeNodes.length; j++) --- Iterate through each node in the route
					} // End while (it.hasNext()) --- Iterate through the route costs
				} // End while (itOD.hasNext()) --- Iterate through all the OD pairs
				
				// --------------------------------------------------------
				//  Create the constraint (for H in Kij'):
				//  xijH = xijH + falpha' + aalphaU - falpha
				// --------------------------------------------------------
				
				// Calculate its new value
				Double dblValueEA = Double.parseDouble(valueEA);
				
				// Obtain the maximum cost
				double finalCost = -1;
				int finalIndex = -1;
				for (int f=0; f<arrayRouteCost.size(); f++) {
					if (arrayRouteCost.get(f) > finalCost)
					{
						finalCost = arrayRouteCost.get(f);
						finalIndex = arrayRouteIndex.get(f);
					}
				}
				
				// Update the index with the one of
				// the route with the greatest cost
				actualTempRouteNodes = finalIndex;
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblValueEA = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.LinkCount,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
					} else {
						dblValueEA = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.a *
								Double.valueOf(value);
					}
				}
				
				// If the route has already been added to the model
				// in the OD demands constraints, calculate its new
				// falpha value properly
				if (addedRoutes.containsKey(actualTempRouteNodes))
				{
					// Calculate its new value
					//dblValueEA = Double.valueOf(value) + dblValueEA;
					
					// Delete the term from the expression (constraint)
					Iterator rangeIt = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.rangeIterator();
					while (rangeIt.hasNext())
					{
						IloRange entry = (IloRange) rangeIt.next();
						IloNumExpr rangeExpr = entry.getExpr();
					}
					
				}
				
				// If the arrayRouteCost.size() equals zero that means
				// there is no route, considering all existing OD pairs,
				// which uses the arc analyzed.
				// -----------------------------------------------------
				// Clearly there's an error in the model.
				if (arrayRouteCost.size()>0)
				{
					// Add the term to the expression (constraint)
					((IloLinearNumExpr) expr).addTerm(
							1.0,
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM2[ actualTempRouteNodes ]
					);
					
					if (addedRoutes.get(actualTempRouteNodes) == null)
					{
						// Calculate its new value
						dblValueEA = Double.valueOf(value) + dblValueEA;
						
						addedRoutes.put(actualTempRouteNodes, dblValueEA);
					} else if (addedRoutes.get(actualTempRouteNodes) < dblValueEA)
					{
						addedRoutes.put(actualTempRouteNodes, dblValueEA);
					} else
					{
						dblValueEA = addedRoutes.get(actualTempRouteNodes);
					}
					
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.addGe(
							expr,
							dblValueEA,
							"Arc "+from+"->"+to
					);
				} else
				{
					System.out.print("There is no path using arc "+from+"->"+to+". ");
					System.out.print(" arrayRouteCost.size()==0 to arc "+from+"->"+to+".");
					System.out.println(" Problem unfeasible.");
					System.exit(1);
				}
			} // End while (itLC.hasNext()) --- Iterate through the link counts
			
			System.out.println("Link counts constraints created.");
			
			// --------------------------------------------------------------
			// --------------- link count constraints created ---------------
			// --------------------------------------------------------------
		} catch (IloException e)
		{
			System.err.println("Concert exception caught while creating problem M2: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Problem M2 created.");
		System.out.println();
	}
	
	/**
	 * Method used to solve problem M2 (the upper bound model)
	 * 
	 * @return the value of the solution of M2
	 */
	public double solveProblemM2(String filePath)
	{
		double solution = -1;
		
		try {
			// Write model to file
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.exportModel(filePath+"Problem M2.lp");
			
			// Save a string representation of the Model M2
			strLPModelM2 =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.getModel().toString();
			
			if ( FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.solve() )
			{
				//System.out.println("Solution status = " + FuzzyLPModel.cplexM2.getStatus());
				solution = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.getObjValue();
				double[] val = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.getValues(
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM2
				);
				int ncols = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.getNcols();
				/*
				for (int j = 0; j < ncols; ++j)
				{
					if (val[j]>0)
					{
						System.out.println("Column: " +
								FuzzyLPModel.xM2[j] +
								" Value = " + val[j]);
					}
				}
				*/
			} else {
				System.out.println(
						"Solution error status = " +
						FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.getStatus()
				);
			}
			
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM2.end();
		} catch (IloException e)
		{
			System.err.println("Concert exception caught while trying to solve problem M2: " + e);
		}
		
		return solution;
	}
	
	/**
	 * Method used to effectively create the second (fuzzy) part
	 * of the linear programming problem,
	 * namely problem M8 referenced in Professor Foulds' method.
	 * 
	 * @param graph The graph representing an urban network.
	 * @param lCounts An object containing the observed link counts on arcs.
	 * @param od An object containing the observed OD counts (or the demands between two points in the graph).
	 * @param rc An object containing the costs of each of the possible routes in our graph.
	 * @param lCosts An object containing the costs of each of the arcs in our graph
	 * (used to sum up the costs in all possible routes).
	 */
	public void createProblemM8(AbstractGraph graph, LinkCounts lCounts, ODCounts od,
			RouteCosts rc, LinkCosts lCosts, ODPairs odPairs, LinkCapacities lCap,
			double solutionUpperBound, double solutionLowerBound)
	{
		// Use the solution lower bound (previously described by problem M3)
		// as zero.
		solutionLowerBound = 0;
		
		System.out.println("Beginning to create M8.");
		
		ErrorValueMapping errorValueMapping = new ErrorValueMapping();
		
		try {
			FuzzyLPModel_AllRoutesInObjectiveFunction.RouteToLPMapping = new HashMap<String, Integer>();
			FuzzyLPModel_AllRoutesInObjectiveFunction.LPToODMapping = new HashMap<Integer, String>();
			
			// create model's lower bounds, upper bounds, objective function variables
			double[] arrlb = new double[1];
			double[] arrub = new double[1];
			double[] arrobjvals = new double[1];
			
			HashMap lb = new HashMap();
			HashMap ub = new HashMap();
			HashMap objvals = new HashMap();
			HashMap objnames = new HashMap();
			
			arrlb[0] = 0;
			arrub[0] = 1;
			arrobjvals[0] = 1;
			
			// create the lambda expression and the objective function
			String[] strTempString = new String[1];
			strTempString[0] = new String("lambda");
			
			FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.numVarArray(
						1,
						arrlb,
						arrub,
						strTempString
				);
			
			// ---------------------------------------------------------------
			// ---------------------- create the model -----------------------
			// ---------------------------------------------------------------
			
			Iterator itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			int i=0;
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				String strI = "";
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices and nodes together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					// Iterate through the nodes together with the costs
					Map.Entry nodes = (Map.Entry) itNodes.next();
					strI = (String) nodes.getValue();
					strI = strI.replace(" ", "").replace(",", "_");
					
					// Initialize the lower bounds with ZERO value
					lb.put(i, 0.0);
					
					// Initialize the upper bounds with the biggest number possible
					ub.put(i, Double.MAX_VALUE);
					
					// Initialize the objective function's coefficient
					objvals.put("Variável "+i, Double.valueOf(
							(String) pairs.getValue()).doubleValue()
					);
					
					// Initialize the names
					objnames.put(i-1, "Route_"+(i)+"_"+strI );
					
					// Create the object that maps the routes in the LP
					// to the structure that contains the estimated OD matrix
					// and vice-versa
					try {
						RouteToLPMapping.put(strI, i);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			// Use these arrays to create the problem with integer variables
			int[] intArrayLb = null;
			int[] intArrayUb = null;
			
			// Use these arrays to create the problem with continuous variables
			double[] dblArrayLb = null;
			double[] dblArrayUb = null;
			
			// Create the problem with integer or continuous variables
			if (FuzzyLPModel_AllRoutesInObjectiveFunction.integerProgramming)
			{
				// Allocate memory
				intArrayLb = new int[lb.size()];
				intArrayUb = new int[lb.size()];
				
				// Populate array
				for (i=0; i < intArrayLb.length; i++)
					intArrayLb[i] = ((Double)(lb.get(i+1))).intValue();
				for (i=0; i < intArrayUb.length; i++)
					intArrayUb[i] =  ((Double)(ub.get(i+1))).intValue();
			} else
			{
				// Allocate memory
				dblArrayLb = new double[lb.size()];
				dblArrayUb = new double[lb.size()];
				
				// Populate array
				for (i=0; i < dblArrayLb.length; i++)
					dblArrayLb[i]= ((Double)(lb.get(i+1))).intValue();
				for (i=0; i < dblArrayUb.length; i++)
					dblArrayUb[i]= ((Double)(ub.get(i+1))).intValue();
			}
			
			double[] arrayObjvals = new double[objvals.size()];
			String[] arrayObjstr = new String[objvals.size()];
			
			for (i=0; i < arrayObjvals.length; i++)
					arrayObjvals[i]= ((Double) objvals.get("Variável "+ ((int)i+1) )).doubleValue();
			for (i=0; i < arrayObjvals.length; i++)
					arrayObjstr[i]= (String) objnames.get(i);
			
			// The coefficients of the objective function
			FuzzyLPModel_AllRoutesInObjectiveFunction.arrayObjectiveValues = arrayObjvals;
			
			// create the expressions regarding the constraints
			// Create the problem with integer or continuous variables
			if (FuzzyLPModel_AllRoutesInObjectiveFunction.integerProgramming)
			{
				FuzzyLPModel_AllRoutesInObjectiveFunction.xM8 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.intVarArray(
						i,
						intArrayLb,
						intArrayUb,
						arrayObjstr
					);
				
				// Use custom properties
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.setParam(
						IntParam.MIPEmphasis,
						1
				);
				// If found a solution with 1% gap, stop
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.setParam(
						DoubleParam.EpGap,
						0.01
				);
				// If executed for 10 seconds, stop
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.setParam(
						DoubleParam.TiLim,
						10
				);
			} else
			{
				FuzzyLPModel_AllRoutesInObjectiveFunction.xM8 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.numVarArray(
						i,
						dblArrayLb,
						dblArrayUb,
						arrayObjstr
					);
			}
			
			// ---------------------------------------------------------------
			// ------------- create the new objective function ---------------
			// ---------------------------------------------------------------
			
			// Create the array to provide to the LP model
			// In one entry it goes the lambda part and in the other
			// it goes the routes sum part
			IloNumExpr[] arrayObjFunction = new IloNumExpr[2];
			
			// Create an array with the number of routes with ones (1's)
			double[] arrayRouteObjVals = new double[objvals.size()];
			for (i=0; i < arrayRouteObjVals.length; i++)
				arrayRouteObjVals[i]= 1;
			
			// Create the expression that sums up all the routes
			IloNumExpr routesSum =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.scalProd(
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8,
						arrayRouteObjVals
				);
			
			// Multiply the sum of the routes by minus one (-1)
			IloNumExpr routesObjectiveFunction =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.prod(
					- 1,
					routesSum
				);
			
			// Create the expression containing the lambda
			IloNumExpr lambdaObjectiveFunction =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.scalProd(
					FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda,
					arrobjvals
			);
			
			// Populate the objective function array
			arrayObjFunction[0] = lambdaObjectiveFunction;
			arrayObjFunction[1] = routesObjectiveFunction;
			
			// Create the objective function
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addMaximize(
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.sum(
							arrayObjFunction
					)
			);
			
			// ---------------------------------------------------------------
			// ------------------- create the constraints --------------------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// -------------- add the positiveness constraints ---------------
			// ---------------------------------------------------------------
			
			IloNumExpr expr2 = null;
			i=0;
			
			itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					// add the xM8[i] >= 0 constraint
					expr2 = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
					((IloLinearNumExpr) expr2).addTerm(
							1.0,
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ i - 1 ]
					);
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addGe(
							expr2,
							0,
							"Positiveness_"+
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ i - 1 ].getName()
					);
				}
			}
			
			// ---------------------------------------------------------------
			// --------------- add the upper bound constraint ----------------
			// ---------------------------------------------------------------
			
			IloNumExpr expr3 = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
			i=0;
			
			itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
					
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					((IloLinearNumExpr) expr3).addTerm(
							arrayObjvals[ i - 1 ],
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ i - 1 ]
					);
				}
			}
			
			// Add the expression as a constraint to the model
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addGe(
					expr3,
					solutionLowerBound,
					"Upper_bound"
			);
			
			// ---------------------------------------------------------------
			// --------------- add the lower bound constraint ----------------
			// ---------------------------------------------------------------
			
			IloNumExpr expr4 =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
			i=0;
			
			itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					// Iterate through the indices together with the costs
					Map.Entry indexes = (Map.Entry) itIndexes.next();
						
					try {
						i = (Integer) indexes.getValue();
					} catch (Exception e)
					{
						System.out.println("A variável i=="+i+
								" não pôde ser convertida de String para Integer.");
						e.printStackTrace();
					}
					
					((IloLinearNumExpr) expr4).addTerm(
							arrayObjvals[ i - 1 ],
							FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ i - 1 ]
					);
				}
			}
			
			// Add the last part of the expression (the lambda part)
			//((IloLinearNumExpr) expr4).addTerm( solutionP2 , FuzzyLPModel.xM8lambda[0] );
			
			// Add the expression as a constraint to the model
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addLe(
					expr4,
					solutionUpperBound,
					"Lower_bound"
			);
			
			// ---------------------------------------------------------------
			// ------------- create the link count constraints ---------------
			// ---------------------------------------------------------------
			
			//System.out.println("Beginning to create link counts constraints.");
			
			// HashMap to contain the link counts and its error measurements
			HashMap hmLinkCounts = lCounts.getAllCounts();
			HashMap hmErrorsAboveLinkCounts = lCounts.getAllErrorsAbove();
			HashMap hmErrorsBelowLinkCounts = lCounts.getAllErrorsBelow();
			
			// Iterators to the HashMaps
			Iterator itLC = hmLinkCounts.entrySet().iterator();
			Iterator itEALC = hmErrorsAboveLinkCounts.entrySet().iterator();
			Iterator itEBLC = hmErrorsBelowLinkCounts.entrySet().iterator();
			
			String[] arrStr;
			String[] arrEAStr;
			String[] arrEBStr;
			String from, to, value;
			String fromEA, toEA, valueEA;
			String fromEB, toEB, valueEB;
			
			// Iterate through the link counts
			// together with its error measurements
			while (itLC.hasNext()) {
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry arc = (Map.Entry) itLC.next();
				Map.Entry arcEA = (Map.Entry) itEALC.next();
				Map.Entry arcEB = (Map.Entry) itEBLC.next();
				
				arrStr = arc.getKey().toString().split(",");
				arrEAStr = arc.getKey().toString().split(",");
				arrEBStr = arc.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				to = arrStr[1].replace("(","").replace(")","").trim();
				value = arc.getValue().toString();
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = arcEA.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = arcEB.getValue().toString();
				
				//System.out.println("Link Count: "+from+","+to+"="+value);
				
				// Variable containing the error measurements to the route
				// being analyzed (according to its index)
				double dblErrorAbove = -1;
				double dblErrorBelow = -1;
				
				// Create the constraints for the existing measurements on ARCS
				IloNumExpr expr5 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				IloNumExpr expr6 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// obtain the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// obtain the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// obtain the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String[] routeNodes;
						String routeValue;
						String routeKey;
						
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						// Iterate through the route indexes and nodes concomitantly
						Map.Entry entryNodes = (Map.Entry) itNodes.next();
						Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
						
						// Obtain the route nodes
						routeNodes = ((String)entryNodes.getValue()).split(",");
						
						// If the route doesn't contain the two nodes, continue.
						// If it does, check whether the nodes appear in sequence.
						if (((String)entryNodes.getValue()).indexOf(from+","+to)<0 &&
								((String)entryNodes.getValue()).indexOf(from+", "+to)<0)
							continue;
						
						int ODPairsCounter=0;
						
						// Iterate through each node in the route
						for (int j=0; j<routeNodes.length; j++)
						{
							if (j==0) continue;
							
							String fromRoute = routeNodes[j-1].replace("(", "").trim();
							String toRoute = routeNodes[j].replace(")", "").trim();
							
							// If the arc fits the j-th position of the route,
							// add the route to the constraint
							if (fromRoute.equalsIgnoreCase(from) && toRoute.equalsIgnoreCase(to))
							{
								try {
									addedODPairs.add(ODPairsCounter++, fromTo[0]+","+fromTo[1]);
									
									String nodesValue = (String)entryNodes.getValue();
									nodesValue = nodesValue.replace(" ", "").replace(",", "_");
									int varPosition = RouteToLPMapping.get(nodesValue)-1;
									
									// Get the route error measurements
									dblErrorBelow = Double.parseDouble(valueEB);
									dblErrorAbove = Double.parseDouble(valueEA);
									
									// Create two expressions:
									// one to the upper bound, the other to the lower bound
									((IloLinearNumExpr) expr5).addTerm(
											1.0,
											FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
									);
									
									((IloLinearNumExpr) expr6).addTerm(
											1.0,
											FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
									);
								} catch (Exception e)
								{
									e.printStackTrace();
								}
							}
							
						}
					}
				}
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.LinkCount,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
						
						dblErrorBelow = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.LinkCount,
								//Double.parseDouble(valueEB)
								Double.valueOf(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.a *
								Double.valueOf(value);
						dblErrorBelow= (double)FuzzyLPModel_AllRoutesInObjectiveFunction.a *
								Double.valueOf(value);
					}
				}
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) expr6).addTerm(
						dblErrorAbove,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the lower-or-equal constraint according to the error measurement
				// (described by a fuzzy number). use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addLe(
						expr6,
						Double.valueOf(value) + dblErrorAbove,
						"Link_count_"+from+"_"+to
				);
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) expr5).addTerm(
						- dblErrorBelow,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addGe(
						expr5,
						Double.valueOf(value) - dblErrorBelow,
						"Link_count_"+from+"_"+to
				);
			}
			
			//System.out.println("Link counts constraints created.");
			
			// ---------------------------------------------------------------
			// --------------- link count constraints created ----------------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// -------------- create the LPToODMapping object ----------------
			// ---------------------------------------------------------------
			
			//System.out.println("Beginning to create LPToODMapping object.");
			
			itOD = null;
			try {
				itOD = odPairs.getODPairs().entrySet().iterator();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// Iterate through all the OD pairs
			while (itOD.hasNext())
			{
				Map.Entry odEntry = (Map.Entry)itOD.next();
				
				String[] fromTo = ((String)odEntry.getValue()).split(",");
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route nodes
				Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry route = (Map.Entry) it.next();
					
					// Iterate through the nodes concomitantly
					Map.Entry entryNodes = (Map.Entry) itNodes.next();
					
					// Extract the nodes from the route
					String[] routeNodes;
					String routeValue;
					String routeKey;
					int ODPairsCounter=0;
					
					routeKey = Integer.toString( (Integer)route.getKey() );
					routeValue = (String) route.getValue();
					
					// Populate the array of Strings with the route nodes
					routeNodes = ((String)entryNodes.getValue()).split(",");
					String nodesValue = (String)entryNodes.getValue();
					nodesValue = nodesValue.replace(" ", "").replace(",", "_");
					int varPosition = RouteToLPMapping.get(nodesValue)-1;
					
					try {
						// Populate the HashMap which maps the routes in the LP
						// to the structure containing the OD matrix
						LPToODMapping.put(varPosition, fromTo[0]+"->"+fromTo[1]);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			//System.out.println("LPToODMapping object created.");
			
			// ---------------------------------------------------------------
			// ---------------- LPToODMapping object created -----------------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// ------------- create the OD demands constraints ---------------
			// ---------------------------------------------------------------
			
			//System.out.println("Beginning to create OD measurement constraints.");
			
			// Iterate through the OD measurements together with its
			// error measurements
			Iterator odIt = od.getODCounts().entrySet().iterator();
			Iterator odEAIt = od.getAllErrorsAbove().entrySet().iterator();
			Iterator odEBIt = od.getAllErrorsBelow().entrySet().iterator();
			while (odIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry odCount = (Map.Entry) odIt.next();
				Map.Entry odEACount = (Map.Entry) odEAIt.next();
				Map.Entry odEBCount = (Map.Entry) odEBIt.next();
				
				arrStr = odCount.getKey().toString().split(",");
				arrEAStr = odEACount.getKey().toString().split(",");
				arrEBStr = odEBCount.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				to = arrStr[1].replace("(","").replace(")","").trim();
				value = odCount.getValue().toString();
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = odEACount.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = odEBCount.getValue().toString();
				
				//System.out.println("OD Count: "+from+","+to+"="+value);
				
				// Create the constraints regarding the existing OD measurements
				IloNumExpr exprOD1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				IloNumExpr exprOD2 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				double dblErrorBelow = -1;
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(from, to).entrySet().iterator();
				
				// obtain the route costs
				Iterator itNodes = rc.getRouteNodes(from, to).entrySet().iterator();
				
				// obtain the route costs
				Iterator itIndexes = rc.getRouteIndexes(from, to).entrySet().iterator();
				
				// For each route, if it starts on the given start point and
				// ends on the given end point, add it to the OD count constraint
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry route = (Map.Entry) it.next();
					
					// Extract the nodes from the route
					String[] routeNodes;
					String routeValue;
					String routeKey;
					
					// Iterate through the route indexes and nodes concomitantly
					Map.Entry entryNodes = (Map.Entry) itNodes.next();
					Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
					
					//System.out.println("route: " + entryNodes.getValue());
					
					routeKey = Integer.toString( (Integer)route.getKey() );
					routeValue = (String) route.getValue();
					
					// Populate the array of Strings with the route nodes
					routeNodes = ((String)entryNodes.getValue()).split(",");
					
					addedODPairs.add(from+","+to);
					String nodesValue = (String)entryNodes.getValue();
					nodesValue = nodesValue.replace(" ", "").replace(",", "_");
					int varPosition = RouteToLPMapping.get(nodesValue)-1;
					
					try {
						// Get the route error measurements
						dblErrorBelow = Double.parseDouble(valueEB);
						dblErrorAbove = Double.parseDouble(valueEA);
						
						// Create the expressions regarding the constraints
						((IloLinearNumExpr) exprOD1).addTerm(
								1.0,
								FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
						);
						
						((IloLinearNumExpr) exprOD2).addTerm(
								1.0,
								FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
						);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				} // End while (it.hasNext()) --- Iterate through the route costs
				
				//System.out.println("exprOD: " + exprOD);
				//System.out.println("value: " + value);
				
				if (exprOD1.toString().trim().isEmpty())
					continue;
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.ODMeasurement,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
						
						dblErrorBelow = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.ODMeasurement,
								//Double.parseDouble(valueEB)
								Double.valueOf(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.b *
								Double.valueOf(value);
						dblErrorBelow= (double)FuzzyLPModel_AllRoutesInObjectiveFunction.b *
								Double.valueOf(value);
					}
				}
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) exprOD1).addTerm(
						dblErrorAbove,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the lower-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addLe(
						exprOD1,
						Double.valueOf(value) + dblErrorAbove,
						"OD_measurement_"+from+"_"+to
				);
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) exprOD2).addTerm(
						- dblErrorBelow,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addGe(
						exprOD2,
						Double.valueOf(value) - dblErrorBelow,
						"OD_measurement_"+from+"_"+to
				);
			}
			
			//System.out.println("OD measurement constraints created.");
			
			// ---------------------------------------------------------------
			// --------------- OD demands constraints created ----------------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// -------- create the origin-only (O) demands constraints -------
			// ---------------------------------------------------------------
			
			//System.out.println("Beginning to create Origin-only measurement constraints.");
			
			// Iterate through the origin-only (O) measurements together with its
			// error measurements
			Iterator oIt = od.getOriginCounts().entrySet().iterator();
			Iterator oEAIt = od.getOriginAllErrorsAbove().entrySet().iterator();
			Iterator oEBIt = od.getOriginAllErrorsBelow().entrySet().iterator();
			while (oIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry oCount = (Map.Entry) oIt.next();
				Map.Entry oEACount = (Map.Entry) oEAIt.next();
				Map.Entry oEBCount = (Map.Entry) oEBIt.next();
				
				arrStr = oCount.getKey().toString().split(",");
				arrEAStr = oEACount.getKey().toString().split(",");
				arrEBStr = oEBCount.getKey().toString().split(",");
				
				from = arrStr[0].replace("(","").replace(")","").trim();
				value = oCount.getValue().toString();
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				valueEA = oEACount.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				valueEB = oEBCount.getValue().toString();
				
				// Create the constraints regarding the existing
				// origin-only (O) measurements
				IloNumExpr exprO1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				IloNumExpr exprO2 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				double dblErrorBelow = -1;
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// We're interested only in routes starting from the origin node
					if (!fromTo[0].equalsIgnoreCase(from)) continue;
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String routeValue;
						String routeKey;
						
						// Iterate through each route in the routes object
						//Iterator itRoutes = routes.entrySet().iterator();
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						try {
							// Iterate through the route indexes and nodes concomitantly
							Map.Entry entryNodes = (Map.Entry) itNodes.next();
							Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
							
							routeKey = Integer.toString( (Integer)route.getKey() );
							routeValue = (String) route.getValue();
							
							String nodesValue = (String)entryNodes.getValue();
							nodesValue = nodesValue.replace(" ", "").replace(",", "_");
							int varPosition = RouteToLPMapping.get(nodesValue)-1;
							
							// Get the route error measurements
							dblErrorBelow = Double.parseDouble(valueEB);
							dblErrorAbove = Double.parseDouble(valueEA);
							
							// Create the expressions regarding the constraints
							((IloLinearNumExpr) exprO1).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
							);
							
							((IloLinearNumExpr) exprO2).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
							);
							//((IloLinearNumExpr) exprO1).addTerm( 1.0, FuzzyLPModel.xM3[varPosition] );
							
							// Get the route error measurements
							//dblErrorAbove = Double.parseDouble(valueEA);
							
							//System.out.println("Origin-only constraint " +
							//		from + ": " + nodesValue + " added." );
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.OriginOnlyMeasurement,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
						
						dblErrorBelow = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.OriginOnlyMeasurement,
								//Double.parseDouble(valueEB)
								Double.valueOf(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.d *
								Double.valueOf(value);
						dblErrorBelow= (double)FuzzyLPModel_AllRoutesInObjectiveFunction.d *
								Double.valueOf(value);
					}
				}
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) exprO1).addTerm(
						dblErrorAbove,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the lower-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addLe(
						exprO1,
						Double.valueOf(value) + dblErrorAbove,
						"Origin_"+from
				);
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) exprO2).addTerm(
						- dblErrorBelow,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addGe(
						exprO2,
						Double.valueOf(value) - dblErrorBelow,
						"Origin_"+from
				);
			}
			
			//System.out.println("Origin-only measurement constraints created.");
			
			// ---------------------------------------------------------------
			// ---------- origin-only (O) demands constraints created --------
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// ----- create the destination-only (D) demands constraints -----
			// ---------------------------------------------------------------
			
			//System.out.println("Beginning to create Destination-only measurement constraints.");
			
			// Iterate through the destination-only (D) measurements
			// together with its error measurements
			Iterator dIt = od.getDestinationCounts().entrySet().iterator();
			Iterator dEAIt = od.getDestinationAllErrorsAbove().entrySet().iterator();
			Iterator dEBIt = od.getDestinationAllErrorsBelow().entrySet().iterator();
			while (dIt.hasNext())
			{
				ArrayList addedODPairs = new ArrayList();
				
				Map.Entry dCount = (Map.Entry) dIt.next();
				Map.Entry dEACount = (Map.Entry) dEAIt.next();
				Map.Entry dEBCount = (Map.Entry) dEBIt.next();
				
				arrStr = dCount.getKey().toString().split(",");
				arrEAStr = dEACount.getKey().toString().split(",");
				arrEBStr = dEBCount.getKey().toString().split(",");
				
				to = arrStr[0].replace("(","").replace(")","").trim();
				value = dCount.getValue().toString();
				
				toEA = arrEAStr[0].replace("(","").replace(")","");
				valueEA = dEACount.getValue().toString();
				
				toEB = arrEBStr[0].replace("(","").replace(")","");
				valueEB = dEBCount.getValue().toString();
				
				// Create the constraints regarding the existing
				// demand-only (D) measurements
				IloNumExpr exprD1 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				IloNumExpr exprD2 =
					FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.linearNumExpr();
				
				// Variable containing the error measurements to the route being analyzed
				// (according to its index)
				double dblErrorAbove = -1;
				double dblErrorBelow = -1;
				
				itOD = null;
				try {
					itOD = odPairs.getODPairs().entrySet().iterator();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
				// Iterate through all the OD pairs
				while (itOD.hasNext())
				{
					Map.Entry odEntry = (Map.Entry)itOD.next();
					
					String[] fromTo = ((String)odEntry.getValue()).split(",");
					fromTo[0] = fromTo[0].replace("(", "").trim();
					fromTo[1] = fromTo[1].replace(")", "").trim();
					
					// We're interested only in routes ending in our destination node
					if (!fromTo[1].equalsIgnoreCase(to)) continue;
					
					// Get an iterator to the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Get an iterator to the route costs
					Iterator itIndexes = rc.getRouteIndexes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Extract the nodes from the route
						String routeValue;
						String routeKey;
						
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer)route.getKey() );
						routeValue = (String) route.getValue();
						
						try {
							// Iterate through the route indexes and nodes concomitantly
							Map.Entry entryNodes = (Map.Entry) itNodes.next();
							Map.Entry entryIndexes = (Map.Entry) itIndexes.next();
							
							routeKey = Integer.toString( (Integer)route.getKey() );
							routeValue = (String) route.getValue();
							
							String nodesValue = (String)entryNodes.getValue();
							nodesValue = nodesValue.replace(" ", "").replace(",", "_");
							int varPosition = RouteToLPMapping.get(nodesValue)-1;
							
							// Get the route error measurements
							dblErrorBelow = Double.parseDouble(valueEB);
							dblErrorAbove = Double.parseDouble(valueEA);
							
							// Create the expressions regarding the constraints
							((IloLinearNumExpr) exprD1).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
							);
							
							((IloLinearNumExpr) exprD2).addTerm(
									1.0,
									FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[ varPosition ]
							);
							
							//System.out.println("Destination-only constraint " +
							//		to + ": " + nodesValue + " added." );
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				// Get the error from the errorMapping object,
				// if flag is set to use hard-coded errors
				if (FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors)
				{
					// Use the errors described by the function in the
					// ErrorValueMapping object or the A and B in this class?
					if (FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors)
					{
						dblErrorAbove = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.DestinationOnlyMeasurement,
								//Double.parseDouble(valueEA)
								Double.valueOf(value)
						);
						
						dblErrorBelow = errorValueMapping.getRelativeError(
								ErrorValueMapping.CountType.DestinationOnlyMeasurement,
								//Double.parseDouble(valueEB)
								Double.valueOf(value)
						);
					} else {
						dblErrorAbove = (double)FuzzyLPModel_AllRoutesInObjectiveFunction.e *
								Double.valueOf(value);
						dblErrorBelow= (double)FuzzyLPModel_AllRoutesInObjectiveFunction.e *
								Double.valueOf(value);
					}
				}
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) exprD1).addTerm(
						dblErrorAbove,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the lower-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addLe(
						exprD1,
						Double.valueOf(value) + dblErrorAbove,
						"Destination_"+to
				);
				
				// Add the last part of the constraint expression (the lambda)
				((IloLinearNumExpr) exprD2).addTerm(
						- dblErrorBelow,
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8lambda[0]
				);
				
				// Create the greater-or-equal constraint according to the error measurement
				// (described by a fuzzy number). Use absolute error measurement.
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.addGe(
						exprD2,
						Double.valueOf(value) - dblErrorBelow,
						"Destination_"+to
				);
			}
			
			//System.out.println("Destination-only measurement constraints created.");
			
			// ---------------------------------------------------------------
			// ------- destination-only (D) demands constraints created ------
			// ---------------------------------------------------------------
			
		} catch (IloException e)
		{
			System.err.println("Concert exception caught: " + e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Problem M8 created.");
		System.out.println();
	}
	
	/**
	 * Method used to solve the second (fuzzy) part of the linear
	 * programming problem, namely problem M8 referenced in
	 * Professor Foulds' method.
	 * 
	 * @return
	 */
	public double solveProblemM8(String filePath)
	{
		double solution = -1;
		
		// The OD pairs that has positive estimated flows
		ArrayList <Integer> usedOD = new ArrayList <Integer> ();
		
		// Initialize the estimated link counts
		FuzzyLPModel_AllRoutesInObjectiveFunction.linkCounts = new HashMap();
		
		// Initialize the estimated counts on routes
		FuzzyLPModel_AllRoutesInObjectiveFunction.routeCounts = new HashMap();
		
		try {
			// Write model to file
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.exportModel(
					filePath+"Problem M8 - All routes in objective function.lp"
			);
			
			// Save a string representation of the LP Model
			FuzzyLPModel_AllRoutesInObjectiveFunction.strLPModel =
				FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.getModel().toString();
			
			if ( FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.solve() )
			{
				//System.out.println("Solution status = " + FuzzyLPModel.cplexM8.getStatus());
				solution = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.getObjValue();
				//System.out.println("solution: " + solution);
				
				double[] val = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.getValues(
						FuzzyLPModel_AllRoutesInObjectiveFunction.xM8
				);
				
				int ncols = FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.getNcols();
				
				// Calculate the solution and create an object with the used OD pairs
				FuzzyLPModel_AllRoutesInObjectiveFunction.solutionM8=0;
				for (int j = 0; j < val.length; j++)
				{
					FuzzyLPModel_AllRoutesInObjectiveFunction.routeCounts.put(
							""+FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[j],
							val[j]
					);
					FuzzyLPModel_AllRoutesInObjectiveFunction.linkCounts.put(
							""+FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[j],
							val[j]
					);
					
					if (val[j]>0)
					{
						System.out.println("Column (route): " +
								FuzzyLPModel_AllRoutesInObjectiveFunction.xM8[j] +
								" Value = " + val[j]);
					}
					
					FuzzyLPModel_AllRoutesInObjectiveFunction.solutionM8 += (
							val[j] *
							FuzzyLPModel_AllRoutesInObjectiveFunction.arrayObjectiveValues[j]
					);
					
					if (val[j]>0) usedOD.add(j);
				}
				//System.out.println("FuzzyLPModel.solutionM8: " + FuzzyLPModel.solutionM8);
				//System.exit(-1);
				//System.out.println("RouteToLPMapping: " + RouteToLPMapping);
				//System.out.println("LPToODMapping: " + LPToODMapping);
				//System.out.println("Used OD pairs: " + usedOD);
				
				// Populate the OD matrix object entries
				for (int key : usedOD)
				{
					String[] index;
					if (LPToODMapping.get(key) != null)
					{
						index = ((String) LPToODMapping.get(key)).split("->");
						
						FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrix.get(index[0]).
								get(index[1]).set(""+
								(
										Double.parseDouble(
												FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrix.get(
														index[0]
												).get(index[1]).get().toString()
										) + val[key]
								)
						);
					}
				}
			} else {
				System.out.println("Solution error status = " +
						FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.getStatus());
			}
			
			FuzzyLPModel_AllRoutesInObjectiveFunction.cplexM8.end();
		} catch (IloException e)
		{
			System.err.println("Concert exception caught while trying to solve problem M8: " + e);
		}
		
		return solution;
	}
	
	/**
	 * Getter: get the estimated link counts
	 * 
	 * @return the link counts estimated by M8
	 */
	public HashMap<String, Double> getValueLinkCounts()
	{
		HashMap<String, Double> hashResult = new HashMap<String, Double>();
		
		// Initialize the hashmap's values
		Iterator it = FuzzyLPModel_AllRoutesInObjectiveFunction.linkCounts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String strKey = (String) entry.getKey();
			
			// Get the arcs from the route
			// String is in the format Route_3_(4_9_10_12_3)
			String[] strTemp = strKey.split("_");
			
			// Remove the first and last parenthesis
			strTemp[2] = strTemp[2].replace("(","").trim();
			strTemp[strTemp.length-1] = strTemp[strTemp.length-1].replace(")","").trim();
			
			// Iterate through all the arcs in the route,
			// beggining from the second until the last
			for (int i=3; i<strTemp.length; i++) {
				// Populate the result hashmap
				hashResult.put(
						strTemp[i-1]+"->"+strTemp[i],
						0.0
				);
			}
		}
		
		// Update the hashmap with the correct values
		it = FuzzyLPModel_AllRoutesInObjectiveFunction.linkCounts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			
			String strKey = (String) entry.getKey();
			Double dblValue = (Double) entry.getValue();
			
			// Get the arcs from the route
			// String is in the format Route_3_(4_9_10_12_3)
			String[] strTemp = strKey.split("_");
			
			// Remove the first and last parenthesis
			strTemp[2] = strTemp[2].replace("(","").trim();
			strTemp[strTemp.length-1] = strTemp[strTemp.length-1].replace(")","").trim();
			
			// Iterate through all the arcs in the route,
			// beggining from the second until the last
			for (int i=3; i<strTemp.length; i++) {
				// Populate the result hashmap
				hashResult.put(
						strTemp[i-1]+"->"+strTemp[i],
						hashResult.get(strTemp[i-1]+"->"+strTemp[i]) + dblValue
				);
			}
		}
		
		return hashResult;
	}
	
	/**
	 * @return the flows estimated by M8 to all routes in the model
	 */
	public HashMap<String, Double> getValueRouteCounts()
	{
		HashMap<String, Double> hashResult = new HashMap<String, Double>();
		
		// Initialize the hashmap's values
		Iterator it = FuzzyLPModel_AllRoutesInObjectiveFunction.routeCounts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String strKey = (String) entry.getKey();
			
			// String is in the format Route_3_(4_9_10_12_3)
			String[] strTemp = strKey.split("_");
			String strRoute = "";
			
			strTemp[2] = strTemp[2].replace("(", "").trim();
			strTemp[strTemp.length-1] = strTemp[strTemp.length-1].replace(")", "").trim();
			
			strRoute = "(";
			// Iterate through all the arcs in the route
			for (int i=2; i<strTemp.length; i++)
				strRoute += strTemp[i]+",";
			
			// Remove the last comma and add the parenthesis
			strRoute = strRoute.substring(0, strRoute.length()-1).concat(")");
			
			// Populate the result hashmap
			hashResult.put( strRoute, 0.0 );
		}
		
		// Update the hashmap with the correct values
		it = FuzzyLPModel_AllRoutesInObjectiveFunction.routeCounts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			
			String strKey = (String) entry.getKey();
			Double dblValue = (Double) entry.getValue();
			
			// String is in the format Route_3_(4_9_10_12_3)
			String[] strTemp = strKey.split("_");
			String strRoute = "";
			
			strTemp[2] = strTemp[2].replace("(", "").trim();
			strTemp[strTemp.length-1] = strTemp[strTemp.length-1].replace(")", "").trim();
			
			strRoute = "(";
			// Iterate through all the arcs in the route
			for (int i=2; i<strTemp.length; i++)
				strRoute += strTemp[i]+",";
			
			// Remove the last comma and add the parenthesis
			strRoute = strRoute.substring(0, strRoute.length()-1).concat(")");
			
			// Populate the result hashmap
			hashResult.put( strRoute, hashResult.get(strRoute) + dblValue );
		}
		
		return hashResult;
	}
	
	/**
	 * Getter: get the solution to M8
	 * 
	 * @return value of the solution of M8
	 */
	public double getValueSolutionM8()
	{
		return FuzzyLPModel_AllRoutesInObjectiveFunction.solutionM8;
	}
	
	/**
	 * Initialize the matrices odMatrixNodes and odMatrix,
	 * used to store the solution of the problem
	 * 
	 * @param value
	 */
	public void setODMatrixNodes(HashMap value)
	{
		FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrixNodes = value;
		FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrix = new AutoMapValue <String, String>();
		
		Set s = FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrixNodes.entrySet();
		for (Object entry : s)
		{
			String index1 = (String) ((Map.Entry)entry).getValue();
			index1 = Integer.toString( Integer.parseInt(index1) );
			for (Object entry2 : s)
			{
				String index2 = (String) ((Map.Entry)entry2).getValue();
				index2 = Integer.toString( Integer.parseInt(index2) );
				
				FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrix.get(index1).get(index2).set("0");
				//System.out.println("OD pair: " + index1 + ", " + index2);
			}
		}
	}
	
	/**
	 * Getter: get the solution OD Matrix
	 * 
	 * @return
	 */
	public AutoMapValue getODMatrix()
	{
		return FuzzyLPModel_AllRoutesInObjectiveFunction.odMatrix;
	}
	
	public FuzzyLPModel_AllRoutesInObjectiveFunction clone() throws CloneNotSupportedException
	{
		return (FuzzyLPModel_AllRoutesInObjectiveFunction) super.clone();
	}

	/**
	 * @return the integerProgramming
	 */
	public boolean isIntegerProgramming() {
		return integerProgramming;
	}

	/**
	 * @param integerProgramming the integerProgramming to set
	 */
	public void setIntegerProgramming(boolean integerProgramming) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.integerProgramming = integerProgramming;
	}
	
	/**
	 * Getter
	 * @return the model M8
	 */
	public String getLPModel() {
		return FuzzyLPModel_AllRoutesInObjectiveFunction.strLPModel;
	}
	
	/**
	 * Getter
	 * @return the model M2
	 */
	public String getLPModelM2() {
		return FuzzyLPModel_AllRoutesInObjectiveFunction.strLPModelM2;
	}
	
	/**
	 * @return the useCodedErrors
	 */
	public boolean isUseCodedErrors() {
		return useCodedErrors;
	}

	/**
	 * @param useCodedErrors the useCodedErrors to set
	 */
	public void setUseCodedErrors(boolean useCodedErrors) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.useCodedErrors = useCodedErrors;
	}
	
	/**
	 * @return the useGradientErrors
	 */
	public boolean isUseGradientErrors() {
		return useGradientErrors;
	}

	/**
	 * @param useGradientErrors the useGradientErrors to set
	 */
	public void setUseGradientErrors(boolean useGradientErrors) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.useGradientErrors = useGradientErrors;
	}
	
	/**
	 * @return the a
	 */
	public float getA() {
		return a;
	}

	/**
	 * @param a the a to set
	 */
	public void setA(float a) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.a = a;
	}

	/**
	 * @return the b
	 */
	public float getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(float b) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.b = b;
	}

	/**
	 * @return the d
	 */
	public float getD() {
		return d;
	}

	/**
	 * @param d the d to set
	 */
	public void setD(float d) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.d = d;
	}

	/**
	 * @return the e
	 */
	public float getE() {
		return e;
	}

	/**
	 * @param e the e to set
	 */
	public void setE(float e) {
		FuzzyLPModel_AllRoutesInObjectiveFunction.e = e;
	}

	@Override
	public String getLPModelM3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoMapValue getODMatrixM3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Double> getValueLinkCountsM3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Double> getValueRouteCountsM3() {
		// TODO Auto-generated method stub
		return null;
	}
}
