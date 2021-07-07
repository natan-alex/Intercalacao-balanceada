# Intercalação balanceada para ordenação externa
* Algoritmo para ordenar um grande volume de dados em memória secundária
* Dados dos arquivos data.csv e mock_data.csv foram gerados randomicamente em sites como __https://www.generatedata.com/__ e __https://www.mockaroo.com/__

### Observações:
* O código feito ignora a priori quaisquer metadados utilizados nos arquivos de dados, tornando necessário que eles tenham somente registros. * É necessário que a classe de domínio contida no arquivo a ser ordenado, ou seja, a classe cujos dados que representam seus objetos estão no arquivo que será ordenado, tenha um construtor que receba um vetor de strings(String[]) e faça o parsing desses dados para atribuir aos atributos da classe. Isso se deve ao fato de que a classe utilitária OperacoesSobreArquivos usa a classe Constructor para gerar uma nova instância da classe de domínio e, por não saber como parsear os dados lidos do arquivo para passar ao Constructor, ela envia ao construtor da classe de domínio esses dados lidos.
