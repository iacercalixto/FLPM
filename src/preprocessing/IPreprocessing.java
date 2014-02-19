/**
 * 
 */
package preprocessing;

import java.util.HashMap;

import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultWeightedEdge;




/**
 * @author Iacer Calixto
 *
 */
public interface IPreprocessing {
	public HashMap<?, ?> doPreprocessing(AbstractGraph<String, DefaultWeightedEdge> graph,
			LinkCounts lCounts, ODCounts od, RouteCosts rc, LinkCosts lCosts, ODPairs odPairs) throws Exception;
}
