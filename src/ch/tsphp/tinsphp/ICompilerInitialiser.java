/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

/*
 * This file is based on the file ICompilerInitialiser from the TSPHP project.
 * TSPHP is also published under the Apache License 2.0
 * For more information see http://tsphp.ch/wiki/display/TSPHP/License
 */

package ch.tsphp.tinsphp;


import ch.tsphp.tinsphp.common.ICompiler;

import java.util.concurrent.ExecutorService;

public interface ICompilerInitialiser
{

    ICompiler create();

    ICompiler create(final int numberOfWorkers);

    ICompiler create(ExecutorService executorService);
}
