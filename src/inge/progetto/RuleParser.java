package inge.progetto;

import java.io.*;

public class RuleParser {

    private String fileName;

    public RuleParser(String fileName) {
        this.fileName = fileName;
    }

    public void writeRuleToFile(String text) {
        try {
            FileWriter fileWriter = new FileWriter(fileName,true);
            PrintWriter writer = new PrintWriter(fileWriter);

            writer.println(text);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readRuleFromFile() {
        StringBuilder output = new StringBuilder();
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(reader);


            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toString();
    }
}
