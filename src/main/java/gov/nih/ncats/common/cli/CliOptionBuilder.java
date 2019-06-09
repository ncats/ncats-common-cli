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

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Public interface that all Option Builder implementations
 * implement.
 *
 * Created by katzelda on 6/4/19.
 */
public interface CliOptionBuilder {
    /**
     * Is this Option required.  If not called,
     * the default is not required.
     * @param isRequired {@code true} if this option is required; {@code false} otherwise.
     * @return this.
     */
    CliOptionBuilder setRequired(boolean isRequired);
    /**
     * Add an additional validation rule to this option Builder.
     * @param validationRule A {@link Predicate} function that passes in the
     *                       parsed {@link Cli} instance from a program invocation
     *                       that will return {@code true} if this Cli passes this new validation
     *                       rule; {@code false} otherwise.  This Predicate can not be null.
     * @param errorMessage The message to use in the new {@link CliValidationException}
     *                     that will be thrown if the given predicate returns {@code false}.
     *
     * @return this.
     * @throws NullPointerException if validationRule is null.
     */
    CliOptionBuilder addValidation(Predicate<Cli> validationRule, String errorMessage);
    /**
     * Add an additional validation rule to this option Builder.
     * @param validationRule A {@link Predicate} function that passes in the
     *                       parsed {@link Cli} instance from a program invocation
     *                       that will return {@code true} if this Cli passes this new validation
     *                       rule; {@code false} otherwise.  This Predicate can not be null.
     * @param errorMessageFunction A function to generate the message to use in the new {@link CliValidationException}
     *                     that will be thrown if the given predicate returns {@code false}.
     * @return this.
     * @throws NullPointerException if either parameter is null.
     */
    CliOptionBuilder addValidation(Predicate<Cli> validationRule, Function<Cli,String> errorMessageFunction);
}
