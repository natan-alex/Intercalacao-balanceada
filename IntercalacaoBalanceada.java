/* 
 * AUTOR: Natan Alexandre
 * Algoritmo de ordenação externa intercalação balanceada
 */
package intercalacao_balanceada;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;
import java.io.EOFException;

import java.io.Serializable;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class IntercalacaoBalanceada<T extends Comparable<T> & Serializable> {
    private byte caminhos; // metade do número de arquivos
    // que serão usados na intercalação e distruibuição dos dados

    // número de registros que podem ser 
    // lidos com base no tamanho do registro
    // e na capacidade da memoria principal
    private int capacidadeRegistrosEmMemoria;

    // para o arquivo que contém os dados
    private FileInputStream fis;
    private ObjectInputStream ois;

    // arrays contendo as classes para manipular os arquivos que 
    // que serão usados no processo de intercalação e distribuição dos dados
    private FileOutputStream[] fileOutputs;
    private FileInputStream[] fileInputs;
    private ObjectOutputStream[] objectOutputs;
    private ObjectInputStream[] objectInputs;

    private int[] qtdBytesDisponivelNosArquivos; // quantidade de bytes para serem lidos de cada 
    // arquivo usado para a escrita durante a intercalação

    // para ler os dados de um arquivo
    private LerDadosDeUmArquivo leitor;

    // diz sobre os modos de abertura e fechamento dos arquivos:
    // utilizado também para determinar qual array utilizar para a distribuição e intercalação ->
    // se for o modo leitura, o array de fileInputs e o objectInputs devem ser utilizados
    // se for o modo escrita, o array de fileOutputs e o objectOutputs devem ser utilizados
    enum ModosDeAberturaEFechamento {
        LEITURA,
        ESCRITA
    }

    public IntercalacaoBalanceada(String arquivoASerOrdenado, int caminhos) {
        this.caminhos = (byte) caminhos;
        this.capacidadeRegistrosEmMemoria = 7;
        qtdBytesDisponivelNosArquivos = new int[caminhos];

        try {
            fis = new FileInputStream(arquivoASerOrdenado);
            ois = new ObjectInputStream(fis);
            // para os arquivos que serão usados
            fileOutputs = new FileOutputStream[caminhos];
            objectOutputs = new ObjectOutputStream[caminhos];
            fileInputs = new FileInputStream[caminhos];
            objectInputs = new ObjectInputStream[caminhos];
        } catch (IOException e) {
            System.out.println("IOException lançada no construtor da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // instanciar novos FileInputStream e ObjectInputStream, para cada item da metade especificada,
    // nos arrays de fileInputs e objectInputs
    // param modo indica o modo de abertura dos arquivos
    // param qualMetade indica qual a metade dos arrays fileInputs e objectInputs que sera usada
    private void abrirConexaoComArquivos(ModosDeAberturaEFechamento modo, int numPrimeiroArquivo) {
        // abrir conexao com arquivos
        String arquivoAtual = "";
        // nome do arquivo padrão é 'arquivo' + número + '.tmp'

        try {
            if (modo == ModosDeAberturaEFechamento.LEITURA) {
                // leitura
                for (int i = 0; i < caminhos; i++) {
                    arquivoAtual = "arquivo" + (i + numPrimeiroArquivo) + ".tmp";
                    fileInputs[i] = new FileInputStream(arquivoAtual);
                    objectInputs[i] = new ObjectInputStream(fileInputs[i]);
                }
            } else {
                // escrita
                for (int i = 0; i < caminhos; i++) {
                    arquivoAtual = "arquivo" + (i + numPrimeiroArquivo) + ".tmp";
                    fileOutputs[i] = new FileOutputStream(arquivoAtual);
                    objectOutputs[i] = new ObjectOutputStream(fileOutputs[i]);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException lançada no método abrirConexaoComArquivos da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }

        arquivoAtual = null;
    }

    // argumento recebido indica quais conexões deverão ser fechadas
    private void fecharConexaoComArquivos(ModosDeAberturaEFechamento modo) {
        try {
            if (modo == ModosDeAberturaEFechamento.LEITURA) {
                // leitura
                for (int i = 0; i < caminhos; i++) {
                    fileInputs[i].close();
                    objectInputs[i].close(); 
                }
            } else {
                // escrita
                for (int i = 0; i < caminhos; i++) {
                    fileOutputs[i].close();
                    objectOutputs[i].close(); 
                }
            }
        } catch (IOException e) {
            System.out.println("IOException lançada no método fecharConexaoComArquivos da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // fecha a conexão com arquivo de onde vieram os dados que devem ser ordenados
    private void fecharConexaoComArquivoDeDados() {
        try {
            fis.close();
            ois.close();
            fis = null;
            ois = null;
        } catch (IOException e) {
            System.out.println("IOException lançada no método fecharConexaoComArquivoDeDados da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ler capacidadeRegistrosEmMemoria registros do arquivo de dados 
    // e armazenar na lista ondeArmazenar
    // retorna a quantidade de bytes restante no arquivo
    @SuppressWarnings("unchecked")
    private int lerRegistrosDoArquivoDeDados(List<T> ondeArmazenar) {
        int qtdBytesDisponivel = 0;
        try {
            for (int i = 0; i < capacidadeRegistrosEmMemoria; i++) {
                ondeArmazenar.add( (T) ois.readObject() );
            }
            qtdBytesDisponivel = fis.available();
        } catch (EOFException e) {
            // fim do arquivo
        } catch (IOException e) {
            System.out.println("IOException lançada no método lerRegistrosDoArquivoDeDados da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException lançada no método lerRegistrosDoArquivoDeDados! Erro: " + e.getMessage());
            e.printStackTrace();
        }
        return qtdBytesDisponivel;
    }

    // escrever dados vindos de uma lista no arquivo + numDoArquivo
    // (o nome do arquivo é composto por arquivo + num, que é o num 
    // recebido como argumento da função)
    private void escreverDadosNoArquivo(List<T> dados, int numDoArquivo) {
        try {
            for (T item : dados) {
                objectOutputs[numDoArquivo].writeObject(item);
            }
        } catch (IOException e) {
            System.out.println("IOException lançada no método escreverDadosNoArquivo da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // escrever um único registro usando objectOutputs[numDoArquivo]
    private void escreverRegistroNoArquivo(T registro, int numDoArquivo) {
        try {
            objectOutputs[numDoArquivo].writeObject(registro);
        } catch (IOException e) {
            System.out.println("IOException lançada no método escreverRegistroNoArquivo da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ler um único registro usando objectInputs[numDoArquivo]
    @SuppressWarnings("unchecked")
    private T lerRegistroDoArquivo(int numDoArquivo) {
        T registroLido = null;
        try {
            registroLido = (T) objectInputs[numDoArquivo].readObject();
        } catch (EOFException e) {

        } catch (IOException e) {
            System.out.println("IOException lançada no método lerRegistroDoArquivo da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException lançada no método lerRegistroDoArquivo da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
        return registroLido;
    }

    // distribuir os dados entre os diferentes caminhos
    public void distribuirDadosEntreCaminhos() {
        // quantidade de bytes disponivel no arquivo
        int qtdBytesDisponivel = 0;

        // diz sobre o número do arquivo em que 
        // os bytes lidos serão armazenados
        int destino = 0;

        List<T> dadosASeremOrdenados = new ArrayList<>();

        // abrir conexões de escrita na primeira metade dos arquivos
        abrirConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA, 0);

        do {
            // ler o número de registros que cabem em um bloco
            // do disco, armazenar em dadosASeremOrdenados e obter a quantidade 
            // de bytes restantes no arquivo
            qtdBytesDisponivel = lerRegistrosDoArquivoDeDados(dadosASeremOrdenados);

            // System.out.println(dadosASeremOrdenados);

            // ordenar antes de inserir no arquivo da vez
            Collections.sort(dadosASeremOrdenados);

            // System.out.println(dadosASeremOrdenados);

            // escrever no arquivo da vez
            escreverDadosNoArquivo(dadosASeremOrdenados, destino);

            // somar um ao destino, de maneira circular, com
            // base nos caminhos
            destino = (destino + 1) % caminhos;

            dadosASeremOrdenados.clear();
        } while (qtdBytesDisponivel > 0);

        // fechar conexões de escrita na primeira metade dos arquivos
        fecharConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA);

        fecharConexaoComArquivoDeDados();
    }

    // obter a chave de um elemento de um hash map de inteiros e T
    private int keyOf(Map<Integer, T> dados, T item) {
        for (int key : dados.keySet()) {
            if (item.compareTo( dados.get(key) ) == 0) {
                return key;
            }
        }
        return -1;
    }

    // o número de bytes que ainda faltam ser lidos dos arquivos abertos em modo leitura
    // preencher o array qtdBytesDisponivelNosArquivos de forma a colocar em cada posição
    // o número de bytes que ainda faltam ser lidos dos arquivos abertos em modo leitura
    private void getNumBytesDisponiveis() {
        for (int i = 0; i < caminhos; i++) {
            try {
                qtdBytesDisponivelNosArquivos[i] = fileInputs[i].available();
            } catch (IOException e) {
                System.out.println("IOException lançada no método getNumBytesDisponiveis da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // retorna o número de arquivos que possuem o número de bytes > 0, com base
    // no array qtdBytesDisponivelNosArquivos
    private int getNumeroDeArquivosQueAindaPossuemDados() {
        int numArquivosComDados = 0;
        for (int i = 0; i < caminhos; i++) {
            if (qtdBytesDisponivelNosArquivos[i] > 0) {
                numArquivosComDados++;
            }
            // System.out.println("qtdBytesDisponivelNosArquivos["+i+"]: "+qtdBytesDisponivelNosArquivos[i]);
        }
        return numArquivosComDados;
    }

    public void intercalarDadosDistribuidos() {
        Map<Integer, T> registrosLidos = new HashMap<Integer, T>(); // para os registros que serão intercalados:
        int tamHashMap = 0;
        // chave é o número do arquivo de onde o registro foi lido
        // valor é o registro lido
        T menorRegistro = null;
        T registroLido = null;
        int posMenorRegistro = 0;
        int destino = 0; // índice do array objectOutputs que será usado para escrita do menor registro
        int pontoInicioEscrita = caminhos; // num do arquivo onde a escrita deve comecar: inicia em 
        // caminhos pois a primeira metade contém os dados distribuídos
        int pontoInicioLeitura = 0;
        int quantosRegistrosLer = capacidadeRegistrosEmMemoria; // quantos registros devem ser lidos de um único arquivo em cada iteração
        int numArquivosComDados = 0; // número de arquivos que possuem mais de 0 bytes disponíveis
        int[] qtdLeiturasFeitas = new int[caminhos]; // quantidade de registros que foram lidos em cada arquivo aberto em modo leitura

        // número da intercalação feita
        int numeroDaIntercalacao = 1;

        // abrir conexões com arquivos com o número do arquivo inicial ditado pelas variáveis
        // pontoInicioLeitura e pontoInicioEscrita
        abrirConexaoComArquivos(ModosDeAberturaEFechamento.LEITURA, pontoInicioLeitura);
        abrirConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA, pontoInicioEscrita);

        // preencher o vetor qtdBytesDisponivel com o número de bytes disponíveis
        // nos arquivos abertos em modo leitura
        getNumBytesDisponiveis();

        // a intercalação para quando somente um único arquivo foi usado para 
        // intercalar os dados
        do {
            // ponto de partida da intercalação:
            // pegar um registro de cada arquivo aberto em modo leitura e inserir no hash map
            // juntamente com o número do arquivo de onde o registro foi lido
            for (int i = 0; i < caminhos; i++) {
                registroLido = lerRegistroDoArquivo(i); 

                if (registroLido != null) {
                    registrosLidos.put( i, registroLido );
                }

                qtdLeiturasFeitas[i]++;
            }

            // ler e intercalar os registros restantes, tendo em vista quantosRegistrosLer
            while ( (tamHashMap = registrosLidos.size()) > 0 ) {
                // System.out.println("registrosLidos: " + registrosLidos);

                // obter menor registro e o número do arquivo de onde ele foi lido
                menorRegistro = Collections.min(registrosLidos.values());
                posMenorRegistro = keyOf(registrosLidos, menorRegistro);

                // escrever o menor registro no arquivo de destino
                // o número do arquivo de destino é igual a (destino + pontoInicioEscrita)
                escreverRegistroNoArquivo(menorRegistro, destino);

                // ler do arquivo em que o menor registro foi encontrado caso ainda
                // nao tenha lido a quantidade de registros que deveria ser lida
                if (qtdLeiturasFeitas[posMenorRegistro] < quantosRegistrosLer) {
                    registroLido = lerRegistroDoArquivo(posMenorRegistro); 

                    if (registroLido != null) {
                        // substituir o menor registro pelo recém lido
                        registrosLidos.put( posMenorRegistro, registroLido );
                        qtdLeiturasFeitas[posMenorRegistro]++;
                    } else {
                        // se nulo, o arquivo acabou, portanto já leu o que tinha que ler
                        // além disso o registro é excluído do Map para não ser lido novamente
                        qtdLeiturasFeitas[posMenorRegistro] = quantosRegistrosLer;
                        registrosLidos.remove( posMenorRegistro );
                    }
                } else {
                    // se todos os registros que deveriam ser lidos do arquivo de número posMenorRegistro
                    // já foram lidos, remover o registro na posição posMenorRegistro do hashmap para 
                    // possibilitar que o menor seja encontrado corretamente
                    registrosLidos.remove( posMenorRegistro );
                }
            }

            // ler o conteúdo do arquivo que foi utilizado para intercalar os dados
            leitor = new LerDadosDeUmArquivo("arquivo" + (destino + pontoInicioEscrita) + ".tmp");
            System.out.println("  Após a " + (numeroDaIntercalacao++) + "ª intercalação");
            leitor.lerDados();
            System.out.println();

            registrosLidos.clear();

            // zerar a quantidade de leituras feitas em cada arquivo
            for (int i = 0; i < caminhos; i++) {
                qtdLeiturasFeitas[i] = 0;
            }

            // novamente, obter a quantidade de bytes que ainda restam nos arquivos abertos em
            // modo leitura
            getNumBytesDisponiveis();

            numArquivosComDados = getNumeroDeArquivosQueAindaPossuemDados();

            // se todos os arquivos abertos em modo leitura chegaram ao fim
            if (numArquivosComDados == 0) {
                // necessário inverter os modos de abertura dos arquivos e as metades 
                // em que serão feitas a leitura e a escrita
                fecharConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA);
                fecharConexaoComArquivos(ModosDeAberturaEFechamento.LEITURA);

                // inverter os números dos arquivos onde devem começar a leitura e a escrita
                pontoInicioLeitura = (pontoInicioLeitura == 0) ? caminhos : 0;
                pontoInicioEscrita = (pontoInicioEscrita == 0) ? caminhos : 0;

                // o que era escrita vira leitura, e o que era leitura vira escrita
                abrirConexaoComArquivos(ModosDeAberturaEFechamento.LEITURA, pontoInicioLeitura);
                abrirConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA, pontoInicioEscrita);

                // quantos registros devem ser lidos de cada arquivo
                quantosRegistrosLer = quantosRegistrosLer * caminhos;

                getNumBytesDisponiveis();

                // após a troca das metades de leitura e escrita, checar o número 
                // de bytes dos arquivos abertos em modo leitura
                numArquivosComDados = getNumeroDeArquivosQueAindaPossuemDados();
            }

            // trocar o destino de maneira circular
            destino = (destino + 1) % caminhos;

            // condição de parada: o número de bytes de um único arquivo deve ser superior a 0,
            // de modo que somente um arquivo foi utilizado para intercalar os dados última intercalação
        } while (numArquivosComDados != 1);

        // passar para o método finalizar() o número do arquivo que contém 
        // os dados ordenados, que corresponde ao último número do destino + pontoInicioEscrita 
        // anterior, que passou a ser o pontoInicioLeitura
        if (destino == 0) {
            finalizar( (caminhos - 1) + pontoInicioLeitura );
        } else {
            finalizar( (destino - 1) + pontoInicioLeitura );
        }
    }

    private void finalizar(int numDoArquivoComOsDados) {
        boolean removido = false;
        boolean renomeado = false;
        String nomeDoArquivo = "";

        fecharConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA);
        fecharConexaoComArquivos(ModosDeAberturaEFechamento.LEITURA);

        // atribuir null aos arrays usados na distruibuição e intercalação
        for (int i = 0; i < caminhos; i++) {
            fileInputs[i] = null;
            fileOutputs[i] = null;
            objectInputs[i] = null;
            objectOutputs[i] = null;
        }
        // deletar arquivos desnecessários
        for (int i = 0; i < 2*caminhos; i++) {
            if (i != numDoArquivoComOsDados) {
                nomeDoArquivo = "arquivo"+i+".tmp";
                removido = ( new File(nomeDoArquivo) ).delete();
                if (removido) {
                    // System.out.println("Arquivo " + nomeDoArquivo + " removido.");
                }
            }
        }

        // renomear arquivo com dados ordenados
        renomeado = ( new File("arquivo"+numDoArquivoComOsDados+".tmp") ).renameTo( new File("dados_ordenados.db") );
    }
}
