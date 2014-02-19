/**
 * 
 */
package cplexModels;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.jgrapht.alg.FloydWarshallShortestPaths;
//import org.jgrapht.alg.FloydWarshall2;
//import org.jgrapht.alg.KShortestPaths;
import odEstimation.AutoMapValue;
import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.alg.Node;
//import org.jgrapht.alg.Edge;
import org.jgrapht.graph.AbstractGraph;
//import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;



//import org.jgrapht.graph.DefaultDirectedWeightedGraph;
//import org.jgrapht.GraphPath;
//import org.jgrapht.Graph;

import ilog.concert.*;
import ilog.cplex.*;

/**
 * @author Iacer Calixto
 */
public class SheraliLPModel
{
	/**
	 * The cplex object containing Sherali's LP model
	 */
	private static IloCplex cplexModel = null;
	
	/**
	 * The variables of the model
	 */
	private static IloNumVar[] cplexVariables = null;
	
	/**
	 * The variables containing the positive link count errors
	 */
	private static IloNumVar[] yLinkCountErrorAbove = null;
	
	/**
	 * The variables containing the negative link count errors
	 */
	private static IloNumVar[] yLinkCountErrorBelow = null;
	
	/**
	 * The variables containing the positive OD measurement errors
	 */
	private static IloNumVar[] yODCountErrorAbove = null;
	
	/**
	 * The variables containing the negative OD measurement errors
	 */
	private static IloNumVar[] yODCountErrorBelow = null;
	
	/**
	 *  The object to hold the estimated OD matrix
	 */
	private static AutoMapValue <String, String> odMatrix = null;
	
	/**
	 * The OD matrix valid nodes
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
	 * The variable to hold the estimated counts on used routes
	 */
	private static HashMap <String, Double> routeCounts = null;
	
	/**
	 * The variable to hold the estimated link counts
	 */
	private static HashMap <String, Double> linkCounts = null;
	
	/**
	 * The constant to which multiply the sum of the link counts
	 * in the model's objective function. See Sherali et al. (1994)
	 */
	private static double linkCountErrorsMultiplier;
	
	/**
	 * The constant to which multiply the sum of the OD estimates
	 * in the model's objective function. See Sherali et al. (1994)
	 */
	private static double odCountErrorsMultiplier;
	
	/**
	 * The String representation of the linear programming model
	 */
	private static String strLPModel = null;
	
	/**
	 * Constructor
	 */
	public SheraliLPModel()
	{
		this.init();
	}
	
	/**
	 * Initialize required variables.
	 */
	public void init()
	{
		// Use a big value as the link count errors' multiplier
		this.linkCountErrorsMultiplier = 999000000;
		
		// Use a value to the OD estimate errors' multiplier that is 10%
		// of the one of the link counts estimates
		this.odCountErrorsMultiplier = this.linkCountErrorsMultiplier * 0.1;
		
		// Initialize the object used to map the LP structures
		// to the final generated OD matrix
		SheraliLPModel.LPToODMapping = new HashMap<Integer, String>();
		SheraliLPModel.RouteToLPMapping = new HashMap<String, Integer>();
		
		try {
			SheraliLPModel.cplexModel = new IloCplex();
		} catch (IloException e)
		{
			System.err.println("Concert exception caught: " + e);
		}
	}
	
