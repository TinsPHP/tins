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

package ch.tsphp.tinsphp.test.unit;


import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.Compiler;
import ch.tsphp.tinsphp.common.IInferenceEngine;
import ch.tsphp.tinsphp.common.IParser;
import ch.tsphp.tinsphp.common.ITranslatorInitialiser;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompilerTest
{

    protected ITSPHPAstAdaptor astAdaptor;
    protected IParser parser;
    protected IInferenceEngine inferenceEngine;
    protected Collection<ITranslatorInitialiser> translatorFactories;
    protected ExecutorService executorService;


    @Test
    public void log_NoErrorLoggers_HasFoundIsTrue() {
        //no arrange necessary

        Compiler compiler = createCompiler();
        compiler.log(new TSPHPException(), EIssueSeverity.Notice);

        assertThat(compiler.hasFound(EnumSet.of(EIssueSeverity.Notice)), is(true));
    }

    @Test
    public void log_Standard_HasFoundErrorIsTrue() {
        //no arrange necessary

        IIssueLogger logger = mock(IIssueLogger.class);
        TSPHPException exception = new TSPHPException();

        Compiler compiler = createCompiler();
        compiler.registerIssueLogger(logger);
        compiler.log(exception, EIssueSeverity.Error);

        assertThat(compiler.hasFound(EnumSet.of(EIssueSeverity.Error)), is(true));
    }

    @Test
    public void registerErrorLogger_Standard_IsInformedIfSomethingIsLogged() {
        IIssueLogger logger = mock(IIssueLogger.class);
        TSPHPException exception = new TSPHPException();

        Compiler compiler = createCompiler();
        compiler.registerIssueLogger(logger);
        compiler.log(exception, EIssueSeverity.Error);

        verify(logger).log(exception, EIssueSeverity.Error);
    }

    protected Compiler createCompiler() {
        astAdaptor = mock(ITSPHPAstAdaptor.class);
        parser = mock(IParser.class);
        inferenceEngine = mock(IInferenceEngine.class);
        translatorFactories = new ArrayList<>();
        executorService = mock(ExecutorService.class);
        return new Compiler(astAdaptor, parser, inferenceEngine, translatorFactories, executorService);
    }
}
