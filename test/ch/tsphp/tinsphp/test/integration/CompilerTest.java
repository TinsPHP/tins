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

package ch.tsphp.tinsphp.test.integration;


import ch.tsphp.tinsphp.HardCodedCompilerInitialiser;
import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.exceptions.CompilerException;
import ch.tsphp.tinsphp.test.testutils.ACompilerTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompilerTest extends ACompilerTest
{

    @Test
    public void addCompilationUnit_AfterCompileWithoutReset_ThrowsCompilerException() throws InterruptedException {

        ICompiler compiler = new HardCodedCompilerInitialiser().create();
        compiler.compile();
        try {
            compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
            Assert.fail("No compiler exception thrown. It should not be allowed to add compilation units "
                    + "during compilation.");
        } catch (CompilerException ex) {
            //we expect an exception
        }
    }

    @Test
    public void reset_DuringCompilation_ThrowsCompilerException() throws InterruptedException {

        ICompiler compiler = createSlowCompiler();
        compiler.compile();
        try {
            compiler.reset();
            Assert.fail("No compiler exception thrown. It should not be allowed to reset during compilation.");
        } catch (CompilerException ex) {
            //we expect an exception
        }
    }

    @Test
    public void needsAReset_BeforeCompilation_ReturnsFalse() throws InterruptedException {
        //no arrange needed

        ICompiler compiler = createCompiler();
        boolean result = compiler.needsAReset();

        assertThat(result, is(false));
    }

    @Test
    public void needsAReset_AfterCompilation_ReturnsTrue() throws InterruptedException {
        //no arrange needed

        ICompiler compiler = createCompiler();
        compiler.compile();
        boolean result = compiler.needsAReset();

        assertThat(result, is(true));
    }

    @Test
    public void isCompiling_DuringCompilation_ReturnsTrue() throws InterruptedException {

        ICompiler compiler = createSlowCompiler();
        compiler.compile();
        boolean result = compiler.isCompiling();

        assertThat(result, is(true));
    }

    @Test
    public void isCompiling_NotDuringCompilation_ReturnsFalse() throws InterruptedException {

        ICompiler compiler = createCompiler();
        boolean result = compiler.isCompiling();

        assertThat(result, is(false));
    }

    @Test
    public void hasFoundError_DuringCompilation_ThrowsCompilerException() throws InterruptedException {
        ICompiler compiler = createSlowCompiler();
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compiler.compile();
        try {
            compiler.hasFound(EnumSet.allOf(EIssueSeverity.class));
            Assert.fail("No compiler exception thrown. It should not be allowed to test whether errors were found"
                    + " during compilation.");
        } catch (CompilerException ex) {
            //we expect an exception
        }
    }

    @Test
    public void compile_DuringCompilation_ThrowsCompilerException() throws InterruptedException {

        ICompiler compiler = createSlowCompiler();
        compiler.compile();
        try {
            compiler.compile();
            Assert.fail("No compiler exception thrown. It should not be allowed to compile during compilation.");
        } catch (CompilerException ex) {
            //we expect an exception
        }
    }

    @Test
    public void testResetAndCompile() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "<?php $a = 1; ?>");
        compileAndCheck(compiler, "test", "namespace{\n    ? $a;\n    $a = 1;\n}");
        compiler.reset();
        lock = new CountDownLatch(1);
        compiler.addCompilationUnit("test", "<?php $a = 2; ?>");
        compileAndCheck(compiler, "test", "namespace{\n    ? $a;\n    $a = 2;\n}");
    }
}
