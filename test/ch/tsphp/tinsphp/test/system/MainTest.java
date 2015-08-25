/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.test.system;

import ch.tsphp.tinsphp.Main;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MainTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test_NoError_TerminatesSuccessfully() throws IOException, InterruptedException {

        File file = folder.newFile("test.php");
        PrintWriter writer = new PrintWriter(file);
        writer.write("<?php\n"
                + "\n"
                + "function add($x, $y){\n"
                + "    return $x + $y;\n"
                + "}\n"
                + "\n"
                + "function fib($n){\n"
                + "    return $n > 0 ? fib($n - 1) + fib($n - 2) : 1;\n"
                + "}\n"
                + "\n"
                + "function fac($n){\n"
                + "    return $n > 0 ? $n * fac($n) : $n;\n"
                + "}\n"
                + "?>");
        writer.close();

        Main.main(new String[]{file.getAbsolutePath(), file.getParentFile().getAbsolutePath() + "\\tmp.tsphp"});
    }

}
