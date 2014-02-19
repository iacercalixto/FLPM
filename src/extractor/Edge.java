package extractor;

public class Edge implements Comparable {

    private Node from, to;
    private Double cost;

    public Edge(Node f, Node t) {
        this.from = f;
        this.to = t;
    }

    public Edge(Node f, Node t, Double c) {
        this.from = f;
        this.to = t;
        this.cost = c;
    }

    public Node getFrom() {
        return this.from;
    }

    public Node getTo() {
        return this.to;
    }

    public void setCost(Double c) {
        this.cost = c;
    }

    public Double getCost() {
        return this.cost;
    }

    public int compareTo(Object o1) {
        if (this.from.getNumber() == ((Edge) o1).from.getNumber()) {
            if (this.to.getNumber() > (((Edge) o1).to.getNumber()))
                    return 1;
            else if (this.to.getNumber() < (((Edge) o1).to.getNumber()))
                    return -1;
            else
                    return 0;
        } else if ((this.from.getNumber()) > ((Edge) o1).from.getNumber()) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public String toString() {
    	return "(" + this.from + "," + this.to + "): " + this.cost;
    }
}
