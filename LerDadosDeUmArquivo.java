/* 
 * AUTOR: Natan Alexandre
 * Algoritmo de ordenação externa intercalação balanceada
 */
package intercalacao_balanceada;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileInputStream;

public class LerDadosDeUmArquivo {
	private FileInputStream fis;
	private ObjectInputStream ois;
	private String deOndeLer;

	public LerDadosDeUmArquivo(String nomeDoArquivo) {
		deOndeLer = nomeDoArquivo;
	}

    public void lerDados() {
		System.out.println("Conteúdo do arquivo " + deOndeLer + ": "); 
		int bytesDisponiveis = 0;
		int qtdRegistrosLidos = 0;
		try {
			fis = new FileInputStream(deOndeLer);
			ois = new ObjectInputStream(fis);
		} catch (IOException e) {
            System.out.println("IOException lançada no procedimento lerDados da classe LerDadosDeUmArquivo! Erro: " + e.getMessage());
            e.printStackTrace();
		}
		do {
			try {
				System.out.println(ois.readObject());
				qtdRegistrosLidos++;
				bytesDisponiveis = fis.available();
			} catch (EOFException e) {
			} catch (IOException e) {
                System.out.println("IOException lançada no procedimento lerDados da classe LerDadosDeUmArquivo! Erro: " + e.getMessage());
                e.printStackTrace();
			} catch (ClassNotFoundException e) {
                System.out.println("IOException lançada no procedimento lerDados da classe LerDadosDeUmArquivo! Erro: " + e.getMessage());
                e.printStackTrace();
			}
		} while (bytesDisponiveis > 0);
		System.out.println("Número de registros lidos: " + qtdRegistrosLidos);
    }
}
