/*
 * Esta classe extrai o grafo de um projeto do
 * PET e gera arquivos .dat com o resultado.
 */
package extractor;

import pet_class.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author Vitor Castro Veloso Soares
 */
public class Extractor {

    public PET pet = null;
    
    public static void main(String[] args) {
        Extractor extractor = new Extractor(2, false);
    }
    
    public Extractor(int idProjeto, boolean cycle) {
        pet = new PET();
        try {
            //Abre a conexao com o Banco de Dados
            pet.connectToDataBase(
            		//"jdbc:firebirdsql:localhost/3050:D://Documents//NetBeansProjects//PFC//pet.fdb",
            		"jdbc:firebirdsql://localhost:3050//usr/share/firebird/2.1/pet.fdb",
            		"SYSDBA",
            		"masterkey"
            );
            
            System.out.println("Carregando projeto...");
            pet.loadProject(idProjeto, 1);
            pet.performPET_EU();
            System.out.println("Projeto carregado!");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.toString());
        }
        
        if (cycle)
            geraCustoNosArcos(idProjeto);
        else {
        	//TODO
        	// Utilizar o metodo geraCustosVelocidadeLivreNosArcos(int idProjeto)
        	// para obter os custos de velocidade livre nos arcos e compor com as
        	// capacidades
            geraGrafo(idProjeto);
            geraArcosLivres(idProjeto);
            geraCustoNosArcos(idProjeto);
            geraNodesXY(idProjeto);
        }
    }
    
    /**
     * 
     * @param idProjeto
     */
    private void geraCustosVelocidadeLivreNosArcos(int idProjeto) {
    	PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("LinkCostFreeFlow_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        try {
            System.out.println("Gerando arquivo LinkCostFreeFlow_" + idProjeto + "_output.dat...");
            for (int i = 0; i < pet.Ruas.size(); i++) {
            	double freeFlowCost = (
            			pet.Ruas.get(i).getComprimento() * 3.6 / pet.Ruas.get(i).getVelocidadeLivre()
            	);
                line = "";
                line = line.concat(Integer.toString(i) + " (" + pet.Ruas.get(i).getOrigem()
                        + "," + pet.Ruas.get(i).getDestino() + ") " + (freeFlowCost)
                );
                outWriter.println(line);
            }
            outWriter.close();
            System.out.println("Arquivo LinkCostFreeFlow_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void geraNodesXY(int idProjeto) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("NodesXY_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            System.out.println("Gerando arquivo NodesXY_" + idProjeto + "_output.dat...");
            for (int i = 0; i < pet.Nos.size(); i++) {
                outWriter.println(
                		pet.Nos.get(i).getNumeroNo() + " " +
                		pet.Nos.get(i).getCoordenadaX() + " " +
                		pet.Nos.get(i).getCoordenadaY()
                );
            }
            outWriter.close();
            System.out.println("Arquivo NodesXY_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void geraGrafo(int idProjeto) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("Graph_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            System.out.println("Gerando arquivo Graph_" + idProjeto + "_output.dat...");
            outWriter.println("Grafo correspondente ao projeto " + idProjeto);
            outWriter.printf("%d %d\n", pet.Nos.size(), pet.Ruas.size());
            line = "";
            for (int i = 0; i < pet.Nos.size(); i++) {
                line = line.concat(Integer.toString(pet.Nos.get(i).getNumeroNo()) + " ");
            }
            outWriter.println(line);
            for (int i = 0; i < pet.Nos.size(); i++) {
                line = "";
                Iterator it = pet.Ruas.iterator();
                while (it.hasNext()) {
                    Arco r = (Arco) it.next();
                    if (r.getOrigem() == pet.Nos.get(i).getNumeroNo()) {
                        line = line.concat("(" + r.getOrigem() + "," + r.getDestino() + ") ");
                    }
                }
                if (!line.equals("")) {
                    outWriter.println(line);
                }
            }
            
            //TODO
            // -------------------------------------------------------------
            // Adicionar as entradas dos possiveis pares OD
            // a partir do PETGyn.
            
            // Para isso dever-se-a criar um SELECT que busque os
            // pares OD no banco. Os valores atualmente buscados no
            // object pet.Demandas nao contem todos os valores,
            // mas somente as entradas OD nao nulas.
            
            // Ainda, nao adianta modificar somente o SELECT chamado na
            // funcao pet.carregaDemandas(idProjeto), porque as demandas
            // nao-nulas nao sao salvas no banco. Deve-se, portanto, salvar
            // as demandas nao-nulas no banco. Isto eh feito atraves de JSP
            // no PET.
            // -------------------------------------------------------------
            
            // Atualmente, iremos utilizar o arquivo
            // Project_2_output_Estimations.dat
            // para adicionar tanto os pares OD como tambem as estimacoes.
            BufferedReader inReader = null;
            try {
                inReader = new BufferedReader(
                		new FileReader(
                				"Project_" + Integer.toString(idProjeto) + "_output_Estimations.dat"
                		)
                );
            } catch (FileNotFoundException e) {
                System.err.println("O arquivo Project_" + Integer.toString(idProjeto) +
                		"_output_Estimations.dat nao pode ser lido.");
                System.exit(1);
            }
            
            // Adicionar o conteudo do arquivo lido no arquivo de output
            while ( (line = inReader.readLine()) != null )
            {
            	outWriter.println(line);
            }
            
            /*
            // Criar a linha com o $ no grafo
            outWriter.println("$");
            
            //System.out.println("pet.Demandas.size(): "+pet.Demandas.size());
            
            // Apos adicionar os nos e arestas do grafo, criar entradas
            // para todos os pares OD possiveis.
            String entry = "";
            for (int i=0; i<pet.Demandas.size(); i++)
            {
            	Trinca t = pet.Demandas.get(i);
            	int origem = t.getOrigem();
            	int destino = t.getDestino();
            	String partial = "(" + origem + "," + destino + ") ";
            	
            	//System.out.println("t.getOrigem(): "+t.getOrigem());
            	//System.out.println("t.getDestino(): "+t.getDestino());
            	
            	// Adicionar a entrada no final da string
            	entry += partial;
            }
            
            // Adicionar a entrada dos pares OD em uma linha
            outWriter.println(entry);
            */
            
            outWriter.close();
            System.out.println("Arquivo Graph_" +  idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        //TODO
        // Gerar os pares OD atraves das demandas na tabela Demandas
        // e adiciona-los ao arquivo Graph_2_output.dat
    }

    private void geraArcosLivres(int idProjeto) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("ArcosLivres_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            System.out.println("Gerando arquivo ArcosLivres_" + idProjeto + "_output.dat...");
            for (int i = 0; i < pet.Nos.size(); i++) {
                line = "";
                Iterator it = pet.arcosLivres.iterator();
                while (it.hasNext()) {
                    ArcoLivre a = (ArcoLivre) it.next();
                    if (a.getNoOrigem1() == pet.Nos.get(i).getNumeroNo()) {
                        line = line.concat(
                        		"(" + a.getNoOrigem1() + "," + a.getNoDestino1() +
                        		"," + a.getNoDestino2() + ") "
                        );
                    }
                }
                if (!line.equals("")) {
                    outWriter.println(line);
                }
            }
            outWriter.close();
            System.out.println("Arquivo ArcosLivres_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void geraCustoNosArcos(int idProjeto) {
        PrintWriter outWriter = null;
        String line;
        try {
            outWriter = new PrintWriter(
                    new FileWriter("CustoNosArcos_" + Integer.toString(idProjeto) + "_output.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Erro ao abrir arquivo");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            System.out.println("Gerando arquivo CustoNosArcos_" + idProjeto + "_output.dat...");
            for (int i = 0; i < pet.Ruas.size(); i++) {
                line = "";
                line = line.concat(Integer.toString(i) + " (" + pet.Ruas.get(i).getOrigem()
                        + "," + pet.Ruas.get(i).getDestino() + ") " + pet.Ruas.get(i).getTempo());
                outWriter.println(line);
            }
            outWriter.close();
            System.out.println("Arquivo CustoNosArcos_" + idProjeto + "_output.dat gerado com sucesso!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

// SELECT para buscar custos de velocidade livre nos arcos
//SELECT
//(SELECT n2."NumeroNo" FROM "Nos" n2 WHERE n2."idNo" = a."NoOrigem") AS "NoOrigem",
//(SELECT n3."NumeroNo" FROM "Nos" n3 WHERE n3."idNo" = a."NoDestino") AS "NoDestino",
//a."Comprimento", a."VelocidadeLivre", a."Nome",
//(a."Comprimento" * 3.6 / a."VelocidadeLivre") AS "CapacidadeNoOrigem"
//FROM "Arcos" a
//INNER JOIN "Nos" n ON a."NoDestino" = n."idNo"
//INNER JOIN "Nos" n1 ON a."NoOrigem" = n1."idNo"
//WHERE n."idProjeto" = 2 AND n1."idProjeto" = 2
//ORDER BY "NoOrigem", "NoDestino"

// SELECT para buscar arcos livres existentes para o projeto id=2
//SELECT a.*,
//(SELECT n1."NumeroNo" FROM "Nos" n1 WHERE n1."idNo" = n1o."idNo") AS "NoOrigem",
//(SELECT n2."NumeroNo" FROM "Nos" n2 WHERE n2."idNo" = n1d."idNo") AS "NoIntermediario",
//(SELECT n2."NumeroNo" FROM "Nos" n2 WHERE n2."idNo" = n2d."idNo") AS "NoDestino"
//FROM "ArcosLivres" a
//INNER JOIN "Arcos" ao ON ao."idArco" = a."idArcoOrigem"
//INNER JOIN "Arcos" ad ON ad."idArco" = a."idArcoDestino"
//INNER JOIN "Nos" n1o ON ao."NoOrigem" = n1o."idNo"
//INNER JOIN "Nos" n1d ON ao."NoDestino" = n1d."idNo"
//INNER JOIN "Nos" n2o ON ad."NoOrigem" = n2o."idNo"
//INNER JOIN "Nos" n2d ON ad."NoDestino" = n2d."idNo"
//WHERE n1o."idProjeto"=2