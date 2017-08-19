package com.liskovsoft.smartyoutubetv.youtubeinfoparser2.webviewstuff;

import android.util.Xml;
import com.liskovsoft.smartyoutubetv.helpers.Helpers;
import com.liskovsoft.smartyoutubetv.youtubeinfoparser2.YouTubeMediaItem;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyMPDBuilder implements MPDBuilder {
    private final List<YouTubeMediaItem> mVideos;
    private final List<YouTubeMediaItem> mAudios;
    private XmlSerializer mXmlSerializer;
    private StringWriter mWriter;

    public MyMPDBuilder() {
        initXmlSerializer();
        mVideos = new ArrayList<>();
        mAudios = new ArrayList<>();
    }

    private void initXmlSerializer() {
        mXmlSerializer = Xml.newSerializer();
        mWriter = new StringWriter();

        setOutput(mXmlSerializer, mWriter);

        startDocument(mXmlSerializer);
        mXmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        //setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        //setPrefix("yt", "http://youtube.com/yt/2012/10/10");

        startTag("", "MPD");
        attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        attribute("", "xmlns", "urn:mpeg:DASH:schema:MPD:2011");
        attribute("", "xmlns:yt", "http://youtube.com/yt/2012/10/10");
        attribute("", "xsi:schemaLocation", "urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd");
        attribute("", "minBufferTime", "PT1.500S");
        attribute("", "profiles", "urn:mpeg:dash:profile:isoff-on-demand:2011");
        attribute("", "type", "static");
        attribute("", "mediaPresentationDuration", "PT135.650S");


        startTag("", "Period");
        attribute("", "duration", "PT135.650S");
    }

    private void setPrefix(String prefix, String namespace) {
        try {
            mXmlSerializer.setPrefix(prefix, namespace);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XmlSerializer attribute(String namespace, String name, String value) {
        if (value == null) {
            value = "";
        }
        try {
            return mXmlSerializer.attribute(namespace, name, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private XmlSerializer startTag(String namespace, String name) {
        try {
            return mXmlSerializer.startTag(namespace, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setOutput(XmlSerializer xmlSerializer, StringWriter writer) {
        try {
            xmlSerializer.setOutput(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDocument(XmlSerializer xmlSerializer) {
        try {
            xmlSerializer.startDocument("UTF-8", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void endDocument() {
        try {
            mXmlSerializer.endDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void endTag(String namespace, String name) {
        try {
            mXmlSerializer.endTag(namespace, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMediaListPrologue(String id, String mimeType) {
        startTag("", "AdaptationSet");
        attribute("", "id", id);
        attribute("", "mimeType", mimeType);
        attribute("", "subsegmentAlignment", "true");

        startTag("", "Role");
        attribute("", "schemeIdUri", "urn:mpeg:DASH:role:2011");
        attribute("", "value", "main");
        endTag("", "Role");
    }

    @Override
    public void appendVideo(YouTubeMediaItem mediaItem) {
        mVideos.add(mediaItem);
    }

    @Override
    public void appendAudio(YouTubeMediaItem mediaItem) {
        mAudios.add(mediaItem);
    }

    private void writeVideoTags() {
        writeMediaListPrologue("0", "video/mp4"); // TODO: detect codec

        // Representation
        for (YouTubeMediaItem item : mVideos) {
            writeMediaItemTag(item, true);
        }

        endTag("", "AdaptationSet");
    }

    private void writeAudioTags() {
        writeMediaListPrologue("1", "audio/mp4"); // TODO: detect codec

        // Representation
        for (YouTubeMediaItem item : mAudios) {
            writeMediaItemTag(item, false);
        }

        endTag("", "AdaptationSet");
    }

    private void writeMediaItemTag(YouTubeMediaItem item, boolean isVideo) {
        startTag("", "Representation");

        attribute("", "id", item.getITag());
        attribute("", "codecs", extractCodecs(item));
        attribute("", "startWithSAP", "1");
        attribute("", "bandwidth", item.getBitrate());

        if (isVideo) {
            // video attrs
            attribute("", "width", getWidth(item));
            attribute("", "height", getHeight(item));
            attribute("", "maxPlayoutRate", "1");
            attribute("", "frameRate", item.getFps());
        } else {
            // audio attrs
            attribute("", "audioSamplingRate", "44100"); // TODO: get this value somewhere
        }

        startTag("", "BaseURL");

        attribute("", "yt:contentLength", item.getClen());
        text(item.getUrl());

        endTag("", "BaseURL");

        startTag("", "SegmentBase");

        attribute("", "indexRange", item.getIndex());
        attribute("", "indexRangeExact", "true");

        startTag("", "Initialization");

        attribute("", "range", item.getInit());

        endTag("", "Initialization");

        endTag("", "SegmentBase");

        endTag("", "Representation");
    }

    private XmlSerializer text(String url) {
        try {
            return mXmlSerializer.text(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getHeight(YouTubeMediaItem item) {
        String size = item.getSize();
        if (size == null) {
            return "";
        }
        return size.split("x")[1];
    }

    private String getWidth(YouTubeMediaItem item) {
        String size = item.getSize();
        if (size == null) {
            return "";
        }
        return size.split("x")[0];
    }

    private String extractCodecs(YouTubeMediaItem item) {
        // input example: video/mp4;+codecs="avc1.640033"
        Pattern pattern = Pattern.compile(".*codecs=\\\"(.*)\\\"");
        Matcher matcher = pattern.matcher(item.getType());
        matcher.find();
        return matcher.group(1);
    }

    @Override
    public InputStream build() {
        writeAudioTags();
        writeVideoTags();
        endTag("", "Period");
        endTag("", "MPD");
        endDocument();

        return Helpers.toStream(mWriter.toString());
    }
}
