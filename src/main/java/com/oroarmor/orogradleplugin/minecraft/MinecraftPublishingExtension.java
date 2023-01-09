/*
 * MIT License
 *
 * Copyright (c) 2021 OroArmor (Eli Orona)
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
import com.oroarmor.orogradleplugin.GenericExtension;
import com.oroarmor.orogradleplugin.publish.PublishProjectExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.jvm.tasks.Jar;

public class MinecraftPublishingExtension {
    private final ListProperty<String> versions, dependencies;
    private final Property<String> modrinthId, curseforgeId, loader;
    private final Property<Jar> modTask;

    public MinecraftPublishingExtension(Project target) {
        versions = target.getObjects().listProperty(String.class);
        dependencies = target.getObjects().listProperty(String.class);

        modrinthId = target.getObjects().property(String.class);
        curseforgeId = target.getObjects().property(String.class);
        loader = target.getObjects().property(String.class);

        modTask = target.getObjects().property(Jar.class);


        target.getExtensions().configure(ModrinthExtension.class, conf -> {
            conf.getToken().set(System.getenv("MODRINTH_TOKEN"));
            conf.getProjectId().set(modrinthId.get());
            conf.getVersionNumber().set(target.getVersion().toString());
            conf.getUploadFile().set(modTask.get());
            this.getVersions().get().forEach(v -> conf.getGameVersions().add(v));
            conf.getLoaders().add(this.getLoader().get());
            conf.getChangelog().set(target.getExtensions().getByType(PublishProjectExtension.class).getChangelog().get());
            conf.getVersionName().set(target.getExtensions().getByType(GenericExtension.class).getName().get() + " - " + conf.getVersionNumber().get());
        });

    }

    public ListProperty<String> getVersions() {
        return versions;
    }

    public ListProperty<String> getDependencies() {
        return dependencies;
    }

    public Property<String> getModrinthId() {
        return modrinthId;
    }

    public Property<String> getCurseforgeId() {
        return curseforgeId;
    }

    public Property<String> getLoader() {
        return loader;
    }

    public Property<Jar> getModTask() {
        return modTask;
    }
}
