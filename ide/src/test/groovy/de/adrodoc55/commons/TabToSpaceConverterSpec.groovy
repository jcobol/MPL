/*
 * Minecraft Programming Language (MPL): A language for easy development of command block
 * applications including an IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL.
 *
 * MPL is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MPL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MPL. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 *
 * Minecraft Programming Language (MPL): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, inklusive einer IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL.
 *
 * MPL ist freie Software: Sie können diese unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.commons

import static de.adrodoc55.TestBase.$String
import static de.adrodoc55.TestBase.some
import spock.lang.Specification

public class TabToSpaceConverterSpec extends Specification {

  void "String ohne tabs und newlines bleibt gleich"() {
    given:
    String text = some($String())
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(text)
    then:
    result == text
  }

  void "String ohne tabs mit verschiedenen newlines bleibt gleich"() {
    given:
    String text = some($String()) + '\r' +some($String()) + '\n' + some($String()) + '\r\n' + some($String())
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(text)
    then:
    result == text
  }

  void "String mit tab am anfang ohne newlines: tab wird durch tabwidth * spaces ersetzt"() {
    given:
    String string = some($String())
    String text = '\t' + string
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == '     ' + string
  }

  void "String mit tab in index 2 ohne newlines: tab wird durch (tabwidth - 2) * spaces ersetzt"() {
    given:
    String string = some($String())
    String text = 'ab\t' + string
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == 'ab   ' + string
  }

  void "String mit tab am anfang jeder newline: tab wird durch tabwidth * spaces ersetzt"() {
    given:
    String string = some($String())
    String text = '\t' + some($String()) + '\r\t' + some($String()) + '\n\t' + some($String()) + '\r\n\t' + some($String())
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == text.replace('\t', '     ')
  }

  void "String mit tab am anfang einiger newlines: tab wird durch tabwidth * spaces ersetzt"() {
    given:
    String string = some($String())
    String text = '\t' + some($String()) + '\r\t' + some($String()) + '\n\t' + '\r\n'
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == text.replace('\t', '     ')
  }

  void "String mit tab in index 2 in jeder newline: tab wird durch (tabwidth - 2) * spaces ersetzt()"() {
    given:
    String string = some($String())
    String text = 'ab\t' + some($String()) + '\rcd\t' + some($String()) + '\nef\t' + some($String()) + '\rgh\n' + some($String())
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == text.replace('\t', '   ')
  }

  void "String mit tab in index 2 in einigen newlines: tab wird durch (tabwidth - 2) * spaces ersetzt()"() {
    given:
    String string = some($String())
    String text = 'ab\t' + some($String()) + '\rcd\t' + some($String()) + '\nef\t' + '\rgh\n'
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == text.replace('\t', '   ')
  }

  void "String mit 2 aufeinander folgenden tabs wird durch 2 * tabwidth spaces ersetzt"() {
    given:
    String string = some($String())
    String text = '\t\t'
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == '     ' + '     '
  }

  void "String mit 2 indirekt aufeinander folgenden tabs werden korrekt konvertiert"() {
    given:
    String string = some($String())
    String text = 'a\tab\t'
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(5, text)
    then:
    result == 'a    ' + 'ab   '
  }

  void "Offset beeinträchtigt nur für den ersten tab"() {
    given:
    String string = some($String())
    String text = 'a\tab\t'
    when:
    String result = TabToSpaceConverter.convertTabsToSpaces(2, 5, text)
    then:
    result == 'a  ' + 'ab   '
  }
}
