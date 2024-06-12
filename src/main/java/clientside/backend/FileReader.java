package clientside.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * The FileReader class is responsible for reading elements from a file.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 09.06.2024
 */
public class FileReader {

  /**
   * Read an element from a file.
   * If there is no element in the file, return an empty string.
   * File is in the format of <element>:<value>.
   *
   * @param path The path to the file
   * @param element The element to read
   * @return The element value
   * @throws IOException If an error occurs
   * @since 1.0
   */
  public static String ReadElementFromFile(String path, String element) throws IOException {
    try (Scanner scanner = new Scanner(new File(path))) {
      String line;
      String[] parts;

      while (scanner.hasNextLine() && (line = scanner.nextLine()) != null) {
        parts = line.split(":");
        if (parts.length != 2) {
          continue;
        }
        if (parts[0].equals(element)) {
          return parts[1];
        }
      }
    } catch (Exception e) {
      throw new FileNotFoundException("File not found. " + e.getMessage());
    }
    return "";
  }
}
