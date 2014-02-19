Autor: Iacer Coimbra Alves Cavalcanti Calixto

Data: 19/08/2011, 18:51:34



Qual a estrutura do FLIPSOD?

O FLIPSOD é um software construido em Java. Seu código-fonte encontra-se dentro da pasta 'src', que possui pacotes nos quais suas classes encontram-se disponíveis.

No pacote 'cplexModels' encontram-se as classes e interfaces Java que encapsulam a criação de um programa linear no formato adotado pelo IBM ILOG CPLEX.
No pacote 'extractor' encontram-se as classes utilizadas para a contrução da interface entre o FLIPSOD e o sistema PETGyn.
No pacote 'flowCostMapping' encontram-se a classe utilizada para o mapeamento dos fluxos nos arcos em custos nos arcos (via função BPR) e também a classe utilizada para o mapeamento dos erros nos dados de entrada no formato de gradiente.
No pacote 'odEstimation' estão todas as classes de 'negócio' utilizadas no processo de estimação de matrizes OD.
No pacote 'parser' estão as classes e interfaces utilizadas para o parsing dos arquivos de dados de entrada do FLIPSOD.
No pacote 'patch' estão as classes utilizadas para a deleção de arquivos temporários gerados pelo FLIPSOD e também a classe utilizada para gerar os custos nos arcos a partir dos custos nas rotas.
No pacote 'postprocessing' estão as classes utilizadas para criar as visualizações de fluxos nos arcos e de matriz OD (não utilizadas). Além destas, há classes usadas para guardar as soluções geradas pelo FLIPSOD e para calcular as medidas estatísticas RMSE, RMSE(OD), MAE e MAE(OD).
No pacote 'tests' estão as classes que guardam as configurações de testes rodadas com o FLIPSOD. Atualmente há testes para a The Corridor Network e para a Região A.
No pacote 'v7' encontram-se as classes que geram as interfaces com o usuário e permitem rodar tanto o FLIPSOD quanto o método de Sherali et al. (1994). A classe para usar Sherali et al. (1994) está desatualizada.



Como executar os testes criados para o FLIPSOD?

Há arquivos que contém configurações de testes configuradas de forma completa dentro do pacote 'tests' na pasta 'src'.

IMPORTANTE: Antes de executar qualquer uma das configurações de testes existentes, apague o arquivos que possam existir na pasta de output do método, descrita pela variável 'outputFilePath'.

A classe GraphLinkCorridorNetworkVisualization.java e GraphLinkRegionAVisualization.java são utilizadas para gerar as visualizações de fluxos nos arcos para as redes viárias The Corridor Network e Região A, respectivamente.

IMPORTANTE: Antes de executar os testes, você precisará alterar algumas configurações nestas classes para gerar as visualizações corretamente. As visualizações são geradas a partir de uma pasta com arquivos de saída gerados pelo FLIPSOD. Para gerar a visualização somente para o problema que se deseja, comente as linhas correspondentes aos testes para os quais não se deseja na função init(). Caso contrário, serão geradas as visualizações para todas as configurações de testes sobre a The Corridor Network ou Região A (para as classes GraphLinkCorridorNetworkVisualization.java e GraphLinkRegionAVisualization.java, respectivamente).

A classe CorridorNetworkTestBed.java permite a execução de testes para a rede viária The Corridor Network. Dentro das funções doTestFiftyPercentOfLinkCountsAvailable(), doTestTwoThirdsOfLinkCountsAvailable() e doTestAllLinkCountsAvailable(), encontram-se todos os testes realizados. Estes testes usam uma pasta para ler os dados de entrada e outra para gravar os dados de saída, representados pelas variáveis inputFilePath e outputFilePath, respectivamente. Cada um dos testes realizados chama a função doCorridorNetworkTest(), na qual o teste é efetivamente criado e executado. Vários parâmetros são setados durante a execução desta função e podem ser modificados.

