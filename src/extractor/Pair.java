package extractor;

public class Pair {
	/**
	 * The exit node in the transformed graph G'
	 */
    private int first;
    
    /**
     * The entry node in the transformed graph G'
     */
    private int last;
    
    /**
     * Constructor
     * @param f
     * @param s
     */
    public Pair(int f, int s) {
        this.first = f;
        this.last = s;
    }
    
    /**
     * 
     * @return
     */
    public int getFirst() {
        return this.first;
    }
    
    /**
     * 
     * @return
     */
    public int getLast() {
        return this.last;
    }
    
    public String toString() {
    	return "("+this.first+","+this.last+")";
    }
}
