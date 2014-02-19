/**
 * 
 */
package patch;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author iacer
 * 
 * A patch that deletes all the unused files and directories
 * to the Corridor Network.
 */
public class CleanUpUnusedFilesCorridorNetworkPatch
{
	/**
	 * Constructor
	 */
	public CleanUpUnusedFilesCorridorNetworkPatch()
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
	 * Clean up the unused files generated to region A
	 */
	public static void init()
	{
		System.out.print("Beginning to clean up unused files... ");
		
		String outputFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		// 50% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_NoODTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_AlternativeEquilibriumTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_CorrectTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_50%LinkCounts_SmallErrorTripTable/";
		doCleanUpFiles(outputFilePath);
		
		// 67% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_NoODTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_AlternativeEquilibriumTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_CorrectTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_67%LinkCounts_SmallErrorTripTable/";
		doCleanUpFiles(outputFilePath);
		
		// 100% of link counts available
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_NoODTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_AlternativeEquilibriumTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_CorrectTripTable/";
		doCleanUpFiles(outputFilePath);
		
		outputFilePath = baseFilePath+"CorridorNetwork_100%LinkCounts_SmallErrorTripTable/";
		doCleanUpFiles(outputFilePath);
		
		System.out.println("Succeeded!");
	}
	
	/**
	 * Delete directory and subdirectories recursively
	 * 
	 * @param path The directory path
	 * @return boolean True if deleted, false otherwise
	 */
	public static boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			
			System.out.println("files.length"+files.length);
			
			for(int i=0; i<files.length; i++)
			{
				//System.out.println("files[i].isDirectory(): "+files[i].isDirectory());
				if(files[i].isDirectory())
				{
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	/**
	 * Clean up the unused files in the given path
	 * 
	 * @param filePath The path to delete files from
	 */
	public static void doCleanUpFiles(String filePath)
	{
		File file = new File(filePath);
		if (file.isDirectory())
		{
			// Iterate through the files in the directory
			String[] fileList = file.list();
			for (int i=0; i<fileList.length; i++)
			{
				File newFile = new File(filePath+fileList[i]);
				
				if (fileList[i].indexOf("finalIteration")>=0)
					deleteDirectory(newFile);
				
				// Delete the files containing the link counts in G'
				//if (newFile.getName().indexOf(" - LinkCounts_")>=0)
				//	newFile.delete();
				
				// Delete the files containing the OD matrices in G'
				if (newFile.getName().indexOf(" - MatrixOD_")>=0)
					newFile.delete();
				//if (newFile.getName().indexOf(" - ODMatrix_")>=0)
				//	newFile.delete();
				
				// Delete the files containing the wrong link counts in G
				//if (newFile.getName().indexOf(" - New_LinkCounts_")>=0)
				//	newFile.delete();
				
				// Delete the files containing the route counts in G'
				//if (newFile.getName().indexOf(" - Rotas_")>=0)
				//	newFile.delete();
			}
		}
	}
}
