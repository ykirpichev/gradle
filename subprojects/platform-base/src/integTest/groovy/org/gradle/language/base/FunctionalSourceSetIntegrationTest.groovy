/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.base

import org.gradle.api.reporting.model.ModelReportOutput
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.platform.base.internal.registry.LanguageTypeModelRuleExtractor
import spock.lang.Ignore

class FunctionalSourceSetIntegrationTest extends AbstractIntegrationSpec {

    //Adding state to DefaultNodeInitializerRegistry which is a global service
    @Ignore
    def "can not create a top level FSS when the language base plugin has not been applied"() {
        buildFile.text = """
        class Rules extends RuleSource {
            @Model
            void functionalSources(FunctionalSourceSet sources) {
            }
        }
        apply plugin: Rules
        """

        when:
        fails "model"
        new LanguageTypeModelRuleExtractor.DefaultLanguageTypeBuilder().setLanguageName('ruby')

        then:
        failureCauseContains("Declaration of model rule Rules#functionalSources is invalid.")
        failureCauseContains("The model node of type: 'org.gradle.language.base.FunctionalSourceSet' can not be constructed. The type must be managed (@Managed) or one of the following types [ModelSet<?>, ManagedSet<?>, ModelMap<?>, List, Set]")
    }

    def "can create a top level functional source set with a rule"() {
        buildFile << """
        apply plugin: 'language-base'

        class Rules extends RuleSource {
            @Model
            void functionalSources(FunctionalSourceSet sources) {

            }

            @Mutate void printTask(ModelMap<Task> tasks, FunctionalSourceSet sources) {
                tasks.create("printTask") {
                  doLast {
                    println "FunctionalSourceSet: \$sources"
                  }
              }
            }

        }
        apply plugin: Rules
        """

        expect:
        succeeds "printTask"
        output.contains("FunctionalSourceSet: []")
    }

    def "can create a top level functional source set via the model dsl"() {
        buildFile << """
        apply plugin: 'language-base'

        model {
            functionalSources(FunctionalSourceSet)
        }
        """

        expect:
        succeeds "components"
    }

    def "model report renders a functional source set"() {
        buildFile << """
        apply plugin: 'language-base'

        model {
            functionalSources(FunctionalSourceSet)
        }
        """

        when:
        succeeds "model"

        then:
        def modelNode = ModelReportOutput.from(output).modelNode
        modelNode.functionalSources.@creator[0] == "model.functionalSources"
        modelNode.functionalSources.@type[0] == "org.gradle.language.base.FunctionalSourceSet"
        modelNode.functionalSources.@nodeValue[0] == "source set 'functionalSources'"
    }

    def "can define a FunctionalSourceSet property and managed ollection element"() {
        buildFile << """
        apply plugin: 'language-base'

        @Managed
        interface BuildType {
            FunctionalSourceSet getSources()

            FunctionalSourceSet getInputs()
            void setInputs(FunctionalSourceSet sources)

            ModelMap<FunctionalSourceSet> getComponentSources()
            ModelSet<FunctionalSourceSet> getTestSources()
        }

        class Rules extends RuleSource {
            @Model
            void buildType(BuildType buildType) {}

            @Mutate void createHelloTask(ModelMap<Task> tasks, BuildType buildType) {
                tasks.create("echo") {
                  doLast {
                    println "sources: \${buildType.sources}"
                    println "inputs: \${buildType.inputs}"
                    println "componentSources: \${buildType.componentSources}"
                    println "testSources: \${buildType.testSources}"
                  }
                }
            }
        }

        apply plugin: Rules
        """
        expect:
        succeeds "echo"

        and:
        output.contains "sources: null"
        output.contains "inputs: null"
        output.contains "componentSources: ModelMap<FunctionalSourceSet> 'buildType.componentSources'"
        output.contains "testSources: []"

    }
}
