/**
 * 
 */
package tests;

import java.io.File;

import patch.CleanUpUnusedFilesCorridorNetworkPatch;
import postprocessing.GraphLinkCorridorNetworkVisualization;

import v7.FLIPSOD;
import v7.FLIPSOD.ArcCostUpdatingStrategyName;
import v7.FLIPSOD.BPRFunctionName;
import v7.FLIPSOD.MethodClassName;
import v7.FLIPSOD.SimpleEstimationModelName;

/**
 * @author iacer
 *
 */
public class CorridorNetworkTestBed
{
	
	private static ElapsedTimeTest elapsedTimeTest;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ElapsedTimeTest.init();
		ElapsedTimeTest.setTrafficNetworkName("The Corridor Network");
		ElapsedTimeTest.setFileName("Test_The Corridor Network.txt");
		ElapsedTimeTest.setFilePath("/home/iacer/workspace/FLPM2_cplex/");
		
		doTestFiftyPercentOfLinkCountsAvailable();
		doTestTwoThirdsOfLinkCountsAvailable();
		doTestAllLinkCountsAvailable();
		
		// After all the tests have been conducted, generate the link visualizations
		GraphLinkCorridorNetworkVisualization corridorVis =
			new GraphLinkCorridorNetworkVisualization();
		corridorVis.init();
		
