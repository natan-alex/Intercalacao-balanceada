package intercalacao_balanceada;

public class Main {
    public static void main(String[] args) throws Exception {
		final String nomeDoArquivoComOsDadosSerializados = "Dados_fonte.db";
		final String nomeDoArquivoCsvComOsDados = "veiculos.csv";
		final int numeroDeCaminhos = 5;

		OperacoesSobreArquivos.escreverConteudoDeUmArquivoCsvEmOutroArquivoUtilizandoObjectOutput(
			nomeDoArquivoCsvComOsDados,
			nomeDoArquivoComOsDadosSerializados,
			Veiculo.class
		);

		IntercalacaoBalanceada<Veiculo> ib = new IntercalacaoBalanceada<Veiculo>(
			nomeDoArquivoComOsDadosSerializados,
			numeroDeCaminhos
		);

		ib.distribuirOsDadosEntreOsCaminhos();

		System.out.println("\n[APOS A DISTRIBUIÇÃO EM " + numeroDeCaminhos + " CAMINHOS]");
		for (int i = 0; i < numeroDeCaminhos; i++) {
			OperacoesSobreArquivos.lerEMostrarConteudoDoArquivoUsandoObjectInputStream(
				IntercalacaoBalanceada.PREFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS +
				i + IntercalacaoBalanceada.SUFIXO_PADRAO_DO_NOME_DOS_ARQUIVOS_TEMPORARIOS 
			);
			System.out.println();
		}

        System.out.println("\n[NA INTERCALAÇÃO]");
		ib.intercalarOsDadosDistribuidos();

		System.out.println("\n[APOS A INTERCALAÇÃO]");
        OperacoesSobreArquivos.lerEMostrarConteudoDoArquivoUsandoObjectInputStream("Dados_ordenados.db");
    }
}

