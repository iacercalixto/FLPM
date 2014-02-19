package cplexModels;

import java.util.HashMap;

import odEstimation.AutoMapValue;
import odEstimation.LinkCapacities;
import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;




public interface IFuzzyLPModel {
	public void createProblemM3(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs);
	
	public double solveProblemM3(String filePath);
	
	public void createProblemM2(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs);
	
	public double solveProblemM2(String filePath);
	
	public void createProblemM8(AbstractGraph graph, LinkCounts lCounts, ODCounts od,
			RouteCosts rc, LinkCosts lCosts, ODPairs odPairs, LinkCapacities lCap,
			double solutionUpperBound, double solutionLowerBound);
	
	public double solveProblemM8(String filePath);
	
	public HashMap<String, Double> getValueLinkCounts();
	
	public HashMap<String, Double> getValueRouteCounts();
	
	public HashMap<String, Double> getValueLinkCountsM3();
	
	public HashMap<String, Double> getValueRouteCountsM3();
	
	public double getValueSolutionM8();
	
	public void setODMatrixNodes(HashMap value);
	
	public AutoMapValue getODMatrix();
	
	public AutoMapValue getODMatrixM3();
	
	public IFuzzyLPModel clone() throws CloneNotSupportedException;
	
	public boolean isIntegerProgramming();
	
	public void setIntegerProgramming(boolean integerProgramming);
	
	public String getLPModel();
	
	public String getLPModelM2();
	
	public String getLPModelM3();
	
	public boolean isUseCodedErrors();
	
	public void setUseCodedErrors(boolean useCodedErrors);
	
	public boolean isUseGradientErrors();
	
	public void setUseGradientErrors(boolean useGradientErrors);
	
	public float getA();
	
	public void setA(float a);
	
	public float getB();
	
	public void setB(float b);
	
	public float getD();
	
	public void setD(float d);
	
	public float getE();
	
	public void setE(float e);
}
