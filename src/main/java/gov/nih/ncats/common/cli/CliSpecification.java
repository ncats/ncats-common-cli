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

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Specification describing the options for a given program.
 * This specification is what is used to validate and parse
 * program options.
 *
 */
public class CliSpecification {
    /**
     * Create a new {@link CliSpecification} with a default help option
     * with the option name "-h" and long name "--help" along with  the given other options.
     * @param options varargs of other options to include in this specification;
     *                may be empty but no item in the list can be null.
     * @return a new {@link CliSpecification} will never be null.
     */
    public static CliSpecification createWithHelp(CliOptionBuilder... options){

        InternalCliOptionBuilder[] helpWithOptions = new InternalCliOptionBuilder[options.length +1];
        System.arraycopy(options,0,helpWithOptions,0, options.length);
        helpWithOptions[options.length] = (InternalCliOptionBuilder) option("h")
                                                .longName("help")
                                                .description("print helptext")
                                                .isFlag(true);

        return new CliSpecification(group(helpWithOptions)
                .setRequired(true));
    }
    /**
     * Create a new {@link CliSpecification} with the given options.
     * @param options varargs of options to include in this specification;
     *                may be empty but no item in the list can be null.
     * @return a new {@link CliSpecification} will never be null.
     */
    public static CliSpecification create(CliOptionBuilder... options){
         return new CliSpecification(group(options)
                .setRequired(true));
    }

    /**
     * Create a new {@link BasicCliOptionBuilder} that
     * is mapped to the given option name. By default this radio group is not required,
     * to make it required in the specification set the {@link CliOptionBuilder#setRequired(boolean)}
     * method.
     * @param optName the name of the option that will be
     *                in the specification as "-$optName".
     * @return a new {@link BasicCliOptionBuilder} will never be null.
     */
    public static BasicCliOptionBuilder option(String optName){
        return new BasicCliOption(optName);
    }

    /**
     * Create a new Radio group of options where the specification
     * is only valid if only one of the given options
     * can be selected. Nesting other groups as one of the radio options is allowed.
     * By default this radio group is not required,
     * to make it required in the specification set the {@link CliOptionBuilder#setRequired(boolean)}
     * method.
     * @param radioOptions a varargs list of radio options;
     *                     none of the options may be null.
     * @return a new {@link CliOptionBuilder}.
     */
    public static CliOptionBuilder radio(CliOptionBuilder... radioOptions){
        return new RadioCliOption(radioOptions);
    }
    /**
     * Create a new group of options where the specification is
     * only valid if either all the required options in the group are present,
     * or none of them are present.  If this group is required, then this specification
     * is only valid if the all the required options in this group are present.
     * Nesting other groups as one of this group's options is allowed.
     * By default this group is not required,
     * to make it required in the specification set the {@link CliOptionBuilder#setRequired(boolean)}
     * method.
     * @param options a varargs list of options in this group;
     *                     none of the options may be null.
     * @return a new {@link CliOptionBuilder}.
     */
    public static CliOptionBuilder group(CliOptionBuilder... options){
        return new GroupedOption(options);
    }
    private final Options options;
    private   InternalCliOption internalCliOption;

    private String programName;
    private String description;

    /**
     * Add an additional validation rule to this overall specification.
     * @param validationRule A {@link Predicate} function that passes in the
     *                       parsed {@link Cli} instance from a program invocation
     *                       that will return {@code true} if this Cli passes this new validation
     *                       rule; {@code false} otherwise.  This Predicate can not be null.
     * @param errorMessage The message to use in the new {@link CliValidationException}
     *                     that will be thrown if the given predicate returns {@code false}.
     *
     * @return this.
     * @throws NullPointerException if validationRule is null.
     */
    public CliSpecification addValidation(Predicate<Cli> validationRule, String errorMessage) {
        internalCliOption.addValidator(new CliValidator(validationRule, errorMessage));
        return this;
    }
    /**
     * Add an additional validation rule to this overall specification.
     * @param validationRule A {@link Predicate} function that passes in the
     *                       parsed {@link Cli} instance from a program invocation
     *                       that will return {@code true} if this Cli passes this new validation
     *                       rule; {@code false} otherwise.  This Predicate can not be null.
     * @param errorMessageFunction A function to generate the message to use in the new {@link CliValidationException}
     *                     that will be thrown if the given predicate returns {@code false}.
     * @return this.
     * @throws NullPointerException if either parameter is null.
     */
    public CliSpecification addValidation(Predicate<Cli> validationRule, Function<Cli, String> errorMessageFunction) {
        internalCliOption.addValidator(new CliValidator(validationRule, errorMessageFunction));
        return this;
    }

