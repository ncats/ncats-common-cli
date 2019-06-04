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

package gov.nih.ncats.common.cli2;

import gov.nih.ncats.common.cli.ValidationError;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 6/1/19.
 */
public class RadioCliOption implements InternalCliOptionBuilder{

    private final InternalCliOptionBuilder[] choices;

    private boolean isRequired;

    public RadioCliOption(InternalCliOptionBuilder[] choices) {

        if(choices ==null || choices.length<2){
            throw new IllegalStateException("Radio option requires at least 2 choices");
        }
        this.choices = choices;
    }

    @Override
    public InternalCliOptionBuilder setRequired(boolean isRequired) {
         this.isRequired = isRequired;
         return this;
    }

    @Override
    public InternalCliOption build() {
        return new RadioInternalCliOption(isRequired, Arrays.stream(choices).map(InternalCliOptionBuilder::build).toArray(i-> new InternalCliOption[i]));
    }

    @Override
    public InternalCliOption build(boolean isRequired) {
        return new RadioInternalCliOption(isRequired, Arrays.stream(choices).map(InternalCliOptionBuilder::build).toArray(i-> new InternalCliOption[i]));

    }

    private static class RadioInternalCliOption implements InternalCliOption{

        private final InternalCliOption[] choices;
        private final boolean isRequired;


        public RadioInternalCliOption(boolean isRequired, InternalCliOption[] choices) {
            this.choices = choices;
            this.isRequired = isRequired;

            System.out.println("radio choices = " + Arrays.toString(choices));
        }

        @Override
        public void addTo(InternalCliSpecification spec, Boolean forceIsRequired) {
            for(InternalCliOption choice : choices){
                choice.addTo(spec, false);
            }
        }

        @Override
        public Optional<String> getMissing(Cli cli) {
            if(isPresent(cli)){
                return Optional.empty();
            }
            List<String> missing = new ArrayList<>();
            for(InternalCliOption choice : choices){
                choice.getMissing(cli).ifPresent(missing::add);
            }
            return Optional.ofNullable(missing.stream().collect(Collectors.joining(" | ", "[ ", " ]")));
        }

        @Override
        public boolean isRequired() {
            return isRequired;
        }

        @Override
        public boolean isPresent(Cli cli) {
            for(InternalCliOption choice : choices){
                if(choice.isPresent(cli)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public void validate(Cli cli) throws ValidationError {

            System.out.println("in validate radio");
            List<String> seen = getSeenList(cli);
            if(seen.size() > 1){
                throw new ValidationError("Radio option must only select at most 1 choice but found " + seen);
            }
            if(isRequired && seen.size() ==0){
                throw new ValidationError("Radio option was required but did not find selected option choice");
            }
            //only 1 can be present
        }
        @Override
        public List<String> getSeenList(Cli cli) {
            List<String> list = new ArrayList<>();
            for(InternalCliOption choice : choices){
                List<String> seen =choice.getSeenList(cli);

                System.out.println("seen list = " + seen);
                if(!seen.isEmpty()){
                    list.add(seen.stream().collect(Collectors.joining(",", "(",")")));
                }
            }
            return list;
        }
        @Override
        public void fireConsumerIfNeeded(Cli cli) throws ValidationError {
            for(InternalCliOption choice : choices){
                choice.fireConsumerIfNeeded(cli);
            }
        }
    }
}
