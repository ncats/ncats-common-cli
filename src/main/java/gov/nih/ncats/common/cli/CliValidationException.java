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

import java.io.IOException;

/**
 * The passed in commandline options did not meet
 * the validation criteria specificed by the {@link CliSpecification}.
 */
public class CliValidationException extends IOException {
    public CliValidationException(String message) {
        super(message);
    }
    public CliValidationException(Throwable cause) {
        super(cause);
    }

    public CliValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
