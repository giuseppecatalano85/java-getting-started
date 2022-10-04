import org.junit.jupiter.api.Test;

import java.io.*;

public class FileTest {


    public void testFile() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while(line != null){
            System.out.println(line);
            line = br.readLine();
        }

        is.close();
        PrintWriter pw = new PrintWriter(new File(getClass().getClassLoader().getResource("data.txt").getFile()));
        pw.write("pluto");
        pw.close();
    }
}
