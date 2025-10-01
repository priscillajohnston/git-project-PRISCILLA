import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class testing {
    public static void main(String[] args) throws IOException {

        //checks blobs to index
        File file = new File("Samples", "blobTest");
        file.createNewFile();
        testBLOB(file, "./Samples/", "Samples");
        File file2 = new File("Samples", "hello.txt");
        file2.createNewFile();
        testBLOB(file2, "./Samples/", "Samples");

        // //checks cleanup and cycles
        // File gitFile = new File("git");
        // cleanup(gitFile);
        // cycles(gitFile);
        
        // //resets
        // fullReset();
        
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

    public static void testHasher(String fileName, String pathName, String folder) throws IOException {
        Git.hashFile(fileName, pathName, folder);
    }

    public static void testBLOB(File file, String pathName, String folderName) throws IOException {
        Git.BLOB(file, pathName, folderName);
        File check = new File("git/objects", Git.hashFile(file.getName(), pathName, folderName));
        if (check.exists()) {
            System.out.println("BLOB EXISTS!");
        }
    }

    // public static void testWriteFileInIndex(File file) throws IOException{
    //     Git.writeToIndexFile(file);
    // }

    public static void fullReset() throws IOException{
        File file = new File("git");
        cleanup(file);
        Git.createGitRepository();
    }
}
