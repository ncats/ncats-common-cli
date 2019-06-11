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

import java.util.Objects;

/**
 * Created by katzelda on 6/11/19.
 */
final class UsageExample {
    private final String usage;
    private final String description;

    public UsageExample(String usage, String description) {
        this.usage = Objects.requireNonNull(usage, "usage string can not be null");
        this.description = Objects.requireNonNull(description, "description string can not be null");
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsageExample)) return false;

        UsageExample that = (UsageExample) o;

        return usage.equals(that.usage);
    }

    @Override
    public int hashCode() {
        return usage.hashCode();
    }
}

