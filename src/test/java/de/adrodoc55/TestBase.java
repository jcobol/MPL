/*
 * MPL (Minecraft Programming Language): A language for easy development of commandblock
 * applications including and IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL (Minecraft Programming Language).
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
 * MPL (Minecraft Programming Language): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, beinhaltet eine IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL (Minecraft Programming Language).
 *
 * MPL ist Freie Software: Sie können es unter den Bedingungen der GNU General Public License, wie
 * von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55;

import java.util.LinkedList;
import java.util.Random;

import net.karneim.pojobuilder.Builder;

public class TestBase {

  private static final Random RANDOM = new Random(5);

  public static int someInt() {
    return RANDOM.nextInt();
  }

  public static int someInt(int bound) {
    return RANDOM.nextInt(bound);
  }

  /**
   * @param lowerBound inclusive
   * @param upperBound inclusive
   * @return
   */
  public static int someInt(int lowerBound, int upperBound) {
    return RANDOM.nextInt(upperBound + 1 - lowerBound) + lowerBound;
  }

  public static int somePositiveInt() {
    return RANDOM.nextInt(Integer.MAX_VALUE);
  }

  public static int few() {
    return someInt(2, 4);
  }

  public static int several() {
    return someInt(5, 10);
  }

  public static int many() {
    return someInt(11, 100);
  }

  public static String someString() {
    return "String" + someInt();
  }

  public static boolean someBoolean() {
    return RANDOM.nextBoolean();
  }

  public static <E extends Enum<E>> E some(Class<E> type) {
    E[] values = type.getEnumConstants();
    return values[someInt(values.length)];
  }

  public static <P> P some(Builder<P> builder) {
    return builder.build();
  }

  public static <P> LinkedList<P> listOf(Builder<P> builder) {
    return listOf(someInt(100), builder);
  }

  public static <P> LinkedList<P> listOf(int count, Builder<P> builder) {
    LinkedList<P> list = new LinkedList<>();
    for (int i = 0; i < count; i++) {
      list.add(builder.build());
    }
    return list;
  }

}
