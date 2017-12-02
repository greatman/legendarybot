/*
 * MIT License
 *
 * Copyright (c) Copyright (c) 2017-2017, Greatmancode
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
package com.greatmancode.legendarybot.plugin.setupmodule;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * The setup Handler. Holds the state. The user that initiated it and the channel to send messages in.
 */
public class SetupHandler {

    /**
     * The user that initiated the setup.
     */
    private final User user;

    /**
     * The channel the setup was initiated in.
     */
    private final MessageChannel channel;

    /**
     * The step in the Setup wizard the guild is in.
     */
    private SetupState state = SetupState.STEP_PREFIX;

    /**
     * Value storage.
     */
    private String tempValue;

    public SetupHandler(User user, MessageChannel channel) {
        this.user = user;
        this.channel = channel;
    }

    /**
     * Retrieve the user that initiated the setup
     * @return The {@link User} that initiated the setup.
     */
    public User getUser() {
        return user;
    }

    /**
     * The channel where the setup wizard was initiated in.
     * @return The channel where teh setup was initiated.
     */
    public MessageChannel getChannel() {
        return channel;
    }

    /**
     * Retrieve the state of the setup wizard. What step the user is now in.
     * @return The state of the setup.
     */
    public SetupState getState() {
        return state;
    }

    /**
     * Set the state of the setup wizard.
     * @param state The state of the setup wizard
     */
    public void setState(SetupState state) {
        this.state = state;
    }

    /**
     * Retrieve the value saved.
     * @return The value that was saved.
     */
    public String getTempValue() {
        return tempValue;
    }

    /**
     * Set the value to save temporarely.
     * @param tempValue The value.
     */
    public void setTempValue(String tempValue) {
        this.tempValue = tempValue;
    }
}
