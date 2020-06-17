/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.model.permissions;

/**
 * @author Florent CHAMFROY (forent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public enum OrganizationPermission implements Permission {
    USER("USER", 1000),
    ENVIRONMENT("ENVIRONMENT", 1100),
    ROLE("ROLE", 1200),
    IDENTITY_PROVIDER("IDENTITY_PROVIDER", 1300),
    IDENTITY_PROVIDER_ACTIVATION("IDENTITY_PROVIDER_ACTIVATION", 1400);

    String name;
    int mask;

    OrganizationPermission(String name, int mask) {
        this.name = name;
        this.mask = mask;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMask() {
        return mask;
    }

}
