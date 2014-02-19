/**
 * Method described in Sherali et al(1994)
 */
package v7;

import odEstimation.AutoMapValue;
import odEstimation.LinkCapacities;
import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODMatrix;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import parser.GraphParser;
import postprocessing.SolutionAnalyser;
import postprocessing.SolutionPoolSherali;
import preprocessing.Preprocessing;
import preprocessing.Preprocessing.PreprocessingMethod;
import v7.FLIPSOD.BPRFunctionName;

import cplexModels.SheraliLPModel;
import flowCostMapping.LinkCostFlowRelationship;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Iacer Calixto
 *
 */
public class Sherali
{
	/**
	 * The vertices of the graph
	 */
	HashMap <String, String> strVertices = null;
	
	/**
	 * The LP Model
	 */
	SheraliLPModel sheraliLpModel;
	
	/**
	 * The observed (reference) link counts
	 */
	private LinkCounts observedLinkCountsSherali = null;
	
	/**
	 * The estimated link counts
	 */
	private LinkCounts estimatedLinkCountsSherali = null;
	
	/**
	 * The link counts previously estimated in the past iteration of the method
	 */
	private LinkCounts previousEstimatedLinkCountsSherali = null;
	
	/**
	 * The estimated link costs
	 */
	private LinkCosts linkCostsSherali = null;
	
	/**
	 * The estimated route costs
	 */
	private RouteCosts routeCostsSherali = null;
	
	/**
	 * The OD estimates
	 */
	private ODCounts odCounts = null;
	
	/**
	 * The link capacities
	 */
	private LinkCapacities linkCapacities = null;
	
	/**
	 * The valid OD pairs
	 */
	private ODPairs odPairs = null;
	
	/**
	 * The graph
	 */
	private DefaultDirectedWeightedGraph <String, DefaultWeightedEdge> problem = null;
	
	/**
	 * The reference (target) OD matrix
	 */
	private ODMatrix referenceODMatrix = null;
	
	/**
	 * Solution pool that contains all generated solutions
	 * that are of some interest
	 */
	private SolutionPoolSherali solutionPoolSherali = null;
	
	/**
	 * The smoothing multiplier for routes in which
	 * flows oscillate strongly
	 */
	private double smoothingMultiplier;
	
	/**
	 * The number of iterations performed
	 */
	private int iterationsCounter;
	
	/**
	 * Class constructor
	 */
	public Sherali() {
		estimatedLinkCountsSherali = new LinkCounts();
		previousEstimatedLinkCountsSherali = new LinkCounts();
		smoothingMultiplier = 0.1;
		
		//createMatrix();
		//if (true) return;
		
		this.strVertices = new HashMap <String, String>();
	}
	
