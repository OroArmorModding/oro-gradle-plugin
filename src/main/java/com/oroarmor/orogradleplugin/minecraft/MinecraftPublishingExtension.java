/*
 * MIT License
 *
 * Copyright (c) 2021 - 2023 OroArmor (Eli Orona)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oroarmor.orogradleplugin.minecraft;

import com.modrinth.minotaur.ModrinthExtension;
import com.modrinth.minotaur.TaskModrinthSyncBody;
import com.oroarmor.orogradleplugin.GenericExtension;
import com.oroarmor.orogradleplugin.minecraft.dependency.ModDependency;
import com.oroarmor.orogradleplugin.publish.PublishProjectExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.jvm.tasks.Jar;

public class MinecraftPublishingExtension {
    private final ListProperty<String> gameVersions, loaders;
    private final Property<String> modrinthId, curseforgeId;
    private final Property<Jar> modTask;
    private final NamedDomainObjectContainer<ModDependency> dependencies;

    public MinecraftPublishingExtension(Project target) {
        gameVersions = target.getObjects().listProperty(String.class);
        dependencies = target.getObjects().domainObjectContainer(ModDependency.class, ModDependency::new);

        modrinthId = target.getObjects().property(String.class);
        curseforgeId = target.getObjects().property(String.class);
        loaders = target.getObjects().listProperty(String.class);

        modTask = target.getObjects().property(Jar.class);

        // From Minotaur.java
        target.getExtensions().create("modrinth", ModrinthExtension.class, target);
        target.getLogger().debug("Created the `modrinth` extension.");
        target.getTasks().register("modrinthSyncBody", TaskModrinthSyncBody.class, task -> {
            task.setGroup("publishing");
            task.setDescription("Sync project description to Modrinth");
        });
        target.getLogger().debug("Registered the `modrinthSyncBody` task.");
        // End From Minotaur.java

        target.getExtensions().configure(ModrinthExtension.class, conf -> {
            conf.getToken().set(System.getenv("MODRINTH_TOKEN"));
            conf.getProjectId().set(modrinthId);
            conf.getUploadFile().set(modTask);
            conf.getGameVersions().set(gameVersions);
            conf.getAutoAddDependsOn().set(false);
            target.afterEvaluate(project -> {
                conf.getVersionNumber().set(target.getVersion().toString());
                conf.getLoaders().addAll(loaders.get().stream().map(String::toLowerCase).toList());
                conf.getDependencies().set(
                        dependencies.stream().map(ModDependency::toModrinthDependency).toList()
                );
                conf.getChangelog().set(project.getExtensions().getByType(PublishProjectExtension.class).getChangelog());
                conf.getVersionName().set(project.getExtensions().getByType(GenericExtension.class).getName().get() + " - " + conf.getVersionNumber().get());
            });
        });
    }

    public ListProperty<String> getGameVersions() {
        return gameVersions;
    }

    public NamedDomainObjectContainer<ModDependency> getDependencies() {
        return dependencies;
    }

    public Property<String> getModrinthId() {
        return modrinthId;
    }

    public Property<String> getCurseforgeId() {
        return curseforgeId;
    }

    public ListProperty<String> getLoaders() {
        return loaders;
    }

    public Property<Jar> getModTask() {
        return modTask;
    }
}
