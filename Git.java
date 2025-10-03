import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Git {
    public static void main(String[] args) throws IOException {
        createGitRepository();
    }

    public static boolean createGitRepository() throws IOException {
        File git = new File("git");
        if (git.exists()) {
            System.out.println("Git Repository Already Exists");
            return false;
        }
        // makes git directory
        git.mkdir();

        // makes objs directory inside git
        File objDir = new File("git", "objects");
        objDir.mkdir();

        // create file index inside git
        File indexFile = new File("git", "index");
        indexFile.createNewFile();

        // create file HEAD inside git
        File HEAD = new File("git", "HEAD");
        HEAD.createNewFile();

        System.out.println("Git Repository Created");
        return true;

    }

    public static String hashFile(String fileName, String folderName) throws IOException {
        File file;
        if (folderName.equals("")) {
            file = new File(fileName);
        } else {
            file = new File(folderName, fileName);
        }

        if (!file.exists()) {
            throw new FileNotFoundException("file doesnt exist!");
        }

        Path pathToFile = Paths.get(file.getAbsolutePath());
        byte[] fileBytes = Files.readAllBytes(pathToFile);

        String fileString = new String(fileBytes);
        System.out.println(fileString);

        try {
            // Get an instance of the SHA-1 message digest algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // Compute the hash of the input string
            byte[] hash = md.digest(fileString.getBytes());

            // Convert the byte array hash to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            System.out.println(hexString.toString());

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Handle the case where SHA-1 algorithm is not available
            e.printStackTrace();
            return null;
        }

    }

    // creates a BLOB and writes it to index
    public static void BLOB(File file, String hash) throws IOException {
        // creates blob and gets path to file
        File blobFile = new File("git/objects", hash);
        blobFile.createNewFile();
        String contents = "";
        Path filePath = Paths.get("./" + file);

        // reads all of the file contents/bytes into a string contents
        byte[] fileBytes = Files.readAllBytes(filePath);
        for (int i = 0; i < fileBytes.length; i++) {
            contents += (char) fileBytes[i];
        }

        // writes contents into blob
        try {
            Files.write(Paths.get("./" + blobFile), contents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToIndex(File file, String folderName) throws IOException {
        String hash = hashFile(file.getName(), folderName);
        String pathString = file.getAbsolutePath();
        int indexOfFolderName = pathString.indexOf(folderName);
        String toWrite = "";

        if (indexOfFolderName == -1) {
            toWrite = hash + " " + file.getAbsolutePath().substring(pathString.indexOf(file.getName()));
        } else {
            toWrite = hash + " " + file.getAbsolutePath().substring(indexOfFolderName);
        }

        // check before writing into index file
        File indexFile = new File("git", "index");
        indexFile.createNewFile();
        ArrayList<String> listy = makeArrayFromIndexHelper(indexFile);

        if (!indexContains(indexFile, listy, toWrite)) {
            if (checkModified(indexFile, listy, toWrite) >= 0) {
                int indexOfModified = checkModified(indexFile, listy, toWrite);
                listy.remove(indexOfModified);
                listy.add(toWrite);
                String editedWrite = "";
                for (int i = 0; i < listy.size(); i++) {
                    editedWrite += listy.get(indexOfModified) + "\n";
                }
                editedWrite = editedWrite.substring(0, editedWrite.length() - 1);
                try {
                    Files.write(Paths.get("./git/index"), editedWrite.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else {
                toWrite += "\n";
                try {
                    Files.write(Paths.get("./git/index"), toWrite.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BLOB(file, hash);
            }
        }

    }

    public static ArrayList<String> makeArrayFromIndexHelper(File indexFile) throws IOException {
        String str = "";
        Path indexFilePath = Paths.get(indexFile.getAbsolutePath());
        byte[] fileBytes = Files.readAllBytes(indexFilePath);
        for (int i = 0; i < fileBytes.length; i++) {
            str += (char) fileBytes[i];
        }

        // makes arrayList
        String[] array = str.split("\n");
        ArrayList<String> listy = new ArrayList<>(Arrays.asList(array));
        return listy;

    }

    public static boolean indexContains(File indexFile, ArrayList<String> listy, String toCheck) {
        for (int i = 0; i < listy.size(); i++) {
            if (listy.get(i).equals(toCheck))
                return true;
        }
        return false;
    }

    // also calls removeModified!!
    public static int checkModified(File indexFile, ArrayList<String> listy,
            String toCheck) {
        for (int i = 0; i < listy.size(); i++) {
            if (listy.get(i).substring(toCheck.indexOf(" ")).equals(toCheck.substring(toCheck.indexOf(" ")))) {
                return i;
            }

        }
        return -1;
    }

    public static void makeTree(String directoryPath) throws IOException {
        // makes the file and initializes important items
        //Path path = new Path(directoryPath);
        File treeFile = new File(directoryPath);
        treeFile.mkdir();
        if(!treeFile.isDirectory()){
            throw new FileNotFoundException("doesnt exist :(");
        }
        File[] files = treeFile.listFiles();
        String toWrite = "";

        // loops through tree - for files adds to index/creates blob?? directories its
        // recursive
        for (File theFile : files) {
            File current = theFile;
            if (current.isDirectory()) {
                makeTree(current.getName());
            } else {
                toWrite += "\n" + "blob" + current.hashCode() + current.getName();
            }
        }
        // gets rid of the new line for the first entry since not needed
        toWrite = toWrite.substring(1);

        //makes official tree file w hash name of its contents (which is hash of towrite)
        File officialTree = new File(generateSHA1HashHelper(toWrite));
        officialTree.createNewFile();
        // writes towrite into official tree file
        try {
            Files.write(Paths.get(officialTree.getAbsolutePath()), toWrite.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        
    }

    public static String generateSHA1HashHelper(String input) {
        try {
            // Get an instance of the SHA-1 MessageDigest
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // Convert the input string to bytes and digest them
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert the byte array into a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                // Convert each byte to its hexadecimal representation
                String hex = Integer.toHexString(0xff & b);
                // Prepend a '0' if the hex value is a single digit
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Handle the case where SHA-1 algorithm is not available
            throw new RuntimeException("SHA-1 algorithm not found.", e);
        }
    }
}