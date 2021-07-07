package balanced_interleaving;

import java.io.Serializable;

public class Vehicle implements Comparable<Vehicle>, Serializable {
	public static final long serialVersionUID = 2L;

	private String name;
	private int numberOfRoads;
	private String brand;

	public Vehicle(String[] data) {
		this.name = data[0];
		this.numberOfRoads = Integer.parseInt(data[1]);
		this.brand = data[2];
	}

	public Vehicle(String name, int numberOfRoads, String brand) {
		this.name = name;
		this.numberOfRoads = numberOfRoads;
		this.brand = brand;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public int getNumberOfRoads() {
		return numberOfRoads;
	}

	public void setNumberOfRoads(int numberOfRoads) {
		this.numberOfRoads = numberOfRoads;
	}

	@Override
	public int compareTo(Vehicle anotherVehicle) {
		int compareToBetween = name.compareTo(anotherVehicle.getName());
		if (compareToBetween != 0) {
			return compareToBetween;
		} 
		int compareToBetweenBrands = brand.compareTo(anotherVehicle.getBrand());
		if (compareToBetweenBrands != 0) {
			return compareToBetweenBrands;
		} 
		return Integer.compare(numberOfRoads, anotherVehicle.getNumberOfRoads());
	}

	@Override
	public String toString() {
		return "Vehicle: [name = '" + name + "', numberOfRoads = " + numberOfRoads + ", brand = '" + brand + "']";
	}
}