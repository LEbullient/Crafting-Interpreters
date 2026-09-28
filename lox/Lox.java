package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      // we will run $jlox script.lox to compile the script.lox file, script.lox is args[0] here
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  // read the file from the given path and execute it
  private static void runFile(String path) throws IOException {
    String source = Files.readString(Paths.get(path));
    run(source);
    if (hadError) System.exit(65);
  }

  // interactive mode
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { 
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
      hadError = false;
    }
  }

  // the method that executes the program
  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    for (Token token : tokens) {
      System.out.println(token);
    }

    Parser parser = new Parser(tokens);
    Expr expr = parser.parse();
    System.out.println(new AstPrinter().print(expr));
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  // it's a good practice to seperate the code that detects errors from the code that reports them
  // bc various phases of the front end will detect errors, but it's not their job to know how to present them to users
  // you can have multiple ways to display the errors, like writing into stderr, color-flagging, etc.
  // write those in a report method makes things simpler
  private static void report(int line, String where, String message) {
    System.out.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }



}