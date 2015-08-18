/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.demo;

import ch.tsphp.common.ITSPHPAstAdaptor;
import ch.tsphp.common.exceptions.TSPHPException;
import ch.tsphp.tinsphp.common.ITranslator;
import ch.tsphp.tinsphp.common.config.IInferenceEngineInitialiser;
import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.common.translation.IDtoCreator;
import ch.tsphp.tinsphp.common.translation.ITranslatorController;
import ch.tsphp.tinsphp.translators.tsphp.DtoCreator;
import ch.tsphp.tinsphp.translators.tsphp.IOperatorHelper;
import ch.tsphp.tinsphp.translators.tsphp.IPrecedenceHelper;
import ch.tsphp.tinsphp.translators.tsphp.IRuntimeCheckProvider;
import ch.tsphp.tinsphp.translators.tsphp.ITempVariableHelper;
import ch.tsphp.tinsphp.translators.tsphp.ITypeTransformer;
import ch.tsphp.tinsphp.translators.tsphp.ITypeVariableTransformer;
import ch.tsphp.tinsphp.translators.tsphp.PhpPlusOperatorHelper;
import ch.tsphp.tinsphp.translators.tsphp.PhpPlusRuntimeCheckProvider;
import ch.tsphp.tinsphp.translators.tsphp.PhpPlusTypeTransformer;
import ch.tsphp.tinsphp.translators.tsphp.PhpPlusTypeVariableTransformer;
import ch.tsphp.tinsphp.translators.tsphp.PrecedenceHelper;
import ch.tsphp.tinsphp.translators.tsphp.TSPHPTranslator;
import ch.tsphp.tinsphp.translators.tsphp.TempVariableHelper;
import ch.tsphp.tinsphp.translators.tsphp.TranslatorController;
import ch.tsphp.tinsphp.translators.tsphp.issues.HardCodedOutputIssueMessageProvider;
import ch.tsphp.tinsphp.translators.tsphp.issues.IOutputIssueMessageProvider;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class PhpPlusTranslatorInitialiser implements ITranslatorInitialiser
{

    private final ITranslatorController controller;
    private final IInferenceEngineInitialiser inferenceEngineInitialiser;

    private StringTemplateGroup templateGroup;
    private Exception loadingTemplateException;

    public PhpPlusTranslatorInitialiser(
            ITSPHPAstAdaptor anAstAdaptor,
            IInferenceEngineInitialiser theInferenceEngineInitialiser) {
        inferenceEngineInitialiser = theInferenceEngineInitialiser;

        IPrecedenceHelper precedenceHelper = new PrecedenceHelper();
        ITempVariableHelper tempVariableHelper = new TempVariableHelper(anAstAdaptor);


        ITypeTransformer typeTransformer = new PhpPlusTypeTransformer();

        IOutputIssueMessageProvider outputIssueMessageProvider = new HardCodedOutputIssueMessageProvider();
        IRuntimeCheckProvider runtimeCheckProvider = new PhpPlusRuntimeCheckProvider();
        IOperatorHelper operatorHelper = new PhpPlusOperatorHelper();
        ITypeVariableTransformer typeVariableTransformer = new PhpPlusTypeVariableTransformer(typeTransformer);
        IDtoCreator dtoCreator = new DtoCreator(
                tempVariableHelper, typeTransformer, typeVariableTransformer, runtimeCheckProvider);

        controller = new TranslatorController(
                precedenceHelper,
                tempVariableHelper,
                operatorHelper,
                dtoCreator,
                runtimeCheckProvider,
                outputIssueMessageProvider);

        loadStringTemplate();
    }

    private void loadStringTemplate() {
        InputStreamReader streamReader = null;
        try {
            // LOAD TEMPLATES (via classpath)
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final InputStream inputStream = classLoader.getResourceAsStream("TSPHP.stg");
            if (inputStream != null) {
                streamReader = new InputStreamReader(inputStream);
                templateGroup = new StringTemplateGroup(streamReader);
                streamReader.close();
            } else {
                loadingTemplateException = new TSPHPException("TSPHP.stg could not be resolved");
            }
        } catch (IOException ex) {
            loadingTemplateException = ex;
        } finally {
            try {
                if (streamReader != null) {
                    streamReader.close();
                }
            } catch (IOException ex) {
                //no further exception handling needed
            }
        }
    }

    @Override
    public ITranslator build() {
        return new TSPHPTranslator(
                templateGroup,
                controller,
                inferenceEngineInitialiser,
                loadingTemplateException);
    }

    @Override
    public void reset() {
        //nothing to reset
    }
}
