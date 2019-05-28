package gov.nih.ncats.common.cli;

/**
 * Created by katzelda on 6/21/17.
 */
public interface CliOption {

    default void visit(OptionVisitor visitor){
        visit(visitor, null);
    }

    void visit(OptionVisitor visitor, Boolean requiredOverride);

    void setIsRequired(boolean b);
}
