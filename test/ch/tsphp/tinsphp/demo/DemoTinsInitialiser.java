/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.demo;

import ch.tsphp.tinsphp.common.config.ITranslatorInitialiser;
import ch.tsphp.tinsphp.config.HardCodedTinsInitialiser;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

public class DemoTinsInitialiser extends HardCodedTinsInitialiser
{

    public DemoTinsInitialiser(ExecutorService theExecutorService) {
        super(theExecutorService);
        Collection<ITranslatorInitialiser> translatorsInitialisers = getTranslatorsInitialisers();
        ITranslatorInitialiser phpPlusTranslatorInitialiser = new PhpPlusTranslatorInitialiser(
                getAstAdaptor(), getSymbolsInitialiser(), getInferenceEngineInitialiser());
        translatorsInitialisers.add(phpPlusTranslatorInitialiser);
    }

}
