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

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Created by katzelda on 6/21/17.
 */
public class Option implements CliOption{
    private String argName, description;

    private char shortName;

    private boolean isRequired;

    private Consumer<String> consumer = (s) ->{}; //no -op

    private boolean isFlag;

    public static Option required(String argname){
        return new Option(argname).required(true);
    }
    public static Option optional(String argname){
        return new Option(argname).required(false);
    }


    public Option(String argName) {
        this.argName = Objects.requireNonNull(argName);
    }

    public Option description(String description){
        this.description = Objects.requireNonNull(description);
        return this;
    }
    public Option isFlag(boolean isFlag){
        this.isFlag = isFlag;
        return this;
    }
    public Option required(boolean isRequired){
        this.isRequired = isRequired;
        return this;
    }
    public Option setter(Consumer<String> consumer){
        return setter(consumer, OptionValidator.noOp());
    }
    public Option setter(Consumer<String> consumer, OptionValidator<String> validator){

        OptionValidator<String> nullSafe = validator ==null? OptionValidator.noOp() : validator;

        this.consumer = nullSafe.validateConsumer(consumer);
        return this;
    }

    public Option setToFile(Consumer<File> consumer){
        Objects.requireNonNull(consumer);
        this.consumer = s -> consumer.accept(new File(s));
        return this;
    }
    public Option setToInt(IntConsumer consumer){
        Objects.requireNonNull(consumer);
        this.consumer = s -> consumer.accept(Integer.parseInt(s));
        return this;
    }

    public String getArgName() {
        return argName;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }

    @Override
    public void visit(OptionVisitor visitor, Boolean forcedOverride) {
        visitor.visit(this);
    }

    org.apache.commons.cli.Option asApacheOption(){
        return org.apache.commons.cli.Option.builder(argName)
                                            .required(isRequired)
                                            .longOpt(argName)
                                            .desc(description)
                                            .hasArg(!isFlag)
                                            .build();
    }

    @Override
    public void setIsRequired(boolean b) {
        this.isRequired = b;
    }
}
