package intercalacao_balanceada;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;
import java.io.EOFException;

import java.io.Serializable;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class IntercalacaoBalanceada<T extends Comparable<T> & Serializable> {
    public static final String PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS = "arquivo";
    public static final String SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS = ".tmp";

    private int numeroDeCaminhos; 
    private int quantosRegistrosLerDeCadaArquivo;

    private ObjectInputStream objectInputParaOArquivoDeDados;
    private FileInputStream fileInputParaOArquivoDeDados;

    private FileOutputStream[] fileOutputsParaOsArquivosTemporarios;
    private ObjectOutputStream[] objectOutputsParaOsArquivosTemporarios;

    private FileInputStream[] fileInputsParaOsArquivosTemporarios;
    private ObjectInputStream[] objectInputsParaOsArquivosTemporarios;

    private int[] numeroDeBytesDisponiveisNosArquivosTemporarios; 

    private int numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura;
    private int numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita;

    private List<T> registrosLidosDoArquivoDeDados;

    private Map<Integer, T> indicesEOsRegistrosLidosDessesIndices;
    private int[] numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs;
    private T menorRegistroDoMapDeIndicesERegistros;
    private int indiceDeOndeOMenorRegistroFoiLido;

    private int numeroDeArquivosTemporariosDaMetadeSendoLidaQueAindaPossuemDados;
    private int numeroDeArquivosDaOutraMetadeDosArquivosTemporariosQueAindaPossuemDados;

    enum ModosDeAberturaEFechamentoDeArquivos {
        LEITURA,
        ESCRITA
    }

    public IntercalacaoBalanceada(String nomeDoArquivoASerOrdenado, int numeroDeCaminhos) throws IOException {
        this.numeroDeCaminhos = numeroDeCaminhos;
        quantosRegistrosLerDeCadaArquivo = 20; // número fictício somente para testes
        numeroDeBytesDisponiveisNosArquivosTemporarios = new int[numeroDeCaminhos];
        numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs = new int[numeroDeCaminhos];
        indicesEOsRegistrosLidosDessesIndices = new HashMap<Integer, T>(numeroDeCaminhos);

        fileInputParaOArquivoDeDados = new FileInputStream(nomeDoArquivoASerOrdenado);
        objectInputParaOArquivoDeDados = new ObjectInputStream(fileInputParaOArquivoDeDados);

        fileInputsParaOsArquivosTemporarios = new FileInputStream[numeroDeCaminhos];
        objectInputsParaOsArquivosTemporarios = new ObjectInputStream[numeroDeCaminhos];

        fileOutputsParaOsArquivosTemporarios = new FileOutputStream[numeroDeCaminhos];
        objectOutputsParaOsArquivosTemporarios = new ObjectOutputStream[numeroDeCaminhos];
    }


    public void distribuirOsDadosEntreOsCaminhos() {
        registrosLidosDoArquivoDeDados = new ArrayList<>(quantosRegistrosLerDeCadaArquivo);

        int numeroDeBytesDisponiveisNoArquivoDeDados = 0;
        int indiceDaConexaoOndeInserirOsRegistros = 0;

        numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita = 0;
        abrirConexoesComArquivosTemporarios(ModosDeAberturaEFechamentoDeArquivos.ESCRITA);

        numeroDeBytesDisponiveisNoArquivoDeDados = obterONumeroDeBytesRestantesNoArquivoDeDados();

        while (numeroDeBytesDisponiveisNoArquivoDeDados > 0) {
            registrosLidosDoArquivoDeDados.clear();
            lerRegistrosDoArquivoDeDadosEAtualizarAtributoCorrespondente();
            Collections.sort(registrosLidosDoArquivoDeDados);
            escreverRegistrosLidosDoArquivoDeDadosEmUmOutroArquivo(indiceDaConexaoOndeInserirOsRegistros);
            indiceDaConexaoOndeInserirOsRegistros = (indiceDaConexaoOndeInserirOsRegistros + 1) % numeroDeCaminhos;
            numeroDeBytesDisponiveisNoArquivoDeDados = obterONumeroDeBytesRestantesNoArquivoDeDados();
        }

        fecharConexoesComArquivosTemporarios(ModosDeAberturaEFechamentoDeArquivos.ESCRITA);
        fecharConexaoComArquivoDeDados();
    }


    private void abrirConexoesComArquivosTemporarios(ModosDeAberturaEFechamentoDeArquivos modo) {
        try {
            if (modo == ModosDeAberturaEFechamentoDeArquivos.LEITURA) {
                for (int i = 0; i < numeroDeCaminhos; i++) {
                    fileInputsParaOsArquivosTemporarios[i] = new FileInputStream(
                        PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS + 
                        (i + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura) +
                        SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS
                    );
                    objectInputsParaOsArquivosTemporarios[i] = new ObjectInputStream(fileInputsParaOsArquivosTemporarios[i]);
                }
            } else {
                for (int i = 0; i < numeroDeCaminhos; i++) {
                    fileOutputsParaOsArquivosTemporarios[i] = new FileOutputStream(
                        PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS + 
                        (i + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita) +
                        SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS
                    );
                    objectOutputsParaOsArquivosTemporarios[i] = new ObjectOutputStream(fileOutputsParaOsArquivosTemporarios[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int obterONumeroDeBytesRestantesNoArquivoDeDados() {
        try {
            return fileInputParaOArquivoDeDados.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private void lerRegistrosDoArquivoDeDadosEAtualizarAtributoCorrespondente() {
        try {
            for (int i = 0; i < quantosRegistrosLerDeCadaArquivo; i++) {
                registrosLidosDoArquivoDeDados.add( (T) objectInputParaOArquivoDeDados.readObject() );
            }
        } catch (EOFException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void escreverRegistrosLidosDoArquivoDeDadosEmUmOutroArquivo(int indiceDaConexaoComOArquivo) {
        try {
            for (T item : registrosLidosDoArquivoDeDados) {
                objectOutputsParaOsArquivosTemporarios[indiceDaConexaoComOArquivo].writeObject(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fecharConexoesComArquivosTemporarios(ModosDeAberturaEFechamentoDeArquivos modo) {
        try {
            if (modo == ModosDeAberturaEFechamentoDeArquivos.LEITURA) {
                for (int i = 0; i < numeroDeCaminhos; i++) {
                    fileInputsParaOsArquivosTemporarios[i].close();
                    objectInputsParaOsArquivosTemporarios[i].close(); 
                }
            } else {
                for (int i = 0; i < numeroDeCaminhos; i++) {
                    fileOutputsParaOsArquivosTemporarios[i].close();
                    objectOutputsParaOsArquivosTemporarios[i].close(); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fecharConexaoComArquivoDeDados() {
        try {
            fileInputParaOArquivoDeDados.close();
            objectInputParaOArquivoDeDados.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void intercalarOsDadosDistribuidos() {
        int indiceOndeInserirOsRegistros = 0;

        numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura = 0;
        numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita = numeroDeCaminhos;
        abrirTodasAsConexoesComOsArquivosTemporarios();

        while (!seSomenteUmArquivoContemDados()) {
            intercalarOsRegistrosLidosDosArquivosTemporariosNoArquivo(indiceOndeInserirOsRegistros);
            numeroDeArquivosTemporariosDaMetadeSendoLidaQueAindaPossuemDados = obterAQuantidadeDeArquivosTemporariosQueAindaPossuemBytesParaSeremLidos();

            if (numeroDeArquivosTemporariosDaMetadeSendoLidaQueAindaPossuemDados == 0) {
                fecharEAbrirArquivosAsConexoesComOsArquivosTemporariosAlternandoOsModosDeAbertura();
                // a cada etapa da intercalação, o número de registros que será lido
                // no próximo arquivo corresponde ao número de registros totais que foram 
                // lidos na etapa passada, o que justifica a multiplicação do valor
                // atual pelo número de caminhos
                quantosRegistrosLerDeCadaArquivo = quantosRegistrosLerDeCadaArquivo * numeroDeCaminhos;
                numeroDeArquivosDaOutraMetadeDosArquivosTemporariosQueAindaPossuemDados = obterAQuantidadeDeArquivosTemporariosQueAindaPossuemBytesParaSeremLidos();
            }

            indiceOndeInserirOsRegistros = (indiceOndeInserirOsRegistros + 1) % numeroDeCaminhos;
        }

        fecharTodasAsConexoesComOsArquivosTemporarios();

        fazerTrativasFinaisNosArquivosTemporarios(indiceOndeInserirOsRegistros);
    }

    private void abrirTodasAsConexoesComOsArquivosTemporarios() {
        try {
            for (int i = 0; i < numeroDeCaminhos; i++) {
                fileInputsParaOsArquivosTemporarios[i] = new FileInputStream(
                    PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS +
                    (i + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura) +
                    SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS     
                );
                objectInputsParaOsArquivosTemporarios[i] = new ObjectInputStream(fileInputsParaOsArquivosTemporarios[i]);
                fileOutputsParaOsArquivosTemporarios[i] = new FileOutputStream(
                    PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS +
                    (i + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita) +
                    SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS     
                );
                objectOutputsParaOsArquivosTemporarios[i] = new ObjectOutputStream(fileOutputsParaOsArquivosTemporarios[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean seSomenteUmArquivoContemDados() {
        return (
            numeroDeArquivosTemporariosDaMetadeSendoLidaQueAindaPossuemDados == 1 && 
            numeroDeArquivosDaOutraMetadeDosArquivosTemporariosQueAindaPossuemDados == 0 || 
            numeroDeArquivosTemporariosDaMetadeSendoLidaQueAindaPossuemDados == 0 &&
            numeroDeArquivosDaOutraMetadeDosArquivosTemporariosQueAindaPossuemDados == 1
        );
    }

    private void intercalarOsRegistrosLidosDosArquivosTemporariosNoArquivo(int indiceDaConexaoComOArquivo) {
        T registroLido = null;

        indicesEOsRegistrosLidosDessesIndices.clear();
        zerarItensDoVetorQueContemONumeroDeRegistrosLidosDeCadaArquivoTemporario();

        lerUmRegistroDeCadaArquivoTemporario();

        while ( indicesEOsRegistrosLidosDessesIndices.size() > 0 ) {
            obterMenorRegistroEIndiceDeOndeEleFoiLidoEAtualizarAtributosCorrespondentes();
            escreverOMenorRegistroNoArquivo(indiceDaConexaoComOArquivo);

            if (numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs[indiceDeOndeOMenorRegistroFoiLido] < quantosRegistrosLerDeCadaArquivo) {
                registroLido = lerUmUnicoRegistroDeUmArquivo(indiceDeOndeOMenorRegistroFoiLido); 

                if (registroLido != null) {
                    indicesEOsRegistrosLidosDessesIndices.put(indiceDeOndeOMenorRegistroFoiLido, registroLido);
                    numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs[indiceDeOndeOMenorRegistroFoiLido]++;
                } else {
                    // se o registro for == null significa que um dos
                    // arquivos, se não ocorrer algum outro erro, chegou ao fim
                    // e portanto considera-se que ele já leu todos os registros que deveria.
                    // Além disso, as remoções abaixo do item do map ocorrem para que
                    // o valor não seja considerado novamente na hora de encontrar o
                    // novo menor registro
                    numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs[indiceDeOndeOMenorRegistroFoiLido] = quantosRegistrosLerDeCadaArquivo;
                    indicesEOsRegistrosLidosDessesIndices.remove(indiceDeOndeOMenorRegistroFoiLido);
                }
            } else {
                indicesEOsRegistrosLidosDessesIndices.remove(indiceDeOndeOMenorRegistroFoiLido);
            }
        }
    }

    private void zerarItensDoVetorQueContemONumeroDeRegistrosLidosDeCadaArquivoTemporario() {
        for (int i = 0; i < numeroDeCaminhos; i++) {
            numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs[i] = 0;
        }
    }

    private void lerUmRegistroDeCadaArquivoTemporario() {
        T registroLido = null;

        for (int i = 0; i < numeroDeCaminhos; i++) {
            registroLido = lerUmUnicoRegistroDeUmArquivo(i); 

            if (registroLido != null) {
                indicesEOsRegistrosLidosDessesIndices.put( i, registroLido );
            }

            numeroDeRegistrosLidosDeCadaIndiceDoObjectInputs[i]++;
        }
    }

    private void obterMenorRegistroEIndiceDeOndeEleFoiLidoEAtualizarAtributosCorrespondentes() {
        menorRegistroDoMapDeIndicesERegistros = Collections.min(indicesEOsRegistrosLidosDessesIndices.values());

        for (Map.Entry<Integer, T> entrySet : indicesEOsRegistrosLidosDessesIndices.entrySet()) {
            if (menorRegistroDoMapDeIndicesERegistros.compareTo(entrySet.getValue()) == 0) {
                indiceDeOndeOMenorRegistroFoiLido = entrySet.getKey();
                return;
            }
        }
    }

    private void escreverOMenorRegistroNoArquivo(int indiceDaConexaoComOArquivo) {
        try {
            objectOutputsParaOsArquivosTemporarios[indiceDaConexaoComOArquivo].writeObject(menorRegistroDoMapDeIndicesERegistros);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private T lerUmUnicoRegistroDeUmArquivo(int indiceDaConexaoComOArquivo) {
        T registroLido = null;

        try {
            registroLido = (T) objectInputsParaOsArquivosTemporarios[indiceDaConexaoComOArquivo].readObject();
        } catch (EOFException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return registroLido;
    }

    private int obterAQuantidadeDeArquivosTemporariosQueAindaPossuemBytesParaSeremLidos() {
        int numeroDeArquivosQueAindaPossuemBytes = 0;

        try {
            for (int i = 0; i < numeroDeCaminhos; i++) {
                numeroDeBytesDisponiveisNosArquivosTemporarios[i] = fileInputsParaOsArquivosTemporarios[i].available();
                if (numeroDeBytesDisponiveisNosArquivosTemporarios[i] > 0) {
                    numeroDeArquivosQueAindaPossuemBytes++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numeroDeArquivosQueAindaPossuemBytes;
    }

    private void fecharEAbrirArquivosAsConexoesComOsArquivosTemporariosAlternandoOsModosDeAbertura() {
        fecharTodasAsConexoesComOsArquivosTemporarios();

        numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura = numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura == 0 ? numeroDeCaminhos : 0;
        numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita = numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita == 0 ? numeroDeCaminhos : 0;

        abrirTodasAsConexoesComOsArquivosTemporarios();
    }

    private void fecharTodasAsConexoesComOsArquivosTemporarios() {
        try {
            for (int i = 0; i < numeroDeCaminhos; i++) {
                fileInputsParaOsArquivosTemporarios[i].close();
                objectInputsParaOsArquivosTemporarios[i].close(); 
                fileOutputsParaOsArquivosTemporarios[i].close();
                objectOutputsParaOsArquivosTemporarios[i].close(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fazerTrativasFinaisNosArquivosTemporarios(int ultimoIndiceUsadoParaInserirOsRegistros) {
        int numeroDoArquivoQueContemOsDadosOrdenados = obterONumeroDoArquivoQueContemOsDadosOrdenados(ultimoIndiceUsadoParaInserirOsRegistros);
        renomearArquivoQueContemOsDadosOrdenados(numeroDoArquivoQueContemOsDadosOrdenados);
        excluirArquivosTemporariosDesnecessarios();
    }

    private int obterONumeroDoArquivoQueContemOsDadosOrdenados(int ultimoIndiceUsadoParaInserirOsRegistros) {
        // o -1 se dá pelo fato de o número dos arquivos começar em 0
        // e o + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura é 
        // devido ao numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura
        // ser o valor anterior do numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoEscrita,
        // já que eles trocam os valores ao alternar os modos de abertura dos arquivos
        if (ultimoIndiceUsadoParaInserirOsRegistros == 0) {
            return ( (numeroDeCaminhos - 1) + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura );
        } else {
            return ( (ultimoIndiceUsadoParaInserirOsRegistros - 1) + numeroDoPrimeiroArquivoTemporarioASerAbertoEmModoLeitura );
        }
    }

    private void renomearArquivoQueContemOsDadosOrdenados(int numeroDoArquivoQueContemOsDadosOrdenados) {
        try {
            Files.move(Path.of(PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS + 
                numeroDoArquivoQueContemOsDadosOrdenados + SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS),
                Path.of("Dados_ordenados.db")

            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void excluirArquivosTemporariosDesnecessarios() {
        try {
            for (int i = 0; i < 2 * numeroDeCaminhos; i++) {
                Files.deleteIfExists(
                    Path.of(PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS + i +
                    SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
