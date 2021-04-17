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
    // armazenados e ordenados em memoria principal
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

    private int[] qtdBytesDisponivelNosArquivos; // quantidade de bytes restantes em cada um dos
    // arquivos abertos em modo leitura durante a intercalação dos dados

    // para ler os dados de um arquivo
    private LerDadosDeUmArquivo leitor;

    // diz sobre os modos de abertura e fechamento dos arquivos:
    // utilizado também para determinar qual array utilizar para a distribuição e intercalação ->
    // se for o modo leitura, os arrays de fileInputs e o objectInputs serão utilizados
    // se for o modo escrita, os arrays de fileOutputs e o objectOutputs serão utilizados
    enum ModosDeAberturaEFechamento {
        LEITURA,
        ESCRITA
    }

    public IntercalacaoBalanceada(String arquivoASerOrdenado, int caminhos) {
        this.caminhos = (byte) caminhos;
        this.capacidadeRegistrosEmMemoria = 20; // número fictício somente para testes
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

    // instanciar novos FileInput/OutputStream e ObjectInput/OutputStream nos arrays fileIn/Outputs e objectIn/Outputs
    // com base no modo de abertura que indica também quais arrays serão usados e no número do primeiro arquivo
    // que será aberto -> sempre serão instanciados caminhos objetos
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
            System.out.println("ClassNotFoundException lançada no método lerRegistrosDoArquivoDeDados da classe IntercalacaoBalanceada! Erro: " + e.getMessage());
            e.printStackTrace();
        }
        return qtdBytesDisponivel;
    }

    // escrever dados vindos de uma lista no arquivo + numDoArquivo
    // (o nome do arquivo é composto por arquivo + num, que é o num 
    // recebido como argumento do método)
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
    // retorna null em caso de fim de arquivo
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
        // quantidade de bytes disponivel no arquivo com os dados a serem ordenados
        int qtdBytesDisponivel = 0;

        // diz sobre o número do arquivo em que 
        // os bytes lidos serão armazenados
        int destino = 0;

        List<T> dadosASeremOrdenados = new ArrayList<>();

        // abrir conexões de escrita na primeira metade dos arquivos
        abrirConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA, 0);

        do {
            // ler capacidadeRegistrosEmMemoria registros, armazenar em dadosASeremOrdenados e obter a quantidade 
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

    // preencher o array qtdBytesDisponivelNosArquivos de forma a colocar em cada posição
    // o número de bytes que ainda faltam ser lidos do arquivo aberto em modo leitura no índice do array
    // objectInputs nessa posição
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
        // chave é índice do array objectInputs de onde o registro foi lido
        // valor é o registro lido
        T menorRegistro = null;
        T registroLido = null;
        int posMenorRegistro = 0;
        int destino = 0; // índice do array objectOutputs que será usado para escrita do menor registro
        int pontoInicioEscrita = caminhos; // num do arquivo onde a escrita deve comecar: inicia em 
        // caminhos pois a primeira metade contém os dados distribuídos
        int pontoInicioLeitura = 0;
        int quantosRegistrosLer = capacidadeRegistrosEmMemoria; // quantos registros devem ser lidos de um único arquivo em cada iteração
        int[] numArquivosComDados = new int[2]; // número de arquivos que possuem mais de 0 bytes disponíveis, isso para cada uma das metades dos arquivos
        int[] qtdLeiturasFeitas = new int[caminhos]; // quantidade de registros que foram lidos em cada arquivo aberto em modo leitura
        int metadeLendo = 0; // inicia lendo os arquivos da primeira metade
        boolean fim = false;

        // // número da intercalação feita
        // int numeroDaIntercalacao = 1;

        // abrir conexões com arquivos com o número do arquivo inicial ditado pelas variáveis
        // pontoInicioLeitura e pontoInicioEscrita
        abrirConexaoComArquivos(ModosDeAberturaEFechamento.LEITURA, pontoInicioLeitura);
        abrirConexaoComArquivos(ModosDeAberturaEFechamento.ESCRITA, pontoInicioEscrita);

        // condição de parada: um único arquivo deve ter um número de bytes disponíveis superior a 0,
        // de modo que somente um arquivo foi utilizado para intercalar os dados na última intercalação
        // fim == true quando numArquivosComDados em uma das posições
        // for 0 e na outra for 1, ou seja, se os dados dos arquivos
        // em uma das metades tiverem acabado e na outra metade só tiver um arquivo com dados
        while (!fim) {
            // ponto de partida da intercalação:
            // pegar um registro de cada arquivo aberto em modo leitura e inserir no hash map
            // juntamente com o índice do array objectInputs de onde o registro foi lido
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

                // obter menor registro e o índice do array de onde ele foi lido
                menorRegistro = Collections.min(registrosLidos.values());
                posMenorRegistro = keyOf(registrosLidos, menorRegistro);

                // escrever o menor registro no arquivo de destino
                // o número do arquivo de destino é igual a (destino + pontoInicioEscrita)
                escreverRegistroNoArquivo(menorRegistro, destino);

                // ler do índice em que o menor registro foi encontrado caso ainda
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
                    // se todos os registros que deveriam ser lidos do índice de número posMenorRegistro
                    // já foram lidos, remover o registro na posição posMenorRegistro do hashmap para 
                    // possibilitar que o menor seja encontrado corretamente
                    registrosLidos.remove( posMenorRegistro );
                }
            }

            // // ler o conteúdo do arquivo que foi utilizado para intercalar os dados: descomentar caso 
            // // queira ver todos os registros em cada arquivo em cada passo da intercalação
            // leitor = new LerDadosDeUmArquivo("arquivo" + (destino + pontoInicioEscrita) + ".tmp");
            // System.out.println("  Após a " + (numeroDaIntercalacao++) + "ª intercalação");
            // leitor.lerDados();
            // System.out.println();

            registrosLidos.clear();

            // zerar a quantidade de leituras feitas em cada índice
            for (int i = 0; i < caminhos; i++) {
                qtdLeiturasFeitas[i] = 0;
            }

            // novamente, obter a quantidade de bytes que ainda restam nos arquivos abertos em
            // modo leitura
            getNumBytesDisponiveis();

            // checar o número de arquivos que ainda contem dados disponíveis para serem lidos e 
            // armazenar no índice que corresponde a metade aberta em modo leitura em numArquivosComDados
            numArquivosComDados[metadeLendo] = getNumeroDeArquivosQueAindaPossuemDados();
            // System.out.println("numArquivoComDados: " + numArquivosComDados);

            // se todos os arquivos abertos em modo leitura chegaram ao fim
            if (numArquivosComDados[metadeLendo] == 0) {
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

                // alternar metade em que os dados estão sendo lidos
                metadeLendo = (metadeLendo + 1) % 2;

                // quantos registros devem ser lidos de cada índice do array objectInputs
                quantosRegistrosLer = quantosRegistrosLer * caminhos;

                getNumBytesDisponiveis();

                // após a troca das metades de leitura e escrita, checar o número 
                // de bytes dos arquivos abertos em modo leitura
                numArquivosComDados[metadeLendo] = getNumeroDeArquivosQueAindaPossuemDados();
            }

            // trocar o destino de maneira circular
            destino = (destino + 1) % caminhos;

            // se não houverem dados nos arquivos de uma das metades
            // e na outra metade somente um arquivo contiver dados
            // padrão: se o número de caminhos for par, a primeira metade
            // será a que vai conter o arquivo com os dados ordenados
            // se for ímpar, vai ser a segunda metade
            if (caminhos % 2 == 0) {
                if ( numArquivosComDados[0] == 1 && numArquivosComDados[1] == 0) {
                    fim = true;
                }
            } else {
                if ( numArquivosComDados[0] == 0 && numArquivosComDados[1] == 1) {
                    fim = true;
                }
            }
        }

        // passar para o método finalizar() o índice do array de objectInputs que contém 
        // os dados ordenados, que corresponde ao último número do destino + o pontoInicioEscrita 
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

        fileInputs = null;
        fileOutputs = null;
        objectInputs = null;
        objectOutputs = null;

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
