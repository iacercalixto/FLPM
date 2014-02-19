/**
 * 
 */
package postprocessing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import odEstimation.AutoMapValue;
import odEstimation.ODMatrix;
import odEstimation.ODPairs;

/**
 * Class used to compute the root mean squared error (RMSE)
 * and the mean absolute error (MAE) between two solutions.
 * 
 * We compute it comparing OD matrices or arc traffic counts.
 * 
 * The formulas are described in Sherali et al. (2003),
 * pages 829-830.
 * 
 * @author Iacer
 *
 */
public class SolutionAnalyser {

	/**
	 * Constructor
	 */
	public SolutionAnalyser() {}
	
	/**
	 * @param odMatrix1 the OD matrix to be compared (estimated)
	 * @param odMatrix2 the OD matrix to compare to ("real" or target)
	 * @return
	 */
	public double compareODMatricesRMSE(AutoMapValue odMatrix1,
			AutoMapValue odMatrix2, ODPairs odPairs)
	{
		double accumulatedDifferenceT1T2 = 0;
		double accumulatedT2 = 0;
		double numerator, denominator;
		
		Iterator itI1, itO1;
		Iterator itI2, itO2; //recently added
		Map i, j;
		Map k, l; //recently added
		AutoMapValue temp, temp2;
		AutoMapValue temp21, temp22; //recently added
		Map.Entry o, d;
		Map.Entry o2, d2; //recently added
		i = odMatrix1.getMap();
		k = odMatrix2.getMap(); //recently added
		int size = 0;
		
		itI1 = i.entrySet().iterator();
		itI2 = k.entrySet().iterator(); //recently added
		while (itI1.hasNext())
		{
			o = (Map.Entry)itI1.next();
			o2 = (Map.Entry)itI2.next(); //recently added
			size++;
			
			temp = (AutoMapValue)o.getValue();
			temp21 = (AutoMapValue)o2.getValue(); //recently added
			j = temp.getMap();
			l = temp21.getMap(); //recently added
			
			itO1 = j.entrySet().iterator();
			itO2 = l.entrySet().iterator(); //recently added
			while (itO1.hasNext())
			{
				d = (Map.Entry)itO1.next();
				d2 = (Map.Entry)itO2.next(); //recently added
				
				temp2 = (AutoMapValue)d.getValue();
				temp22 = (AutoMapValue)d2.getValue(); //recently added
				
				if ( Double.parseDouble( (String) temp2.get() ) > 0 )
				{
					int from = Integer.parseInt((String)o.getKey());
					int to = Integer.parseInt((String)d.getKey());
					
					if (temp2.get() != null)
					{
						double entry1 = Double.parseDouble((String)temp2.get());
						double entry2 = Double.parseDouble((String)temp22.get());
						
						double square = Math.pow(entry1 - entry2, 2);
						accumulatedDifferenceT1T2 += square;
						accumulatedT2 += entry2;
					}
				}
			}
		}
		
		if (size>0)
		{
			numerator = Math.sqrt(accumulatedDifferenceT1T2 / size);
			denominator = accumulatedT2 / size;
		} else
		{
			numerator = 0;
			denominator = 1;
		}
		
		return (numerator / denominator * 100);
	}
	