    /**
     * Sets a description to this usage to describe what this program
     * does.
     * @param description the description of this program,
     *                   if {@code null}, then there is no description.
     * @return this.
     */
    public CliSpecification description(String description){
        this.description = description;
        return this;
    }
    /**
     * Sets this program name to this usage.
     * @param programName the name of this program,
     *                   if {@code null}, then there is no name.
     * @return this.
     */
    public CliSpecification programName(String programName){
        this.programName = programName;
        return this;
    }

    private CliSpecification(CliOptionBuilder options ){
        InternalCliSpecification internalSpec = new InternalCliSpecification();

        internalCliOption = ((InternalCliOptionBuilder) options).build();
        internalCliOption.addTo(internalSpec, null);

        this.options = internalSpec.getInternalOptions();

    }


    /**
     * Parse the query parameters as a urlencoded command line arguments.
     * It is a assumed that {@code key=value} means {@code -key value}.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public Cli parse(URL url) throws IOException {
        List<String> args = new ArrayList<>();

        String[] split = url.getQuery().split("&");
        if(split !=null){
            for(String s : split){
                int index = s.indexOf('=');
                if(index >0){
                    args.add("-"+ URLDecoder.decode( s.substring(0, index), "UTF-8"));
                    args.add(URLDecoder.decode( s.substring(index+1, s.length()), "UTF-8"));
                }else{
                    args.add("-"+ URLDecoder.decode( s, "UTF-8"));
                }
            }
        }

        return parse(args.toArray(new String[args.size()]));
    }

    /**
     * Generate the Usage String of this specification.
     * @return a new String will never be null.
     */
    public String generateUsage(){

        StringBuilder builder = new StringBuilder();
        if(programName !=null){
            builder.append(programName).append(" ");
        }
        internalCliOption.generateUsage(false).ifPresent(builder::append);
        String programLine= builder.toString();

        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        try(PrintWriter writer = new PrintWriter(sw)) {
            formatter.printHelp(writer, formatter.getWidth(), programLine, description, options,
                    formatter.getLeftPadding(), formatter.getDescPadding(), "");
        }
        return sw.toString();
    }

    /**
     * Is one of these passed in arguments -h, --h, -help or --help.
     * @param args the command line arguments to parse.
     * @return {@code true} if at least one of these arguments
     * is "-h", "--h", "-help" or "--help"; {@code false} otherwise
     */
    public boolean helpRequested(String[] args){
        for(int i=0; i< args.length; i++){
            String v = args[i];
            if("-h".equals(v) || "--help".equals(v) ||"--h".equals(v) || "-help".equals(v)){
                return true;
            }
        }
        return false;

    }

    /**
     * Parse the command line options of the given String array, often the arguments from a Main method.
     * @param args the arguments array to parse.
     * @return a new {@link Cli} of the parsed options.
     * @throws CliValidationException if the arguments violate this {@link CliSpecification}.
     */
    public Cli parse(String[] args) throws CliValidationException {

        CommandLineParser parser = new DefaultParser();

        Cli cli;
        try {
            org.apache.commons.cli.CommandLine cmdline = parser.parse(options, args);
            cli = new Cli(cmdline);
        } catch (ParseException e) {
            throw new CliValidationException(e);
        }

        internalCliOption.validate(cli);

        internalCliOption.fireConsumerIfNeeded(cli);
        return cli;

    }
}
