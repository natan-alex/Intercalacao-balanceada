/* 
 * AUTOR: Natan Alexandre
 * Algoritmo de ordenação externa intercalação balanceada
 */
package intercalacao_balanceada;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.io.FileReader;
import java.io.BufferedReader;

import java.io.Serializable;
import java.io.IOException;

public class GeradorDeDados {
    // ler arquivo com registros de Pessoa e adicionar em dados_fonte.db
	public GeradorDeDados(String nomeDoArquivoCsv) {
        String[] partes = new String[2];
        String linha = "";
		try ( 
			FileReader fr = new FileReader(nomeDoArquivoCsv);
            BufferedReader br = new BufferedReader(fr);
            FileOutputStream fos = new FileOutputStream("dados_fonte.db");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {

            while ( (linha = br.readLine() ) != null) {
                partes = linha.split(";");
                oos.writeObject( new Pessoa( partes[0], Integer.parseInt(partes[1]) ) );
            }
		} catch (IOException e) {
            System.out.println("IOException lançada no construtor da classe GeradorDeDados! Erro: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
