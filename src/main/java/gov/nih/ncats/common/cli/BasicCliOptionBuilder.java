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

import gov.nih.ncats.common.functions.ThrowableConsumer;
import gov.nih.ncats.common.functions.ThrowableFunction;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * Builder that builds a single option.
 */
public interface BasicCliOptionBuilder extends CliOptionBuilder{
    BasicCliOptionBuilder argName(String argName);

    BasicCliOptionBuilder longName(String longName);

    BasicCliOptionBuilder description(String description);

    BasicCliOptionBuilder isFlag(boolean isFlag);

    BasicCliOptionBuilder setter(Consumer<String> consumer);

    <T extends Throwable, R> BasicCliOptionBuilder setter(ThrowableFunction<String, R, T> typeConverter,
                                                   ThrowableConsumer<R, T> consumer, Predicate<R> validator);

    <T extends Throwable> BasicCliOptionBuilder setter(ThrowableConsumer<String, T> consumer, Predicate<String> validator);

    BasicCliOptionBuilder setToFile(Consumer<File> consumer);

    BasicCliOptionBuilder setToInt(IntConsumer consumer);

    @Override
    BasicCliOptionBuilder setRequired(boolean isRequired);
    @Override
    BasicCliOptionBuilder addValidation(Predicate<Cli> validationRule, String errorMessage);

    @Override
    BasicCliOptionBuilder addValidation(Predicate<Cli> validationRule, Function<Cli,String> errorMessageFunction);
}
