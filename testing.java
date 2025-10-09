import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public class testing {
    public static void main(String[] args) throws IOException {
        // Git.createGitRepository();
        // testAddToIndex();
        // testAddToIndex2();
        // testAddToIndexNested();
        // testWorkingList();

        //tree testing 
        // testTreeMakerOneNested();

        //checks indexContains
        // testIndexContains();

        // checks new addToIndex
        // testAddToIndexEmpty();
        // testAddToIndex2();
        // testAddToIndexNested();
        // testAddToIndex();

        // checks arraylist making
        // testArrayList();

        // // checks hash update
        // testHasher("blobTest", "Samples");

        // //checks cleanup and cycles
        // File gitFile = new File("git");
        // cleanup(gitFile);
        // cycles(gitFile);

        fullReset();

    }

    public static boolean checkExists() {
        File git = new File("git");
        if (!git.exists()) {
            return false;
        }
        File objects = new File("git/objects");
        if (!objects.exists()) {
            return false;
        }
        File index = new File("git/index");
        if (!index.exists()) {
            return false;
        }
        File head = new File("git/HEAD");
        if (!head.exists()) {
            return false;
        }
        return true;
    }

    public static boolean cleanup(File file) throws IOException {
        if (file.exists()) {

            File[] files = file.listFiles();
            for (File theFile : files) {
                File current = theFile;
                if (current.isDirectory()) {
                    cleanup(current);
                } else {
                    current.delete();
                }
            }
            file.delete();
            return true;
        }
        return false;
    }

    public static void cycles(File file) throws IOException {
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
    }

    public static void testHasher(String fileName, String folder) throws IOException {
        Git.hashFile(fileName, folder);
    }

    public static void fullReset() throws IOException {
        File file = new File("git");
        cleanup(file);
        Git.createGitRepository();
    }

    public static void testArrayList() throws IOException {
        File indexFile = new File("git", "index");
        ArrayList<String> listy = Git.makeArrayFromIndexHelper(indexFile);
        System.out.println(listy.toString());

    }

    // also tests blob because adding to the index calls BLOB
    public static void testAddToIndex() throws IOException {
        File file = new File("Samples", "hello.txt");
        String folderName = "Samples";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndex2() throws IOException {
        File file = new File("Samples", "test.txt");
        String folderName = "Samples";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndexEmpty() throws IOException {
        File file = new File("", "random.txt");
        String folderName = "";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndexNested() throws IOException {
        File file = new File("Samples/inner", "inside.txt");
        String folderName = "Samples/inner";
        Git.addToIndex(file, folderName);
    }


    public static void testIndexContains() throws IOException{
        File indexFile = new File("git", "index");
        indexFile.createNewFile();
        ArrayList<String> listy = Git.makeArrayFromIndexHelper(indexFile);
        String toCheck = "c65f99f8c5376adadddc46d5cbcf5762f9e55eb7 Samples/blobTest";
        System.out.println(Git.indexContains(indexFile, listy, toCheck));
    }

    public static void testAddModified() throws IOException{
        File file = new File("Samples/inner", "hi.txt");
        String folderName = "Samples/inner";
        Git.addToIndex(file, folderName);
    }

    public static void testTreeMakerOneNested() throws IOException{
        File file = new File("Samples");
        Git.makeTree("Samples");
    }

    public static void testWorkingList() throws IOException{
        Git.makeTree();
    }
}