	/**
	 * Opens the file passed as a parameter and loads
	 * its contents into the appropriate objects.
	 * @param file the file to open
	 * @return whether to use PETGyn to cycle or not
	 */
	public boolean openFile(File file)
	{
		// Create the parser object and feed the vertices
		GraphParser parser = new GraphParser();
		parser.setStrVertices(strVertices);
		parser.parse(file);
		
		// Whether to use or not PETGyn to cycle
		boolean usePETGyn = parser.isBoolUsePet();
		
		// Get the model objects from the parser
		strVertices = parser.getStrVertices();
		observedLinkCountsSherali = parser.getLinkCounts();
		
		// Use the observed link counts as the estimated link counts
		// in order to calculate the arc costs for the first iteration
		estimatedLinkCountsSherali = observedLinkCountsSherali;
		
		// Some other objects extracted by the parser
		odCounts = parser.getOdCounts();
		routeCostsSherali = parser.getRouteCosts();
		linkCostsSherali = parser.getLinkCosts();
		odPairs = parser.getOdPairs();
		problem = parser.getG();
		
		String fileLinkCapacitiesName = null;
		String fileODMatrixName = null;
		File fileLinkCapacities = null;
		File fileODMatrix = null;
		
		referenceODMatrix = new ODMatrix();
		
		if (!usePETGyn)
		{
			// Try to reach the files with the link capacities and the reference
			// (target) OD matrix and update the corresponding objects.
			fileLinkCapacitiesName = file.getParent() + file.separator +
					file.getName().substring(0, file.getName().length()-4) +
					"_LinkCapacities" +
					file.getName().substring(file.getName().length()-4, file.getName().length());
			
			fileODMatrixName = file.getParent() + file.separator +
					file.getName().substring(0, file.getName().length()-4) +
					"_ODMatrix" +
					file.getName().substring(file.getName().length()-4, file.getName().length());
			
			fileLinkCapacities = new File(fileLinkCapacitiesName);
			fileODMatrix = new File(fileODMatrixName);
			
			try {
				// If the file containing the link capacities exists, populate its object
				if (fileLinkCapacities.exists())
					linkCapacities = parser.parseLinkCapacities(fileLinkCapacities);
				
				// If the file containing the OD matrix exists, populate its object
				if (fileODMatrix.exists())
					referenceODMatrix = parser.parseODMatrix(fileODMatrix);
				else
					referenceODMatrix = new ODMatrix();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return usePETGyn;
	}
	
	/**
	 * Iterate until OD matrix convergence is met.
	 */
	public void iterateSheraliBilevelOptimization()
	{
		try {
			// The maximum difference allowed between two different OD matrices
			// Controls if it should continue iterating or stop
			double epslon = 0.0005;
			
			// The previous and actual OD matrices. Their differences
			// shall be minimized throughout the estimation process
			AutoMapValue previousODMatrix = null;
			AutoMapValue actualODMatrix = null;
			
			// The solution analyser object
			SolutionAnalyser sa = new SolutionAnalyser();
			
			iterationsCounter=0;
			
			// Make a first estimation and generate an OD matrix
			cleanSheraliProblemData();
			doCalculateArcCostsSherali();
			doSheraliPreprocessing();
			doSheraliSimpleEstimation();
			updateLinkFlowsSherali();
			
			iterationsCounter++;
			
			previousODMatrix = (AutoMapValue) solutionPoolSherali.getArrSolutionODMatrix().get(0);
			
			// Iterate until the difference between the previous and
			// current OD matrices estimated are sufficiently small
			while ( actualODMatrix==null ||
					sa.compareODMatricesRMSE(previousODMatrix, actualODMatrix, odPairs) > epslon )
			{
				// Clean up the estimations
				cleanSheraliProblemData();
				
				// Calculate the arc costs
				// If using the BPR function, one needs:
				// - arc flows, arc capacities and arc costs (without traffic)
				// If using PET-Gyn to do the calculations, one needs:
				// - an OD matrix.
				System.out.println("Beginning arc costs calculation...");
				doCalculateArcCostsSherali();
				System.out.println("Arc costs calculation concluded.");
				
				// Preprocess the problem
				// Use the arc costs to generate route costs
				doSheraliPreprocessing();
				
				// Do the estimation one step each iteration
				System.out.println("Beginning estimation...");
				doSheraliSimpleEstimation();
				System.out.println("Estimation concluded.");
				
				// Update the link flows
				updateLinkFlowsSherali();
				
				System.out.println("Iteration "+ iterationsCounter++ +" concluded.");
				
				// Update the previous and actual OD matrices
				if (actualODMatrix!=null) previousODMatrix = actualODMatrix;
				actualODMatrix = (AutoMapValue) solutionPoolSherali.getArrSolutionODMatrix().get(0);
				
				System.out.println("Difference between matrices: "+
						sa.compareODMatricesRMSE(previousODMatrix, actualODMatrix, odPairs));
			}
			
			System.out.println("Iterations concluded.");
			
			analyseSheraliSolution();
			showSheraliProblemData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Iterate until OD matrix convergence is met.
	 */
	public void iterateSheraliUsingPETGyn()
	{
		try {
			// The maximum difference allowed between two different OD matrices
			// Controls if it should continue iterating or stop
			double epslon = 0.0005;
			
			// The previous and actual OD matrices. Their differences
			// shall be minimized throughout the estimation process
			AutoMapValue previousODMatrix = null;
			AutoMapValue actualODMatrix = null;
			
			// The solution analyser object
			SolutionAnalyser sa = new SolutionAnalyser();
			
			iterationsCounter=0;
			
			// Make a first estimation and generate an OD matrix
			cleanSheraliProblemData();
			doSheraliPreprocessing();
			doSheraliSimpleEstimation();
			updateLinkFlowsSherali();
			doSheraliPETGynUpdateProblem();
			iterationsCounter++;
			
			previousODMatrix = (AutoMapValue) solutionPoolSherali.getArrSolutionODMatrix().get(0);
			
			// Iterate until the difference between the previous and
			// current OD matrices estimated are sufficiently small
			while ( actualODMatrix==null ||
					sa.compareODMatricesRMSE(previousODMatrix, actualODMatrix, odPairs) > epslon )
			{
				// Clean up the estimations
				cleanSheraliProblemData();
				
				// Preprocess the problem
				// Use the arc costs to generate route costs
				doSheraliPreprocessing();
				
				// Do the estimation one step each iteration
				System.out.println("Beginning estimation...");
				doSheraliSimpleEstimation();
				System.out.println("Estimation concluded.");
				
				// Update the link flows
				updateLinkFlowsSherali();
				
				System.out.println("Iteration "+ iterationsCounter++ +" concluded.");
				
				// Update the previous and actual OD matrices
				if (actualODMatrix!=null) previousODMatrix = actualODMatrix;
				actualODMatrix = (AutoMapValue) solutionPoolSherali.getArrSolutionODMatrix().get(0);
				
				System.out.println("Difference between matrices: "+
						sa.compareODMatricesRMSE(previousODMatrix, actualODMatrix, odPairs));
				
				// Update the arc costs with the new ones provided by PETGyn
				doSheraliPETGynUpdateProblem();
			}
			
			System.out.println("Iterations concluded.");
			
			analyseSheraliSolution();
			showSheraliProblemData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clean up the problem data
	 */
	private void cleanSheraliProblemData() {
		linkCostsSherali = new LinkCosts();
		routeCostsSherali = new RouteCosts();
	}
	
	/**
	 * Uses the objects referenceLinkCountsSherali and linkCapacities
	 * in order to calculate linkCostsSherali
	 */
	private void doCalculateArcCostsSherali() {
		LinkCostFlowRelationship linkCost = new LinkCostFlowRelationship();
		
		// Create the link costs objects (for FLPSOD and Sherali)
		LinkCosts newLinkCostsSherali = null;
		
		System.out.println("linkCosts Sherali (before): "+linkCostsSherali);
		System.out.println("estimatedLinkCounts: "+estimatedLinkCountsSherali);
		
		// Calculate link costs with BPR's cost function
		try {
			newLinkCostsSherali = linkCost.calculateCostFlowRelationship(
					problem,
					linkCapacities,
					estimatedLinkCountsSherali,
					previousEstimatedLinkCountsSherali,
					smoothingMultiplier,
					BPRFunctionName.BPR
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Update the classes' variables
		linkCostsSherali = newLinkCostsSherali;
		
		System.out.println("linkCosts Sherali (after): "+linkCostsSherali);
	}
	
	/**
	 * Show the problem data
	 */
	private void showSheraliProblemData() {
		// Show the problem data
		System.out.println(problem.toString());
		System.out.println("strVertices: "+strVertices);
		System.out.println("observedLinkCountsSherali: "+observedLinkCountsSherali);
		System.out.println(odCounts);
		System.out.println("routeCostsSherali: "+routeCostsSherali);
		System.out.println("linkCostsSherali: "+linkCostsSherali);
		System.out.println(linkCapacities);
		System.out.println(odPairs);
		System.out.println(referenceODMatrix);
	}
	
	/**
	 * Uses the objects problem and odPairs in order to
	 * initialize the objects sheraliLpModel and routeCostsSherali
	 */
	private void doSheraliPreprocessing() {
		// Check whether the OD pairs were given.
		// If not, add all the possible pair of vertices as OD pairs
		if (odPairs.getCount()<=0)
			for (String v1 : problem.vertexSet())
				for (String v2 : problem.vertexSet())
					if ((String)v1!=(String)v2) odPairs.add((String)v1, (String)v2);
		
		// Set the preprocessing method to use
		Preprocessing prep = new Preprocessing(
				PreprocessingMethod.PreprocessingKPathsPerODPair
		);
		
		// Create the linear programming object and
		// choose the preprocessing routine
		
		// Sherali, 1994 LP
		sheraliLpModel = new SheraliLPModel();
		
		// Set the OD Matrix' nodes
		sheraliLpModel.setODMatrixNodes(strVertices);
		
		// Pass the (eventual) parameters to the preprocessing class
		String[] names = new String[2];
		Object[] values = new Object[2];
		Class<?>[] types = new Class[2];
		
		names[0] = "setUseConstant";
		values[0] = true;
		types[0] = Boolean.class;
		
		names[1] = "setK";
		values[1] = 6;
		types[1] = Integer.class;
		
		try {
			prep.setPreprocessingArguments(names, values, types);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Use a temporary object to hold Sherali's link counts
		LinkCounts temporaryLinkCountsSherali = new LinkCounts();
		Iterator itEstimatedLinkCountsSherali =
			estimatedLinkCountsSherali.getAllCounts().entrySet().iterator();
		while (itEstimatedLinkCountsSherali.hasNext())
		{
			Map.Entry entry = (Map.Entry) itEstimatedLinkCountsSherali.next();
			
			String separator;
			String entryValue="";
			
			if (((String)entry.getKey()).indexOf(",")>-1)
			{
				separator = ",";
				
				//System.out.println("entry.getKey(): "+entry.getKey());
				//System.out.println("entry.getValue(): "+entry.getValue());
				
				Class<?> c = entry.getValue().getClass();
				if (c.toString().indexOf("String") != -1) // if String
					entryValue = (String)entry.getValue();
				else if (c.toString().indexOf("Double") != -1) // if Double
					entryValue = Double.toString((Double)entry.getValue());
			} else {
				separator = "->";
				entryValue = Double.toString((Double)entry.getValue());
			}
			
			String[] fromTo = ((String)entry.getKey()).split(separator);
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			temporaryLinkCountsSherali.setCount(fromTo[0], fromTo[1], entryValue);
		}
		
		//System.out.println("temporaryLinkCountsFLPSOD: "+temporaryLinkCountsFLPSOD);
		
		// Do the preprocessing and obtain the route costs
		routeCostsSherali = prep.doPreprocessing(
				problem,
				temporaryLinkCountsSherali,
				odCounts,
				linkCostsSherali,
				odPairs
		);
		
		// Order the routes
		routeCostsSherali.order();
	}
	
	/**
	 * Does an one-step estimation.
	 */
	private void doSheraliSimpleEstimation()
	{
		double dblSolutionSherali;
		String strModelSherali;
		AutoMapValue mapODMatrixSherali;
		HashMap<String, Double> hashRouteCountsSherali, hashLinkCountsSherali;
		// Do not use the errors read from the files, instead use
		// the values provided to the class programmatically
		// Uncomment the four lines below to do so
		//((IFuzzyLPModel)lpModel).setUseCodedErrors(true);
		//((IFuzzyLPModel)lpModel).setUseGradientErrors(false);
		//((IFuzzyLPModel)lpModel).setA(0.1f);
		//((IFuzzyLPModel)lpModel).setB(0.2f);
		
		// Get the value of the first solution to M8
		sheraliLpModel.createSherali1994Problem(
				problem,
				observedLinkCountsSherali,
				odCounts,
				routeCostsSherali,
				linkCostsSherali,
				odPairs//,
				//linkCapacities
		);
		
		// Re-Initialize the solution pool object
		solutionPoolSherali = new SolutionPoolSherali();
		
		// Obtain the first solution to M8 based on
		// the original M2
		dblSolutionSherali = sheraliLpModel.solveSherali1994Problem();
		mapODMatrixSherali = sheraliLpModel.getODMatrix();
		strModelSherali = sheraliLpModel.getLPModel();
		hashRouteCountsSherali = sheraliLpModel.getValueRouteCounts();
		hashLinkCountsSherali = sheraliLpModel.getValueLinkCounts();
		
		// If it is a valid solution, save it
		solutionPoolSherali.addSolution(
				dblSolutionSherali,
				mapODMatrixSherali,
				hashRouteCountsSherali,
				hashLinkCountsSherali,
				strModelSherali
		);
	}
	
	/**
	 * Analyse the solution(s) generated by Sherali.
	 * Write its results either in the screen or in a file.
	 */
	public void analyseSheraliSolution() {
		// ------------------------------------------------------------------
		// Compare Sherali's solutions and the observed solution
		// ------------------------------------------------------------------
		
		SolutionAnalyser sa = new SolutionAnalyser();
		ArrayList arrSolutionsSheraliODMatrix = solutionPoolSherali.getArrSolutionODMatrix();
		double rmseOD, maeOD;
		
		if (!referenceODMatrix.getODMatrix().isEmpty())
		{
			// If Sherali found at least one feasible solution, show its results
			if (!arrSolutionsSheraliODMatrix.isEmpty())
			{
				// Get the first solution added to the model
				AutoMapValue odMatrixSherali = (AutoMapValue) arrSolutionsSheraliODMatrix.get(0);
				
				System.out.println("Comparing Sherali's OD matrix to the observed OD matrix:");
				rmseOD = sa.compareODMatricesRMSE(odMatrixSherali, referenceODMatrix);
				System.out.println("%RMSE OD: " + rmseOD);
				
				maeOD = sa.compareODMatricesMAE(odMatrixSherali, referenceODMatrix);
				System.out.println("%MAE OD: " + maeOD);
			} else {
				System.out.println("Sherali couldn't find a feasible solution.");
			}
		} else {
			System.out.println("No reference OD matrix loaded. "+
					"No comparison between OD matrices will be done.");
		}
		
		System.out.println();
		
		ArrayList arrSolutionsSheraliLinkFlows = solutionPoolSherali.getArrSolutionLinkCounts();
		HashMap<String, Double> SheraliLinkCounts = null;
		
		if (!arrSolutionsSheraliLinkFlows.isEmpty())
			SheraliLinkCounts = (HashMap<String, Double>) arrSolutionsSheraliLinkFlows.get(0);
		
		// For each link that has counts, calculate the sum of its flows
		// estimated by Sherali
		Iterator itReferenceLinkCountsSherali =
			observedLinkCountsSherali.getAllCounts().entrySet().iterator();
		while (itReferenceLinkCountsSherali.hasNext())
		{
			Map.Entry entryReference = (Map.Entry) itReferenceLinkCountsSherali.next();
			String linkName = (String) entryReference.getKey();
			
			double estimatedLinkValueSherali = 0;
			
			String[] fromTo = linkName.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();
			
			//System.out.println("Sherali has "+sheraliLinkCounts.entrySet().size()+" link counts.");
			// Calculate the sum to Sherali
			if (!arrSolutionsSheraliLinkFlows.isEmpty())
			{
				Iterator itEstimatedLinkCounts = SheraliLinkCounts.entrySet().iterator();
				while (itEstimatedLinkCounts.hasNext())
				{
					Map.Entry entryEstimated = (Map.Entry) itEstimatedLinkCounts.next();
					String strKey = (String) entryEstimated.getKey();
					Double dblValue = (Double) entryEstimated.getValue();
					
					String[] strLink = strKey.split("->");
					
					if ( Integer.parseInt(strLink[0])==Integer.parseInt(fromTo[0]) &&
							Integer.parseInt(strLink[1])==Integer.parseInt(fromTo[1]) )
						estimatedLinkValueSherali += dblValue;
				}
			}
			if (!arrSolutionsSheraliLinkFlows.isEmpty())
				SheraliLinkCounts.put(fromTo[0]+"->"+fromTo[1], estimatedLinkValueSherali);
		}
		
		double rmseArcCounts, maeArcCounts;
		
		// If Sherali found at least one feasible solution, show its results
		if (!arrSolutionsSheraliODMatrix.isEmpty())
		{
			System.out.println("Comparing Sherali's arc counts to the observed arc counts:");		
			rmseArcCounts = sa.compareArcFlowsRMSE(
					SheraliLinkCounts,
					observedLinkCountsSherali.getAllCounts()
			);
			System.out.println("%RMSE ARC: " + rmseArcCounts);
			
			maeArcCounts = sa.compareArcFlowsMAE(
					SheraliLinkCounts,
					observedLinkCountsSherali.getAllCounts()
			);
			System.out.println("%MAE ARC: " + maeArcCounts);
		} else {
			System.out.println("Sherali couldn't find a feasible solution.");
		}
		
		System.out.println();
	}
	
	/**
	 * Update the link flows with the last estimation values.
	 * If there is a big variation on the flows in links with
	 * missing estimations, use a smoothing factor to calculate
	 * its new values.
	 * 
	 * @param solutionPool
	 */
	//private void updateLinkFlowsSherali(SolutionPoolSherali solutionPool, boolean smooth)
	private void updateLinkFlowsSherali()
	throws Exception
	{
		// Only don't smooth the flows in the first iteration
		boolean smooth = (iterationsCounter!=1) ? true : false;
		
		ArrayList arrSolutionsSheraliLinkFlows = solutionPoolSherali.getArrSolutionLinkCounts();
		
		if (arrSolutionsSheraliLinkFlows.isEmpty())
			throw new Exception("No solutions available in the solution pool.");
		
		// Always use the first solution found (the one with highest lambda)
		HashMap<String,Double> sheraliNewLinkCounts =
			(HashMap<String, Double>) arrSolutionsSheraliLinkFlows.get(0);
		
		//System.out.println("previousEstimatedLinkCountsFLPSOD (before): "+
		//		previousEstimatedLinkCountsFLPSOD);
		System.out.println("estimatedLinkCountsSherali: "+
				estimatedLinkCountsSherali);
		System.out.println("sheraliNewLinkCounts (before update): "+
				sheraliNewLinkCounts);
		System.out.println("observedLinkCountsSherali: "+
				observedLinkCountsSherali);
		
		// Iterate through the link counts and their estimations
		Iterator itLCE = sheraliNewLinkCounts.entrySet().iterator();
		while (itLCE.hasNext()) {
			Map.Entry entry = (Map.Entry) itLCE.next();
			
			String strArc = (String) entry.getKey();
			double newFlow = 0;
			double actualFlow;
			double previousFlow = 0;
			String separator;
			
			// Gets the string separator to the link flow
			separator =  (strArc.indexOf(",") > -1) ? "," : "->";
			
			// Gets the value of the actual flow
			Class c = entry.getValue().getClass();
			if (c.toString().indexOf("String") != -1) // is String
				actualFlow = Double.parseDouble( (String)entry.getValue() );
			else
				actualFlow = (Double)entry.getValue();
			
			String from = strArc.split(separator)[0];
			String to = strArc.split(separator)[1];
			from = from.replace("(", "").trim();
			to = to.replace(")", "").trim();
			
			newFlow = actualFlow;
			System.out.println("(from,to): ("+from+","+to+")");
			
			if (previousEstimatedLinkCountsSherali.getAllCounts().containsKey("("+from+","+to+")"))
				previousFlow = Double.parseDouble(
						(String) previousEstimatedLinkCountsSherali.getAllCounts().get("("+from+","+to+")")
				);
			
			// if the link has no observation of its counts, smooth
			// its calculations by the smoothingMultiplier parameter
			if ( !observedLinkCountsSherali.hasCounts(from, to) )
			{
				// If we're supposed to smooth the solution flow
				if (smooth)
					newFlow = previousFlow - ((previousFlow - actualFlow)*smoothingMultiplier);
				//if ((from.equalsIgnoreCase("4") && to.equalsIgnoreCase("2")) ||
				//		(from.equalsIgnoreCase("4") && to.equalsIgnoreCase("5")) ||
				//		(from.equalsIgnoreCase("5") && to.equalsIgnoreCase("2")))
				//{
					System.out.print("Unnobserved link ("+from+","+to+")");
					System.out.print(", previous: "+previousFlow);
					System.out.print(", new: "+actualFlow);
					System.out.print(", actual: "+newFlow);
					System.out.println();
				//}
			}
			
			// if the link does have observation of its counts, smooth
			// its calculations by the smoothingMultiplier parameter
			if ( observedLinkCountsSherali.hasCounts(from, to) )
			{
				// If we're supposed to smooth the solution flow
				//if (smooth)
				//{
					System.out.print("Observed link ("+from+","+to+")");
					System.out.print(", previous flow: "+previousFlow);
					System.out.print(", actual flow: "+actualFlow);
					System.out.print(", new (used) flow: "+newFlow);
					System.out.println();
				//}
			}
			
			String errors = estimatedLinkCountsSherali.getErrorsBetween(
					Integer.parseInt(from),
					Integer.parseInt(to)
			);
			
			String errorBelow = errors.split(" - ")[0];
			String errorAbove = errors.split(" - ")[1];
			
			if (errorBelow.isEmpty()) errorBelow = "1";
			if (errorAbove.isEmpty()) errorAbove = "1";
			
			// Update the previous and actual flows
			previousEstimatedLinkCountsSherali.setCount(
					from,
					to,
					Double.toString(newFlow),
					1, // negative error of 100%
					1  // positive error of 100%
			);
			
			estimatedLinkCountsSherali.setCount(
					from,
					to,
					Double.toString(newFlow),
					1, // negative error of 100%
					1  // positive error of 100%
			);
		}
		
		//System.out.println("Updated link flows:");
		//System.out.println("previousEstimatedLinkCountsSherali (after update): "+
		//		previousEstimatedLinkCountsSherali);
		System.out.println("estimatedLinkCountsSherali(after update): "+
				this.estimatedLinkCountsSherali);
	}
	
	/**
	 * Use the values estimated in the last iteration
	 * and provide them to PETGyn, in order to obtain
	 * updated arc costs.
	 */
	private void doSheraliPETGynUpdateProblem()
	{
		// TODO
		System.out.println("BEEN HERE");
		System.exit(1);
	}
	
	/**
	 * A dummy method to generate some data simulating PET-Gyn's
	 * project 2
	 */
	public void createMatrix()
	{
		int[] o = new int[25];
		o[0] = 322;
		o[1] = 326;
		o[2] = 336;
		o[3] = 464;
		o[4] = 465;
		o[5] = 470;
		o[6] = 472;
		o[7] = 474;
		o[8] = 494;
		o[9] = 586;
		//o[10] = 588;
		o[10] = 590;
		//o[11] = 592;
		o[11] = 605;
		o[12] = 606;
		o[13] = 610;
		o[14] = 611;
		//o[16] = 619;
		o[15] = 622;
		o[16] = 2054;
		o[17] = 2056;
		o[18] = 2058;
		o[19] = 2060;
		o[20] = 2062;
		o[21] = 2064;
		o[22] = 2066;
		o[23] = 2068;
		o[24] = 2070;
		
		System.out.println();
		for (int i=0; i<o.length; i++)
		{
			for (int j=0; j<o.length; j++)
			{
				if (i==j || o[i]==-1 || o[j]==-1) continue;
				System.out.print("("+o[i]+","+o[j]+") ");
			}
		}
	}
}
