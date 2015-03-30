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

package ch.tsphp.tinsphp;

import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.ITranslatorInitialiser;
import ch.tsphp.tinsphp.inference_engine.InferenceEngine;
import ch.tsphp.tinsphp.parser.ParserFacade;
import ch.tsphp.tinsphp.translators.tsphp.TSPHPTranslatorInitialiser;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HardCodedCompilerInitialiser implements ICompilerInitialiser
{

    private static final int CORE_MULTIPLICATION_FACTOR = 4;

    @Override
    public ICompiler create() {
        return create(Runtime.getRuntime().availableProcessors() * CORE_MULTIPLICATION_FACTOR);
    }

    @Override
    public ICompiler create(final int numberOfWorkers) {
        return create(Executors.newFixedThreadPool(numberOfWorkers));
    }

    public ICompiler create(ExecutorService executorService) {
        Collection<ITranslatorInitialiser> translatorFactories = new ArrayDeque<>();
        translatorFactories.add(new TSPHPTranslatorInitialiser());

        ITSPHPAstAdaptor adaptor = new TSPHPAstAdaptor();

        return new ch.tsphp.tinsphp.Compiler(
                adaptor,
                new ParserFacade(adaptor),
                new InferenceEngine(adaptor),
                translatorFactories,
                executorService);
    }
}
