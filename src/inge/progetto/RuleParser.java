package inge.progetto;

import java.io.*;
import java.util.ArrayList;

/**
 * Classe di utility usata per le varie operazioni di gestione delle regole, che esse vengano importate o generate interattivamente da un utente.
 * Si occupa di salvataggio permanente delle regole o di ripristino/lettura da file per l'utente che interagisce con i vari componenti del sistema come unita immobiliari o dispositivi
 * come sensori o attuatori . Inoltre è possibile anche importare infatti regole da file esterni purchè queste siano compatibili con la descrizione
 * precedentemente fornita del sistema e che rispettino la grammatica e sintassi del sistema.
 * Svolge appunto la funzione di parsing, ovvero traduce il formato testuale delle regole assegnate/inserite dall'utente in comandi o azioni con le
 * quali modifica la configurazione di una determinata unita immobiliare agendo su singoli sensori e attuatori in essa presenti.
 *
 * @author Parampal Singh, Mattia Nodari
 */
public class RuleParser {

    /** nome file contenente le regole di un'unità immobiliare*/
    private String fileName;

    /**
     * Costruttore di un'istanza RuleParser
     */
    public RuleParser() {
        this.fileName = "";
    }

    /**Specifica il percorso del file di testo con cui lavorare
     * @param fileName percorso del file
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**Permette la scrittura di regola/e nel file dal percorso specficato in modo da salvare permanentemente
     * @param text regola/e che si desiderano salvare
     */
    public void writeRuleToFile(String text) {
        if (fileName.isEmpty())
            return;

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

    /**
     * Viene effettuata la lettura da file per recuperare la lista delle regole create dal fruitore.
     * @return la lettura delle regole dal file dell'unità immobiliare corrente
     */
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

    /**
     * Il metodo applica le regole presenti nella sezione conseguente di una regola quando l'antecedente risulta true.
     * @param listaSensori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     * @param listaAttuatori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     */
    public void applyRules(ArrayList<Sensore> listaSensori, ArrayList<Attuatore> listaAttuatori) {
        String readRules = this.readRuleFromFile();

        System.out.println("\n\n...APPLICAZIONE REGOLE...\n");

        if (readRules.equals("")) {
            System.out.println("XX Non sono state inserite regole per l'unita immobiliare XX\n");
            return;
        }

        String[] rules = readRules.split("\n");

        for (String r : rules) {

            String r2 = r.replace("IF ", "");
            String[] tokens = r2.split(" THEN ");

            boolean ris = calculate(tokens[0], listaSensori);
            System.out.println(r + " :: " + ris);
            if (ris)
                applyActions(tokens[1], listaAttuatori);
        }
    }

    /**
     * Il metodo isola una singola azione e la passa al metodo che applica le singole regole.
     * @param token sezione di azione da eseguire
     * @param listaAttuatori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     */
    private void applyActions(String token, ArrayList<Attuatore> listaAttuatori) {
        for (String tok : token.split(" ; "))
            apply(tok, listaAttuatori);
    }

    /**
     * Il metodo applica la regola vera e propria, settando il nuovo valoro della modalità operativa dell'attuatore.
     * @param act azione da effettuare.
     * @param listaAttuatori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     */
    private static void apply(String act, ArrayList<Attuatore> listaAttuatori) {
        String[] toks = act.split(" := ");

        Attuatore actD = null;
        for (Attuatore att : listaAttuatori) {
            if (att.getNome().equals(toks[0])) {
                actD = att;
            }
        }

        if (actD == null)
            return;

        String modPrecedente = actD.getNome() + ": " + actD.getModalitaAttuale() + " -> ";
        if (toks[1].contains("|")) {
            String[] againTokens = toks[1].split("\\|");

            if (!againTokens[2].matches("-?[0-9]+"))
                return;

            actD.setModalitaAttuale(againTokens[0], againTokens[1], Integer.parseInt(againTokens[2]));
        } else {
            actD.setModalitaAttuale(toks[1]);
        }
        System.out.println(modPrecedente + actD.getModalitaAttuale() + "\n");

    }

    /**
     * Il metodo utilizza gli operatori logici per separare la stringa delle condizioni e verificare singolarmente le varie operazioni e poi applicare
     * gli operatori logici di AND e OR.
     * @param cos è la condizione affinchè una regola si verifichi.
     * @param listaSensori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     * @return il risultato di una determinata espressione booleana/antecedente
     */
    private boolean calculate(String cos, ArrayList<Sensore> listaSensori) {
        if (cos.equals("true"))
            return true;

        if (cos.contains("OR")) {
            String[] expTok = cos.split(" OR ", 2);
            return calculate(expTok[0], listaSensori) || calculate(expTok[1], listaSensori);
        }

        if (cos.contains("AND")) {
            String[] expTok = cos.split(" AND ", 2);
            return calculate(expTok[0], listaSensori) && calculate(expTok[1], listaSensori);
        }

        if (cos.matches("[^<>=\t\n ]+ ([<>=]|<=|>=) [^<>=\t\n ]+")) {
            String[] expTok = cos.split(" ");
            return getValExp(expTok, listaSensori);

        }
        return false;

    }

    /**
     * Il metodo viene usato per acquisire i valori dei sensori in gioco e per confrontare l'effettiva operazione tra due sensori o tra un sensore e un valore numerico costante
     * o una stringa nel caso di un'informazione non numerica
     *
     * @param listaSensori dell'unità immobiliare sulla quale si stanno effettuando le operazioni
     * @param toks componenti dell'espressione da valutare
     * @return il risultato dell'operazione in termini di true se le operazioni sono verificate altrimenti false se sono false
     */
    private boolean getValExp(String[] toks, ArrayList<Sensore> listaSensori) {
        String var1 = toks[0];
        String operator = toks[1];
        String var2 = toks[2];


        String[] sensVar = var1.split("\\.");
        Sensore sens1 = null;

        for (Sensore s : listaSensori) {
            if (s.getNome().equals(sensVar[0])) {
                sens1 = s;
                break;
            }
        }

        if (sens1 == null)
            return false;

        Informazione misura1 = sens1.getInformazione(sensVar[1]);

        if (misura1 == null) {
            return false;
        }

        if (var2.matches("-?[0-9]+")) {

            if (misura1.getTipo().equals("NN"))
                return false;

            int value = (int) misura1.getValore();
            int num = Integer.parseInt(var2);

            System.out.println();

            return evalOp(operator, value, num);
        }
        if (var2.matches("[A-Za-z]([a-zA-Z0-9])*_[A-Za-z]([a-zA-Z0-9])+\\.([a-zA-Z0-9])+(_[A-Za-z][a-zA-Z0-9]*)*")) {
            String[] sensVar2 = var2.split("\\.");
            Sensore sens2 = null;

            for (Sensore s : listaSensori) {
                if (s.getNome().equals(sensVar2[0])) {
                    sens2 = s;
                    break;
                }
            }

            if (sens2 == null)
                return false;

            Informazione misura2 = sens2.getInformazione(sensVar2[1]);

            if (misura2 == null)
                return false;

            if (!misura1.getTipo().equals(misura2.getTipo()))
                return false;

            if (misura1.getTipo().equals("NN")) {
                String sca1 = (String) misura1.getValore();
                String sca2 = (String) misura2.getValore();

                if (!operator.equals("="))
                    return false;
                else
                    return sca1.equals(sca2);

            } else {
                int value1 = (int) misura1.getValore();
                int value2 = (int) misura2.getValore();

                return evalOp(operator, value1, value2);
            }
        }

        if (var2.matches("[a-zA-Z]+") && operator.equals("=")) {
            if (misura1.getTipo().equals("NN")) {
                String sca1 = (String) misura1.getValore();
                return var2.equals(sca1);
            }
        }

        return false;
    }

    /**
     * Il metodo effettua il risultato booleano dell'operazione logica tra i due valori con l'operatore designato.
     * @param operator è l'operatore per il confronto della regola
     * @param value1 valore di sx dell'operazione
     * @param value2 valore di dx dell'operazione
     * @return il confronto dell' operazione
     */
    private boolean evalOp(String operator, int value1, int value2) {
        if (operator.startsWith("<")) {
            if (operator.endsWith("="))
                return value1 <= value2;
            else
                return value1 < value2;

        } else if (operator.startsWith(">")) {
            if (operator.endsWith("="))
                return value1 >= value2;
            else
                return value1 > value2;

        } else {
            return value1 == value2;

        }
    }
}
