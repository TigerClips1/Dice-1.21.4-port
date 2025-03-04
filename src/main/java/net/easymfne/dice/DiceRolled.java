
/*
 * This file is part of the Dice plugin by EasyMFnE.
 *
 * Dice is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * Dice is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU General Public License for details.
 *
 * You should have received a copy of the GNU General Public License v3 along
 * with Dice. If not, see <http://www.gnu.org/licenses/>.
 */
package net.easymfne.dice;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Implements custom DiceRolled event, so other plugins can possibly listen
 * to the result of this plugin's dice throw.
 *
 * @author Martin Ambrus
 */
public final class DiceRolled extends Event {

	private static final HandlerList handlers = new HandlerList();
    private final String message;
    private final Integer[] numbers;

    public DiceRolled(String event, Integer[] rolled) {
        message = event;
        numbers = rolled;
    }

    public String getMessage() {
        return message;
    }

    public Integer[] getNumbersRolled() {
    	return numbers;
    }

    @Override
	public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
