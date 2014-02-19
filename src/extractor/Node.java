package extractor;

public class Node implements Comparable {
    private int father, number, first, second;
    
    /**
     * Se o no eh o no de saida de uma aresta, 0
     * Se o no eh o no de chegada de uma aresta, 1
     */
    private int d;
    
    //Construtor para nos originais
    public Node(int f, int n) {
        this.father = f;
        this.number = n;
        this.first = -1;
        this.second = -1;
    }
    //Construtor para nos gerados a partir de arestas
    public Node(int f, int n, int p, int s) {
        this.father = f;
        this.number = n;
        this.first = p;
        this.second = s;
    }
    
    //Construtor para nos de G' que representam uma aresta em G duplicados
    public Node(int f, int n, int p, int s, int d) {
        this.father = f;
        this.number = n;
        this.first = p;
        this.second = s;
        this.d = d;
    }
    
    public int getFather() {
        return this.father;
    }

    public int getNumber() {
        return this.number;
    }

    public int getFirst() {
        return this.first;
    }

    public int getSecond(){
        return this.second;
    }
    
    public int getD(){
        return this.d;
    }
    
    public void setD(int d){
        this.d = d;
    }
    
    public int compareTo(Object o1) {
        if (this.number == ((Node) o1).number)
            return 0;
        else if ((this.number) > ((Node) o1).number)
            return 1;
        else
            return -1;
    }
    
    public String toString() {
    	return Integer.toString( this.number ); //+ "(" + this.first + "," + this.second + ")";
    }
}