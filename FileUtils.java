import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class FileUtils {
	public static Map<String, Integer> getAllHangmanGames(String fileName) {
		Map<String, Integer> list = new LinkedHashMap<String, Integer>();
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader reader = new BufferedReader(fileReader);
			reader.readLine();
			reader.readLine(); // skip header lines
			String line = null;
			Scanner stringScanner = null;
			while ((line = reader.readLine()) != null) {
				stringScanner = new Scanner(line);
				list.put(stringScanner.next(), stringScanner.nextInt());
			}
			if (stringScanner != null) {
				stringScanner.close();
			}
			fileReader.close();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public static void initFile(PrintWriter out) {
		String format = "%s%34s\t\t%s\n";
		out.printf(format, "Word", "Guessed?", "Tries");
		for (int i = 0; i < 49; i++) {
			out.print("-");
		}
		out.print('\n');
		out.flush();
	}

	public static void printToFile(PrintWriter out, String word, String guessed, int tries) {
		int x = 30 - word.length() + (guessed.equals("Yes") ? 3 : 2);
		String format = "%s%" + x + "s\t\t\t%s\n";
		out.printf(format, word, guessed, tries);
		out.flush();
	}
}
