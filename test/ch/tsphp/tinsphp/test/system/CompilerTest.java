/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.test.system;

import ch.tsphp.tinsphp.common.ICompiler;
import ch.tsphp.tinsphp.common.issues.EIssueSeverity;
import ch.tsphp.tinsphp.test.testutils.ACompilerTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    public void testFunctionCallWithArguments() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test",
                "<?php function foo($x, $y){return $x + $y;} "
                        + "$a = foo(1, 2);"
                        + "$b = foo(1.2, 2.5);"
                        + "$c = foo(1, 2.5);"
                        + "$d = foo(1.5, 2);"
                        + "$e = foo(1, true);"
                        + "$f = foo(true, true);"
                        + "$g = foo('a', 1.2);"
                        + "$h = foo('a', 2);"
                        + "$i = foo('1', '2');");
        compileAndCheck(compiler, "test", "namespace{\n"
                + "    num $i;\n"
                + "    num $h;\n"
                + "    float $g;\n"
                + "    int $f;\n"
                + "    int $e;\n"
                + "    float $d;\n"
                + "    float $c;\n"
                + "    float $b;\n"
                + "    int $a;\n"
                + "\n"
                + "    function array foo0(array $x, array $y) {\n"
                + "        return $x + $y;\n"
                + "    }\n"
                + "\n"
                + "    function float foo1(float $x, float $y) {\n"
                + "        return $x + $y;\n"
                + "    }\n"
                + "\n"
                + "    function int foo2(int $x, int $y) {\n"
                + "        return $x + $y;\n"
                + "    }\n"
                + "\n"
                + "    function T foo3<T>({as T} $x, {as T} $y) where [T <: num] {\n"
                + "        return cast<T>(oldSchoolAddition($x, $y));\n"
                + "    }\n"
                + "\n"
                + "    $a = foo2(1, 2);\n"
                + "    $b = foo1(1.2, 2.5);\n"
                + "    $c = foo3(1, 2.5);\n"
                + "    $d = foo3(1.5, 2);\n"
                + "    $e = foo3(1, true);\n"
                + "    $f = foo3(true, true);\n"
                + "    $g = foo3('a', 1.2);\n"
                + "    $h = foo3('a', 2);\n"
                + "    $i = foo3('1', '2');\n"
                + "}");
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

    @Test
    public void testErroneousCodeBeforeCompileCorrectCode() throws InterruptedException, IOException {
        ICompiler compiler = createCompiler();
        compiler.addCompilationUnit("test", "!erroneousCode!");
        compiler.compile();
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(compiler.hasFound(EnumSet.allOf(EIssueSeverity.class)));

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
}
