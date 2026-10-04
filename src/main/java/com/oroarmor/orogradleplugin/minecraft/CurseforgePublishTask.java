/*
 * MIT License
 *
 * Copyright (c) 2021 - 2026 OroArmor (Eli Orona)
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

import com.oroarmor.orogradleplugin.GenericExtension;
import com.oroarmor.orogradleplugin.publish.PublishProjectExtension;
import com.oroarmor.orogradleplugin.publish.PublishProjectToLocationTask;
import net.darkhax.curseforgegradle.Constants;
import net.darkhax.curseforgegradle.TaskPublishCurseForge;
import org.gradle.api.tasks.Internal;

public abstract class CurseforgePublishTask extends TaskPublishCurseForge implements PublishProjectToLocationTask {
    @Internal
    private String releaseURL;

    public CurseforgePublishTask() {
        this.setGroup("publishProject");

        this.onlyIf(_unused -> System.getenv("CURSE_API_KEY") != null);

        this.apiToken = System.getenv("CURSE_API_KEY");

        MinecraftPublishingExtension extension = getProject().getExtensions().getByType(MinecraftPublishingExtension.class);
        this.upload(extension.getCurseforgeId().get(), extension.getModTask().get(), artifact -> {
            artifact.changelog = getProject().getExtensions().getByType(PublishProjectExtension.class).getChangelog().get();
            artifact.changelogType = Constants.CHANGELOG_MARKDOWN;

            artifact.displayName = getProject().getExtensions().getByType(GenericExtension.class).getName().get() + " - " + getProject().getVersion();

            artifact.releaseType = switch (extension.getReleaseType().get()) {
                case RELEASE -> "release";
                case BETA -> "beta";
                case ALPHA -> "alpha";
            };

            artifact.addModLoader(extension.getLoaders().get().toArray());

            extension.getDependencies().all(modDependency -> {
                switch (modDependency.getType()) {
                    case REQUIRED -> artifact.addRequirement(modDependency.getName());
                    case OPTIONAL -> artifact.addOptional(modDependency.getName());
                    case INCOMPATIBLE -> artifact.addIncompatibility(modDependency.getName());
                    case EMBEDDED -> artifact.addEmbedded(modDependency.getName());
                    case TOOL -> artifact.addTool(modDependency.getName());
                }
            });
        });

        this.dependsOn(extension.getModTask().get());

        this.doLast(task -> {
            CurseforgePublishTask curseforgeTask = ((CurseforgePublishTask) task);
            releaseURL = "https://www.curseforge.com/minecraft/mc-mods/" + getProject().getExtensions().getByType(GenericExtension.class).getName().get().toLowerCase() + "/files/" + curseforgeTask.getUploadArtifacts().getFirst().getCurseFileId();
        });
    }

    @Override
    public String getReleaseURL() {
        return releaseURL;
    }
}
