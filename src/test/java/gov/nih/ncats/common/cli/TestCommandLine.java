/*******************************************************************************
 * NCATS-COMMON-CLI
 *
 * Copyright 2019 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package gov.nih.ncats.common.cli;

import static gov.nih.ncats.common.cli.CliSpecification.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 6/22/17.
 */
public class TestCommandLine {

    @Test
    public void singleRequiredOption() throws IOException {
        Cli cli = CliSpecification.create(option("foo").setRequired(true))
                .parse(toArgList("-foo bar"));

        assertTrue(cli.hasOption("foo"));
        assertEquals("bar", cli.getOptionValue("foo"));
    }
    @Test
    public void singleRequiredFlag() throws IOException {
        Cli cli = CliSpecification.create(option("foo").setRequired(true).isFlag(true))
                .parse(toArgList("-foo bar"));

        assertTrue(cli.hasOption("foo"));
    }



    @Test
    public void withSetter() throws IOException{
        Example ex = new Example();

        Cli cli = CliSpecification.create(option("foo").setRequired(true).setter(ex::setFoo))
                .parse(toArgList("-foo bar"));

        assertEquals("bar", ex.getFoo());
    }

    @Test
    public void withFileSetter() throws IOException{
        Example ex = new Example();
        Cli cli = CliSpecification.create(option("path").setRequired(true)
                                    .setToFile(ex::setMyFile))

                .parse(toArgList("-path /usr/local/foo/bar/baz.txt"));

        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test
    public void multipleOptions() throws IOException{
        Example ex = new Example();
        Cli cli = CliSpecification.create(option("path")
                                    .setToFile(ex::setMyFile),

                            option("a")
                        )

                .parse(toArgList("-path /usr/local/foo/bar/baz.txt"));

        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());

        assertTrue(cli.hasOption("path"));
        assertEquals("/usr/local/foo/bar/baz.txt", cli.getOptionValue("path"));
    }

    @Test
    public void asUrl() throws IOException{
        Example ex = new Example();

        CliSpecification.create(option("path")
                                        .setToFile(ex::setMyFile),
                                option("a").setToInt(ex::setA))
                .parse(new URL("http://example.com?path=/usr/local/foo/bar/baz.txt&a=2"));

        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
        assertEquals(2, ex.getA());

    }

    @Test
    public void optionalRadioGroup() throws IOException{

        CliSpecification.create(radio(
                option("foo"),
                option("bar")

        ))

                .parse(toArgList("-foo x"));
    }

    @Test(expected = CliValidationException.class)
    public void optionalRadioGroupMultipleShouldThrowException() throws IOException{

        CliSpecification.create(radio(
                option("foo"),
                option("bar")

        ).setRequired(true))

                .parse(new String[]{"-foo", "x", "-bar","y"});
    }
    @Test
    public void radioWithOtherOptions() throws IOException{
        Example ex = new Example();
        CliSpecification.create(radio(
                        option("foo"),
                        option("bar")

                        ),
                        option("path")
                                        .setToFile(ex::setMyFile)


                                )

                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt"});

        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test
    public void groupSomeRequiredSomeNot() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),
                                        option("bar"))
                                .setRequired(true),
                                option("path").setToFile(ex::setMyFile)
        )

                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test
    public void groupAllRequired() throws IOException{
        Example ex = new Example();
        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),
                option("bar").setRequired(true))
                        .setRequired(true),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )

                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123", "-bar", "lah"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test(expected = CliValidationException.class)
    public void groupAllRequiredButNotAllInCommandLine() throws IOException{
        Example ex = new Example();
        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),
                option("bar").setRequired(true))
                        .setRequired(true),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )

                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123"});

    }

    @Test
    public void nestedGroupsRadioGroupInsideGenericGroup() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),

                radio(option("bar"), option("baz"))
                        .setRequired(true)
                ).setRequired(true),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )


                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123", "-bar", "stool"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test
    public void radioGroupOptionsSetToRequiredShouldBeNoOp() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),

                radio(option("bar").setRequired(true), option("baz").setRequired(true))
                        .setRequired(true)
                ).setRequired(true),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )


                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123", "-bar", "stool"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test
    public void nestedGroupsOptionalRadioGroupInsideGenericGroup() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),

                radio(option("bar"), option("baz"))
                        .setRequired(true)
                ),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )



                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123", "-bar", "stool"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }

    @Test(expected = CliValidationException.class)
    public void nestedGroupsOptionalRadioGroupInsideGenericGroupRequiredAndNotSelected() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),

                                radio(option("bar"), option("baz"))
                                        .setRequired(true)
                                ).setRequired(true),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )



                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123"});

    }
    @Test
    public void nestedGroupsOptionalRadioGroupInsideGenericGroupNotSelected() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),

                radio(option("bar"), option("baz"))

                ),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )



                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }
    @Test(expected = CliValidationException.class)
    public void nestedGroupsRequiredRadioGroupInsideGenericGroupNotSelected() throws IOException{
        Example ex = new Example();

        CliSpecification.create( group(option("foo").setToInt(ex::setA).setRequired(true),

                radio(option("bar"), option("baz"))
                .setRequired(true)
                ),
                option("path").setToFile(ex::setMyFile).setRequired(true)
        )



                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123"});

        assertEquals(123, ex.getA());
        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }


    @Test
    public void requireRadioWithOtherOptions() throws IOException{
        Example ex = new Example();

        CliSpecification.create( radio(option("foo"), option("bar")),
                                option("path").setToFile(ex::setMyFile))

                .parse(new String[]{"-foo", "x", "-path","/usr/local/foo/bar/baz.txt"});

        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }



    @Test(expected = CliValidationException.class)
    public void nestedGroupsGenericGroupInsideRadioMultiSelectFails() throws IOException{
        Example ex = new Example();
        CliSpecification.create( radio( option("bar"), option("baz"),
                                        group(option("foo").setRequired(true).setToInt(ex::setA),
                                                option("anotherFoo")))
                                        .setRequired(true),
                                option("path").setRequired(true).setToFile(ex::setMyFile)
                )

                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-foo", "123", "-bar", "stool"});

    }

    @Test
    public void nestedGroupsGenericGroupInsideRadioNotSelected() throws IOException{
        Example ex = new Example();
        CliSpecification.create( radio( option("bar"), option("baz"),
                group(option("foo").setRequired(true).setToInt(ex::setA),
                        option("anotherFoo"))),
                option("path").setRequired(true).setToFile(ex::setMyFile)
        )

                .parse(new String[]{"-path","/usr/local/foo/bar/baz.txt", "-bar", "stool"});

        assertEquals("/usr/local/foo/bar/baz.txt", ex.getMyFile().getAbsolutePath());
    }


    private static String[] toArgList(String s){
        return s.split(" ");
    }

    private static class Example{
        public String foo;

        public File myFile;

        public int a;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public File getMyFile() {
            return myFile;
        }

        public void setMyFile(File myFile) {
            this.myFile = myFile;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }
    }
}
