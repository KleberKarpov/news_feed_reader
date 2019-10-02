package com.example.newsfeedreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ArrayList<NewsItem> news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        news=new ArrayList<>();
    }

    private class GetdataAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            InputStream inputStream=getInputStream();
            try {
                initXMLPullParser(inputStream);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void initXMLPullParser(InputStream inputStream) throws XmlPullParserException, IOException {
        Log.d(TAG, "initXMLPullParser: started");

        XmlPullParser parser= Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
        parser.setInput(inputStream,null);
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG,null,"rss");
        while (parser.next()!=XmlPullParser.END_TAG){
            if(parser.getEventType()!= XmlPullParser.START_TAG){
                continue;
            }

            parser.require(XmlPullParser.START_TAG,null,"channel");
            while(parser.next()!=XmlPullParser.END_TAG){
                if(parser.getEventType()!=XmlPullParser.START_TAG){
                    continue;
                }

                if(parser.getName().equals("item")){
                    parser.require(XmlPullParser.START_TAG,null,"item");

                    String title="";
                    String dscrp="";
                    String link="";
                    String date="";

                    while (parser.next()!=XmlPullParser.END_TAG){
                        if(parser.getEventType()!=XmlPullParser.START_TAG){
                            continue;
                        }

                        String tagName=parser.getName();

                        if(tagName.equals("title")){
                            title=getContent(parser,"title");
                        }
                        else if(tagName.equals("description")){
                            dscrp=getContent(parser,"description");
                        }
                        else if (tagName.equals("link")){
                            link=getContent(parser,"link");
                        }
                        else if(tagName.equals("pubdate")){
                            date=getContent(parser,"pubdate");
                        }
                        else{
                            //TODO:skip the tag
                            skipTag(parser);
                        }

                    }

                    NewsItem item=new NewsItem(title,dscrp,link,date);
                    news.add(item);

                }
                else{
                    //TODO:skip the tag
                    skipTag(parser);
                }
                
            }
        }

    }

    private void skipTag(XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.d(TAG, "skipTag: started");

        if(parser.getEventType()!=XmlPullParser.START_TAG){
            throw new IllegalStateException();
        }

        int num=1;
        while (num!=0){
            switch (parser.next()){
                case XmlPullParser.START_TAG:
                    num++;
                    break;
                case XmlPullParser.END_TAG:
                    num--;
                    break;
                default:break;
            }
        }
    }

    private String getContent(XmlPullParser parser,String tagname){
        Log.d(TAG, "getContent: started");
        String content="";

        try {
            parser.require(XmlPullParser.START_TAG,null,tagname);

            if(parser.next()==XmlPullParser.TEXT){
                content=parser.getText();
                parser.next();
            }
            return content;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return null;
    }

    private InputStream getInputStream(){

        Log.d(TAG, "getInputStream: started");

        URL url= null;
        try {
            url = new URL("");
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            return connection.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
