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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 6/21/17.
 */
public class RadioGroup implements OptionGroup {

    private boolean isRequired;

    private final List<Option> options;

    private final List<OptionGroup> optionGroups;

    public static RadioGroup required(CliOption...options){
        return new RadioGroup(true, Arrays.asList(options));
    }
    public static RadioGroup optional(CliOption...options){
        return new RadioGroup(false, Arrays.asList(options));
    }

    private RadioGroup(boolean isRequired, List<CliOption> options) {
        this.isRequired = isRequired;
        this.options = new ArrayList<>(options.size());
        this.optionGroups = new ArrayList<>(options.size());
        for(CliOption o : options){
            Objects.requireNonNull(o);
            if(o instanceof OptionGroup){

                OptionGroup o1 = (OptionGroup) o;
                o1.setIsRequired(false);
                optionGroups.add(o1);
            }else{
                this.options.add( (Option) o);
            }
        }
    }

    @Override
    public void setIsRequired(boolean b) {
        this.isRequired = b;
    }

    @Override
    public void visit(OptionVisitor visitor, Boolean forcedOverride) {
        visitor.preVisit(this);
        for(Option o : options){
            org.apache.commons.cli.Option opt = o.asApacheOption();
            //set not required for cli validation
            opt.setRequired(false);
            visitor.visit(opt, o.getConsumer());
        }
        for(OptionGroup og: optionGroups){
//            og.setIsRequired(false);
           og.visit(visitor, false);
        }
        visitor.postVisit(this);
    }

    public boolean isRequired() {
        return isRequired;
    }
}
