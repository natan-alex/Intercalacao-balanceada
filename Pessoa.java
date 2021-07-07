package intercalacao_balanceada;

import java.io.Serializable;

public class Pessoa implements Comparable<Pessoa>, Serializable {
	private static final long serialVersionUID = 1L;

	private String nome;
	private int idade;

	public Pessoa(String[] dadosDaPessoa) {
		this.nome = dadosDaPessoa[0];
		this.idade = Integer.parseInt( dadosDaPessoa[1] );
	} 

	public Pessoa(String nome, int idade) {
		this.nome = nome;
		this.idade = idade;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getIdade() {
		return idade;
	}

	public void setIdade(int idade) {
		this.idade = idade;
	}

	@Override
	public int compareTo(Pessoa p) {
		int i = this.nome.compareTo(p.getNome());
        if (i == 0) {
			return Integer.compare(this.idade, p.getIdade());
        }
        return i;
	}

	@Override
	public String toString() {
		return "Pessoa: [nome = '" + nome + "', idade = " + idade + "]";
	}
}
