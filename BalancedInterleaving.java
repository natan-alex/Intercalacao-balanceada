package balanced_interleaving;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;
import java.io.EOFException;

import java.io.Serializable;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class BalancedInterleaving<T extends Comparable<T> & Serializable> {
    public static final String DEFAULT_PREFIX_FOR_TEMP_FILENAMES = "file";
    public static final String DEFAULT_SUFIX_FOR_TEMP_FILENAMES = ".temp";

    private int numberOfPaths; 
    private int numberOfRecordsToReadInEachFile;

    private ObjectInputStream objectInputForTheDataFile;
    private FileInputStream fileInputForTheDataFile;

    private FileOutputStream[] fileOutputsForTheTempFiles;
    private ObjectOutputStream[] objectOutputsForTheTempFiles;

    private FileInputStream[] fileInputsForTheTempFiles;
    private ObjectInputStream[] objectInputsForTheTempFiles;

    private int[] numberOfRemainingBytesInEachTempFile; 

    private int numberOfTheFirstTempFileOpenedInReadMode;
    private int numberOfTheFirstTempFileOpenedInWriteMode;

    private List<T> recordsReadFromDataFile;

    private Map<Integer, T> indexesAndTheRecordsReadFromTheseIndexes;
    private int[] numberOfRecordsReadFromEachIndexOfObjectInputs;
    private T lowestRecordFromIndexesAndRecordsMap;
    private int indexFromWhereTheLowestRecordWasRead;

    private int numberOfTempFilesOfHalfBeingReadThatStillHaveData;
    private int numberOfTempFilesOfTheOtherHalfThatStillHaveData;

    enum WhichKindOfConnectionsToOpenOrClose {
        INPUTS,
        OUTPUTS
    }

    public BalancedInterleaving(String nameOfTheFileToBeSorted, int numberOfPaths) throws IOException {
        this.numberOfPaths = numberOfPaths;
        numberOfRecordsToReadInEachFile = 20; // fictitious number
        numberOfRemainingBytesInEachTempFile = new int[numberOfPaths];
        numberOfRecordsReadFromEachIndexOfObjectInputs = new int[numberOfPaths];
        indexesAndTheRecordsReadFromTheseIndexes = new HashMap<Integer, T>(numberOfPaths);

        fileInputForTheDataFile = new FileInputStream(nameOfTheFileToBeSorted);
        objectInputForTheDataFile = new ObjectInputStream(fileInputForTheDataFile);

        fileInputsForTheTempFiles = new FileInputStream[numberOfPaths];
        objectInputsForTheTempFiles = new ObjectInputStream[numberOfPaths];

        fileOutputsForTheTempFiles = new FileOutputStream[numberOfPaths];
        objectOutputsForTheTempFiles = new ObjectOutputStream[numberOfPaths];
    }


    public void distributeTheDataOfTheDataFileBetweenThePaths() {
        recordsReadFromDataFile = new ArrayList<>(numberOfRecordsToReadInEachFile);

        int numberOfRemainingBytesInTheDataFile = 0;
        int connectionIndexWhereToInsertTheRecords = 0;

        numberOfTheFirstTempFileOpenedInWriteMode = 0;
        openConnectionWithTheTempFiles(WhichKindOfConnectionsToOpenOrClose.OUTPUTS);

        numberOfRemainingBytesInTheDataFile = getNumberOfRemainingBytesOnDataFile();

        while (numberOfRemainingBytesInTheDataFile > 0) {
            recordsReadFromDataFile.clear();
            readRecordsFromDataFileAndUpdateTheCorrespondingAttributes();
            Collections.sort(recordsReadFromDataFile);
            writeRecordsReadFromTheDataFileInTheTempFile(connectionIndexWhereToInsertTheRecords);
            connectionIndexWhereToInsertTheRecords = (connectionIndexWhereToInsertTheRecords + 1) % numberOfPaths;
            numberOfRemainingBytesInTheDataFile = getNumberOfRemainingBytesOnDataFile();
        }

        closeConnectionsWithTheTempFiles(WhichKindOfConnectionsToOpenOrClose.OUTPUTS);
        closeTheConnectionWithTheDataFile();
    }


    private void openConnectionWithTheTempFiles(WhichKindOfConnectionsToOpenOrClose kind) {
        try {
            if (kind == WhichKindOfConnectionsToOpenOrClose.INPUTS) {
                for (int i = 0; i < numberOfPaths; i++) {
                    fileInputsForTheTempFiles[i] = new FileInputStream(
                        DEFAULT_PREFIX_FOR_TEMP_FILENAMES + 
                        (i + numberOfTheFirstTempFileOpenedInReadMode) +
                        DEFAULT_SUFIX_FOR_TEMP_FILENAMES
                    );
                    objectInputsForTheTempFiles[i] = new ObjectInputStream(fileInputsForTheTempFiles[i]);
                }
            } else {
                for (int i = 0; i < numberOfPaths; i++) {
                    fileOutputsForTheTempFiles[i] = new FileOutputStream(
                        DEFAULT_PREFIX_FOR_TEMP_FILENAMES + 
                        (i + numberOfTheFirstTempFileOpenedInWriteMode) +
                        DEFAULT_SUFIX_FOR_TEMP_FILENAMES
                    );
                    objectOutputsForTheTempFiles[i] = new ObjectOutputStream(fileOutputsForTheTempFiles[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getNumberOfRemainingBytesOnDataFile() {
        try {
            return fileInputForTheDataFile.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private void readRecordsFromDataFileAndUpdateTheCorrespondingAttributes() {
        try {
            for (int i = 0; i < numberOfRecordsToReadInEachFile; i++) {
                recordsReadFromDataFile.add( (T) objectInputForTheDataFile.readObject() );
            }
        } catch (EOFException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeRecordsReadFromTheDataFileInTheTempFile(int fileConnectionIndex) {
        try {
            for (T item : recordsReadFromDataFile) {
                objectOutputsForTheTempFiles[fileConnectionIndex].writeObject(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnectionsWithTheTempFiles(WhichKindOfConnectionsToOpenOrClose kind) {
        try {
            if (kind == WhichKindOfConnectionsToOpenOrClose.INPUTS) {
                for (int i = 0; i < numberOfPaths; i++) {
                    fileInputsForTheTempFiles[i].close();
                    objectInputsForTheTempFiles[i].close(); 
                }
            } else {
                for (int i = 0; i < numberOfPaths; i++) {
                    fileOutputsForTheTempFiles[i].close();
                    objectOutputsForTheTempFiles[i].close(); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeTheConnectionWithTheDataFile() {
        try {
            fileInputForTheDataFile.close();
            objectInputForTheDataFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void mergeTheDistributedData() {
        int indexWhereToInsertTheRecords = 0;

        numberOfTheFirstTempFileOpenedInReadMode = 0;
        numberOfTheFirstTempFileOpenedInWriteMode = numberOfPaths;
        openAllTheConnectionsWithTheTempFiles();

        while (!isOnlyOneTempFileThatContainsTheData()) {
            mergeTheRecordsReadFromTheTempFilesInTheFile(indexWhereToInsertTheRecords);
            numberOfTempFilesOfHalfBeingReadThatStillHaveData = getNumberOfTempFilesThatStillHaveBytesToRead();

            if (numberOfTempFilesOfHalfBeingReadThatStillHaveData == 0) {
                openAndCloseTheConnectionsWithTheTempFilesSwitchingTheirOpeningModes();
                // at each step of the merge, the number of records that will be read
                // in the next file corresponds to the number of total records that were
                // read in the last step, which justifies the multiplication of the
                // current value by the number of paths
                numberOfRecordsToReadInEachFile = numberOfRecordsToReadInEachFile * numberOfPaths;
                numberOfTempFilesOfTheOtherHalfThatStillHaveData = getNumberOfTempFilesThatStillHaveBytesToRead();
            }

            indexWhereToInsertTheRecords = (indexWhereToInsertTheRecords + 1) % numberOfPaths;
        }

        closeAllTheConnectionsWithTheTempFiles();

        makeTheFinalDealsInTheTempFiles(indexWhereToInsertTheRecords);
    }

    private void openAllTheConnectionsWithTheTempFiles() {
        try {
            for (int i = 0; i < numberOfPaths; i++) {
                fileInputsForTheTempFiles[i] = new FileInputStream(
                    DEFAULT_PREFIX_FOR_TEMP_FILENAMES +
                    (i + numberOfTheFirstTempFileOpenedInReadMode) +
                    DEFAULT_SUFIX_FOR_TEMP_FILENAMES     
                );
                objectInputsForTheTempFiles[i] = new ObjectInputStream(fileInputsForTheTempFiles[i]);
                fileOutputsForTheTempFiles[i] = new FileOutputStream(
                    DEFAULT_PREFIX_FOR_TEMP_FILENAMES +
                    (i + numberOfTheFirstTempFileOpenedInWriteMode) +
                    DEFAULT_SUFIX_FOR_TEMP_FILENAMES     
                );
                objectOutputsForTheTempFiles[i] = new ObjectOutputStream(fileOutputsForTheTempFiles[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnlyOneTempFileThatContainsTheData() {
        return (
            numberOfTempFilesOfHalfBeingReadThatStillHaveData == 1 && 
            numberOfTempFilesOfTheOtherHalfThatStillHaveData == 0 || 
            numberOfTempFilesOfHalfBeingReadThatStillHaveData == 0 &&
            numberOfTempFilesOfTheOtherHalfThatStillHaveData == 1
        );
    }

    private void mergeTheRecordsReadFromTheTempFilesInTheFile(int fileConnectionIndex) {
        T recordRead = null;

        indexesAndTheRecordsReadFromTheseIndexes.clear();
        resetTheItensOfTheVectorThatContainsTheNumberOfRecordsReadInEachTempFile();

        readARecordFromEachTempFileAndUpdateCorrespondingAttribute();

        while ( indexesAndTheRecordsReadFromTheseIndexes.size() > 0 ) {
            getLowestRecordAndTheIndexFromWhereItWasReadAndUpdateCorrespondingAttributes();
            writeTheLowestRecordInTheFile(fileConnectionIndex);

            if (numberOfRecordsReadFromEachIndexOfObjectInputs[indexFromWhereTheLowestRecordWasRead] < numberOfRecordsToReadInEachFile) {
                recordRead = readOneRecordFromTheTempFile(indexFromWhereTheLowestRecordWasRead); 

                if (recordRead != null) {
                    indexesAndTheRecordsReadFromTheseIndexes.put(indexFromWhereTheLowestRecordWasRead, recordRead);
                    numberOfRecordsReadFromEachIndexOfObjectInputs[indexFromWhereTheLowestRecordWasRead]++;
                } else {
                    // if the record is null means that one of the
                    // files, if no other error occurs, is over
                    // and therefore it is considered that it has already 
                    // read all the records it should.
                    // Also, the removals of the map item below occur so that
                    // the value is not considered again when finding the
                    // new lowest record
                    numberOfRecordsReadFromEachIndexOfObjectInputs[indexFromWhereTheLowestRecordWasRead] = numberOfRecordsToReadInEachFile;
                    indexesAndTheRecordsReadFromTheseIndexes.remove(indexFromWhereTheLowestRecordWasRead);
                }
            } else {
                indexesAndTheRecordsReadFromTheseIndexes.remove(indexFromWhereTheLowestRecordWasRead);
            }
        }
    }

    private void resetTheItensOfTheVectorThatContainsTheNumberOfRecordsReadInEachTempFile() {
        for (int i = 0; i < numberOfPaths; i++) {
            numberOfRecordsReadFromEachIndexOfObjectInputs[i] = 0;
        }
    }

    private void readARecordFromEachTempFileAndUpdateCorrespondingAttribute() {
        T recordRead = null;

        for (int i = 0; i < numberOfPaths; i++) {
            recordRead = readOneRecordFromTheTempFile(i); 

            if (recordRead != null) {
                indexesAndTheRecordsReadFromTheseIndexes.put( i, recordRead );
            }

            numberOfRecordsReadFromEachIndexOfObjectInputs[i]++;
        }
    }

    private void getLowestRecordAndTheIndexFromWhereItWasReadAndUpdateCorrespondingAttributes() {
        lowestRecordFromIndexesAndRecordsMap = Collections.min(indexesAndTheRecordsReadFromTheseIndexes.values());

        for (Map.Entry<Integer, T> entrySet : indexesAndTheRecordsReadFromTheseIndexes.entrySet()) {
            if (lowestRecordFromIndexesAndRecordsMap.compareTo(entrySet.getValue()) == 0) {
                indexFromWhereTheLowestRecordWasRead = entrySet.getKey();
                return;
            }
        }
    }

    private void writeTheLowestRecordInTheFile(int fileConnectionIndex) {
        try {
            objectOutputsForTheTempFiles[fileConnectionIndex].writeObject(lowestRecordFromIndexesAndRecordsMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private T readOneRecordFromTheTempFile(int fileConnectionIndex) {
        T recordRead = null;

        try {
            recordRead = (T) objectInputsForTheTempFiles[fileConnectionIndex].readObject();
        } catch (EOFException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return recordRead;
    }

    private int getNumberOfTempFilesThatStillHaveBytesToRead() {
        int numberOfFilesThatStillHaveBytes = 0;

        try {
            for (int i = 0; i < numberOfPaths; i++) {
                numberOfRemainingBytesInEachTempFile[i] = fileInputsForTheTempFiles[i].available();
                if (numberOfRemainingBytesInEachTempFile[i] > 0) {
                    numberOfFilesThatStillHaveBytes++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numberOfFilesThatStillHaveBytes;
    }

    private void openAndCloseTheConnectionsWithTheTempFilesSwitchingTheirOpeningModes() {
        closeAllTheConnectionsWithTheTempFiles();

        numberOfTheFirstTempFileOpenedInReadMode = numberOfTheFirstTempFileOpenedInReadMode == 0 ? numberOfPaths : 0;
        numberOfTheFirstTempFileOpenedInWriteMode = numberOfTheFirstTempFileOpenedInWriteMode == 0 ? numberOfPaths : 0;

        openAllTheConnectionsWithTheTempFiles();
    }

    private void closeAllTheConnectionsWithTheTempFiles() {
        try {
            for (int i = 0; i < numberOfPaths; i++) {
                fileInputsForTheTempFiles[i].close();
                objectInputsForTheTempFiles[i].close(); 
                fileOutputsForTheTempFiles[i].close();
                objectOutputsForTheTempFiles[i].close(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeTheFinalDealsInTheTempFiles(int lastIndexUsedToInsertTheRecords) {
        int numberOfTheFileThatContainsTheSortedData = getTheNumberOfFileThatContainsTheSortedData(lastIndexUsedToInsertTheRecords);
        renameTheFileThatContainsTheSortedData(numberOfTheFileThatContainsTheSortedData);
        deleteTempFilesNoLongerNeeded();
    }

    private int getTheNumberOfFileThatContainsTheSortedData(int lastIndexUsedToInsertTheRecords) {
        // the -1 is because the number of the files start in 0
        // and the + numberOfTheFirstTempFileOpenedInReadMode is because
        // the numberOfTheFirstTempFileOpenedInReadMode is the last value
        // of the numberOfTheFirstTempFileOpenedInWriteMode since they 
        // exchanged their values 
        if (lastIndexUsedToInsertTheRecords == 0) {
            return ( (numberOfPaths - 1) + numberOfTheFirstTempFileOpenedInReadMode );
        } else {
            return ( (lastIndexUsedToInsertTheRecords - 1) + numberOfTheFirstTempFileOpenedInReadMode );
        }
    }

    private void renameTheFileThatContainsTheSortedData(int numberOfTheFileThatContainsTheSortedData) {
        try {
            Files.move(Path.of(DEFAULT_PREFIX_FOR_TEMP_FILENAMES + 
                numberOfTheFileThatContainsTheSortedData + DEFAULT_SUFIX_FOR_TEMP_FILENAMES),
                Path.of("Sorted_data.db")

            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTempFilesNoLongerNeeded() {
        try {
            for (int i = 0; i < 2 * numberOfPaths; i++) {
                Files.deleteIfExists(
                    Path.of(DEFAULT_PREFIX_FOR_TEMP_FILENAMES + i +
                    DEFAULT_SUFIX_FOR_TEMP_FILENAMES)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
