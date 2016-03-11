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
package de.adrodoc55.minecraft.mpl.blocks;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;

public abstract class MplBlock {

  protected Coordinate3D coordinate;

  public MplBlock(@Nonnull Coordinate3D coordinate) {
    this.setCoordinate(coordinate);
  }

  @Nonnull
  public Coordinate3D getCoordinate() {
    return coordinate;
  }

  public void setCoordinate(@Nonnull Coordinate3D coordinate) {
    this.coordinate = Preconditions.checkNotNull(coordinate, "coordinate == null!");
  }

  public int getX() {
    return coordinate.getX();
  }

  public int getY() {
    return coordinate.getY();
  }

  public int getZ() {
    return coordinate.getZ();
  }

  @Override
  public String toString() {
    return "MplBlock [coordinate=" + coordinate + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((coordinate == null) ? 0 : coordinate.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MplBlock other = (MplBlock) obj;
    if (coordinate == null) {
      if (other.coordinate != null)
        return false;
    } else if (!coordinate.equals(other.coordinate))
      return false;
    return true;
  }

}