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
 * to region A.
 */
public class CleanUpUnusedFilesRegionAPatch
{
	/**
	 * Constructor
	 */
	public CleanUpUnusedFilesRegionAPatch()
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
		
		String firstTestFilePath, secondTestFilePath, thirdTestFilePath;
		String fourthTestFilePath, fifthTestFilePath, sixthTestFilePath;
		String seventhTestFilePath, eighthTestFilePath;
		String ninethTestFilePath, tenthTestFilePath;
		String eleventhTestFilePath;
		String baseFilePath = "/home/iacer/workspace/FLPM2_cplex/";
		
		// BPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BPR_M3_ODError_1/";
		doCleanUpFiles(firstTestFilePath);
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BPR_M8_ODError_150/";
		doCleanUpFiles(secondTestFilePath);
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BPR_M3_ODError_1/";
		doCleanUpFiles(thirdTestFilePath);
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BPR_M8_ODError_150/";
		doCleanUpFiles(fourthTestFilePath);
		
		// PET
		
		fifthTestFilePath = baseFilePath+"5 - solutions_errors_100%_PET_M8_ODError_150/";
		doCleanUpFiles(fifthTestFilePath);
		
		sixthTestFilePath = baseFilePath+"6 - solutions_errors_100%_PET_M3_ODError_1/";
		doCleanUpFiles(sixthTestFilePath);
		
		seventhTestFilePath = baseFilePath+"7 - solutions_optimal_errors_PET_M8_ODError_150/";
		doCleanUpFiles(seventhTestFilePath);
		
		eighthTestFilePath = baseFilePath+"8 - solutions_optimal_errors_PET_M3_ODError_1/";
		doCleanUpFiles(eighthTestFilePath);
		
		// UBPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_UBPR_M3_ODError_1/";
		doCleanUpFiles(firstTestFilePath);
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_UBPR_M8_ODError_150/";
		doCleanUpFiles(secondTestFilePath);
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_UBPR_M3_ODError_1/";
		doCleanUpFiles(thirdTestFilePath);
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_UBPR_M8_ODError_150/";
		doCleanUpFiles(fourthTestFilePath);
		
		// BBPR
		
		firstTestFilePath = baseFilePath+"1 - solutions_errors_100%_BBPR_M3_ODError_1/";
		doCleanUpFiles(firstTestFilePath);
		
		secondTestFilePath = baseFilePath+"2 - solutions_errors_100%_BBPR_M8_ODError_150/";
		doCleanUpFiles(secondTestFilePath);
		
		thirdTestFilePath = baseFilePath+"3 - solutions_optimal_errors_BBPR_M3_ODError_1/";
		doCleanUpFiles(thirdTestFilePath);
		
		fourthTestFilePath = baseFilePath+"4 - solutions_optimal_errors_BBPR_M8_ODError_150/";
		doCleanUpFiles(fourthTestFilePath);
		
		// BPR
		ninethTestFilePath = baseFilePath+"9 - solutions_arbitrary_errors_BPR_M8_ODError_180/";
		doCleanUpFiles(ninethTestFilePath);
		
		tenthTestFilePath = baseFilePath+"10 - solutions_arbitrary_errors_BPR_M8_ODError_180/";
		doCleanUpFiles(tenthTestFilePath);
		
		eleventhTestFilePath = baseFilePath+"11 - errors-mapped-with-gradient-function_BPR_M8/";
		doCleanUpFiles(eleventhTestFilePath);
		
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
				System.out.println("files[i].isDirectory(): "+files[i].isDirectory());
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
				if (newFile.getName().indexOf(" - LinkCounts_")>=0)
					newFile.delete();
				
				// Delete the files containing the OD matrices in G'
				if (newFile.getName().indexOf(" - MatrixOD_")>=0)
					newFile.delete();
				if (newFile.getName().indexOf(" - ODMatrix_")>=0)
					newFile.delete();
				
				// Delete the files containing the wrong link counts in G
				if (newFile.getName().indexOf(" - New_LinkCounts_")>=0)
					newFile.delete();
				
				// Delete the files containing the route counts in G'
				if (newFile.getName().indexOf(" - Rotas_")>=0)
					newFile.delete();
			}
		}
	}
}
