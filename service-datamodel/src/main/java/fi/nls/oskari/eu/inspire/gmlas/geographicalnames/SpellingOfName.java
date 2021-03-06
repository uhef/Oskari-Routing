package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.fe.xml.util.Nillable;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "SpellingOfName")
public class SpellingOfName {

    public String text;
    public Nillable script;
    public Nillable transliterationScheme;

}
