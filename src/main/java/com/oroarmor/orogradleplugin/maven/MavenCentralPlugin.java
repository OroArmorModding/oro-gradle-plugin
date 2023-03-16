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

package com.oroarmor.orogradleplugin.maven;

import java.util.Map;

import io.codearte.gradle.nexus.NexusStagingExtension;
import io.codearte.gradle.nexus.NexusStagingPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.plugins.signing.Sign;
import org.gradle.plugins.signing.SigningExtension;

public class MavenCentralPlugin implements Plugin<Project> {
    @Override
    @SuppressWarnings("unchecked")
    public void apply(Project target) {
        target.getPluginManager().apply("signing");

        if (target.hasProperty("sign")) {
            target.getExtensions().getByType(PublishingExtension.class).repositories(repositories -> {
                repositories.maven(maven -> {
                    String releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/";
                    String snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/";
                    String url = target.getVersion().toString().endsWith("SNAPSHOT") ? snapshotsRepoUrl : releasesRepoUrl;

                    maven.setUrl(url);
                    maven.credentials(credentials -> {
                        credentials.setUsername("OroArmor");
                        credentials.setPassword(((Map<String, String>) target.getProperties()).getOrDefault("ossrhPassword", System.getenv("OSSRH_PASSWORD")));
                    });

                    maven.setName("MavenCentral");
                });
            });
        }

        target.getTasks().withType(Sign.class, sign -> sign.onlyIf(_unused -> target.hasProperty("sign")));

        if (target.hasProperty("sign")) {
            SigningExtension signingExtension = target.getExtensions().getByType(SigningExtension.class);
            signingExtension.useGpgCmd();
            target.getExtensions().getByType(PublishingExtension.class).getPublications().all(signingExtension::sign);
        }

        target.getPluginManager().apply(NexusStagingPlugin.class);
        NexusStagingExtension stagingExtension = target.getExtensions().getByType(NexusStagingExtension.class);
        stagingExtension.setUsername("OroArmor");
        stagingExtension.setPassword(((Map<String, String>) target.getProperties()).getOrDefault("ossrhPassword", System.getenv("OSSRH_PASSWORD")));
    }
}