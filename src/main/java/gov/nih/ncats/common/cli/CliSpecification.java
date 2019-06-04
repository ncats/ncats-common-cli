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

import gov.nih.ncats.common.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by katzelda on 6/4/19.
 */
public class CliSpecification {

    public static CliSpecification create(InternalCliOptionBuilder... options){
         return new CliSpecification(group(options)
                .setRequired(true));
    }

    public static BasicCliOption option(String argName){
        return new BasicCliOption(argName);
    }

    public static InternalCliOptionBuilder radio(InternalCliOptionBuilder... radioOptions){
        return new RadioCliOption(radioOptions);
    }

    public static InternalCliOptionBuilder group(InternalCliOptionBuilder... options){
        return new GroupedOption(options);
    }
    private final Options options;
    private   InternalCliOption internalCliOption;

    private CliSpecification(InternalCliOptionBuilder options ){
        InternalCliSpecification internalSpec = new InternalCliSpecification();

        internalCliOption = options.build();
        internalCliOption.addTo(internalSpec, null);

        this.options = internalSpec.getInternalOptions();

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
    public Cli parse(String[] args) throws ValidationError{
        CommandLineParser parser = new DefaultParser();

        Cli cli;
        try {
            org.apache.commons.cli.CommandLine cmdline = parser.parse(options, args);
            cli = new Cli(cmdline);
        } catch (ParseException e) {
            throw new ValidationError(e);
        }

        internalCliOption.validate(cli);

        internalCliOption.fireConsumerIfNeeded(cli);
        return cli;

    }
}