	/**
	 * @param odMatrix1 the compared OD matrix
	 * @param referenceODMatrix the OD matrix to be compared to (reference)
	 * @param odPairs the OD pairs in the model
	 * @return
	 */
	public double compareODMatricesRMSE(AutoMapValue odMatrix1, ODMatrix referenceODMatrix)
	{
		double accumulatedDifferenceT1T2 = 0;
		double accumulatedT2 = 0;
		double numerator, denominator;
		
		Iterator itI1, itO1;
		Map i, j;
		AutoMapValue temp, temp2;
		Map.Entry o, d;
		int size;
		
		i = odMatrix1.getMap();
		size = 0;
		
		itI1 = i.entrySet().iterator();
		while (itI1.hasNext())
		{
			o = (Map.Entry)itI1.next();
			size++;
			
			temp = (AutoMapValue)o.getValue();
			j = temp.getMap();
			itO1 = j.entrySet().iterator();
			while (itO1.hasNext())
			{
				d = (Map.Entry)itO1.next();
				temp2 = (AutoMapValue)d.getValue();
				
				if ( Double.parseDouble( (String) temp2.get() ) > 0 )
				{
					String from = (String) o.getKey();
					String to = (String) d.getKey();
					double dblReferenceValue = -1;
					
					try {
						// Get the corresponding value in the reference OD matrix
						dblReferenceValue = referenceODMatrix.getCountBetween(from, to);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (temp2.get() != null)
					{
						double entry1 = Double.parseDouble((String)temp2.get());
						double entry2 = dblReferenceValue;
						
						double square = Math.pow(entry1 - entry2, 2);
						accumulatedDifferenceT1T2 += square;
						accumulatedT2 += entry2;
					}
				}
			}
		}
		
		if (size>0)
		{
			numerator = Math.sqrt(accumulatedDifferenceT1T2 / size);
			denominator = accumulatedT2 / size;
		} else
		{
			numerator = 0;
			denominator = 1;
		}
		
		return (numerator / denominator * 100);
	}
	
	/**
	 * @param arcs1 the arcs to be compared (estimated)
	 * @param arcs2 the arcs to compare to ("real" or target)
	 * @return
	 */
	public double compareArcFlowsRMSE(HashMap arcs1, HashMap arcs2)
	{
		double accumulatedDifferencef1f2 = 0;
		double accumulatedf2 = 0;
		double multiplier1, multiplier2;
		int size = 0;
		
		if (arcs1==null || arcs2==null)
		{
			System.err.println("arcs1: "+arcs1);
			System.err.println("arcs2: "+arcs2);
			return Double.POSITIVE_INFINITY;
		}
		
		System.out.println("arcs1: " + arcs1);
		System.out.println("arcs2: " + arcs2);
		
		Iterator itArcs1 = arcs1.entrySet().iterator();
		
		if (!arcs2.isEmpty())
		{
			// Iterate through the arcs
			while (itArcs1.hasNext())
			{
				Map.Entry entryArcs1 = (Map.Entry) itArcs1.next();
				String arcs1Key = (String)entryArcs1.getKey();
				double arcs1Value = (Double)entryArcs1.getValue();
				
				String matchArcs1 = "";
				if (arcs1Key.contains("->"))
					matchArcs1 = "->";
				else
					matchArcs1 = ",";
				
				String[] fromTo = arcs1Key.split(matchArcs1);
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// Iterate through both arcs concomitantly
				String matchArcs2 = "";
				double arcs2Value;
				
				if (arcs2.keySet().iterator().next().toString().contains("->"))
					matchArcs2 = "->";
				else
					matchArcs2 = ",";
				
				//System.out.println("Entrei 2");
				//System.out.println("("+fromTo[0]+matchArcs2+fromTo[1]+")");
				//System.out.println("arcs2.keySet(): "+arcs2.keySet());
				
				// If the link count has estimated measurements, compare them
				if (arcs2.containsKey("("+fromTo[0]+matchArcs2+fromTo[1]+")") ||
						arcs2.containsKey(fromTo[0]+"->"+fromTo[1]))
				{
					
					//System.out.println("Entrei 3");
					//System.out.println("arcs2.containsKey(fromTo[0]->fromTo[1])): "+
					//		arcs2.containsKey(fromTo[0]+"->"+fromTo[1]));
					//System.out.println("arcs2.containsKey(fromTo[0]+matchArcs2+fromTo[1]): "+
					//		arcs2.containsKey("("+fromTo[0]+matchArcs2+fromTo[1]+")"));
					if (arcs2.keySet().iterator().next().toString().contains("->"))
					{
						
						//System.out.println("Entrei 4");
						
						matchArcs2 = "->";
						arcs2Value = (Double)arcs2.get(fromTo[0]+matchArcs2+fromTo[1]);
					} else
					{
						
						//System.out.println("Entrei 5");
						
						matchArcs2 = ",";
						
						Class c = arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")").getClass();
						if (c.toString().indexOf("String") != -1) // is String
							arcs2Value = Double.parseDouble(
									(String)arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")")
							);
						else // is Double
							arcs2Value = (Double)arcs2.get(
									"("+fromTo[0]+matchArcs2+fromTo[1]+")"
							);
					}
					
					//System.err.println("arcs1Value: "+arcs1Value+", arcs2Value: "+arcs2Value);
					
					// Sum up the quadratic differences
					accumulatedDifferencef1f2 += Math.pow((arcs1Value - arcs2Value), 2);
					accumulatedf2 += arcs2Value;
					
					size++;
				}
			}
		}
		
