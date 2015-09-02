/*
 * This file is part of the TinsPHP project published under the Apache License 2.0
 * For the full copyright and license information, please have a look at LICENSE in the
 * root folder or visit the project's website http://tsphp.ch/wiki/display/TINS/License
 */

package ch.tsphp.tinsphp.test.system;

import ch.tsphp.tinsphp.Main;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test_SyntaxError_TerminatesSuccessfully() throws IOException, InterruptedException {
        final CountDownLatch latch = setUpTest("$forgotPhpTags = 1;");
        final int waitTime = 2;
        if (!latch.await(waitTime, TimeUnit.SECONDS)) {
            Assert.fail("Did not terminate properly, waited " + waitTime + " seconds");
        }
    }

    @Test
    public void test_ReferenceError_TerminatesSuccessfully() throws IOException, InterruptedException {
        final CountDownLatch latch = setUpTest("<?php echo nonExistingConstant;");
        final int waitTime = 2;
        if (!latch.await(waitTime, TimeUnit.SECONDS)) {
            Assert.fail("Did not terminate properly, waited " + waitTime + " seconds");
        }
    }

    @Test
    public void test_DefinitionError_TerminatesSuccessfully() throws IOException, InterruptedException {
        final CountDownLatch latch = setUpTest("<?php "
                + "/**\n"
                + " * adapted from\n"
                + " * http://webdeveloperplus.com/php/21-really-useful-handy-php-code-snippets/\n"
                + " */\n"
                + "function generate_rand($l){\n"
                + "  $c= ['A','B','C','D','E','F','G','H','I','J','K', 'L','M',\n"
                + "    'N','O','P','Q','R','S','T','U','V','W','X','Y','Z',\n"
                + "    'a','b','c','d','e','f','g','h','i','j','k','l','m',\n"
                + "    'n','o','p','q','r','s','t','u','v','w','x','y','z',\n"
                + "    '0','1','2','3','4','5','6','7','8','9'];\n"
                + "  srand((double)microtime()*1000000);\n"
                + "  $rand = '';\n"
                + "  for($i=0; $i<$l; $i++) {\n"
                + "      $rand.= $c[rand()%strlen($c)];\n"
                + "  }\n"
                + "  return $rand;\n"
                + " }" +
                "?>");
        final int waitTime = 2;
        if (!latch.await(waitTime, TimeUnit.SECONDS)) {
            Assert.fail("Did not terminate properly, waited " + waitTime + " seconds");
        }
    }

    private CountDownLatch setUpTest(String phpCode) throws IOException {
        final File file = folder.newFile("test.php");
        PrintWriter writer = new PrintWriter(file);
        writer.write(phpCode);
        writer.close();
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    Main.main(new String[]{
                            file.getAbsolutePath(), file.getParentFile().getAbsolutePath() + "\\tmp.tsphp"});
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Assert.fail("Exception occurred");
                } catch (RuntimeException ex) {
                    //that's fine
                }
                latch.countDown();
            }
        }).start();
        return latch;
    }

}
