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
import ch.tsphp.tinsphp.common.ITranslatorInitialiser;
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Compiler implements ICompiler, IIssueLogger
{

    private final ITSPHPAstAdaptor astAdaptor;
    private final IParser parser;
    private final IInferenceEngine inferenceEngine;
    private final ExecutorService executorService;

    private final Collection<ICompilerListener> compilerListeners = new ArrayDeque<>();
    private final Collection<ITranslatorInitialiser> translatorFactories;

    private Collection<CompilationUnitDto> compilationUnits = new ArrayDeque<>();
    private final Collection<IIssueLogger> iIssueLoggers = new ArrayDeque<>();
    private boolean isCompiling = false;
    private boolean needReset = false;
    private EnumSet<EIssueSeverity> foundIssues = EnumSet.noneOf(EIssueSeverity.class);

    private final Object lock = new Object();
    private Map<String, String> translations = new HashMap<>();
    private Collection<Future> tasks = new ArrayDeque<>();

    public Compiler(
            ITSPHPAstAdaptor theAstAdaptor,
            IParser theParser,
            IInferenceEngine theInferenceEngine,
            Collection<ITranslatorInitialiser> theTranslatorFactories,
            ExecutorService theExecutorService) {

        astAdaptor = theAstAdaptor;
        inferenceEngine = theInferenceEngine;
        parser = theParser;
        translatorFactories = theTranslatorFactories;
        executorService = theExecutorService;

        init();
    }

    private void init() {
        parser.registerIssueLogger(this);
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
        iIssueLoggers.add(errorLogger);
    }

    @Override
    public void log(TSPHPException exception, EIssueSeverity severity) {
        foundIssues.add(severity);
        for (IIssueLogger logger : iIssueLoggers) {
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
        synchronized (lock) {
            doesNotNeedReset = !needReset;
        }
        if (doesNotNeedReset) {
            tasks.add(executorService.submit(runner));
        } else {
            throw new CompilerException("Tried to parse after calling compile(). If compilation was finished "
                    + "and you wish to recompile, then use reset() first.");
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
        synchronized (lock) {
            doesNotNeedReset = !needReset;
            isCompiling = true;
            needReset = true;
        }
        if (doesNotNeedReset) {
            waitUntilExecutorFinished(new Runnable()
            {
                @Override
                public void run() {
                    doReferencePhase();
                }
            });
        } else {
            throw new CompilerException("Cannot compile during an ongoing compilation.");
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
        inferenceEngine.reset();
        parser.reset();
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
                }
            }
        }).start();

    }

    private void doReferencePhase() {
        informParsingDefinitionCompleted();
        if (!compilationUnits.isEmpty()) {
            if (!foundIssues.contains(EIssueSeverity.FatalError)) {
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
            if (!foundIssues.contains(EIssueSeverity.FatalError)) {
                for (CompilationUnitDto compilationUnit : compilationUnits) {
                    tasks.add(executorService.submit(new InferencePhaseRunner(compilationUnit)));
                }
                waitUntilExecutorFinished(new Runnable()
                {
                    @Override
                    public void run() {
                        doTranslation();
                    }
                });
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

    private void doTranslation() {
        informTypeCheckingCompleted();
        if (!foundIssues.contains(EIssueSeverity.FatalError)) {
            if (translatorFactories != null && translatorFactories.size() > 0) {
                for (final ITranslatorInitialiser translatorFactory : translatorFactories) {
                    for (final CompilationUnitDto compilationUnit : compilationUnits) {
                        tasks.add(executorService.submit(new TranslatorRunner(translatorFactory, compilationUnit)));
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
                log(new TSPHPException("No translator factories specified"), EIssueSeverity.FatalError);
                informCompilingCompleted();
            }
        } else {
            log(new TSPHPException("Translation aborted due to occurred fatal errors"), EIssueSeverity.FatalError);
            informCompilingCompleted();
        }
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
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

    private void informTypeCheckingCompleted() {
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

    private class InferencePhaseRunner implements Runnable
    {

        private final CompilationUnitDto dto;

        InferencePhaseRunner(CompilationUnitDto aDto) {
            dto = aDto;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            try {
                inferenceEngine.enrichtWithTypes(dto.compilationUnit, dto.treeNodeStream);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                        EIssueSeverity.FatalError);
            }
        }
    }

    private class TranslatorRunner implements Runnable
    {

        private final CompilationUnitDto dto;
        private final ITranslatorInitialiser translatorFactory;

        public TranslatorRunner(ITranslatorInitialiser theTranslatorFactory, CompilationUnitDto compilationUnit) {
            translatorFactory = theTranslatorFactory;
            dto = compilationUnit;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            try {
                dto.treeNodeStream.reset();
                ITranslator translator = translatorFactory.build();
                translator.registerIssueLogger(Compiler.this);
                String translation = translator.translate(dto.treeNodeStream);
                translations.put(dto.id, translation);
            } catch (Exception ex) {
                log(new TSPHPException("Unexpected exception occurred: " + ex.getMessage(), ex),
                        EIssueSeverity.FatalError);
            }
        }
    }
}
