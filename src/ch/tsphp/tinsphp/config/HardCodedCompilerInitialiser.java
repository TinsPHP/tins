/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

/*
 * This file is based on the file HardCodedCompilerInitialiser from the TSPHP project.
 * TSPHP is also published under the Apache License 2.0
 * For more information see http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.tinsphp.config;

import ch.tsphp.common.AstHelper;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.config.ICompilerInitialiser;
import ch.tsphp.tinsphp.common.config.ICoreInitialiser;
import ch.tsphp.tinsphp.common.config.IInferenceEngineInitialiser;
import ch.tsphp.tinsphp.common.config.ISymbolsInitialiser;
import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.core.config.HardCodedCoreInitialiser;
import ch.tsphp.tinsphp.inference_engine.config.HardCodedInferenceEngineInitialiser;
import ch.tsphp.tinsphp.parser.ParserFacade;
import ch.tsphp.tinsphp.symbols.config.HardCodedSymbolsInitialiser;
import ch.tsphp.tinsphp.translators.tsphp.config.HardCodedTSPHPTranslatorInitialiser;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HardCodedCompilerInitialiser implements ICompilerInitialiser
{

    private static final int CORE_MULTIPLICATION_FACTOR = 4;
    private final ICompiler compiler;

    public HardCodedCompilerInitialiser() {
        this(Runtime.getRuntime().availableProcessors() * CORE_MULTIPLICATION_FACTOR);
    }

    public HardCodedCompilerInitialiser(final int numberOfWorkers) {
        this(Executors.newFixedThreadPool(numberOfWorkers));
    }

    public HardCodedCompilerInitialiser(ExecutorService theExecutorService) {

        ITSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();
        ISymbolsInitialiser symbolsInitialiser = new HardCodedSymbolsInitialiser();
        AstHelper astHelper = new AstHelper(astAdaptor);
        ICoreInitialiser coreInitialiser = new HardCodedCoreInitialiser(astHelper, symbolsInitialiser);
        IInferenceEngineInitialiser inferenceEngineInitialiser = new HardCodedInferenceEngineInitialiser(
                astAdaptor, astHelper, symbolsInitialiser, coreInitialiser);
        ITranslatorInitialiser translatorInitialiser = new HardCodedTSPHPTranslatorInitialiser(
                astAdaptor, inferenceEngineInitialiser);
        Collection<ITranslatorInitialiser> translatorInitialisers = new ArrayDeque<>();
        translatorInitialisers.add(translatorInitialiser);

        ParserFacade parser = new ParserFacade(astAdaptor);

        compiler = new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                parser,
                inferenceEngineInitialiser,
                translatorInitialisers,
                theExecutorService,
                Arrays.asList(symbolsInitialiser, coreInitialiser));
    }

    @Override
    public void reset() {
        compiler.reset();
    }

    @Override
    public ICompiler getCompiler() {
        return compiler;
    }
}
