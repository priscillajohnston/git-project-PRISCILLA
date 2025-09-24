import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

   
    

}