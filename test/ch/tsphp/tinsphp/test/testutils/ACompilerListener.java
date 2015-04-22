/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.test.testutils;

import ch.tsphp.common.ICompilerListener;

public abstract class ACompilerListener implements ICompilerListener
{
    @Override
    public void afterParsingAndDefinitionPhaseCompleted() {

    }

    @Override
    public void afterReferencePhaseCompleted() {

    }

    @Override
    public void afterTypecheckingCompleted() {

    }

    @Override
    public void afterCompilingCompleted() {

    }
}
