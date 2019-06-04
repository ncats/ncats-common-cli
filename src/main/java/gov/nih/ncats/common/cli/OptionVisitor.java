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

import org.apache.commons.cli.*;

import java.util.function.Consumer;

/**
 * Created by katzelda on 6/21/17.
 */
interface OptionVisitor {

    void visit(org.apache.commons.cli.Option apacheOption, Consumer<String> consumer);

    void visit(Option option);

    void preVisit(RadioGroup group);

    void postVisit(RadioGroup group);

    void preVisit(GroupedOptionGroup group);

    void postVisit(GroupedOptionGroup group);
}
