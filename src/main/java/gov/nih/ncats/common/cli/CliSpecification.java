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

/**
 * Specification describing the options for a given program.
 * This specification is what is used to validate and parse
 * program options.
 *
 * Created by katzelda on 6/4/19.
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
        helpWithOptions[options.length] = option("h")
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

    public static BasicCliOption option(String argName){
        return new BasicCliOption(argName);
    }

    public static CliOptionBuilder radio(CliOptionBuilder... radioOptions){
        return new RadioCliOption(radioOptions);
    }

    public static CliOptionBuilder group(CliOptionBuilder... options){
        return new GroupedOption(options);
    }
    private final Options options;
    private   InternalCliOption internalCliOption;

    private String programName;
    private String description;

    public CliSpecification description(String description){
        this.description = description;
        return this;
    }
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

    public boolean hasHelp(){
        return options.hasOption("h");
    }
    public void parse(URL url) throws IOException {
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

        parse(args.toArray(new String[args.size()]));
    }

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
    public boolean helpRequested(String[] args){
        if(!hasHelp()){
            return false;
        }
        for(int i=0; i< args.length; i++){
            String v = args[i];
            if("-h".equals(v) || "--help".equals(v) ||"--h".equals(v) || "-help".equals(v)){
                return true;
            }
        }
        return false;

    }
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
