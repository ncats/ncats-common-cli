package gov.nih.ncats.common.cli;

import gov.nih.ncats.common.functions.ThrowableConsumer;

public class Trailer {

	private final String name;
	private final String description;
	
	private final ThrowableConsumer<String, CliValidationException> consumer;
	
	Trailer(String name, String description, ThrowableConsumer<String, CliValidationException> consumer) {
		this.name = name;
		this.description = description;
		this.consumer = consumer;
	}

	public void fireConsumerIfNeeded(String arg) throws CliValidationException {
        consumer.accept(arg);
        
    }

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
}