	/**
	 * Method used to effectively create Sherali's LP(TT) model.
	 * See Sherali et al. (1994) and Sherali et al. (2003).
	 * 
	 * @param graph The graph representing an urban network.
	 * @param lCounts An object containing the observed link counts on arcs.
	 * @param od An object containing the observed OD counts (or the demands between two points in the graph).
	 * @param rc An object containing the costs of each of the possible routes in our graph.
	 * @param lCosts An object containing the costs of each of the arcs in our graph
	 * (used to sum up the costs in all possible routes).
	 * @param odPairs The valid OD pairs in the model.
	 */
	public static void createSherali1994Problem(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs)
	{
		try {
			SheraliLPModel.RouteToLPMapping = new HashMap<String, Integer>();
			SheraliLPModel.LPToODMapping = new HashMap<Integer, String>();
			
			// create model (lower bounds, upper bounds, objective function variables)
			HashMap<Integer, Double> lb = new HashMap<Integer, Double>();
			HashMap<Integer, Double> ub = new HashMap<Integer, Double>();
			HashMap<Integer, Double> objvals = new HashMap<Integer, Double>();
			HashMap<Integer, String> objnames = new HashMap<Integer, String>();
			
			System.out.println(rc.toString());
			
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
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry)it.next();
					
					//iterate through the indices together with the costs
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
			
			System.out.println();
			
			// -------------------------------------------------------------
			// --------------- Create the objective function ---------------
			// -------------------------------------------------------------
			
			// converting HashMap into double[]
			double[] arrayLb = new double[lb.size()];
			double[] arrayUb = new double[ub.size()];
			double[] arrayObjvals = new double[objvals.size()];
			String[] arrayObjnames = new String[objnames.size()];
			
			for (i=0; i < arrayLb.length; i++)
					arrayLb[i]= ((Double) lb.get(i+1)).doubleValue();
			for (i=0; i < arrayUb.length; i++)
					arrayUb[i]= ((Double) ub.get(i+1)).doubleValue();
			for (i=0; i < arrayObjvals.length; i++)
					arrayObjvals[i]= ((Double) objvals.get(i+1)).doubleValue();
			for (i=0; i < arrayObjnames.length; i++)
					arrayObjnames[i]= objnames.get(i+1);
			
			// --------------------------------------------------------------
			// -- for each existing link count, create its error variables --
			// --------------------------------------------------------------
			HashMap hmLinkCounts = lCounts.getAllCounts();
			HashMap hmErrorsAboveLinkCounts = lCounts.getAllErrorsAbove();
			HashMap hmErrorsBelowLinkCounts = lCounts.getAllErrorsBelow();
			
			// Link count error variables
			double[] arrayLbYLinkCountErrors = new double[hmLinkCounts.size()];
			double[] arrayUbYLinkCountErrors = new double[hmLinkCounts.size()];
			double[] arrayObjvalsYLinkCountErrorAbove = new double[hmLinkCounts.size()];
			double[] arrayObjvalsYLinkCountErrorBelow = new double[hmLinkCounts.size()];
			String[] arrayObjnamesYLinkCountErrorAbove = new String[hmLinkCounts.size()];
			String[] arrayObjnamesYLinkCountErrorBelow = new String[hmLinkCounts.size()];
			
			Iterator itLC = hmLinkCounts.entrySet().iterator();
			Iterator itEALC = hmErrorsAboveLinkCounts.entrySet().iterator();
			Iterator itEBLC = hmErrorsBelowLinkCounts.entrySet().iterator();
			
			String[] arrStr;
			String[] arrEAStr;
			String[] arrEBStr;
			String from, to, value;
			String fromEA, toEA, valueEA;
			String fromEB, toEB, valueEB;
			
			int counterYLinkCountErrors = 0;
			
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
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = arcEA.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = arcEB.getValue().toString();
				
				// Put the artificial variable lower bound as zero
				arrayLbYLinkCountErrors[counterYLinkCountErrors] = 0;
				// Don't create a upper bound to the artificial variable
				arrayUbYLinkCountErrors[counterYLinkCountErrors] = Double.POSITIVE_INFINITY;
				// Create the name of the arcs' constraints
				arrayObjnamesYLinkCountErrorBelow[counterYLinkCountErrors] = "Arc_"+from+"to"+to+"_Below";
				arrayObjnamesYLinkCountErrorAbove[counterYLinkCountErrors] = "Arc_"+from+"to"+to+"_Above";
				// Create arrays with the error above and below
				arrayObjvalsYLinkCountErrorBelow[counterYLinkCountErrors] = Double.parseDouble(valueEB);
				arrayObjvalsYLinkCountErrorAbove[counterYLinkCountErrors] = Double.parseDouble(valueEA);
				
				counterYLinkCountErrors++;
			}
			
			// ----------------------------------------------------------------
			// --- create the variables regarding the OD error measurements ---
			// ----------------------------------------------------------------
			
			// OD count errors variables
			double[] arrayLbYODCountErrors = new double[od.getODCounts().entrySet().size()];
			double[] arrayUbYODCountErrors = new double[od.getODCounts().entrySet().size()];
			double[] arrayObjvalsYODCountErrorAbove = new double[od.getODCounts().entrySet().size()];
			double[] arrayObjvalsYODCountErrorBelow = new double[od.getODCounts().entrySet().size()];
			String[] arrayObjnamesYODCountErrorAbove = new String[od.getODCounts().entrySet().size()];
			String[] arrayObjnamesYODCountErrorBelow = new String[od.getODCounts().entrySet().size()];
			
