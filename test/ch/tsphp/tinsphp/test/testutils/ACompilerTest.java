/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

/*
 * This file is part of the TSPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.tinsphp.test.testutils;


import ch.tsphp.common.ACompilerListener;
import ch.tsphp.common.AstHelper;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ParserUnitDto;
import ch.tsphp.common.TSPHPAst;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.IInferenceEngine;
import ch.tsphp.tinsphp.common.IParser;
import ch.tsphp.tinsphp.common.config.ICoreInitialiser;
import ch.tsphp.tinsphp.common.config.IInferenceEngineInitialiser;
import ch.tsphp.tinsphp.common.config.IInitialiser;
import ch.tsphp.tinsphp.common.config.ISymbolsInitialiser;
import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import ch.tsphp.tinsphp.config.HardCodedCompilerInitialiser;
import ch.tsphp.tinsphp.core.config.HardCodedCoreInitialiser;
import ch.tsphp.tinsphp.inference_engine.config.HardCodedInferenceEngineInitialiser;
import ch.tsphp.tinsphp.symbols.config.HardCodedSymbolsInitialiser;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class ACompilerTest
{
    protected CountDownLatch lock = new CountDownLatch(1);

    protected ICompiler createCompiler() {
        ICompiler compiler = new HardCodedCompilerInitialiser().getCompiler();
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.registerIssueLogger(new IIssueLogger()
        {
            @Override
            public void log(TSPHPException e, EIssueSeverity severity) {
                e.printStackTrace();
            }
        });
        return compiler;
    }

    protected void compileAndCheck(ICompiler compiler, String id, String translation) throws InterruptedException {
        compiler.compile();
        lock.await(200, TimeUnit.SECONDS);

        Assert.assertFalse(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)));

        Map<String, String> translations = compiler.getTranslations();
        assertThat(translations.size(), is(1));
        assertThat(translations.get(id).replaceAll("\r", ""), is(translation));
    }

    protected ICompiler createSlowCompiler() {
        ITSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();
        Collection<ITranslatorInitialiser> translatorInitialisers = new ArrayDeque<>();
        translatorInitialisers.add(mock(ITranslatorInitialiser.class));


        IParser mockParser = mock(IParser.class);
        IInferenceEngineInitialiser inferenceEngineInitialiser = mock(IInferenceEngineInitialiser.class);
        when(inferenceEngineInitialiser.getEngine()).thenReturn(mock(IInferenceEngine.class));

        when(mockParser.parse(Mockito.anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(2000);
                return new ParserUnitDto("dummy", new TSPHPAst(), new CommonTokenStream());
            }
        });
        return new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                mockParser,
                inferenceEngineInitialiser,
                translatorInitialisers,
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
    }

    protected IInferenceEngineInitialiser createInferenceEngineInitialiser() {
        ITSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();
        ISymbolsInitialiser symbolsInitialiser = new HardCodedSymbolsInitialiser();
        AstHelper astHelper = new AstHelper(astAdaptor);
        ICoreInitialiser coreInitialiser = new HardCodedCoreInitialiser(astHelper, symbolsInitialiser);
        return new HardCodedInferenceEngineInitialiser(
                astAdaptor, astHelper, symbolsInitialiser, coreInitialiser);
    }
}
