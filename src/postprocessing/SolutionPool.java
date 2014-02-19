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
public class SolutionPool {
	private ArrayList <Double> arrSolutionLambda;
	private ArrayList <Double> arrSolutionM6;
	private ArrayList <AutoMapValue> arrSolutionODMatrix;
	private ArrayList <HashMap<String, Double>> arrSolutionRouteCounts;
	private ArrayList <HashMap<String, Double>> arrSolutionLinkCounts;
	private ArrayList <String> arrSolutionModel;
	
	/**
	 * Constructor
	 */
	public SolutionPool()
	{
		this.arrSolutionLambda = new ArrayList<Double>();
		this.arrSolutionM6 = new ArrayList<Double>();
		this.arrSolutionODMatrix = new ArrayList<AutoMapValue>();
		this.arrSolutionRouteCounts = new ArrayList<HashMap<String, Double>>();
		this.arrSolutionLinkCounts = new ArrayList<HashMap<String, Double>>();
		this.arrSolutionModel = new ArrayList<String>();
	}
	
	/**
	 * States if the solution already exists in the solution pool
	 * 
	 * @param lambda the final lambda
	 * @param solutionM6 the final value of problem M6
	 * @param ODMatrix the final OD matrix (Tij)
	 * @return boolean
	 */
	public boolean hasSolution(double lambda, double solutionM6, AutoMapValue ODMatrix)
	{
		int i;
		boolean hasSolution = false;
		boolean lambdaComparator = false;
		boolean M6Comparator = false;
		
		double lambdaError = 0.005;
		double M6Error = 1;
		
		for (i=0;i<arrSolutionLambda.size();i++)
		{
			if (Math.abs(arrSolutionLambda.get(i) - lambda) < lambdaError)
			{
				lambdaComparator = true;
			}
			
			if (Math.abs(arrSolutionM6.get(i) - solutionM6) < M6Error)
			{
				M6Comparator = true;
			}
			
			if (lambdaComparator && M6Comparator &&
					areODMatricesEqual(arrSolutionODMatrix.get(i), ODMatrix) )
					//arrSolutionODMatrix.get(i).toString().equals(ODMatrix.toString()))
			{
				hasSolution = true;
			}
		}
		return hasSolution;
	}
	
	public boolean areSolutionsEqual(double lambda_1, double solutionM6_1, AutoMapValue ODMatrix_1,
			double lambda_2, double solutionM6_2, AutoMapValue ODMatrix_2)
	{
		int i;
		boolean lambdaComparator = false;
		boolean M6Comparator = false;
		boolean ODMatrixComparator = false;
		
		double lambdaError = 0.05;
		double M6Error = 10;
		
		if (Math.abs(lambda_1 - lambda_2) <= lambdaError)
			lambdaComparator = true;
		
		if (Math.abs(solutionM6_1 - solutionM6_2) <= M6Error)
			M6Comparator = true;
		
		if ( areODMatricesEqual(ODMatrix_1, ODMatrix_2) )
			ODMatrixComparator = true;
		
		//System.out.println("Math.abs(lambda_1 - lambda_2): "+Math.abs(lambda_1 - lambda_2));
		//System.out.println("solutionM6_1: "+solutionM6_1+", solutionM6_2: "+solutionM6_2);
		//System.out.println("Math.abs(solutionM6_1 - solutionM6_2): "+Math.abs(solutionM6_1 - solutionM6_2));
		
		return (lambdaComparator && M6Comparator && ODMatrixComparator);
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
	
	public void addSolution(double lambda, double solutionM6,
			AutoMapValue ODMatrix, HashMap<String, Double> routeCounts,
			HashMap<String, Double> linkCounts, String strModel)
	{
		if (!this.hasSolution(lambda, solutionM6, ODMatrix))
		{
			this.arrSolutionLambda.add(lambda);
			this.arrSolutionM6.add(solutionM6);
			this.arrSolutionODMatrix.add(ODMatrix);
			this.arrSolutionRouteCounts.add(routeCounts);
			this.arrSolutionLinkCounts.add(linkCounts);
			this.arrSolutionModel.add(strModel);
		}
	}
	
	/**
	 * @return the arrSolutionLambda
	 */
	public ArrayList getArrSolutionLambda() {
		return arrSolutionLambda;
	}
	
	/**
	 * @param arrSolutionLambda the arrSolutionLambda to set
	 */
	public void setArrSolutionLambda(ArrayList arrSolutionLambda) {
		this.arrSolutionLambda = arrSolutionLambda;
	}
	
	/**
	 * @return the arrSolutionM6
	 */
	public ArrayList getArrSolutionM6() {
		return arrSolutionM6;
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
	 * @param arrSolutionM6 the arrSolutionM6 to set
	 */
	public void setArrSolutionM6(ArrayList arrSolutionM6) {
		this.arrSolutionM6 = arrSolutionM6;
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
		for (i=0;i<arrSolutionLambda.size();i++)
		{
			strSolution += "Solution " + (i+1) + ":\n";
			strSolution += "Lambda: " + Double.toString(arrSolutionLambda.get(i)) + ",\n";
			strSolution += "SolutionM6: " + Double.toString(arrSolutionM6.get(i)) + ",\n";
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
					retVal += ("(" + o.getKey() + "," + d.getKey() + "," + temp2.get()) + ") \n";
					//retVal += (o.getKey() + "->" + d.getKey() + ": " + temp2.get()) + "\n";
			}
		}
		
		return retVal;
	}
	
	public String getLinkCountsValues(HashMap linkCounts)
	{
		String retVal = "";
		Iterator itLinkCounts = linkCounts.entrySet().iterator();
		while (itLinkCounts.hasNext())
		{
			Map.Entry entry = (Map.Entry) itLinkCounts.next();
			String key = (String) entry.getKey();
			Double value = (Double) entry.getValue();
			
			retVal += (key + ": " + value + "\n");
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
