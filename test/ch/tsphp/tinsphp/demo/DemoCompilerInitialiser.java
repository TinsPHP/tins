/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.demo;

import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.config.HardCodedCompilerInitialiser;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoCompilerInitialiser extends HardCodedCompilerInitialiser
{

    public DemoCompilerInitialiser() {
        this(Executors.newSingleThreadExecutor());
    }

    public DemoCompilerInitialiser(ExecutorService theExecutorService) {
        super(theExecutorService);
        Collection<ITranslatorInitialiser> translatorsInitialisers = getTranslatorsInitialisers();
        ITranslatorInitialiser phpPlusTranslatorInitialiser = new PhpPlusTranslatorInitialiser(
                getAstAdaptor(), getInferenceEngineInitialiser());
        translatorsInitialisers.add(phpPlusTranslatorInitialiser);
    }

}
