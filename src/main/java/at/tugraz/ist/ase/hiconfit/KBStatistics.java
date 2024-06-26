/*
 * KBStatistics - A Knowledge Base Statistics Tool
 *
 * Copyright (c) 2022-2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.hiconfit;

import at.tugraz.ist.ase.hiconfit.cli.KBStatistics_CmdLineOptions;
import at.tugraz.ist.ase.hiconfit.fm.core.*;
import at.tugraz.ist.ase.hiconfit.fm.parser.FMFormat;
import at.tugraz.ist.ase.hiconfit.fm.parser.FMParserFactory;
import at.tugraz.ist.ase.hiconfit.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.hiconfit.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.hiconfit.kb.camera.CameraKB;
import at.tugraz.ist.ase.hiconfit.kb.core.KB;
import at.tugraz.ist.ase.hiconfit.kb.fm.FMKB;
import at.tugraz.ist.ase.hiconfit.kb.pc.PCKB;
import at.tugraz.ist.ase.hiconfit.kb.renault.RenaultKB;
import lombok.Cleanup;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The class calculates the statistics of given knowledge bases.
 * Supports the following knowledge bases:
 * - Feature Models from SPLOT, FeatureIDE, Glencoe, and other tools
 * - PC and Renault from <a href="https://www.itu.dk/research/cla/externals/clib/">https://www.itu.dk/research/cla/externals/clib/</a>
 * <p>
 * Supports the following statistics:
 * - The knowledge base name
 * - The knowledge base source
 * - Number of variables
 * - Number of constraints
 * - Number of Choco variables
 * - Number of Choco constraints
 * - The consistency of the knowledge base
 * Statistics for feature models are:
 * - The CTC ratio
 * - The number of features
 * - The number of relationships
 * - The number of cross-tree constraints
 * - The number of MANDATORY relationships
 * - The number of OPTIONAL relationships
 * - The number of ALTERNATIVE relationships
 * - The number of OR relationships
 * - The number of REQUIRES constraints
 * - The number of EXCLUDES constraints
 */
public class KBStatistics {

    static String welcome = """
                           ,--.                                                                                                                             \s
                       ,--/  /|    ,---,.           .--.--.       ___                   ___                           ___                                   \s
                    ,---,': / '  ,'  .'  \\         /  /    '.   ,--.'|_               ,--.'|_    ,--,               ,--.'|_    ,--,                         \s
                    :   : '/ / ,---.' .' |        |  :  /`. /   |  | :,'              |  | :,' ,--.'|               |  | :,' ,--.'|                         \s
                    |   '   ,  |   |  |: |        ;  |  |--`    :  : ' :              :  : ' : |  |,      .--.--.   :  : ' : |  |,                .--.--.   \s
                    '   |  /   :   :  :  /        |  :  ;_    .;__,'  /    ,--.--.  .;__,'  /  `--'_     /  /    '.;__,'  /  `--'_       ,---.   /  /    '  \s
                    |   ;  ;   :   |    ;          \\  \\    `. |  |   |    /       \\ |  |   |   ,' ,'|   |  :  /`./|  |   |   ,' ,'|     /     \\ |  :  /`./  \s
                    :   '   \\  |   :     \\          `----.   \\:__,'| :   .--.  .-. |:__,'| :   '  | |   |  :  ;_  :__,'| :   '  | |    /    / ' |  :  ;_    \s
                    |   |    ' |   |   . |          __ \\  \\  |  '  : |__  \\__\\/: . .  '  : |__ |  | :    \\  \\    `. '  : |__ |  | :   .    ' /   \\  \\    `. \s
                    '   : |.  \\'   :  '; |         /  /`--'  /  |  | '.'| ," .--.; |  |  | '.'|'  : |__   `----.   \\|  | '.'|'  : |__ '   ; :__   `----.   \\\s
                    |   | '_\\.'|   |  | ;         '--'.     /   ;  :    ;/  /  ,.  |  ;  :    ;|  | '.'| /  /`--'  /;  :    ;|  | '.'|'   | '.'| /  /`--'  /\s
                    '   : |    |   :   /            `--'---'    |  ,   /;  :   .'   \\ |  ,   / ;  :    ;'--'.     / |  ,   / ;  :    ;|   :    :'--'.     / \s
                    ;   |,'    |   | ,'                          ---`-' |  ,     .-./  ---`-'  |  ,   /   `--'---'   ---`-'  |  ,   /  \\   \\  /   `--'---'  \s
                    '---'      `----'                                    `--`---'               ---`-'                        ---`-'    `----'              \s
                    """;
    static String programTitle = "Knowledge Base Statistics";
    static String subtitle = """
            Supports the following knowledge bases:
            (1) Feature Models from SPLOT, FeatureIDE, Glencoe,...;\s
            (2) PC and Renault from "https://www.itu.dk/research/cla/externals/clib/\"""";
    static String usage = "Usage: java -jar kbstatistics.jar [options]";