		if (size>0)
		{
			//System.err.println("accumulatedDifferencef1f2: "+accumulatedDifferencef1f2+
			//		", accumulatedf2: "+accumulatedf2);
			multiplier1 = Math.sqrt(accumulatedDifferencef1f2 / size);
			multiplier2 = 100 / (accumulatedf2 / size);
			
			if (Double.isInfinite(multiplier2))
				multiplier2 = 0;
		} else
		{
			multiplier1 = 0;
			multiplier2 = 0;
		}
		
		return (multiplier1 * multiplier2);
	}
	
	/**
	 * @param odMatrix1 the OD matrix to be compared (estimated)
	 * @param odMatrix2 the OD matrix to compare to ("real" or target)
	 * @return
	 */
	public double compareODMatricesMAE(AutoMapValue odMatrix1,
			AutoMapValue odMatrix2, ODPairs odPairs)
	{
		double accumulatedDifferenceT1T2 = 0;
		double accumulatedT2 = 0;
		
		Iterator itI1, itO1;
		Iterator itI2, itO2; //recently added
		Map i, j;
		Map k, l; //recently added
		AutoMapValue temp, temp2;
		AutoMapValue temp21, temp22; //recently added
		Map.Entry o, d;
		Map.Entry o2, d2; //recently added
		i = odMatrix1.getMap();
		k = odMatrix2.getMap(); //recently added
		int size = 0;
		
		itI1 = i.entrySet().iterator();
		itI2 = k.entrySet().iterator(); //recently added
		while (itI1.hasNext())
		{
			o = (Map.Entry)itI1.next();
			o2 = (Map.Entry)itI2.next(); //recently added
			size++;
			
			temp = (AutoMapValue)o.getValue();
			temp21 = (AutoMapValue)o2.getValue(); //recently added
			j = temp.getMap();
			l = temp21.getMap(); //recently added
			
			itO1 = j.entrySet().iterator();
			itO2 = l.entrySet().iterator(); //recently added
			while (itO1.hasNext())
			{
				d = (Map.Entry)itO1.next();
				d2 = (Map.Entry)itO2.next(); //recently added
				
				temp2 = (AutoMapValue)d.getValue();
				temp22 = (AutoMapValue)d2.getValue(); //recently added
				
				if ( Double.parseDouble( (String) temp2.get() ) > 0 )
				{
					int from = Integer.parseInt((String)o.getKey());
					int to = Integer.parseInt((String)d.getKey());
					
					if (temp2.get() != null)
					{
						double entry1 = Double.parseDouble((String)temp2.get());
						double entry2 = Double.parseDouble((String)temp22.get());
						
						double abs = Math.abs(entry1 - entry2);
						accumulatedDifferenceT1T2 += abs;
						accumulatedT2 += entry2;
					}
				}
			}
		}
		
		double dblResult = 0;
		
		if (accumulatedT2==0)
			dblResult = 0;
		else
			dblResult = accumulatedDifferenceT1T2 / accumulatedT2 * 100;
		
		return dblResult;
	}
	
	/**
	 * @param odMatrix1 the OD matrix to compare
	 * @param referenceODMatrix the OD matrix to be compared to (reference)
	 * @return
	 */
	public double compareODMatricesMAE(AutoMapValue odMatrix1, ODMatrix referenceODMatrix)
	{
		double accumulatedDifferenceT1T2 = 0;
		double accumulatedT2 = 0;
		
		Iterator itI1, itO1;
		Map i, j;
		AutoMapValue temp, temp2;
		Map.Entry o, d;
		i = odMatrix1.getMap();
		int size = 0;
		
		itI1 = i.entrySet().iterator();
		while (itI1.hasNext())
		{
			o = (Map.Entry)itI1.next();
			size++;
			
			temp = (AutoMapValue)o.getValue();
			j = temp.getMap();
			itO1 = j.entrySet().iterator();
			while (itO1.hasNext())
			{
				d = (Map.Entry)itO1.next();
				temp2 = (AutoMapValue)d.getValue();
				
				if ( Double.parseDouble( (String) temp2.get() ) > 0 )
				{
					String from = (String) o.getKey();
					String to = (String) d.getKey();
					double dblReferenceValue = -1;
					
					try {
						// Get the corresponding value in the reference OD matrix
						dblReferenceValue = referenceODMatrix.getCountBetween(from, to);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (temp2.get() != null)
					{
						double entry1 = Double.parseDouble((String)temp2.get());
						double entry2 = dblReferenceValue;
						
						double abs = Math.abs(entry1 - entry2);
						accumulatedDifferenceT1T2 += abs;
						accumulatedT2 += entry2;
					}
				}
			}
		}
		
		double dblResult = 0;
		
		if (accumulatedT2==0)
			dblResult = 0;
		else
			dblResult = accumulatedDifferenceT1T2 / accumulatedT2 * 100;
		
		return dblResult;
	}
	
	/**
	 * @param arcs1
	 * @param arcs2
	 * @return
	 */
	public double compareArcFlowsMAE(HashMap arcs1, HashMap arcs2)
	{
		double accumulatedDifferencef1f2 = 0;
		double accumulatedf2 = 0;
		
		if (arcs1==null || arcs2==null)
		{
			System.err.println("arcs1: "+arcs1);
			System.err.println("arcs2: "+arcs2);
			return Double.POSITIVE_INFINITY;
		}
		
		System.out.println("arcs1: " + arcs1);
		System.out.println("arcs2: " + arcs2);
		
		if (!arcs2.isEmpty())
		{
			Iterator itArcs1 = arcs1.entrySet().iterator();
			
			// Iterate through the arcs
			while (itArcs1.hasNext())
			{
				Map.Entry entryArcs1 = (Map.Entry) itArcs1.next();
				String arcs1Key = (String)entryArcs1.getKey();
				double arcs1Value = (Double)entryArcs1.getValue();
				
				String matchArcs1 = "";
				if (arcs1Key.contains("->"))
					matchArcs1 = "->";
				else
					matchArcs1 = ",";
				
				String[] fromTo = arcs1Key.split(matchArcs1);
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// Iterate through both arcs concomitantly
				String matchArcs2 = "";
				double arcs2Value;
				
				if (arcs2.keySet().iterator().next().toString().contains("->"))
					matchArcs2 = "->";
				else
					matchArcs2 = ",";
				
				// If the link count has estimated measurements, compare them
				//if (arcs2.containsKey("("+fromTo[0]+matchArcs2+fromTo[1]+")"))
				if (arcs2.containsKey("("+fromTo[0]+matchArcs2+fromTo[1]+")") ||
						arcs2.containsKey(fromTo[0]+"->"+fromTo[1]))
				{
					if (arcs2.keySet().iterator().next().toString().contains("->"))
					{
						matchArcs2 = "->";
						arcs2Value = (Double)arcs2.get(fromTo[0]+matchArcs2+fromTo[1]);
					} else
					{
						matchArcs2 = ",";
						
						Class c = arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")").getClass();
						if (c.toString().indexOf("String") != -1) // is String
							arcs2Value = Double.parseDouble(
									(String)arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")")
							);
						else
							arcs2Value = (Double)arcs2.get(
									"("+fromTo[0]+matchArcs2+fromTo[1]+")"
							);
					}
					
					//System.out.println("("+fromTo[0]+","+fromTo[1]+"): " + arcs2Value);
					
					// Sum up the quadratic differences
					accumulatedDifferencef1f2 += Math.abs(arcs1Value - arcs2Value);
					accumulatedf2 += arcs2Value;
				}
			}
		}
		
		return accumulatedDifferencef1f2 * 100 / accumulatedf2;
	}
	
	/**
	 * 
	 * @param arcs1
	 * @param arcs2
	 * @return
	 */
	public double compareArcFlowsRMSE_PETGyn(HashMap arcs1, HashMap arcs2)
	{
		double accumulatedDifferencef1f2 = 0;
		double accumulatedf2 = 0;
		double multiplier1, multiplier2;
		int size = 0;
		
		if (arcs1==null || arcs2==null)
		{
			System.err.println("arcs1: "+arcs1);
			System.err.println("arcs2: "+arcs2);
			return Double.POSITIVE_INFINITY;
		}
		
		System.out.println("arcs1: " + arcs1);
		System.out.println("arcs2: " + arcs2);
		
		Iterator itArcs1 = arcs1.entrySet().iterator();
		
		if (!arcs2.isEmpty())
		{
			// Iterate through the arcs
			while (itArcs1.hasNext())
			{
				Map.Entry entryArcs1 = (Map.Entry) itArcs1.next();
				String arcs1Key = (String)entryArcs1.getKey();
				double arcs1Value = (Double)entryArcs1.getValue();
				
				String matchArcs1 = "";
				if (arcs1Key.contains("->"))
					matchArcs1 = "->";
				else
					matchArcs1 = ",";
				
				String[] fromTo = arcs1Key.split(matchArcs1);
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// Only compute the difference to the edges that
				// contains the G graph link costs and link estimates
				if ( Integer.parseInt(fromTo[0]) > 1000 &&
						Integer.parseInt(fromTo[1]) == 10 * Integer.parseInt(fromTo[0]))
				{
					
					// Iterate through both arcs concomitantly
					String matchArcs2 = "";
					double arcs2Value;
					
					if (arcs2.keySet().iterator().next().toString().contains("->"))
						matchArcs2 = "->";
					else
						matchArcs2 = ",";
					
					//System.out.println("Entrei 2");
					//System.out.println("("+fromTo[0]+matchArcs2+fromTo[1]+")");
					//System.out.println("arcs2.keySet(): "+arcs2.keySet());
					
					// If the link count has estimated measurements, compare them
					if (arcs2.containsKey("("+fromTo[0]+matchArcs2+fromTo[1]+")") ||
							arcs2.containsKey(fromTo[0]+"->"+fromTo[1]))
					{
						
						//System.out.println("Entrei 3");
						
						if (arcs2.keySet().iterator().next().toString().contains("->"))
						{
							
							//System.out.println("Entrei 4");
							
							matchArcs2 = "->";
							arcs2Value = (Double)arcs2.get(fromTo[0]+matchArcs2+fromTo[1]);
						} else
						{
							
							//System.out.println("Entrei 5");
							
							matchArcs2 = ",";
							
							Class c = arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")").getClass();
							if (c.toString().indexOf("String") != -1) // is String
								arcs2Value = Double.parseDouble(
										(String)arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")")
								);
							else // is Double
								arcs2Value = (Double)arcs2.get(
										"("+fromTo[0]+matchArcs2+fromTo[1]+")"
								);
						}
						
						// Sum up the quadratic differences
						accumulatedDifferencef1f2 += Math.pow((arcs1Value - arcs2Value), 2);
						accumulatedf2 += arcs2Value;
						
						size++;
					}
					
				}
				
			}
		}
		
		if (size>0)
		{
			multiplier1 = Math.sqrt(accumulatedDifferencef1f2 / size);
			multiplier2 = 100 / (accumulatedf2 / size);
			
			if (Double.isInfinite(multiplier2))
				multiplier2 = 0;
		} else
		{
			multiplier1 = 0;
			multiplier2 = 0;
		}
		
		return (multiplier1 * multiplier2);
	}
	
	/**
	 * 
	 * @param arcs1
	 * @param arcs2
	 * @return
	 */
	public double compareArcFlowsMAE_PETGyn(HashMap arcs1, HashMap arcs2)
	{
		double accumulatedDifferencef1f2 = 0;
		double accumulatedf2 = 0;
		int size = 0;
		
		if (arcs1==null || arcs2==null)
		{
			System.err.println("arcs1: "+arcs1);
			System.err.println("arcs2: "+arcs2);
			return Double.POSITIVE_INFINITY;
		}
		
		if (!arcs2.isEmpty())
		{
			Iterator itArcs1 = arcs1.entrySet().iterator();
			
			// Iterate through the arcs
			while (itArcs1.hasNext())
			{
				Map.Entry entryArcs1 = (Map.Entry) itArcs1.next();
				String arcs1Key = (String)entryArcs1.getKey();
				double arcs1Value = (Double)entryArcs1.getValue();
				
				String matchArcs1 = "";
				if (arcs1Key.contains("->"))
					matchArcs1 = "->";
				else
					matchArcs1 = ",";
				
				String[] fromTo = arcs1Key.split(matchArcs1);
				fromTo[0] = fromTo[0].replace("(", "").trim();
				fromTo[1] = fromTo[1].replace(")", "").trim();
				
				// Only compute the difference to the edges that
				// contains the G graph link costs and link estimates
				if ( Integer.parseInt(fromTo[0]) > 1000 &&
						Integer.parseInt(fromTo[1]) == 10 * Integer.parseInt(fromTo[0]))
				{
					
					// Iterate through both arcs concomitantly
					String matchArcs2 = "";
					double arcs2Value;
					
					if (arcs2.keySet().iterator().next().toString().contains("->"))
						matchArcs2 = "->";
					else
						matchArcs2 = ",";
					
					// If the link count has estimated measurements, compare them
					if (arcs2.containsKey("("+fromTo[0]+matchArcs2+fromTo[1]+")") ||
							arcs2.containsKey(fromTo[0]+"->"+fromTo[1]))
					{
						if (arcs2.keySet().iterator().next().toString().contains("->"))
						{
							matchArcs2 = "->";
							arcs2Value = (Double)arcs2.get(fromTo[0]+matchArcs2+fromTo[1]);
						} else
						{
							matchArcs2 = ",";
							
							Class c = arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")").getClass();
							if (c.toString().indexOf("String") != -1) // is String
								arcs2Value = Double.parseDouble(
										(String)arcs2.get("("+fromTo[0]+matchArcs2+fromTo[1]+")")
								);
							else
								arcs2Value = (Double)arcs2.get(
										"("+fromTo[0]+matchArcs2+fromTo[1]+")"
								);
						}
						
						//System.out.println("("+fromTo[0]+","+fromTo[1]+"): " + arcs2Value);
						
						// Sum up the quadratic differences
						accumulatedDifferencef1f2 += Math.abs(arcs1Value - arcs2Value);
						accumulatedf2 += arcs2Value;
						
						size++;
					}
					
				}
				
			}
		}
		
		double returnValue;
		
		if (size==0)
		{
			//accumulatedf2==0
			returnValue = 0;
		} else
		{
			if (accumulatedf2==0)
				returnValue = 0;
			else
				returnValue = accumulatedDifferencef1f2 * 100 / accumulatedf2;
		}
		
		return returnValue;
	}
}
