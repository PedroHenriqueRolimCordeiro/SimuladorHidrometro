package br.com.simulador.hidrometro;

import java.io.Console;
import java.util.*;
import java.util.regex.*;

import br.com.simulador.hidrometro.client.CLIClient;

public class Main {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("\nAté mais!")));

        Console console = System.console();
        Scanner scanner = (console == null) ? new Scanner(System.in) : null;

        while (true) {
            String line;
            if (console != null) {
                line = console.readLine("> ");
            } else {
                System.out.print("> ");
                System.out.flush();
                if (!scanner.hasNextLine()) break; // EOF
                line = scanner.nextLine();
            }

            if (line == null) break; // EOF
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) break;

            String[] cmdArgs = splitRespectingQuotes(line);
            try {
                CLIClient.run(cmdArgs);
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
            }
        }
    }

    // Divide a linha em argumentos, respeitando "aspas" e 'aspas simples'
    private static String[] splitRespectingQuotes(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)").matcher(input);
        while (m.find()) {
            String tok = m.group(1);
            if (tok == null) tok = m.group(2);
            if (tok == null) tok = m.group(3);
            tokens.add(tok);
        }
        return tokens.toArray(new String[0]);
    }
}
