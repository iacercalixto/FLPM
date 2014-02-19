/**
 * 
 */
package postprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import odEstimation.AutoMapValue;


/**
 * @author Iacer
 *
 */
public class SolutionPoolSherali {
	private ArrayList <Double> arrSolutionZ;
	private ArrayList <AutoMapValue> arrSolutionODMatrix;
	private ArrayList <HashMap<String, Double>> arrSolutionRouteCounts;
	private ArrayList <HashMap<String, Double>> arrSolutionLinkCounts;
	private ArrayList <String> arrSolutionModel;
	
	/**
	 * Constructor
	 */
	public SolutionPoolSherali()
	{
		this.arrSolutionZ = new ArrayList<Double>();
		this.arrSolutionODMatrix = new ArrayList<AutoMapValue>();
		this.arrSolutionRouteCounts = new ArrayList<HashMap<String, Double>>();
		this.arrSolutionLinkCounts = new ArrayList<HashMap<String, Double>>();
		this.arrSolutionModel = new ArrayList<String>();
	}
	
	/**
	 * States if the solution already exists in the solution pool
	 * 
	 * @param lambda the final lambda
	 * @param solutionZ the final value of problem Z
	 * @param ODMatrix the final OD matrix (Tij)
	 * @return boolean
	 */
	public boolean hasSolution(double solutionZ, AutoMapValue ODMatrix)
	{
		int i;
		boolean hasSolution = false;
		boolean ZComparator = false;
		double ZError = 0.5;
		
		for (i=0;i<arrSolutionZ.size();i++)
		{
			if (arrSolutionZ.get(i) - ZError <= solutionZ ||
					arrSolutionZ.get(i) + ZError >= solutionZ)
			{
				ZComparator = true;
			}
			
			if (areODMatricesEqual(arrSolutionODMatrix.get(i), ODMatrix) &&
					ZComparator)
			{
				hasSolution = true;
			}
		}
		
		return hasSolution;
	}
	
	public boolean areSolutionsEqual(double solutionZ_1, AutoMapValue ODMatrix_1,
			double solutionZ_2, AutoMapValue ODMatrix_2)
	{
		int i;
		boolean ZComparator = false;
		boolean ODMatrixComparator = false;
		
		double ZError = 10;
		
		if (Math.abs(solutionZ_1 - solutionZ_2) <= ZError)
			ZComparator = true;
		
		if ( areODMatricesEqual(ODMatrix_1, ODMatrix_2) )
			ODMatrixComparator = true;
		
		//System.out.println("Math.abs(lambda_1 - lambda_2): "+Math.abs(lambda_1 - lambda_2));
		//System.out.println("solutionZ_1: "+solutionZ_1+", solutionZ_2: "+solutionZ_2);
		//System.out.println("Math.abs(solutionZ_1 - solutionZ_2): "+Math.abs(solutionZ_1 - solutionZ_2));
		
		return (ZComparator && ODMatrixComparator);
	}
	
	public boolean areODMatricesEqual(AutoMapValue ODMatrix_1, AutoMapValue ODMatrix_2)
	{
		double odMatrixError = 1;
		boolean bothMatricesEqual = true;
		
		// Variables to the first OD matrix
		Iterator itI_1, itO_1, itD_1;
		Map i_1, j_1;
		AutoMapValue temp_1, temp2_1;
		Map.Entry o_1, d_1;
		i_1 = ODMatrix_1.getMap();
		
		// Variables to the second OD matrix
		Iterator itI_2, itO_2, itD_2;
		Map i_2, j_2;
		AutoMapValue temp_2, temp2_2;
		Map.Entry o_2, d_2;
		i_2 = ODMatrix_2.getMap();
		
		// Iterate through the first and second OD matrices concomitantly
		itI_1 = i_1.entrySet().iterator();
		itI_2 = i_2.entrySet().iterator();
		while (itI_1.hasNext())
		{
			o_1 = (Map.Entry)itI_1.next();
			o_2 = (Map.Entry)itI_2.next();
			
			temp_1 = (AutoMapValue)o_1.getValue();
			temp_2 = (AutoMapValue)o_2.getValue();
			
			j_1 = temp_1.getMap();
			j_2 = temp_2.getMap();
			
			itO_1 = j_1.entrySet().iterator();
			itO_2 = j_2.entrySet().iterator();
			while (itO_1.hasNext())
			{
				d_1 = (Map.Entry)itO_1.next();
				d_2 = (Map.Entry)itO_2.next();
				
				temp2_1 = (AutoMapValue)d_1.getValue();
				temp2_2 = (AutoMapValue)d_2.getValue();
				
				// If the difference between two entries in the same position
				// is greater than an error deviation, the matrices are considered
				// different
				if ( Math.abs(
						Double.parseDouble((String)temp2_1.get()) -
						Double.parseDouble((String)temp2_2.get())
						) > odMatrixError )
					bothMatricesEqual = false;
			}
		}
		
		return bothMatricesEqual;
	}
	
