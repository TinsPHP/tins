/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

/*
 * This file is based on the file CompilerErrorTest from the TSPHP project.
 * TSPHP is also published under the Apache License 2.0
 * For more information see http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.tinsphp.test.integration;

import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.TSPHPAst;
import ch.tsphp.common.TSPHPAstAdaptor;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.IInferenceEngine;
import ch.tsphp.tinsphp.common.IParser;
import ch.tsphp.tinsphp.common.config.IInferenceEngineInitialiser;
import ch.tsphp.tinsphp.common.config.IInitialiser;
import ch.tsphp.tinsphp.common.config.IParserInitialiser;
import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import ch.tsphp.tinsphp.parser.ParserFacade;
import ch.tsphp.tinsphp.parser.config.HardCodedParserInitialiser;
import ch.tsphp.tinsphp.test.testutils.ACompilerListener;
import ch.tsphp.tinsphp.test.testutils.ACompilerTest;
import org.antlr.runtime.tree.TreeNodeStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompilerErrorTest extends ACompilerTest
{
    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testLogUnexpectedExceptionDuringParsingPhase() throws InterruptedException {
        IIssueLogger logger = mock(IIssueLogger.class);
        RuntimeException exception = new RuntimeException();
        IParserInitialiser parserInitialiser = mock(IParserInitialiser.class);
        IParser parser = spy(new ParserFacade(new TSPHPAstAdaptor()));
        when(parserInitialiser.getParser()).thenReturn(parser);
        when(parser.parse(anyString())).thenThrow(exception);

        ICompiler compiler = new ch.tsphp.tinsphp.Compiler(
                mock(ITSPHPAstAdaptor.class),
                parserInitialiser,
                createInferenceEngineInitialiser(),
                new ArrayList<ITranslatorInitialiser>(),
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
        compiler.registerIssueLogger(logger);
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        lock.await(2, TimeUnit.SECONDS);

        assertThat(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        verify(logger).log(captor.capture(), any(EIssueSeverity.class));
        assertThat(captor.getValue().getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringDefinitionPhase() throws InterruptedException {
        IIssueLogger logger = mock(IIssueLogger.class);
        IInferenceEngineInitialiser inferenceEngineInitialiser = mock(IInferenceEngineInitialiser.class);
        IInferenceEngine inferenceEngine = mock(IInferenceEngine.class);
        when(inferenceEngineInitialiser.getEngine()).thenReturn(inferenceEngine);
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(inferenceEngine).enrichWithDefinitions(any(TSPHPAst.class), any(TreeNodeStream.class));
        TSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();

        ICompiler compiler = new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                new HardCodedParserInitialiser(astAdaptor),
                inferenceEngineInitialiser,
                new ArrayList<ITranslatorInitialiser>(),
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
        compiler.registerIssueLogger(logger);
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);

        assertThat(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        //two since one is about the abortion of the compilation process - translation is not done due to errors
        verify(logger, times(2)).log(captor.capture(), any(EIssueSeverity.class));
        assertThat(captor.getAllValues().get(0).getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringReferencePhase() throws InterruptedException {
        IIssueLogger logger = mock(IIssueLogger.class);
        IInferenceEngineInitialiser inferenceEngineInitialiser = mock(IInferenceEngineInitialiser.class);
        IInferenceEngine inferenceEngine = mock(IInferenceEngine.class);
        when(inferenceEngineInitialiser.getEngine()).thenReturn(inferenceEngine);
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(inferenceEngine).enrichWithReferences(any(TSPHPAst.class), any(TreeNodeStream.class));
        TSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();

        ICompiler compiler = new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                new HardCodedParserInitialiser(astAdaptor),
                inferenceEngineInitialiser,
                new ArrayList<ITranslatorInitialiser>(),
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
        compiler.registerIssueLogger(logger);
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);

        assertThat(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        //two since one is about the abortion of the compilation process - translation is not done due to errors
        verify(logger, times(2)).log(captor.capture(), any(EIssueSeverity.class));
        assertThat(captor.getAllValues().get(0).getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringInferencePhase() throws InterruptedException {
        IIssueLogger logger = mock(IIssueLogger.class);
        IInferenceEngineInitialiser inferenceEngineInitialiser = mock(IInferenceEngineInitialiser.class);
        IInferenceEngine inferenceEngine = mock(IInferenceEngine.class);
        when(inferenceEngineInitialiser.getEngine()).thenReturn(inferenceEngine);
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(inferenceEngine).solveConstraints();
        TSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();

        ICompiler compiler = new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                new HardCodedParserInitialiser(astAdaptor),
                inferenceEngineInitialiser,
                new ArrayList<ITranslatorInitialiser>(),
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
        compiler.registerIssueLogger(logger);
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);

        assertThat(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        //two since one is about the abortion of the compilation process - translation is not done due to errors
        verify(logger, times(2)).log(captor.capture(), any(EIssueSeverity.class));
        assertThat(captor.getAllValues().get(0).getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogUnexpectedExceptionDuringTranslatorPhase() throws InterruptedException {
        IIssueLogger logger = mock(IIssueLogger.class);
        RuntimeException exception = new RuntimeException();
        ITranslatorInitialiser translatorInitialiser = mock(ITranslatorInitialiser.class);
        when(translatorInitialiser.build()).thenThrow(exception);
        Collection<ITranslatorInitialiser> translatorFactories = new ArrayList<>();
        translatorFactories.add(translatorInitialiser);
        TSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();

        ICompiler compiler = new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                new HardCodedParserInitialiser(astAdaptor),
                createInferenceEngineInitialiser(),
                translatorFactories,
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
        compiler.registerIssueLogger(logger);
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);

        assertThat(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)), is(true));
        ArgumentCaptor<TSPHPException> captor = ArgumentCaptor.forClass(TSPHPException.class);
        verify(logger).log(captor.capture(), any(EIssueSeverity.class));
        assertThat(captor.getValue().getCause(), is((Throwable) exception));
    }

    @Test
    public void testLogWhenNoTranslatorInitialiserIsProvided() throws InterruptedException {
        IIssueLogger logger = mock(IIssueLogger.class);
        TSPHPAstAdaptor astAdaptor = new TSPHPAstAdaptor();

        ICompiler compiler = new ch.tsphp.tinsphp.Compiler(
                astAdaptor,
                new HardCodedParserInitialiser(astAdaptor),
                createInferenceEngineInitialiser(),
                new ArrayList<ITranslatorInitialiser>(),
                Executors.newSingleThreadExecutor(),
                new ArrayList<IInitialiser>());
        compiler.registerIssueLogger(logger);
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compiler.registerCompilerListener(new ACompilerListener()
        {
            @Override
            public void afterCompilingCompleted() {
                lock.countDown();
            }
        });
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);

        assertThat(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)), is(true));
        verify(logger).log(any(TSPHPException.class), any(EIssueSeverity.class));
    }
}
