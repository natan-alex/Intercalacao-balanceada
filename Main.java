/* 
 * AUTOR: Natan Alexandre
 * Algoritmo de ordenação externa intercalação balanceada
 */
package intercalacao_balanceada;

public class Main {
    public static void main(String[] args) {
        // EscreverDadosDeUmArquivoEmOutro gerador = new EscreverDadosDeUmArquivoEmOutro("mock_data.csv");
		LerDadosDeUmArquivo leitor;
		int caminhos = 15;
		IntercalacaoBalanceada<Integer> ib = new IntercalacaoBalanceada<Integer>("dados_fonte.db", caminhos);
		ib.distribuirDadosEntreCaminhos();

		System.out.println("\n\tAPOS A DISTRIBUIÇÃO EM " + caminhos + " CAMINHOS: ");
		for (int i = 0; i < caminhos; i++) {
			leitor = new LerDadosDeUmArquivo("arquivo"+i+".tmp");
			leitor.lerDados();
			System.out.println();
		}

        // System.out.println("\n\tNA INTERCALAÇÃO");
		ib.intercalarDadosDistribuidos();

		System.out.println("\n\tAPOS A INTERCALAÇÃO: ");
        leitor = new LerDadosDeUmArquivo("dados_ordenados.db");
        leitor.lerDados();
    }
}

