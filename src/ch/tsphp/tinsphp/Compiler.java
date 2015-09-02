/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

/*
 * This class is based on the class Compiler from the TSPHP project.
 * TSPHP is also published under the Apache License 2.0
 * For more information see http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.tinsphp;

import ch.tsphp.common.CompilationUnitDto;
import ch.tsphp.common.ICompilerListener;
import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.ParserUnitDto;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.IInferenceEngine;
import ch.tsphp.tinsphp.common.IParser;
import ch.tsphp.tinsphp.common.ITranslator;
import ch.tsphp.tinsphp.common.config.IInferenceEngineInitialiser;
import ch.tsphp.tinsphp.common.config.IInitialiser;
import ch.tsphp.tinsphp.common.config.IParserInitialiser;
import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.common.issues.IIssueLogger;
import ch.tsphp.tinsphp.common.issues.IssueReporterHelper;
import ch.tsphp.tinsphp.exceptions.CompilerException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Compiler implements ICompiler, IIssueLogger
{

    private final ITSPHPAstAdaptor astAdaptor;
    private final IParserInitialiser parserInitialiser;
    private IParser parser;
    private final IInferenceEngineInitialiser inferenceEngineInitialiser;
    private IInferenceEngine inferenceEngine;
    private final ExecutorService executorService;
    private final List<IInitialiser> initialisers;

    private final Collection<ICompilerListener> compilerListeners = new ArrayDeque<>();
    private final Collection<ITranslatorInitialiser> translatorFactories;

    private Collection<CompilationUnitDto> compilationUnits = new ArrayDeque<>();
    private final Collection<IIssueLogger> issueLoggers = new ArrayDeque<>();
    private boolean isCompiling = false;
    private boolean needReset = false;
    private boolean isShutdown = false;
    private EnumSet<EIssueSeverity> foundIssues = EnumSet.noneOf(EIssueSeverity.class);

    private final Object lock = new Object();
    private Map<String, String> translations = new HashMap<>();
    private Collection<Future> tasks = new ArrayDeque<>();

    public Compiler(
            ITSPHPAstAdaptor theAstAdaptor,
            IParserInitialiser theParserInitialiser,
            IInferenceEngineInitialiser theInferenceEngineInitialiser,
            Collection<ITranslatorInitialiser> theTranslatorFactories,
            ExecutorService theExecutorService,
            List<IInitialiser> initialisersToReset) {

        astAdaptor = theAstAdaptor;
        inferenceEngineInitialiser = theInferenceEngineInitialiser;
        parserInitialiser = theParserInitialiser;
        translatorFactories = theTranslatorFactories;
        executorService = theExecutorService;
        initialisers = initialisersToReset;
        initialisers.addAll(translatorFactories);

        init();
    }

    private void init() {
        parser = parserInitialiser.getParser();
        parser.registerIssueLogger(this);
        inferenceEngine = inferenceEngineInitialiser.getEngine();
        inferenceEngine.registerIssueLogger(this);
    }

    @Override
    public void registerCompilerListener(ICompilerListener listener) {
        compilerListeners.add(listener);
    }

    @Override
    public boolean hasFound(EnumSet<EIssueSeverity> severities) {
        boolean hasStartedCompiling;
        synchronized (lock) {
            hasStartedCompiling = isCompiling;
        }
        if (hasStartedCompiling) {
            throw new CompilerException("Cannot check for exceptions during compilation.");
        }

        return IssueReporterHelper.hasFound(foundIssues, severities);
    }

    @Override
    public void registerIssueLogger(IIssueLogger errorLogger) {
        issueLoggers.add(errorLogger);
    }

    @Override
    public void log(TSPHPException exception, EIssueSeverity severity) {
        foundIssues.add(severity);
        for (IIssueLogger logger : issueLoggers) {
            logger.log(exception, severity);
        }
    }

    @Override
    public void addCompilationUnit(String id, final String string) {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) {
                return theParser.parse(string);
            }
        }));
    }

    private void add(ParseAndDefinitionPhaseRunner runner) {
        boolean doesNotNeedReset;
        boolean isNotShutdown;
        synchronized (lock) {
            doesNotNeedReset = !needReset;
            isNotShutdown = !isShutdown;
        }
        if (doesNotNeedReset && isNotShutdown) {
            tasks.add(executorService.submit(runner));
        } else if (!doesNotNeedReset) {
            throw new CompilerException("Tried to parse after calling compile(). If compilation was finished "
                    + "and you wish to recompile, then use reset() first.");
        } else {
            throw new CompilerException("Compiler was shutdown and cannot longer be used.");
        }
    }

    @Override
    public void addCompilationUnit(String id, final char[] chars, final int numberOfActualCharsInArray) {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) {
                return theParser.parse(chars, numberOfActualCharsInArray);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final int size) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, size);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final String encoding) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, encoding);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final int initialBufferSize,
            final String encoding)
            throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, initialBufferSize, encoding);
            }
        }));
    }

    @Override
    public void addCompilationUnit(String id, final InputStream inputStream, final int initialBufferSize,
            final int readBufferSize,
            final String encoding) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(id, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseInputStream(inputStream, initialBufferSize, readBufferSize, encoding);
            }
        }));
    }

    @Override
    public void addFile(final String pathToFileInclFileName) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(pathToFileInclFileName, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseFile(pathToFileInclFileName);
            }
        }));
    }

    @Override
    public void addFile(final String pathToFileInclFileName, final String encoding) throws IOException {
        add(new ParseAndDefinitionPhaseRunner(pathToFileInclFileName, new IParserMethod()
        {
            @Override
            public ParserUnitDto parser(IParser theParser) throws IOException {
                return theParser.parseFile(pathToFileInclFileName, encoding);
            }
        }));
    }

    @Override
    public void compile() {
        boolean doesNotNeedReset;
        boolean isNotShutdown;

        synchronized (lock) {
            doesNotNeedReset = !needReset;
            isNotShutdown = !isShutdown;
            isCompiling = true;
            needReset = true;

        }
        if (doesNotNeedReset && isNotShutdown) {
            waitUntilExecutorFinished(new Runnable()
            {
                @Override
                public void run() {
                    doReferencePhase();
                }
            });
        } else if (!doesNotNeedReset) {
            throw new CompilerException("Cannot compile during an ongoing compilation.");
        } else {
            throw new CompilerException("Compiler was shutdown and cannot longer be used.");
        }
    }

    @Override
    public boolean isCompiling() {
        synchronized (lock) {
            return isCompiling;
        }
    }

    @Override
    public boolean needsAReset() {
        synchronized (lock) {
            return needReset;
        }
    }

    @Override
    public void reset() {
        boolean hasStartedCompiling;
        synchronized (lock) {
            hasStartedCompiling = isCompiling;
        }
        if (hasStartedCompiling) {
            throw new CompilerException("Cannot reset during compilation.");
        }
        parserInitialiser.reset();
        inferenceEngineInitialiser.reset();
        for (IInitialiser initialiser : initialisers) {
            initialiser.reset();
        }
        compilationUnits = new ArrayDeque<>();
        translations = new HashMap<>();
        foundIssues = EnumSet.noneOf(EIssueSeverity.class);
        needReset = false;
    }

    @SuppressWarnings("checkstyle:illegalcatch")
    private void waitUntilExecutorFinished(final Runnable callback) {
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    for (Future task : tasks) {
                        task.get();
                    }
                    tasks = new ArrayDeque<>();
                    callback.run();
                } catch (Exception ex) {
                    log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                            EIssueSeverity.FatalError);
                    informCompilingCompleted();
                }
            }
        }).start();

    }

    private void doReferencePhase() {
        informParsingDefinitionCompleted();
        if (!compilationUnits.isEmpty()) {
            if (noIssuesOrCanBeIgnored()) {
                for (CompilationUnitDto compilationUnit : compilationUnits) {
                    tasks.add(executorService.submit(new ReferencePhaseRunner(compilationUnit)));
                }
                waitUntilExecutorFinished(new Runnable()
                {
                    @Override
                    public void run() {
                        doInferencePhase();
                    }
                });
            } else {
                log(new TSPHPException("Reference phase aborted due to occurred fatal errors."),
                        EIssueSeverity.FatalError);
                informCompilingCompleted();
            }
        } else {
            log(new TSPHPException("No compilation units specified"), EIssueSeverity.FatalError);
            informCompilingCompleted();
        }
    }

    private void doInferencePhase() {
        informReferenceCompleted();
        if (!compilationUnits.isEmpty()) {
            if (noIssuesOrCanBeIgnored()) {
                try {
                    //solving constraints is over all compilation units. Parallelism happens inside of this method
                    inferenceEngine.solveConstraints();
                } catch (Exception ex) {
                    log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                            EIssueSeverity.FatalError);
                }
                doTranslation();
            } else {
                log(new TSPHPException("Inference phase aborted due to occurred fatal errors."),
                        EIssueSeverity.FatalError);
                informCompilingCompleted();
            }
        } else {
            log(new TSPHPException("No compilation units specified"), EIssueSeverity.FatalError);
            informCompilingCompleted();
        }
    }

    private boolean noIssuesOrCanBeIgnored() {
        return !foundIssues.contains(EIssueSeverity.FatalError) && !foundIssues.contains(EIssueSeverity.Error);
    }

    private void doTranslation() {
        informInferenceCompleted();
        if (noIssuesOrCanBeIgnored()) {
            Iterator<ITranslatorInitialiser> iterator = translatorFactories.iterator();
            if (iterator.hasNext()) {
                ITranslatorInitialiser translatorInitialiser = iterator.next();
                for (final CompilationUnitDto compilationUnit : compilationUnits) {
                    tasks.add(executorService.submit(new TranslatorRunner(translatorInitialiser, compilationUnit)));
                }
                while (iterator.hasNext()) {
                    translatorInitialiser = iterator.next();
                    for (final CompilationUnitDto compilationUnit : compilationUnits) {
                        CommonTreeNodeStream commonTreeNodeStream = new CommonTreeNodeStream(
                                astAdaptor, compilationUnit.compilationUnit);
                        commonTreeNodeStream.setTokenStream(compilationUnit.treeNodeStream.getTokenStream());
                        CompilationUnitDto copy = new CompilationUnitDto(
                                compilationUnit.id, compilationUnit.compilationUnit, commonTreeNodeStream);
                        tasks.add(executorService.submit(new TranslatorRunner(translatorInitialiser, copy)));
                    }
                }
                waitUntilExecutorFinished(new Runnable()
                {
                    @Override
                    public void run() {
                        informCompilingCompleted();
                    }
                });
            } else {
                log(new TSPHPException("No translator initialisers specified"), EIssueSeverity.FatalError);
                informCompilingCompleted();
            }
        } else {
            log(new TSPHPException("Translation aborted due to occurred errors or fatal errors"),
                    EIssueSeverity.FatalError);
            informCompilingCompleted();
        }
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        compilationUnits = null;
        isShutdown = true;
    }

    private void informParsingDefinitionCompleted() {
        for (ICompilerListener listener : compilerListeners) {
            listener.afterParsingAndDefinitionPhaseCompleted();
        }
    }

    private void informReferenceCompleted() {
        for (ICompilerListener listener : compilerListeners) {
            listener.afterReferencePhaseCompleted();
        }
    }

    private void informInferenceCompleted() {
        for (ICompilerListener listener : compilerListeners) {
            listener.afterTypecheckingCompleted();
        }
    }

    private void informCompilingCompleted() {
        isCompiling = false;
        for (ICompilerListener listener : compilerListeners) {
            listener.afterCompilingCompleted();
        }
    }

    /**
     * Delegate of a parser method which returns a ParserUnitDto.
     */
    private interface IParserMethod
    {

        ParserUnitDto parser(IParser parser) throws IOException;
    }

    private class ParseAndDefinitionPhaseRunner implements Runnable
    {

        private final IParserMethod parserMethod;
        private final String id;

        public ParseAndDefinitionPhaseRunner(String theId, IParserMethod aParserMethod) {
            parserMethod = aParserMethod;
            id = theId;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            try {
                ParserUnitDto parserUnit = parserMethod.parser(parser);
                CommonTreeNodeStream commonTreeNodeStream = new CommonTreeNodeStream(
                        astAdaptor, parserUnit.compilationUnit);
                commonTreeNodeStream.setTokenStream(parserUnit.tokenStream);

                inferenceEngine.enrichWithDefinitions(parserUnit.compilationUnit, commonTreeNodeStream);
                compilationUnits.add(new CompilationUnitDto(id, parserUnit.compilationUnit, commonTreeNodeStream));

            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                        EIssueSeverity.FatalError);
            }
        }
    }

    private class ReferencePhaseRunner implements Runnable
    {

        private final CompilationUnitDto dto;

        ReferencePhaseRunner(CompilationUnitDto aDto) {
            dto = aDto;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            try {
                inferenceEngine.enrichWithReferences(dto.compilationUnit, dto.treeNodeStream);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                        EIssueSeverity.FatalError);
            }
        }
    }

    private class TranslatorRunner implements Runnable
    {

        private final CompilationUnitDto dto;
        private final ITranslatorInitialiser translatorInitialiser;

        public TranslatorRunner(ITranslatorInitialiser theTranslatorFactory, CompilationUnitDto compilationUnit) {
            translatorInitialiser = theTranslatorFactory;
            dto = compilationUnit;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            try {
                dto.treeNodeStream.reset();
                ITranslator translator = translatorInitialiser.build();
                translator.registerIssueLogger(Compiler.this);
                String translation = translator.translate(dto.treeNodeStream);
                translations.put(translatorInitialiser.getClass().getCanonicalName() + "_" + dto.id, translation);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                        EIssueSeverity.FatalError);
            }
        }
    }
}