	public void addSolution(double solutionZ,
			AutoMapValue ODMatrix, HashMap<String, Double> routeCounts,
			HashMap<String, Double> linkCounts, String strModel)
	{
		if (!this.hasSolution(solutionZ, ODMatrix))
		{
			this.arrSolutionZ.add(solutionZ);
			this.arrSolutionODMatrix.add(ODMatrix);
			this.arrSolutionRouteCounts.add(routeCounts);
			this.arrSolutionLinkCounts.add(linkCounts);
			this.arrSolutionModel.add(strModel);
		}
	}
	
	/**
	 * @return the arrSolutionZ
	 */
	public ArrayList getArrSolutionZ() {
		return arrSolutionZ;
	}
	
	/**
	 * @return the arrSolutionModel
	 */
	public ArrayList<String> getArrSolutionModel() {
		return arrSolutionModel;
	}
	
	public ArrayList<HashMap<String, Double>> getArrSolutionRouteCounts() {
		return arrSolutionRouteCounts;
	}
	
	/**
	 * @param arrSolutionZ the arrSolutionZ to set
	 */
	public void setArrSolutionZ(ArrayList arrSolutionZ) {
		this.arrSolutionZ = arrSolutionZ;
	}
	
	/**
	 * @return the arrSolutionODMatrix
	 */
	public ArrayList getArrSolutionODMatrix() {
		return arrSolutionODMatrix;
	}
	
	/**
	 * @param arrSolutionODMatrix the arrSolutionODMatrix to set
	 */
	public void setArrSolutionODMatrix(ArrayList arrSolutionODMatrix) {
		this.arrSolutionODMatrix = arrSolutionODMatrix;
	}
	
	public String toString()
	{
		String strSolution = "";
		int i;
		for (i=0;i<arrSolutionZ.size();i++)
		{
			strSolution += "Solution " + (i+1) + ":\n";
			strSolution += "SolutionZ: " + Double.toString(arrSolutionZ.get(i)) + ",\n";
			strSolution += "ODMatrix:\n";
			strSolution += this.getODMatrixValues(arrSolutionODMatrix.get(i));
			strSolution += "Route counts:\n";
			strSolution += arrSolutionRouteCounts.get(i)+"\n";
			strSolution += "Link counts:\n";
			strSolution += arrSolutionLinkCounts.get(i)+"\n";
			strSolution += "Model:\n"+arrSolutionModel.get(i);
			strSolution += "\n";
		}
		return strSolution;
	}
	
	/**
	 * @param odMatrix
	 */
	public String getODMatrixValues(AutoMapValue odMatrix)
	{
		String retVal = "";
		Iterator itI, itO, itD;
		Map i, j;
		AutoMapValue temp, temp2;
		Map.Entry o, d;
		i = odMatrix.getMap();
		
		itI = i.entrySet().iterator();
		while (itI.hasNext())
		{
			o = (Map.Entry)itI.next();
			temp = (AutoMapValue)o.getValue();
			j = temp.getMap();
			
			itO = j.entrySet().iterator();
			while (itO.hasNext())
			{
				d = (Map.Entry)itO.next();
				temp2 = (AutoMapValue)d.getValue();
				if ( Double.parseDouble( (String) temp2.get() ) > 0 )
					retVal += (o.getKey() + "->" + d.getKey() + ": " + temp2.get()) + "\n";
			}
		}
		
		return retVal;
	}

	/**
	 * @return the arrSolutionLinkCounts
	 */
	public ArrayList<HashMap<String, Double>> getArrSolutionLinkCounts() {
		return arrSolutionLinkCounts;
	}

	/**
	 * @param arrSolutionLinkCounts the arrSolutionLinkCounts to set
	 */
	public void setArrSolutionLinkCounts(
			ArrayList<HashMap<String, Double>> arrSolutionLinkCounts) {
		this.arrSolutionLinkCounts = arrSolutionLinkCounts;
	}
}
