package postprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import odEstimation.AutoMapValue;
import odEstimation.ODMatrix;


public class RegionACompareODMatrices
{
	private static String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
	
	private static String observedODMatrixFilePath = "";
	
	private static String firstFilePath = "";
	
	private static String observedODMatrixFileName = "";
	
	private static String firstFileName = "";
	
	private static AutoMapValue observedODMatrix = null;
	
	private static ODMatrix firstODMatrix = null;
	
	public RegionACompareODMatrices()
	{
		init();
	}
	
	public static void main(String[] args)
	{
		init();
	}
	
	public static void init()
	{
		double difference;
		SolutionAnalyser sa = new SolutionAnalyser();
		
		observedODMatrixFilePath = baseFilePath;
		observedODMatrixFileName = "Project_2_output_Estimations.dat";
		parseObservedODMatrixFile();
		
		//firstFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		//firstFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		//firstFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		//firstFilePath = baseFilePath+"9 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		firstFilePath = baseFilePath+"10 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		
		firstFileName = "1 - New_MatrixOD_2_solution_1.dat";
		parseODDemandsFirstFile();
		
		difference = sa.compareODMatricesRMSE(observedODMatrix, firstODMatrix);
		System.out.println("Difference %RMSE(OD) between");
		System.out.println(observedODMatrixFilePath + observedODMatrixFileName);
		System.out.println(firstFilePath + firstFileName + ":");
		System.out.println(difference);
		System.out.println();
		
		difference = sa.compareODMatricesMAE(observedODMatrix, firstODMatrix);
		System.out.println("Difference %MAE(OD) between");
		System.out.println(observedODMatrixFilePath + observedODMatrixFileName);
		System.out.println(firstFilePath + firstFileName + ":");
		System.out.println(difference);
		System.out.println();
	}
	
	private static void parseObservedODMatrixFile()
	{
		BufferedReader inReader = null;
        String line;
        
        // Create the input reader
        try {
            inReader = new BufferedReader(
            		new FileReader(observedODMatrixFilePath + observedODMatrixFileName)
            );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + observedODMatrixFileName + " nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        observedODMatrix = new AutoMapValue();
        boolean odEstimationsAreInNextLine = false;
        
        try {
            System.out.println("Carregando estimações da matriz OD observada...");
            while ((line = inReader.readLine()) != null)
            {
            	// Read the file until the # is found
            	if (line.equalsIgnoreCase("#"))
            	{
            		odEstimationsAreInNextLine = true;
            		// read one more line
            		line = inReader.readLine();
            		continue;
            	}
            	
            	// When the # is found, the next line contains the
            	// OD estimations
            	if (odEstimationsAreInNextLine)
            	{
            		String[] entries = line.split(" ");
            		String from, to, flow;
                    
                    // For each existing estimation, add it
                    // to the hashmap
                    for (int i=0; i<entries.length; i++)
                    {
                    	String entry = entries[i];
                    	
                    	//System.out.println("entry: "+entry);
                    	
                    	from = entry.split(",")[0].trim().replace("(","");
                        to = entry.split(",")[1].trim();
                        flow = entry.split(",")[2].trim();
                        
                        observedODMatrix.get(from).get(to).set(flow);
                        //observedODMatrix.put(from+"->"+to,
                        //		Double.parseDouble(flow)
                        //);
                    }
                    break;
            	}
            }
            
            // Finished
            inReader.close();
            System.out.println("Estimações da matriz OD observada carregadas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
	}
	
	private static void parseODDemandsFirstFile()
	{
		BufferedReader inReader = null;
        String line;
        
        // Create the input reader
        try {
            inReader = new BufferedReader( new FileReader(firstFilePath + firstFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + firstFileName + " nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        firstODMatrix = new ODMatrix();
        
        try {
            System.out.println("Carregando estimações da matriz OD estimada...");
            while ((line = inReader.readLine()) != null)
            {
            	String from = "";
                String to = "";
                String flow = "";
                
                from = line.split(",")[0].replace("(", "").trim();
                to = line.split(",")[1].trim();
                flow = line.split(",")[2].replace(")", "").trim();
                
                firstODMatrix.setCount(from, to, Double.parseDouble(flow));
                //firstODMatrix.put(from+"->"+to,
                //		Double.parseDouble(flow)
                //);
            }
            
            // Finished
            inReader.close();
            System.out.println("Estimações da matriz OD estimada carregadas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
	}
}
