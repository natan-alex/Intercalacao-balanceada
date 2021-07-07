package balanced_interleaving;

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

public class FileOperations {
    public static void readAndShowTheSerializedFileContent(String filename) throws IOException, ClassNotFoundException {
		int numberOfRemainingBytes = 0;
		int numberOfRecordsRead = 0;

		System.out.println("File content of " + filename + ": "); 

		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			do {
				System.out.println(ois.readObject());
				numberOfRecordsRead++;
				numberOfRemainingBytes = fis.available();
			} while (numberOfRemainingBytes > 0);
		} catch (EOFException e) {

		}

		fis.close();
		ois.close();

		System.out.println("Number of records read: " + numberOfRecordsRead);
    }

	/* 
	* Requires that the class that will be instantiated from the data of the
	* CSV file has a constructor that receives a String vector
	* and parse the data for the class attributes 
	*/
	public static <T extends Serializable> void serializeTheContentsOfTheCsvFileIntoAnotherFile(
		String nameOfTheCsvFile,
		String nameOfTheNewFile,
		Class<T> domainClassInTheCsvFile
	) throws InstantiationException, IllegalAccessException, 
			 InvocationTargetException, NoSuchMethodException, IOException {

        String[] partsOfTheLine;
        String lineReadFromFile = "";

		BufferedReader br = new BufferedReader(new FileReader(nameOfTheCsvFile));
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nameOfTheNewFile));
		Constructor<T> construtor = domainClassInTheCsvFile.getConstructor(String[].class);

		while ( (lineReadFromFile = br.readLine() ) != null) {
			partsOfTheLine = lineReadFromFile.split(";");
			oos.writeObject(construtor.newInstance(new Object[] {partsOfTheLine} ));
		}
		
		br.close();
		oos.close();
	}
}