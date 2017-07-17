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
package com.greatmancode.legendarybot.commands.gif;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.Utils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by lebel on 7/6/2017.
 */
public class GifCommand extends LegendaryBotPlugin implements PublicCommand {

    private Properties props;

    public GifCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    //4bd92fa73acc4d1696705c57363585f7
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        StringBuilder builder = new StringBuilder();
        for(String s : args) {
            builder.append(" ").append(s);
        }
        String url = "http://api.giphy.com/v1/gifs/search?q="+builder.toString()+"&api_key="+props.getProperty("giphy.key")+"&limit=1&rating=pg";
        String result = Utils.doRequest(url);
        try {

            try {
                JSONObject json = (JSONObject) Utils.jsonParser.parse(result);
                JSONArray array = (JSONArray) json.get("data");
                JSONObject obj = (JSONObject) array.get(0);
                String gif = (String) ((JSONObject)((JSONObject)obj.get("images")).get("fixed_height")).get("url");
                event.getChannel().sendMessage(gif).queue();
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                event.getChannel().sendMessage("No gif found for " + builder.toString() + "!").queue();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 99;
    }

    @Override
    public String help() {
        return "gif [Search] - Search for a gif";
    }

    @Override
    public void start() throws PluginException {
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        getBot().getInstance().getCommandHandler().addCommand("gif", this);
    }

    @Override
    public void stop() throws PluginException {
        getBot().getInstance().getCommandHandler().removeCommand("gif");
    }
}
