package gov.nih.ncats.common.cli;

import java.io.File;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import gov.nih.ncats.common.functions.ThrowableConsumer;
import gov.nih.ncats.common.functions.ThrowableFunction;
import gov.nih.ncats.common.functions.ThrowableIntConsumer;

public class TrailerBuilder {

	private String name;
	private String description;
	
	private ThrowableConsumer<String, CliValidationException> consumer = (s) ->{}; //no -op
	
	public Trailer build() {
		return new Trailer(this.name, this.description, this.consumer);
		
	}
	    public <T extends Throwable> TrailerBuilder setter(ThrowableConsumer<String, T> consumer){
	        return setter(ThrowableFunction.identity(), consumer,null);
	    }

	    public <T extends Throwable> TrailerBuilder setToFile(ThrowableConsumer<File, T> consumer) {
	        Objects.requireNonNull(consumer);
	        this.consumer = s -> {
	            try{
	                consumer.accept(new File(s));
	            }catch(Throwable t){
	                if( t instanceof CliValidationException){
	                    throw (CliValidationException)t;
	                }
	                throw new CliValidationException(t.getMessage(), t);
	            }
	        };
	        return this;
	    }

	    public <T extends Throwable> TrailerBuilder setToInt(ThrowableIntConsumer<T> consumer, IntPredicate validator) {
	        if(validator == null){
	            return setToInt(consumer);
	        }

	        this.consumer = s->{
	            int value;
	            try {
	                value = Integer.parseInt(s);
	            }catch(Throwable t){
	                throw new CliValidationException("error parsing int value", t);
	            }
	            if(validator.test(value)){
	                try {
	                    consumer.accept(value);
	                }catch(Throwable t){
	                    if( t instanceof CliValidationException){
	                        throw (CliValidationException)t;
	                    }
	                    throw new CliValidationException(t.getMessage(), t);
	                }
	            }else{
	                throw new CliValidationException("setter did not pass validation test");
	            }
	        };
	        return this;
	    }

	    public <T extends Throwable, R> TrailerBuilder setter(ThrowableFunction<String, R, T> typeConverter,
	                                                          ThrowableConsumer<R, T> consumer, Predicate<R> validator){
	        if(validator ==null){
	            this.consumer=s-> {
	                try {
	                    consumer.accept(typeConverter.apply(s));
	                } catch (Throwable t) {
	                    if( t instanceof CliValidationException){
	                        throw (CliValidationException)t;
	                    }
	                    throw new CliValidationException(t.getMessage(), t);
	                }
	            };
	        }else{

	            this.consumer = s->{
	                R value;
	                try {
	                    value = typeConverter.apply(s);
	                } catch (Throwable t) {
	                    if( t instanceof CliValidationException){
	                        throw (CliValidationException)t;
	                    }
	                   throw new CliValidationException(t);
	                }
	                if(validator.test(value)){
	                    try {
	                        consumer.accept(value);
	                    }catch(Throwable t){
	                        if( t instanceof CliValidationException){
	                            throw (CliValidationException)t;
	                        }
	                        throw new CliValidationException(t.getMessage(), t);
	                    }
	                }else{
	                    throw new CliValidationException("setter did not pass validation test");
	                }
	            };
	        }
	        return this;
	    }

	    public <T extends Throwable> TrailerBuilder setter(ThrowableConsumer<String, T> consumer, Predicate<String> validator){
	        return setter(ThrowableFunction.identity(), consumer, validator);
	    }
	    
	    public <T extends Throwable> TrailerBuilder setToInt(ThrowableIntConsumer<T> consumer){
	        Objects.requireNonNull(consumer);
	        this.consumer = s ->{
	            try {
	                consumer.accept(Integer.parseInt(s));
	            }catch(Throwable t){
	                throw new CliValidationException(t);
	            }
	        };
	        return this;
	    }
}
