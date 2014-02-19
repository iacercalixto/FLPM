/**
 * 7th version of the FLIPSOD method
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
import postprocessing.SolutionPool;
import preprocessing.Preprocessing;
import preprocessing.Preprocessing.PreprocessingMethod;

import cplexModels.FuzzyLPModel_Original;
import cplexModels.IFuzzyLPModel;
import extractor.Cycle;
import extractor.FixSolution;
import flowCostMapping.LinkCostFlowRelationship;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.text.Position;

/**
 * Class that describes the FLIPSOD method
 * 
 * @author Iacer Calixto
 */
public class FLIPSOD_Maximize {
	/**
	 * Vertices of the graph
	 */
	HashMap<String, String> strVertices = null;

	/**
	 * The LP model
	 */
	IFuzzyLPModel lpModel = null;

	/**
	 * The observed (reference) link counts
	 */
	private LinkCounts observedLinkCountsFLPSOD = null;

	/**
	 * The estimated link counts
	 */
	private LinkCounts estimatedLinkCountsFLPSOD = null;

	/**
	 * The link counts previously estimated in the past iteration of the method
	 */
	private LinkCounts previousEstimatedLinkCountsFLPSOD = null;

	/**
	 * The estimated link costs
	 */
	private LinkCosts linkCostsFLPSOD = null;

	/**
	 * The estimated route costs
	 */
	private RouteCosts routeCostsFLPSOD = null;

	/**
	 * The OD estimates
	 */
	private ODCounts odCounts = null;

	/**
	 * The link capacities
	 */
	private LinkCapacities linkCapacities = null;

	/**
	 * The valid OD pairs in the model
	 */
	private ODPairs odPairs = null;

	/**
	 * The graph
	 */
	private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> problem = null;

	/**
	 * The reference (target) OD matrix
	 */
	private ODMatrix referenceODMatrix = null;

	/**
	 * Solution pool that contains all the generated solutions that are of some
	 * interest
	 */
	private SolutionPool solutionPool = null;

	/**
	 * The smoothing multiplier for routes in which flows oscillate strongly
	 */
	private double smoothingMultiplier;

	/**
	 * The number of iterations performed
	 */
	private int iterationsCounter;

	/**
	 * Whether to calculate user-equilibrium or system optimal
	 */
	private boolean userEquilibrium;

	/**
	 * Maximum number of routes per OD pair to use
	 */
	private int numberOfRoutesPerODPair;

	/**
	 * Whether to use mixed integer programming or continuous optimization
	 */
	private boolean mixedIntegerProgramming;

	/**
	 * Whether to use errors coded in the input file or to ignore those errors
	 * and use predefined error values
	 */
	private boolean useCodedErrors;

	/**
	 * Whether to use prefined errors with values of a gradient, linear
	 * distribution or a fixed distribution.
	 */
	private boolean useGradientErrors;

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
	 * The maximum difference between two lambdas
	 * for them to still be considered the same
	 */
	private static double lambdaMaximumError = 0.01;
	
	/**
	 * The destination-only error measurements
	 */
	private static double odMatrixMaximumError = 0.0005;
	
	/**
	 * The maximum difference between two sets of
	 * flows on arcs for them to still be considered the same
	 */
	private static double arcFlowsMaximumError = 25;
	
	/**
	 * Whether the last solution found is a feasible one. Used in the cycling
	 * routine.
	 */
	private static boolean boolIsSolutionUnfeasible = false;

	/**
	 * An enumeration containing all the possible FLIPSOD methods to run.
	 * 
	 * @author iacer
	 */
	public enum MethodClassName {
		FuzzyLPModel_Original,
		FuzzyLPModel_Original_Maximize,
		FuzzyLPModel_AllRoutesInObjectiveFunction,
		FuzzyLPModel_UnconstrainedRoutesInObjectiveFunction,
		FuzzyLPModel_CrispConstraintsInUnobservedRoutes
	}

	/**
	 * The class name of the implemented method to use
	 */
	private MethodClassName methodClassName;

	/**
	 * The class of the method to use
	 */
	private Class methodClass;
	
	/**
	 * The model to use to do the one-step estimation
	 */
	public enum SimpleEstimationModelName {
		M3, M8
	}
	
	/**
	 * The class name of the implemented method to use
	 */
	private SimpleEstimationModelName simpleEstimationModel;
	
	/**
	 * The arc cost updating strategy to adopt.
	 * We can use PETGyn to update the arc costs or
	 * the BPR function.
	 */
	public enum ArcCostUpdatingStrategyName {
		PET, BPR
	}
	
	/**
	 * The class name of the implemented method to use
	 */
	private ArcCostUpdatingStrategyName arcCostUpdatingStrategy;
	
	/**
	 * The input file
	 */
	private File inputFile = null;
	
	/**
	 * Whether to use PETGyn to cycle or not
	 */
	private boolean boolUsePETGyn = false;
	
	/**
	 * An enumeration containing all the possible set of constants
	 * to use with the BPR function to calculate arc costs from arc flows.
	 * 
	 * @author iacer
	 */
	//public enum BPRFunctionName {
	//	BPR,
	//	UBPR,
	//	BBPR
	//}
	
	/**
	 * The name of the BPR function to use, if so
	 */
	private FLIPSOD.BPRFunctionName bprFunctionName;
	
	/**
	 * The project id in PETGyn, when applicable
	 */
	private int idProjeto = 2;
	
	/**
	 * The name of the last OD matrix generated by FLIPSOD
	 */
	private String lastEstimatedODMatrixFileName = null;
	
	/**
	 * The name of the last route generated by FLIPSOD
	 */
	private String lastEstimatedRouteFileName = null;
	
	/**
	 * Class constructor
	 */
	public FLIPSOD_Maximize() {
		init();
	}

	/**
	 * Performs the initialization of some objects
	 */
	public void init() {

		// this.createMatrix();
		// if (true) return;

		estimatedLinkCountsFLPSOD = new LinkCounts();
		previousEstimatedLinkCountsFLPSOD = new LinkCounts();
		this.strVertices = new HashMap<String, String>();

		// Default values
		setMethodClassName(MethodClassName.FuzzyLPModel_Original_Maximize);
		setSimpleEstimationModel(simpleEstimationModel.M8);
		setArcCostUpdatingStrategy(ArcCostUpdatingStrategyName.BPR);
		setBPRFunctionName(FLIPSOD.BPRFunctionName.BPR);
		boolUsePETGyn = false;
		idProjeto = 2;
		lambdaMaximumError = 0.01;
		odMatrixMaximumError = 0.0005;
		smoothingMultiplier = 0.1;
		userEquilibrium = true;
		numberOfRoutesPerODPair = 10;
		mixedIntegerProgramming = false;
		a = 0.1f;
		b = 0.2f;
		d = 0.2f;
		e = 0.2f;
	}

