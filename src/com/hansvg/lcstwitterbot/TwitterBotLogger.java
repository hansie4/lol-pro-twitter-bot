package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;

class TwitterBotLogger extends FileWriter {

    PrintWriter printWriter;

    protected TwitterBotLogger(File logFile) throws IOException {
        super(logFile, true);
    }

    protected void open() {
        this.printWriter = new PrintWriter(this);
    }

    protected void log(String logType, String stringToLog) {
        this.printWriter.printf("%s | %s | %s\n", OffsetDateTime.now().toString(), logType, stringToLog);
    }

}