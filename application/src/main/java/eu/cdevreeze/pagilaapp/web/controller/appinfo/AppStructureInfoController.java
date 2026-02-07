/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.pagilaapp.web.controller.appinfo;

import com.google.common.base.Preconditions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.module.ModuleDescriptor;
import java.util.Objects;

/**
 * Web MVC controller for outputting the internal application structure in terms of Java Modules as JSON.
 *
 * @author Chris de Vreeze
 */
@RestController
public class AppStructureInfoController {

    @GetMapping(value = "/system/appstructure", produces = MediaType.APPLICATION_JSON_VALUE)
    public ObjectNode retrieveAppStructure() {
        Module module = Objects.requireNonNull(AppStructureInfoController.class.getModule());

        // TODO Obtain injected JsonMapper
        JsonMapper jsonMapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
        return convertModuleToJson(module, jsonMapper);
    }

    private ObjectNode convertModuleToJson(Module module, JsonMapper mapper) {
        ObjectNode moduleJson = mapper.createObjectNode();
        moduleJson.put("moduleName", module.getName()); // Is null for unnamed modules

        if (module.isNamed()) {
            ModuleDescriptor descriptor = Objects.requireNonNull(module.getDescriptor());
            moduleJson.set("moduleDescriptor", convertDescriptorToJson(descriptor, mapper));
        } else {
            Preconditions.checkState(module.getDescriptor() == null);
            moduleJson.putNull("moduleDescriptor");
        }
        return moduleJson;
    }

    private ObjectNode convertDescriptorToJson(ModuleDescriptor descriptor, JsonMapper mapper) {
        ObjectNode descriptorJson = mapper.createObjectNode();
        ArrayNode exportsSetJson = descriptorJson.putArray("exports");
        descriptor.exports().forEach(export -> exportsSetJson.add(convertExportsToJson(export, mapper)));
        ArrayNode requiresSetJson = descriptorJson.putArray("requires");
        descriptor.requires().forEach(requires -> requiresSetJson.add(convertRequiresToJson(requires, mapper)));
        return descriptorJson;
    }

    private ObjectNode convertExportsToJson(ModuleDescriptor.Exports exports, JsonMapper mapper) {
        ObjectNode exportsJson = mapper.createObjectNode();
        exportsJson.put("isQualified", exports.isQualified());
        exportsJson.put("source", exports.source());
        ArrayNode targetsJson = mapper.createArrayNode();
        exports.targets().forEach(targetsJson::add);
        exportsJson.set("targets", targetsJson);
        ArrayNode modifiersJson = mapper.createArrayNode();
        exports.modifiers().forEach(mod -> modifiersJson.add(mod.name()));
        ArrayNode accessFlagsJson = mapper.createArrayNode();
        exports.accessFlags().forEach(flag -> accessFlagsJson.add(flag.name()));
        return exportsJson;
    }

    private ObjectNode convertRequiresToJson(ModuleDescriptor.Requires requires, JsonMapper mapper) {
        ObjectNode requiresJson = mapper.createObjectNode();
        requiresJson.put("name", requires.name());
        requires.compiledVersion().ifPresent(ver -> requiresJson.put("compiledVersion", ver.toString()));
        ArrayNode modifiersJson = mapper.createArrayNode();
        requires.modifiers().forEach(mod -> modifiersJson.add(mod.name()));
        ArrayNode accessFlagsJson = mapper.createArrayNode();
        requires.accessFlags().forEach(flag -> accessFlagsJson.add(flag.name()));
        return requiresJson;
    }
}
