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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 6/1/19.
 */
class GroupedOption implements InternalCliOptionBuilder{

    private final InternalCliOptionBuilder[] choices;

    private boolean isRequired;

    private final List<CliValidator> validators = new ArrayList<>();

    public GroupedOption(CliOptionBuilder[] choices) {
        if(choices ==null || choices.length<1){
            throw new IllegalStateException("group option requires at least 1 choices");
        }
        this.choices = new InternalCliOptionBuilder[choices.length];
        for(int i=0; i< choices.length; i++){
            this.choices[i] = (InternalCliOptionBuilder)choices[i];
        }
    }

    @Override
    public InternalCliOptionBuilder addValidation(Predicate<Cli> validationRule, String errorMessage) {
        validators.add(new CliValidator(validationRule, errorMessage));
        return this;
    }

    @Override
    public InternalCliOptionBuilder addValidation(Predicate<Cli> validationRule, Function<Cli, String> errorMessageFunction) {
        validators.add(new CliValidator(validationRule, errorMessageFunction));
        return this;
    }

    @Override
    public InternalCliOption build() {
        return new GroupedOptionCliOption(isRequired,
                Arrays.stream(choices).map(InternalCliOptionBuilder::build).toArray(i-> new InternalCliOption[i]),
                validators);
    }

    @Override
    public InternalCliOption build(boolean isRequired) {
        return new GroupedOptionCliOption(isRequired,
                Arrays.stream(choices).map(InternalCliOptionBuilder::build).toArray(i-> new InternalCliOption[i]),
                validators);

    }

    @Override
    public GroupedOption setRequired(boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    private static class GroupedOptionCliOption implements InternalCliOption{

        private final InternalCliOption[] choices;
        private final boolean isRequired;

        private List<InternalCliOption> requiredOptions = new ArrayList<>();
        private List<InternalCliOption> optionalOptions = new ArrayList<>();

        private final List<CliValidator> validators;
        public GroupedOptionCliOption(boolean isRequired, InternalCliOption[] choices,
                                      List<CliValidator> validators) {
            this.choices = choices;
            this.isRequired = isRequired;

            this.validators = validators;
            for(InternalCliOption choice : choices){
                if(choice.isRequired()){
                    requiredOptions.add(choice);
                }else{
                    optionalOptions.add(choice);
                }
            }
        }

        @Override
        public void addValidator(CliValidator validator) {
            validators.add(validator);
        }

        @Override
        public Optional<String> getMissing(Cli cli) {
            List<String> missingOps = new ArrayList<>();
            for(InternalCliOption choice : requiredOptions){
                choice.getMissing(cli).ifPresent(missingOps::add);
            }
            if(missingOps.isEmpty()){
                return Optional.empty();
            }

            return Optional.of(missingOps.stream().collect(Collectors.joining(" ", "( ", " )")));
        }

        @Override
        public void addTo(InternalCliSpecification spec, Boolean forceIsRequired) {
            boolean force=isRequired;
            if(forceIsRequired !=null){
                force = forceIsRequired;
            }
            for(InternalCliOption choice : choices){

                choice.addTo(spec, choice.isRequired() && force);
            }
        }

        @Override
        public boolean isPresent(Cli cli) {
            int seen=0;
            for(InternalCliOption choice : requiredOptions){
                if(choice.isPresent(cli)){
                    seen++;
                }else{
                    return false;
                }
            }
            if(!requiredOptions.isEmpty()) {
                return seen == requiredOptions.size();
            }
            for(InternalCliOption choice : optionalOptions){
                if(choice.isPresent(cli)){
                    return true;
                }
            }
            return false;

        }

        @Override
        public Optional<String> generateUsage(boolean force) {
            if(!force && !isRequired){
                return Optional.empty();
            }
            List<String> list = new ArrayList<>(requiredOptions.size());
            for(InternalCliOption choice : requiredOptions){
                choice.generateUsage(true).ifPresent(list::add);
            }
            String requiredGroup =list.stream().collect(Collectors.joining(" , "));

            List<String> optList = new ArrayList<>(optionalOptions.size());
            for(InternalCliOption choice : optionalOptions){
                choice.generateUsage(true).ifPresent(c-> optList.add("[ " +c + " ]"));
            }

            String optionalGroup =optList.stream().collect(Collectors.joining(" , "));

            if(requiredGroup.isEmpty() && optionalGroup.isEmpty()){
                return Optional.empty();
            }
            if(requiredGroup.isEmpty()){
                //just show optionals
                return Optional.of("(" + optionalGroup + " )");
            }
            if(optionalGroup.isEmpty()){
                return Optional.of("(" + requiredGroup + " )");
            }
            return Optional.of("(" + requiredGroup + " " + optionalGroup + " )");
        }

        @Override
        public boolean isRequired() {
            return isRequired;
        }

        @Override
        public void validate(Cli cli) throws CliValidationException {
            List<String> missing = new ArrayList<>();
            for(InternalCliOption choice : requiredOptions){

                choice.getMissing(cli).ifPresent( missing::add);

            }
            if(!missing.isEmpty() && isRequired){
                throw new CliValidationException("required group was not found require " +
                        missing.stream().collect(Collectors.joining(",", "( ", " )")));
            }

            for(InternalCliOption choice : requiredOptions){
                choice.validate(cli);
            }
            for(InternalCliOption choice : optionalOptions){
                choice.validate(cli);
            }
            for(CliValidator v : validators){
                v.validate(cli);
            }
        }


        @Override
        public List<String> getSeenList(Cli cli) {
            List<String> list = new ArrayList<>();
            for(InternalCliOption choice : choices){
                List<String> seen =choice.getSeenList(cli);
                if(!seen.isEmpty()){
                    list.add(seen.stream().collect(Collectors.joining(",", "(",")")));
                }
            }
            return list;
        }
        @Override
        public void fireConsumerIfNeeded(Cli cli) throws CliValidationException {
            for(InternalCliOption choice : choices){
                choice.fireConsumerIfNeeded(cli);
            }
        }
    }
}
