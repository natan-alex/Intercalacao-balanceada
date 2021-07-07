package balanced_interleaving;

import java.io.Serializable;

public class Person implements Comparable<Person>, Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private int age;

	public Person(String[] data) {
		this.name = data[0];
		this.age = Integer.parseInt( data[1] );
	} 

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public int compareTo(Person p) {
		int i = this.name.compareTo(p.getName());
        if (i == 0) {
			return Integer.compare(this.age, p.getAge());
        }
        return i;
	}

	@Override
	public String toString() {
		return "Person: [name = '" + name + "', age = " + age + "]";
	}
}
