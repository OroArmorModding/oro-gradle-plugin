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
import java.util.List;

import com.oroarmor.orogradleplugin.GenericExtension;
import groovy.lang.Closure;
import net.dumbcode.gradlehook.tasks.form.discord.DiscordEmbed;
import net.dumbcode.gradlehook.tasks.form.discord.DiscordEmbedAuthor;
import net.dumbcode.gradlehook.tasks.form.discord.DiscordEmbedField;
import net.dumbcode.gradlehook.tasks.form.discord.DiscordEmbedImage;
import net.dumbcode.gradlehook.tasks.form.discord.DiscordFormBuilder;
import net.dumbcode.gradlehook.tasks.form.discord.DiscordFormBuilderProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.tasks.TaskAction;

public class PublishProjectTask extends DefaultTask {
    public PublishProjectTask() {
        this.setGroup("publishProject");

        this.dependsOn(getProject().getTasks().withType(PublishProjectToLocationTask.class));
        this.dependsOn(getProject().getTasksByName("publish", true));
    }

    @TaskAction
    public void createWebhook() throws IOException {
        if (System.getenv("DISCORD_WEBHOOK_URL") != null) {
            GenericExtension generic = getProject().getExtensions().getByType(GenericExtension.class);
            PublishProjectExtension publishing = getProject().getExtensions().getByType(PublishProjectExtension.class);

            DiscordFormBuilderProvider.INSTANCE.createForm(new Closure<DiscordFormBuilder>(this) {
                public void doCall(DiscordFormBuilder formBuilder) {
                    formBuilder.setUsername(generic.getName().get());
                    String imgUrl = getProject().property("discord_hook_image_url").toString();
                    formBuilder.setAvatar_url(imgUrl);

                    formBuilder.embed(new Closure<DiscordEmbed>(PublishProjectTask.this) {
                        public void doCall(DiscordEmbed embed) {
                            DiscordEmbedAuthor author = DiscordEmbedAuthor.builder()
                                    .name(generic.getName().get())
                                    .url("https://www.github.com/" + generic.getGithubProjectName().get()).build();
                            embed.setAuthor(author);

                            embed.setTitle(generic.getName().get() + " version " + getProject().getVersion() + " released!");
                            embed.setThumbnail(DiscordEmbedImage.builder().url(imgUrl).build());

                            DiscordEmbedField changes = new DiscordEmbedField();
                            changes.setName("Changes:");
                            changes.setValue(publishing.getChangelog().get());

                            DiscordEmbedField downloads = new DiscordEmbedField();
                            downloads.setName("Downloads:");
                            StringBuilder downloadsString = new StringBuilder();

                            for (PublishProjectToLocationTask task : getProject().getTasks().withType(PublishProjectToLocationTask.class)) {
                                if (task.getReleaseURL() != null) {
                                    downloadsString.append("[")
                                            .append(formatName(task.getName()))
                                            .append("](")
                                            .append(task.getReleaseURL())
                                            .append(")\n");
                                }
                            }

                            for (MavenArtifactRepository repository : getProject().getExtensions().getByType(PublishingExtension.class).getRepositories().withType(MavenArtifactRepository.class)) {
                                if (!repository.getName().equals("MavenLocal")) {
                                    downloadsString.append("[")
                                            .append(repository.getName())
                                            .append("](")
                                            .append(repository.getUrl())
                                            .append(")\n");
                                }
                            }

                            System.out.println(downloadsString.length());
                            downloads.setValue(downloadsString.length() == 0 ? "No downloads detected" : downloadsString.toString());

                            embed.setFields(List.of(changes, downloads));
                        }
                    });
                }
            }).send(System.getenv("DISCORD_WEBHOOK_URL"));
        }
    }

    private static String formatName(String name) {
        StringBuilder formatted = new StringBuilder();
        int lastIndex = 0;
        for(int i = 1; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                formatted.append(Character.toUpperCase(name.charAt(lastIndex))).append(name, lastIndex + 1, i).append(" ");
                lastIndex = i;
            }
        }

        formatted.append(Character.toUpperCase(name.charAt(lastIndex))).append(name.substring(lastIndex + 1));

        return formatted.toString().trim();
    }
}
