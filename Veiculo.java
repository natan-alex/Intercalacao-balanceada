package intercalacao_balanceada;

import java.io.Serializable;

public class Veiculo implements Comparable<Veiculo>, Serializable {
	public static final long serialVersionUID = 2L;

	private String nome;
	private int numeroDeRodas;
	private String marca;

	public Veiculo(String[] dadosDoVeiculo) {
		this.nome = dadosDoVeiculo[0];
		this.numeroDeRodas = Integer.parseInt(dadosDoVeiculo[1]);
		this.marca = dadosDoVeiculo[2];
	}

	public Veiculo(String nome, int numeroDeRodas, String marca) {
		this.nome = nome;
		this.numeroDeRodas = numeroDeRodas;
		this.marca = marca;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public int getNumeroDeRodas() {
		return numeroDeRodas;
	}

	public void setNumeroDeRodas(int numeroDeRodas) {
		this.numeroDeRodas = numeroDeRodas;
	}

	@Override
	public int compareTo(Veiculo outroVeiculo) {
		int compareToEntreNomes = nome.compareTo(outroVeiculo.getNome());
		if (compareToEntreNomes != 0) {
			return compareToEntreNomes;
		} 
		int compareToEntreMarcas = marca.compareTo(outroVeiculo.getMarca());
		if (compareToEntreMarcas != 0) {
			return compareToEntreMarcas;
		} 
		return Integer.compare(numeroDeRodas, outroVeiculo.getNumeroDeRodas());
	}

	@Override
	public String toString() {
		return "Veículo: [nome = '" + nome + "', numeroDeRodas = " + numeroDeRodas + ", marca = '" + marca + "']";
	}
}