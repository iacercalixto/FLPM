/**
 * 
 */
package patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author iacer
 * 
 * A patch that generates a file containing outputted link counts
 * from a file containing route counts to region A.
 */
public class GenerateLinkCountsFromRouteCountsPatch
{
	/**
	 * Constructor
	 */
	public GenerateLinkCountsFromRouteCountsPatch()
	{
		init();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		init();
	}
	
	/**
	 * Generate the visualizations to The Corridor Network testbed
	 */
	public static void init()
	{
		int idProjeto, solutionNumber;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		String[] folders = new String[19];
		
		// BPR
		String bprFirstTestFilePath, bprSecondTestFilePath;
		String bprThirdTestFilePath, bprFourthTestFilePath;
		String bprNinethTestFilePath, bprTenthTestFilePath;
		String bprEleventhTestFilePath;
		
		// UBPR
		String ubprFirstTestFilePath, ubprSecondTestFilePath;
		String ubprThirdTestFilePath, ubprFourthTestFilePath;
		
		// BBPR
		String bbprFirstTestFilePath, bbprSecondTestFilePath;
		String bbprThirdTestFilePath, bbprFourthTestFilePath;
		
		// PET
		String petFifthTestFilePath, petSixthTestFilePath;
		String petSeventhTestFilePath, petEighthTestFilePath;
		
		// BPR
		bprFirstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BPR_M3_ODError_1/";
		bprSecondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		//bprSecondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150_Maximize/";
		bprThirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BPR_M3_ODError_1/";
		//bprFourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		bprFourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150_Maximize/";
		
		// PET
		petFifthTestFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		petSixthTestFilePath = baseFilePath+"6 - solutions_errors_100%_PET_M3_ODError_1/";
		petSeventhTestFilePath = baseFilePath+"7 - solutions_optimal_errors_PET_M8_ODError_150/";
		petEighthTestFilePath = baseFilePath+"8 - solutions_optimal_errors_PET_M3_ODError_1/";
		
		// UBPR
		ubprFirstTestFilePath = baseFilePath+"1 - solutions_errors_100%_UBPR_M3_ODError_1/";
		ubprSecondTestFilePath = baseFilePath+"2 - solutions_errors_100%_UBPR_M8_ODError_150/";
		ubprThirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_UBPR_M3_ODError_1/";
		ubprFourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_UBPR_M8_ODError_150/";
		
		// BBPR
		bbprFirstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BBPR_M3_ODError_1/";
		bbprSecondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BBPR_M8_ODError_150/";
		bbprThirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BBPR_M3_ODError_1/";
		bbprFourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BBPR_M8_ODError_150/";
		
		// BPR again
		bprNinethTestFilePath = baseFilePath+"9 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		bprTenthTestFilePath = baseFilePath+"10 - solutions_arbitrary_errors_PET_M8_ODError_180/";
		
		bprEleventhTestFilePath = baseFilePath+"11 - errors-mapped-with-gradient-function_BPR_M8/";
		// Populate the folders array
		
		/*
		folders[0] = bprFirstTestFilePath;
		folders[1] = bprSecondTestFilePath;
		folders[2] = bprThirdTestFilePath;
		folders[3] = bprFourthTestFilePath;
		
		folders[4] = ubprFirstTestFilePath;
		folders[5] = ubprSecondTestFilePath;
		folders[6] = ubprThirdTestFilePath;
		folders[7] = ubprFourthTestFilePath;
		
		folders[8] = bbprFirstTestFilePath;
		folders[9] = bbprSecondTestFilePath;
		folders[10] = bbprThirdTestFilePath;
		folders[11] = bbprFourthTestFilePath;
		
		folders[12] = petFifthTestFilePath;
		folders[13] = petSixthTestFilePath;
		folders[14] = petSeventhTestFilePath;
		folders[15] = petEighthTestFilePath;
		
		folders[16] = bprNinethTestFilePath;
		
		folders[17] = bprTenthTestFilePath;
		
		folders[18] = bprEleventhTestFilePath;
		*/
		
		folders[0] = bprFourthTestFilePath;
		
		// Iterate through the folders
		for (int h=0; h>=0; h--)
		//for (int h=18; h>=0; h--)
		{
			File file = new File( folders[h] );
			if (file.isDirectory())
			{
				// Iterate through the files in the directory
				File[] fileList = file.listFiles();
				for (int i=0; i<fileList.length; i++)
				{
					// If it is a file representing a transformed route
					if (fileList[i].getName().indexOf("New_Rotas")>=0)
					{
						// File name in the format
						// 3 - New_Rotas_2_solution_3.dat
						// i.e.
						// SOLUTIONNUMBER - New_Rotas_IDPROJETO_solution_SOLUTIONNUMBER.dat
						
						// Parse the project id and the solution number
						solutionNumber = Integer.parseInt( fileList[i].getName().split(" ")[0] );
						idProjeto = Integer.parseInt(
								fileList[i].getName().split("_")[2]
						);
						
						String inputFileName = solutionNumber+" - New_Rotas_"+idProjeto+
								"_solution_"+solutionNumber+".dat";
				        String outputFileName = solutionNumber+" - Right_Link_Counts_"+idProjeto+
				        		"_solution_"+solutionNumber+".dat";
				        
						// Generate the link counts from the route counts
						generateLinkCountsFromRouteCounts(
								folders[h],
								idProjeto,
								solutionNumber,
								inputFileName,
								outputFileName
						);
					}
				}
			}
		}
	}
	
