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

package com.oroarmor.orogradleplugin.publish;

import java.io.IOException;

import com.oroarmor.orogradleplugin.GenericExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class PublishProjectToGithubTask extends DefaultTask implements PublishProjectToLocationTask {
    private final ConfigurableFileCollection assets;
    @Internal
    protected String releaseURL = null;

    public PublishProjectToGithubTask() {
        this.setGroup("publishProject");
        this.onlyIf(task -> System.getenv("GITHUB_TOKEN") != null);

        assets = getProject().getObjects().fileCollection();
    }

    @TaskAction
    public void publishToGithub() throws IOException {
        GitHub github = GitHub.connectUsingOAuth(System.getenv("GITHUB_TOKEN"));
        GenericExtension extension = getProject().getExtensions().getByType(GenericExtension.class);
        GHRepository repository = github.getRepository(extension.getGithubProjectName().get());

        GHReleaseBuilder releaseBuilder = new GHReleaseBuilder(repository, getProject().getVersion().toString());
        releaseBuilder.name(extension.getName().get() + " " + getProject().getVersion());
        releaseBuilder.body(getProject().getExtensions().getByType(PublishProjectExtension.class).getChangelog().get());
        releaseBuilder.commitish("master");

        GHRelease release = releaseBuilder.create();
        assets.forEach(file -> {
            try {
                release.uploadAsset(file, "application/java-archive");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        releaseURL = release.getHtmlUrl().toString();
    }

    @InputFiles
    public ConfigurableFileCollection getAssets() {
        return assets;
    }

    @Override
    public String getReleaseURL() {
        return releaseURL;
    }
}
