/**
 * 
 */
package tests;

import java.io.File;

import patch.CleanUpUnusedFilesRegionAPatch;
import patch.GenerateLinkCountsFromRouteCountsPatch;
import postprocessing.GraphLinkRegionAVisualization;
import postprocessing.GraphRouteRegionAVisualization;

import v7.FLIPSOD;
import v7.FLIPSOD.ArcCostUpdatingStrategyName;
import v7.FLIPSOD.BPRFunctionName;
import v7.FLIPSOD.MethodClassName;
import v7.FLIPSOD.SimpleEstimationModelName;

/**
 * @author iacer
 *
 */
public class RegionATestBed
{
	public static void main(String[] args)
	{
		String inputFilePath = "/home/iacer/workspace/FLPM2_cplex/src/instances/v7/Region A/";
		String inputFileName = "Regiao-A_LinkCounts100%_ODCounts100%.dat";
		File file = new File (inputFilePath + inputFileName);
		
		//String outputFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		ElapsedTimeTest.init();
		ElapsedTimeTest.setTrafficNetworkName("Region A");
		ElapsedTimeTest.setFileName("Test_Region A.txt");
		ElapsedTimeTest.setFilePath("/home/iacer/workspace/FLPM2_cplex/");
		
		doTestbedRegionA(file);
	}
	/**
	 * This method should be invoked to perform a testbed
	 * with region A (PETGyn's) according to different parameter values.
	 * @param file
	 */
	public static void doTestbedRegionA(File file)
	{
		BPRFunctionName bpr;
		String firstTestFilePath, secondTestFilePath, thirdTestFilePath;
		String fourthTestFilePath, fifthTestFilePath, sixthTestFilePath;
		String seventhTestFilePath, eighthTestFilePath, ninethTestFilePath;
		String tenthTestFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		// Apply these tests on region A with different configuration parameters
		
		// --------------------------------------------------------------
		// First, use the BPR function with the USA parameters      - PENDING
		// Use the maximum and optimal error measurements           - PENDING
		// --------------------------------------------------------------
		
		bpr = BPRFunctionName.BPR;
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BPR_M3_ODError_1/";
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BPR_M3_ODError_1/";
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		
		/*
		System.out.println("Before first test...");
		firstTest(file, firstTestFilePath, bpr, false);
		*/
		
		// ---------------------------------------------------------------------
		// ---------- First test described in master thesis ( TR1 ) ------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName( "TR1" );
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before second test...");
		secondTest(file, secondTestFilePath, bpr, false);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		// ---------------------------------------------------------------------
		// ------ Finished first test described in master thesis ( TR1 ) -------
		// ---------------------------------------------------------------------
		
		/*
		System.out.println("Before third test...");
		thirdTest(file, thirdTestFilePath, bpr, false);
		*/
		
		// ---------------------------------------------------------------------
		// ---------- Second test described in master thesis ( TR2 ) -----------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName( "TR2" );
		
		// Start the test clock
		ElapsedTimeTest.start();
		
		System.out.println("Before fourth test...");
		fourthTest(file, fourthTestFilePath, bpr, false);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		// ---------------------------------------------------------------------
		// ------ Finished second test described in master thesis ( TR2 ) ------
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------
		// Use PETGyn to calculate the arc costs          - PENDING
		// Use the maximum and optimal error measurements - PENDING
		// ---------------------------------------------------
		
		fifthTestFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		sixthTestFilePath = baseFilePath+"6 - solutions_errors_100%_PET_M3_ODError_1/";
		seventhTestFilePath = baseFilePath+"7 - solutions_optimal_errors_PET_M8_ODError_150/";
		eighthTestFilePath = baseFilePath+"8 - solutions_optimal_errors_PET_M3_ODError_1/";
		
		// ---------------------------------------------------------------------
		// ---------- Third test described in master thesis ( TR3 ) ------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName( "TR3" );
		ElapsedTimeTest.start(); // Start the test clock
		
		System.out.println("Before fifth test...");
		fifthTest(file, fifthTestFilePath, bpr);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		// ---------------------------------------------------------------------
		// ------ Finished third test described in master thesis ( TR3 ) -------
		// ---------------------------------------------------------------------
		
		/*
		System.out.println("Before sixth test...");
		sixthTest(file, sixthTestFilePath, bpr);
		System.out.println("Before seventh test...");
		seventhTest(file, seventhTestFilePath, bpr);
		System.out.println("Before eighth test...");
		eightTest(file, eighthTestFilePath, bpr);
		*/
		
		// ---------------------------------------------------------------------
		// Secondly, use the BPR function with the Japan/Holland parameters
		// ---------------------------------------------------------------------
		// Use the maximum error measurements and a gradient function
		// to generate the error measurements                          - PENDING
		// ---------------------------------------------------------------------
		
		bpr = BPRFunctionName.UBPR;
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_UBPR_M3_ODError_1/";
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_UBPR_M8_ODError_150/";
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_UBPR_M3_ODError_1/";
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_UBPR_M8_ODError_150/";
		/*
		System.out.println("Before first test...");
		firstTest(file, firstTestFilePath, bpr, false);
		System.out.println("Before second test...");
		secondTest(file, secondTestFilePath, bpr, false);
		System.out.println("Before third test...");
		thirdTest(file, thirdTestFilePath, bpr, true);
		System.out.println("Before fourth test...");
		fourthTest(file, fourthTestFilePath, bpr, true);
		*/
		
		// ------------------------------------------------------------------------
		// Finally, use the BPR function with the (to be) Brazilian parameters
		// ------------------------------------------------------------------------
		// Use the maximum error measurements and a gradient function
		// to generate the error measurements                             - PENDING
		// ------------------------------------------------------------------------
		bpr = BPRFunctionName.BBPR;
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BBPR_M3_ODError_1/";
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BBPR_M8_ODError_150/";
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BBPR_M3_ODError_1/";
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BBPR_M8_ODError_150/";
		/*
		System.out.println("Before first test...");
		firstTest(file, firstTestFilePath, bpr, false);
		System.out.println("Before second test...");
		secondTest(file, secondTestFilePath, bpr, false);
		System.out.println("Before third test...");
		thirdTest(file, thirdTestFilePath, bpr, true);
		System.out.println("Before fourth test...");
		fourthTest(file, fourthTestFilePath, bpr, true);
		*/
		
		// ---------------------------------------------------------------------------
		// Nineth and tenth tests
		// ---------------------------------------------------------------------------
		// Used to verify if the problem's structure allows multiple solutions
		// with the same total number of cars in the traffic network
		// ---------------------------------------------------------------------------
		
		bpr = BPRFunctionName.BPR;
		ninethTestFilePath = baseFilePath+"9 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		
		// ---------------------------------------------------------------------
		// ---------- Fourth test described in master thesis ( TR4 ) -----------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName( "TR4" );
		ElapsedTimeTest.start(); // Start the test clock
		
		System.out.println("Before nineth test...");
		ninethTest(file, ninethTestFilePath, bpr, false);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		// ---------------------------------------------------------------------
		// ------ Finished fourth test described in master thesis ( TR4 ) ------
		// ---------------------------------------------------------------------
		
		bpr = BPRFunctionName.BPR;
		tenthTestFilePath = baseFilePath+"10 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		
		// ---------------------------------------------------------------------
		// ---------- Fifth test described in master thesis ( TR5 ) ------------
		// ---------------------------------------------------------------------
		ElapsedTimeTest.setConfigurationName( "TR4" );
		ElapsedTimeTest.start(); // Start the test clock
		
		System.out.println("Before tenth test...");
		tenthTest(file, tenthTestFilePath, bpr, false);
		
		// Stop the test clock
		ElapsedTimeTest.end();
		ElapsedTimeTest.writeResultsToFile();
		// ---------------------------------------------------------------------
		// ------ Finished fifth test described in master thesis ( TR5 ) -------
		// ---------------------------------------------------------------------
		
		bpr = BPRFunctionName.BPR;
		tenthTestFilePath = baseFilePath+"11 - errors-mapped-with-gradient-function_BPR_M8/";
		/*
		System.out.println("Before eleventh test "+
				"11 - errors-mapped-with-gradient-function_BPR_M8/"+" ...");
		tenthTest(file, tenthTestFilePath, bpr, true);
		*/
		
		// Generate updated link count files from route count files
		GenerateLinkCountsFromRouteCountsPatch.init();
		
		// After all the tests have been conducted, generate the route visualizations
		//GraphRouteRegionAVisualization routeVis = new GraphRouteRegionAVisualization();
		//routeVis.init();
		
		// After generating the route visualizations, generate the link visualizations
		// Generate the visualizations with absolute boundaries
		GraphLinkRegionAVisualization.setGenerateVisualizationsWithAbsoluteBoundaries(true);
		GraphLinkRegionAVisualization.setAbsMaxBoundary(2000d);
		GraphLinkRegionAVisualization.setAbsMinBoundary(0d);
		GraphLinkRegionAVisualization.init();
		
		// Generate visualizations with relative boundaries
		GraphLinkRegionAVisualization.setGenerateVisualizationsWithAbsoluteBoundaries(false);
		GraphLinkRegionAVisualization.init();
		
		// After everything, delete the unused files
		//CleanUpUnusedFilesRegionAPatch.init();
	}
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - errors of 100% (a, b, d and e)
	 * - maximum error between two OD matrices of 1
	 * - use the BPR function in order to calculate the arc costs
	 * - use M3 to cycle and calculate the arc costs
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 * @param boolUseGradientErrors Whether to use gradient errors or not
	 */
	public static void firstTest(File file, String filePath,
			BPRFunctionName bprFunctionName, boolean boolUseGradientErrors)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( boolUseGradientErrors );
		flipsod.setA( 1f );
		flipsod.setB( 1f );
		flipsod.setD( 1f );
		flipsod.setE( 1f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.05 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 1 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
		// Calculate user-equilibrium or system optimal
		// assignment
		flipsod.setUserEquilibrium( true );
		
		// Use mixed integer programming or continuous
		// optimization?
		flipsod.setMixedIntegerProgramming( false );
		
		// Use M3 or M8 to do the one-step estimation?
		flipsod.setSimpleEstimationModel(
				SimpleEstimationModelName.M3
		);
		
		// Shall we calculate the arc costs using PETGyn
		// or using the BPR function?
		flipsod.setArcCostUpdatingStrategy(
				ArcCostUpdatingStrategyName.BPR
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - errors of 100% (a, b, d and e)
	 * - maximum error between two OD matrices of 150
	 * - use the BPR function in order to calculate the arc costs
	 * - use M8 to cycle and calculate the arc costs
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 * @param boolUseGradientErrors Whether to use gradient errors or not
	 */
	public static void secondTest(File file, String filePath,
			BPRFunctionName bprFunctionName, boolean boolUseGradientErrors)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( boolUseGradientErrors );
		flipsod.setA( 1f );
		flipsod.setB( 1f );
		flipsod.setD( 1f );
		flipsod.setE( 1f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		//flipsod.setSmoothingMultiplier( 0.05 );
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.1 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 1 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
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
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - minimum possible errors (a, b, d and e)
	 * - maximum error between two OD matrices of 1
	 * - use the BPR function in order to calculate the arc costs
	 * - use M3 to cycle and calculate the arc costs
	 * - use 5 routes per OD pair
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 * @param boolUseGradientErrors Whether to use gradient errors or not
	 */
	public static void thirdTest(File file, String filePath,
			BPRFunctionName bprFunctionName, boolean boolUseGradientErrors)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( boolUseGradientErrors );
		flipsod.setA( 0.1000000f );
		flipsod.setB( 0.1000000f );
		flipsod.setD( 0.1000000f );
		flipsod.setE( 0.2500000f );
		
		//flipsod.setA( 0.1000000f );
		//flipsod.setB( 0.5500000f );
		//flipsod.setD( 0.1000000f );
		//flipsod.setE( 0.1000000f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.05 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 1 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 180 );
		
		// Calculate user-equilibrium or system optimal
		// assignment
		flipsod.setUserEquilibrium( true );
		
		// Use mixed integer programming or continuous
		// optimization?
		flipsod.setMixedIntegerProgramming( false );
		
		// Use M3 or M8 to do the one-step estimation?
		flipsod.setSimpleEstimationModel(
				SimpleEstimationModelName.M3
		);
		
		// Shall we calculate the arc costs using PETGyn
		// or using the BPR function?
		flipsod.setArcCostUpdatingStrategy(
				ArcCostUpdatingStrategyName.BPR
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - minimum possible errors (a, b, d and e)
	 * - maximum error between two OD matrices of 150
	 * - use the BPR function in order to calculate the arc costs
	 * - use M8 to cycle and calculate the arc costs
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 * @param boolUseGradientErrors Whether to use gradient errors or not
	 */
	public static void fourthTest(File file, String filePath,
			BPRFunctionName bprFunctionName, boolean boolUseGradientErrors)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( boolUseGradientErrors );
		flipsod.setA( 0.1000000f );
		flipsod.setB( 0.1000000f );
		flipsod.setD( 0.1000000f );
		flipsod.setE( 0.2500000f );
		
		//flipsod.setA( 0.1000000f );
		//flipsod.setB( 0.5500000f );
		//flipsod.setD( 0.1000000f );
		//flipsod.setE( 0.1000000f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.1 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 1 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
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
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - all errors of 100% (a, b, d and e)
	 * - maximum error between two OD matrices of 150
	 * - use PETGyn in order to calculate the arc costs
	 * - use M8 in order to do the one-step OD matrix estimation
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 */
	public static void fifthTest(File file, String filePath, BPRFunctionName bprFunctionName)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( false );
		flipsod.setA( 1f );
		flipsod.setB( 1f );
		flipsod.setD( 1f );
		flipsod.setE( 1f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.1 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 20 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
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
				ArcCostUpdatingStrategyName.PET
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - all errors of 100% (a, b, d and e)
	 * - maximum error between two OD matrices of 1
	 * - use PETGyn in order to calculate the arc costs
	 * - use M3 in order to do the one-step OD matrix estimation
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 */
	public static void sixthTest(File file, String filePath, BPRFunctionName bprFunctionName)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( false );
		flipsod.setA( 1f );
		flipsod.setB( 1f );
		flipsod.setD( 1f );
		flipsod.setE( 1f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.05 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 20 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
		// Calculate user-equilibrium or system optimal
		// assignment
		flipsod.setUserEquilibrium( true );
		
		// Use mixed integer programming or continuous
		// optimization?
		flipsod.setMixedIntegerProgramming( false );
		
		// Use M3 or M8 to do the one-step estimation?
		flipsod.setSimpleEstimationModel(
				SimpleEstimationModelName.M3
		);
		
		// Shall we calculate the arc costs using PETGyn
		// or using the BPR function?
		flipsod.setArcCostUpdatingStrategy(
				ArcCostUpdatingStrategyName.PET
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - minimum error measurements (a, b, d and e)
	 * - maximum error between two OD matrices of 150
	 * - use PETGyn in order to calculate the arc costs
	 * - use M8 in order to do the one-step OD matrix estimation
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 */
	public static void seventhTest(File file, String filePath, BPRFunctionName bprFunctionName)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( false );
		flipsod.setA( 0.1000000f );
		flipsod.setB( 0.1000000f );
		flipsod.setD( 0.1000000f );
		flipsod.setE( 0.2500000f );
		
		//flipsod.setA( 0.1000000f );
		//flipsod.setB( 0.5500000f );
		//flipsod.setD( 0.1000000f );
		//flipsod.setE( 0.1000000f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.05 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 20 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
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
				ArcCostUpdatingStrategyName.PET
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - minimum error measurements (a, b, d and e)
	 * - maximum error between two OD matrices of 1
	 * - use PETGyn in order to calculate the arc costs
	 * - use M3 in order to do the one-step OD matrix estimation
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 */
	public static void eightTest(File file, String filePath, BPRFunctionName bprFunctionName)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( false );
		flipsod.setA( 0.1000000f );
		flipsod.setB( 0.1000000f );
		flipsod.setD( 0.1000000f );
		flipsod.setE( 0.2500000f );
		
		//flipsod.setA( 0.1000000f );
		//flipsod.setB( 0.5500000f );
		//flipsod.setD( 0.1000000f );
		//flipsod.setE( 0.1000000f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.05 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 20 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
		// Calculate user-equilibrium or system optimal
		// assignment
		flipsod.setUserEquilibrium( true );
		
		// Use mixed integer programming or continuous
		// optimization?
		flipsod.setMixedIntegerProgramming( false );
		
		// Use M3 or M8 to do the one-step estimation?
		flipsod.setSimpleEstimationModel(
				SimpleEstimationModelName.M3
		);
		
		// Shall we calculate the arc costs using PETGyn
		// or using the BPR function?
		flipsod.setArcCostUpdatingStrategy(
				ArcCostUpdatingStrategyName.PET
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - arbitrary errors (a, b, d and e)
	 * - maximum error between two OD matrices of 1
	 * - use the PET function in order to calculate the arc costs
	 * - use M8 to cycle and calculate the arc costs
	 * - use 5 routes per OD pair
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 * @param boolUseGradientErrors Whether to use gradient errors or not
	 */
	public static void ninethTest(File file, String filePath,
			BPRFunctionName bprFunctionName, boolean boolUseGradientErrors)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( boolUseGradientErrors );
		flipsod.setA( 1.0000000f );
		flipsod.setB( 0.1000000f );
		flipsod.setD( 1.0000000f );
		flipsod.setE( 1.0000000f );
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.1 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 20 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 180 );
		
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
				ArcCostUpdatingStrategyName.PET
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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
	
	/**
	 * Test FLIPSOD on region A with the following inputs:
	 * 
	 * - arbitrary errors (a, b, d and e)
	 * - maximum error between two OD matrices of 1
	 * - use the PET function in order to calculate the arc costs
	 * - use M8 to cycle and calculate the arc costs
	 * - use 5 routes per OD pair
	 * 
	 * @param file The input file with the problem specification
	 * @param filePath The file path to write the output files to
	 * @param bprFunctionName The BRP function version to use (BPR, UBPR or BBPR)
	 * @param boolUseGradientErrors Whether to use gradient errors or not
	 */
	public static void tenthTest(File file, String filePath,
			BPRFunctionName bprFunctionName, boolean boolUseGradientErrors)
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
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( boolUseGradientErrors );
		flipsod.setA( 0.1000000f );
		flipsod.setB( 1.0000000f );
		flipsod.setD( 1.0000000f );
		flipsod.setE( 1.0000000f );
		
		//a = 0.1000000f
		//b = 0.1000000f
		//d = 0.1000000f
		//e = 0.2500000f
		
		// PETGyn's project id, when applicable
		flipsod.setIdProjeto( 2 );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.1 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 20 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 180 );
		
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
				ArcCostUpdatingStrategyName.PET
		);
		
		// Use the BPR function as given by the parameter
		flipsod.setBPRFunctionName( bprFunctionName );
		
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

/**
 * Minimum error values to the following configuration of region A:
 * BPR function
 * M8 to calculate arc costs
 * 5 routes per OD pair
 * 
 * a = 0.10000000 - alpha
 * b = 0.10000000 - Qij
 * d = 0.10000000 - Oi
 * e = 0.25000000 - Dj
 */