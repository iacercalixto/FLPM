package extractor;

import java.util.*;

public class Graph {
    private LinkedList<Node> nodes;
    private LinkedList<Edge> edges;
    private String description;
    private int numNodes, numEdges;
    private HashMap<Integer, Integer> nodeTable;

    public Graph() {
        this.nodes = new LinkedList<Node>();
        this.edges = new LinkedList<Edge>();
    }

    public void addNode(Node n) {
        this.nodes.add(n);
    }

    public void addEdge(Edge e) {
        this.edges.add(e);
    }

    public Node getNode(int i) {
        return this.nodes.get(i);
    }

    public Edge getEdge(int i) {
        return this.edges.get(i);
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public String getDescription() {
        return this.description;
    }

    public void setNumNodes(int n) {
        this.numNodes = n;
    }

    public void setNumEdges(int n) {
        this.numEdges = n;
    }

    public int getNumNodes() {
        return this.numNodes;
    }

    public int getNumEdges() {
        return this.numEdges;
    }

    public int getEdgesSize() {
        return this.edges.size();
    }

    public int getNodesSize() {
        return this.nodes.size();
    }

    public boolean isValid() {
        boolean valid = false;
        if (this.numEdges == this.edges.size() && this.numNodes == getNodesSize())
            valid = true;
        return valid;
    }

    public void setTable(HashMap<Integer, Integer> t) {
        this.nodeTable = t;
    }

    public HashMap<Integer, Integer> getTable() {
        return this.nodeTable;
    }

    public void order() {
        Collections.sort(this.nodes);
        Collections.sort(this.edges);
    }
    
    public String toString() {
    	return "Nodes: " + this.nodes + "\nEdges: " + this.edges;
    }
}
