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
    public static void lerEMostrarConteudoDoArquivoSerializado(String nomeDoArquivo) throws IOException, ClassNotFoundException {
		int numeroDeBytesDisponiveis = 0;
		int numeroDeRegistrosLidos = 0;

		System.out.println("Conteúdo do arquivo " + nomeDoArquivo + ": "); 

		FileInputStream fis = new FileInputStream(nomeDoArquivo);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			do {
				System.out.println(ois.readObject());
				numeroDeRegistrosLidos++;
				numeroDeBytesDisponiveis = fis.available();
			} while (numeroDeBytesDisponiveis > 0);
		} catch (EOFException e) {

		}

		fis.close();
		ois.close();

		System.out.println("Número de registros lidos: " + numeroDeRegistrosLidos);
    }

	/* 
	 * Requer que a classe que será instanciada a partir dos dados do
	 * arquivo CSV tenha um construtor que receba um vetor de String
	 * e faça o parsing dos dados para os atributos da classe
	*/
	public static <T extends Serializable> void serializarConteudoDeUmArquivoCsvEmOutroArquivo(
		String nomeDoArquivoCsv,
		String nomeDoNovoArquivo,
		Class<T> classeDeDominioNoArquivoCsv
	) throws InstantiationException, IllegalAccessException, 
			 InvocationTargetException, NoSuchMethodException, IOException {

        String[] partesDaLinhaDoArquivo;
        String linhaLidaDoArquivo = "";

		BufferedReader br = new BufferedReader(new FileReader(nomeDoArquivoCsv));
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nomeDoNovoArquivo));
		Constructor<T> construtor = classeDeDominioNoArquivoCsv.getConstructor(String[].class);

		while ( (linhaLidaDoArquivo = br.readLine() ) != null) {
			partesDaLinhaDoArquivo = linhaLidaDoArquivo.split(";");
			oos.writeObject(construtor.newInstance(new Object[] {partesDaLinhaDoArquivo} ));
		}
		
		br.close();
		oos.close();
	}
}