	/**
	 * Opens the file passed as a parameter and loads its contents into the
	 * appropriate objects. If there are more files to be read, it is done
	 * according to file name conventions.
	 * 
	 * @param file the file to open
	 * @return whether to use PETGyn to cycle or not
	 */
	public boolean loadFile() {
		File file = inputFile;
		
		// Create the parser object and feed the vertices
		GraphParser parser = new GraphParser();
		parser.setStrVertices(strVertices);

		parser.parse(file);

		// Whether to use or not PETGyn to cycle
		boolUsePETGyn = parser.isBoolUsePet();
		
		// If the input file relates to a PETGyn project,
		// redo the its parsing accordingly
		if (boolUsePETGyn) {
			// Do not use the original (partial) graph,
			// but the complete, combined input file
			File completeInputFile = parser.extractDataFromPETGyn();
			parser.parse(completeInputFile);
		}
		
		// Get the model objects from the parser
		strVertices = parser.getStrVertices();
		observedLinkCountsFLPSOD = parser.getLinkCounts();

		// Use the observed link counts as the estimated link counts
		// in order to calculate the arc costs in the first iteration
		// (clone the object)
		estimatedLinkCountsFLPSOD = observedLinkCountsFLPSOD.copyLinkCounts();

		// Some other objects extracted by the parser
		odCounts = parser.getOdCounts();
		routeCostsFLPSOD = parser.getRouteCosts();
		linkCostsFLPSOD = parser.getLinkCosts();
		odPairs = parser.getOdPairs();
		problem = parser.getG();

		String fileLinkCapacitiesName = null;
		String fileODMatrixName = null;
		File fileLinkCapacities = null;
		File fileODMatrix = null;

		referenceODMatrix = new ODMatrix();

		// Try to reach the files with the link capacities and the reference
		// (target) OD matrix and update the corresponding objects.
		fileLinkCapacitiesName = file.getParent()
				+ file.separator
				+ file.getName().substring(0, file.getName().length() - 4)
				+ "_LinkCapacities"
				+ file.getName().substring(file.getName().length() - 4,
						file.getName().length());

		fileODMatrixName = file.getParent()
				+ file.separator
				+ file.getName().substring(0, file.getName().length() - 4)
				+ "_ODMatrix"
				+ file.getName().substring(file.getName().length() - 4,
						file.getName().length());
		
		fileLinkCapacities = new File(fileLinkCapacitiesName);
		fileODMatrix = new File(fileODMatrixName);
		
		try {
			// If the file containing the link capacities exists,
			// populate its object
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

		return boolUsePETGyn;
	}
	
	/**
	 * Iterate PETGyn in order to find the minimal values
	 * to the error measurements that allows a feasible
	 * solution to be found.
	 */
	public void iterateFLIPSODUsingPETGynFindMinimalErrorValues()
	{
		File file = inputFile;
		
		String filePath = "/home/iacer/workspace/FLPM2_cplex/minimal_error_values/";
		
		try {
			// The maximum difference allowed between two different OD matrices
			// Controls if it should continue iterating or stop
			//double epslon = FLIPSOD.odMatrixMaximumError;

			// The previous and actual OD matrices. Their differences
			// shall be minimized throughout the estimation process
			AutoMapValue previousODMatrix = null;
			AutoMapValue actualODMatrix = null;

			// The solution analyser object
			SolutionAnalyser sa = new SolutionAnalyser();

			iterationsCounter = 0;

			// Control variables that say who we are decrementing
			// in each of the iterations
			// Array containing who is to be decremented first
			int[] arrPreferences = new int[4];

			// 0-A, 1-B, 2-C, 3-D
			arrPreferences[0] = 0; // A
			arrPreferences[1] = 1; // B
			arrPreferences[2] = 2; // C
			arrPreferences[3] = 3; // D

			// Tḧe variable under consideration in one step
			int actualIndex = 0;

			// The precision
			double otherEpslon = 0.001;

			// The first unit to be decremented
			double standardStepA = 0.1;
			double standardStepB = 0.1;
			double standardStepD = 0.1;
			double standardStepE = 0.1;

			double stepA = 0.1;
			double stepB = 0.1;
			double stepD = 0.1;
			double stepE = 0.1;

			int who = 0;

			// Iterate
			while (a >= 0 && b >= 0 && d >= 0 && e >= 0) {
				if (actualIndex == 4)
					break;

				// Who are we decrementing?
				who = arrPreferences[actualIndex];

				// If there was no feasible solution in the last iteration,
				// return the error variable back to its last feasible value
				// and start decrementing the next variable
				if (boolIsSolutionUnfeasible) {
					// Return the variables back to where they were
					if (who == 0)
						a += 2 * stepA;
					if (who == 1)
						b += 2 * stepB;
					if (who == 2)
						d += 2 * stepD;
					if (who == 3)
						e += 2 * stepE;

					// Calculate the new step to decrement by
					if (who == 0)
						if (stepA >= otherEpslon)
							stepA = (standardStepA /= 10);
					if (who == 1)
						if (stepB >= otherEpslon)
							stepB = (standardStepB /= 10);
					if (who == 2)
						if (stepD >= otherEpslon)
							stepD = (standardStepD /= 10);
					if (who == 3)
						if (stepE >= otherEpslon)
							stepE = (standardStepE /= 10);

					// If so, calculate the next variable to decrement
					if (actualIndex == 0) {
						if (who == 0)
							if (stepA < otherEpslon)
								actualIndex = 1;
						if (who == 1)
							if (stepB < otherEpslon)
								actualIndex = 1;
						if (who == 2)
							if (stepD < otherEpslon)
								actualIndex = 1;
						if (who == 3)
							if (stepE < otherEpslon)
								actualIndex = 1;
					} else if (actualIndex == 1) {
						if (who == 0)
							if (stepA < otherEpslon)
								actualIndex = 2;
						if (who == 1)
							if (stepB < otherEpslon)
								actualIndex = 2;
						if (who == 2)
							if (stepD < otherEpslon)
								actualIndex = 2;
						if (who == 3)
							if (stepE < otherEpslon)
								actualIndex = 2;
					} else if (actualIndex == 2) {
						if (who == 0)
							if (stepA < otherEpslon)
								actualIndex = 3;
						if (who == 1)
							if (stepB < otherEpslon)
								actualIndex = 3;
						if (who == 2)
							if (stepD < otherEpslon)
								actualIndex = 3;
						if (who == 3)
							if (stepE < otherEpslon)
								actualIndex = 3;
					} else if (actualIndex == 3) {
						if (who == 0)
							if (stepA < otherEpslon)
								actualIndex = 4;
						if (who == 1)
							if (stepB < otherEpslon)
								actualIndex = 4;
						if (who == 2)
							if (stepD < otherEpslon)
								actualIndex = 4;
						if (who == 3)
							if (stepE < otherEpslon)
								actualIndex = 4;
					}

					boolIsSolutionUnfeasible = false;
					continue;
				}
				
				// Make a first estimation and generate an OD matrix.
				// In the first estimation, the arc costs will be available
				// in the problem input file.
				doFLIPSODPreprocessing();

				// Use M3 or M8 to do the simple (a.k.a. one-step) estimation?
				if (this.simpleEstimationModel.equals(SimpleEstimationModelName.M3))
					doFLIPSODSimpleEstimationM3(filePath);
				else
					doFLIPSODSimpleEstimationM8(filePath);
				
				updateLinkFlowsFLIPSOD();
				
				// Use the last added solution added
				int solutionNumber = solutionPool.getArrSolutionLambda().size();
				generateRoutesAndODMatrixFiles(filePath, solutionNumber);
				
				doFLIPSODPETGynUpdateProblem(false, filePath);
				showFLIPSODProblemData();

				iterationsCounter++;

				previousODMatrix = (AutoMapValue) solutionPool
						.getArrSolutionODMatrix().get(0);

				System.out.println(solutionPool
						.getODMatrixValues(previousODMatrix));
				System.out.println("actualIndex: " + actualIndex + ", who: "
						+ who + ", a: " + a + ", b: " + b + ", d: " + d
						+ ", e: " + e + ", boolIsSolutionUnfeasible: "
						+ boolIsSolutionUnfeasible);
				System.out.println("stepA: " + Double.toString(stepA)
						+ ", stepB: " + Double.toString(stepB) + ", stepD: "
						+ Double.toString(stepD) + ", stepE: "
						+ Double.toString(stepE));
				System.out.println("to be continued...");
				
				//if (a - stepA < 0) a = 0;
				//if (b - stepB < 0) b = 0;
				//if (d - stepD < 0) d = 0;
				//if (e - stepE < 0) e = 0;
				
				if (who == 0)
					if (a >= stepA)
						a -= stepA;
					else
						actualIndex++;
				if (who == 1)
					if (b >= stepB)
						b -= stepB;
					else
						actualIndex++;
				if (who == 2)
					if (d >= stepD)
						d -= stepD;
					else
						actualIndex++;
				if (who == 3)
					if (e >= stepE)
						e -= stepE;
					else
						actualIndex++;
			}
			
			// Use the last added solution added
			int solutionNumber = solutionPool.getArrSolutionLambda().size();
			generateRoutesAndODMatrixFiles(filePath, solutionNumber);
			
			doFLIPSODPETGynUpdateProblem(true, filePath);

			System.out.println(
					"actualIndex: " + actualIndex + ", who: " + who +
					", a: " + a + ", b: " + b + ", d: " + d + ", e: " + e
			);
			System.out.println(
					"stepA: " + Double.valueOf(stepA) +
					", stepB: " + Double.valueOf(stepB) +
					", stepD: " + Double.valueOf(stepD) +
					", stepE: " + Double.valueOf(stepE) +
					", boolIsSolutionUnfeasible: " +
					boolIsSolutionUnfeasible
			);
			System.out.println("the end.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Output the values of the solutions (Z and lambda) generated by FLIPSOD.
	 */
	private void generateFLIPSODGraph() {
		// Create the entries to generate the graph
		// (solution Z's) x (1 - solution lambda's)
		String[] solutions_Z = new String[solutionPool.getArrSolutionLambda()
				.size()];
		String[] solutions_lambda = new String[solutionPool
				.getArrSolutionLambda().size()];

		for (int i = 0; i < solutionPool.getArrSolutionLambda().size(); i++) {
			Double d = (Double) solutionPool.getArrSolutionM6().get(i);
			
			String str_Z = Long.toString( d.longValue() );
			String str_lambda = Double.toString((Double) solutionPool
					.getArrSolutionLambda().get(i));

			str_Z = str_Z.replace(".", ",");
			str_lambda = str_lambda.replace(".", ",");

			solutions_Z[i] = str_Z;
			solutions_lambda[i] = str_lambda;
		}

		System.out.println("Solutions in the solution pool (Z):");
		for (int i = 0; i < solutionPool.getArrSolutionLambda().size(); i++) {
			//System.out.print(solutions_Z[i] + "\t");
			System.out.println(solutions_Z[i]);
		}
		System.out.println();
		
		System.out.println("Solutions in the solution pool (lambda):");
		for (int i = 0; i < solutionPool.getArrSolutionLambda().size(); i++) {
			System.out.println(solutions_lambda[i]);
		}
		
		System.out.println();
	}

	/**
	 * Clean up the estimated data
	 */
	private void cleanFLIPSODProblemData() {
		linkCostsFLPSOD = new LinkCosts();
		routeCostsFLPSOD = new RouteCosts();
	}

	/**
	 * Uses the reference link counts and the link capacities in order to
	 * calculate the link costs using the BPR flow-cost function.
	 */
	private void doCalculateArcCostsFLIPSOD()
	{
		LinkCostFlowRelationship linkCost = new LinkCostFlowRelationship();

		// Create the link costs objects (for FLPSOD and Sherali)
		LinkCosts newLinkCostsFLPSOD = null;
		
		System.out.println("previousEstimatedLinkCounts: " +
				previousEstimatedLinkCountsFLPSOD);
		System.out.println("estimatedLinkCounts: " +
				estimatedLinkCountsFLPSOD);

		// Calculate link costs with BPR's cost function
		try {
			newLinkCostsFLPSOD = linkCost.calculateCostFlowRelationship(
					problem,
					linkCapacities,
					estimatedLinkCountsFLPSOD,
					previousEstimatedLinkCountsFLPSOD,
					smoothingMultiplier,
					bprFunctionName
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Update the classes' variables
		linkCostsFLPSOD = newLinkCostsFLPSOD;

		System.out.println("linkCosts FLPSOD (after): "+linkCostsFLPSOD);
	}

	/**
	 * Output the problem data
	 */
	private void showFLIPSODProblemData() {
		// Show the problem data
		System.out.println(problem.toString());
		System.out.println("strVertices: " + strVertices);
		System.out.println("observedLinkCountsFLPSOD: "
				+ observedLinkCountsFLPSOD);
		System.out.println(odCounts);
		System.out.println("routeCostsFLPSOD: " + routeCostsFLPSOD);
		System.out.println("linkCostsFLPSOD: " + linkCostsFLPSOD);
		System.out.println(linkCapacities);
		System.out.println(odPairs);
		System.out.println(referenceODMatrix);
	}

	/**
	 * Uses the objects problem and odPairs in order to initialize the objects
	 * lpModel and routeCostsFLPSOD
	 */
	private void doFLIPSODPreprocessing() {
		// Initialize the LP model to its defaults
		cleanInitLP();

		// Set the preprocessing method to use
		Preprocessing prep = new Preprocessing(
				PreprocessingMethod.PreprocessingKPathsPerODPair
		);

		// Pass the (eventual) parameters to the preprocessing class
		String[] names = new String[3];
		Object[] values = new Object[3];
		Class<?>[] types = new Class[3];

		names[0] = "setUseConstant";
		values[0] = userEquilibrium;
		types[0] = Boolean.class;

		names[1] = "setK";
		values[1] = numberOfRoutesPerODPair;
		types[1] = Integer.class;
		
		names[2] = "setUsingPETGyn";
		values[2] = this.boolUsePETGyn;
		types[2] = Boolean.class;
		
		try {
			prep.setPreprocessingArguments(names, values, types);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Use a temporary object to hold FLIPSOD's link counts
		LinkCounts temporaryLinkCountsFLPSOD = new LinkCounts();
		
		// Iterate through the estimated link counts
		Iterator itEstimatedLinkCountsFLPSOD = estimatedLinkCountsFLPSOD
				.getAllCounts().entrySet().iterator();
		while (itEstimatedLinkCountsFLPSOD.hasNext()) {
			Map.Entry entry = (Map.Entry) itEstimatedLinkCountsFLPSOD.next();

			String separator;
			String entryValue = "";

			if (((String) entry.getKey()).indexOf(",") > -1) {
				separator = ",";

				// System.out.println("entry.getKey(): "+entry.getKey());
				// System.out.println("entry.getValue(): "+entry.getValue());

				Class<?> c = entry.getValue().getClass();
				if (c.toString().indexOf("String") != -1) // if String
					entryValue = (String) entry.getValue();
				else if (c.toString().indexOf("Double") != -1) // if Double
					entryValue = Double.toString((Double) entry.getValue());
			} else {
				separator = "->";
				entryValue = Double.toString((Double) entry.getValue());
			}

			String[] fromTo = ((String) entry.getKey()).split(separator);
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();

			temporaryLinkCountsFLPSOD
					.setCount(fromTo[0], fromTo[1], entryValue);
		}

		// Do the preprocessing and obtain the route costs
		routeCostsFLPSOD = prep.doPreprocessing(
				problem,
				temporaryLinkCountsFLPSOD,
				odCounts,
				linkCostsFLPSOD,
				odPairs
		);

		// Order the routes
		routeCostsFLPSOD.order();
		
		//System.out.println("routeCostsFLPSOD: "+routeCostsFLPSOD);
	}

	/**
	 * Initialize the LP model M8 to its defaults
	 */
	private void cleanInitLP() {
		// Check whether the OD pairs were given.
		// If not, add all the possible pair of vertices as OD pairs
		if (odPairs.getCount() <= 0)
			for (String v1 : problem.vertexSet())
				for (String v2 : problem.vertexSet())
					if ((String) v1 != (String) v2)
						odPairs.add((String) v1, (String) v2);

		// Create the linear programming object and
		// choose the preprocessing routine
		Constructor<?> ct;
		try {
			ct = methodClass.getConstructor();
			lpModel = (IFuzzyLPModel) ct.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Set the OD Matrix nodes
		lpModel.setODMatrixNodes(strVertices);
		
		// Use mixed integer programming or continuous optimization?
		((IFuzzyLPModel) lpModel).setIntegerProgramming(mixedIntegerProgramming);
		
		// Do not use the errors read from the files, instead use
		// the values provided to the class programmatically
		// Uncomment the six lines below to do so
		((IFuzzyLPModel) lpModel).setUseCodedErrors(useCodedErrors);
		((IFuzzyLPModel) lpModel).setUseGradientErrors(useGradientErrors);
		((IFuzzyLPModel) lpModel).setA(a);
		((IFuzzyLPModel) lpModel).setB(b);
		((IFuzzyLPModel) lpModel).setD(d);
		((IFuzzyLPModel) lpModel).setE(e);
	}

	/**
	 * Do a one-step estimation: One lower bound, one upper bound and one fuzzy
	 * estimation. Save the results of the fuzzy estimation.
	 */
	private void doFLIPSODSimpleEstimationM8(String filePath) {
		double solutionM2;
		double solutionM3;
		
		System.out.print("Beginning simple estimation with M8...");
		//System.out.println("observedLinkCountsFLPSOD: "+observedLinkCountsFLPSOD);
		
		// Create the problem M2 (the one that calculates the upper bound) and
		// solve it
		((IFuzzyLPModel) lpModel).createProblemM2(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs
		);
		solutionM2 = ((IFuzzyLPModel) lpModel).solveProblemM2(filePath);
		System.out.println("Solução retornada (M2 - upper bound): "
				+ solutionM2);
		System.out.println();
		
		cleanInitLP();
		
		// Create the problem M3 (the one that calculates the lower bound) and
		// solve it
		((IFuzzyLPModel) lpModel).createProblemM3(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs
		);
		solutionM3 = ((IFuzzyLPModel) lpModel).solveProblemM3(filePath);
		System.out.println("Solução retornada (M3 - lower bound): "
				+ solutionM3);
		System.out.println();
		
		cleanInitLP();
		
		// Get the value of the first solution to M8
		((IFuzzyLPModel) lpModel).createProblemM8(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs,
				linkCapacities,
				solutionM2,
				solutionM3
		);
		
		// ------------------------------------------------
		// Objects to use in the iteration (binary search)
		// ------------------------------------------------

		// The solution found to M8
		double lambda_M8_1, solution_M8_1;
		AutoMapValue odMatrix_M8_1;
		String strModel_M8_1;

		// Re-Initialize the solution pool object
		solutionPool = new SolutionPool();
		
		// Obtain the first solution to M8 based on
		// the original M2
		lambda_M8_1 = ((IFuzzyLPModel) lpModel).solveProblemM8(filePath);
		solution_M8_1 = lpModel.getValueSolutionM8();
		odMatrix_M8_1 = lpModel.getODMatrix();
		
		//System.out.println("odMatrix_M8_1: "+odMatrix_M8_1);
		
		strModel_M8_1 = ((IFuzzyLPModel) lpModel).getLPModel();
		HashMap<String, Double> hashRouteCounts_M8_1 = lpModel.getValueRouteCounts();
		HashMap<String, Double> hashLinkCounts_M8_1 = lpModel.getValueLinkCounts();
		
		// If it is a valid solution, save it
		if (!solutionPool.hasSolution(lambda_M8_1, solution_M8_1, odMatrix_M8_1)) {
			solutionPool.addSolution(
					lambda_M8_1,
					solution_M8_1,
					odMatrix_M8_1,
					hashRouteCounts_M8_1,
					hashLinkCounts_M8_1,
					strModel_M8_1
			);
			
			System.out.println("Solution M8 added to the solution pool.");
		}

		// If there is no feasible solution
		if (lambda_M8_1 == -1)
			boolIsSolutionUnfeasible = true;
		else
			boolIsSolutionUnfeasible = false;
		
		System.out.println(" Finished!");
	}
	
	/**
	 * Do a one-step estimation using M3 only.
	 * Save the results of the estimation.
	 */
	private void doFLIPSODSimpleEstimationM3(String filePath) {
		double solutionM3;
		
		System.out.println("Beginning simple estimation with M3...");
		//System.out.println("observedLinkCountsFLPSOD: "+observedLinkCountsFLPSOD);
		
		// Create the problem M3 (the one that calculates the lower bound) and
		// solve it
		((IFuzzyLPModel) lpModel).createProblemM3(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs
		);
		solutionM3 = ((IFuzzyLPModel) lpModel).solveProblemM3(filePath);
		
		// ------------------------------------------------
		// Objects to use in the iteration (binary search)
		// ------------------------------------------------
		
		// The solution found to M3
		double lambda_M3_1, solution_M3_1;
		AutoMapValue odMatrix_M3_1;
		String strModel_M3_1;
		
		// Re-Initialize the solution pool object
		solutionPool = new SolutionPool();
		
		lambda_M3_1 = 0; // Lambda is not applicable to M3
		solution_M3_1 = solutionM3;
		odMatrix_M3_1 = lpModel.getODMatrixM3();
		strModel_M3_1 = ((IFuzzyLPModel) lpModel).getLPModelM3();
		HashMap<String, Double> hashRouteCounts_M3_1 = lpModel.getValueRouteCountsM3();
		HashMap<String, Double> hashLinkCounts_M3_1 = lpModel.getValueLinkCountsM3();
		
		// If it is a valid solution, save it
		if (!solutionPool.hasSolution(lambda_M3_1, solution_M3_1, odMatrix_M3_1)) {
			solutionPool.addSolution(
					lambda_M3_1,
					solution_M3_1,
					odMatrix_M3_1,
					hashRouteCounts_M3_1,
					hashLinkCounts_M3_1,
					strModel_M3_1
			);
		}

		// If there is no feasible solution
		if (lambda_M3_1 == -1)
			boolIsSolutionUnfeasible = true;
		else
			boolIsSolutionUnfeasible = false;
	}

	/**
	 * Do a complete FLIPSOD estimation. This estimation generates all the
	 * solutions between the solution with the highest lambda (closest to the
	 * best estimates) and the solution with the lowest lambda (closest to
	 * user-equilibrium).
	 */
	private void doFLIPSODEstimation(String filePath) {
		boolean boolMixedIntegerProgramming = false;
		long solutionM2, solutionM3;

		// ------------------------------------------------
		// Objects to use in the iteration (binary search)
		// ------------------------------------------------

		// The first solution found to M8
		double lambda_M8_1;
		long solution_M8_1;
		AutoMapValue odMatrix_M8_1;
		String strModel_M8_1;

		// The second (temporary) solutions
		// found to M2 and M3
		long solution_M2_2, solution_M3_2;

		// The medium solution between M2 and M3
		long solution_NM = Long.MAX_VALUE;

		// The second solution found to M8
		double lambda_M8_2;
		long solution_M8_2;
		AutoMapValue odMatrix_M8_2;
		String strModel_M8_2;

		// The third solution found to M8
		double lambda_M8_3;
		long solution_M8_3;
		AutoMapValue odMatrix_M8_3;
		String strModel_M8_3;

		// The maximum error allowed between two
		// equal lambdas
		double lambda_epslon = FLIPSOD_Maximize.lambdaMaximumError;

		// ------------------------------------------------
		// Initialize the defaults
		cleanInitLP();
		
		// Use mixed integer programming or continuous optimization?
		((IFuzzyLPModel) lpModel).setIntegerProgramming(boolMixedIntegerProgramming);
		
		//TODO
		
		// Do not use the errors read from the files, instead use
		// the values provided to the class programmatically
		((IFuzzyLPModel) lpModel).setUseCodedErrors(useCodedErrors);
		((IFuzzyLPModel) lpModel).setUseGradientErrors(useGradientErrors);
		((IFuzzyLPModel) lpModel).setA(a);
		((IFuzzyLPModel) lpModel).setB(b);
		((IFuzzyLPModel) lpModel).setD(d);
		((IFuzzyLPModel) lpModel).setE(e);

		//System.out.println("observedLinkCounts: " + observedLinkCountsFLPSOD);

		// Create the problem M2 (the one that calculates the upper bound) and
		// solve it
		((IFuzzyLPModel) lpModel).createProblemM2(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs);
		solutionM2 = (long) ((IFuzzyLPModel) lpModel).solveProblemM2(filePath);
		//solutionM2 = 25000;
		
		//System.out.println("Solução retornada (M2 - upper bound): " + solutionM2);
		//System.out.println();
		// System.out.println("Model M2:" +
		// ((IFuzzyLPModel)lpModel).getLPModelM2());
		
		// Create the problem M3 (the one that calculates the lower bound) and
		// solve it
		((IFuzzyLPModel) lpModel).createProblemM3(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs
		);
		solutionM3 = (long) ((IFuzzyLPModel) lpModel).solveProblemM3(filePath);
		
		// Get the value of the first solution to M8
		((IFuzzyLPModel) lpModel).createProblemM8(
				problem,
				observedLinkCountsFLPSOD,
				odCounts,
				routeCostsFLPSOD,
				linkCostsFLPSOD,
				odPairs,
				linkCapacities,
				solutionM2,
				solutionM3
		);
		
		// Initialize the solution pool object
		solutionPool = new SolutionPool();

		// Obtain the first solution to M8 based on
		// the original M2
		// *** not using M3 as an input anymore
		lambda_M8_1 = ((IFuzzyLPModel) lpModel).solveProblemM8(filePath);
		solution_M8_1 = (long) lpModel.getValueSolutionM8();
		odMatrix_M8_1 = lpModel.getODMatrix();
		strModel_M8_1 = ((IFuzzyLPModel) lpModel).getLPModel();
		HashMap<String, Double> hashRouteCounts_M8_1 = lpModel
				.getValueRouteCounts();
		HashMap<String, Double> hashLinkCounts_M8_1 = lpModel
				.getValueLinkCounts();
		
		// If the first solution found has lambda of one,
		// do not generate more solutions
		if (lambda_M8_1 == 1)
		{
			solutionPool.addSolution(lambda_M8_1, solution_M8_1,
					odMatrix_M8_1, hashRouteCounts_M8_1,
					hashLinkCounts_M8_1, strModel_M8_1);
			
			System.out.println( solutionPool.getODMatrixValues(odMatrix_M8_1) );
			
			// Use the last added solution added
			int solutionNumber = solutionPool.getArrSolutionLambda().size();
			
			generateRoutesAndODMatrixFiles(filePath, solutionNumber);
			
			// Comment the return below to save the lambda == 1 solution
			// and still generate the other possible solutions
			return;
		}
		// -------------------------------------------------------------------
		// -------------------- Here starts the iteration --------------------
		// -------------------------------------------------------------------
		
		// If it is the last iteration of the method, make it obtain the whole
		// spectrum of solutions available
		solution_M2_2 = Math.min(solutionM2, solution_M8_1);
		
		// Lambda to M2'
		double lambda_M2_2 = lambda_M8_1;
		
		OUTERMOST: while (solution_M2_2 >= solutionM3) {
			solution_M3_2 = solutionM3;
			
			// Lambda to M3'
			double lambda_M3_2 = 0;
			
			HashMap<String, Double> hashRouteCounts_M8_2;
			HashMap<String, Double> hashLinkCounts_M8_2;
			
			long temp_solution_M2_2 = Long.MAX_VALUE;
			long temp_solution_NM = Long.MAX_VALUE;
			
			lambda_M8_2 = -1;
			solution_M8_2 = -1;
			odMatrix_M8_2 = null;
			strModel_M8_2 = "";
			hashRouteCounts_M8_2 = null;
			hashLinkCounts_M8_2 = null;
			
			// Search for solution with the same lambda_M8
			// but the smallest value for Z possible
			INNERMOST: while (solution_M2_2 > solution_M3_2)
			{
				//System.out.println("Before NM assignment...");
				//System.out.print("solution_NM: "+ solution_NM);
				//System.out.println("solution_M2_2: "+solution_M2_2+
				//		", solution_M3_2: "+solution_M3_2);
				solution_NM = (solution_M2_2 + solution_M3_2) / 2;
				
				System.out.println("Before if");
				System.out.println("M2_2: " + solution_M2_2+ ", "+lambda_M2_2);
				System.out.println("M3_2: " + solution_M3_2+ ", "+lambda_M3_2);
				System.out.println("NM: " + solution_NM+ ", "+lambda_M8_2);
				System.out.println();
				
				// -----------------------------------------------
				// Execute M8 with upper-bound = solution_NM
				// and obtain the second solution for M8
				// -----------------------------------------------

				// Initialize the defaults
				cleanInitLP();
				
				// Use mixed integer programming or continuous optimization?
				((IFuzzyLPModel) lpModel).setIntegerProgramming(boolMixedIntegerProgramming);

				// Do not use the errors read from the files, instead use
				// the values provided to the class programmatically
				((IFuzzyLPModel) lpModel).setUseCodedErrors(useCodedErrors);
				((IFuzzyLPModel) lpModel).setUseGradientErrors(useGradientErrors);
				((IFuzzyLPModel) lpModel).setA(a);
				((IFuzzyLPModel) lpModel).setB(b);
				((IFuzzyLPModel) lpModel).setD(d);
				((IFuzzyLPModel) lpModel).setE(e);
				
				// Create the model
				((IFuzzyLPModel) lpModel).createProblemM8(
						problem,
						observedLinkCountsFLPSOD,
						odCounts,
						routeCostsFLPSOD,
						linkCostsFLPSOD,
						odPairs,
						linkCapacities,
						solution_NM,
						solution_M3_2
				);

				// Obtain the second solution to M8 based on
				// the upperbound solution_NM
				
				lambda_M8_2 = ((IFuzzyLPModel) lpModel).solveProblemM8(filePath);
				solution_M8_2 = (long) lpModel.getValueSolutionM8();
				odMatrix_M8_2 = lpModel.getODMatrix();
				strModel_M8_2 = ((IFuzzyLPModel) lpModel).getLPModel();
				hashRouteCounts_M8_2 = lpModel.getValueRouteCounts();
				hashLinkCounts_M8_2 = lpModel.getValueLinkCounts();

				//System.out.println();
				//System.out.println("M8_1: " + solution_M8_1 +
				//		", lambdaM8_1 = " + lambda_M8_1);
				//System.out.println("M8_2: " + solution_M8_2 +
				//		", lambdaM8_2 = " + lambda_M8_2);
				//System.out.println();
				
				// If the problem is unfeasible
				if (lambda_M8_2 == -1)
				{
					//break INNERMOST;
					solution_M3_2 = solution_NM;
				} else
				// If the two solutions found have equal lambdas
				if (Math.abs(lambda_M8_1 - lambda_M8_2) < lambda_epslon) {
					// solution_M2_2 = Math.min(solution_M8_2, solution_NM - 1);
					//System.out.println("First");
					solution_M2_2 = Math.min(solution_M8_2, solution_NM);
					lambda_M2_2 = lambda_M8_2;
				} else {
					// solution_M3_2 = solution_NM + 1;
					//System.out.println("Last");
					solution_M3_2 = solution_NM;
					lambda_M3_2 = lambda_M8_2;
				}
				
				System.out.println("After if");
				//System.out.println("M2_2: " + solution_M2_2+", "+lambda_M2_2);
				//System.out.println("M3_2: " + solution_M3_2+", "+lambda_M3_2);
				System.out.println("NM: " + solution_NM + ", "+lambda_M8_2);
				System.out.println();
				
				// If the solution between the lower bound and the upper bound
				// (NM) is equal to the lower bound, it has just reached its minimum
				// Leave the innermost while
				if ((temp_solution_M2_2 != Long.MAX_VALUE &&temp_solution_NM != Long.MAX_VALUE) &&
						((temp_solution_M2_2 - temp_solution_NM) <= 1))
				{
					if (solution_NM == solution_M3_2) {
						//System.out.println("solution_NM == solution_M3_2 == "
						//		+ solution_NM);
						//System.out.println("Leaving inner while...");
						break INNERMOST;
					}
				}
				
				// Set the temporary variables to use them in a
				// comparison in the beginning the next iteration
				temp_solution_M2_2 = solution_M2_2;
				temp_solution_NM = solution_NM;
			}
			
			// -----------------------------------------------
			// Execute M8 with upper-bound = solution_M2_2
			// and obtain the second solution for M8
			// -----------------------------------------------
			
			// Initialize the defaults
			cleanInitLP();
			
			// Do not use the errors read from the files, instead use
			// the values provided to the class programmatically
			((IFuzzyLPModel) lpModel).setUseCodedErrors(useCodedErrors);
			((IFuzzyLPModel) lpModel).setUseGradientErrors(useGradientErrors);
			((IFuzzyLPModel) lpModel).setA(a);
			((IFuzzyLPModel) lpModel).setB(b);
			((IFuzzyLPModel) lpModel).setD(d);
			((IFuzzyLPModel) lpModel).setE(e);
			
			// Create the model
			((IFuzzyLPModel) lpModel).createProblemM8(
					problem,
					observedLinkCountsFLPSOD,
					odCounts,
					routeCostsFLPSOD,
					linkCostsFLPSOD,
					odPairs,
					linkCapacities,
					solution_M2_2,
					solution_M3_2
			);

			// Obtain the third solution to M8 based on
			// the upperbound solution_M2_2 + 1
			lambda_M8_3 = ((IFuzzyLPModel) lpModel).solveProblemM8(filePath);
			solution_M8_3 = (long) lpModel.getValueSolutionM8();
			odMatrix_M8_3 = lpModel.getODMatrix();
			strModel_M8_3 = ((IFuzzyLPModel) lpModel).getLPModel();
			HashMap<String, Double> hashRouteCounts_M8_3 = lpModel
					.getValueRouteCounts();
			HashMap<String, Double> hashLinkCounts_M8_3 = lpModel
					.getValueLinkCounts();
			
			// Save the solution just found (the third one)
			// If it is a valid solution, save it
			if (!solutionPool.hasSolution(lambda_M8_3, solution_M8_3, odMatrix_M8_3))
			{
				if (lambda_M8_3 < 0)
					lambda_M8_3 = 0.0;
				
				solutionPool.addSolution(lambda_M8_3, solution_M8_3,
						odMatrix_M8_3, hashRouteCounts_M8_3,
						hashLinkCounts_M8_3, strModel_M8_3);
				
				System.out.println( solutionPool.getODMatrixValues(odMatrix_M8_3) );
				
				// Use the last added solution added
				int solutionNumber = solutionPool.getArrSolutionLambda().size();
				
				generateRoutesAndODMatrixFiles(filePath, solutionNumber);
			} else {
				System.out.println("Didn't add the solution with lambda "+
						lambda_M8_3 +", ("+solution_M8_3+")");
			}
			
			System.out.println("solutionPool: "+
					solutionPool.getArrSolutionLambda().get(
							//0
							solutionPool.getArrSolutionLambda().size()-1
					) + "\n" +
					solutionPool.getArrSolutionM6().get(
							//0
							solutionPool.getArrSolutionLambda().size()-1
					)
			);
			
			// Update M2_2 with M3_2
			solution_M2_2 = solution_M3_2;
			
			// -----------------------------------------------
			// Execute M8 with upper-bound = solution_M2_2 + 1
			// and obtain the second solution for M8
			// -----------------------------------------------
			
			// Initialize the defaults
			cleanInitLP();
			
			// Do not use the errors read from the files, instead use
			// the values provided to the class programmatically
			((IFuzzyLPModel) lpModel).setUseCodedErrors(useCodedErrors);
			((IFuzzyLPModel) lpModel).setUseGradientErrors(useGradientErrors);
			((IFuzzyLPModel) lpModel).setA(a);
			((IFuzzyLPModel) lpModel).setB(b);
			((IFuzzyLPModel) lpModel).setD(d);
			((IFuzzyLPModel) lpModel).setE(e);
			
			// Create the model
			((IFuzzyLPModel) lpModel).createProblemM8(
					problem,
					observedLinkCountsFLPSOD,
					odCounts,
					routeCostsFLPSOD,
					linkCostsFLPSOD,
					odPairs,
					linkCapacities,
					solution_M2_2,
					solution_M3_2
			);

			// Obtain the third solution to M8 based on
			// the upperbound solution_M2_2 + 1
			lambda_M8_3 = ((IFuzzyLPModel) lpModel).solveProblemM8(filePath);
			solution_M8_3 = (long) lpModel.getValueSolutionM8();
			odMatrix_M8_3 = lpModel.getODMatrix();
			strModel_M8_3 = ((IFuzzyLPModel) lpModel).getLPModel();
			hashRouteCounts_M8_3 = lpModel.getValueRouteCounts();
			hashLinkCounts_M8_3 = lpModel.getValueLinkCounts();
			
			// Uncomment to generate all solutions or
			// find only the solution with maximum lambda
			//System.exit(0);
			
			// Update the original lambda with the newly calculated one
			lambda_M8_1 = lambda_M8_3;
			
			if (lambda_M8_1 == -1 || solution_M2_2==0 || lambda_M8_3 <= 0 ||
					(solution_M2_2 == solutionM3 && solution_M3_2 == solutionM3) )
			{
				break OUTERMOST;
			}
		}

		// -------------------------------------------------------------------
		// --------------------- Iteration has finished ----------------------
		// -------------------------------------------------------------------

	}

	/**
	 * Analyse the solution(s) generated by FLIPSOD. Write its results either in
	 * the screen or in a file.
	 */
	public void analyseFLIPSODSolution() {
		// --------------------------------------------------------------------
		// Compare FLPSOD's solutions and the observed solution
		// --------------------------------------------------------------------

		SolutionAnalyser sa = new SolutionAnalyser();
		ArrayList arrSolutionsFLPSODMatrix = solutionPool
				.getArrSolutionODMatrix();
		double rmseOD, maeOD;

		if (!referenceODMatrix.getODMatrix().isEmpty()) {
			// If FLPSOD found at least one feasible solution, show its results
			if (!arrSolutionsFLPSODMatrix.isEmpty()) {
				// Get the first solution added to the model
				AutoMapValue odMatrixFLPSOD = (AutoMapValue) arrSolutionsFLPSODMatrix
						.get(0);

				System.out.println("Comparing FLPSOD's OD matrix to the observed OD matrix:");
				rmseOD = sa.compareODMatricesRMSE(odMatrixFLPSOD, referenceODMatrix);
				System.out.println("%RMSE OD: " + rmseOD);

				maeOD = sa.compareODMatricesMAE(odMatrixFLPSOD, referenceODMatrix);
				System.out.println("%MAE OD: " + maeOD);
			} else {
				System.out.println("FLPSOD couldn't find a feasible solution.");
			}
			System.out.println();
		} else {
			System.out.println("No reference OD matrix loaded. "
					+ "No comparison between OD matrices will be done.");
		}

		System.out.println();

		ArrayList arrSolutionsFLPSODLinkFlows = solutionPool
				.getArrSolutionLinkCounts();
		HashMap<String, Double> FLPSODLinkCounts = null;

		if (!arrSolutionsFLPSODLinkFlows.isEmpty())
			FLPSODLinkCounts = (HashMap<String, Double>) arrSolutionsFLPSODLinkFlows.get(0);

		// For each link that has counts, calculate the sum of its flows
		// estimated by FLIPSOD
		Iterator itReferenceLinkCountsFLPSOD = observedLinkCountsFLPSOD
				.getAllCounts().entrySet().iterator();
		while (itReferenceLinkCountsFLPSOD.hasNext()) {
			Map.Entry entryReference = (Map.Entry) itReferenceLinkCountsFLPSOD
					.next();
			String linkName = (String) entryReference.getKey();

			double estimatedLinkValueFLPSOD = 0;

			String[] fromTo = linkName.split(",");
			fromTo[0] = fromTo[0].replace("(", "").trim();
			fromTo[1] = fromTo[1].replace(")", "").trim();

			// System.out.println("FLPSOD has "+flpsodLinkCounts.entrySet().size()+" link counts.");
			// Calculate the sum to FLPSOD
			if (!arrSolutionsFLPSODLinkFlows.isEmpty()) {
				Iterator itEstimatedLinkCounts = FLPSODLinkCounts.entrySet()
						.iterator();
				while (itEstimatedLinkCounts.hasNext()) {
					Map.Entry entryEstimated = (Map.Entry) itEstimatedLinkCounts
							.next();
					String strKey = (String) entryEstimated.getKey();
					Double dblValue = (Double) entryEstimated.getValue();

					String[] strLink = strKey.split("->");

					if (Integer.parseInt(strLink[0]) == Integer
							.parseInt(fromTo[0])
							&& Integer.parseInt(strLink[1]) == Integer
									.parseInt(fromTo[1]))
						estimatedLinkValueFLPSOD += dblValue;
				}
				// }
				// if (!arrSolutionsFLPSODLinkFlows.isEmpty())
				FLPSODLinkCounts.put(fromTo[0] + "->" + fromTo[1],
						estimatedLinkValueFLPSOD);
			}
		}

		double rmseArcCounts, maeArcCounts;

		// If FLPSOD found at least one feasible solution, show its results
		if (!arrSolutionsFLPSODMatrix.isEmpty()) {
			System.out
					.println("Comparing FLPSOD's arc counts to the observed arc counts:");
			rmseArcCounts = sa.compareArcFlowsRMSE(FLPSODLinkCounts,
					observedLinkCountsFLPSOD.getAllCounts());
			System.out.println("%RMSE ARC: " + rmseArcCounts);

			maeArcCounts = sa.compareArcFlowsMAE(FLPSODLinkCounts,
					observedLinkCountsFLPSOD.getAllCounts());
			System.out.println("%MAE ARC: " + maeArcCounts);
		} else {
			System.out.println("FLPSOD couldn't find a feasible solution.");
		}

		System.out.println();
	}

	/**
	 * Update the link flows with the last estimation values. If there is a big
	 * variation on the flows in links with missing estimations, use a smoothing
	 * factor to calculate its new values.
	 * 
	 * @param solutionPool
	 *            the pool with all the generated solutions
	 */
	private void updateLinkFlowsFLIPSOD() throws Exception {
		// Shouldn't smooth only the flows in the first iteration
		boolean smooth = (iterationsCounter <= 1) ? false : true;

		ArrayList arrSolutionsFLPSODLinkFlows = solutionPool
				.getArrSolutionLinkCounts();

		if (arrSolutionsFLPSODLinkFlows.isEmpty())
			throw new Exception("No solutions available in the solution pool.");

		// Always use the first solution found (the one with highest lambda)
		HashMap<String, Double> flpsodNewLinkCounts = (HashMap<String, Double>)
				arrSolutionsFLPSODLinkFlows.get(arrSolutionsFLPSODLinkFlows.size() - 1);
		
		System.out.println("estimatedLinkCountsFLPSOD: "
				+ estimatedLinkCountsFLPSOD);
		System.out.println("flpsodNewLinkCounts (before update): "
				+ flpsodNewLinkCounts);
		System.out.println("observedLinkCountsFLPSOD: "
				+ observedLinkCountsFLPSOD);

		// Iterate through the link counts and their estimations
		Iterator itLCE = flpsodNewLinkCounts.entrySet().iterator();
		while (itLCE.hasNext()) {
			Map.Entry entry = (Map.Entry) itLCE.next();

			String strArc = (String) entry.getKey();
			double newFlow = 0;
			double actualFlow;
			double previousFlow = 0;
			String separator;

			// Gets the string separator to the link flow
			separator = (strArc.indexOf(",") > -1) ? "," : "->";

			// Gets the value of the actual flow
			Class c = entry.getValue().getClass();
			if (c.toString().indexOf("String") != -1) // is String
				actualFlow = Double.parseDouble((String) entry.getValue());
			else
				actualFlow = (Double) entry.getValue();

			String from = strArc.split(separator)[0];
			String to = strArc.split(separator)[1];
			from = from.replace("(", "").trim();
			to = to.replace(")", "").trim();

			newFlow = actualFlow;
			// System.out.println("(from,to): ("+from+","+to+")");

			if (previousEstimatedLinkCountsFLPSOD.getAllCounts().containsKey(
					"(" + from + "," + to + ")"))
				previousFlow = Double.parseDouble(
						(String) previousEstimatedLinkCountsFLPSOD
								.getAllCounts()
								.get("(" + from + "," + to + ")")
				);
			
			if (previousEstimatedLinkCountsFLPSOD.getAllCounts().containsKey(
							from + "->" + to))
				previousFlow = Double.parseDouble(
						(String) previousEstimatedLinkCountsFLPSOD
								.getAllCounts()
								.get(from + "->" + to)
				);

			// if the link has no observation of its counts, smooth
			// its calculations by the smoothingMultiplier parameter
			//if (!observedLinkCountsFLPSOD.hasCounts(from, to)) {
				// If we're supposed to smooth the solution flow
				if (smooth)
				{
					// The new flow to add or remove from the
					// actual flow on arc
					double flowDiff = Math.abs(previousFlow - actualFlow)
							* smoothingMultiplier;
					
					// If the flow recently estimated is bigger than
					// the previous flow on the arc
					if (actualFlow > previousFlow)
					{
						if (previousFlow + flowDiff <= actualFlow)
							newFlow = previousFlow + flowDiff;
						else
							newFlow = actualFlow;
					} else //if (actualFlow <= previousFlow)
					{
						if (previousFlow - flowDiff >= actualFlow)
							newFlow = previousFlow - flowDiff;
						else
							newFlow = actualFlow;
					}
					
					//newFlow = previousFlow - ((previousFlow - actualFlow) * smoothingMultiplier);
				} else
				{
					//System.err.println("previousFlow: "+previousFlow);
					//System.err.println("actualFlow: "+actualFlow);
					newFlow = previousFlow;
				}

				// System.out.print("Unnobserved link ("+from+","+to+")");
				// System.out.print(", previous: "+previousFlow);
				// System.out.print(", new: "+actualFlow);
				// System.out.print(", actual: "+newFlow);
				// System.out.println();
			//}

			// if the link does have observation of its counts,
			// don't do anything
			if (observedLinkCountsFLPSOD.hasCounts(from, to)) {
				// If we're supposed to smooth the solution flow
				// if (smooth)
				// newFlow = previousFlow - ((previousFlow -
				// actualFlow)*smoothingMultiplier);

				// System.out.print("Observed link ("+from+","+to+")");
				// System.out.print(", previous flow: "+previousFlow);
				// System.out.print(", actual flow: "+actualFlow);
				// System.out.print(", new (used) flow: "+newFlow);
				// System.out.println();

			}

			String errors = estimatedLinkCountsFLPSOD.getErrorsBetween(Integer
					.parseInt(from), Integer.parseInt(to));

			String errorBelow = errors.split(" - ")[0];
			String errorAbove = errors.split(" - ")[1];

			if (errorBelow.isEmpty())
				errorBelow = "1";
			if (errorAbove.isEmpty())
				errorAbove = "1";

			// Update the previous and actual flows
			previousEstimatedLinkCountsFLPSOD.setCount(
					from,
					to,
					Double.toString(newFlow),
					1, // negative error of 100%
					1 // positive error of 100%
			);

			estimatedLinkCountsFLPSOD.setCount(
					from,
					to,
					Double.toString(newFlow),
					1, // negative error of 100%
					1 // positive error of 100%
			);
		}

		// System.out.println("Updated link flows:");
		// System.out.println("previousEstimatedLinkCountsFLPSOD (after update): "+
		// previousEstimatedLinkCountsFLPSOD);
		System.out.println("estimatedLinkCountsFLPSOD (after update): "
				+ this.estimatedLinkCountsFLPSOD);
	}

	/**
	 * Generate the routes file with the name <code>Rotas_2_output.dat</code> in
	 * which each line is in the format
	 * <code>[0..9] (node1_node2_node3_..._nodeN) routeFlow</code>
	 * 
	 * - The first number that appears [0..9] is just an index of the route and
	 * may be overlooked. - The middle entry (node1_node2_node3_..._nodeN)
	 * contains the nodes that compose the route. - The routeFlow is a float
	 * value with the final allocated route flow.
	 * 
	 * @param idProjeto The PETGyn project id
	 */
	private void generateRoutesFile(int idProjeto, String filePath, String fileName, int solutionNumber)
	{
		// If there is already a file corresponding to the routes,
		// delete it and create a new one
		
		// Create one route file per iteration
		File outputFileRoutes = new File(
				filePath + fileName
		);

		outputFileRoutes.delete();
		try {
			outputFileRoutes.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create a writer to the new, empty routes file
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter( new FileWriter(outputFileRoutes) );
		} catch (FileNotFoundException e) {
			System.err.println("Erro ao abrir arquivo");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// The array containing the feasible solutions found by FLIPSOD
		ArrayList arrSolutionsFLPSODRoutes = solutionPool.getArrSolutionRouteCounts();

		int counter = 0;
		
		// If FLPSOD found at least one feasible solution,
		// write the first solution received in the solutionNumber parameter
		// in the solution pool to a file
		if (!arrSolutionsFLPSODRoutes.isEmpty()) {
			// Get the first solution added to the model
			HashMap<String, Double> routeFlowsFLPSOD =
				(HashMap<String, Double>) arrSolutionsFLPSODRoutes.get( solutionNumber );

			Iterator itRoute = routeFlowsFLPSOD.entrySet().iterator();

			// Iterate through each route
			String node = null;
			while (itRoute.hasNext()) {
				Map.Entry entry = (Map.Entry) itRoute.next();
				Double entryValue = (Double) entry.getValue();

				// Prints the route index and the open bracket
				outWriter.print(counter++ + " ");

				// Iterate through each node in the route
				// Print the nodes in the format:
				// (node1_node2_node3_..._nodeN)

				String strRoute = (String) entry.getKey();
				String[] strNodes = strRoute.split(",");
				for (int i = 0; i < strNodes.length; i++) {
					outWriter.print(strNodes[i]);

					// In the last node, don't print the underscore
					if (i != strNodes.length - 1)
						outWriter.print("_");
				}

				// Add the route flow to the file and break the line
				outWriter.println(" " + entryValue);
			}
		} else {
			System.out.println("FLPSOD couldn't find a feasible solution.");
		}

		// Free the resource
		outWriter.close();
		
		System.out.println("The routes file has been successively created.");
	}

	/**
	 * Generates the MatrixOD_2_output.dat file using the data available in the
	 * solution pool object.
	 * 
	 * @param idProjeto The PETGyn project id
	 */
	private void generateODMatrixFile(int idProjeto, String filePath, String fileName, int solutionNumber)
	{
		// If there is already a file corresponding to the OD matrix,
		// delete it and create a new one
		
		// Generate one OD matrix file per iteration
		File fileODMatrix = new File(
				filePath + fileName
		);

		fileODMatrix.delete();
		try {
			fileODMatrix.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create a writer to the new, empty OD Matrix file
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter( new FileWriter(fileODMatrix) );
		} catch (FileNotFoundException e) {
			System.err.println("Erro ao abrir arquivo");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// The array containing the feasible solutions found by FLIPSOD
		ArrayList arrSolutionsFLPSODMatrix = solutionPool
				.getArrSolutionODMatrix();

		// If FLPSOD found at least one feasible solution,
		// write the solution received in the solutionNumber parameter
		// in the solution pool to a file
		if (!arrSolutionsFLPSODMatrix.isEmpty()) {
			// Get the first solution added to the model
			AutoMapValue odMatrixFLPSOD = (AutoMapValue)
					arrSolutionsFLPSODMatrix.get( solutionNumber );

			Iterator itI1, itO1;
			Map i, j;
			AutoMapValue temp, temp2;
			Map.Entry o, d;
			i = odMatrixFLPSOD.getMap();
			int size = 0;

			// Iterate through the OD matrix structure
			itI1 = i.entrySet().iterator();
			while (itI1.hasNext()) {
				o = (Map.Entry) itI1.next();
				size++;

				temp = (AutoMapValue) o.getValue();
				j = temp.getMap();

				itO1 = j.entrySet().iterator();
				while (itO1.hasNext()) {
					d = (Map.Entry) itO1.next();
					temp2 = (AutoMapValue) d.getValue();

					int from = Integer.parseInt((String) o.getKey());
					int to = Integer.parseInt((String) d.getKey());

					if (from == to)
						continue;
					// /System.out.println("Entry "+from+","+to);

					if (temp2.get() != null) {
						double entryValue = Double.parseDouble((String) temp2
								.get());

						// Add the line entry to the file
						outWriter.println("(" + from + "," + to + ","
								+ entryValue + ")");
					}
				}
			}
		} else {
			System.out.println("FLIPSOD couldn't find a feasible solution.");
		}

		// Free the resource
		outWriter.close();

		System.out.println("The OD Matrix file has been successively created.");
	}
	
	/**
	 * 
	 */
	private void generateRoutesAndODMatrixFiles(String filePath, int solutionNumber)
	{
		// Creates the link count file and the OD matrix file
		writeOneSolutionToFile(filePath, solutionNumber);
		
		// OD matrix
		String odMatrixFileName = solutionNumber+" - MatrixOD_"+idProjeto
				+"_solution_"+solutionNumber+".dat";
		String outputOdMatrixFileName = solutionNumber+" - New_MatrixOD_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		
		// Routes
		String routeFileName = solutionNumber+" - Rotas_"+idProjeto+
				"_solution_"+solutionNumber+".dat";
		String convertedRouteFileName = solutionNumber+" - New_Rotas_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		
		// Link counts
		String linkCountFileName = solutionNumber+" - LinkCounts_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		String convertedLinkCountFileName = solutionNumber+" - New_LinkCounts_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		String partialLinkCountFileName = solutionNumber+" - Partial_New_LinkCounts_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		
		solutionNumber--;
		
		// Generates the routes file
		generateRoutesFile(idProjeto, filePath, routeFileName, solutionNumber);
		// Generates the OD matrix file
		generateODMatrixFile(idProjeto, filePath, odMatrixFileName, solutionNumber);
		
		// Update the OD matrix and route file names
		lastEstimatedODMatrixFileName = outputOdMatrixFileName;
		lastEstimatedRouteFileName = convertedRouteFileName;
		
		// If using PETGyn, transform the solutions in G' to G
		if (this.boolUsePETGyn)
		{
			// It generates three kinds files as well:
			// - New_Rotas_2_solution_1.dat with the correspondant routes in G
			// - New_MatrixOD_2_solution_1.dat with the correspondant OD pairs in G
			// - New_LinkCounts_2_solution_1.dat with the correspondant link counts in G
			FixSolution f = new FixSolution();
			f.carregaMap(2);
			// Generate the converted OD matrix file
			f.fixMatrixOD(filePath, odMatrixFileName, outputOdMatrixFileName);
			// Overwrite the converted OD matrix file with the converted, ordered OD matrix to G'
			f.orderODMatrixEstimations(filePath, outputOdMatrixFileName, outputOdMatrixFileName);
			// Generate the converted routes
			f.fixRoutes(filePath, routeFileName, convertedRouteFileName);
			// Overwrite the converted routes with the converted, ordered routes
			f.orderRoutes(filePath, convertedRouteFileName, convertedRouteFileName);
		}
	}
	
	/**
	 * Use the values estimated in the last iteration and provide them to
	 * PETGyn, in order to obtain updated arc costs.
	 */
	private void doFLIPSODPETGynUpdateProblem(boolean isLastCycle, String filePath)
	{
		File file = inputFile;
		
		//filePath += "routesAndOdMatrix/";
		//System.out.println("filePath: "+filePath);
		
		// The FixSolution object uses two input files:
		// - Rotas_2_output.dat with the routes in G'
		// - MatrixOD_2_output.dat with the OD matrix in G'
		// The new constructor allows the need of only the last file
		// (MatrixOD_2_output.dat)
		// by providing the 2nd parameter the value false

		// It generates two files as well:
		// - New_Rotas_2_output.dat with the correspondant routes in G
		// - New_MatrixOD_2_output.dat with the correspondant OD pairs in G
		//FixSolution f = new FixSolution(2, true);
		
		System.out.println("iterationsCounter: "+iterationsCounter);
		System.out.println("lastEstimatedODMatrixFileName: "+lastEstimatedODMatrixFileName);
		
		//System.exit(0);
		
		// It uses one input file:
		// TODO
		// - New_MatrixOD_2_output.dat with the correspondant OD pairs in G

		// It generates one output file:
		// - CustoNosArcos_2_output.dat with the new arc costs generated by
		// PETGyn
		Cycle cycle = new Cycle(
				2,
				(iterationsCounter == 0),
				isLastCycle,
				filePath,
				lastEstimatedODMatrixFileName
		);

		// Update the arc costs with the new costs obtained from PETGyn
		// by opening all the newly generated input files
		loadFile();
	}
	
	/**
	 * A dummy method to generate some data simulating PET-Gyn's project 2
	 */
	public void createMatrix() {
		int[] o = new int[25];
		o[0] = 322;
		o[1] = 326;
		o[2] = 336;
		o[3] = 464;
		o[4] = 465;
		o[5] = 470;
		o[6] = 472;
		o[7] = 474;
		o[8] = 586;
		o[9] = 588;
		o[10] = 590;
		o[11] = 592;
		o[12] = 606;
		o[13] = 610;
		o[14] = 611;
		// o[15] = 619;
		o[15] = -1;
		o[16] = 622;
		o[17] = 2056;
		o[18] = 2058;
		o[19] = 2060;
		o[20] = 2062;
		o[21] = 2064;
		o[22] = 2066;
		o[23] = 2068;
		o[24] = 2070;

		System.out.println();
		for (int i = 0; i < o.length; i++) {
			for (int j = 0; j < o.length; j++) {
				if (i == j || o[i] == -1 || o[j] == -1)
					continue;
				System.out.print("(" + o[i] + "," + o[j] + ") ");
			}
		}
	}

	/**
	 * @return the methodClassName
	 */
	public MethodClassName getMethodClassName() {
		return methodClassName;
	}

	/**
	 * @param methodClassName
	 *            the methodClassName to set
	 */
	public void setMethodClassName(MethodClassName methodClassName) {
		this.methodClassName = methodClassName;
		try {
			methodClass = Class.forName("cplexModels" + "."
					+ methodClassName.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the smoothingMultiplier
	 */
	public double getSmoothingMultiplier() {
		return smoothingMultiplier;
	}

	/**
	 * @param smoothingMultiplier
	 *            the smoothingMultiplier to set
	 */
	public void setSmoothingMultiplier(double smoothingMultiplier) {
		this.smoothingMultiplier = smoothingMultiplier;
	}

	/**
	 * @return the userEquilibrium
	 */
	public boolean isUserEquilibrium() {
		return userEquilibrium;
	}

	/**
	 * @param userEquilibrium
	 *            the userEquilibrium to set
	 */
	public void setUserEquilibrium(boolean userEquilibrium) {
		this.userEquilibrium = userEquilibrium;
	}

	/**
	 * @return the numberOfRoutesPerODPair
	 */
	public int getNumberOfRoutesPerODPair() {
		return numberOfRoutesPerODPair;
	}

	/**
	 * @param numberOfRoutesPerODPair
	 *            the numberOfRoutesPerODPair to set
	 */
	public void setNumberOfRoutesPerODPair(int numberOfRoutesPerODPair) {
		this.numberOfRoutesPerODPair = numberOfRoutesPerODPair;
	}

	/**
	 * @return the mixedIntegerProgramming
	 */
	public boolean isMixedIntegerProgramming() {
		return mixedIntegerProgramming;
	}

	/**
	 * @param mixedIntegerProgramming
	 *            the mixedIntegerProgramming to set
	 */
	public void setMixedIntegerProgramming(boolean mixedIntegerProgramming) {
		this.mixedIntegerProgramming = mixedIntegerProgramming;
	}

	/**
	 * @return the useCodedErrors
	 */
	public boolean isUseCodedErrors() {
		return useCodedErrors;
	}

	/**
	 * @param useCodedErrors
	 *            the useCodedErrors to set
	 */
	public void setUseCodedErrors(boolean useCodedErrors) {
		this.useCodedErrors = useCodedErrors;
	}

	/**
	 * @return the useGradientErrors
	 */
	public boolean isUseGradientErrors() {
		return useGradientErrors;
	}

	/**
	 * @param useGradientErrors
	 *            the useGradientErrors to set
	 */
	public void setUseGradientErrors(boolean useGradientErrors) {
		this.useGradientErrors = useGradientErrors;
	}

	/**
	 * @return the a
	 */
	public static float getA() {
		return a;
	}

	/**
	 * @param a
	 *            the a to set
	 */
	public static void setA(float a) {
		FLIPSOD_Maximize.a = a;
	}

	/**
	 * @return the b
	 */
	public static float getB() {
		return b;
	}

	/**
	 * @param b
	 *            the b to set
	 */
	public static void setB(float b) {
		FLIPSOD_Maximize.b = b;
	}

	/**
	 * @return the d
	 */
	public static float getD() {
		return d;
	}

	/**
	 * @param d
	 *            the d to set
	 */
	public static void setD(float d) {
		FLIPSOD_Maximize.d = d;
	}

	/**
	 * @return the e
	 */
	public static float getE() {
		return e;
	}

	/**
	 * @param e
	 *            the e to set
	 */
	public static void setE(float e) {
		FLIPSOD_Maximize.e = e;
	}

	/**
	 * @return the simpleEstimationModel
	 */
	public SimpleEstimationModelName getSimpleEstimationModel() {
		return simpleEstimationModel;
	}

	/**
	 * @param simpleEstimationModel the simpleEstimationModel to set
	 */
	public void setSimpleEstimationModel(
			SimpleEstimationModelName simpleEstimationModel) {
		this.simpleEstimationModel = simpleEstimationModel;
	}

	/**
	 * @return the lambdaMaximumError
	 */
	public static double getLambdaMaximumError() {
		return lambdaMaximumError;
	}

	/**
	 * @param lambdaMaximumError the lambdaMaximumError to set
	 */
	public static void setLambdaMaximumError(double lambdaMaximumError) {
		FLIPSOD_Maximize.lambdaMaximumError = lambdaMaximumError;
	}

	/**
	 * @return the odMatrixMaximumError
	 */
	public static double getOdMatrixMaximumError() {
		return odMatrixMaximumError;
	}

	/**
	 * @param odMatrixMaximumError the odMatrixMaximumError to set
	 */
	public static void setOdMatrixMaximumError(double odMatrixMaximumError) {
		FLIPSOD_Maximize.odMatrixMaximumError = odMatrixMaximumError;
	}

	/**
	 * @return the arcCostUpdatingStrategy
	 */
	public ArcCostUpdatingStrategyName getArcCostUpdatingStrategy() {
		return arcCostUpdatingStrategy;
	}

	/**
	 * @param arcCostUpdatingStrategy the arcCostUpdatingStrategy to set
	 */
	public void setArcCostUpdatingStrategy(
			ArcCostUpdatingStrategyName arcCostUpdatingStrategy) {
		this.arcCostUpdatingStrategy = arcCostUpdatingStrategy;
	}
	
	/**
	 * @return the inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	/**
	 * The method used to do concert the whole FLIPSOD estimation.
	 * 
	 * @throws Exception
	 */
	public void doEstimation(String filePath) throws Exception
	{
		if ( !this.boolUsePETGyn &&
				arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.PET) )
		{
			throw new Exception("Cannot use FLIPSOD on a network that's not a project "+
					"on PETGyn and apply PETGyn to update the arc costs.");
		}
		
		File file = inputFile;
		
		try {
			// The maximum difference allowed between two different OD matrices
			// Controls if the method should carry on iterating or stop
			double epslon = FLIPSOD_Maximize.odMatrixMaximumError;
			double epslonArcFlows = FLIPSOD_Maximize.arcFlowsMaximumError;

			// The previous and actual OD matrices. Their differences
			// shall be minimized throughout the estimation process
			AutoMapValue previousODMatrix = null;
			AutoMapValue actualODMatrix = null;
			
			HashMap previousArcFlows = null;
			HashMap actualArcFlows = null;
			
			// The solution analyser object
			SolutionAnalyser sa = new SolutionAnalyser();

			iterationsCounter = 0;
			
			// Make a first estimation and generate an OD matrix
			if (arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.BPR))
			{
				// If using the BPR function to update arc costs, calculate
				// the costs according to the available flows on arcs
				// (zero or the link capacity if none is available)
				cleanInitLP();
				cleanFLIPSODProblemData();
				doCalculateArcCostsFLIPSOD();
			}
			
			// Generate routes and transform arc costs in route costs
			doFLIPSODPreprocessing();
			
			// Use M3 or M8 to do the simple (a.k.a. one-step) estimation?
			if (this.simpleEstimationModel.equals(SimpleEstimationModelName.M3))
				doFLIPSODSimpleEstimationM3(filePath);
			else
				doFLIPSODSimpleEstimationM8(filePath);
			
			updateLinkFlowsFLIPSOD();
			
			//System.exit(0);
			
			if (this.boolUsePETGyn)
			{
				// Use the last added solution added
				int solutionNumber = solutionPool.getArrSolutionLambda().size();
				
				// Generate the route and OD matrix files to this preliminary estimation
				generateRoutesAndODMatrixFiles(filePath, solutionNumber);
			}
			
			// If using PETGyn, call it to update the arc costs
			if (arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.PET))
			{
				doFLIPSODPETGynUpdateProblem(false, filePath);
			}
			
			System.out.println("Results generated for first one-step execution.");
			showFLIPSODProblemData();
			//System.out.println();
			
			iterationsCounter++;

			//previousODMatrix = (AutoMapValue) solutionPool.getArrSolutionODMatrix().get(0);
			//System.out.println(solutionPool.getODMatrixValues(previousODMatrix));
			previousArcFlows = (HashMap) solutionPool.getArrSolutionLinkCounts().get(0);
			System.out.println(solutionPool.getLinkCountsValues(previousArcFlows));
			generateFLIPSODGraph();
			analyseFLIPSODSolution();
			
			double dblArcFlowsDifference = Double.POSITIVE_INFINITY;
			
			//while (actualODMatrix == null ||
					//sa.compareODMatricesRMSE(previousODMatrix, actualODMatrix, odPairs) > epslon)
			
			// Iterate until the difference between the previous and
			// current OD matrices estimated are sufficiently small
			//while (actualArcFlows == null ||
			//		sa.compareArcFlowsRMSE(previousArcFlows, actualArcFlows) > epslonArcFlows)
			while (true)
			{
				// Check the loop conditions
				if (actualArcFlows!=null)
				{
					if (this.boolUsePETGyn)
					{
						if (sa.compareArcFlowsRMSE_PETGyn(previousArcFlows, actualArcFlows) <=
								epslonArcFlows)
							break;
					} else
					{
						if (sa.compareArcFlowsRMSE(previousArcFlows, actualArcFlows) <=
								epslonArcFlows)
							break;
					}
				}
				
				// Clean the past estimations and problem data
				// and calculate the arc costs when using the BPR function
				if (arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.BPR))
				{
					// Clean up the estimations
					cleanFLIPSODProblemData();
					cleanInitLP();
					
					// Calculate the arc costs
					// If using the BPR function, one needs:
					// - arc flows, arc capacities and arc costs (without traffic)
					// If using PET-Gyn to do the calculations, one needs:
					// - an OD matrix.
					System.out.println("Beginning arc costs calculation...");
					doCalculateArcCostsFLIPSOD();
					System.out.println("Arc costs calculation concluded.");
				}
				
				if (this.boolUsePETGyn)
				{
					// Use the last added solution added
					int solutionNumber = solutionPool.getArrSolutionLambda().size();
					
					// Generate the route and OD matrix files to this preliminary estimation
					generateRoutesAndODMatrixFiles(filePath, solutionNumber);
				}
				
				// If using PETGyn, call it to update the arc costs
				if (arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.PET))
				{
					doFLIPSODPETGynUpdateProblem(false, filePath);
				}
				
				// Preprocess the problem
				// Use the arc costs to generate route costs
				doFLIPSODPreprocessing();

				// Do the estimation one step each iteration
				System.out.println("Beginning estimation...");

				// Use M3 or M8 to do the simple (a.k.a. one-step) estimation?
				if (this.simpleEstimationModel.equals(SimpleEstimationModelName.M3))
					doFLIPSODSimpleEstimationM3(filePath);
				else
					doFLIPSODSimpleEstimationM8(filePath);
				
				System.out.println("Estimation concluded.");

				// Update the link flows according to the estimated values
				// in the solution pool
				updateLinkFlowsFLIPSOD();

				System.out.println("Iteration " + iterationsCounter++ + " concluded.");
				
				// Update the previous and actual OD matrices
				//if (actualODMatrix != null)
				//	previousODMatrix = actualODMatrix;
				
				// Update the previous and actual arc flows
				if (actualArcFlows != null)
					previousArcFlows = actualArcFlows;
				
				//actualODMatrix = (AutoMapValue) solutionPool.getArrSolutionODMatrix().get(0);
				actualArcFlows = (HashMap) solutionPool.getArrSolutionLinkCounts().get(0);
				
				//System.out.println("Previous OD matrix: "+previousODMatrix);
				//System.out.println("Actual OD matrix: "+actualODMatrix);
				
				if (this.boolUsePETGyn)
				{
					System.out.println(
							"Difference between arc flows: " +
							sa.compareArcFlowsRMSE_PETGyn(
									previousArcFlows,
									actualArcFlows
							)
					);
				} else
				{
					System.out.println(
							"Difference between arc flows: " +
							sa.compareArcFlowsRMSE(
									previousArcFlows,
									actualArcFlows
							)
					);
				}
				
				//System.out.println("Difference between matrices: "
				//		+ sa.compareODMatricesRMSE(previousODMatrix,
				//				actualODMatrix, odPairs));
			}
			System.out.println("Iterations concluded.");

			System.out.println();
			System.out.println("Results generated to last one-step execution.");
			generateFLIPSODGraph();
			System.out.println();

			// Final estimation was here
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Do one final estimation to generate all the intermediate
			// solutions
			System.out.println("Beginning last estimation...");
			
			//System.exit(0);
			
			if (this.boolUsePETGyn)
			{
				// Use the last added solution added
				int solutionNumber = solutionPool.getArrSolutionLambda().size();
				
				// Generate the route and OD matrix files to this preliminary estimation
				generateRoutesAndODMatrixFiles(filePath, solutionNumber);
			}
			
			// If using PETGyn, call it to update the arc costs
			if (arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.PET))
			{
				doFLIPSODPETGynUpdateProblem(false, filePath);
			}
			
			// Clean the past estimations and problem data
			// and calculate the arc costs when using the BPR function
			if (arcCostUpdatingStrategy.equals(ArcCostUpdatingStrategyName.BPR))
			{
				cleanFLIPSODProblemData();
				cleanInitLP();
				doCalculateArcCostsFLIPSOD();
			}
			
			doFLIPSODPreprocessing();
			doFLIPSODEstimation(filePath);
			updateLinkFlowsFLIPSOD();
			
			System.out.println("Last estimation concluded.");
			System.out.println("Final solutions obtained in last complete execution.");
			
			generateFLIPSODGraph();
			analyseFLIPSODSolution();
			
			filePath += "finalIteration/";
			
			writeSolutionPoolToFile(filePath);
		}
	}
	
	public void writeOneSolutionToFile(String filePath, int solutionNumber)
	{
		// Create the entries to output the solution to a file
		String solutions_Z = new String();
		String solutions_lambda = new String();
		String solutions_link_counts = new String();
		String solutions_od_matrix = new String();
		
		int i = solutionNumber-1;
		Double d = (Double) solutionPool.getArrSolutionM6().get(i);
		String str_Z = Long.toString( Long.valueOf( d.longValue() ) );
		
		String str_lambda = Double.toString(
				(Double) solutionPool.getArrSolutionLambda().get(i)
		);
		
		String str_link_counts = solutionPool.getLinkCountsValues(
				(HashMap) solutionPool.getArrSolutionLinkCounts().get(i)
		);
		
		String str_od_matrix = solutionPool.getODMatrixValues(
				(AutoMapValue) solutionPool.getArrSolutionODMatrix().get(i)
		);
		
		String str_model = solutionPool.getArrSolutionModel().get(i);
		
		str_Z = str_Z.replace(".", ",");
		str_lambda = str_lambda.replace(".", ",");
		
		solutions_Z = str_Z;
		solutions_lambda = str_lambda;
		solutions_link_counts = str_link_counts;
		solutions_od_matrix = str_od_matrix;
		
		System.out.println("solutionNumber: "+solutionNumber);
		
		String odFileName = solutionNumber+" - ODMatrix_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		String newOdFileName = solutionNumber+" - New_MatrixOD_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		String linkFileName = solutionNumber+" - LinkCounts_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		String transformedLinkFileName = solutionNumber+" - New_LinkCounts_"+
				idProjeto+"_solution_"+solutionNumber+".dat";
		
		String modelFileName = solutionNumber+" - Model_"+idProjeto+
				"_solution_"+solutionNumber+".dat";
		String transformedModelFileName = solutionNumber+" - New_Model_"+idProjeto+
				"_solution_"+solutionNumber+".dat";
		
		File fileODMatrix = new File( filePath + odFileName );
		File fileLinkCounts = new File( filePath + linkFileName );
		File fileModel = new File( filePath + modelFileName );
		
		try {
			// If the file containing the OD matrix, link counts or Z and lambda
			// exists, delete it and recreate the file.
			if (fileODMatrix.exists())
				fileODMatrix.delete();
			if (fileLinkCounts.exists())
				fileLinkCounts.delete();
			if (fileModel.exists())
				fileModel.delete();
			
			fileODMatrix.createNewFile();
			fileLinkCounts.createNewFile();
			fileModel.createNewFile();
			
			// Create a writer to the new, empty files
			PrintWriter outWriterODMatrix = null;
			PrintWriter outWriterLinkCounts = null;
			PrintWriter outWriterModel = null;
			
			try {
				outWriterODMatrix = new PrintWriter( new FileWriter(fileODMatrix) );
				outWriterLinkCounts = new PrintWriter( new FileWriter(fileLinkCounts) );
				outWriterModel = new PrintWriter( new FileWriter(fileModel) );
			} catch (FileNotFoundException e) {
				System.err.println("Erro ao abrir arquivo");
				System.exit(1);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
			
			// Append the data to the files
			outWriterLinkCounts.println(
					"Solution value: " + solutions_Z
			);
			outWriterLinkCounts.println(
					"Solution lambda: " + solutions_lambda
			);
			outWriterLinkCounts.println( "Solution's estimated link counts: " );
			outWriterLinkCounts.println( solutions_link_counts );
			outWriterLinkCounts.close();
			
			outWriterODMatrix.println( solutions_od_matrix );
			outWriterODMatrix.close();
			
			// The file model
			outWriterModel.println( str_model );
			outWriterModel.close();
			
			// If using PETGyn, transform the solutions in G' to G
			if (boolUsePETGyn)
			{
				// After creating the files, transform the OD matrices
				// in G' to OD matrices in G
				FixSolution f = new FixSolution();
				f.carregaMap(idProjeto);
				
				f.fixMatrixOD(
						filePath, odFileName, newOdFileName
				);
				
				// Also transform the link counts in G' in counts in G
				f.convertLinkEstimationsInGToGLine(
						filePath,
						linkFileName,
						transformedLinkFileName
				);
				
				// Order the entries in the link counts files
				// (both the ones before and after transformation)
				f.orderLinkEstimations(
						filePath,
						transformedLinkFileName,
						transformedLinkFileName
				);
				
				f.orderLinkEstimations(
						filePath,
						linkFileName,
						linkFileName
				);
				
				// Order the entries in the OD matrix file
				f.orderODMatrixEstimations(
						filePath,
						newOdFileName,
						newOdFileName
				);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes the solutions in the solution pool
	 * to a set of files in the filesystem.
	 */
	public void writeSolutionPoolToFile(String filePath)
	{
		// Create the entries to output the solutions to a file
		String[] solutions_Z = new String[solutionPool.getArrSolutionM6().size()];
		String[] solutions_lambda = new String[solutionPool.getArrSolutionLambda().size()];
		String[] solutions_link_counts = new String[solutionPool.getArrSolutionLinkCounts().size()];
		String[] solutions_od_matrix = new String[solutionPool.getArrSolutionODMatrix().size()];
		
		for (int i = 0; i < solutionPool.getArrSolutionLambda().size(); i++) {
			Double d = (Double) solutionPool.getArrSolutionM6().get(i);
			String str_Z = Long.toString( d.longValue() );
			
			String str_lambda = Double.toString(
					(Double) solutionPool.getArrSolutionLambda().get(i)
			);
			
			String str_link_counts = solutionPool.getLinkCountsValues(
					(HashMap) solutionPool.getArrSolutionLinkCounts().get(i)
			);
			
			String str_od_matrix = solutionPool.getODMatrixValues(
					(AutoMapValue) solutionPool.getArrSolutionODMatrix().get(i)
			);
			
			str_Z = str_Z.replace(".", ",");
			str_lambda = str_lambda.replace(".", ",");
			
			solutions_Z[i] = str_Z;
			solutions_lambda[i] = str_lambda;
			solutions_link_counts[i] = str_link_counts;
			solutions_od_matrix[i] = str_od_matrix;
		}
		
		// Print the solutions to an output file
		for (int i = 0; i < solutionPool.getArrSolutionLambda().size(); i++) {
			String odFileName = (i+1)+" - ODMatrix_"+idProjeto+
					"_solution_"+(i+1)+".dat";
			String newOdFileName = (i+1)+" - New_MatrixOD_"+idProjeto+
					"_solution_"+(i+1)+".dat";
			String linkFileName = (i+1)+" - LinkCounts_"+idProjeto+
					"_solution_"+(i+1)+".dat";
			String transformedLinkFileName = (i+1)+" - New_LinkCounts_"+
					idProjeto+"_solution_"+(i+1)+".dat";
			
			File fileODMatrix = new File( filePath + odFileName );
			File fileLinkCounts = new File( filePath + linkFileName );
			
			try {
				// If the path to the files doesn't exist, create it
				File fileTempPath = new File( filePath );
				if (!fileTempPath.exists())
					fileTempPath.mkdir();
				
				// If the file containing the OD matrix, link counts or Z and lambda
				// exists, delete it and recreate the file.
				if (fileODMatrix.exists())
					fileODMatrix.delete();
				if (fileLinkCounts.exists())
					fileLinkCounts.delete();
				
				//System.err.println( fileODMatrix.getPath() + fileODMatrix.getName() );
				fileODMatrix.createNewFile();
				fileLinkCounts.createNewFile();
				
				// Create a writer to the new, empty files
				PrintWriter outWriterODMatrix = null;
				PrintWriter outWriterLinkCounts = null;
				
				try {
					outWriterODMatrix = new PrintWriter( new FileWriter(fileODMatrix) );
					outWriterLinkCounts = new PrintWriter( new FileWriter(fileLinkCounts) );
				} catch (FileNotFoundException e) {
					System.err.println("Erro ao abrir arquivo");
					System.exit(1);
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
				
				// Append the data to the files
				outWriterLinkCounts.println(
						"Solution value: " + solutions_Z[i]
				);
				outWriterLinkCounts.println(
						"Solution lambda: " + solutions_lambda[i]
				);
				outWriterLinkCounts.println( "Solution's estimated link counts: " );
				outWriterLinkCounts.println( solutions_link_counts[i] );
				outWriterLinkCounts.close();
				
				outWriterODMatrix.println( solutions_od_matrix[i] );
				outWriterODMatrix.close();
				
				// If using PETGyn, transform the solutions in G' to G
				if (boolUsePETGyn)
				{
					// After creating the files, transform the OD matrices
					// in G' to OD matrices in G
					FixSolution f = new FixSolution();
					f.carregaMap(idProjeto);
					
					f.fixMatrixOD(
							filePath, odFileName, newOdFileName
					);
					
					// Also transform the link counts in G' in counts in G
					f.convertLinkEstimationsInGToGLine(
							filePath,
							linkFileName,
							transformedLinkFileName
					);
					
					// Order the entries in the link counts files
					// (both the ones before and after transformation)
					f.orderLinkEstimations(
							filePath,
							transformedLinkFileName,
							transformedLinkFileName
					);
					
					f.orderLinkEstimations(
							filePath,
							linkFileName,
							linkFileName
					);
					
					// Order the entries in the OD matrix file
					f.orderODMatrixEstimations(
							filePath,
							newOdFileName,
							newOdFileName
					);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the bPRFunctionName
	 */
	public FLIPSOD.BPRFunctionName getBPRFunctionName() {
		return bprFunctionName;
	}

	/**
	 * @param bPRFunctionName the bPRFunctionName to set
	 */
	public void setBPRFunctionName(FLIPSOD.BPRFunctionName newBprFunctionName) {
		bprFunctionName = newBprFunctionName;
	}

	/**
	 * @return the idProjeto
	 */
	public int getIdProjeto() {
		return idProjeto;
	}

	/**
	 * @param idProjeto the idProjeto to set
	 */
	public void setIdProjeto(int idProjeto) {
		this.idProjeto = idProjeto;
	}

	/**
	 * @return the arcFlowsMaximumError
	 */
	public static double getArcFlowsMaximumError() {
		return arcFlowsMaximumError;
	}

	/**
	 * @param arcFlowsMaximumError the arcFlowsMaximumError to set
	 */
	public static void setArcFlowsMaximumError(double arcFlowsMaximumError) {
		FLIPSOD_Maximize.arcFlowsMaximumError = arcFlowsMaximumError;
	}
}
