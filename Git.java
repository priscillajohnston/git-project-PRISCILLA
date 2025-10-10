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
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

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
        if (indexFile.length() == 0) {
            toWrite += "\n";
            try {
                Files.write(Paths.get("./git/index"), toWrite.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }

            BLOB(file, hash);

        } else if (!indexContains(indexFile, listy, toWrite)) {
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
                 BLOB(file, hash);
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
    public static int checkModified(File indexFile, ArrayList<String> listy, String toCheck) {
        for (int i = 0; i < listy.size(); i++) {
            if (listy.get(i).substring(toCheck.indexOf(" ")).equals(toCheck.substring(toCheck.indexOf(" ")))) {
                return i;
            }

        }
        return -1;
    }

    public static String makeTree(String directoryPath) throws IOException {
        // makes the file and initializes important items
        // Path path = new Path(directoryPath);
        File treeFile = new File(directoryPath);
        // treeFile.mkdir();
        // System.out.println(treeFile.getAbsolutePath());
        if (!treeFile.exists() || !treeFile.isDirectory()) {
            throw new FileNotFoundException("doesnt exist :(");
        }
        File[] files = treeFile.listFiles();
        String toWrite = "";

        // loops through tree - for files adds to index/creates blob?? directories its
        // recursive
        for (File theFile : files) {
            File current = theFile;
            if (current.isDirectory()) {
                toWrite += "tree " + makeTree(current.getPath()) + " " + current.getName() + "\n";
            } else {
                toWrite += "blob " + current.hashCode() + " " + current.getName() + "\n";
            }
        }

        // gets rid of last new line
        toWrite = toWrite.substring(0, toWrite.length() - 1);

        // makes official tree file w hash name of its contents (which is hash of
        // towrite)
        String hashTitle = generateSHA1HashHelper(toWrite);
        File officialTree = new File(hashTitle);
        officialTree.createNewFile();
        // writes towrite into official tree file
        try {
            Files.write(Paths.get(officialTree.getAbsolutePath()), toWrite.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hashTitle;

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

    public static String makeTree() throws IOException {

        Path indexPath = Paths.get("git/index");

        byte[] indexBytes = Files.readAllBytes(indexPath);
        String index = new String(indexBytes);

        // arraylist with each line as an entry
        String[] arr = index.split("\n");
        ArrayList<String> listy = new ArrayList<String>(Arrays.asList(arr));
        Git.addBlob(listy);
        // sorts listy by longest pathName - working list!
        ArrayList<String> treelist = Git.makeTreeRecursive(listy, getIndexToWorkOn(listy));
        return treelist.get(0); // added this and modified line above
        // String last = treelist.get(treelist.size() - 1);
        // String[] parts = last.split(" ");
        // return parts[1];
    }

    public static ArrayList<String> makeTreeRecursive(ArrayList<String> workingList, int index) throws IOException {

        String currentPath = getPathNameHelper(workingList.get(index));
        String[] splitSlashes = currentPath.split("/");
        if (splitSlashes.length == 1) {
            String toWrite = "";
            for (int i = 0; i < workingList.size(); i++) {
                toWrite += workingList.get(i) + "\n";
            }
            toWrite = toWrite.substring(0, toWrite.length() - 1);

            File rootTree = new File("git/objects", generateSHA1HashHelper(toWrite));
            rootTree.createNewFile(); 

            try {
                Files.write(Paths.get(rootTree.getAbsolutePath()), toWrite.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<String> rootHash = new ArrayList<>();
            rootHash.add(generateSHA1HashHelper(toWrite)); //added this to ensure returning the right thing
            return rootHash;
        }
        // gets rid of fileName in path
        String shortenedPath = "";
        for (int i = 0; i < splitSlashes.length - 1; i++) {
            shortenedPath += splitSlashes[i] + "/";
        }

        int indexOfPath = workingList.get(index).length() - currentPath.length(); // check math

        // goes through working list and adds to a new arraylist the current stuff we
        // need for tree for current directory
        ArrayList<String> currentDirectoryEntries = new ArrayList<String>();

        for (int i = 0; i < workingList.size(); i++) {
            // if it is in the directory we are working with
            if (workingList.get(i).indexOf(shortenedPath) == indexOfPath) {
                currentDirectoryEntries.add(workingList.get(i).substring(0, indexOfPath)
                        + workingList.get(i).substring(indexOfPath + shortenedPath.length()));
                workingList.remove(i);
                i--;
            }
        }

        String toWrite = "";
        for (int i = 0; i < currentDirectoryEntries.size(); i++) {
            toWrite += currentDirectoryEntries.get(i) + "\n";
        }
        toWrite = toWrite.substring(0, toWrite.length() - 1);

        File dirTree = new File("git/objects", generateSHA1HashHelper(toWrite)); // had to edit to save in the objects folder
        dirTree.createNewFile();

        try {
            Files.write(Paths.get(dirTree.getAbsolutePath()), toWrite.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        workingList.add( "tree " + generateSHA1HashHelper(toWrite) + " " + shortenedPath.substring(0, shortenedPath.length() - 1));

        return makeTreeRecursive(workingList, getIndexToWorkOn(workingList));

    }

    public static String getPathNameHelper(String entryLine) {
        entryLine = entryLine.substring(entryLine.indexOf(" ") + 1);
        entryLine = entryLine.substring(entryLine.indexOf(" ") + 1);
        return entryLine;
    }

    public static ArrayList<String> addBlob(ArrayList<String> unsortedArr) {
        for (int i = 0; i < unsortedArr.size(); i++) {
            unsortedArr.set(i, "blob " + unsortedArr.get(i));
        }
        return unsortedArr;
    }

    public static int getIndexToWorkOn(ArrayList<String> unsortedArr) {
        // loops through array to get longest path
        int longestIndex = -1;

        int longestArrLength = -1;
        for (int i = 0; i < unsortedArr.size(); i++) {
            String thisPath = getPathNameHelper(unsortedArr.get(i));
            String[] thisArr = thisPath.split("/");
            if (thisArr.length > longestArrLength) {
                longestIndex = i;
                longestArrLength = thisArr.length;

            }
        }
        return longestIndex;
    }

    public static void commit(String author, String messsage) throws IOException{
        String rootHash = makeTree();
        Path headPath = Paths.get("git/HEAD");
        String parent = "";
        if(Files.exists(headPath)){
            String lastCommitString = Files.readString(headPath);
            if(lastCommitString.isEmpty()){
                parent = lastCommitString;
            }
        }
        //how to get date and time? look up when you have wifi

        String commitContents = "";
        commitContents += "tree: " + rootHash + "\n";
        commitContents += "parent: " + parent + "\n";
        commitContents += "author: " + author + "\n";
        Date currentdate = new Date();
        commitContents += "date: " + currentdate + "\n";
        commitContents += "message: " + messsage + "\n";

        String commitHash = generateSHA1HashHelper(commitContents);
        File commitFile = new File("git/objects", commitHash);
        commitFile.createNewFile();
        Files.write(commitFile.toPath(), commitContents.getBytes(StandardCharsets.UTF_8));
        Files.write(headPath, commitHash.getBytes(StandardCharsets.UTF_8));
        //how do i get the head file to point to something... do i just write into the HEAD file...?
        
    }
}