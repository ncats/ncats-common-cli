package gov.nih.ncats.common.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


class AtLeastOneOfOption implements InternalCliOptionBuilder{
	private final InternalCliOptionBuilder[] choices;

    private boolean isRequired;
    private final List<CliValidator> validators = new ArrayList<>();
    

	AtLeastOneOfOption(CliOptionBuilder[] choices) {

        if(choices ==null || choices.length<2){
            throw new IllegalStateException("At Least One Of option requires at least 2 choices");
        }
        this.choices = new InternalCliOptionBuilder[choices.length];
        for(int i=0; i< choices.length; i++){
            this.choices[i] = (InternalCliOptionBuilder)choices[i];
        }
	}

	@Override
	public CliOptionBuilder addValidation(Predicate<Cli> validationRule, String errorMessage) {
		 validators.add(new CliValidator(validationRule, errorMessage));
	     return this;
	}

	@Override
	public CliOptionBuilder addValidation(Predicate<Cli> validationRule, Function<Cli, String> errorMessageFunction) {
		 validators.add(new CliValidator(validationRule, errorMessageFunction));
	     return this;
	}

	@Override
	public InternalCliOptionBuilder setRequired(boolean isRequired) {
		this.isRequired = isRequired;
		return this;
	}

	 @Override
	    public InternalCliOption build() {
	        return new AtLeastOneOfInternalCliOption(isRequired,
	                Arrays.stream(choices).map(InternalCliOptionBuilder::build).toArray(i-> new InternalCliOption[i]),
	                validators);
	    }

	    @Override
	    public InternalCliOption build(boolean isRequired) {
	        return new AtLeastOneOfInternalCliOption(isRequired,
	                Arrays.stream(choices).map(InternalCliOptionBuilder::build).toArray(i-> new InternalCliOption[i]),
	                validators);

	    }
	
	private static class AtLeastOneOfInternalCliOption implements InternalCliOption{

        private final InternalCliOption[] choices;
        private final boolean isRequired;

        private final List<CliValidator> validators;

        public AtLeastOneOfInternalCliOption(boolean isRequired, InternalCliOption[] choices,
                                      List<CliValidator> validators) {
            this.choices = choices;
            this.isRequired = isRequired;
            this.validators = validators;

        }

        @Override
        public void addValidator(CliValidator validator) {
            validators.add(validator);
        }
        @Override
        public Optional<String> generateUsage(boolean force) {
            if(!force && !isRequired){
                return Optional.empty();
            }
            List<String> list = new ArrayList<>(choices.length);
            for(InternalCliOption choice : choices){
               choice.generateUsage(true).ifPresent(list::add);
            }
            if(list.isEmpty()){
                return Optional.empty();
            }
            return Optional.of(list.stream().collect(Collectors.joining(" | ", "[", "]")));
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
        public void validate(Cli cli) throws CliValidationException {
            List<String> seen = getSeenList(cli);
           
            if(isRequired && seen.size() ==0){
                throw new CliValidationException("AtLeastOneOf option was required but did not find selected option choice");
            }
            for(InternalCliOption choice : choices){
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