	/**
	 * Parses the route counts file, computes the link counts
	 * and generates an output link counts file.
	 * 
	 * @param filePath The path to the input and output files
	 * @param idProjeto The project id in region A
	 * @param solutionNumber The solution number
	 * @param inputFileName The route counts file name
	 * @param outputFileName The link counts file name
	 */
	public static void generateLinkCountsFromRouteCounts(String filePath,
			int idProjeto, int solutionNumber, String inputFileName, String outputFileName)
	{
		PrintWriter outWriter = null;
        BufferedReader inReader = null;
        String line;
        Double dblFlow;
        
        // Create the writer and reader to the input and output files
        try {
        	// If the output file already exists, recreate a new one
        	File temp = new File(filePath + outputFileName);
        	temp.delete();
        	temp.createNewFile();
        	temp = null;
        	
            outWriter = new PrintWriter( new FileWriter(filePath + outputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+outputFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            inReader = new BufferedReader( new FileReader(filePath + inputFileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+inputFileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.print("Beginning to create "+outputFileName+"... ");
        
        try {
        	HashMap<String, Double> hashLinkCounts = new HashMap<String, Double>();
        	
			while ((line = inReader.readLine()) != null) {
				//System.out.println("line: "+line);
				
				String[] routeParts = line.split(" ");
				
				// routeParts[0] has the route id in CPLEX's linear program
				// routeParts[1] is in the form (322_330_484_486_608_609_610_482_326)
				// routeParts[2] has the flow on the route (a double)
				
				dblFlow = Double.parseDouble( routeParts[2] );
				
				String[] nodes = routeParts[1].split("_");
				nodes[0] = nodes[0].replace("(", "").trim();
				nodes[ nodes.length-1 ] = nodes[ nodes.length-1 ].replace(")", "").trim();
				
				// Iterate from the second node until the last one
				for (int i=1; i<nodes.length; i++)
				{
					String strFrom = nodes[i-1];
					String strTo = nodes[i];
					Double currentCount = 0d;
					
					//System.out.println(strFrom+","+strTo);
					
					// If there is a previous value to the count, get it
					if (hashLinkCounts.containsKey(strFrom+","+strTo))
						currentCount = hashLinkCounts.get(strFrom+","+strTo);
					
					// Sum up the new value to the old one
					currentCount += dblFlow;
					
					// Update the hashMap
					hashLinkCounts.put(strFrom+","+strTo, currentCount);
				}
			}
			
			inReader.close();
			
			//System.out.println("hashLinkCounts.size(): "+hashLinkCounts.size());
			//System.out.println("hashLinkCounts: "+hashLinkCounts);
			
			// For each entry in the hashLinkCounts,
			// create a link in the output file
			Iterator<Entry<String, Double>> itLinkCounts = hashLinkCounts.entrySet().iterator();
			while (itLinkCounts.hasNext())
			{
				Map.Entry<String, Double> entry =
					(Map.Entry<String, Double>) itLinkCounts.next();
				
				// Get the link representation as (154,123)
				// and the flow on the link as 5435.5
				String strLink = entry.getKey();
				Double newDblFlow = entry.getValue();
				
				// Get the from and to nodes
				String strFrom = strLink.split(",")[0].replace("(", "").trim();
				String strTo = strLink.split(",")[1].replace(")", "").trim();
				
				// Generate the line to output to the output file
				String strOutput = strFrom+"->"+strTo+": "+ Double.toString(newDblFlow);
				
				//System.out.println("strOutput: "+hashLinkCounts);
				
				// Do it
				outWriter.println(strOutput);
			}
			
			outWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Succeeded!");
	}

}
