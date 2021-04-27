/*
 * Copyright © 2021 Kif Contributors (https://kif.firkin.io/)
 * Copyright © 2021 Firkin•IO (https://firkin.io/)
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

package io.firkin.kif.script;

import org.jline.console.ScriptEngine;
import org.jline.reader.Completer;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An engine for interpreting Kif Scripts which is a Business-Driven Design (BDD) language with a
 * Gherkin-like syntax supporting Given-When-Then statements for doing stream processing and flow-
 * based programming.
 * <p/>
 * Kif Scripts are implemented as a BDD script, typically by a non-programmer and a JVM-compiled
 * library which supports the semantics of the script. In this way, a domain-specific language
 * may be created by a few programmers can support a large number of analysts in creating a large
 * number of text-based business process rules which reflect the language of the organization in
 * a more friendly (albeit stylized) natural language.
 * <p/>
 * For example, the following kif script might specify a rule for anonymizing any field which
 * contains a Social Security Number from a Kafka topic "CreditApps".
 * <p/>
 * <pre>
 * Rule "ssn cleaner"
 *   Given a stream "credit apps"
 *   When we receive a new "app" with an unsanitized "ssn"
 *   Then ananymize the "ssn" with "***-**-nnnn"
 * </pre>
 */
public class KifEngine implements ScriptEngine {

  public KifEngine() {
    // For inspiration see:
    //   org.jline.script.GroovyEngine    (org.jline.script)
    //   groovy.lang.GroovyShell          (org.codehaus.groovy)
  }

  /**
   * @return scriptEngine name
   */
  @Override
  public String getEngineName() {
    return this.getClass().getSimpleName();
  }

  /**
   * @return script file name extensions
   */
  @Override
  public Collection<String> getExtensions() {
    return Collections.singletonList("kif");
  }

  /**
   * @return script tab completer
   */
  @Override
  public Completer getScriptCompleter() {
    return null;
  }

  /**
   * Tests if console variable exists
   *
   * @param name variable name
   * @return true if variable exists
   */
  @Override
  public boolean hasVariable(String name) {
    return false;
  }

  /**
   * Creates variable
   *
   * @param name  variable name
   * @param value value
   */
  @Override
  public void put(String name, Object value) {

  }

  /**
   * Gets variable value
   *
   * @param name variable name
   * @return value of the variable
   */
  @Override
  public Object get(String name) {
    return null;
  }

  /**
   * Gets all the variables that match the name. Name can contain * wild cards.
   *
   * @param name variable name
   * @return map the variables
   */
  @Override
  public Map<String, Object> find(String name) {
    return null;
  }

  /**
   * Deletes variables. Variable name can contain * wild cards.
   *
   * @param vars variables to be deleted
   */
  @Override
  public void del(String... vars) {

  }

  /**
   * Serialize object to JSON string.
   *
   * @param object object to serialize to JSON
   * @return formatted JSON string
   */
  @Override
  public String toJson(Object object) {
    return null;
  }

  /**
   * Converts object to string.
   *
   * @param object the object
   * @return object string value
   */
  @Override
  public String toString(Object object) {
    return null;
  }

  /**
   * Converts object fields to map.
   *
   * @param object the object
   * @return object fields map
   */
  @Override
  public Map<String, Object> toMap(Object object) {
    return null;
  }

  /**
   * Deserialize value
   *
   * @param value  the value
   * @param format serialization format
   * @return deserialized value
   */
  @Override
  public Object deserialize(String value, String format) {
    return null;
  }

  /**
   * @return Supported serialization formats
   */
  @Override
  public List<String> getSerializationFormats() {
    return null;
  }

  /**
   * @return Supported deserialization formats
   */
  @Override
  public List<String> getDeserializationFormats() {
    return null;
  }

  /**
   * Persists object value to file.
   *
   * @param file   file
   * @param object object
   */
  @Override
  public void persist(Path file, Object object) {

  }

  /**
   * Persists object value to file.
   *
   * @param file   the file
   * @param object the object
   * @param format serialization format
   */
  @Override
  public void persist(Path file, Object object, String format) {

  }

  /**
   * Executes scriptEngine statement
   *
   * @param statement the statement
   * @return result
   * @throws Exception in case of error
   */
  @Override
  public Object execute(String statement) throws Exception {
    return null;
  }

  /**
   * Executes scriptEngine script
   *
   * @param script the script
   * @param args   arguments
   * @return result
   * @throws Exception in case of error
   */
  @Override
  public Object execute(File script, Object[] args) throws Exception {
    return null;
  }

  /**
   * Executes scriptEngine closure
   *
   * @param closure closure
   * @param args    arguments
   * @return result
   */
  @Override
  public Object execute(Object closure, Object... args) {
    return null;
  }
}