		// Clean up the generated but unused files
		CleanUpUnusedFilesCorridorNetworkPatch cleanUp =
			new CleanUpUnusedFilesCorridorNetworkPatch();
		cleanUp.init();
	}
	
	/**
	 * Run the tests to the instances of the corridor network
	 * with 50% of the link counts available.
	 * 
	 * The tests are done for:
	 * - no target OD matrix;
	 * - alternative user-equilibrium target OD matrix;
	 * - correct target OD matrix;
	 * - small-error target OD matrix;
	 */
	public static void doTestFiftyPercentOfLinkCountsAvailable()
	{
		String inputFileName, inputFilePath, outputFilePath;
		File file;
		
		inputFilePath = "/home/iacer/workspace/FLPM2_cplex/src/instances/v7/"+
				"Corridor Network/Link counts in 50% of arcs/";
		
		// ---------------------------------------------------------------------
		// --------------------------- First test ------------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"No Trip Table, 50% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 50% link counts, no OD entries test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_50%LinkCounts_NoODTripTable/";
		
		inputFileName = "Corridor Network_LinkCount10%_noTripTable (no OD entries).dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Second test -----------------------------
		// ---------------------------------------------------------------------
		
		// Configurations
		ElapsedTimeTest.setConfigurationName(
				"Alternative UE Trip Table, 50% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 50% link counts, alternativeEquilibriumTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_50%LinkCounts_AlternativeEquilibriumTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_alternativeEquilibriumTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Third test ------------------------------
		// ---------------------------------------------------------------------
		
		// Configurations
		ElapsedTimeTest.setConfigurationName(
				"Correct Trip Table, 50% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 50% link counts, correctTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_50%LinkCounts_CorrectTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_correctTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Fourth test -----------------------------
		// ---------------------------------------------------------------------
		
		// Configurations
		ElapsedTimeTest.setConfigurationName(
				"Small Error Trip Table, 50% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 50% link counts, smallErrorTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_50%LinkCounts_SmallErrorTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_smallErrorTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
	}
	
	/**
	 * Run the tests to the instances of the corridor network
	 * with 67% of the link counts available.
	 * 
	 * The tests are done for:
	 * - no target OD matrix;
	 * - alternative user-equilibrium target OD matrix;
	 * - correct target OD matrix;
	 * - small-error target OD matrix;
	 */
	public static void doTestTwoThirdsOfLinkCountsAvailable()
	{
		String inputFileName, inputFilePath, outputFilePath;
		File file;
		inputFilePath = "/home/iacer/workspace/FLPM2_cplex/src/instances/v7/"+
				"Corridor Network/Link counts in 67% of arcs/";
		
		// ---------------------------------------------------------------------
		// --------------------------- First test ------------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"No Trip Table, 67% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 67% link counts, no OD entries test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_67%LinkCounts_NoODTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_noTripTable (no OD entries).dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Second test -----------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"Alternative UE Trip Table, 67% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 67% link counts, alternativeEquilibriumTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_67%LinkCounts_AlternativeEquilibriumTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_alternativeEquilibriumTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Third test ------------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"Correct Trip Table, 67% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 67% link counts, correctTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_67%LinkCounts_CorrectTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_correctTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Fourth test -----------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"Small Error Trip Table, 67% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 67% link counts, smallErrorTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_67%LinkCounts_SmallErrorTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_smallErrorTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
	}
	
	/**
	 * Run the tests to the instances of the corridor network
	 * with 100% of the link counts available.
	 * 
	 * The tests are done for:
	 * - no target OD matrix;
	 * - alternative user-equilibrium target OD matrix;
	 * - correct target OD matrix;
	 * - small-error target OD matrix;
	 */
	public static void doTestAllLinkCountsAvailable()
	{
		String inputFileName, inputFilePath, outputFilePath;
		File file;
		inputFilePath = "/home/iacer/workspace/FLPM2_cplex/src/instances/v7/"+
				"Corridor Network/Link counts in 100% of arcs/";
		
		// Configurations
		//ElapsedTimeTest.setFilePath(inputFilePath);
		
		// ---------------------------------------------------------------------
		// --------------------------- First test ------------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"No Trip Table, 100% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 100% link counts, no OD entries test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_100%LinkCounts_NoODTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_noTripTable (no OD entries).dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Second test -----------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"Alternative UE Trip Table, 100% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 100% link counts, alternativeEquilibriumTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_100%LinkCounts_AlternativeEquilibriumTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_alternativeEquilibriumTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Third test ------------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"Correct Trip Table, 100% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 100% link counts, correctTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_100%LinkCounts_CorrectTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_correctTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		
		// ---------------------------------------------------------------------
		// --------------------------- Fourth test -----------------------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName(
				"Small Error Trip Table, 100% of link counts available"
		);
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before 100% link counts, smallErrorTripTable test...");
		outputFilePath = "/home/iacer/workspace/FLPM2_cplex/"+
				"CorridorNetwork_100%LinkCounts_SmallErrorTripTable/";
		
		// The output test file path
		//ElapsedTimeTest.setFilePath(outputFilePath);
		
		inputFileName = "Corridor Network_LinkCount10%_ODCounts20%_smallErrorTripTable.dat";
		file = new File (inputFilePath + inputFileName);
		doCorridorNetworkTest(file, outputFilePath);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
	}
	
	/**
	 * Corridor network
	 * 
	 * @param file
	 * @param filePath
	 * @param bprFunctionName
	 */
	private static void doCorridorNetworkTest(File file, String filePath)
	{
		// Estimate using FLIPSOD
		FLIPSOD flipsod = new FLIPSOD();
		
		// Which version of the method to use?
		flipsod.setMethodClassName(
				MethodClassName.FuzzyLPModel_Original
		);
		
		// Do not use the errors in the input file.
		// Rather, use the errors described in the
		// ErrorValueMapping object
		flipsod.setUseCodedErrors( false );
		flipsod.setUseGradientErrors( false );
		//flipsod.setA( 0.000f );
		//flipsod.setB( 0.000f );
		flipsod.setA( 0.100f );
		flipsod.setB( 0.200f );
		flipsod.setD( 1f );
		flipsod.setE( 1f );
		
		// PETGyn's project id, when applicable
		//flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.05 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.05 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 0.05 );
		
		// Calculate user-equilibrium or system optimal
		// assignment
		flipsod.setUserEquilibrium( true );
		
		// Use mixed integer programming or continuous
		// optimization?
		flipsod.setMixedIntegerProgramming( false );
		
		// Use M3 or M8 to do the one-step estimation?
		flipsod.setSimpleEstimationModel(
				SimpleEstimationModelName.M8
		);
		
		// Shall we calculate the arc costs using PETGyn
		// or using the BPR function?
		flipsod.setArcCostUpdatingStrategy(
				ArcCostUpdatingStrategyName.BPR
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( BPRFunctionName.BPR );
		
		// Set the input file to use and load it
		flipsod.setInputFile(file);
		flipsod.loadFile();
		
		try {
			// Do the estimation
			flipsod.doEstimation(filePath);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
