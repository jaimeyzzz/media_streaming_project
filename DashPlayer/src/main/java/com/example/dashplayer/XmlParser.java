package com.example.dashplayer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.example.dashplayer.common.VideoFragmentsInfo;
import com.example.dashplayer.common.VideoInfo;
import com.example.dashplayer.common.func;


public class XmlParser 
{
    public VideoInfo parseXmlFromFile(String fi)
    {
    	try {
			InputStream in = new FileInputStream(fi);
			return parseXml(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    private static final String ns = null;
    
    public VideoInfo parseXml(InputStream fi)
    {
    	VideoInfo ret = new VideoInfo();
        VideoFragmentsInfo tmp = null;
        ArrayList<String> tmps = null;
    	try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fi, null);
            parser.nextTag(); 
            
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                    	ret = new VideoInfo();
                    break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if ( name.equals("Representation") ){
                        	tmp = new VideoFragmentsInfo();
                        	tmp.width = Integer.valueOf(parser.getAttributeValue(ns,"width"));
                        	tmp.height = Integer.valueOf(parser.getAttributeValue(ns,"height"));
                        	tmp.bitrate = Integer.valueOf(parser.getAttributeValue(ns,"bandwidth"));
                        	tmps = new ArrayList<String>();
                        } else 
                        if ( name.equals("Url") ) {
                        	tmps.add(parser.getAttributeValue(ns,"sourceURL"));
                        } else
                        if ( name.equals("SegmentInfo")) {
                        	int sec = func.TimeintervalToInt(parser.getAttributeValue(ns,"duration"));
                        	tmp.fragmentDuration = sec;
                        } else
                        if ( name.equals("MPD"))
                        {
                        	ret.duration = func.TimeintervalToInt(parser.getAttributeValue(ns,"mediaPresentationDuration"));
                        }
                   
                    break;
                    case XmlPullParser.END_TAG:
                    	name = parser.getName();
                    	if ( name.equals("Representation") ){
                    		tmp.url = func.objArrToStrArr(tmps.toArray());
                    		ret.add(tmp);
                    		tmp = null;
                    		tmps = null;
                    	}
                    break;
                }
                eventType = parser.next();
            }
            fi.close();
        }
    	catch(Exception e) 	{
        	ret = new VideoInfo();
    	}
        return ret;
    }
    
}