O mesmo vale para a classe RegionATestBed.java, que permite a execução de testes para a rede viária Região A. Na função doTestbedRegionA() estão descritos os cinco testes utilizados na dissertação de mestrado que propõe o FLIPSOD, dados por TR1, TR2, TR3, TR4 e TR5. Cada um destes testes chama uma função firstTest(), secondTest(), thirdTest(), ..., eleventhTest(), nas quais os testes são efetivamente criados e executados. Há vários parâmetros que podem ser setados nessas funções. Atualmente somente cinco dessas funções são utilizadas, já que dos 16 (?) testes pensados para a região A somente cinco (5) deles entraram na dissertação de mestrado que propõe o FLIPSOD.

A classe RegionATestBedMaximize.java é idêntica à anterior, com a diferença que usa um modelo de programação linear diferente, proposto para a construção do artigo EJOR 2011/2012.

A classe ElapsedTimeTest.java permite a gravação dos tempos de execução dos testes realizados.



Como utilizar o FLIPSOD para criar um teste?

Primeiramente, baseie-se em algum dos testes já criados através das classes CorridorNetworkTestBed.java, RegionATestBed.java ou RegionATestBedMaximize.java. Altere as configurações da classe ElapsedTimeTest.java para que esta gere os resultados dos tempos de execução em um arquivo de texto com o nome e pasta desejados.

A variável 'baseFilePath' guarda o caminho para os arquivos de entrada utilizados pelo FLIPSOD. Caso se utilize um projeto importado do PETGyn, o seguinte arquivo de entrada deverá existir com o seguinte nome (como exemplo, para um projeto de id 12 no PETGyn):
Project_12_output_estimations.dat - contém as observações de entrada para o projeto do PETGyn.

As variáveis firstTestFilePath, secondTestFilePath, (...), eleventhTestFilePath guardam o caminho para os arquivos de saída gerados pelo FLIPSOD. Mais uma vez, caso se utilize um projeto importado do PETGyn, os seguintes arquivos de saída serão gerados e terão os seguintes nomes (como exemplo, para um projeto de id 12 no PETGyn):
ArcosLivres_12_output.dat - contém os arcos livres
CustosNosArcos_12_output.dat - contém os custos iniciais nos arcos para o grafo G.
CompleteInputFile_12_output.dat - contém o grafo G' final transformado, com os pares OD e todas as observações de entrada também transformadas, incluíndo os custos iniciais nos arcos.
Graph_12_output.dat - contém o grafo G importado do PETGyn, com os pares OD e as observações de entrada.
Demandas_12_output.dat - não sei o que contém.
Map_12_output.dat - contém as representações do mapeamento entre o grafo G e o grafo G'.
New_CustosNosArcos_12_output.dat - custos nos arcos do grafo G' transformado.
New_Graph_12_output.dat - contém o grafo G' transformado a partir do grafo G, com os pares OD e as observações de entrada.
NodesXY_12_output.dat - contém a latitude e longitude para todos os nós do grafo G.
Problem M2.lp - contém a representação do LP do programa linear que calcula o limitante superior para o problema fuzzy.
Problem M3.lp - contém a representação do LP do programa linear que calcula o limitante inferior para o problema fuzzy.
Problem M8 - Original.lp - contém a representação do LP do programa linear que calcula o problema fuzzy.

Altere as configurações em sua classe para os parâmetros que se deseja, através das funções setMethodClassName(), setUseCodedErrors(), setUseGradientErrors(), setA(), setB(), setD(), setE(), setIdProjeto(), setSmoothingMultiplier(), setNumberOfRoutesPerODPair(), setLambdaMaximumError(), setArcFlowsMaximumError(), setOdMatrixMaximumError(), setUserEquilibrium(), setMixedIntegerProgramming(), setSimpleEstimationModel(), setArcCostUpdatingStrategy() e setBPRFunctionName().

Após a execução dos testes, deve-se gerar as visualizações para fluxos nos arcos. Para construir a classe que irá desenhar o grafo através do GraphViz, deve basear na classe GraphLinkRegionAVisualization.jar e GraphLinkCorridorNetworkVisualization.jar.

Antes disso, deve-se utilizar a classe GenerateLinkCountsFromRouteCountsPatch.java para gerar os fluxos nos arcos a partir dos fluxos recém-estimados nas rotas. Após gerar o fluxos nos arcos em um arquivo, deve-se utilizar este arquivo na execução da classe que irá gerar as visualizações de fluxos nos arcos.
