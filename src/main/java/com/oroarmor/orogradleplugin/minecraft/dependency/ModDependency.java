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

package com.oroarmor.orogradleplugin.minecraft.dependency;

import com.modrinth.minotaur.dependencies.Dependency;
import org.gradle.api.Named;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ModDependency implements Named, Serializable {
    @Input
    private final String id;
    @Input
    private @Nullable String version;
    @Input
    private DependencyType type = DependencyType.REQUIRED;

    public ModDependency(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DependencyType getType() {
        return type;
    }

    public void setType(DependencyType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return id;
    }

    public Dependency toModrinthDependency() {
        if (this.version == null) {
            return new com.modrinth.minotaur.dependencies.ModDependency(
                    this.id,
                    this.type.toModrinthType());
        }

        return new com.modrinth.minotaur.dependencies.VersionDependency(
                this.id,
                this.version,
                this.type.toModrinthType());
    }

    public enum DependencyType {
        REQUIRED,
        OPTIONAL,
        INCOMPATIBLE,
        EMBEDDED,
        TOOL;

        public com.modrinth.minotaur.dependencies.DependencyType toModrinthType() {
            return switch (this) {
                case REQUIRED -> com.modrinth.minotaur.dependencies.DependencyType.REQUIRED;
                case OPTIONAL, TOOL -> com.modrinth.minotaur.dependencies.DependencyType.OPTIONAL;
                case INCOMPATIBLE -> com.modrinth.minotaur.dependencies.DependencyType.INCOMPATIBLE;
                case EMBEDDED -> com.modrinth.minotaur.dependencies.DependencyType.EMBEDDED;
            };
        }
    }
}
