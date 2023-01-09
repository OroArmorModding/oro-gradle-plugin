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

package com.oroarmor.orogradleplugin;

import com.oroarmor.orogradleplugin.publish.PublishProjectExtension;
import com.oroarmor.orogradleplugin.publish.PublishProjectTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.quiltmc.gradle.licenser.QuiltLicenserGradlePlugin;
import org.quiltmc.gradle.licenser.extension.QuiltLicenserGradleExtension;

public class GenericPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(MavenPublishPlugin.class);

        target.getExtensions().getByType(PublishingExtension.class).repositories(repositories -> {
            if (System.getenv("MAVEN_USERNAME") != null) {
                repositories.maven(maven -> {
                    maven.setName("OroArmor");
                    maven.setUrl("https://maven.oroarmor.com");
                    maven.credentials(credentials -> {
                        credentials.setUsername(System.getenv("MAVEN_USERNAME"));
                        credentials.setPassword(System.getenv("MAVEN_PASSWORD"));
                    });
                });
            }
        });

        target.getExtensions().create("oroarmor", GenericExtension.class, target);

        target.getPluginManager().apply(QuiltLicenserGradlePlugin.class);

        target.getExtensions().configure(QuiltLicenserGradleExtension.class, licenseExtension -> licenseExtension.rule(target.file("LICENSE")));

        target.getExtensions().create("projectPublishing", PublishProjectExtension.class, target);
        target.getTasks().register("publishProject", PublishProjectTask.class);
    }
}