    public static void main(String[] args) {

        KBStatistics_CmdLineOptions cmdLineOptions = new KBStatistics_CmdLineOptions(welcome, programTitle, subtitle, usage);
        cmdLineOptions.parseArgument(args);

        if (cmdLineOptions.isHelp()) {
            cmdLineOptions.printUsage();
            System.exit(0);
        }

        cmdLineOptions.printWelcome();

        KBStatistics kbStatistics = new KBStatistics(cmdLineOptions);
        try {
            kbStatistics.calculate();
        } catch (IOException | FeatureModelParserException e) {
            e.printStackTrace();
        }
        System.out.println("\nDONE.");
    }

    KBStatistics_CmdLineOptions options;

    /**
     * A constructor with a folder's path which stores feature model's files,
     * and the output file's path which will save the statistics.
     */
    public KBStatistics(@NonNull KBStatistics_CmdLineOptions options) {
        this.options = options;
    }

    public void calculate() throws IOException, FeatureModelParserException {
        @Cleanup BufferedWriter writer = new BufferedWriter(new FileWriter(options.getOutFile()));
        // check the type of knowledge base
        int counter = 0;

        if (options.getKb() != null) {
            for (String nameKb : options.getKb()) {
                KB kb = null;
                switch (nameKb) {
                    case "PC" -> {  // if pc, then calculate the statistics of pc
                        System.out.println("\nCalculating statistics for PC...");
                        kb = new PCKB(false);
                    }
                    case "Renault" -> {  // if Renault, then calculate the statistics of Renault
                        System.out.println("\nCalculating statistics for Renault...");
                        kb = new RenaultKB(false);
                    }
                    case "Camera" -> {  // if Camera, then calculate the statistics of Camera
                        System.out.println("\nCalculating statistics for Camera...");
                        kb = new CameraKB(false);
                    }
                }

                checkArgument(kb != null, "The knowledge base is not supported.");

                System.out.println("Saving statistics to " + options.getOutFile() + "...");
                saveStatistics(writer, ++counter, kb);

                System.out.println("Done - " + nameKb);
            }
        }

        if (options.getFm() != null) {
            File file = new File(options.getFm());

            processFM(writer, ++counter, file);
        }

        if (options.getFmDir() != null) {
            // if a folder, then calculate the statistics of all feature models in the folder
            File folder = new File(options.getFmDir());

            for (final File file : Objects.requireNonNull(folder.listFiles())) {
                // check if the file is a feature model
                if (FMFormat.getFMFormat(file.getName()).isValid()) {
                    processFM(writer, ++counter, file);
                }
            }
        }
    }

    private void processFM(BufferedWriter writer, int counter, File file) throws IOException, FeatureModelParserException {
        System.out.println("\nCalculating statistics for " + file.getName() + "...");

        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(file.getName());

        FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> fm = parser.parse(file);
        FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> fmkb = new FMKB<>(fm, false);

        System.out.println("Saving statistics to " + options.getOutFile() + "...");
        saveFMStatistics(writer, counter, fmkb, fm);

        System.out.println("Done - " + file.getName());
    }

    private void saveStatistics(BufferedWriter writer, int counter, KB kb) throws IOException {
        boolean consistent = kb.getModelKB().getSolver().solve();

        writer.write(String.valueOf(counter)); writer.newLine();
        writer.write("Name: " + kb.getName()); writer.newLine();
        writer.write("Source: " + kb.getSource()); writer.newLine();
        writer.write("#variables: " + kb.getNumVariables()); writer.newLine();
        writer.write("#constraints: " + kb.getNumConstraints()); writer.newLine();
        writer.write("#Choco variables: " + kb.getNumChocoVars()); writer.newLine();
        writer.write("#Choco constraints: " + kb.getNumChocoConstraints()); writer.newLine();
        writer.write("Consistency: " + consistent); writer.newLine();

        writer.flush();
    }

    @SuppressWarnings("unchecked")
    private void saveFMStatistics(BufferedWriter writer, int counter, KB kb,
                                  FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> fm) throws IOException {
        double ctc = (double)fm.getNumOfConstraints() / kb.getNumConstraints();

        saveStatistics(writer, counter, kb);

        writer.newLine();
        writer.write("CTC ratio: " + ctc); writer.newLine();
        writer.write("#features: " + fm.getNumOfFeatures()); writer.newLine();
        writer.write("#leaf features: " + fm.getNumOfLeaf()); writer.newLine();
        writer.write("#relationships: " + fm.getNumOfRelationships()); writer.newLine();
        writer.write("#constraints: " + fm.getNumOfConstraints()); writer.newLine();
        writer.write("#MANDATORY: " + fm.getNumOfRelationships(MandatoryRelationship.class)); writer.newLine();
        writer.write("#OPTIONAL: " + fm.getNumOfRelationships(OptionalRelationship.class)); writer.newLine();
        writer.write("#ALTERNATIVE: " + fm.getNumOfRelationships(AlternativeRelationship.class)); writer.newLine();
        writer.write("#OR: " + fm.getNumOfRelationships(OrRelationship.class)); writer.newLine();
        writer.write("#REQUIRES: " + fm.getNumOfRequires()); writer.newLine();
        writer.write("#EXCLUDES: " + fm.getNumOfExcludes()); writer.newLine();

        writer.flush();
    }
}
