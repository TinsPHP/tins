/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp;

import ch.tsphp.common.ICompilerListener;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import ch.tsphp.tinsphp.config.HardCodedTinsInitialiser;
import ch.tsphp.tinsphp.translators.tsphp.config.HardCodedTSPHPTranslatorInitialiser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

public class Main implements ICompilerListener, IIssueLogger
{
    private final CountDownLatch latch;
    private final ICompiler compiler;
    private final String outputFilePath;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 2) {
            new Main(args[0], args[1]);
        } else {
            throw new IllegalArgumentException("Require two arguments, input file and output path");
        }
    }

    public Main(String inputFilePath, String theOutputFilePath) throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
        outputFilePath = theOutputFilePath;
        compiler = new HardCodedTinsInitialiser().getCompiler();
        compiler.registerCompilerListener(this);
        compiler.registerIssueLogger(this);
        compiler.addCompilationUnit("test", new FileInputStream(inputFilePath));
        compiler.compile();
        latch.await();
        compiler.shutdown();
        if (!compiler.hasFound(EnumSet.of(EIssueSeverity.FatalError, EIssueSeverity.Error))) {
            System.out.println("Translation done, output written to " + theOutputFilePath);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void afterCompilingCompleted() {
        if (!compiler.hasFound(EnumSet.of(EIssueSeverity.FatalError, EIssueSeverity.Error))) {
            String output = compiler.getTranslations().get(
                    HardCodedTSPHPTranslatorInitialiser.class.getCanonicalName() + "_test");
            try {
                PrintWriter codeWriter = new PrintWriter(outputFilePath, "UTF-8");
                codeWriter.write(output);
                codeWriter.close();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                System.err.print("could not write to file " + output + ". " + e.getMessage());
            }
        }
        latch.countDown();
    }

    @Override
    public void log(TSPHPException e, EIssueSeverity severity) {
        if (severity == EIssueSeverity.Error || severity == EIssueSeverity.FatalError) {
            System.err.println("Issue with severity " + severity + " occurred.");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void afterParsingAndDefinitionPhaseCompleted() {

    }

    @Override
    public void afterReferencePhaseCompleted() {

    }

    @Override
    public void afterTypecheckingCompleted() {

    }

}
