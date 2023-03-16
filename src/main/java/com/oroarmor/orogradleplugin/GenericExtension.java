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

package com.oroarmor.orogradleplugin;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.MavenPublication;

public class GenericExtension {
    private final Property<String> name, githubProjectName, description;
    private final Property<LicenseType> licenseType;

    public GenericExtension(Project project) {
        name = project.getObjects().property(String.class);
        githubProjectName = project.getObjects().property(String.class);
        description = project.getObjects().property(String.class);

        licenseType = project.getObjects().property(LicenseType.class);
        licenseType.convention(LicenseType.MIT);
    }

    /**
     * @return A property with the Owner/Repo
     */
    public Property<String> getGithubProjectName() {
        return githubProjectName;
    }

    /**
     * @return The project name
     */
    public Property<String> getName() {
        return name;
    }

    /**
     * @return Project description
     */
    public Property<String> getDescription() {
        return description;
    }

    /**
     * @return The LicenseType
     */
    public Property<LicenseType> getLicenseType() {
        return licenseType;
    }

    public void generateDefaultPom(MavenPublication publication) {
        publication.pom(mavenPom -> {
            mavenPom.getName().set(name);
            mavenPom.setPackaging("jar");
            mavenPom.getDescription().set(description);
            mavenPom.getUrl().set("http://github.com/" + githubProjectName.get());

            mavenPom.licenses(mavenPomLicenseSpec -> {
                mavenPomLicenseSpec.license(mavenPomLicense -> {
                    mavenPomLicense.getName().set(licenseType.get().name);
                    mavenPomLicense.getUrl().set(licenseType.get().url);
                });
            });

            mavenPom.developers(mavenPomDeveloperSpec -> {
                mavenPomDeveloperSpec.developer(mavenPomDeveloper -> {
                    mavenPomDeveloper.getName().set("Eli Orona");
                    mavenPomDeveloper.getId().set("OroArmor");
                    mavenPomDeveloper.getEmail().set("eliorona@live.com");
                    mavenPomDeveloper.getUrl().set("oroarmor.com");
                });
            });

            mavenPom.scm(mavenPomScm -> {
                mavenPomScm.getConnection().set("scm:git:git://github.com/" + githubProjectName.get() + ".git");
                mavenPomScm.getDeveloperConnection().set("scm:git:ssh://github.com:" + githubProjectName.get() + ".git");
                mavenPomScm.getUrl().set("https://github.com/" + githubProjectName.get());
            });
        });
    }
}
