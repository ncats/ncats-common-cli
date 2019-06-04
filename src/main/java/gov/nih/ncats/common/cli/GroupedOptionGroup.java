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
 * Created by katzelda on 5/28/19.
 */
public class GroupedOptionGroup implements OptionGroup{
    private boolean isRequired;

    private final List<Option> options;

    private final List<OptionGroup> optionGroups;

    public static GroupedOptionGroup requiredGroup(CliOption ... options){
        return new GroupedOptionGroup(true, Arrays.asList(options));
    }

    public static GroupedOptionGroup optionalGroup(CliOption... options){
        return new GroupedOptionGroup(false, Arrays.asList(options));
    }

    private GroupedOptionGroup(boolean isRequired, List<CliOption> options) {
        this.isRequired = isRequired;
        this.options = new ArrayList<>(options.size());
        this.optionGroups = new ArrayList<>(options.size());
        for(CliOption o : options){
            Objects.requireNonNull(o);
            if(o instanceof OptionGroup){
                optionGroups.add((OptionGroup) o);
            }else{
                this.options.add( (Option) o);
            }
        }
    }

    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public void visit(OptionVisitor visitor, Boolean forcedOverride) {
        visitor.preVisit(this);
        for(Option o : options){
            org.apache.commons.cli.Option apacheOption = o.asApacheOption();
            if(!isRequired || Boolean.FALSE.equals(forcedOverride)) {
                apacheOption.setRequired(false);
            }
            visitor.visit(apacheOption, o.getConsumer());
        }
        for(OptionGroup og: optionGroups){
            og.visit(visitor, forcedOverride);
        }
        visitor.postVisit(this);
    }

    @Override
    public void setIsRequired(boolean b) {
        this.isRequired = b;
    }
}
