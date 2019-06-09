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

/**
 * A Parsed command line object that
 * lets the user programmatically see what
 * options were set and what those option values are.
 */
public class Cli {

    private final org.apache.commons.cli.CommandLine delegate;

    Cli(org.apache.commons.cli.CommandLine cmd){
        this.delegate = cmd;
    }

    /**
     * Does this {@link Cli} object have the given
     * option name.
     * @param optName the short option name to look for.
     * @return {@code true} if this command line has the given option; {@code flase} otherwise.
     */
    public boolean hasOption(String optName) {
        return delegate.hasOption(optName);
    }

    public String getOptionValue(String optName){
        return delegate.getOptionValue(optName);
    }

    public boolean helpRequested(){
        return delegate.hasOption("h") || delegate.hasOption("help");
    }
}
