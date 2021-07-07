package intercalacao_balanceada;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.io.FileReader;
import java.io.BufferedReader;

import java.io.Serializable;

import java.io.IOException;
import java.io.EOFException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class OperacoesSobreArquivos {
    public static void lerEMostrarConteudoDoArquivo(String nomeDoArquivo) {
		int numeroDeBytesDisponiveis = 0;
		int numeroDeRegistrosLidos = 0;

		System.out.println("Conteúdo do arquivo " + nomeDoArquivo + ": "); 

		try (FileInputStream fis = new FileInputStream(nomeDoArquivo);
			ObjectInputStream ois = new ObjectInputStream(fis) ) {

			do {
				System.out.println(ois.readObject());
				numeroDeRegistrosLidos++;
				numeroDeBytesDisponiveis = fis.available();
			} while (numeroDeBytesDisponiveis > 0);

		} catch (EOFException e) {

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Número de registros lidos: " + numeroDeRegistrosLidos);
    }

	/* 
	 * Requer que a classe que será instanciada a partir dos dados do
	 * arquivo CSV tenha um construtor que receba um vetor de String
	 * e faça o parsing dos dados para os atributos da classe
	*/
	public static void escreverConteudoDeUmArquivoCsvEmOutroArquivoUtilizandoObjectOutput(
		String nomeDoArquivoCsv,
		String nomeDoNovoArquivo,
		Class<?> classeDeDominioNoArquivoCsv
	) throws InstantiationException, IllegalAccessException, 
			 InvocationTargetException, NoSuchMethodException {

        String[] partesDaLinhaDoArquivo;
        String linhaLidaDoArquivo = "";

		try ( 
            BufferedReader br = new BufferedReader(new FileReader(nomeDoArquivoCsv));
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nomeDoNovoArquivo));
        ) {
			Constructor<?> construtor = classeDeDominioNoArquivoCsv.getConstructor(String[].class);

            while ( (linhaLidaDoArquivo = br.readLine() ) != null) {
                partesDaLinhaDoArquivo = linhaLidaDoArquivo.split(";");
                oos.writeObject(construtor.newInstance(new Object[] {partesDaLinhaDoArquivo} ));
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}