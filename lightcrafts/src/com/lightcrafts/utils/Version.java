/* Copyright (C) 2005-2011 Fabio Riccardi */

package com.lightcrafts.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/** A container and accessor for static version data, either configured in
  * properties or added to resources at compile time.
  * <p>
  * The compile-time files are generated by the "revision" ant target, which
  * just calls "git log -1", "git config --list" and "cat version.txt" and
  * places the stdout into lightcrafts/resources/com/lightcrafts/app/resources.
  */
public final class Version {

    /**
     * Gets the application's name.
     *
     * @return Returns said name.
     */
    public static String getApplicationName() {
        return m_properties.getString( "app-name" );
    }

    public static URL getApplicationURL() {
        try {
            return new URL( m_properties.getString( "app-URL" ) );
        }
        catch ( MalformedURLException e ) {
            return null;
        }
    }

    public static URL getVideoLearningCenterURL() {
        try {
            return new URL( m_properties.getString( "VideoLearningCenter-URL" ) );
        }
        catch ( MalformedURLException e ) {
            return null;
        }
    }

    public static Map<String, URL> getVideoURLs() {
        Map<String, URL> map = new HashMap<>();
        // TODO put in final video URLs
        map.put("Introduction to Relight", getVideoLearningCenterURL());
        map.put("Advanced Relight", getVideoLearningCenterURL());
        map.put("Introduction to ZoneMapper", getVideoLearningCenterURL());
        map.put("Advanced ZoneMapper", getVideoLearningCenterURL());
        return map;
    }

    /**
     * Gets the Subversion revision number that was current at the time this
     * class was compiled.
     */
    public static String getRevisionNumber() {
        if ( GITInfo == null )
            return "";
        try {
            String text = RevisionPattern.matcher( GITInfo ).replaceAll( "$1" );
            if (text != null && 7 < text.length()) {
                text = text.substring(0,7);
            }
            return text;
        }
        catch ( NumberFormatException e ) {
            return "";
        }
    }

    private static Date getChangeDate() {
        if ( GITInfo == null )
            return null;
        try {
            final String text =
                ChangeDatePattern.matcher( GITInfo ).replaceAll( "$1" );
            return ChangeDateFormat.parse( text );
        }
        catch ( ParseException e ) {
            return null;
        }
    }

    /**
     * Get the URL of the Subversion repository that provided this class.
     */
    public static URI getUri() {
        if (GITInfo == null) {
            return null;
        }
        try {
            String text = UrlPattern.matcher(GITInfo ).replaceAll("$1");
            return new URI(text);
        }
        catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Gets the user-presentable version String.
     */
    @NotNull
    public static String getVersionName() {
        return Version != null ? Version : "";
    }

    /**
     * Reads a given resource.
     *
     * @param name The name of the resource file to read.
     * @return Returns the contents of said resource file as a string.
     */
    private static String readResource( String name ) {
        try {
            final URL url = Version.class.getResource( "resources/" + name );
            if ( url == null ) {
                throw new FileNotFoundException("Revision resource");
            }
            try (InputStream in = url.openStream()) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                final StringBuilder sb = new StringBuilder();
                String line;
                do {
                    line = reader.readLine();
                    if ( line != null ) {
                        sb.append( line );
                        sb.append( '\n' );
                    }
                } while ( line != null );

                return sb.toString();
            }
        }
        catch ( Throwable t ) {
            System.err.println( "Failed to read " + name + " resource" );
        }
        return null;
    }

    /**
     * This is where the actual labels for the tags are.
     */
    private static final ResourceBundle m_properties =
        ResourceBundle.getBundle( "com.lightcrafts.utils.resources.Version" );

    private static DateFormat ChangeDateFormat =
        new SimpleDateFormat("MMM dd HH:mm:ss yyyy Z");

    // The date pattern from "git log -1" looks like
    // "Date:   Sat Dec 22 10:55:47 2012 -0800":
    private static Pattern ChangeDatePattern = Pattern.compile(
        ".*^Date:\\s*... ([^(]+).*",
        Pattern.DOTALL | Pattern.MULTILINE
    );

    /**
     * The output of the <code>git log -1</code> and <code>git config --list</code> command.
     */
    private static String GITInfo;

    private static String Version;  // Contents of "version.txt"

    // The revision number from "git log -1" looks like
    // "commit 268da1ba96c935681e412f1cbb1146666daafd78":
    private static Pattern RevisionPattern = Pattern.compile(
        ".*^commit\\s*([0-9a-fA-F]+).*", Pattern.DOTALL | Pattern.MULTILINE
    );

    // The URL pattern from "git config --list" looks like
    // "remote.origin.url=git@github.com:...":
    private static Pattern UrlPattern = Pattern.compile(
        ".*^remote.origin.url=([^\\s]+).*", Pattern.DOTALL | Pattern.MULTILINE
    );

    static {
        GITInfo = readResource( "Revision" );
        Version = readResource( "Version" );
        if ( Version != null )
            Version = Version.trim();
    }

    ////////// main() for testing /////////////////////////////////////////////

    public static void main( String[] args ) {
        System.out.println( getApplicationName() );
        System.out.println( getUri() );
        System.out.println( getRevisionNumber() );
        System.out.println( getChangeDate() );
    }
}
/* vim:set et sw=4 ts=4: */
