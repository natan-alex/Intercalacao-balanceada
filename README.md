# Ordenacao_externa
* Algoritmos para ordenar um grande volume de dados em memória secundária
* Dados dos arquivos data.csv e mock_data.csv gerados randomicamente em sites como __https://www.generatedata.com/__ e __https://www.mockaroo.com/__
* Observações: o algoritmo de Intercalação Balanceada ignora a priori quaisquer metadados utilizados nos arquivos, tornando necessário que os arquivos tenham somente registros. Além disso, a classe GeradorDeDados é responsável por escrever, em dados_fonte.db, os registros lidos do arquivo passado como argumento contendo diversos registros de Pessoa, isso utilizando a classe ObjectOutputStream para possiblitar a leitura com ObjectInputStream.
