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
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readRuleFromFile() {
        StringBuilder output = new StringBuilder();
        boolean presente = new File(this.fileName).exists();

        if (!presente) {
            return "";
        }
        try {
            FileReader reader = new FileReader(fileName);

            BufferedReader bufferedReader = new BufferedReader(reader);


            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            bufferedReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toString();
    }
}
