import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

   public static String hashFile(String fileName) throws IOException {
        File file = new File(fileName);
        if(!file.exists()){
            throw new FileNotFoundException("file doesnt exist!");
        }

        Path pathToFile = Paths.get("./" + fileName);
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
    

}