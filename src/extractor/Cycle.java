package extractor;

import pet_class.*;

import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * Cicla o PETGyn.
 * Executar main();
 * 
 * A primeira execucao deve ter o parametro first como true,
 * e a ultima execucao deve ter o parametro last como true.
 * Todas as outras devem ter ambos os parametros como false.
 * 
 * @author iacer
 *
 */
public class Cycle {

    /*
     * To do:
     * receber matriz od do FLPSOD
     * fazer backup da matriz do PET
     * gravar a matriz do flpsod no banco do PET
     * rodar o PET-EU
     * retornar resultados para o FLPSOD
     * devolver a matriz original para o banco
     */
    private Extractor extractor = null;
    private Connection aConnection = null;
    private Statement aStatement = null;
    public ArrayList<Trinca> Demandas = new ArrayList<Trinca>();
    
    public static void main(String[] args) {
    	int idProjeto = 2;
    	String filePath = "/home/iacer/workspace/FLPM2_cplex/";
    	String odMatrixFileName = "New_MatrixOD_"+idProjeto+"_output.dat";
    	
        Cycle cycle = new Cycle(idProjeto, false, false, filePath, odMatrixFileName);
    }
    
    public Cycle(int idProjeto, boolean first, boolean last,
    		String filePath, String odMatrixFileName)
    {
        if (first && last) {
            System.err.println("Dados inconsistentes!");
            System.exit(1);
        }
        
        connectToDataBase(
        		"jdbc:firebirdsql://localhost:3050//usr/share/firebird/2.1/pet.fdb",
        		"SYSDBA",
        		"masterkey"
        );

        if (first) {
            try {
                //Carrega e faz backup das demandas
                carregaDemandas(idProjeto, filePath);
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.toString());
            }
        }
        
        //Exclui as demandas existentes no PET para o projeto em questao
        excluiDemandas(idProjeto);
        
        //Salvar as demandas estimadas pelo FLIPSOD na memoria
        Demandas = readCycle(idProjeto, filePath, odMatrixFileName);
        
        //Salvar as demandas na propriedade Demandas no banco do PETGyn
        persistirDemandas(idProjeto);
        
        //Rodar o PETGyn e gravar resultados (criar o arquivo de saida CustoNosArcos_2_output.dat)
        extractor = new Extractor(idProjeto, true);
        
