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
package com.greatmancode.legendarybot;

import org.influxdb.InfluxDB;
import org.influxdb.dto.*;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InfluxDBNull implements InfluxDB{
    @Override
    public InfluxDB setLogLevel(LogLevel logLevel) {
        return null;
    }

    @Override
    public InfluxDB enableGzip() {
        return null;
    }

    @Override
    public InfluxDB disableGzip() {
        return null;
    }

    @Override
    public boolean isGzipEnabled() {
        return false;
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit) {
        return null;
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory) {
        return null;
    }

    @Override
    public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory, BiConsumer<Iterable<Point>, Throwable> exceptionHandler) {
        return null;
    }

    @Override
    public void disableBatch() {

    }

    @Override
    public boolean isBatchEnabled() {
        return false;
    }

    @Override
    public Pong ping() {
        return null;
    }

    @Override
    public String version() {
        return null;
    }

    @Override
    public void write(Point point) {

    }

    @Override
    public void write(String records) {

    }

    @Override
    public void write(List<String> records) {

    }

    @Override
    public void write(String database, String retentionPolicy, Point point) {

    }

    @Override
    public void write(int udpPort, Point point) {

    }

    @Override
    public void write(BatchPoints batchPoints) {

    }

    @Override
    public void write(String database, String retentionPolicy, ConsistencyLevel consistency, String records) {

    }

    @Override
    public void write(String database, String retentionPolicy, ConsistencyLevel consistency, List<String> records) {

    }

    @Override
    public void write(int udpPort, String records) {

    }

    @Override
    public void write(int udpPort, List<String> records) {

    }

    @Override
    public QueryResult query(Query query) {
        return null;
    }

    @Override
    public void query(Query query, int chunkSize, Consumer<QueryResult> consumer) {

    }

    @Override
    public QueryResult query(Query query, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public void createDatabase(String name) {

    }

    @Override
    public void deleteDatabase(String name) {

    }

    @Override
    public List<String> describeDatabases() {
        return null;
    }

    @Override
    public boolean databaseExists(String name) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public InfluxDB setConsistency(ConsistencyLevel consistency) {
        return null;
    }

    @Override
    public InfluxDB setDatabase(String database) {
        return null;
    }

    @Override
    public InfluxDB setRetentionPolicy(String retentionPolicy) {
        return null;
    }
}
