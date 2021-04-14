/* 
 * AUTOR: Natan Alexandre
 * Algoritmo de ordenação externa intercalação balanceada
 */
package intercalacao_balanceada;

import java.io.Serializable;

public class Pessoa implements Comparable<Pessoa>, Serializable {
	private static final long serialVersionUID = 1L;
	private String nome;
	private int idade;

	public Pessoa(String nome, int idade) {
		this.nome = nome;
		this.idade = idade;
	}

	@Override
	public int compareTo(Pessoa p) {
		int i = this.nome.compareTo(p.nome);
        if (i == 0) {
            if (idade == p.idade) {
                return 0;
            } else if (idade < p.idade) {
                return -1;
            } else {
                return 1;
            }
        }
        return i;
	}

	@Override
	public String toString() {
		return "Pessoa: [nome = '" + nome + "', idade = " + idade + "]";
	}
}
