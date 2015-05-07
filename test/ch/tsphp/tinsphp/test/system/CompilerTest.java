/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.test.system;

import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.test.testutils.ACompilerTest;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class CompilerTest extends ACompilerTest
{
    @Test
    public void testResetAndCompileWithConstantDefinition() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "<?php const a = 1; ?>");
        compileAndCheck(compiler, "test", "namespace{\n    const int a = 1;\n}");
        compiler.reset();
        lock = new CountDownLatch(1);
        compiler.addCompilationUnit("test", "<?php const a = 2; ?>");
        compileAndCheck(compiler, "test", "namespace{\n    const int a = 2;\n}");
    }

    @Test
    public void testResetAndCompileWithFunctionCall() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "<?php function foo(){return 'hello world';} foo();");
        compileAndCheck(compiler, "test", "namespace{"
                + "\n"
                + "\n    function string foo() {"
                + "\n        return 'hello world';"
                + "\n    }"
                + "\n"
                + "\n    foo();"
                + "\n}");
        compiler.reset();
        lock = new CountDownLatch(1);
        compiler.addCompilationUnit("test", "<?php function foo(){return 'hello world';} foo();");
        compileAndCheck(compiler, "test", "namespace{"
                + "\n"
                + "\n    function string foo() {"
                + "\n        return 'hello world';"
                + "\n    }"
                + "\n"
                + "\n    foo();"
                + "\n}");
    }

    @Test
    public void testSimpleFunctionCall() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "<?php function foo(){return 'hello world';} foo();");
        compileAndCheck(compiler, "test", "namespace{"
                + "\n"
                + "\n    function string foo() {"
                + "\n        return 'hello world';"
                + "\n    }"
                + "\n"
                + "\n    foo();"
                + "\n}");
    }

    @Test
    public void testResetBeforeFirstCompile() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.reset();
        compiler.addCompilationUnit("test", "<?php function foo(){return 'hello world';} foo();");
        compileAndCheck(compiler, "test", "namespace{"
                + "\n"
                + "\n    function string foo() {"
                + "\n        return 'hello world';"
                + "\n    }"
                + "\n"
                + "\n    foo();"
                + "\n}");
    }
}
