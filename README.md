# Balanced interleaving for external sorting
* Algorithm to sort a large amount of data in secondary memory
* Data from person_data.csv and person_data2.csv files were generated randomly on sites such as __https://www.generatedata.com/__ and __https://www.mockaroo.com/__

### Comments:
* The code made ignores any metadata used in the data files, making it necessary that they only have records.
* It is necessary that the domain class contained in the file to be sorted, that is, the class whose data representing its objects are in the file to be sorted, has a constructor that receives a string array(String[]) and parses it to assign to the attributes of the class. This is because the FileOperations utility class uses the Constructor class to generate a new instance of the domain class, and because it doesn't know how to parse the data read from the file to pass to the Constructor, it sends these data to the domain class's constructor.