			int counterYODCountErrors = 0;
			
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
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = odEACount.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = odEBCount.getValue().toString();
				
				// Put the artificial variable lower bound as zero
				arrayLbYODCountErrors[counterYODCountErrors] = 0;
				// Don't create a upper bound to the artificial variable
				arrayUbYODCountErrors[counterYODCountErrors] = Double.POSITIVE_INFINITY;
				// Create the names of the OD measurement constraints
				arrayObjnamesYODCountErrorAbove[counterYODCountErrors] = "OD "+from+"to"+to+"_Above";
				arrayObjnamesYODCountErrorBelow[counterYODCountErrors] = "OD "+from+"to"+to+"_Below";
				// Create arrays with the error above and below
				arrayObjvalsYODCountErrorAbove[counterYODCountErrors] = Double.parseDouble(valueEA);
				arrayObjvalsYODCountErrorBelow[counterYODCountErrors] = Double.parseDouble(valueEB);
				
				counterYODCountErrors++;
			}
			
			
			
			
			
			
			
			// ---------------------------------------------------------
			// Create the first part of the objective function
			// ---------------------------------------------------------
			// sum (for each ij in OD) sum (for each k in K) Cijk * xijk
			// ---------------------------------------------------------
			SheraliLPModel.cplexVariables = SheraliLPModel.cplexModel.numVarArray(
					i,
					arrayLb,
					arrayUb,
					arrayObjnames
			);
			IloNumExpr routeCostsSum = SheraliLPModel.cplexModel.scalProd(
					SheraliLPModel.cplexVariables,
					arrayObjvals
			);
			
			// ---------------------------------------------------------
			// Create the 2nd part of the objective function
			// ---------------------------------------------------------
			// sum (for each a in A) ya+ + ya-
			// ---------------------------------------------------------
			SheraliLPModel.yLinkCountErrorAbove = SheraliLPModel.cplexModel.numVarArray(
					counterYLinkCountErrors,
					arrayLbYLinkCountErrors,
					arrayUbYLinkCountErrors,
					arrayObjnamesYLinkCountErrorAbove
			);
			
			SheraliLPModel.yLinkCountErrorBelow = SheraliLPModel.cplexModel.numVarArray(
					counterYLinkCountErrors,
					arrayLbYLinkCountErrors,
					arrayUbYLinkCountErrors,
					arrayObjnamesYLinkCountErrorBelow
			);
			
			// ---------------------------------------------------------
			// Create the 3rd part of the objective function
			// ---------------------------------------------------------
			// sum (for each i,j in OD) Yij+ + Yij-
			// ---------------------------------------------------------
			SheraliLPModel.yODCountErrorAbove = SheraliLPModel.cplexModel.numVarArray(
					counterYODCountErrors,
					arrayLbYODCountErrors,
					arrayUbYODCountErrors,
					arrayObjnamesYODCountErrorAbove
			);
			
			SheraliLPModel.yODCountErrorBelow = SheraliLPModel.cplexModel.numVarArray(
					counterYODCountErrors,
					arrayLbYODCountErrors,
					arrayUbYODCountErrors,
					arrayObjnamesYODCountErrorBelow
			);
			
			// --------------------------------------------------------------
			// --------------- for each existing link count, ----------------
			// -------- create the entries in the objective function --------
			// --------------------------------------------------------------
			IloNumExpr linkCountErrorsAbove = SheraliLPModel.cplexModel.scalProd(
					SheraliLPModel.yLinkCountErrorAbove,
					arrayObjvalsYLinkCountErrorAbove
			);
			
			IloNumExpr linkCountErrorsBelow = SheraliLPModel.cplexModel.scalProd(
					SheraliLPModel.yLinkCountErrorBelow,
					arrayObjvalsYLinkCountErrorBelow
			);
			
			// ---------------------------------------------------------------
			// ------------- for each existing OD measurement, ---------------
			// -------- create the entries in the objective function ---------
			// ---------------------------------------------------------------
			IloNumExpr ODCountErrorsAbove = SheraliLPModel.cplexModel.scalProd(
					SheraliLPModel.yODCountErrorAbove,
					arrayObjvalsYODCountErrorAbove
			);
			
			IloNumExpr ODCountErrorsBelow = SheraliLPModel.cplexModel.scalProd(
					SheraliLPModel.yODCountErrorBelow,
					arrayObjvalsYODCountErrorBelow
			);
			
			// ---------------------------------------------------------------
			// Add the values to the objective function
			// ---------------------------------------------------------------
			
			// Create the array to provide to the LP model
			IloNumExpr[] arrayObjFunction = new IloNumExpr[5];
			
			// Multiply the link count errors by M
			IloNumExpr linkCountErrorsAboveMultiplied = SheraliLPModel.cplexModel.prod(
					SheraliLPModel.linkCountErrorsMultiplier,
					linkCountErrorsAbove
			);
			IloNumExpr linkCountErrorsBelowMultiplied = SheraliLPModel.cplexModel.prod(
					SheraliLPModel.linkCountErrorsMultiplier,
					linkCountErrorsBelow
			);
			
			// Multiply the OD count errors by M'
			IloNumExpr odCountErrorsAboveMultiplied = SheraliLPModel.cplexModel.prod(
					SheraliLPModel.odCountErrorsMultiplier,
					ODCountErrorsAbove
			);
			IloNumExpr odCountErrorsBelowMultiplied = SheraliLPModel.cplexModel.prod(
					SheraliLPModel.odCountErrorsMultiplier,
					ODCountErrorsBelow
			);
			
			arrayObjFunction[0] = routeCostsSum;
			arrayObjFunction[1] = linkCountErrorsAboveMultiplied;
			arrayObjFunction[2] = linkCountErrorsBelowMultiplied;
			arrayObjFunction[3] = odCountErrorsAboveMultiplied;
			arrayObjFunction[4] = odCountErrorsBelowMultiplied;
			
			SheraliLPModel.cplexModel.addMinimize(
					SheraliLPModel.cplexModel.sum(
							arrayObjFunction
					)
			);
			
			// --------------------------------------------------------------
			// ----------------- create the constraints ---------------------
			// --------------------------------------------------------------
			
			// --------------------------------------------------------------
			// --------- first add the positiveness constraints -------------
			// --------------------------------------------------------------
			
			// --------------------------------------------------------------
			// ----------- add routes positiveness constraints --------------
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
					
					// add the cplexVariables[i] >= 0 constraint
					IloNumExpr expr = SheraliLPModel.cplexModel.linearNumExpr();
					((IloLinearNumExpr) expr).addTerm( 1.0, SheraliLPModel.cplexVariables[ i - 1 ] );
					SheraliLPModel.cplexModel.addGe( expr, 0 );
				}
			}
			
			// --------------------------------------------------------------
			// ------- add link count errors positiveness constraints -------
			// --------------------------------------------------------------
			
			// Iterate through the link counts
			for (int f=0; f<counterYLinkCountErrors; f++)
			{
				// add the yLinkCountErrorAbove[i] >= 0
				// and the yLinkCountErrorBelow[i] >= 0 constraints
				IloNumExpr exprAbove = SheraliLPModel.cplexModel.linearNumExpr();
				IloNumExpr exprBelow = SheraliLPModel.cplexModel.linearNumExpr();
				((IloLinearNumExpr) exprAbove).addTerm(
						1.0,
						SheraliLPModel.yLinkCountErrorAbove[ f ]
				);
				((IloLinearNumExpr) exprBelow).addTerm(
						1.0,
						SheraliLPModel.yLinkCountErrorBelow[ f ]
				);
				SheraliLPModel.cplexModel.addGe( exprAbove, 0 );
				SheraliLPModel.cplexModel.addGe( exprBelow, 0 );
			}
			
			// --------------------------------------------------------------
			// -------- add OD count errors positiveness constraints --------
			// --------------------------------------------------------------
			
			// Iterate through the OD counts
			for (int f=0; f<counterYODCountErrors; f++)
			{
				// add the yODCountErrorAbove[i] >= 0
				// and the yODCountErrorBelow[i] >= 0 constraints
				IloNumExpr exprAbove = SheraliLPModel.cplexModel.linearNumExpr();
				IloNumExpr exprBelow = SheraliLPModel.cplexModel.linearNumExpr();
				((IloLinearNumExpr) exprAbove).addTerm(
						1.0,
						SheraliLPModel.yODCountErrorAbove[ f ]
				);
				((IloLinearNumExpr) exprBelow).addTerm(
						1.0,
						SheraliLPModel.yODCountErrorBelow[ f ]
				);
				SheraliLPModel.cplexModel.addGe( exprAbove, 0 );
				SheraliLPModel.cplexModel.addGe( exprBelow, 0 );
			}
			
			// --------------------------------------------------------------
			// ------------- positiveness constraints finished --------------
			// --------------------------------------------------------------
			
			// --------------------------------------------------------------
			// ------------- create the link counts constraints -------------
			// --------------------------------------------------------------
			
			hmLinkCounts = lCounts.getAllCounts();
			hmErrorsAboveLinkCounts = lCounts.getAllErrorsAbove();
			hmErrorsBelowLinkCounts = lCounts.getAllErrorsBelow();
			
			itLC = hmLinkCounts.entrySet().iterator();
			itEALC = hmErrorsAboveLinkCounts.entrySet().iterator();
			itEBLC = hmErrorsBelowLinkCounts.entrySet().iterator();
			
			counterYLinkCountErrors = 0;
			
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
				
				fromEA = arrEAStr[0].replace("(","").replace(")","");
				toEA = arrEAStr[1].replace("(","").replace(")","");
				valueEA = arcEA.getValue().toString();
				
				fromEB = arrEBStr[0].replace("(","").replace(")","");
				toEB = arrEBStr[1].replace("(","").replace(")","");
				valueEB = arcEB.getValue().toString();
				
				//System.out.println("Link Count: "+from+","+to+"="+value);
				
				// create the expressions to contain the 
				// constraint on the existing measurements on ARCS
				
				// Create an array to create the sum expression
				IloNumExpr[] exprArray = new IloNumExpr[2];
				IloNumExpr expr = SheraliLPModel.cplexModel.linearNumExpr();
				
				// Create the link count constraint including
				// the error measurements above and below
				IloNumExpr exprLinkErrorAbove = SheraliLPModel.cplexModel.linearNumExpr();
				IloNumExpr exprLinkErrorBelow = SheraliLPModel.cplexModel.linearNumExpr();
				
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
					
					//System.out.println("OD Pair "+fromTo[0]+"->"+fromTo[1]);
					
					// obtain the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// obtain the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Iterate through the route indexes and nodes concomitantly
						Map.Entry entryNodes = (Map.Entry) itNodes.next();
						
						// Extract the nodes from the route
						String[] routeNodes;
						String routeValue;
						String routeKey;
						
						routeKey = Integer.toString( (Integer) route.getKey());
						routeValue = (String) route.getValue();
						
						// Obtain the route nodes
						routeNodes = ((String)entryNodes.getValue()).split(",");
						
						// If the route doesn't contain the two nodes, continue.
						// If it does, check whether the nodes appear in sequence.
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
							
							// If the route being accessed contains the arc with link counts
							// Add the route to the model
							if ( fromRoute.equalsIgnoreCase(from) && toRoute.equalsIgnoreCase(to) )
							{
								try {
									addedODPairs.add(ODPairsCounter++, fromTo[0]+","+fromTo[1]);
									
									String nodesValue = (String)entryNodes.getValue();
									nodesValue = nodesValue.replace(" ", "").replace(",", "_");
									int varPosition = RouteToLPMapping.get(nodesValue)-1;
									
									// Add the term to the expression (constraint)
									((IloLinearNumExpr) expr).addTerm(
											1.0,
											SheraliLPModel.cplexVariables[ varPosition ]
									);
									
									// Sum the errors only once
									if (addedODPairs.size()<=1)
									{
										((IloLinearNumExpr) exprLinkErrorAbove).addTerm(
												1.0,
												SheraliLPModel.yLinkCountErrorAbove[ counterYLinkCountErrors ]
										);
										
										((IloLinearNumExpr) exprLinkErrorBelow).addTerm(
												1.0,
												SheraliLPModel.yLinkCountErrorBelow[ counterYLinkCountErrors ]
										);
									}
								} catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				
				exprArray[0] = expr;
				exprArray[1] = SheraliLPModel.cplexModel.diff(exprLinkErrorAbove, exprLinkErrorBelow);
				
				SheraliLPModel.cplexModel.addEq(
						SheraliLPModel.cplexModel.sum(exprArray),
						Double.valueOf(value)
				);
				
				counterYLinkCountErrors++;
			}
			
			// --------------------------------------------------------------
			// -------------- link counts constraints finished --------------
			// --------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// ---- create the constraints on the existing OD measurements ---
			// ---------------------------------------------------------------
			
			counterYODCountErrors = 0;
			
			// Iterate through the OD measurements
			odIt = od.getODCounts().entrySet().iterator();
			odEAIt = od.getAllErrorsAbove().entrySet().iterator();
			odEBIt = od.getAllErrorsBelow().entrySet().iterator();
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
				
				// Create the constraint regarding the existing OD measurements
				
				// Create an array to create the sum expression
				IloNumExpr[] exprArray = new IloNumExpr[2];
				IloNumExpr exprOD = SheraliLPModel.cplexModel.linearNumExpr();
				
				// Create the OD count constraint including
				// the error measurements above and below
				IloNumExpr exprODErrorAbove = SheraliLPModel.cplexModel.linearNumExpr();
				IloNumExpr exprODErrorBelow = SheraliLPModel.cplexModel.linearNumExpr();
				
				// For each route, if it starts on the given start point and
				// ends on the given end point, add it to the OD count constraint
				
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
					
					// obtain the route costs
					Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// obtain the route costs
					Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
					
					// Iterate through the route costs
					while (it.hasNext()) {
						Map.Entry route = (Map.Entry) it.next();
						
						// Iterate through the route nodes concomitantly
						Map.Entry entryNodes = (Map.Entry) itNodes.next();
						
						// Extract the nodes from the route
						String[] routeNodes;
						String routeValue;
						String routeKey;
						
						int ODPairsCounter=0;
						routeKey = Integer.toString( (Integer) route.getKey() );
						routeValue = (String) route.getValue();
						
						// Populate the array of Strings with the route nodes
						routeNodes = ((String)entryNodes.getValue()).split(",");
							
						// If the route being accessed starts in the origin node
						// and ends in the destination node under consideration,
						// add the route to the model
						if ( fromTo[0].equalsIgnoreCase(from) && fromTo[1].equalsIgnoreCase(to) )
						{
							addedODPairs.add(ODPairsCounter++, from+","+to);
							
							try {
								String nodesValue = (String)entryNodes.getValue();
								nodesValue = nodesValue.replace(" ", "").replace(",", "_");
								int varPosition = RouteToLPMapping.get(nodesValue)-1;
								
								((IloLinearNumExpr) exprOD).addTerm(
										1.0,
										SheraliLPModel.cplexVariables[ varPosition ]
								);
								
								// Sum the errors only once
								if (addedODPairs.size()<=1)
								{
									((IloLinearNumExpr) exprODErrorAbove).addTerm(
											1.0,
											SheraliLPModel.yODCountErrorAbove[ counterYODCountErrors ]
									);
									
									((IloLinearNumExpr) exprODErrorBelow).addTerm(
											1.0,
											SheraliLPModel.yODCountErrorBelow[ counterYODCountErrors ]
									);
								}
							} catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				
				//System.out.println("exprOD: " + exprOD);
				//System.out.println("value: " + value);
				
				// If there is no route between the (possible) OD pair
				// skip the entry
				if (exprOD.toString().trim().isEmpty())
					continue;
				
				exprArray[0] = exprOD;
				exprArray[1] = SheraliLPModel.cplexModel.diff(exprODErrorAbove, exprODErrorBelow);
				
				SheraliLPModel.cplexModel.addEq(
						SheraliLPModel.cplexModel.sum(exprArray),
						Double.valueOf(value)
				);
				
				counterYODCountErrors++;
			}
			
			// ---------------------------------------------------------------
			// ----- constraints on the existing OD measurements finished ----
			// ---------------------------------------------------------------
			
			// ---------------------------------------------------------------
			// -------------- create the LPToODMapping object ----------------
			// ---------------------------------------------------------------
			
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
				
				// obtain the route costs
				Iterator it = rc.getRouteCosts(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// obtain the route nodes
				Iterator itNodes = rc.getRouteNodes(fromTo[0], fromTo[1]).entrySet().iterator();
				
				// Iterate through the route costs
				while (it.hasNext()) {
					Map.Entry route = (Map.Entry) it.next();
					Map.Entry entryNodes = (Map.Entry) itNodes.next();
					
					// Extract the nodes from the route
					String[] routeNodes;
					String routeValue;
					String routeKey;
					
					int ODPairsCounter=0;
					routeKey = Integer.toString( (Integer) route.getKey() );
					routeValue = (String) route.getValue();
					
					// Populate the array of Strings with the route nodes
					routeNodes = ((String)entryNodes.getValue()).split(",");
					
					try {
						String nodesValue = (String)entryNodes.getValue();
						nodesValue = nodesValue.replace(" ", "").replace(",", "_");
						int varPosition = RouteToLPMapping.get(nodesValue)-1;
						
						// Populate the HashMap which maps the routes in the LP
						// to the structure containing the OD matrix
						LPToODMapping.put(varPosition, fromTo[0]+"->"+fromTo[1]);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			// ---------------------------------------------------------------
			// --------- creation of LPToODMapping object finished -----------
			// ---------------------------------------------------------------
		} catch (IloException e)
		{
			System.err.println(
					"Concert exception caught while creating cplexModel M3: " + e
			);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method used to solve the LP.
	 * 
	 * @return
	 */
	public static double solveSherali1994Problem()
	{
		// The final solution value
		double solution = -1;
		
		// ArrayList containing all the OD pairs that have
		// at least one path effectively used.
		ArrayList <Integer> usedOD = new ArrayList <Integer> ();
		
		SheraliLPModel.linkCounts = new HashMap();
		SheraliLPModel.routeCounts = new HashMap();
		
		try {
			// Write model to file
			SheraliLPModel.cplexModel.exportModel("Problem Sherali1994.lp");
			
			// Get the string representation of the LP Model
			SheraliLPModel.strLPModel = SheraliLPModel.cplexModel.getModel().toString();
			
			if ( SheraliLPModel.cplexModel.solve() )
			{
				solution = SheraliLPModel.cplexModel.getObjValue();
				double[] routesValue = SheraliLPModel.cplexModel.getValues(
						SheraliLPModel.cplexVariables
				);
				
				int ncols = SheraliLPModel.cplexModel.getNcols();
				
				//System.out.println("ncols: " + ncols);
				//System.out.println(
				//		"SheraliLPModel.cplexVariables.length: "+
				//		SheraliLPModel.cplexVariables.length
				//);
				
				// Obtain the used ODs and the link counts
				for (int j = 0; j < SheraliLPModel.cplexVariables.length; ++j)
				{
					SheraliLPModel.linkCounts.put(
							""+SheraliLPModel.cplexVariables[j],
							routesValue[j]
					);
					SheraliLPModel.routeCounts.put(
							""+SheraliLPModel.cplexVariables[j],
							routesValue[j]
					);
					
					if (routesValue[j]>0) usedOD.add(j);
					
					if (routesValue[j]>0)
					{
						System.out.println("Column (route): " +
								SheraliLPModel.cplexVariables[j] +
								" Value = " + routesValue[j]);
					}
					
				}
				
				// Create the OD matrix
				for (int key : usedOD)
				{
					String[] index;
					if (LPToODMapping.get(key) != null)
					{
						index = ((String) LPToODMapping.get(key)).split("->");
						
						SheraliLPModel.odMatrix.get(index[0]).get(index[1]).set(""+
								(
										Double.parseDouble(
												SheraliLPModel.odMatrix.get(index[0]).
														get(index[1]).get().toString()
										) + routesValue[key]
								)
						);
					}
				}
				
				double[] linkErrorsAbove = SheraliLPModel.cplexModel.getValues(
						SheraliLPModel.yLinkCountErrorAbove
				);
				double[] linkErrorsBelow = SheraliLPModel.cplexModel.getValues(
						SheraliLPModel.yLinkCountErrorBelow
				);
				
				// Obtain the calculated errors in link counts
				for (int j = 0; j < SheraliLPModel.yLinkCountErrorAbove.length; ++j)
				{
					String[] str = SheraliLPModel.yLinkCountErrorAbove[j].toString().split("to");
					str[0] = str[0].replace("Arc", "").replace(" ", "").replace("_", "");
					str[1] = str[1].replace("_Above", "");
					
					System.out.println("Column (link counts): "+j+" ("+
							str[0]+"->"+str[1]+"). "+
							" Value = - "+linkErrorsBelow[j]+" / + "+
							linkErrorsAbove[j]);
				}
				
				double[] odErrorsAbove = SheraliLPModel.cplexModel.getValues(
						SheraliLPModel.yODCountErrorAbove
				);
				double[] odErrorsBelow = SheraliLPModel.cplexModel.getValues(
						SheraliLPModel.yODCountErrorBelow
				);
				
				// Obtain the calculated errors in OD measurements
				for (int j = 0; j < SheraliLPModel.yODCountErrorAbove.length; ++j)
				{
					String[] str = SheraliLPModel.yODCountErrorAbove[j].toString().split("to");
					str[0] = str[0].replace("OD", "").replace(" ", "").replace("_", "");
					str[1] = str[1].replace("_Above", "");
					System.out.println("Column (OD counts): "+j+" ("+
							str[0]+"->"+str[1]+"). "+
							" Value = - "+odErrorsBelow[j]+" / + "+
							odErrorsAbove[j]);
				}
			} else {
				System.out.println(
						"Solution error status = "+
						SheraliLPModel.cplexModel.getStatus()
				);
			}
			
			SheraliLPModel.cplexModel.end();
		} catch (IloException e)
		{
			System.err.println(
					"Concert exception caught while trying to solve "+
					"Sherali's LP cplexModel: "+ e
			);
		}
		
		return solution;
	}
	
	/**
	 * Initialize the matrices odMatrixNodes and odMatrix,
	 * used to store the solution of the cplexModel
	 * 
	 * @param value
	 */
	public void setODMatrixNodes(HashMap value)
	{
		SheraliLPModel.odMatrixNodes = value;
		SheraliLPModel.odMatrix = new AutoMapValue <String, String>();
		
		Set s = SheraliLPModel.odMatrixNodes.entrySet();
		for (Object entry : s)
		{
			String index1 = (String) ((Map.Entry)entry).getValue();
			index1 = Integer.toString( Integer.parseInt(index1) );
			for (Object entry2 : s)
			{
				String index2 = (String) ((Map.Entry)entry2).getValue();
				index2 = Integer.toString( Integer.parseInt(index2) );
				
				SheraliLPModel.odMatrix.get(index1, index2).set("0");
			}
		}
	}
	
	/**
	 * Getter: get the solution's estimated OD Matrix
	 * 
	 * @return the solution's estimated OD Matrix
	 */
	public AutoMapValue getODMatrix()
	{
		return SheraliLPModel.odMatrix;
	}
	
	/**
	 * Getter: get the estimated link counts
	 * 
	 * @return the solution's estimated link counts
	 */
	public HashMap<String, Double> getValueLinkCounts()
	{
		HashMap<String, Double> hashResult = new HashMap<String, Double>();
		
		// Initialize the hashmap's values
		Iterator it = SheraliLPModel.linkCounts.entrySet().iterator();
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
		it = SheraliLPModel.linkCounts.entrySet().iterator();
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
	 * Getter: get the estimated counts for each route.
	 * Route index is in the format: "Route_3_(4_9_10_12_3)"
	 * 
	 * @return the estimated flows on all the routes in the model
	 */
	public HashMap<String, Double> getValueRouteCounts()
	{
		HashMap<String, Double> hashResult = new HashMap<String, Double>();
		
		// Initialize the hashmap's values
		Iterator it = SheraliLPModel.routeCounts.entrySet().iterator();
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
		it = SheraliLPModel.routeCounts.entrySet().iterator();
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
	 * @return the linkCountErrorsMultiplier
	 */
	public static double getLinkCountErrorsMultiplier() {
		return linkCountErrorsMultiplier;
	}

	/**
	 * @param linkCountErrorsMultiplier the linkCountErrorsMultiplier to set
	 */
	public static void setLinkCountErrorsMultiplier(double linkCountErrorsMultiplier) {
		SheraliLPModel.linkCountErrorsMultiplier = linkCountErrorsMultiplier;
	}

	/**
	 * @return the odCountErrorsMultiplier
	 */
	public static double getOdCountErrorsMultiplier() {
		return odCountErrorsMultiplier;
	}

	/**
	 * @param odCountErrorsMultiplier the odCountErrorsMultiplier to set
	 */
	public static void setOdCountErrorsMultiplier(double odCountErrorsMultiplier) {
		SheraliLPModel.odCountErrorsMultiplier = odCountErrorsMultiplier;
	}
	
	public static String getLPModel() {
		return SheraliLPModel.strLPModel;
	}
}
