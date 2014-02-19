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


public class RegionACompareArcFlows
{
	private static String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
	
	private static String estimatedFlowsFilePath = "";
	
	private static String firstFilePath = "";
	
	private static String secondFilePath = "";
	
	private static String estimatedFlowsFileName = "";
	
	private static String firstFileName = "";
	
	private static String secondFileName = "";
	
	private static HashMap<String, Double> estimatedArcFlows = null;
	
	private static HashMap<String, Double> firstArcFlows = null;
	
	private static HashMap<String, Double> secondArcFlows = null;
	
	public RegionACompareArcFlows()
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
		
		estimatedFlowsFilePath = baseFilePath;
		estimatedFlowsFileName = "Project_2_output_Estimations.dat";
		parseEstimatedLinkFlowsFile();
		
		//firstFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		//firstFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		//firstFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		//firstFilePath = baseFilePath+"9 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		firstFilePath = baseFilePath+"10 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		
		firstFileName = "1 - Right_Link_Counts_2_solution_1.dat";
		parseLinkFlowsFirstFile();
		/*
		secondFilePath = baseFilePath+"2 - solutions_errors_100%_BBPR_M8_ODError_150/";
		secondFileName = "1 - Right_Link_Counts_2_solution_1.dat";
		parseLinkFlowsSecondFile();
		
		difference = sa.compareArcFlowsRMSE(firstArcFlows, secondArcFlows);
		System.out.println("Difference %RMSE between");
		System.out.println(firstFilePath + firstFileName);
		System.out.println(secondFilePath + secondFileName + ":");
		System.out.println(difference);
		System.out.println();
		
		difference = sa.compareArcFlowsMAE(firstArcFlows, secondArcFlows);
		System.out.println("Difference %MAE between");
		System.out.println(firstFilePath + firstFileName);
		System.out.println(secondFilePath + secondFileName + ":");
		System.out.println(difference);
		System.out.println();
		*/
		difference = sa.compareArcFlowsRMSE(estimatedArcFlows, firstArcFlows);
		System.out.println("Difference %RMSE between");
		System.out.println(estimatedFlowsFilePath + estimatedFlowsFileName);
		System.out.println(firstFilePath + firstFileName + ":");
		System.out.println(difference);
		System.out.println();
		
		difference = sa.compareArcFlowsMAE(estimatedArcFlows, firstArcFlows);
		System.out.println("Difference %MAE between");
		System.out.println(estimatedFlowsFilePath + estimatedFlowsFileName);
		System.out.println(firstFilePath + firstFileName + ":");
		System.out.println(difference);
		System.out.println();
		/*
		difference = sa.compareArcFlowsRMSE(estimatedArcFlows, secondArcFlows);
		System.out.println("Difference %RMSE between");
		System.out.println(estimatedFlowsFilePath + estimatedFlowsFileName);
		System.out.println(secondFilePath + secondFileName + ":");
		System.out.println(difference);
		System.out.println();
		
		difference = sa.compareArcFlowsMAE(estimatedArcFlows, secondArcFlows);
		System.out.println("Difference %MAE between");
		System.out.println(estimatedFlowsFilePath + estimatedFlowsFileName);
		System.out.println(secondFilePath + secondFileName + ":");
		System.out.println(difference);
		System.out.println();
		*/
	}
	
	private static void parseEstimatedLinkFlowsFile()
	{
		BufferedReader inReader = null;
        String line;
        
        // Create the input reader
        try {
            inReader = new BufferedReader(
            		new FileReader(estimatedFlowsFilePath + estimatedFlowsFileName)
            );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + estimatedFlowsFileName + " nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        estimatedArcFlows = new HashMap<String,Double>();
        boolean linkCountsEstimationsAreInNextLine = false;
        
        try {
            System.out.println("Carregando estimações nos arcos...");
            while ((line = inReader.readLine()) != null)
            {
            	// Read the file until the # is found
            	if (line.equalsIgnoreCase("#"))
            	{
            		linkCountsEstimationsAreInNextLine = true;
            		continue;
            	}
            	
            	// When the # is found, the next line contains the
            	// link counts estimations
            	if (linkCountsEstimationsAreInNextLine)
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
                        
                        estimatedArcFlows.put(from+"->"+to,
                        		Double.parseDouble(flow)
                        );
                    }
                    break;
            	}
            }
            
            // Finished
            inReader.close();
            System.out.println("Estimações nos arcos carregadas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
	}
	
	private static void parseLinkFlowsFirstFile()
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
        
        firstArcFlows = new HashMap<String,Double>();
        
        try {
            System.out.println("Carregando estimações nos arcos...");
            while ((line = inReader.readLine()) != null)
            {
            	String from = "";
                String to = "";
                String flow = "";
                
                from = line.split("->")[0].trim();
                to = line.split("->")[1].split(":")[0].trim();
                flow = line.split(" ")[1];
                
                firstArcFlows.put(from+"->"+to,
                		Double.parseDouble(flow)
                );
            }
            
            // Finished
            inReader.close();
            System.out.println("Estimações nos arcos carregadas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
	}
	
	private static void parseLinkFlowsSecondFile()
	{
		BufferedReader inReader = null;
        String line;
        
        // Create the input reader
        try {
            inReader = new BufferedReader( new FileReader(secondFilePath + secondFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo " + secondFileName + " nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        secondArcFlows = new HashMap<String,Double>();
        
        try {
            System.out.println("Carregando estimações nos arcos...");
            while ((line = inReader.readLine()) != null)
            {
            	String from = "";
                String to = "";
                String flow = "";
                
                from = line.split("->")[0].trim();
                to = line.split("->")[1].split(":")[0].trim();
                flow = line.split(" ")[1];
                
                secondArcFlows.put(from+"->"+to,
                		Double.parseDouble(flow)
                );
            }
            
            // Finished
            inReader.close();
            System.out.println("Estimações nos arcos carregadas!");
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
        }
	}
}
