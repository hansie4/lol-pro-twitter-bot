/**
 * The Logger Class is used to log processes or errors throughout the code that 
 * it is used to a file that is passed in when the Logger object is created so 
 * that the user can see what is going on when the program runs.
 * 
 * @author Hans Von Gruenigen
 * @version 1.0
 */
package com.hansvg.lcstwitterbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;

class Logger extends FileWriter {

    PrintWriter printWriter;

    /**
     * Logger Class Constructor.
     * 
     * @param logFile File for the logs to be written to
     * @throws IOException If an input or output exception occurred
     */
    protected Logger(File logFile) throws IOException {
        super(logFile, true);
    }

    /**
     * Creates the PrintWriter object that the logger uses to write formated output
     * to the file.
     */
    protected void open() {
        this.printWriter = new PrintWriter(this);
    }

    /**
     * Logs the passed in Strings to the file.
     * 
     * @param logType     The type of the log(ex. ERROR, TASK)
     * @param stringToLog The string to be logged
     */
    protected void log(String logType, String stringToLog) {
        this.printWriter.printf("%-40s | %s | %s\n", OffsetDateTime.now().toString(), logType, stringToLog);
    }

}