        if (last) {
        	//Exclui as demandas existentes no PET para o projeto em questao
        	//que sao as demandas do FLIPSOD
            excluiDemandas(idProjeto);
            
            //Carrega as demandas originais de arquivo
            Demandas = readFrom(idProjeto, filePath);
            
            //Grava as demandas originais no banco do PETGyn
            devolveDemandas();
        }
    }
    
    /**
     * Exclui todas as demandas existentes no PETGyn para o projeto
     * 
     * @param idProjeto
     */
    private void excluiDemandas(int idProjeto) {
        try {
            String sSQL = "DELETE ";
            sSQL += "FROM 'Demandas' D ";
            sSQL += "WHERE ";
            sSQL += "D.'NoOrigem' IN ";
            sSQL += "( ";
            sSQL += " SELECT N.'idNo' FROM 'Nos' N ";
            sSQL += " WHERE ";
            sSQL += " N.'idProjeto'=" + Integer.toString(idProjeto) + " ";
            sSQL += ") ";
            sSQL += "AND ";
            sSQL += "D.'NoDestino' IN ";
            sSQL += "( ";
            sSQL += " SELECT N.'idNo' FROM 'Nos' N ";
            sSQL += " WHERE ";
            sSQL += " N.'idProjeto'=" + Integer.toString(idProjeto) + " ";
            sSQL += ") ";
            sSQL = sSQL.replace("'".charAt(0), '"');
            aStatement.executeUpdate(sSQL);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Carrega as demandas no banco do PETGyn
     * e salva backup das mesmas em arquivo
     * @param idProjeto
     */
    private void carregaDemandas(int idProjeto, String filePath) {
        String sSQL = "SELECT ";
        sSQL += " D.'Ativo', ";
        sSQL += " D.'NoOrigem', ";
        sSQL += " D.'NoDestino', ";
        sSQL += " D.'QuantidadeVeiculos' ";
        sSQL += "FROM 'Demandas' D ";
        sSQL += "WHERE ";
        sSQL += "D.'NoOrigem' IN ";
        sSQL += "( ";
        sSQL += " SELECT N.'idNo' FROM 'Nos' N ";
        sSQL += " WHERE ";
        sSQL += " N.'idProjeto'=" + Integer.toString(idProjeto) + " ";
        sSQL += ") ";
        sSQL += "AND ";
        sSQL += "D.'NoDestino' IN ";
        sSQL += "( ";
        sSQL += " SELECT N.'idNo' FROM 'Nos' N ";
        sSQL += " WHERE ";
        sSQL += " N.'idProjeto'=" + Integer.toString(idProjeto) + " ";
        sSQL += ") ";
        sSQL += "ORDER BY 'NoOrigem','NoDestino' ";
        sSQL = sSQL.replace("'".charAt(0), '"');
        try {
            ResultSet aResultSet = aStatement.executeQuery(sSQL);
            Demandas.clear();
            //System.out.println("Vai ler agora");
            while (aResultSet.next()) {
                Demandas.add(new Trinca(aResultSet.getInt("Ativo"),
                        aResultSet.getInt("NoOrigem"),
                        aResultSet.getInt("NoDestino"),
                        aResultSet.getInt("QuantidadeVeiculos")));
                System.out.println(
                		aResultSet.getInt("NoOrigem") + " -> " +
                		aResultSet.getInt("NoDestino") + ": " +
                		aResultSet.getInt("QuantidadeVeiculos")
                );
            }
            //System.out.println("Leu?");
            writeTo(idProjeto, filePath);
            aResultSet.close();
        } catch (Exception e) {
        	e.printStackTrace();
        	System.exit(1);
            //System.out.println(e.toString());
        }
    }

    private void connectToDataBase(String dbPath, String userName, String password) {
        //Abre a conexao com o Banco de Dados
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
            aConnection = DriverManager.getConnection(dbPath, userName, password);
            aStatement = aConnection.createStatement();
        } catch (Exception e) {
        	e.printStackTrace();
            System.exit(1);
        }
    }

    private void devolveDemandas() {
        for (int i = 0; i < Demandas.size(); i++) {
            System.out.println(
            		"Inserindo " + Demandas.get(i).getIdDemanda() +
            		" (" + Demandas.get(i).getOrigem() + "," + Demandas.get(i).getDestino() +
            		"," + Demandas.get(i).getDemanda() + ")");
            String insertSQL;
            insertSQL = "INSERT INTO 'Demandas' ";
            insertSQL += "('NoOrigem','NoDestino','QuantidadeVeiculos','Ativo') ";
            insertSQL += "VALUES ";
            insertSQL += "(" + Integer.toString(Demandas.get(i).getOrigem()) + "," +
            		Integer.toString(Demandas.get(i).getDestino()) + "," +
            		Integer.toString(Demandas.get(i).getDemanda()) + "," +
            		Integer.toString(Demandas.get(i).getIdDemanda()) + ")";
            insertSQL = insertSQL.replace("'".charAt(0), '"');
            try {
                aStatement.executeUpdate(insertSQL);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    /**
     * Salva as demandas carregadas na memoria (propriedade Demandas)
     * no banco do PETGyn.
     * 
     * @param idProjeto
     */
    private void persistirDemandas(int idProjeto)
    {
    	System.out.println("Iniciando persistirDemandas()...");
    	
        ArrayList<Integer> Origens = new ArrayList<Integer>();
        ArrayList<Integer> Destinos = new ArrayList<Integer>();
        
        int counter = 1;
        int counterO = 1;
        int counterD = 1;
        
        for (int i = 0; i < Demandas.size(); i++)
        {
        	
        	System.out.println(
            		counter++ + " - NumeroNo da demanda de referencia: "+
            		Demandas.get(i).getOrigem()+" -> "+Demandas.get(i).getDestino()
            );
        	
            String sSQL = "SELECT ";
            sSQL += " 'idNo', 'NumeroNo' ";
            sSQL += "FROM 'Nos' ";
            sSQL += "WHERE ";
            sSQL += "'NumeroNo' = " + Integer.toString(Demandas.get(i).getOrigem());
            sSQL += "AND ";
            sSQL += "'idProjeto' = " + Integer.toString(idProjeto);
            sSQL = sSQL.replace("'".charAt(0), '"');
            try {
                ResultSet aResultSet = aStatement.executeQuery(sSQL);
                //System.out.println(aResultSet.toString());
                
                while (aResultSet.next()) {
                	System.out.println("Adicionando origem para 'idNo'="+
                			aResultSet.getInt("idNo"));
                	
                	System.out.println(
                    		counterO++ + " - NumeroNo: "+aResultSet.getInt("NumeroNo")+", "+
                    		"idNo: "+aResultSet.getInt("idNo")
                    );
                    
                    Origens.add(aResultSet.getInt("idNo"));
                    //System.out.println(
                    //		Demandas.get(i).getOrigem() + " -> " + aResultSet.getInt("idNo")
                    //);
                }
                aResultSet.close();
            } catch (Exception e) {
                //System.err.println(e.toString());
            	e.printStackTrace();
            	System.exit(1);
            }
            
            sSQL = "SELECT ";
            sSQL += " 'idNo', 'NumeroNo' ";
            sSQL += "FROM 'Nos' ";
            sSQL += "WHERE ";
            sSQL += "'NumeroNo' = " + Integer.toString(Demandas.get(i).getDestino());
            sSQL += "AND ";
            sSQL += "'idProjeto' = " + Integer.toString(idProjeto);
            sSQL = sSQL.replace("'".charAt(0), '"');
            try {
                ResultSet aResultSet = aStatement.executeQuery(sSQL);
                
                while (aResultSet.next()) {
                	System.out.println("Adicionando destino para 'idNo'="+
                			aResultSet.getInt("idNo"));
                	
                	System.out.println(
                    		counterD++ + " - NumeroNo: "+aResultSet.getInt("NumeroNo")+", "+
                    		"idNo: "+aResultSet.getInt("idNo")
                    );
                	
                    Destinos.add(aResultSet.getInt("idNo"));
                    //System.out.println(
                    //		Demandas.get(i).getDestino() + " -> " + aResultSet.getInt("idNo")
                    //);
                }
                aResultSet.close();
            } catch (Exception e) {
                //System.err.println(e.toString());
            	e.printStackTrace();
            	System.exit(1);
            }
            
            System.out.println();
        }
        
        for (int i = 0; i < Demandas.size(); i++) {
            if (Demandas.size() != Origens.size())
                System.out.println("Demandas.size() ["+Demandas.size()+
                		"] != Origens.size() ["+Origens.size()+"]");
            
            if (Demandas.size() != Destinos.size())
            	System.out.println("Demandas.size() ["+Demandas.size()+
                		"] != Destinos.size() ["+Destinos.size()+"]");

            System.out.println(
            		"Inserindo " + Demandas.get(i).getIdDemanda() + " (" +
            		Origens.get(i) + "," + Destinos.get(i) + "," +
            		Demandas.get(i).getDemanda() + ")"
            );

            String insertSQL;
            insertSQL = "INSERT INTO 'Demandas' ";
            insertSQL += "('NoOrigem','NoDestino','QuantidadeVeiculos','Ativo') ";
            insertSQL += "VALUES ";
            insertSQL += "(" + Integer.toString(Origens.get(i)) + "," +
            		Integer.toString(Destinos.get(i)) + "," +
            		Integer.toString(Demandas.get(i).getDemanda()) + "," +
            		Integer.toString(Demandas.get(i).getIdDemanda()) + ")";
            insertSQL = insertSQL.replace("'".charAt(0), '"');
            try {
                aStatement.executeUpdate(insertSQL);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    /**
     * Le as demandas do FLIPSOD e carrega essas demandas
     * na memoria.
     *  
     * @param idProjeto
     * @return
     */
    public ArrayList<Trinca> readCycle(int idProjeto,
    		String filePath, String odMatrixFileName)
    {
    	//TODO
    	// Reescrever o metodo para ler do arquivo New_MatrixOD_2_output.dat
    	// no lugar de usar o arquivo Cycle_2_output.dat
    	// Os dois arquivos possuem pequenas diferencas na forma de salvar a matriz OD
    	// que devem ser compatibilizadas
    	String fileName = odMatrixFileName;
    	
        ArrayList<Trinca> dem = new ArrayList<Trinca>();
        BufferedReader inReader = null;
        String line;

        try {
            inReader = new BufferedReader( new FileReader(filePath + fileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+fileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
        	while ( (line = inReader.readLine()) != null ) {
        		StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    line = st.nextToken();
                    String o = "";
                    String d = "";
                    String v = "";
                    int i = 1;
                    int j, k;
                    while (line.charAt(i) != ',') {
                        i++;
                    }
                    o = line.substring(1, i);
                    i++;
                    j = i;
                    while (line.charAt(i) != ',') {
                        i++;
                    }
                    d = line.substring(j, i);
                    i++;
                    k = i;
                    while (line.charAt(k) != ')') {
                        k++;
                    }
                    v = line.substring(i, k);
                    dem.add(
                    		new Trinca(
                    				1,
                    				Integer.parseInt(o),
                    				Integer.parseInt(d),
                    				(int) Double.parseDouble(v)
                    		)
                    );
                    System.out.println(
                    		dem.get(dem.size() - 1).getOrigem() + "->" +
                    		dem.get(dem.size() - 1).getDestino() + ": " +
                    		dem.get(dem.size() - 1).getDemanda()
                    );
                }
        	}
            
            inReader.close();
        } catch (IOException e) {
        	e.printStackTrace();
        	System.exit(1);
        }

        return dem;
    }

    public ArrayList<Trinca> readFrom(int idProjeto, String filePath)
    {
        ArrayList<Trinca> dem = new ArrayList<Trinca>();
        BufferedReader inReader = null;
        String fileName = "Demandas_"+idProjeto+"_output.dat";
        String line;
        
        try {
            inReader = new BufferedReader( new FileReader(filePath + fileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+fileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            //System.out.println("Carregando demandas...");
            while ((line = inReader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                int a = Integer.parseInt(st.nextToken());
                line = st.nextToken();
                String o = "";
                String d = "";
                String v = "";
                int i = 1;
                int j;
                while (line.charAt(i) != ',') {
                    i++;
                }
                o = line.substring(1, i);
                i++;
                j = i;
                while (line.charAt(i) != ',') {
                    i++;
                }
                d = line.substring(j, i);
                i++;
                v = line.substring(i, line.length() - 1);
                dem.add(
                		new Trinca(
                				a,
                				Integer.parseInt(o),
                				Integer.parseInt(d),
                				Integer.parseInt(v)
                		)
                );
                //System.out.println(dem.get(dem.size()-1).getOrigem() + "->" + dem.get(dem.size()-1).getDestino() + ": " + dem.get(dem.size()-1).getDemanda() );
            }
            //System.out.println("Arcos livres carregados com sucesso!");
            inReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        	System.exit(1);
        }

        return dem;
    }

    public void writeTo(int idProjeto, String filePath) {
        PrintWriter outWriter = null;
        String line;
        String fileName = "Demandas_"+idProjeto+"_output.dat";
        
        try {
            outWriter = new PrintWriter( new FileWriter(filePath + fileName) );
        } catch (FileNotFoundException e) {
            System.err.println("O arquivo "+fileName+" nao pode ser lido.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        	System.exit(1);
        }

        try {
            System.out.println("Gerando arquivo "+fileName+"...");


            for (int i = 0; i < Demandas.size(); i++) {
                line = "";
                line = line.concat(
                		Demandas.get(i).getIdDemanda() + " (" +
                		Demandas.get(i).getOrigem() + "," +
                		Demandas.get(i).getDestino() + "," +
                		Demandas.get(i).getDemanda() + ")"
                );
                outWriter.println(line);
                System.out.println(line);
            }
            outWriter.close();
            System.out.println("Arquivo "+fileName+" gerado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        	System.exit(1);
        }
    }
}
