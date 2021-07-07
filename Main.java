package balanced_interleaving;

public class Main {
    public static void main(String[] args) throws Exception {
		final String filenameWithTheSerialazedData = "Source_data.db";
		final String csvFilename = "vehicles.csv";
		final int numberOfPaths = 5;

		FileOperations.serializeTheContentsOfTheCsvFileIntoAnotherFile(
			csvFilename,
			filenameWithTheSerialazedData,
			Vehicle.class
		);

		BalancedInterleaving<Vehicle> ib = new BalancedInterleaving<Vehicle>(
			filenameWithTheSerialazedData,
			numberOfPaths
		);

		ib.distributeTheDataOfTheDataFileBetweenThePaths();

		System.out.println("\n[AFTER THE DISTRIBUTION BETWEEN " + numberOfPaths + " PATHS]");
		for (int i = 0; i < numberOfPaths; i++) {
			FileOperations.readAndShowTheSerializedFileContent(
				BalancedInterleaving.DEFAULT_PREFIX_FOR_TEMP_FILENAMES +
				i + BalancedInterleaving.DEFAULT_SUFIX_FOR_TEMP_FILENAMES 
			);
			System.out.println();
		}

        System.out.println("\n[IN THE INTERLEAVING]");
		ib.mergeTheDistributedData();

		System.out.println("\n[AFTER THE MERGE]");
        FileOperations.readAndShowTheSerializedFileContent("Sorted_data.db");
    }
}

