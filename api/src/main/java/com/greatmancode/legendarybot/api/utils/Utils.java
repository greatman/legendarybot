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
package com.greatmancode.legendarybot.api.utils;

import com.greatmancode.legendarybot.api.LegendaryBot;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * Generic instance to parse JSON
     */
    public final static JSONParser jsonParser = new JSONParser();

    /**
     * The day Mythic+ started on US servers. Helps calculate which affix is this week
     */
    public final static DateTime startDateMythicPlus = new DateTime(2016,10,20,0,0, DateTimeZone.forID("America/Montreal"));

    //Start date of the Invasion
    private final static DateTime startDateInvasion = new DateTime(2017,4,14,17,0, DateTimeZone.forID("America/Montreal"));

    /**
     * Do a web request that returns a string response
     * @param urlString the URL to query
     * @return A string containing the result, else null
     */
    public static String doRequest(String urlString) {
        return doRequest(urlString, new HashMap<>());
    }

    /**
     * Do a web request that returns a string response
     * @param urlString the URL to query
     * @param headers A list of headers to add to the query
     * @return A string containing the result, else null
     */
    private static String doRequest(String urlString, Map<String, String> headers) {
        return doRequest(urlString,"GET",null,headers);
    }

    private static String doRequest(String urlString, String requestMethod, String body, Map<String, String> headers) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            // By default it is GET request
            con.setRequestMethod(requestMethod);
            con.setRequestProperty("User-Agent", "LegendaryBot 1.0");
            if (body != null) {
                con.setDoOutput(true);
                try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
                    wr.write(body.getBytes());
                }
            }

            int responseCode = con.getResponseCode();

            if (responseCode == 200) {
                // Reading response from input Stream
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String output;
                StringBuilder response = new StringBuilder();

                while ((output = in.readLine()) != null) {
                    response.append(output);
                }
                in.close();
                return response.toString();
            } else {
                System.out.println("Got response code " + responseCode + " from " + urlString);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            LegendaryBot.getRaygunClient().Send(e);
        }
        return null;
    }

}
